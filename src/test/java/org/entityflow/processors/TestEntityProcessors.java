package org.entityflow.processors;

import org.entityflow.component.ComponentBase;
import org.entityflow.entity.Entity;
import org.entityflow.entity.Message;
import org.entityflow.persistence.PersistenceService;
import org.entityflow.world.ConcurrentWorld;
import org.flowutils.Stopwatch;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * High level test of the processors.
 */
public class TestEntityProcessors {

    @Test
    public void testSystem() throws Exception {
        // Create world
        final ManualTime worldTime = new ManualTime();
        ConcurrentWorld world = new ConcurrentWorld(worldTime, new TestPersistence());

        // Add a processor
        final TestProcessor testProcessor = new TestProcessor(false, 1, 10);
        world.addProcessor(testProcessor);
        assertEquals("World should have the test processor we specified",
                     testProcessor, world.getProcessor(TestProcessor.class));

        // Initialize world
        world.init();

        // Add an entity
        final TestComponent testComponent = new TestComponent();
        final Entity entity = world.createEntity(testComponent);

        assertNotNull("Entity should have been created", entity);

        assertNotEquals("Entity should have a valid id", 0, entity.getId());

        assertEquals("Entity should have the component we specified",
                     testComponent, entity.get(TestComponent.class));

        assertEquals("Entity should know the world it is in", world, entity.getWorld());

        // Simulate
        final TestComponent testComponent2 = entity.get(TestComponent.class);
        assertEquals("No tick should have been logged", 0, testComponent2.counter);

        worldTime.advanceTime(2500);
        worldTime.nextStep();
        world.process();

        final TestComponent testComponent3 = entity.get(TestComponent.class);
        assertEquals("Two ticks should have been logged", 2, testComponent3.counter);

        // Test entity deleted
        final long id = entity.getId();
        assertEquals(entity, world.getEntity(id));
        world.deleteEntity(entity);

        worldTime.advanceTime(2500);
        worldTime.nextStep();
        world.process();

        assertEquals(null, world.getEntity(id));
        assertEquals(2, testComponent.counter);

        // Test recreate (should use pooled entity)
        final Entity entity2 = world.createEntity(new TestComponent());
        assertEquals(2, entity2.getId());
        assertTrue(entity == entity2);

        final Entity entity3 = world.createEntity(new TestComponent());
        assertEquals(3, entity3.getId());
        assertTrue(entity != entity3);

        // Shutdown
        world.shutdown();
    }

    @Test
    public void testMessaging() throws Exception {

        // Create world
        final TestPersistence persistence = new TestPersistence();
        final ManualTime time = new ManualTime();
        ConcurrentWorld world = new ConcurrentWorld(time, persistence);

        // Add message handler
        final List<String> receivedMessages = new ArrayList<String>();
        world.addMessageHandler(TestMessage.class, new MessageHandler<TestMessage>() {
            @Override public boolean handleMessage(Entity entity, TestMessage message) {
                receivedMessages.add(message.content);
                return true;
            }
        });

        // Add an entity
        final TestComponent testComponent = new TestComponent();
        final Entity entity = world.createEntity(testComponent);

        // Initialize world
        world.init();

        entity.sendMessage(new TestMessage("msg1int"), false);
        entity.sendMessage(new TestMessage("msg2ext"), true);

        world.sendMessage(entity, new TestMessage("msg3ext"), true);
        world.sendMessage(entity, new TestMessage("msg4int"), false);

        world.sendMessage(entity.getId(), new TestMessage("msg5int"), false);
        world.sendMessage(entity.getId(), new TestMessage("msg6ext"), true);

        assertArrayEquals(new String[]{}, receivedMessages.toArray());

        world.process();

        world.sendMessage(entity, new TestMessage("msg7ext"), true);

        assertArrayEquals(new String[]{"msg1int", "msg2ext", "msg3ext", "msg4int", "msg5int", "msg6ext"},
                          receivedMessages.toArray());

        world.process();

        assertArrayEquals(new String[]{"msg1int", "msg2ext", "msg3ext", "msg4int", "msg5int", "msg6ext", "msg7ext"},
                          receivedMessages.toArray());

        world.sendMessage(entity, new TestMessage("msg8ext"), true);

        assertArrayEquals(new String[]{"msg2ext", "msg3ext", "msg6ext", "msg7ext", "msg8ext"}, persistence.messages.toArray());
        assertArrayEquals(new Long[]{1L, 1L, 1L, 1L, 1L}, persistence.entityIds.toArray());
        assertArrayEquals(new Long[]{0L, 0L, 0L, 1L, 2L}, persistence.ticks.toArray());

        world.process();

        assertArrayEquals(new String[]{"msg1int", "msg2ext", "msg3ext", "msg4int", "msg5int", "msg6ext", "msg7ext", "msg8ext"}, receivedMessages.toArray());

        // Shutdown
        world.shutdown();
    }

    @org.junit.Test
    public void testLotsOfComponents() throws Exception {
        //timeProcessLots(false, 100, 10000);
        //timeProcessLots(true,  100, 10000);
        timeProcessLots(false, 10000, 10, 1000);
        timeProcessLots(true,  10000, 10, 1000);
        timeProcessLots(false, 1000, 100, 10000);
        timeProcessLots(true,  1000, 100, 10000);
        timeProcessLots(false, 1000, 100, 100000);
        timeProcessLots(true,  1000, 100, 100000);
        //timeProcessLots(false, 1000000, 1);
        //timeProcessLots(true,  1000000, 1);
    }

