package io.github.stainlessstasis.manifold.factory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.manifold.factory_component.Container;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class Persisted {
    private Persisted() {}

    record BeltItem(double position, Identifier itemId) {
        static final Codec<BeltItem> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.fieldOf("position").forGetter(BeltItem::position),
                Identifier.CODEC.fieldOf("itemId").forGetter(BeltItem::itemId)
        ).apply(i, BeltItem::new));
    }

    record Belt(GlobalPos pos, double speed, double minGap, Optional<GlobalPos> outputPos, List<BeltItem> items) {
        static final Codec<Belt> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Belt::pos),
                Codec.DOUBLE.fieldOf("speed").forGetter(Belt::speed),
                Codec.DOUBLE.fieldOf("minGap").forGetter(Belt::minGap),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Belt::outputPos),
                BeltItem.CODEC.listOf().fieldOf("items").forGetter(Belt::items)
        ).apply(i, Belt::new));
    }

    record Producer(GlobalPos pos, Identifier itemType, long interval, Optional<GlobalPos> outputPos,
                    boolean active, Optional<Identifier> pendingItemId, long nextProductionTick) {
        static final Codec<Producer> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Producer::pos),
                Identifier.CODEC.fieldOf("itemType").forGetter(Producer::itemType),
                Codec.LONG.fieldOf("interval").forGetter(Producer::interval),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Producer::outputPos),
                Codec.BOOL.fieldOf("active").forGetter(Producer::active),
                Identifier.CODEC.optionalFieldOf("pendingItemId").forGetter(Producer::pendingItemId),
                Codec.LONG.fieldOf("nextProductionTick").forGetter(Producer::nextProductionTick)
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

    record Machine(GlobalPos pos, Identifier recipeId, int bufferMultiplier, boolean crafting, long craftCompletionTick,
                   int[] bufferedCounts, List<List<Identifier>> pendingOutputItemIds,
                   Map<Direction, Integer> inputFaces, Map<Direction, Integer> outputFaces) {
        static final Codec<Machine> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Machine::pos),
                Identifier.CODEC.fieldOf("recipeId").forGetter(Machine::recipeId),
                Codec.INT.fieldOf("bufferMultiplier").forGetter(Machine::bufferMultiplier),
                Codec.BOOL.fieldOf("crafting").forGetter(Machine::crafting),
                Codec.LONG.fieldOf("craftCompletionTick").forGetter(Machine::craftCompletionTick),
                Codec.INT.listOf().xmap(
                        list -> list.stream().mapToInt(Integer::intValue).toArray(),
                        arr -> java.util.Arrays.stream(arr).boxed().toList()
                ).fieldOf("bufferedCounts").forGetter(Machine::bufferedCounts),
                Identifier.CODEC.listOf().listOf().fieldOf("pendingOutputItemIds").forGetter(Machine::pendingOutputItemIds),
                Codec.unboundedMap(Direction.CODEC, Codec.INT).fieldOf("inputFaces").forGetter(Machine::inputFaces),
                Codec.unboundedMap(Direction.CODEC, Codec.INT).fieldOf("outputFaces").forGetter(Machine::outputFaces)
        ).apply(i, Machine::new));
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

    /** The full snapshot of everything FactoryNetwork tracks*/
    record Snapshot(List<Producer> producers, List<Belt> belts, List<Consumer> consumers, List<Machine> machines, List<Container> containers) {
        static final Codec<Snapshot> CODEC = RecordCodecBuilder.create(i -> i.group(
                Producer.CODEC.listOf().fieldOf("producers").forGetter(Snapshot::producers),
                Belt.CODEC.listOf().fieldOf("belts").forGetter(Snapshot::belts),
                Consumer.CODEC.listOf().fieldOf("consumers").forGetter(Snapshot::consumers),
                Machine.CODEC.listOf().fieldOf("machines").forGetter(Snapshot::machines),
                Container.CODEC.listOf().fieldOf("containers").forGetter(Snapshot::containers)
        ).apply(i, Snapshot::new));
    }
}

