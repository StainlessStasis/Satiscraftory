package com.example.examplemod.engine_internal.factory;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.engine_internal.*;
import com.example.examplemod.engine_internal.Belt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.function.Supplier;

public class FactoryNetwork extends SavedData {

    public static final SavedDataType<FactoryNetwork> TYPE = new SavedDataType<>(
            ExampleMod.id("factory_network"),
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
    private final Map<GlobalPos, GlobalPos> machineOutputPos = new HashMap<>();

    private List<GlobalPos> tickOrder = null; // cached; null means needs rebuild

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
        return consumers.get(pos);
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
        if (belt != null && port != null) {
            belt.setOutput(port);
            beltOutputPos.put(beltPos, outputPos);
            setDirty();
        }
    }

    public void linkMachineOutput(GlobalPos machinePos, GlobalPos outputPos) {
        Machine machine = machines.get(machinePos);
        Port port = getPortAt(outputPos);
        if (machine != null && port != null) {
            machine.setOutput(port);
            machineOutputPos.put(machinePos, outputPos);
            setDirty();
        }
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

        for (GlobalPos pos : tickOrder) {
            Producer producer = producers.get(pos);
            if (producer != null) { producer.tick(currentTick); continue; }
            Belt belt = belts.get(pos);
            if (belt != null) { belt.tick(currentTick); continue; }
            Machine machine = machines.get(pos);
            if (machine != null) { machine.tick(currentTick); continue; }
            Consumer consumer = consumers.get(pos);
            if (consumer != null) consumer.tick(currentTick);
        }

        for (var entry : belts.entrySet()) {
            Belt belt = entry.getValue();
            if (belt.hasUnsyncedChanges()) {
                GlobalPos globalPos = entry.getKey();
                ServerLevel beltLevel = level.getServer().getLevel(globalPos.dimension());
                if (beltLevel != null) {
                    BlockPos pos = globalPos.pos();
                    BlockState state = beltLevel.getBlockState(pos);
                    beltLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                }
                belt.markSynced(currentTick);
            }
        }
    }

    private List<GlobalPos> computeTickOrder() {
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
        return order;
    }

    private GlobalPos outputOf(GlobalPos pos) {
        if (belts.containsKey(pos)) return beltOutputPos.get(pos);
        if (producers.containsKey(pos)) return producerOutputPos.get(pos);
        if (machines.containsKey(pos)) return machineOutputPos.get(pos);
        return null; // consumers have no output
    }

    private boolean isTrackedNode(GlobalPos pos) {
        return pos != null && (belts.containsKey(pos) || producers.containsKey(pos)
                || machines.containsKey(pos) || consumers.containsKey(pos));
    }

