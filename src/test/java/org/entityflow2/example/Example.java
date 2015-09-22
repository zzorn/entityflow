package org.entityflow2.example;

import org.entityflow2.ConcurrentEntityManager;
import org.entityflow2.EntityManager;
import org.flowutils.Stopwatch;
import org.flowutils.random.RandomSequence;
import org.flowutils.random.XorShift;
import org.flowutils.time.ManualTime;
import org.flowutils.updating.strategies.CappedTimestepStrategy;

/**
 *
 */
public class Example {
    public static void main(String[] args) {
        // Setup
        EntityManager entityManager = new ConcurrentEntityManager(new CappedTimestepStrategy(0.1));
        final Position position = entityManager.addComponentType(new Position());
        final Physical physical = entityManager.addComponentType(new Physical());

        final PhysicsProcessor physicsProcessor = entityManager.addProcessor(new PhysicsProcessor(position, physical));

        // Create entity
        final int entity = entityManager.createEntity(position);
        physical.addToEntity(entity);

        final int entity2 = entityManager.createEntity(position, physical);
        final int entity3 = entityManager.createEntity(position, physical);

        // Initialize entity
        position.x.set(entity, 100.0);
        position.y.set(entity, 43);
        position.z.set(entity, -20.1);
        physical.mass.set(entity, 4.1);
        physical.material.set(entity, Material.getMaterial("wood"));

        // Initialize entity manager
        entityManager.init();

        createAndRemoveEntities(entityManager, position, physical, 100000);

        // Run some updates
        Stopwatch stopwatch = new Stopwatch("Immaginary physics");
        ManualTime time = new ManualTime();
        for (int i = 0; i < 1000; i++) {
            System.out.println("lap " + i);
            System.out.println("entity count = " + entityManager.getEntityCount());

            time.advanceTimeSeconds(0.01);
            time.nextStep();

            entityManager.update(time);
            stopwatch.lap();
            stopwatch.printResult();
            createAndRemoveEntities(entityManager, position, physical, 10);
        }
        stopwatch.printResult();
    }

    private static void createAndRemoveEntities(EntityManager entityManager,
                                                Position position,
                                                Physical physical,
                                                final int num) {
        RandomSequence randomSequence = new XorShift();
        for (int i = 0; i < num; i++) {
            if (randomSequence.nextBoolean(0.25)) {
                entityManager.createEntity(position, physical);
            }
            if (randomSequence.nextBoolean(0.25)) {
                entityManager.createEntity(physical);
            }
            if (randomSequence.nextBoolean(0.25)) {
                entityManager.createEntity(position);
            }
            if (randomSequence.nextBoolean(0.25)) {
                entityManager.createEntity();
            }

            if (randomSequence.nextBoolean(0.3)) {
                entityManager.removeEntity(randomSequence.nextInt(num));
            }
        }
    }


}
