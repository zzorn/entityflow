package org.entityflow.entity;

import org.entityflow.component.Component;
import org.entityflow.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An entity that exists in a World.  An entity has zero or more components, which contain data for various aspects
 * of the entity.  Entities are processed by Processors in the World, the Processors provide functionality for entities.
 */
public interface Entity {

    /**
     * @return id of this entity.  Unique within the world the entity belongs to.
     */
    long getId();

    /**
     * @return the world that this entity is stored in.
     */
    World getWorld();

    /**
     * Initializes a previously uninitialized entity.
     * Used internally by World when recycling entities.
     * Throws an error if called on an entity that has nonzero entity id or non-null world.
     * @param entityId entity id to give the new entity.  Must be unique over time.
     * @param world world that the new entity belongs to.
     */
    void init(long entityId, World world);

    /**
     * @return the component with the specified base type, or null if nor present in this entity.
     */
    <T extends Component> T get(Class<T> type);

    /**
     * Adds the specified component(s) to this entity.  The components will replace any previous components with the same base types.
     */
    void add(Component... components);

    /**
     * Removes the component of the specified base type from this entity.
     */
    <T extends Component> void remove(Class<T> type);

    /**
     * @return true if this entity contains a component with the specified base type.
     */
    <T extends Component> boolean has(Class<T> type);

    /**
     * Returns all components in this entity by adding them to the specified collection.
     * The collection is not initially cleared.
     */
    void getComponents(Collection<Component> componentsOut);

    /**
     * @return true if this entity contains all components of the specified type ids.
     */
    boolean hasAll(Set<Class<? extends Component>> componentTypes);

    /**
     * Removes this entity from the game world on the next world process update.
     */
    void delete();

    /**
     * Called when this entity was removed from the game world, during world processing startup phase.
     * Prepares the entity for recycling.
     */
    void onDeleted();

    /**
     * Sends the specified message to this entity.
     * A suitable message handler will handle it during the next update pass.
     *
     * @param message the message to handle.
     * @param externalSource true if the message originated from an external source such as a player client.
     *                       false if it is from a system inside the simulation, such as a Processor.
     */
    void sendMessage(Message message, boolean externalSource);

}
