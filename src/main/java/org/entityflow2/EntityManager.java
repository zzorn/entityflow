package org.entityflow2;

import org.entityflow2.component.ComponentType;
import org.entityflow2.group.EntityGroup;
import org.entityflow2.processor.Processor;
import org.flowutils.Symbol;
import org.flowutils.service.Service;
import org.flowutils.time.Time;
import org.flowutils.updating.Updating;

import java.util.List;

/**
 * Main facade for the entity component system.
 *
 * Manages a set of entities with components of some range of registered types, processed with some registered processors.
 */
public interface EntityManager extends Service, Updating {


    /**
     * Add a component type.
     * @return the added component type, for chaining or storing in a field or similar.
     */
    <T extends ComponentType> T addComponentType(T componentType);

    /**
     * Add a processor that gets called during updates, and can be used to process entities with some specified component types.
     * @return the added processor, for chaining or storing in a field or similar.
     */
    <T extends Processor> T addProcessor(T processor);

    /**
     * @return the component type with the specified id, or null if none found.
     */
    ComponentType getComponentType(Symbol componentTypeId);

    /**
     * Creates a new entity with the specified component types.
     * @return the id of the created entity
     */
    int createEntity(ComponentType ... initialComponentTypes);

    /**
     * Creates a new entity with the specified component types.
     * @return the id of the created entity
     */
    int createEntity(List<ComponentType> initialComponentTypes);

    /**
     * Deletes the specified entity.
     * If the entityId does not exist, it is ignored.
     */
    void removeEntity(int entityId);

    int getEntityCount();

    EntityGroup getEntityGroup(ComponentType ... componentTypes);

    /**
     * Initializes the EntityManager and the registered processors.
     */
    void init();

    /**
     * Call regularly from a simulation update loop (e.g. opengl game render call).
     * @param time contains information on time elapsed since the last call to update.  Does not have to correspond to wall-clock time.
     */
    void update(Time time);

    /**
     * Shuts down the EntityManager and the registered processors, freeing any used resources.
     * No updates or registrations are possible after shutdown.
     * Re-initialization after shutdown is not possible.
     */
    void shutdown();

    /**
     * Called by component types when a component was added to an entity.
     * @param entityId id of the entity that the component was added to.
     * @param componentType type of the added component.
     */
    void onComponentAdded(int entityId, ComponentType componentType);

    /**
     * Called by component types when a component was removed from an entity.
     * @param entityId id of the entity that the component was removed from.
     * @param componentType type of the removed component.
     */
    void onComponentRemoved(int entityId, ComponentType componentType);

}
