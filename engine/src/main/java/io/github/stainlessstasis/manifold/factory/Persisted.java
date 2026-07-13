package io.github.stainlessstasis.manifold.factory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;

import java.util.List;
import java.util.Optional;

final class Persisted {
    private Persisted() {}

    record BeltItem(double position, String typeId) {
        static final Codec<BeltItem> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.fieldOf("position").forGetter(BeltItem::position),
                Codec.STRING.fieldOf("typeId").forGetter(BeltItem::typeId)
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

    record Producer(GlobalPos pos, String itemType, long interval, Optional<GlobalPos> outputPos,
                    boolean active, Optional<String> pendingTypeId, long nextProductionTick) {
        static final Codec<Producer> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Producer::pos),
                Codec.STRING.fieldOf("itemType").forGetter(Producer::itemType),
                Codec.LONG.fieldOf("interval").forGetter(Producer::interval),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Producer::outputPos),
                Codec.BOOL.fieldOf("active").forGetter(Producer::active),
                Codec.STRING.optionalFieldOf("pendingTypeId").forGetter(Producer::pendingTypeId),
                Codec.LONG.fieldOf("nextProductionTick").forGetter(Producer::nextProductionTick)
        ).apply(i, Producer::new));
    }

    record Consumer(GlobalPos pos, int capacity, int processTime, List<String> bufferedTypeIds,
                    Optional<String> processingTypeId, long processStartTick, int consumedCount) {
        static final Codec<Consumer> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Consumer::pos),
                Codec.INT.fieldOf("capacity").forGetter(Consumer::capacity),
                Codec.INT.fieldOf("processTime").forGetter(Consumer::processTime),
                Codec.STRING.listOf().fieldOf("bufferedTypeIds").forGetter(Consumer::bufferedTypeIds),
                Codec.STRING.optionalFieldOf("processingTypeId").forGetter(Consumer::processingTypeId),
                Codec.LONG.fieldOf("processStartTick").forGetter(Consumer::processStartTick),
                Codec.INT.fieldOf("consumedCount").forGetter(Consumer::consumedCount)
        ).apply(i, Consumer::new));
    }

    record Machine(GlobalPos pos, String inputTypeId, String outputTypeId, long durationTicks,
                   Optional<GlobalPos> outputPos, boolean crafting,
                   Optional<String> pendingOutputTypeId, long craftCompletionTick) {
        static final Codec<Machine> CODEC = RecordCodecBuilder.create(i -> i.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Machine::pos),
                Codec.STRING.fieldOf("inputTypeId").forGetter(Machine::inputTypeId),
                Codec.STRING.fieldOf("outputTypeId").forGetter(Machine::outputTypeId),
                Codec.LONG.fieldOf("durationTicks").forGetter(Machine::durationTicks),
                GlobalPos.CODEC.optionalFieldOf("outputPos").forGetter(Machine::outputPos),
                Codec.BOOL.fieldOf("crafting").forGetter(Machine::crafting),
                Codec.STRING.optionalFieldOf("pendingOutputTypeId").forGetter(Machine::pendingOutputTypeId),
                Codec.LONG.fieldOf("craftCompletionTick").forGetter(Machine::craftCompletionTick)
        ).apply(i, Machine::new));
    }


    /** The full snapshot of everything FactoryNetwork tracks*/
    record Snapshot(List<Producer> producers, List<Belt> belts, List<Consumer> consumers, List<Machine> machines) {
        static final Codec<Snapshot> CODEC = RecordCodecBuilder.create(i -> i.group(
                Producer.CODEC.listOf().fieldOf("producers").forGetter(Snapshot::producers),
                Belt.CODEC.listOf().fieldOf("belts").forGetter(Snapshot::belts),
                Consumer.CODEC.listOf().fieldOf("consumers").forGetter(Snapshot::consumers),
                Machine.CODEC.listOf().fieldOf("machines").forGetter(Snapshot::machines)
        ).apply(i, Snapshot::new));
    }
}

