package com.example.examplemod;

import com.example.examplemod.engine_internal.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EngineTest {

    @Test
    @DisplayName("Every produced payload eventually reaches the consumer")
    void producerBeltConsumerBasicFlow() {
        Scheduler scheduler = new Scheduler();
        Consumer consumer = new Consumer(5, 2);
        Belt belt = new Belt(10, 0.1);
        belt.setOutput(consumer);
        Producer producer = new Producer("ore", 5, belt, scheduler);

        long t = 0;
        for (; t < 50; t++) {
            scheduler.tick(t);
            belt.tick(t);
            consumer.tick(t);
            producer.tick(t);
        }
        int producedSoFar = 9;

        for (int i = 0; i < 30; i++, t++) {
            belt.tick(t);
            consumer.tick(t);
        }

        assertEquals(producedSoFar, consumer.getConsumedCount(), "every produced item should reach the consumer");
        assertFalse(producer.isBlocked());
        assertEquals(0, belt.getItemCount(), "belt should fully drain once production stops feeding it");
    }

    @Test
    @DisplayName("A slow/full consumer backs up the belt, then blocks the producer")
    void backpressurePropagatesToProducer() {
        Scheduler scheduler = new Scheduler();
        Consumer consumer = new Consumer( 1, 1000);
        Belt belt = new Belt(5, 0.3);
        belt.setOutput(consumer);
        Producer producer = new Producer("ore", 3, belt, scheduler);

        for (long t = 0; t < 50; t++) {
            scheduler.tick(t);
            belt.tick(t);
            consumer.tick(t);
            producer.tick(t);
        }

        assertTrue(producer.isBlocked(), "producer should block once belt/consumer fill up");
        assertEquals(1, consumer.getBufferedCount());
        assertTrue(belt.getItemCount() > 0, "belt should hold jammed items instead of losing them");
        assertEquals(0, consumer.getConsumedCount(), "nothing should finish processing");
    }

    @Test
    @DisplayName("A jam clears once the consumer frees up, and flow resumes")
    void jamClearsAndFlowResumes() {
        Scheduler scheduler = new Scheduler();
        Consumer consumer = new Consumer(1, 3);
        Belt belt = new Belt(4, 0.3);
        belt.setOutput(consumer);
        Producer producer = new Producer("ore", 1, belt, scheduler);

        for (long t = 0; t < 200; t++) {
            scheduler.tick(t);
            belt.tick(t);
            consumer.tick(t);
            producer.tick(t);
        }

        assertTrue(consumer.getConsumedCount() > 5, "flow should resume repeatedly after jams, not permanently stall");
        assertTrue(belt.getItemCount() > 0, "belt should still hold a queued item (production outpaces consumption)");
    }

    @Test
    @DisplayName("Items on a belt never violate the minimum gap, even under maximum backpressure")
    void beltMinGapNeverViolated() {
        Port neverAccepts = new Port() {
            @Override public boolean canAccept(Payload payload) { return false; }
            @Override public void accept(Payload payload) { fail("should never be called"); }
        };

        Scheduler scheduler = new Scheduler();
        Belt belt = new Belt(10, 0.25);
        belt.setOutput(neverAccepts);
        Producer producer = new Producer("ore", 1, belt, scheduler);

        for (long t = 0; t < 100; t++) {
            scheduler.tick(t);
            belt.tick(t);
            producer.tick(t);
        }

        List<Double> positions = belt.getPositions(); // front-to-back
        for (int i = 1; i < positions.size(); i++) {
            double gap = positions.get(i - 1) - positions.get(i);
            assertTrue(gap >= 0.25 - 1e-6, "gap between items should never be below minGap");
        }
        assertTrue(positions.size() > 1, "multiple items should have queued up on the belt");
        assertTrue(producer.isBlocked(), "producer should block once the belt itself fills up");
    }

    @Test
    @DisplayName("Disabling a producer stops production; resuming retimes it")
    void producerPauseResumeTimedFromRealResume() {
        Scheduler scheduler = new Scheduler();
        Consumer sink = new Consumer(100, 0);
        Producer producer = new Producer("ore", 10, sink, scheduler);

        for (long t = 0; t < 25; t++) {
            scheduler.tick(t);
            sink.tick(t);
        }
        assertEquals(2, sink.getConsumedCount());

        producer.setActive(false);
        for (long t = 25; t < 60; t++) {
            scheduler.tick(t);
            sink.tick(t);
        }
        assertEquals(2, sink.getConsumedCount(), "no items should be produced while disabled");

        scheduler.tick(60);
        sink.tick(60);
        producer.setActive(true);

        for (long t = 61; t < 70; t++) {
            scheduler.tick(t);
            sink.tick(t);
        }
        assertEquals(2, sink.getConsumedCount(), "not done yet one tick before the resumed completion");

        scheduler.tick(70);
        sink.tick(70);
        assertEquals(3, sink.getConsumedCount(), "next item lands at exactly 60 + 10 = 70");
    }
}
