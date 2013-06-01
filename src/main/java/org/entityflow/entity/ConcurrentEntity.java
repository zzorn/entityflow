package org.entityflow.entity;

import org.entityflow.component.Component;
import org.entityflow.world.World;
import org.flowutils.Check;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An entity that supports concurrent accessing of its components and adding / removing components from different threads.
 * Note that modifications to a specific component should only happen from one thread at a time unless the component
 * itself supports concurrency.
 */
public final class ConcurrentEntity extends BaseEntity {

    /**
     * Components that the entity contains.
     */
    private final ConcurrentMap<Class<? extends Component>, Component> components = new ConcurrentHashMap<Class<? extends Component>, Component>();

    /**
     * Lock used to synchronize component changes with.
     */
    private final Object changeLock = new Object();

    @Override public void getComponents(Collection<Component> componentsOut) {
        componentsOut.addAll(components.values());
    }

    public Map<Class<? extends Component>, Component> getComponents() {
        return components;
    }

    @Override public <T extends Component> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }

    @Override public void addComponent(Component component) {
        Check.notNull(component, "component");

        synchronized (changeLock) {
            rawAddComponent(component);
        }
    }

    @Override public void addComponents(Component... components) {
        synchronized (changeLock) {
            for (Component component : components) {
                rawAddComponent(component);
            }
        }
    }

    @Override public <T extends Component> void removeComponent(final Class<T> type) {
        synchronized (changeLock) {
            // Remove component
            final Component oldComponent = components.remove(type);

            // Check if some component was removed
            if (oldComponent != null) {
                // Notify removed component
                oldComponent.onRemoved();

                // Notify world
                getWorld().onEntityComponentsChanged(this);

                // TODO: Recycle component?
            }
        }
    }

    @Override public <T extends Component> boolean containsComponent(Class<T> type) {
        return components.containsKey(type);
    }

    @Override public boolean containsAllComponents(Set<Class<? extends Component>> componentTypes) {
        for (Class<? extends Component> componentType : componentTypes) {
            if (!components.containsKey(componentType)) return false;
        }
        return true;
    }

    @Override public void onDeleted() {
        // Notify components
        for (Component component : components.values()) {
            component.onRemoved();

            // TODO: Recycle component?
        }

        // Cleanup entity
        components.clear();

        super.onDeleted();
    }

    private void rawAddComponent(Component component) {// Add component
        final Component oldValue = components.put(component.getBaseType(), component);

        // Ignore cases where we replace a component with itself
        if (oldValue != component) {
            // Notify previous component, if any
            if (oldValue != null) oldValue.onRemoved();

            // Notify new component
            component.setEntity(this);

            // Notify world
            getWorld().onEntityComponentsChanged(this);
        }
    }
}
