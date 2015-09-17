package org.entityflow2.group;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import net.openhft.koloboke.collect.set.IntSet;
import org.entityflow2.component.ComponentType;

/**
 * Group with the entities that have (or do not have) some specific component types.
 */
public interface EntityGroup {

    /**
     * @return a read only view of the entities in this group.
     */
    IntSortedSet getEntities();

    /**
     * Called after a group has been created, for each entity that existed before the group was created.
     */
    void handleExistingEntity(int entityId);

    /**
     * Called when an entity has been created
     * @param initialComponentTypes the component types that the entity currently has.
     */
    void handleEntityCreated(int entityId, ComponentType[] initialComponentTypes);

    /**
     * Called when an entity is removed.
     */
    void handleEntityRemoved(int entityId);

    /**
     * Called when the specified component types is added to an entity.
     */
    void handleComponentAdded(int entityId, ComponentType addedComponentType);

    /**
     * Called when the specified component types is removed from an entity.
     */
    void handleComponentRemoved(int entityId, ComponentType removedComponentType);

    /**
     * Notifies listeners about additions and removals in this group since the last call to update.
     */
    void update();

    /**
     * @param listener listener to notify about entities added and removed from this group.
     */
    void addListener(EntityGroupListener listener);

    /**
     * @param listener listener to remove.
     */
    void removeListener(EntityGroupListener listener);

    /**
     * @return true if this group has exactly the specified required components, and no forbidden components.
     */
    boolean matches(ComponentType[] requiredComponents);

    /**
     * @return true if this group has exactly the specified required and forbidden components.
     */
    boolean matches(ComponentType[] requiredComponents, ComponentType[] forbiddenComponents);

}
