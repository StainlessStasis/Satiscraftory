package io.github.stainlessstasis.manifold.factory_power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PowerGrid {
    private final Set<GlobalPos> nodes = new HashSet<>();
    private final Map<GlobalPos, Set<GlobalPos>> adjacency = new HashMap<>();

    private final Map<GlobalPos, GlobalPos> parent = new HashMap<>();
    private final Map<GlobalPos, Integer> rank = new HashMap<>();

    private boolean dirty = true;
    private List<PowerNetwork> networks = List.of();
    private final Map<GlobalPos, PowerNetwork> networkByNode = new HashMap<>();

    private final Map<GlobalPos, Double> supplyByNode = new HashMap<>();
    private final Map<GlobalPos, Double> demandByNode = new HashMap<>();
    private final Map<GlobalPos, Double> satisfactionByNode = new HashMap<>();
    private final Map<GlobalPos, Boolean> poweredByNode = new HashMap<>();

    private final Map<GlobalPos, PowerStateListener> consumerListeners = new HashMap<>();

    public interface PowerStateListener {
        void onPowerStateChanged(boolean powered);
    }

    /**
     * Adds a bare node with no connections yet (e.g. a pole placed with nothing wired to it)
     */
    public void addNode(GlobalPos pos) {
        if (nodes.add(pos)) {
            adjacency.put(pos, new HashSet<>());
            registerInUnionFind(pos);
            dirty = true;
        }
    }

    public void registerProducer(GlobalPos pos, double supplyRate) {
        addNode(pos);
        supplyByNode.put(pos, supplyRate);
    }

    public void unregisterProducer(GlobalPos pos) {
        supplyByNode.remove(pos);
    }

    public void registerConsumer(GlobalPos pos, double demandRate, @Nullable PowerStateListener listener) {
        addNode(pos);
        demandByNode.put(pos, demandRate);
        if (listener != null) consumerListeners.put(pos, listener);
        else consumerListeners.remove(pos);
    }

    public void unregisterConsumer(GlobalPos pos) {
        demandByNode.remove(pos);
        consumerListeners.remove(pos);
        poweredByNode.remove(pos);
        satisfactionByNode.remove(pos);
    }

    /**
     * Removes a node and every edge touching it.
     * May split its former network in two, so this triggers a full rebuild.
     * Also clears any supply/demand/listener registration for this node
     */
    public void removeNode(GlobalPos pos) {
        if (!nodes.remove(pos)) return;

        Set<GlobalPos> neighbors = adjacency.remove(pos);
        if (neighbors != null) {
            for (GlobalPos neighbor : neighbors) {
                Set<GlobalPos> reverse = adjacency.get(neighbor);
                if (reverse != null) reverse.remove(pos);
            }
        }
        parent.remove(pos);
        rank.remove(pos);

        supplyByNode.remove(pos);
        demandByNode.remove(pos);
        consumerListeners.remove(pos);
        satisfactionByNode.remove(pos);
        poweredByNode.remove(pos);

        rebuildUnionFindFromEdges();
        dirty = true;
    }

    /**
     *  Connects two nodes, registering either endpoint as a node if needed
     */
    public void addEdge(GlobalPos nodeA, GlobalPos nodeB) {
        if (nodeA.equals(nodeB)) return;
        addNode(nodeA);
        addNode(nodeB);
        adjacency.get(nodeA).add(nodeB);
        adjacency.get(nodeB).add(nodeA);
        union(nodeA, nodeB);
        dirty = true;
    }

    /**
     * Removes the connection between two nodes, if one exists.
     * May split their shared network in two, so this triggers nodeA full rebuild
     */
    public void removeEdge(GlobalPos nodeA, GlobalPos nodeB) {
        Set<GlobalPos> aNeighbors = adjacency.get(nodeA);
        Set<GlobalPos> bNeighbors = adjacency.get(nodeB);
        boolean existed = (aNeighbors != null && aNeighbors.remove(nodeB)) | (bNeighbors != null && bNeighbors.remove(nodeA));
        if (!existed) return;

        rebuildUnionFindFromEdges();
        dirty = true;
    }

    public boolean hasEdge(GlobalPos nodeA, GlobalPos nodeB) {
        Set<GlobalPos> neighbors = adjacency.get(nodeA);
        return neighbors != null && neighbors.contains(nodeB);
    }

    /**
     *  Every connection currently in the grid
     */
    public Set<Edge> getEdges() {
        Set<Edge> allEdges = new HashSet<>();
        for (var adjacencyEntry : adjacency.entrySet()) {
            for (GlobalPos neighborPos : adjacencyEntry.getValue()) {
                allEdges.add(Edge.canonical(adjacencyEntry.getKey(), neighborPos));
            }
        }
        return allEdges;
    }

    /**
     *  A connection between two nodes
     */
    public record Edge(GlobalPos nodeA, GlobalPos nodeB) {
        public static final Codec<Edge> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("nodeA").forGetter(Edge::nodeA),
                GlobalPos.CODEC.fieldOf("nodeB").forGetter(Edge::nodeB)
        ).apply(i, Edge::canonical));

        static Edge canonical(GlobalPos firstNode, GlobalPos secondNode) {
            return firstNode.toString().compareTo(secondNode.toString()) <= 0
                    ? new Edge(firstNode, secondNode)
                    : new Edge(secondNode, firstNode);
        }
    }

    public Set<GlobalPos> getNodes() {
        return Set.copyOf(nodes);
    }

    public Set<GlobalPos> getNeighbors(GlobalPos pos) {
        return Set.copyOf(adjacency.getOrDefault(pos, Set.of()));
    }

    /**
     *  The network a given node currently belongs to, or null if it isn't a registered node
     */
    public @Nullable PowerNetwork networkOf(GlobalPos pos) {
        if (dirty) rebuild();
        return networkByNode.get(pos);
    }

    public List<PowerNetwork> getNetworks() {
        if (dirty) rebuild();
        return networks;
    }

    /**
     * How much of this node's network's demand is currently met, from 0-1
     */
    public double getSatisfaction(GlobalPos pos) {
        return satisfactionByNode.getOrDefault(pos, 0d);
    }

    /**
     *  True if this node's network is fully meeting demand (satisfaction >= 1)
     */
    public boolean isPowered(GlobalPos pos) {
        return poweredByNode.getOrDefault(pos, false);
    }

    /**
     * The supply rate at this node, or 0 if it isn't a producer
     */
    public double getSupply(GlobalPos pos) {
        return supplyByNode.getOrDefault(pos, 0d);
    }

    /**
     * The demand rate at this node, or 0 if it isn't a consumer
     */
    public double getDemand(GlobalPos pos) {
        return demandByNode.getOrDefault(pos, 0d);
    }

    public void tick() {
        for (PowerNetwork network : getNetworks()) {
            double totalSupply = 0;
            double totalDemand = 0;
            for (GlobalPos member : network.getMembers()) {
                totalSupply += supplyByNode.getOrDefault(member, 0d);
                totalDemand += demandByNode.getOrDefault(member, 0d);
            }

            double supplyDemandRatio = totalDemand <= 0 ? 1 : Math.min(1, totalSupply / totalDemand);
            boolean powered = supplyDemandRatio >= 1;

            for (GlobalPos member : network.getMembers()) {
                satisfactionByNode.put(member, supplyDemandRatio);

                if (!demandByNode.containsKey(member)) continue; // only consumers have a powered/unpowered state
                Boolean previous = poweredByNode.put(member, powered);
                if (previous == null || previous != powered) {
                    PowerStateListener listener = consumerListeners.get(member);
                    if (listener != null) listener.onPowerStateChanged(powered);
                }
            }
        }
    }

    /**
     * Clears all nodes, connections, and power registrations from the grid
     */
    public void clear() {
        nodes.clear();
        adjacency.clear();
        parent.clear();
        rank.clear();

        supplyByNode.clear();
        demandByNode.clear();
        satisfactionByNode.clear();
        poweredByNode.clear();
        consumerListeners.clear();

        networkByNode.clear();
        networks = List.of();
        dirty = false;
    }

    private void registerInUnionFind(GlobalPos pos) {
        parent.putIfAbsent(pos, pos);
        rank.putIfAbsent(pos, 0);
    }

    private void rebuildUnionFindFromEdges() {
        parent.clear();
        rank.clear();

        for (GlobalPos pos : nodes) {
            registerInUnionFind(pos);
        }

        for (var entry : adjacency.entrySet()) {
            for (GlobalPos neighbor : entry.getValue()) {
                union(entry.getKey(), neighbor);
            }
        }
    }

    private GlobalPos find(GlobalPos pos) {
        GlobalPos parentOf = parent.get(pos);
        if (parentOf == null) {
            registerInUnionFind(pos);
            return pos;
        }
        if (!parentOf.equals(pos)) {
            GlobalPos root = find(parentOf);
            parent.put(pos, root);
            return root;
        }
        return pos;
    }

    private void union(GlobalPos nodeA, GlobalPos nodeB) {
        GlobalPos rootA = find(nodeA);
        GlobalPos rootB = find(nodeB);
        if (rootA.equals(rootB)) return;

        int rankA = rank.getOrDefault(rootA, 0);
        int rankB = rank.getOrDefault(rootB, 0);

        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }
    }

    private void rebuild() {
        Map<GlobalPos, List<GlobalPos>> groups = new HashMap<>();
        for (GlobalPos pos : nodes) {
            GlobalPos root = find(pos);
            groups.computeIfAbsent(root, _ -> new ArrayList<>()).add(pos);
        }

        List<PowerNetwork> result = new ArrayList<>(groups.size());
        networkByNode.clear();
        for (List<GlobalPos> members : groups.values()) {
            PowerNetwork network = new PowerNetwork(Set.copyOf(members));
            result.add(network);
            for (GlobalPos member : members) networkByNode.put(member, network);
        }

        this.networks = List.copyOf(result);
        this.dirty = false;
    }
}