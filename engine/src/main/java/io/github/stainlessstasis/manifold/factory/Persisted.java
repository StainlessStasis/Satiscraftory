package io.github.stainlessstasis.manifold.factory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.manifold.factory_power.network.PowerGrid;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

import java.util.*;

final class Persisted {
    private Persisted() {}

    private static final Codec<int[]> INT_ARRAY_CODEC = Codec.INT.listOf().xmap(
            list -> list.stream().mapToInt(Integer::intValue).toArray(),
            arr -> Arrays.stream(arr).boxed().toList()
    );

    record BeltItem(long id, double position, Identifier itemId) {
        static final Codec<BeltItem> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.optionalFieldOf("id", -1L).forGetter(BeltItem::id),
                Codec.DOUBLE.fieldOf("position").forGetter(BeltItem::position),
                Identifier.CODEC.fieldOf("itemId").forGetter(BeltItem::itemId)
        ).apply(i, BeltItem::new));
    }

    record BeltLane(UUID id, List<GlobalPos> blocks, double speed, double minGap, Optional<GlobalPos> outputPos, List<BeltItem> items) {
        static final Codec<BeltLane> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(BeltLane::id),
                GlobalPos.CODEC.listOf().fieldOf("blocks").forGetter(BeltLane::blocks),
                Codec.DOUBLE.fieldOf("speed").forGetter(BeltLane::speed),
                Codec.DOUBLE.fieldOf("minGap").forGetter(BeltLane::minGap),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(BeltLane::outputPos),
                BeltItem.CODEC.listOf().fieldOf("items").forGetter(BeltLane::items)
        ).apply(i, BeltLane::new));
    }

    record Producer(GlobalPos pos, Identifier itemType, long interval, Optional<GlobalPos> outputPos,
                    boolean active, int bufferedCount, long nextProductionTick,
                    boolean powered, long pausedRemainingTicks) {
        static final Codec<Producer> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Producer::pos),
                Identifier.CODEC.fieldOf("itemType").forGetter(Producer::itemType),
                Codec.LONG.fieldOf("interval").forGetter(Producer::interval),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Producer::outputPos),
                Codec.BOOL.fieldOf("active").forGetter(Producer::active),
                Codec.INT.optionalFieldOf("bufferedCount", 0).forGetter(Producer::bufferedCount),
                Codec.LONG.fieldOf("nextProductionTick").forGetter(Producer::nextProductionTick),
                Codec.BOOL.optionalFieldOf("powered", true).forGetter(Producer::powered),
                Codec.LONG.optionalFieldOf("pausedRemainingTicks", -1L).forGetter(Producer::pausedRemainingTicks)
        ).apply(i, Producer::new));
    }

    record Consumer(GlobalPos pos, int capacity, int processTime, List<Identifier> bufferedItemIds,
                    Optional<Identifier> processingItemId, long processStartTick, int consumedCount) {
        static final Codec<Consumer> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Consumer::pos),
                Codec.INT.fieldOf("capacity").forGetter(Consumer::capacity),
                Codec.INT.fieldOf("processTime").forGetter(Consumer::processTime),
                Identifier.CODEC.listOf().fieldOf("bufferedItemIds").forGetter(Consumer::bufferedItemIds),
                Identifier.CODEC.optionalFieldOf("processingItemId").forGetter(Consumer::processingItemId),
                Codec.LONG.fieldOf("processStartTick").forGetter(Consumer::processStartTick),
                Codec.INT.fieldOf("consumedCount").forGetter(Consumer::consumedCount)
        ).apply(i, Consumer::new));
    }

    record Machine(GlobalPos pos, Identifier recipeId, int bufferMultiplier, boolean crafting, boolean stalled,
                   long craftCompletionTick, int[] inputCounts, int[] outputCounts,
                   Map<Direction, Integer> inputFaces, Map<Direction, Integer> outputFaces,
                   Map<Integer, GlobalPos> outputPos, boolean powered, long pausedRemainingTicks) {
        static final Codec<Machine> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Machine::pos),
                Identifier.CODEC.fieldOf("recipeId").forGetter(Machine::recipeId),
                Codec.INT.fieldOf("bufferMultiplier").forGetter(Machine::bufferMultiplier),
                Codec.BOOL.fieldOf("crafting").forGetter(Machine::crafting),
                Codec.BOOL.optionalFieldOf("stalled", false).forGetter(Machine::stalled),
                Codec.LONG.fieldOf("craftCompletionTick").forGetter(Machine::craftCompletionTick),
                INT_ARRAY_CODEC.fieldOf("inputCounts").forGetter(Machine::inputCounts),
                INT_ARRAY_CODEC.fieldOf("outputCounts").forGetter(Machine::outputCounts),
                Codec.unboundedMap(Direction.CODEC, Codec.INT).fieldOf("inputFaces").forGetter(Machine::inputFaces),
                Codec.unboundedMap(Direction.CODEC, Codec.INT).fieldOf("outputFaces").forGetter(Machine::outputFaces),
                Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf), GlobalPos.CODEC)
                        .optionalFieldOf("outputPos", Map.of()).forGetter(Machine::outputPos),
                Codec.BOOL.optionalFieldOf("powered", true).forGetter(Machine::powered),
                Codec.LONG.optionalFieldOf("pausedRemainingTicks", -1L).forGetter(Machine::pausedRemainingTicks)
        ).apply(i, Machine::new));
    }

    record Generator(GlobalPos pos, Identifier generatorType, double powerRate,
                      Optional<Identifier> heldItemId, int heldCount, boolean burning, long burnEndTick) {
        static final Codec<Generator> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Generator::pos),
                Identifier.CODEC.fieldOf("generatorType").forGetter(Generator::generatorType),
                Codec.DOUBLE.fieldOf("powerRate").forGetter(Generator::powerRate),
                Identifier.CODEC.optionalFieldOf("heldItemId").forGetter(Generator::heldItemId),
                Codec.INT.optionalFieldOf("heldCount", 0).forGetter(Generator::heldCount),
                Codec.BOOL.optionalFieldOf("burning", false).forGetter(Generator::burning),
                Codec.LONG.optionalFieldOf("burnEndTick", -1L).forGetter(Generator::burnEndTick)
        ).apply(i, Generator::new));
    }

    record PowerProducer(GlobalPos pos, double supplyRate, boolean active) {
        static final Codec<PowerProducer> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(PowerProducer::pos),
                Codec.DOUBLE.fieldOf("supplyRate").forGetter(PowerProducer::supplyRate),
                Codec.BOOL.optionalFieldOf("active", true).forGetter(PowerProducer::active)
        ).apply(i, PowerProducer::new));
    }

    record ContainerSlot(Optional<Identifier> itemId, int count) {
        static final Codec<ContainerSlot> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.optionalFieldOf("itemId").forGetter(ContainerSlot::itemId),
                Codec.INT.fieldOf("count").forGetter(ContainerSlot::count)
        ).apply(i, ContainerSlot::new));

        static final ContainerSlot EMPTY = new ContainerSlot(Optional.empty(), 0);
    }

    record Container(GlobalPos pos, int slotCount, List<ContainerSlot> slots, Optional<GlobalPos> outputPos) {
        static final Codec<Container> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Container::pos),
                Codec.INT.fieldOf("slotCount").forGetter(Container::slotCount),
                ContainerSlot.CODEC.listOf().fieldOf("slots").forGetter(Container::slots),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Container::outputPos)
        ).apply(i, Container::new));
    }

    record Splitter(GlobalPos pos, int nextOutputIndex, Map<Direction, Integer> outputFaces, Map<Integer, GlobalPos> outputPos) {
        static final Codec<Splitter> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Splitter::pos),
                Codec.INT.fieldOf("nextOutputIndex").forGetter(Splitter::nextOutputIndex),
                Codec.unboundedMap(Direction.CODEC, Codec.INT).fieldOf("outputFaces").forGetter(Splitter::outputFaces),
                Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf), GlobalPos.CODEC)
                        .optionalFieldOf("outputPos", Map.of()).forGetter(Splitter::outputPos)
        ).apply(i, Splitter::new));
    }

    record Merger(GlobalPos pos, int nextInputIndex, Map<Direction, Integer> inputFaces,
                  Map<Integer, Identifier> bufferedItemIds, Optional<GlobalPos> outputPos) {
        static final Codec<Merger> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Merger::pos),
                Codec.INT.fieldOf("nextInputIndex").forGetter(Merger::nextInputIndex),
                Codec.unboundedMap(Direction.CODEC, Codec.INT).fieldOf("inputFaces").forGetter(Merger::inputFaces),
                Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf), Identifier.CODEC)
                        .optionalFieldOf("bufferedItemIds", Map.of()).forGetter(Merger::bufferedItemIds),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Merger::outputPos)
        ).apply(i, Merger::new));
    }

    record PowerNode(GlobalPos pos, double supply, double demand, int maxConnections) {
        static final Codec<PowerNode> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(PowerNode::pos),
                Codec.DOUBLE.optionalFieldOf("supply", 0d).forGetter(PowerNode::supply),
                Codec.DOUBLE.optionalFieldOf("demand", 0d).forGetter(PowerNode::demand),
                Codec.INT.optionalFieldOf("maxConnections", PowerGrid.DEFAULT_MAX_CONNECTIONS).forGetter(PowerNode::maxConnections)
        ).apply(i, PowerNode::new));
    }

    record PowerEdge(PowerGrid.Edge edge) {
        static final Codec<PowerEdge> CODEC = RecordCodecBuilder.create(i -> i.group(
                PowerGrid.Edge.CODEC.fieldOf("nodeA").forGetter(PowerEdge::edge)
        ).apply(i, PowerEdge::new));
    }

    record PowerGridData(List<PowerNode> nodes, List<PowerEdge> edges) {
        static final Codec<PowerGridData> CODEC = RecordCodecBuilder.create(i -> i.group(
                PowerNode.CODEC.listOf().fieldOf("nodes").forGetter(PowerGridData::nodes),
                PowerEdge.CODEC.listOf().optionalFieldOf("edges", List.of()).forGetter(PowerGridData::edges)
        ).apply(i, PowerGridData::new));

        static final PowerGridData EMPTY = new PowerGridData(List.of(), List.of());
    }

    /**
     * The full snapshot of everything FactoryNetwork tracks (producers, belts, machines, ...you get it)
     */
    record Snapshot(List<Producer> producers, List<BeltLane> belts, List<Consumer> consumers, List<Machine> machines,
                    List<Container> containers, List<Splitter> splitters, List<Merger> mergers,
                    List<Generator> generators, List<PowerProducer> powerProducers, PowerGridData powerGrid) {
        static final Codec<Snapshot> CODEC = RecordCodecBuilder.create(i -> i.group(
                Producer.CODEC.listOf().fieldOf("producers").forGetter(Snapshot::producers),
                BeltLane.CODEC.listOf().fieldOf("belts").forGetter(Snapshot::belts),
                Consumer.CODEC.listOf().fieldOf("consumers").forGetter(Snapshot::consumers),
                Machine.CODEC.listOf().fieldOf("machines").forGetter(Snapshot::machines),
                Container.CODEC.listOf().fieldOf("containers").forGetter(Snapshot::containers),
                Splitter.CODEC.listOf().optionalFieldOf("splitters", List.of()).forGetter(Snapshot::splitters),
                Merger.CODEC.listOf().optionalFieldOf("mergers", List.of()).forGetter(Snapshot::mergers),
                Generator.CODEC.listOf().optionalFieldOf("generators", List.of()).forGetter(Snapshot::generators),
                PowerProducer.CODEC.listOf().optionalFieldOf("powerProducers", List.of()).forGetter(Snapshot::powerProducers),
                PowerGridData.CODEC.optionalFieldOf("powerGrid", PowerGridData.EMPTY).forGetter(Snapshot::powerGrid)
        ).apply(i, Snapshot::new));
    }
}