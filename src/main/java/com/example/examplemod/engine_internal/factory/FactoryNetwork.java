package com.example.examplemod.engine_internal.factory;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.engine_internal.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
    private final Map<BlockPos, Producer> producers = new HashMap<>();
    private final Map<BlockPos, Belt> belts = new HashMap<>();
    private final Map<BlockPos, Consumer> consumers = new HashMap<>();
    private final Map<BlockPos, Machine> machines = new HashMap<>();

    private final Map<BlockPos, BlockPos> producerOutputPos = new HashMap<>();
    private final Map<BlockPos, BlockPos> beltOutputPos = new HashMap<>();
    private final Map<BlockPos, BlockPos> machineOutputPos = new HashMap<>();

    public FactoryNetwork() {}

    /**
     * Attached to the Overworld specifically, so there's one shared network
     * across all dimensions rather than a separate one per dimension
     */
    public static FactoryNetwork get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }


    public Producer getOrCreateProducer(BlockPos pos, Supplier<Producer> factory) {
        return producers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Belt getOrCreateBelt(BlockPos pos, Supplier<Belt> factory) {
        return belts.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Consumer getOrCreateConsumer(BlockPos pos, Supplier<Consumer> factory) {
        return consumers.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Machine getOrCreateMachine(BlockPos pos, Supplier<Machine> factory) {
        return machines.computeIfAbsent(pos, _ -> {
            setDirty();
            return factory.get();
        });
    }

    public Port getPortAt(BlockPos pos) {
        Belt belt = belts.get(pos);
        if (belt != null) return belt;
        return consumers.get(pos);
    }

    public void linkProducerOutput(BlockPos producerPos, BlockPos outputPos) {
        Producer producer = producers.get(producerPos);
        Port port = getPortAt(outputPos);
        System.out.println("[Link] producer@" + producerPos + " -> outputPos=" + outputPos
                + ", producerFound=" + (producer != null) + ", portFound=" + (port != null));
        if (producer != null && port != null) {
            producer.setOutput(port);
            producerOutputPos.put(producerPos, outputPos);
            setDirty();
        }
    }

    public void linkBeltOutput(BlockPos beltPos, BlockPos outputPos) {
        Belt belt = belts.get(beltPos);
        Port port = getPortAt(outputPos);
        System.out.println("[Link] belt@" + beltPos + " -> outputPos=" + outputPos
                + ", producerFound=" + (belt != null) + ", portFound=" + (port != null));
        if (belt != null && port != null) {
            belt.setOutput(port);
            beltOutputPos.put(beltPos, outputPos);
            setDirty();
        }
    }

    public void linkMachineOutput(BlockPos machinePos, BlockPos outputPos) {
        Machine machine = machines.get(machinePos);
        Port port = getPortAt(outputPos);
        if (machine != null && port != null) {
            machine.setOutput(port);
            machineOutputPos.put(machinePos, outputPos);
            setDirty();
        }
    }


    public void removeProducer(BlockPos pos) {
        if (producers.remove(pos) != null) {
            producerOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeBelt(BlockPos pos) {
        if (belts.remove(pos) != null) {
            beltOutputPos.remove(pos);
            setDirty();
        }
    }

    public void removeConsumer(BlockPos pos) {
        if (consumers.remove(pos) != null) setDirty();
    }

    public void removeMachine(BlockPos pos) {
        if (machines.remove(pos) != null) {
            machineOutputPos.remove(pos);
            setDirty();
        }
    }

    public void tickAll(long currentTick) {
        scheduler.tick(currentTick);
        for (Belt belt : belts.values()) belt.tick(currentTick);
        for (Producer producer : producers.values()) producer.tick(currentTick);
        for (Consumer consumer : consumers.values()) consumer.tick(currentTick);
        for (Machine machine : machines.values()) machine.tick(currentTick);
    }


    private Persisted.Snapshot toSnapshot() {
        List<Persisted.Producer> persistedProducers = new ArrayList<>();
        for (Map.Entry<BlockPos, Producer> entry : producers.entrySet()) {
            BlockPos pos = entry.getKey();
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
        for (Map.Entry<BlockPos, Belt> entry : belts.entrySet()) {
            BlockPos pos = entry.getKey();
            Belt belt = entry.getValue();
            List<Persisted.BeltItem> items = new ArrayList<>();
            for (Belt.ItemSnapshot snapshot : belt.getItemSnapshots()) {
                items.add(new Persisted.BeltItem(
                        snapshot.position(), snapshot.typeId())
                );
            }
            persistedBelts.add(new Persisted.Belt(pos, belt.getLengthTicks(), belt.getMinGap(),
                    Optional.ofNullable(beltOutputPos.get(pos)), items));
        }

        List<Persisted.Consumer> persistedConsumers = new ArrayList<>();
        for (Map.Entry<BlockPos, Consumer> entry : consumers.entrySet()) {
            BlockPos pos = entry.getKey();
            Consumer consumer = entry.getValue();
            persistedConsumers.add(new Persisted.Consumer(
                    pos, consumer.getCapacity(), consumer.getProcessTime(), consumer.getBufferedTypeIds(),
                    Optional.ofNullable(consumer.getProcessingTypeId()), consumer.getProcessStartTick(), consumer.getConsumedCount())
            );
        }

        List<Persisted.Machine> persistedMachines = new ArrayList<>();
        for (Map.Entry<BlockPos, Machine> entry : machines.entrySet()) {
            BlockPos pos = entry.getKey();
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

        // Create every producer, belt, consumer, and machine with a temporary no-op output
        // Nothing here depends on anything else existing yet
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

        // Now that every belt/consumer/producer exists, resolve the real output links
        for (Map.Entry<BlockPos, BlockPos> entry : network.beltOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.belts.get(entry.getKey()).setOutput(port);
        }

        for (Map.Entry<BlockPos, BlockPos> entry : network.producerOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.producers.get(entry.getKey()).setOutput(port);
        }

        for (Map.Entry<BlockPos, BlockPos> entry : network.machineOutputPos.entrySet()) {
            Port port = network.getPortAt(entry.getValue());
            if (port != null) network.machines.get(entry.getKey()).setOutput(port);
        }

        return network;
    }
}


