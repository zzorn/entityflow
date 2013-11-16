package org.entityflow.world;

import org.entityflow.system.Processor;
import org.entityflow.util.Ticker;
import org.entityflow.component.Component;
import org.entityflow.entity.Entity;

/**
 * Manages all entities and systems in a game/simulation.
 */
public interface World {

    /**
     * Adds a processor.  Should be done before calling init.
     */
    <T extends Processor> T addProcessor(T processor);

    /**
     * @return the processor of the specified type.  Throws an exception if not found.
     */
    <T extends Processor> T getProcessor(Class<T> processorType);

    /**
     * Initializes all processors.
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
     * Will stop the main game loop, and shut down all processors, after the next game loop is completed,
     * or do the shutdown immediately if the game loop was not started.
     */
    void shutdown();

    /**
     * Add and delete any recently added/removed entities, then call process for each Processor, in the order they were added,
     * letting them process the entities they are interested in.
     * @param ticker contains time since last frame and since the beginning of the simulation.
     */
    void process(Ticker ticker);

    /**
     * @param entityId the id of the entity to get.
     * @return the entity with the specified id, or null if none found.
     */
    Entity getEntity(long entityId);

    /**
     * Creates a new entity and adds it to the world.
     *
     * Can be done both before and after init is called on the World.
     *
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
     * Notify the world when components are added or removed to an entity.  This is called automatically by an entity, no need to call manually.
     * Will notify Processors about the change, so that they can decide if they should add or delete the entity from their lists of entities to process.
     */
    void onEntityComponentsChanged(Entity entity);

}
