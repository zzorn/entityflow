package org.entityflow.entity;

import org.entityflow.component.Component;
import org.entityflow.world.World;

/**
 * Common functionality for entities.
 */
public abstract class BaseEntity implements Entity {

    /**
     * Unique id for this entity within the World.
     */
    private long entityId;

    /**
     * Host world that the entity belongs to.
     */
    private World world;

    /**
     * @param world world that this entity exists in.
     * @param entityId unique id for the entity within the world.
     */
    protected BaseEntity(long entityId, World world) {
        this.entityId = entityId;
        this.world = world;
    }

    @Override
    public void remove() {
        world.removeEntity(this);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public long getEntityId() {
        return entityId;
    }

    @Override public void onRemoved() {
        // Cleanup entity
        entityId = 0;
        world = null;
    }

}
