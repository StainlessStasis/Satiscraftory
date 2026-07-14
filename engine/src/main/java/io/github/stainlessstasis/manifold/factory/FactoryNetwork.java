package io.github.stainlessstasis.manifold.factory;

import io.github.stainlessstasis.manifold.*;
import io.github.stainlessstasis.manifold.factory_component.*;
import io.github.stainlessstasis.manifold.network.BeltSyncPacket;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Supplier;

public class FactoryNetwork extends SavedData {
    private static final int MAX_ENTRIES_PER_PACKET = 500; // for belt syncing

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
    private final Map<GlobalPos, Belt> belts = new HashMap<>();
    private final Map<GlobalPos, Consumer> consumers = new HashMap<>();
    private final Map<GlobalPos, Machine> machines = new HashMap<>();

    private final Map<GlobalPos, GlobalPos> producerOutputPos = new HashMap<>();
    private final Map<GlobalPos, GlobalPos> beltOutputPos = new HashMap<>();
    private final Map<GlobalPos, List<GlobalPos>> machineOutputPos = new HashMap<>();

    private List<TickTarget> tickOrder = null; // cached; null means needs rebuild
    private interface TickTarget {
        void tick(long currentTick);
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

    public Producer getOrCreateProducer(GlobalPos pos, Supplier<Producer> factory) {
        return producers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Belt getOrCreateBelt(GlobalPos pos, Supplier<Belt> factory) {
        return belts.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
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

    public Port getPortAt(GlobalPos pos) {
        Belt belt = belts.get(pos);
        if (belt != null) return belt;
        Consumer consumer = consumers.get(pos);
        if (consumer != null) return consumer;
        Machine machine = machines.get(pos);
        if (machine != null && machine.inputSlotCount() > 0) return machine.inputPort(0);
        return null;
    }

    public void linkProducerOutput(GlobalPos producerPos, GlobalPos outputPos) {
        Producer producer = producers.get(producerPos);
        Port port = getPortAt(outputPos);
        if (producer != null && port != null) {
            producer.setOutput(port);
            producerOutputPos.put(producerPos, outputPos);
            setDirty();
        }
    }

    public void linkBeltOutput(GlobalPos beltPos, GlobalPos outputPos) {
        Belt belt = belts.get(beltPos);
        Port port = getPortAt(outputPos);
        if (belt == null || port == null) return;

        GlobalPos previousOutput = beltOutputPos.get(beltPos);
        if (port == belt.getOutput() && outputPos.equals(previousOutput)) return;

        belt.setOutput(port);
        beltOutputPos.put(beltPos, outputPos);
        setDirty();
    }

    public void linkMachineOutput(GlobalPos machinePos, int slotIndex, GlobalPos outputPos) {
        Machine machine = machines.get(machinePos);
        Port port = getPortAt(outputPos);
        if (machine == null || port == null) return;

        machine.setOutputPort(slotIndex, port);
        machineOutputPos.computeIfAbsent(machinePos, _ -> new ArrayList<>(machine.outputSlotCount()));
        List<GlobalPos> slots = machineOutputPos.get(machinePos);
        while (slots.size() <= slotIndex) slots.add(null);
        slots.set(slotIndex, outputPos);
        setDirty();
    }

    public void removeProducer(GlobalPos pos) {
        if (producers.remove(pos) != null) {
            producerOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeBelt(GlobalPos pos) {
        if (belts.remove(pos) != null) {
            beltOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeConsumer(GlobalPos pos) {
        if (consumers.remove(pos) != null) {
            setDirty();
        }
    }

    public void removeMachine(GlobalPos pos) {
        if (machines.remove(pos) != null) {
            machineOutputPos.remove(pos);
            setDirty();
        }
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

        Map<ResourceKey<Level>, List<BeltSyncPacket.Entry>> changedByDimension = new HashMap<>();
        for (var entry : belts.entrySet()) {
            Belt belt = entry.getValue();
            if (!belt.hasUnsyncedChanges(currentTick)) continue;

            GlobalPos globalPos = entry.getKey();
            ServerLevel beltLevel = level.getServer().getLevel(globalPos.dimension());
            if (beltLevel == null) { belt.markSynced(currentTick); continue; }

            BlockPos pos = globalPos.pos();
            if (!hasTrackingPlayers(beltLevel, ChunkPos.containing(pos))) {
                belt.markSynced(currentTick);
                continue;
            }

            changedByDimension
                    .computeIfAbsent(globalPos.dimension(), _ -> new ArrayList<>())
                    .add(new BeltSyncPacket.Entry(pos, currentTick, belt.getItemSnapshots()));
            belt.markSynced(currentTick);
        }

        for (var entry : changedByDimension.entrySet()) {
            ServerLevel targetLevel = level.getServer().getLevel(entry.getKey());
            if (targetLevel == null) continue;

            List<BeltSyncPacket.Entry> allChanged = entry.getValue();
            for (int start = 0; start < allChanged.size(); start += MAX_ENTRIES_PER_PACKET) {
                int end = Math.min(start + MAX_ENTRIES_PER_PACKET, allChanged.size());
                BeltSyncPacket payload = new BeltSyncPacket(allChanged.subList(start, end));
                for (var player : targetLevel.players()) {
                    PacketDistributor.sendToPlayer(player, payload);
                }
            }
        }
    }

    private List<TickTarget> computeTickOrder() {
        List<GlobalPos> order = new ArrayList<>(producers.size() + belts.size() + consumers.size() + machines.size());
        Set<GlobalPos> visited = new HashSet<>();

        Set<GlobalPos> allNodes = new LinkedHashSet<>();
        allNodes.addAll(producers.keySet());
        allNodes.addAll(belts.keySet());
        allNodes.addAll(machines.keySet());
        allNodes.addAll(consumers.keySet());

        for (GlobalPos start : allNodes) {
            if (!visited.contains(start)) visitIterative(start, order, visited);
        }

        List<TickTarget> resolved = new ArrayList<>(order.size());
        for (GlobalPos pos : order) {
            Producer producer = producers.get(pos);
            if (producer != null) { resolved.add(producer::tick); continue; }
            Belt belt = belts.get(pos);
            if (belt != null) { resolved.add(belt::tick); continue; }
            Machine machine = machines.get(pos);
            if (machine != null) { resolved.add(machine::tick); continue; }
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

    private List<GlobalPos> outputsOf(GlobalPos pos) {
        if (belts.containsKey(pos)) {
            GlobalPos out = beltOutputPos.get(pos);
            return out != null ? List.of(out) : List.of();
        }
        if (producers.containsKey(pos)) {
            GlobalPos out = producerOutputPos.get(pos);
            return out != null ? List.of(out) : List.of();
        }
        if (machines.containsKey(pos)) {
            List<GlobalPos> outs = machineOutputPos.get(pos);
            return outs != null ? outs : List.of();
        }
        return List.of();
    }

    private boolean isTrackedNode(GlobalPos pos) {
        return pos != null && (belts.containsKey(pos) || producers.containsKey(pos)
                || machines.containsKey(pos) || consumers.containsKey(pos));
    }

    private void visitIterative(GlobalPos start, List<GlobalPos> order, Set<GlobalPos> visited) {
        Deque<GlobalPos> stack = new ArrayDeque<>();
        Deque<Iterator<GlobalPos>> pendingOutputs = new ArrayDeque<>();
        Set<GlobalPos> onStack = new HashSet<>();

        stack.push(start);
        onStack.add(start);
        pendingOutputs.push(outputsOf(start).iterator());

        while (!stack.isEmpty()) {
            GlobalPos pos = stack.peek();
            Iterator<GlobalPos> outIter = pendingOutputs.peek();

            boolean pushedChild = false;
            while (outIter != null && outIter.hasNext()) {
                GlobalPos next = outIter.next();
                if (isTrackedNode(next) && !visited.contains(next) && !onStack.contains(next)) {
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
            onStack.remove(pos);
            visited.add(pos);
            order.add(pos);
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

        List<Persisted.Belt> persistedBelts = new ArrayList<>();
        for (Map.Entry<GlobalPos, Belt> entry : belts.entrySet()) {
            GlobalPos pos = entry.getKey();
            Belt belt = entry.getValue();
            List<Persisted.BeltItem> items = new ArrayList<>();
            for (Belt.ItemSnapshot snapshot : belt.getItemSnapshots()) {
                items.add(new Persisted.BeltItem(snapshot.position(), snapshot.itemId()));
            }
            persistedBelts.add(new Persisted.Belt(pos, belt.getSpeed(), belt.getMinGap(),
                    Optional.ofNullable(beltOutputPos.get(pos)), items));
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
            Machine machine = entry.getValue();
            persistedMachines.add(new Persisted.Machine(
                    entry.getKey(), machine.getRecipe().id(), machine.getBufferMultiplier(),
                    machine.isCrafting(), machine.getCraftCompletionTick(),
                    machine.getBufferedCounts(), machine.getPendingOutputItemIds()
            ));
        }
        return new Persisted.Snapshot(persistedProducers, persistedBelts, persistedConsumers, persistedMachines);
    }

    private static FactoryNetwork fromSnapshot(Persisted.Snapshot snapshot) {
        FactoryNetwork network = new FactoryNetwork();

        for (Persisted.Belt beltData : snapshot.belts()) {
            Belt belt = new Belt(beltData.speed(), beltData.minGap());
            for (Persisted.BeltItem item : beltData.items()) belt.restoreItem(item.itemId(), item.position());
            network.belts.put(beltData.pos(), belt);
            beltData.outputPos().ifPresent(outPos -> network.beltOutputPos.put(beltData.pos(), outPos));
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
                    machineData.bufferedCounts(), machineData.pendingOutputItemIds());
            network.machines.put(machineData.pos(), machine);
        }

        for (Map.Entry<GlobalPos, GlobalPos> entry : network.beltOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.belts.get(entry.getKey()).setOutput(port);
        }
        for (Map.Entry<GlobalPos, GlobalPos> entry : network.producerOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.producers.get(entry.getKey()).setOutput(port);
        }

        return network;
    }

    public int getBeltCount() { return belts.size(); }
    public int getProducerCount() { return producers.size(); }
    public int getConsumerCount() { return consumers.size(); }
    public int getMachineCount() { return machines.size(); }
}