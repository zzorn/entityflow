package org.entityflow;

import org.entityflow.component.BaseComponent;
import org.entityflow.component.Component;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.entityflow.system.Processor;
import org.entityflow.util.Ticker;
import org.entityflow.world.BaseWorld;
import org.entityflow.world.ConcurrentWorld;
import org.junit.Assert;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * High level test of the system.
 */
public class TestEntitySystem {

    @org.junit.Test
    public void testSystem() throws Exception {
        // Create world
        ConcurrentWorld world = new ConcurrentWorld();

        // Add a processor
        final TestProcessor testProcessor = new TestProcessor();
        world.addProcessor(testProcessor);
        assertEquals("World should have the test processor we specified",
                     testProcessor, world.getProcessor(TestProcessor.class));

        // Initialize world
        world.init();

        // Add an entity
        final TestComponent testComponent = new TestComponent();
        final Entity entity = world.createEntity(testComponent);

        assertNotNull("Entity should have been created", entity);

        assertNotEquals("Entity should have a valid id", 0, entity.getEntityId());

        assertEquals("Entity should have the component we specified",
                     testComponent, entity.getComponent(TestComponent.class));

        assertEquals("Entity should know the world it is in", world, entity.getWorld());

        // Simulate
        final TestComponent testComponent2 = entity.getComponent(TestComponent.class);
        assertEquals("No tick should have been logged", 0, testComponent2.counter);

        final Ticker ticker = new Ticker(); // TODO: Ticker with manually advanceable time.
        ticker.tick();
        Thread.sleep(2500);
        world.process(ticker);

        final TestComponent testComponent3 = entity.getComponent(TestComponent.class);
        assertEquals("One tick should have been logged", 1, testComponent3.counter);

        // Test entity deleted
        final long id = entity.getEntityId();
        assertEquals(entity, world.getEntity(id));
        world.deleteEntity(entity);
        ticker.tick();
        Thread.sleep(2500);
        world.process(ticker);
        assertEquals(null, world.getEntity(id));
        assertEquals(1, testComponent.counter);

        // Test recreate (should use pooled entity)
        final Entity entity2 = world.createEntity(new TestComponent());
        assertEquals(2, entity2.getEntityId());
        assertTrue(entity == entity2);

        final Entity entity3 = world.createEntity(new TestComponent());
        assertEquals(3, entity3.getEntityId());
        assertTrue(entity != entity3);

    }

    private class TestComponent extends BaseComponent {
        public int counter = 0;
    }

    private class TestProcessor extends BaseEntityProcessor {
        protected TestProcessor() {
            super(null, 1, TestComponent.class);
        }

        @Override protected void processEntity(Ticker ticker, Entity entity) {
            final TestComponent testComponent = entity.getComponent(TestComponent.class);
            testComponent.counter++;
        }
    }

}
