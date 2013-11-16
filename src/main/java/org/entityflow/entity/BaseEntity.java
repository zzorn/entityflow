package org.entityflow.entity;

import org.entityflow.component.Component;
import org.entityflow.world.World;

import java.util.Set;

/**
 * Common functionality for entities.
 */
public abstract class BaseEntity implements Entity {

    /**
     * Unique id for this entity within the World.
     */
    private long entityId = 0;

    /**
     * Host world that the entity belongs to.
     */
    private World world = null;

    @Override public final void init(long entityId, World world) {
        if (this.entityId != 0) throw new IllegalStateException("Can not initialize an entity that was already initialized.  Attempted to initialize to entity id "+entityId+", but already had id "+ this.entityId);

        this.entityId = entityId;
        this.world = world;

        onInit();
    }

    @Override public void addComponents(Component... components) {
        for (Component component : components) {
            addComponent(component);
        }
    }

    @Override
    public final World getWorld() {
        return world;
    }

    @Override
    public final long getEntityId() {
        return entityId;
    }

    @Override
    public void delete() {
        world.deleteEntity(this);
    }

    @Override public void onDeleted() {
        // Cleanup entity
        entityId = 0;
        world = null;
    }

    @Override public boolean containsAllComponents(Set<Class<? extends Component>> componentTypes) {
        for (Class<? extends Component> componentType : componentTypes) {
            if (!containsComponent(componentType)) return false;
        }
        return true;
    }

    /**
     * Called when an entity was initialized.
     * May be overridden in subclasses to do any needed initialization.
     */
    protected void onInit() {}
}
