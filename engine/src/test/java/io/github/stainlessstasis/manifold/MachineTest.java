//package io.github.stainlessstasis.manifold;
//
//import io.github.stainlessstasis.manifold.factory_component.Machine;
//import io.github.stainlessstasis.manifold.factory_component.Payload;
//import io.github.stainlessstasis.manifold.factory_component.Port;
//import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MachineTest {
//    static class Collector implements Port {
//        final List<Payload> received = new ArrayList<>();
//        boolean accepting = true;
//
//        @Override public boolean canAccept(Payload payload) { return accepting; }
//        @Override public void accept(Payload payload) { received.add(payload); }
//    }
//
//    @Nested
//    @DisplayName("Recipe completion timing")
//    class CompletionTiming {
//
//        @Test
//        @DisplayName("Completes at exactly startTick + durationTicks")
//        void completesAtExactNonAlignedTick() {
//            Scheduler scheduler = new Scheduler();
//            Collector output = new Collector();
//            MachineRecipe recipe = new MachineRecipe("ore", "plate", 27);
//            Machine machine = new Machine(recipe, scheduler, output);
//
//            scheduler.tick(5);
//            machine.accept(new Payload("ore")); // completes at tick 32
//
//            for (long t = 6; t < 32; t++) {
//                scheduler.tick(t);
//                machine.tick(t);
//                assertTrue(output.received.isEmpty(), "no output yet at tick " + t);
//            }
//
//            scheduler.tick(32);
//            machine.tick(32);
//            assertEquals(1, output.received.size(), "output should appear at exactly tick 32 (5 + 27)");
//        }
//
//        @Test
//        @DisplayName("Does not fire early or late relative to the nearest 20th tick")
//        void doesNotRoundToNearestSecondBoundary() {
//            Scheduler scheduler = new Scheduler();
//            Collector output = new Collector();
//            MachineRecipe recipe = new MachineRecipe("ore", "plate", 27);
//            Machine machine = new Machine(recipe, scheduler, output);
//
//            scheduler.tick(0);
//            machine.accept(new Payload("ore")); // completes at tick 27
//
//            scheduler.tick(20);
//            machine.tick(20);
//            assertTrue(output.received.isEmpty(), "should not round down to tick 20");
//
//            scheduler.tick(26);
//            machine.tick(26);
//            assertTrue(output.received.isEmpty(), "should not be done one tick early");
//
//            scheduler.tick(27);
//            machine.tick(27);
//            assertEquals(1, output.received.size(), "should be done at exactly tick 27, not rounded up to 40");
//        }
//    }
//
//    @Nested
//    @DisplayName("Backpressure / Jamming")
//    class Jamming {
//
//        @Test
//        @DisplayName("Machine refuses new input while crafting or while holding an unflushed finished item")
//        void blocksNewInputWhileCraftingOrJammed() {
//            Scheduler scheduler = new Scheduler();
//            Collector output = new Collector();
//            output.accepting = false;
//            MachineRecipe recipe = new MachineRecipe("ore", "plate", 10);
//            Machine machine = new Machine(recipe, scheduler, output);
//
//            scheduler.tick(0);
//            machine.accept(new Payload("ore"));
//            assertFalse(machine.canAccept(new Payload("ore")), "can't feed a second item while crafting");
//
//            scheduler.tick(10);
//            machine.tick(10);
//            assertTrue(machine.hasPendingOutput(), "finished item should be stuck because output refuses it");
//            assertFalse(machine.canAccept(new Payload("ore")), "still can't accept new input while jammed");
//        }
//
//        @Test
//        @DisplayName("Jam clears once output accepts again; a new recipe is timed from the new restart, not the old one")
//        void jamClearsAndNewRecipeTimedFromRealStart() {
//            Scheduler scheduler = new Scheduler();
//            Collector output = new Collector();
//            output.accepting = false;
//            MachineRecipe recipe = new MachineRecipe("ore", "plate", 10);
//            Machine machine = new Machine(recipe, scheduler, output);
//
//            scheduler.tick(0);
//            machine.accept(new Payload("ore")); // completes at tick 10, but jammed
//
//            scheduler.tick(10);
//            machine.tick(10);
//            assertTrue(machine.hasPendingOutput());
//
//            output.accepting = true; // jam clears at tick 15
//            scheduler.tick(15);
//            machine.tick(15);
//            assertFalse(machine.hasPendingOutput());
//            assertEquals(1, output.received.size());
//
//            assertTrue(machine.canAccept(new Payload("ore")));
//            machine.accept(new Payload("ore")); // should complete at tick 25
//
//            scheduler.tick(24);
//            machine.tick(24);
//            assertEquals(1, output.received.size(), "second item not done yet at tick 24");
//
//            scheduler.tick(25);
//            machine.tick(25);
//            assertEquals(2, output.received.size(), "second item done at exactly tick 25 (15 + 10)");
//        }
//    }
//}
//