    private void visitIterative(GlobalPos start, List<GlobalPos> order, Set<GlobalPos> visited) {
        Deque<GlobalPos> stack = new ArrayDeque<>();
        Set<GlobalPos> onStack = new HashSet<>();
        stack.push(start);
        onStack.add(start);

        while (!stack.isEmpty()) {
            GlobalPos pos = stack.peek();
            if (visited.contains(pos)) { stack.pop(); onStack.remove(pos); continue; }

            GlobalPos next = outputOf(pos);
            if (isTrackedNode(next) && !visited.contains(next) && !onStack.contains(next)) {
                stack.push(next);
                onStack.add(next);
                continue;
            }
            stack.pop();
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
                    pos, producer.getItemType(), producer.getInterval(),
                    Optional.ofNullable(producerOutputPos.get(pos)),
                    producer.isActive(),
                    Optional.ofNullable(pending == null ? null : pending.typeId()),
                    producer.getNextProductionTick()
            ));
        }

        List<Persisted.Belt> persistedBelts = new ArrayList<>();
        for (Map.Entry<GlobalPos, Belt> entry : belts.entrySet()) {
            GlobalPos pos = entry.getKey();
            Belt belt = entry.getValue();
            List<Persisted.BeltItem> items = new ArrayList<>();
            for (Belt.ItemSnapshot snapshot : belt.getItemSnapshots()) {
                items.add(new Persisted.BeltItem(snapshot.position(), snapshot.typeId()));
            }
            persistedBelts.add(new Persisted.Belt(pos, belt.getLengthTicks(), belt.getMinGap(),
                    Optional.ofNullable(beltOutputPos.get(pos)), items));
        }

        List<Persisted.Consumer> persistedConsumers = new ArrayList<>();
        for (Map.Entry<GlobalPos, Consumer> entry : consumers.entrySet()) {
            GlobalPos pos = entry.getKey();
            Consumer consumer = entry.getValue();
            persistedConsumers.add(new Persisted.Consumer(
                    pos, consumer.getCapacity(), consumer.getProcessTime(), consumer.getBufferedTypeIds(),
                    Optional.ofNullable(consumer.getProcessingTypeId()), consumer.getProcessStartTick(), consumer.getConsumedCount())
            );
        }

        List<Persisted.Machine> persistedMachines = new ArrayList<>();
        for (Map.Entry<GlobalPos, Machine> entry : machines.entrySet()) {
            GlobalPos pos = entry.getKey();
            Machine machine = entry.getValue();
            Recipe recipe = machine.getRecipe();
            persistedMachines.add(new Persisted.Machine(
                    pos, recipe.inputTypeId(), recipe.outputTypeId(), recipe.durationTicks(),
                    Optional.ofNullable(machineOutputPos.get(pos)),
                    machine.isCrafting(),
                    Optional.ofNullable(machine.getPendingOutputTypeId()),
                    machine.getCraftCompletionTick()
            ));
        }
        return new Persisted.Snapshot(persistedProducers, persistedBelts, persistedConsumers, persistedMachines);
    }

    private static FactoryNetwork fromSnapshot(Persisted.Snapshot snapshot) {
        FactoryNetwork network = new FactoryNetwork();

        for (Persisted.Belt beltData : snapshot.belts()) {
            Belt belt = new Belt(beltData.lengthTicks(), beltData.minGap());
            for (Persisted.BeltItem item : beltData.items()) belt.restoreItem(item.typeId(), item.position());
            network.belts.put(beltData.pos(), belt);
            beltData.outputPos().ifPresent(outPos -> network.beltOutputPos.put(beltData.pos(), outPos));
        }

        for (Persisted.Consumer consumerData : snapshot.consumers()) {
            Consumer consumer = Consumer.restore(
                    consumerData.capacity(), consumerData.processTime(), consumerData.bufferedTypeIds(),
                    consumerData.processingTypeId().orElse(null), consumerData.processStartTick(), consumerData.consumedCount()
            );
            network.consumers.put(consumerData.pos(), consumer);
        }

        for (Persisted.Producer producerData : snapshot.producers()) {
            Payload pending = producerData.pendingTypeId().map(Payload::new).orElse(null);
            Producer producer = Producer.restore(
                    producerData.itemType(), producerData.interval(), NO_OP_PORT, network.scheduler,
                    producerData.active(), pending, producerData.nextProductionTick()
            );
            network.producers.put(producerData.pos(), producer);
            producerData.outputPos().ifPresent(outPos -> network.producerOutputPos.put(producerData.pos(), outPos));
        }

        for (Persisted.Machine machineData : snapshot.machines()) {
            Payload pendingOutput = machineData.pendingOutputTypeId().map(Payload::new).orElse(null);
            Recipe recipe = new Recipe(machineData.inputTypeId(), machineData.outputTypeId(), machineData.durationTicks());
            Machine machine = Machine.restore(
                    recipe, network.scheduler, NO_OP_PORT,
                    machineData.crafting(), pendingOutput, machineData.craftCompletionTick()
            );
            network.machines.put(machineData.pos(), machine);
            machineData.outputPos().ifPresent(outPos -> network.machineOutputPos.put(machineData.pos(), outPos));
        }

        for (Map.Entry<GlobalPos, GlobalPos> entry : network.beltOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.belts.get(entry.getKey()).setOutput(port);
        }
        for (Map.Entry<GlobalPos, GlobalPos> entry : network.producerOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.producers.get(entry.getKey()).setOutput(port);
        }
        for (Map.Entry<GlobalPos, GlobalPos> entry : network.machineOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.machines.get(entry.getKey()).setOutput(port);
        }

        return network;
    }
}