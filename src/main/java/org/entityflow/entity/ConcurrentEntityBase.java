package org.entityflow.entity;

import org.entityflow.component.Component;
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
public final class ConcurrentEntityBase extends EntityBase {

    /**
     * Components that the entity contains.
     */
    private final ConcurrentMap<Class<? extends Component>, Component> components = new ConcurrentHashMap<Class<? extends Component>, Component>();

    /**
     * Lock used to synchronize component changes with.
     */
    private final Object componentChangeLock = new Object();



    @Override public void getComponents(Collection<Component> componentsOut) {
        componentsOut.addAll(components.values());
    }

    public Map<Class<? extends Component>, Component> getComponents() {
        return components;
    }

    @Override public <T extends Component> T get(Class<T> type) {
        return (T) components.get(type);
    }


    @Override public void add(Component... components) {
        synchronized (componentChangeLock) {
            for (Component component : components) {
                rawAddComponent(component);
            }
        }
    }

    @Override public <T extends Component> void remove(final Class<T> type) {
        synchronized (componentChangeLock) {
            rawRemoveComponent(type);
        }
    }

    @Override public <T extends Component> boolean has(Class<T> type) {
        return components.containsKey(type);
    }

    @Override public boolean hasAll(Set<Class<? extends Component>> componentTypes) {
        for (Class<? extends Component> componentType : componentTypes) {
            if (!components.containsKey(componentType)) return false;
        }
        return true;
    }

    @Override public void onDeleted() {
        // Notify components
        for (Component component : components.values()) {
            handleComponentRemoved(component);
        }

        // Cleanup entity
        components.clear();

        super.onDeleted();
    }

    @Override public void sendMessage(Message message, boolean externalSource) {
        Check.notNull(message, "message");

        // Delegate to world
        getWorld().sendMessage(this, message, externalSource);
    }

    /**
     * Only call this from a synchronized context.
     */
    private void rawAddComponent(Component component) {
        // Add component
        final Component oldValue = components.put(component.getBaseType(), component);

        // Ignore cases where we replace a component with itself
        if (oldValue != component) {
            // Notify previous component, if any
            if (oldValue != null) {
                handleComponentRemoved(oldValue);
            }

            // Notify new component
            component.setEntity(this);

            // Notify world
            getWorld().onEntityComponentsChanged(this);
        }
    }

    /**
     * Only call this from a synchronized context.
     */
    private <T extends Component> void rawRemoveComponent(Class<T> type) {
        // Remove component
        final Component oldComponent = components.remove(type);

        // Check if some component was removed
        if (oldComponent != null) {
            // Notify removed component
            handleComponentRemoved(oldComponent);

            // Notify world
            getWorld().onEntityComponentsChanged(this);
        }
    }

    private void handleComponentRemoved(Component removedComponent) {
        // Notify removed component
        removedComponent.onRemoved();

        // TODO: Recycle component?
    }
}
