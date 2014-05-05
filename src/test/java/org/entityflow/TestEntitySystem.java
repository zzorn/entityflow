package org.entityflow;

import org.entityflow.component.ComponentBase;
import org.entityflow.entity.Entity;
import org.entityflow.entity.Message;
import org.entityflow.persistence.PersistenceService;
import org.entityflow.system.EntityProcessorBase;
import org.entityflow.system.MessageHandler;
import org.entityflow.world.ConcurrentWorldBase;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * High level test of the system.
 */
public class TestEntitySystem {

    @org.junit.Test
    public void testSystem() throws Exception {
        // Create world
        final ManualTime worldTime = new ManualTime();
        ConcurrentWorldBase world = new ConcurrentWorldBase(worldTime, new TestPersistence());

        // Add a processor
        final TestProcessorBase testProcessor = new TestProcessorBase();
        world.addProcessor(testProcessor);
        assertEquals("World should have the test processor we specified",
                     testProcessor, world.getProcessor(TestProcessorBase.class));

        // Initialize world
        world.init();

        // Add an entity
        final TestComponentBase testComponent = new TestComponentBase();
        final Entity entity = world.createEntity(testComponent);

        assertNotNull("Entity should have been created", entity);

        assertNotEquals("Entity should have a valid id", 0, entity.getId());

        assertEquals("Entity should have the component we specified",
                     testComponent, entity.get(TestComponentBase.class));

        assertEquals("Entity should know the world it is in", world, entity.getWorld());

        // Simulate
        final TestComponentBase testComponent2 = entity.get(TestComponentBase.class);
        assertEquals("No tick should have been logged", 0, testComponent2.counter);

        worldTime.advanceTime(2500);
        worldTime.nextStep();
        world.process();

        final TestComponentBase testComponent3 = entity.get(TestComponentBase.class);
        assertEquals("One tick should have been logged", 1, testComponent3.counter);

        // Test entity deleted
        final long id = entity.getId();
        assertEquals(entity, world.getEntity(id));
        world.deleteEntity(entity);

        worldTime.advanceTime(2500);
        worldTime.nextStep();
        world.process();

        assertEquals(null, world.getEntity(id));
        assertEquals(1, testComponent.counter);

        // Test recreate (should use pooled entity)
        final Entity entity2 = world.createEntity(new TestComponentBase());
        assertEquals(2, entity2.getId());
        assertTrue(entity == entity2);

        final Entity entity3 = world.createEntity(new TestComponentBase());
        assertEquals(3, entity3.getId());
        assertTrue(entity != entity3);

    }


    @Test
    public void testMessaging() throws Exception {

        // Create world
        final TestPersistence persistence = new TestPersistence();
        final ManualTime time = new ManualTime();
        ConcurrentWorldBase world = new ConcurrentWorldBase(time, persistence);

        // Add message handler
        final List<String> receivedMessages = new ArrayList<String>();
        world.addMessageHandler(TestMessage.class, new MessageHandler<TestMessage>() {
            @Override public boolean handleMessage(Entity entity, TestMessage message) {
                receivedMessages.add(message.content);
                return true;
            }
        });

        // Add an entity
        final TestComponentBase testComponent = new TestComponentBase();
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
    }

    private class TestComponentBase extends ComponentBase {
        public int counter = 0;
    }

    private class TestProcessorBase extends EntityProcessorBase {
        protected TestProcessorBase() {
            super(null, 1, TestComponentBase.class);
        }

        @Override protected void processEntity(Time time, Entity entity) {
            final TestComponentBase testComponent = entity.get(TestComponentBase.class);
            testComponent.counter++;
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
