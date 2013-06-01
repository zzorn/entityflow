package org.entityflow.world;

import org.entityflow.util.Ticker;
import org.entityflow.component.Component;
import org.entityflow.entity.Entity;
import org.entityflow.system.EntitySystem;

/**
 * Manages all entities and systems in a game/simulation.
 */
public interface World {

    /**
     * Adds an entity system.  Should be done before calling initialize.
     */
    <T extends EntitySystem> T addSystem(T entitySystem);

    /**
     * @return the system of the specified type.  Throws an exception if not found.
     */
    <T extends EntitySystem> T getSystem(Class<T> systemType);

    /**
     * Initializes all systems.
     */
    void init();

    /**
     * Starts a world simulation loop that will repeatedly call process.
     * If init has not been called, this will call init first.
     */
    void start();

    /**
     * Starts a world simulation loop that will repeatedly call process.
     * If init has not been called, this will call init first.
     */
    void start(long simulationStepMilliseconds);

    /**
     * Will stop the main game loop, and shut down all systems, after the next game loop is completed,
     * or do the shutdown immediately if the game loop was not started.
     */
    void shutdown();

    /**
     * Add and delete any recently added/removed entities, then call process for each EntitySystem, in the order they were added,
     * letting them process the entities they are interested in.
     * @param ticker contains time since last frame and since the beginning of the simulation.
     */
    void process(Ticker ticker);

    /**
     * Creates a new entity and adds it to the world.
     * @param components initial components to add to the entity.
     * @return the created entity.
     */
    Entity createEntity(Component... components);

    /**
     * Removes an entity from the world, and recycles it.
     * @param entity entity to delete if found.
     */
    void deleteEntity(Entity entity);

    /**
     * @param entityId the id of the entity to get.
     * @return the entity with the specified id, or null if none found.
     */
    Entity getEntity(long entityId);

    /**
     * Notify the world when components are added or removed to an entity.  This is called automatically by an entity, no need to call manually.
     * Will notify EntitySystems about the change, so that they can decide if they should add or delete the entity.
     */
    void onEntityComponentsChanged(Entity entity);

}
