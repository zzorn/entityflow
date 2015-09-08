package org.entityflow2.group;

/**
 * Listener that gets notified when entities are added or removed from an entity group.
 */
public interface EntityGroupListener {

    /**
     * Called when an entity is added to the group.
     */
    void onEntityAdded(EntityGroup group, int entityId);

    /**
     * Called when an entity is removed from to the group.
     */
    void onEntityRemoved(EntityGroup group, int entityId);

}
