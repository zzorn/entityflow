package org.entityflow.entity;

import org.entityflow.component.Component;
import org.entityflow.world.World;
import org.flowutils.Check;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private final Object componentChangeLock = new Object();

    /**
     * Lock used to synchronize message queuing with.
     */
    private final Object messageLock = new Object();

    private final ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();

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

        synchronized (componentChangeLock) {
            rawAddComponent(component);
        }
    }

    @Override public void addComponents(Component... components) {
        synchronized (componentChangeLock) {
            for (Component component : components) {
                rawAddComponent(component);
            }
        }
    }

    @Override public <T extends Component> void removeComponent(final Class<T> type) {
        synchronized (componentChangeLock) {
            rawRemoveComponent(type);
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
            handleComponentRemoved(component);
        }

        // Cleanup entity
        components.clear();

        super.onDeleted();
    }

    @Override public void sendMessage(Message message, boolean externalSource) {
        Check.notNull(message, "message");

        // For external message sources (e.g. player clients), we also persistently store the message,
        // to allow unrolling in the event of a crash.
        if (externalSource) {
            // Make sure multiple calls will have the messages added in the same order to the messageQueue and the persistence service.
            synchronized (messageLock) {
                // Queue the message for handling by processors during simulation update.
                messageQueue.add(message);

                // Store message persistently if it is from outside the simulation, to allow rollback recovery
                final World world = getWorld();
                world.getPersistenceService().storeExternalMessage(world.getSimulationTick(), getEntityId(), message);
            }
        }
        else {
            //Just queue the message for handling by processors during simulation update.
            messageQueue.add(message);
        }
    }

    @Override public Message popNextMessage() {
        return messageQueue.remove();
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
