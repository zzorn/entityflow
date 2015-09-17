package org.entityflow2.example;

import org.entityflow2.ConcurrentEntityManager;
import org.entityflow2.EntityManager;
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


        // Initialize entity
        position.x.set(entity, 100.0);
        position.y.set(entity, 43);
        position.z.set(entity, -20.1);
        physical.mass.set(entity, 4.1);
        physical.material.set(entity, Material.getMaterial("wood"));

        // Run some updates
        ManualTime time = new ManualTime();
        for (int i = 0; i < 100; i++) {
            time.advanceTimeSeconds(0.1);
            time.nextStep();

            entityManager.update(time);
        }

    }



}
