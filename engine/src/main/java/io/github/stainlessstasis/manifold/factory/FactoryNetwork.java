package io.github.stainlessstasis.manifold.factory;

import io.github.stainlessstasis.manifold.*;
import io.github.stainlessstasis.manifold.factory_component.*;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltLane;
import io.github.stainlessstasis.manifold.factory_component.belt.LanePort;
import io.github.stainlessstasis.manifold.factory_component.consumer.Consumer;
import io.github.stainlessstasis.manifold.factory_component.container.Container;
import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.factory_component.merger.Merger;
import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import io.github.stainlessstasis.manifold.factory_component.splitter.Splitter;
import io.github.stainlessstasis.manifold.network.BeltSyncPacket;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import io.github.stainlessstasis.manifold.util.FactoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class FactoryNetwork extends SavedData {
    private static final int MAX_ENTRIES_PER_PACKET = 150; // for belt syncing

    public static final SavedDataType<FactoryNetwork> TYPE = new SavedDataType<>(
            Manifold.id("factory_network"),
            FactoryNetwork::new,
            Persisted.Snapshot.CODEC.xmap(FactoryNetwork::fromSnapshot, FactoryNetwork::toSnapshot)
    );

    public static final Port NO_OP_PORT = new Port() {
        @Override public boolean canAccept(Payload payload) { return false; }
        @Override public void accept(Payload payload) {}
    };

    private final Scheduler scheduler = new Scheduler();
    private final Map<GlobalPos, Producer> producers = new HashMap<>();
    private final LaneManager laneManager = new LaneManager();
    private final Map<GlobalPos, Consumer> consumers = new HashMap<>();
    private final Map<GlobalPos, Machine> machines = new HashMap<>();
    private final Map<GlobalPos, Container> containers = new HashMap<>();
    private final Map<GlobalPos, Splitter> splitters = new HashMap<>();
    private final Map<GlobalPos, Merger> mergers = new HashMap<>();

    private final Map<GlobalPos, GlobalPos> producerOutputPos = new HashMap<>();
    // keyed by lane UUID rather than GlobalPos since a lane isn't a single position
    private final Map<UUID, GlobalPos> laneOutputPos = new HashMap<>();
    private final Map<GlobalPos, List<GlobalPos>> machineOutputPos = new HashMap<>();
    private final Map<GlobalPos, GlobalPos> containerOutputPos = new HashMap<>();
    private final Map<GlobalPos, List<GlobalPos>> splitterOutputPos = new HashMap<>();
    private final Map<GlobalPos, GlobalPos> mergerOutputPos = new HashMap<>();

    private final Map<ResourceKey<Level>, Map<ChunkPos, List<BeltSyncPacket.Entry>>> pendingBeltSyncsByDimension = new HashMap<>();
    private List<TickTarget> tickOrder = null; // cached; null means needs rebuild
    private interface TickTarget {
        void tick(long currentTick);
    }

    private volatile boolean frozen = false;
    public boolean isFrozen() {
        return frozen;
    }
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public FactoryNetwork() {}

    /**
     * Attached to the Overworld specifically, so there's one shared network
     * across all dimensions rather than a separate one per dimension.
     */
    public static FactoryNetwork get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public LaneManager getLaneManager() {
        return laneManager;
    }

    public Producer getOrCreateProducer(GlobalPos pos, Supplier<Producer> factory) {
        return producers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public @Nullable Producer getProducer(GlobalPos pos) {
        return producers.getOrDefault(pos, null);
    }

    public Consumer getOrCreateConsumer(GlobalPos pos, Supplier<Consumer> factory) {
        return consumers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Machine getOrCreateMachine(GlobalPos pos, Supplier<Machine> factory) {
        return machines.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Container getOrCreateContainer(GlobalPos pos, Supplier<Container> factory) {
        return containers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Splitter getOrCreateSplitter(GlobalPos pos, Supplier<Splitter> factory) {
        return splitters.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Merger getOrCreateMerger(GlobalPos pos, Supplier<Merger> factory) {
        return mergers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Port getPortAt(GlobalPos pos, @Nullable Direction fromDirection) {
        BeltLane lane = laneManager.laneAt(pos);
        if (lane != null) return new LanePort(laneManager, pos);

        Consumer consumer = consumers.get(pos);
        if (consumer != null) {
            return (fromDirection == null || consumer.acceptsFrom(fromDirection)) ? consumer : null;
        }

        Container container = containers.get(pos);
        if (container != null) return container;

        Machine machine = machines.get(pos);
        if (machine != null && fromDirection != null) return machine.inputPortForFace(fromDirection.getOpposite());

        Merger merger = mergers.get(pos);
        if (merger != null && fromDirection != null) return merger.inputPortForFace(fromDirection.getOpposite());

        Splitter splitter = splitters.get(pos);
        if (splitter != null) return splitter.inputPort;

        return null;
    }

    // --- belt lanes ---

    @SuppressWarnings("UnusedReturnValue")
    public BeltLane attachBeltBlock(
            GlobalPos pos, double speed, double minGap, @Nullable GlobalPos inputNeighbor, @Nullable GlobalPos outputNeighbor
    ) {
        BeltLane lane = laneManager.attachBlock(pos, speed, minGap, inputNeighbor, outputNeighbor);
        setDirty();
        return lane;
    }

    public void linkLaneOutput(GlobalPos beltPos, GlobalPos outputPos, Direction outputDirection) {
        BeltLane lane = laneManager.laneAt(beltPos);
        if (lane == null || !beltPos.equals(lane.tailBlock())) return;

        Port port = getPortAt(outputPos, outputDirection);
        Port resolved = (port != null) ? port : NO_OP_PORT;
        boolean unchanged = resolved.equals(lane.getOutput()) && outputPos.equals(laneOutputPos.get(lane.getId()));
        if (unchanged) return;

        lane.setOutput(resolved);
        laneOutputPos.put(lane.getId(), outputPos);
        setDirty();
    }

    /**
     * @param gapFillerPort The Port which fills the gap left by the belt.
     *                      Will eventually be used for splitter and merger inlining (like in Satisfactory)
     */
    public void detachBeltBlock(ServerLevel level, GlobalPos pos, Port gapFillerPort) {
        LaneManager.LaneReference ref = laneManager.getReference(pos);
        UUID originalLaneId = (ref != null) ? ref.laneId() : null;

        laneManager.detachBlock(pos, gapFillerPort, (removedPos, ejected) -> dropEjectedItems(level, removedPos, ejected));

        if (originalLaneId != null) {
            laneOutputPos.remove(originalLaneId);
        }
        setDirty();
    }

    private void dropEjectedItems(ServerLevel level, GlobalPos removedPos, List<BeltLane.BeltItem> items) {
        if (Config.BELTS_DROP_ITEMS.isFalse()) return;

        BlockPos blockPos = removedPos.pos();
        for (BeltLane.BeltItem item : items) {
            Payload payload = item.getPayload();
            ItemStack stack = PayloadItems.toItemStack(payload.itemId(), payload.count());
            if (stack == null) continue;
            ItemEntity entity = new ItemEntity(
                    level, blockPos.getX() + 0.5, blockPos.getY() + 0.67, blockPos.getZ() + 0.5, stack);
            level.addFreshEntity(entity);
        }
    }

    // --- ok no more belt lanes ---

    public void linkProducerOutput(GlobalPos producerPos, GlobalPos outputPos, Direction outputDirection) {
        Producer producer = producers.get(producerPos);
        Port port = getPortAt(outputPos, outputDirection);
        if (producer != null && port != null) {
            producer.setOutput(port);
            producerOutputPos.put(producerPos, outputPos);
            setDirty();
        }
    }

    public void linkContainerOutput(GlobalPos containerPos, GlobalPos outputPos, Direction outputDirection) {
        Container container = containers.get(containerPos);
        Port port = getPortAt(outputPos, outputDirection);
        if (container == null || port == null) return;

        container.setOutput(port);
        containerOutputPos.put(containerPos, outputPos);
        setDirty();
    }

    public void linkMachineOutput(GlobalPos machinePos, int slotIndex, GlobalPos outputPos, Direction outputDirection) {
        Machine machine = machines.get(machinePos);
        Port port = getPortAt(outputPos, outputDirection);
        if (machine == null || port == null) return;

        machine.setOutputPort(slotIndex, port);
        machineOutputPos.computeIfAbsent(machinePos, _ -> new ArrayList<>(machine.outputSlotCount()));
        List<GlobalPos> slots = machineOutputPos.get(machinePos);
        while (slots.size() <= slotIndex) slots.add(null);
        slots.set(slotIndex, outputPos);
        setDirty();
    }

    public void linkSplitterOutput(GlobalPos splitterPos, int slotIndex, GlobalPos outputPos, Direction outputDirection) {
        Splitter splitter = splitters.get(splitterPos);
        Port port = getPortAt(outputPos, outputDirection);
        if (splitter == null || port == null) return;

        splitter.setOutput(slotIndex, port);
        splitterOutputPos.computeIfAbsent(splitterPos, _ -> new ArrayList<>(Splitter.MAX_OUTPUTS));
        List<GlobalPos> slots = splitterOutputPos.get(splitterPos);
        while (slots.size() <= slotIndex) slots.add(null);
        slots.set(slotIndex, outputPos);
        setDirty();
    }

    public void linkMergerOutput(GlobalPos mergerPos, GlobalPos outputPos, Direction outputDirection) {
        Merger merger = mergers.get(mergerPos);
        Port port = getPortAt(outputPos, outputDirection);
        if (merger == null || port == null) return;

        merger.setOutput(port);
        mergerOutputPos.put(mergerPos, outputPos);
        setDirty();
    }

    public void removeProducer(GlobalPos pos) {
        Producer producer = producers.remove(pos);
        if (producer != null) {
            producer.cancelScheduledTask();
            producerOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeConsumer(GlobalPos pos) {
        clearReferencesTo(pos);
        if (consumers.remove(pos) != null) {
            setDirty();
        }
    }

    public void removeMachine(GlobalPos pos) {
        clearReferencesTo(pos);
        Machine machine = machines.remove(pos);
        if (machine != null) {
            machine.cancelScheduledTask();
            machineOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeContainer(GlobalPos pos) {
        clearReferencesTo(pos);
        if (containers.remove(pos) != null) {
            containerOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeSplitter(GlobalPos pos) {
        clearReferencesTo(pos);
        if (splitters.remove(pos) != null) {
            splitterOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeMerger(GlobalPos pos) {
        clearReferencesTo(pos);
        if (mergers.remove(pos) != null) {
            mergerOutputPos.remove(pos);
            setDirty();
        }
    }

    private void clearReferencesTo(GlobalPos removedPos) {
        laneOutputPos.entrySet().removeIf(entry -> {
            if (!removedPos.equals(entry.getValue())) return false;
            BeltLane lane = laneManager.getLane(entry.getKey());
            if (lane != null) lane.setOutput(NO_OP_PORT);
            return true;
        });

        for (var entry : producerOutputPos.entrySet()) {
            if (removedPos.equals(entry.getValue())) {
                Producer producer = producers.get(entry.getKey());
                if (producer != null) producer.setOutput(NO_OP_PORT);
            }
        }
        producerOutputPos.entrySet().removeIf(e -> removedPos.equals(e.getValue()));

        for (var entry : containerOutputPos.entrySet()) {
            if (removedPos.equals(entry.getValue())) {
                Container container = containers.get(entry.getKey());
                if (container != null) container.setOutput(NO_OP_PORT);
            }
        }
        containerOutputPos.entrySet().removeIf(e -> removedPos.equals(e.getValue()));

        for (var entry : machineOutputPos.entrySet()) {
            List<GlobalPos> slots = entry.getValue();
            for (int i = 0; i < slots.size(); i++) {
                if (removedPos.equals(slots.get(i))) {
                    Machine machine = machines.get(entry.getKey());
                    if (machine != null) machine.setOutputPort(i, NO_OP_PORT);
                    slots.set(i, null);
                }
            }
        }

        for (var entry : splitterOutputPos.entrySet()) {
            List<GlobalPos> slots = entry.getValue();
            for (int i = 0; i < slots.size(); i++) {
                if (removedPos.equals(slots.get(i))) {
                    Splitter splitter = splitters.get(entry.getKey());
                    if (splitter != null) splitter.setOutput(i, NO_OP_PORT);
                    slots.set(i, null);
                }
            }
        }

        for (var entry : mergerOutputPos.entrySet()) {
            if (removedPos.equals(entry.getValue())) {
                Merger merger = mergers.get(entry.getKey());
                if (merger != null) merger.setOutput(NO_OP_PORT);
            }
        }
        mergerOutputPos.entrySet().removeIf(e -> removedPos.equals(e.getValue()));
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        tickOrder = null;
    }

    public void tickAll(ServerLevel level, long currentTick) {
        scheduler.tick(currentTick);

        if (tickOrder == null) tickOrder = computeTickOrder();
        for (TickTarget target : tickOrder) target.tick(currentTick);

        for (var chunkMap : pendingBeltSyncsByDimension.values())
            for (var list : chunkMap.values()) list.clear();

        for (BeltLane lane : laneManager.getAllLanes().values()) {
            if (!lane.hasUnsyncedChanges(currentTick)) continue;

            GlobalPos headPos = lane.headBlock();
            ResourceKey<Level> dim = headPos.dimension();
            ServerLevel laneLevel = level.getServer().getLevel(dim);
            if (laneLevel == null) {
                lane.markSynced(currentTick);
                continue;
            }

            BlockPos pos = headPos.pos();
            ChunkPos chunkPos = ChunkPos.containing(pos);

            if (laneLevel.getChunkSource().chunkMap.getPlayers(chunkPos, false).isEmpty()) {
                lane.markSynced(currentTick);
                continue;
            }

            List<GlobalPos> blocks = lane.getBlocks();
            List<BlockPos> blockPositions = new ArrayList<>(blocks.size());
            for (GlobalPos globalPos : blocks) blockPositions.add(globalPos.pos());

            BeltSyncPacket.Entry entry = new BeltSyncPacket.Entry(
                    pos, blockPositions, currentTick, lane.getItemSnapshots(), lane.isFrontJammed()
            );

            pendingBeltSyncsByDimension
                    .computeIfAbsent(dim, _ -> new HashMap<>())
                    .computeIfAbsent(chunkPos, _ -> new ArrayList<>())
                    .add(entry);

            lane.markSynced(currentTick);
        }

        for (var dimEntry : pendingBeltSyncsByDimension.entrySet()) {
            ServerLevel targetLevel = level.getServer().getLevel(dimEntry.getKey());
            if (targetLevel == null) continue;

            for (var chunkEntry : dimEntry.getValue().entrySet()) {
                List<BeltSyncPacket.Entry> entries = chunkEntry.getValue();
                if (entries.isEmpty()) continue;

                for (int start = 0; start < entries.size(); start += MAX_ENTRIES_PER_PACKET) {
                    int end = Math.min(start + MAX_ENTRIES_PER_PACKET, entries.size());
                    BeltSyncPacket payload = new BeltSyncPacket(entries.subList(start, end));
                    PacketDistributor.sendToPlayersTrackingChunk(targetLevel, chunkEntry.getKey(), payload);
                }
            }
        }
    }

    private List<TickTarget> computeTickOrder() {
        Set<Object> visited = new HashSet<>();
        List<Object> order = new ArrayList<>();

        Set<Object> allNodes = new LinkedHashSet<>();
        allNodes.addAll(producers.keySet());
        allNodes.addAll(laneManager.getAllLanes().keySet());
        allNodes.addAll(machines.keySet());
        allNodes.addAll(containers.keySet());
        allNodes.addAll(consumers.keySet());
        allNodes.addAll(splitters.keySet());
        allNodes.addAll(mergers.keySet());

        for (Object start : allNodes) {
            if (!visited.contains(start)) visitIterative(start, order, visited);
        }

        List<TickTarget> resolved = new ArrayList<>(order.size());
        for (Object node : order) {
            if (node instanceof UUID laneId) {
                BeltLane lane = laneManager.getLane(laneId);
                if (lane != null) resolved.add(lane::tick);
                continue;
            }
            GlobalPos pos = (GlobalPos) node;
            Producer producer = producers.get(pos);
            if (producer != null) { resolved.add(producer::tick); continue; }
            Splitter splitter = splitters.get(pos);
            if (splitter != null) continue; // splitters don't tick
            Merger merger = mergers.get(pos);
            if (merger != null) { resolved.add(merger::tick); continue; }
            Machine machine = machines.get(pos);
            if (machine != null) { resolved.add(machine::tick); continue; }
            Container container = containers.get(pos);
            if (container != null) { resolved.add(container::tick); continue; }
            Consumer consumer = consumers.get(pos);
            if (consumer != null) resolved.add(consumer::tick);
        }
        return resolved;
    }

    private static boolean hasTrackingPlayers(ServerLevel level, ChunkPos chunkPos) {
        ServerChunkCache chunkCache = level.getChunkSource();
        if (!chunkCache.hasChunk(chunkPos.x(), chunkPos.z())) return false;
        return !chunkCache.chunkMap.getPlayers(chunkPos, false).isEmpty();
    }

    private @Nullable Object resolveNode(GlobalPos pos) {
        LaneManager.LaneReference ref = laneManager.getReference(pos);
        if (ref != null) return ref.laneId();
        if (       producers.containsKey(pos)
                || machines.containsKey(pos)
                || containers.containsKey(pos)
                || consumers.containsKey(pos)
                || splitters.containsKey(pos)
                || mergers.containsKey(pos)
        ) {
            return pos;
        }
        return null;
    }

    private List<Object> outputsOf(Object node) {
        if (node instanceof UUID laneId) {
            GlobalPos out = laneOutputPos.get(laneId);
            if (out == null) return List.of();
            Object resolved = resolveNode(out);
            return resolved != null ? List.of(resolved) : List.of();
        }

        GlobalPos pos = (GlobalPos) node;
        if (producers.containsKey(pos)) {
            GlobalPos out = producerOutputPos.get(pos);
            return out != null ? List.of(out) : List.of();
        }

        if (containers.containsKey(pos)) {
            GlobalPos out = containerOutputPos.get(pos);
            return out != null ? List.of(out) : List.of();
        }

        if (machines.containsKey(pos)) {
            List<GlobalPos> outs = machineOutputPos.get(pos);
            if (outs == null || outs.isEmpty()) return List.of();

            List<Object> resolvedOutputs = new ArrayList<>();
            for (GlobalPos outPos : outs) {
                if (outPos != null) {
                    Object resolved = resolveNode(outPos);
                    if (resolved != null) {
                        resolvedOutputs.add(resolved);
                    }
                }
            }
            return resolvedOutputs;
        }

        if (splitters.containsKey(pos)) {
            List<GlobalPos> outs = splitterOutputPos.get(pos);
            if (outs == null || outs.isEmpty()) return List.of();

            List<Object> resolvedOutputs = new ArrayList<>();
            for (GlobalPos outPos : outs) {
                if (outPos != null) {
                    Object resolved = resolveNode(outPos);
                    if (resolved != null) resolvedOutputs.add(resolved);
                }
            }
            return resolvedOutputs;
        }

        if (mergers.containsKey(pos)) {
            GlobalPos out = mergerOutputPos.get(pos);
            if (out == null) return List.of();
            Object resolved = resolveNode(out);
            return resolved != null ? List.of(resolved) : List.of();
        }

        return List.of();
    }

    private void visitIterative(Object start, List<Object> order, Set<Object> visited) {
        Deque<Object> stack = new ArrayDeque<>();
        Deque<Iterator<Object>> pendingOutputs = new ArrayDeque<>();
        Set<Object> onStack = new HashSet<>();

        stack.push(start);
        onStack.add(start);
        pendingOutputs.push(outputsOf(start).iterator());

        while (!stack.isEmpty()) {
            Object node = stack.peek();
            Iterator<Object> outIter = pendingOutputs.peek();

            boolean pushedChild = false;
            while (outIter != null && outIter.hasNext()) {
                Object next = outIter.next();
                if (!visited.contains(next) && !onStack.contains(next)) {
                    stack.push(next);
                    onStack.add(next);
                    pendingOutputs.push(outputsOf(next).iterator());
                    pushedChild = true;
                    break;
                }
            }
            if (pushedChild) continue;

            stack.pop();
            pendingOutputs.pop();
            onStack.remove(node);
            visited.add(node);
            order.add(node);
        }
    }


    private Persisted.Snapshot toSnapshot() {
        List<Persisted.Producer> persistedProducers = new ArrayList<>();
        for (Map.Entry<GlobalPos, Producer> entry : producers.entrySet()) {
            GlobalPos pos = entry.getKey();
            Producer producer = entry.getValue();
            Payload pending = producer.getPending();
            persistedProducers.add(new Persisted.Producer(
                    pos, producer.getItemId(), producer.getInterval(),
                    Optional.ofNullable(producerOutputPos.get(pos)),
                    producer.isActive(),
                    Optional.ofNullable(pending == null ? null : pending.itemId()),
                    producer.getNextProductionTick()
            ));
        }

        List<Persisted.BeltLane> persistedLanes = new ArrayList<>();
        for (BeltLane lane : laneManager.getAllLanes().values()) {
            List<GlobalPos> blocks = lane.getBlocks();
            List<Persisted.BeltItem> items = new ArrayList<>();
            for (BeltLane.ItemSnapshot snapshot : lane.getItemSnapshots()) {
                items.add(new Persisted.BeltItem(snapshot.id(), snapshot.position(), snapshot.itemId()));
            }
            persistedLanes.add(new Persisted.BeltLane(lane.getId(), blocks, lane.getSpeed(), lane.getMinGap(),
                    Optional.ofNullable(laneOutputPos.get(lane.getId())), items));
        }

        List<Persisted.Consumer> persistedConsumers = new ArrayList<>();
        for (Map.Entry<GlobalPos, Consumer> entry : consumers.entrySet()) {
            GlobalPos pos = entry.getKey();
            Consumer consumer = entry.getValue();
            persistedConsumers.add(new Persisted.Consumer(
                    pos, consumer.getCapacity(), consumer.getProcessTime(), consumer.getBufferedItemIds(),
                    Optional.ofNullable(consumer.getProcessingItemId()), consumer.getProcessStartTick(), consumer.getConsumedCount())
            );
        }

        List<Persisted.Machine> persistedMachines = new ArrayList<>();
        for (Map.Entry<GlobalPos, Machine> entry : machines.entrySet()) {
            GlobalPos pos = entry.getKey();
            Machine machine = entry.getValue();

            Map<Integer, GlobalPos> outputPositions = new HashMap<>();
            List<GlobalPos> slots = machineOutputPos.get(pos);
            if (slots != null) {
                for (int i = 0; i < slots.size(); i++) {
                    GlobalPos outPos = slots.get(i);
                    if (outPos != null) outputPositions.put(i, outPos);
                }
            }

            persistedMachines.add(new Persisted.Machine(
                    pos, machine.getRecipe().id(), machine.getBufferMultiplier(),
                    machine.isCrafting(), machine.getCraftCompletionTick(),
                    machine.getBufferedCounts(), machine.getPendingOutputItemIds(),
                    machine.getInputFaceAssignments(), machine.getOutputFaceAssignments(),
                    outputPositions
            ));
        }

        List<Persisted.Container> persistedContainers = new ArrayList<>();
        for (Map.Entry<GlobalPos, Container> entry : containers.entrySet()) {
            GlobalPos pos = entry.getKey();
            Container container = entry.getValue();
            List<Persisted.ContainerSlot> slotData = new ArrayList<>();
            for (Payload payload : container.snapshotSlots()) {
                slotData.add(payload == null
                        ? Persisted.ContainerSlot.EMPTY
                        : new Persisted.ContainerSlot(Optional.of(payload.itemId()), payload.count()));
            }
            persistedContainers.add(new Persisted.Container(
                    pos, container.getSlotCount(), slotData, Optional.ofNullable(containerOutputPos.get(pos)))
            );
        }

        List<Persisted.Splitter> persistedSplitters = new ArrayList<>();
        for (Map.Entry<GlobalPos, Splitter> entry : splitters.entrySet()) {
            GlobalPos pos = entry.getKey();
            Splitter splitter = entry.getValue();
            Map<Integer, GlobalPos> outputPositions = new HashMap<>();
            List<GlobalPos> slots = splitterOutputPos.get(pos);
            if (slots != null) {
                for (int i = 0; i < slots.size(); i++) {
                    GlobalPos outPos = slots.get(i);
                    if (outPos != null) outputPositions.put(i, outPos);
                }
            }
            persistedSplitters.add(new Persisted.Splitter(
                    pos, splitter.getNextOutputIndex(), splitter.getOutputFaceAssignments(), outputPositions)
            );
        }

        List<Persisted.Merger> persistedMergers = new ArrayList<>();
        for (Map.Entry<GlobalPos, Merger> entry : mergers.entrySet()) {
            GlobalPos pos = entry.getKey();
            Merger merger = entry.getValue();
            Identifier[] itemIds = merger.getBufferedItemIds();
            Map<Integer, Identifier> bufferedItemIds = new HashMap<>();
            for (int i = 0; i < itemIds.length; i++) {
                if (itemIds[i] != null) bufferedItemIds.put(i, itemIds[i]);
            }
            persistedMergers.add(new Persisted.Merger(
                    pos, merger.getNextInputIndex(), merger.getInputFaceAssignments(), bufferedItemIds,
                    Optional.ofNullable(mergerOutputPos.get(pos)))
            );
        }

        return new Persisted.Snapshot(persistedProducers, persistedLanes, persistedConsumers, persistedMachines, persistedContainers, persistedSplitters, persistedMergers);
    }

    private static FactoryNetwork fromSnapshot(Persisted.Snapshot snapshot) {
        FactoryNetwork network = new FactoryNetwork();

        // restore factory components
        for (Persisted.BeltLane laneData : snapshot.belts()) {
            BeltLane lane = new BeltLane(laneData.id(), laneData.blocks(), laneData.speed(), laneData.minGap());
            for (Persisted.BeltItem item : laneData.items()) lane.restoreItem(item.itemId(), item.position(), item.id());
            network.laneManager.restoreLane(lane);
            laneData.outputPos().ifPresent(outPos -> network.laneOutputPos.put(laneData.id(), outPos));
        }

        for (Persisted.Consumer consumerData : snapshot.consumers()) {
            Consumer consumer = Consumer.restore(
                    consumerData.capacity(), consumerData.processTime(), consumerData.bufferedItemIds(),
                    consumerData.processingItemId().orElse(null), consumerData.processStartTick(), consumerData.consumedCount()
            );
            network.consumers.put(consumerData.pos(), consumer);
        }

        for (Persisted.Producer producerData : snapshot.producers()) {
            Payload pending = producerData.pendingItemId().map(Payload::new).orElse(null);
            Producer producer = Producer.restore(
                    producerData.itemType(), producerData.interval(), NO_OP_PORT, network.scheduler,
                    producerData.active(), pending, producerData.nextProductionTick()
            );
            network.producers.put(producerData.pos(), producer);
            producerData.outputPos().ifPresent(outPos -> network.producerOutputPos.put(producerData.pos(), outPos));
        }

        for (Persisted.Machine machineData : snapshot.machines()) {
            MachineRecipe recipe = ManifoldRecipes.get(machineData.recipeId());
            if (recipe == null) continue;

            List<Port> outputPorts = new ArrayList<>();
            for (int i = 0; i < recipe.outputCount(); i++) outputPorts.add(NO_OP_PORT);

            Machine machine = Machine.restore(recipe, network.scheduler, outputPorts, machineData.bufferMultiplier(),
                    machineData.crafting(), machineData.craftCompletionTick(),
                    machineData.bufferedCounts(), machineData.pendingOutputItemIds(),
                    machineData.inputFaces(), machineData.outputFaces());
            network.machines.put(machineData.pos(), machine);

            if (!machineData.outputPos().isEmpty()) {
                List<GlobalPos> slots = new ArrayList<>();
                while (slots.size() < recipe.outputCount()) slots.add(null);
                for (Map.Entry<Integer, GlobalPos> outEntry : machineData.outputPos().entrySet()) {
                    int slotIndex = outEntry.getKey();
                    if (slotIndex >= 0 && slotIndex < slots.size()) slots.set(slotIndex, outEntry.getValue());
                }
                network.machineOutputPos.put(machineData.pos(), slots);
            }
        }

        for (Persisted.Container containerData : snapshot.containers()) {
            List<Payload> slotPayloads = new ArrayList<>();
            for (Persisted.ContainerSlot slot : containerData.slots()) {
                slotPayloads.add(slot.itemId().isPresent() && slot.count() > 0
                        ? new Payload(slot.itemId().get(), slot.count())
                        : null);
            }
            Container container = Container.restore(containerData.slotCount(), slotPayloads);
            network.containers.put(containerData.pos(), container);
            containerData.outputPos().ifPresent(outPos -> network.containerOutputPos.put(containerData.pos(), outPos));
        }

        for (Persisted.Splitter splitterData : snapshot.splitters()) {
            Splitter splitter = Splitter.restore(splitterData.nextOutputIndex(), splitterData.outputFaces());
            network.splitters.put(splitterData.pos(), splitter);

            if (!splitterData.outputPos().isEmpty()) {
                List<GlobalPos> slots = new ArrayList<>();
                while (slots.size() < Splitter.MAX_OUTPUTS) slots.add(null);
                for (Map.Entry<Integer, GlobalPos> outEntry : splitterData.outputPos().entrySet()) {
                    int slotIndex = outEntry.getKey();
                    if (slotIndex >= 0 && slotIndex < slots.size()) slots.set(slotIndex, outEntry.getValue());
                }
                network.splitterOutputPos.put(splitterData.pos(), slots);
            }
        }

        for (Persisted.Merger mergerData : snapshot.mergers()) {
            Identifier[] itemIds = new Identifier[Merger.MAX_INPUTS];
            for (Map.Entry<Integer, Identifier> entry : mergerData.bufferedItemIds().entrySet()) {
                int index = entry.getKey();
                if (index >= 0 && index < itemIds.length) itemIds[index] = entry.getValue();
            }
            Merger merger = Merger.restore(mergerData.nextInputIndex(), mergerData.inputFaces(), itemIds);
            network.mergers.put(mergerData.pos(), merger);
            mergerData.outputPos().ifPresent(outPos -> network.mergerOutputPos.put(mergerData.pos(), outPos));
        }

        // relink outputs
        for (Map.Entry<UUID, GlobalPos> entry : network.laneOutputPos.entrySet()) {
            BeltLane lane = network.laneManager.getLane(entry.getKey());
            if (lane == null) continue;
            GlobalPos outPos = entry.getValue();
            Direction dir = FactoryUtils.getOutputDirection(lane.tailBlock().pos(), outPos.pos());
            Port port = network.getPortAt(outPos, dir);
            lane.setOutput(port != null ? port : NO_OP_PORT);
        }

        for (Map.Entry<GlobalPos, GlobalPos> entry : network.producerOutputPos.entrySet()) {
            GlobalPos outPos = entry.getValue();
            Direction dir = FactoryUtils.getOutputDirection(entry.getKey().pos(), outPos.pos());
            Port port = network.getPortAt(outPos, dir);
            if (port != null) network.producers.get(entry.getKey()).setOutput(port);
        }

        for (Map.Entry<GlobalPos, GlobalPos> entry : network.containerOutputPos.entrySet()) {
            GlobalPos outPos = entry.getValue();
            Direction dir = FactoryUtils.getOutputDirection(entry.getKey().pos(), outPos.pos());
            Port port = network.getPortAt(outPos, dir);
            if (port != null) network.containers.get(entry.getKey()).setOutput(port);
        }

        for (Map.Entry<GlobalPos, List<GlobalPos>> entry : network.machineOutputPos.entrySet()) {
            Machine machine = network.machines.get(entry.getKey());
            if (machine == null) continue;

            List<GlobalPos> slotOutputs = entry.getValue();
            for (int slotIndex = 0; slotIndex < slotOutputs.size(); slotIndex++) {
                GlobalPos outPos = slotOutputs.get(slotIndex);
                if (outPos == null) continue;
                Direction dir = FactoryUtils.getOutputDirection(entry.getKey().pos(), outPos.pos());
                Port port = network.getPortAt(outPos, dir);
                if (port != null) machine.setOutputPort(slotIndex, port);
            }
        }

        for (Map.Entry<GlobalPos, GlobalPos> entry : network.mergerOutputPos.entrySet()) {
            GlobalPos outPos = entry.getValue();
            Direction dir = FactoryUtils.getOutputDirection(entry.getKey().pos(), outPos.pos());
            Port port = network.getPortAt(outPos, dir);
            if (port != null) network.mergers.get(entry.getKey()).setOutput(port);
        }

        for (Map.Entry<GlobalPos, List<GlobalPos>> entry : network.splitterOutputPos.entrySet()) {
            Splitter splitter = network.splitters.get(entry.getKey());
            if (splitter == null) continue;

            List<GlobalPos> slotOutputs = entry.getValue();
            for (int slotIndex = 0; slotIndex < slotOutputs.size(); slotIndex++) {
                GlobalPos outPos = slotOutputs.get(slotIndex);
                if (outPos == null) continue;
                Direction dir = FactoryUtils.getOutputDirection(entry.getKey().pos(), outPos.pos());
                Port port = network.getPortAt(outPos, dir);
                if (port != null) splitter.setOutput(slotIndex, port);
            }
        }

        return network;
    }

    public int getBeltCount() {
        int count = 0;
        for (BeltLane lane : laneManager.getAllLanes().values()) count += lane.size();
        return count;
    }
    public int getLaneCount() { return laneManager.getLaneCount(); }
    public int getProducerCount() { return producers.size(); }
    public int getConsumerCount() { return consumers.size(); }
    public int getMachineCount() { return machines.size(); }
    public int getContainerCount() { return containers.size(); }

    // Counts factory components whose chunk is currently loaded
    // Intended for debug use only

    public int getLoadedBeltCount(MinecraftServer server) {
        int count = 0;
        for (BeltLane lane : laneManager.getAllLanes().values()) {
            if (isChunkLoaded(server, lane.headBlock())) count += lane.size();
        }
        return count;
    }
    public int getLoadedProducerCount(MinecraftServer server) { return countLoaded(server, producers.keySet()); }
    public int getLoadedConsumerCount(MinecraftServer server) { return countLoaded(server, consumers.keySet()); }
    public int getLoadedMachineCount(MinecraftServer server) { return countLoaded(server, machines.keySet()); }
    public int getLoadedContainerCount(MinecraftServer server) { return countLoaded(server, containers.keySet()); }

    private static int countLoaded(MinecraftServer server, Set<GlobalPos> positions) {
        int count = 0;
        for (GlobalPos pos : positions) {
            if (isChunkLoaded(server, pos)) count++;
        }
        return count;
    }

    private static boolean isChunkLoaded(MinecraftServer server, GlobalPos pos) {
        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null) return false;
        ChunkPos chunkPos = ChunkPos.containing(pos.pos());
        return level.getChunkSource().hasChunk(chunkPos.x(), chunkPos.z());
    }
}