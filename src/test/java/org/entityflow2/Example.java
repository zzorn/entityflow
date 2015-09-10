package org.entityflow2;

/**
 *
 */
public class Example {
    public static void main(String[] args) {
        // Setup
        EntityManager entityManager = new ConcurrentEntityManager();
        final Position position = entityManager.registerComponentType(new Position());
        final Physical physical = entityManager.registerComponentType(new Physical());

        final PhysicsProcessor physicsProcessor = entityManager.registerProcessor(new PhysicsProcessor());

        // Create entity
        final int entity = entityManager.createEntity(position);
        physical.addToEntity(entity);


        // Initialize entity
        position.x.set(entity, 100.0);
        position.y.set(entity, 43);
        position.z.set(entity, -20.1);
        physical.mass.set(entity, 4.1);
        physical.material.set(entity, Material.getMaterial("wood"));

    }



}
