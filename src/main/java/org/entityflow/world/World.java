package org.entityflow.world;

import org.entityflow.entity.Message;
import org.entityflow.persistence.PersistenceService;
import org.entityflow.processors.MessageHandler;
import org.entityflow.processors.Processor;
import org.entityflow.component.Component;
import org.entityflow.entity.Entity;
import org.flowutils.time.Time;

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
     * Add a handler for the specified type of message.
     *
     * Must be called before init() or start().
     *
     * @param handledMessageType the exact type of messages that will be handled by this handler.  Subtypes of this message type will not be handled.
     * @param messageHandler the handler for the messages.
     * @return the messageHandler
     */
    <T extends Message> MessageHandler<T> addMessageHandler(Class<T> handledMessageType, MessageHandler<T> messageHandler);

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
     */
    void process();

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
     * Sends a message to the specified entity.
     * The message will be handled by any suitable MessageProcessor next time process() is called.
     * The message may also be serialized to disk if it is from an external source, to allow recovery in case of a crash.
     *
     * @param entity entity to send message to
     * @param message message to send
     * @param externalSource true if the message originates from outside the world simulation, e.g. from a player client.
     *                       false if the message originates from inside the world, e.g. a Processor.
     */
    void sendMessage(Entity entity, Message message, boolean externalSource);

    /**
     * Sends a message to the specified entity.
     * The message will be handled by any suitable MessageProcessor next time process() is called.
     * The message may also be serialized to disk if it is from an external source, to allow recovery in case of a crash.
     *
     * @param entityId entity to send message to
     * @param message message to send
     * @param externalSource true if the message originates from outside the world simulation, e.g. from a player client.
     *                       false if the message originates from inside the world, e.g. a Processor.
     */
    void sendMessage(long entityId, Message message, boolean externalSource);

    /**
     * Notify the world when components are added or removed to an entity.  This is called automatically by an entity, no need to call manually.
     * Will notify Processors about the change, so that they can decide if they should add or delete the entity from their lists of entities to process.
     */
    void onEntityComponentsChanged(Entity entity);

    /**
     * @return number of simulation passes that have been handled by the world so far.
     *         Starts at zero and is increased each time process is called.
     */
    long getSimulationTick();

    /**
     * @return the persistence service used by the World.
     */
    PersistenceService getPersistenceService();

    /**
     * @return Time used by this World.
     */
    Time getTime();
}
