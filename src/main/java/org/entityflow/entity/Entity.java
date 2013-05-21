package org.entityflow.entity;

import org.entityflow.component.Component;
import org.entityflow.world.World;

import java.util.Map;
import java.util.Set;

/**
 * An entity that exists in a World.  An entity has zero or more components, which contain data for various aspects
 * of the entity.  Entities are processed by Systems in the World, the Systems provide functionality for entities.
 */
public interface Entity {

    /**
     * @return id of this entity.  Unique within the world the entity belongs to.
     */
    long getEntityId();

    /**
     * @return the world that this entity is stored in.
     */
    World getWorld();

    /**
     * @return the component with the specified base type, or null if nor present in this entity.
     */
    <T extends Component> T getComponent(Class<T> type);

    /**
     * Adds the specified component to this entity.  The component will replace any previous component with the same base type.
     */
    void addComponent(Component component);

    /**
     * Removes the component of the specified base type from this entity.
     */
    <T extends Component> void removeComponent(Class<T> type);

    /**
     * @return true if this entity contains a component with the specified base type.
     */
    <T extends Component> boolean containsComponent(Class<T> type);

    /**
     * Returns all components in this entity, by adding them to the specified map, as mappings from component base type to component.
     */
    // TODO: Restore version that just returns map?
    void getComponents(Map<Class<? extends Component>, Component> componentsOut);

    /**
     * @return true if this entity contains all components of the specified type ids.
     */
    boolean containsAllComponents(Set<Class<? extends Component>> componentTypes);

    /**
     * Removes this entity from the game world on the next world process update.
     */
    void remove();

    /**
     * Called when this entity was removed from the game world, during world processing startup phase.
     * Prepares the entity for recycling.
     */
    void onRemoved();

}