    private void timeProcessLots(boolean concurrentProcessing, final int entityCount, final int simulationLoops, final int entityUpdateHeaviness) {
        final int laps = 10;
        final int lapsToUse = laps / 3;
        final int lapsToDiscard = laps - lapsToUse;

        String config = "Time with "+
                        entityCount+" entities, "+
                        simulationLoops+" simulation loops, "+
                        entityUpdateHeaviness+" entity update load, " +
                        (concurrentProcessing?"concurrently" : "singlethread") + " " +
                        "for ";

        Stopwatch setupStopwatch    = new Stopwatch(config + "Setup     ", lapsToDiscard, true);
        Stopwatch simulateStopwatch = new Stopwatch(config + "Simulation", lapsToDiscard, true);
        Stopwatch testStopwatch     = new Stopwatch(config + "Testing   ", lapsToDiscard, true);
        Stopwatch shutdownStopwatch = new Stopwatch(config + "Shutdown  ", lapsToDiscard, true);
        for (int i = 0; i < laps; i++) {
            processLotsOfComponents(concurrentProcessing, entityCount, simulationLoops, entityUpdateHeaviness,
                                    setupStopwatch, simulateStopwatch, testStopwatch, shutdownStopwatch);
            setupStopwatch.lap();
            simulateStopwatch.lap();
            testStopwatch.lap();
            shutdownStopwatch.lap();
        }

        //setupStopwatch.printResult();
        simulateStopwatch.printResult();
        //testStopwatch.printResult();
        //shutdownStopwatch.printResult();
    }

    private void processLotsOfComponents(final boolean concurrentProcessing,
                                         final int entityCount,
                                         final int simulationLoops,
                                         final int entityUpdateHeaviness,
                                         Stopwatch setupStopwatch,
                                         Stopwatch simulateStopwatch,
                                         Stopwatch testStopwatch,
                                         Stopwatch shutdownStopwatch) {

        // Setup phase
        setupStopwatch.resume();

        // Create world
        final ManualTime worldTime = new ManualTime();
        ConcurrentWorld world = new ConcurrentWorld(worldTime, new TestPersistence());

        // Add a processor
        final TestProcessor testProcessor = new TestProcessor(concurrentProcessing, 1, entityUpdateHeaviness);
        world.addProcessor(testProcessor);

        // Initialize world
        world.init();

        // Add entities
        for (int i = 0; i < entityCount; i++) {
            world.createEntity(new TestComponent());
        }

        // Simulation phase
        setupStopwatch.pause();
        simulateStopwatch.resume();

        // Simulate
        for (int i = 0; i < simulationLoops; i++) {
            // Simulate
            worldTime.advanceTime(1000);
            worldTime.nextStep();
            world.process();

            // Create one entity
            world.createEntity(new TestComponent(i+1));

            // Remove one entity
            final Entity entity = world.getEntities().iterator().next();
            world.deleteEntity(entity);
        }

        // Test phase
        simulateStopwatch.pause();
        testStopwatch.resume();

        // Check entity balance between sets
        int size = -1;
        for (Set<Entity> entitySet : testProcessor.getHandledComponentSets()) {
            if (size < 0) {
                size = entitySet.size();
            }
            else {
                assertEquals("All entity sets should have nearly the same size", size, entitySet.size(), 2.1);
            }
        }


        // Check that number of invocations is correct
        for (Entity entity : world.getEntities()) {
            final TestComponent testComponent = entity.get(TestComponent.class);
            assertEquals("Number of calls should be same as loops", simulationLoops, testComponent.counter);
        }


        // Shutdown phase
        testStopwatch.pause();
        shutdownStopwatch.resume();

        world.shutdown();

        shutdownStopwatch.pause();
    }

    private class TestComponent extends ComponentBase {
        public int counter = 0;

        private TestComponent() {
        }

        private TestComponent(int counter) {
            this.counter = counter;
        }
    }

    private class TestProcessor extends EntityProcessorBase {
        private final int heaviness;

        protected TestProcessor(final boolean concurrentProcessing, final double processingIntervalSeconds, final int heaviness) {
            super(null, processingIntervalSeconds, concurrentProcessing, TestComponent.class);
            this.heaviness = heaviness;
        }

        @Override protected void processEntity(Time time, Entity entity) {
            final TestComponent testComponent = entity.get(TestComponent.class);
            testComponent.counter++;

            // Do some busywork
            int steps = heaviness;
            testComponent.counter += steps;
            for (int i = 0; i < steps; i++) {
                testComponent.counter--;
            }
        }
    }

    private class TestPersistence implements PersistenceService {
        public List<String> messages = new ArrayList<String>();
        public List<Long> ticks= new ArrayList<Long>();
        public List<Long> entityIds= new ArrayList<Long>();

        @Override public void storeExternalMessage(long simulationTick, long recipientEntity, Message message) {
            ticks.add(simulationTick);
            entityIds.add(recipientEntity);
            messages.add(((TestMessage)message).content);
        }
    }

    private class TestMessage implements Message {
        public final String content;

        private TestMessage(String content) {
            this.content = content;
        }
    }

}
