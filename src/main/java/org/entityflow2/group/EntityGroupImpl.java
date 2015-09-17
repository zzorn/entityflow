package org.entityflow2.group;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import net.openhft.koloboke.collect.set.IntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;
import net.openhft.koloboke.function.IntConsumer;
import org.entityflow2.component.ComponentType;
import org.flowutils.Check;

import java.util.*;

import static org.flowutils.Check.notNull;

/**
 * All entities that have the specified component types.
 */
public final class EntityGroupImpl implements EntityGroup {

    private static final int EXPECTED_GROUP_SIZE = 1000;

    private final IntSortedSet entities = new IntAVLTreeSet();
    private final IntSortedSet readOnlyEntities = IntSortedSets.unmodifiable(entities);
    private final ComponentType[] requiredComponentTypes;
    private final ComponentType[] forbiddenComponentTypes;

    private final IntSet addedEntities = HashIntSets.newMutableSet();
    private final IntSet removedEntities = HashIntSets.newMutableSet();


    private final List<EntityGroupListener> listeners = new ArrayList<EntityGroupListener>(4);

    private final IntConsumer additionNotifier = new IntConsumer() {
        @Override public void accept(int value) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onEntityAdded(EntityGroupImpl.this, value);
            }
        }
    };

    private final IntConsumer removalNotifier = new IntConsumer() {
        @Override public void accept(int value) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onEntityRemoved(EntityGroupImpl.this, value);
            }
        }
    };

    /**
     * @param requiredComponentTypes the components that an entity must have to be part of this group.
     */
    public EntityGroupImpl(ComponentType... requiredComponentTypes) {
        this(Arrays.asList(requiredComponentTypes), Collections.<ComponentType>emptyList());
    }

    /**
     * @param requiredComponentTypes the components that an entity must have to be part of this group.
     */
    public EntityGroupImpl(Collection<ComponentType> requiredComponentTypes) {
        this(requiredComponentTypes, Collections.<ComponentType>emptyList());
    }

    /**
     * @param requiredComponentTypes the components that an entity must have to be part of this group.
     * @param forbiddenComponentTypes the components that an entity can not have if it is part of this group.
     */
    public EntityGroupImpl(Collection<ComponentType> requiredComponentTypes,
                           Collection<ComponentType> forbiddenComponentTypes) {
        notNull(requiredComponentTypes, "requiredComponentTypes");
        notNull(forbiddenComponentTypes, "forbiddenComponentTypes");

        this.requiredComponentTypes = requiredComponentTypes.toArray(new ComponentType[requiredComponentTypes.size()]);
        this.forbiddenComponentTypes = forbiddenComponentTypes.toArray(new ComponentType[forbiddenComponentTypes.size()]);
    }

    @Override public IntSortedSet getEntities() {
        return readOnlyEntities;
    }

    @Override public void handleExistingEntity(int entityId) {
        if (matches(entityId)) entities.add(entityId);
    }

    @Override public void handleEntityCreated(int entityId, ComponentType[] initialComponentTypes) {
        // Skip entity if it does not contain all of the required components for this group
        for (int i = 0, len = requiredComponentTypes.length; i < len; i++) {
            if (!contains(initialComponentTypes, requiredComponentTypes[i])) return;
        }

        // Skip entity if it contains any of the forbidden components for this group
        for (int i = 0, len = forbiddenComponentTypes.length; i < len; i++) {
            if (contains(initialComponentTypes, forbiddenComponentTypes[i])) return;
        }

        // Entity was acceptable to this group, add it
        entities.add(entityId);
        addedEntities.add(entityId);
    }

    @Override public void handleEntityRemoved(int entityId) {
        if (entities.contains(entityId)) {
            removeEntity(entityId);
        }
    }

    @Override public void handleComponentAdded(int entityId, ComponentType addedComponentType) {
        if (!entities.contains(entityId)) {
            if (contains(requiredComponentTypes, addedComponentType) &&
                !contains(forbiddenComponentTypes, addedComponentType) &&
                matches(entityId)) {
                addEntity(entityId);
            }
        } else {
            if (contains(forbiddenComponentTypes, addedComponentType)) {
                removeEntity(entityId);
            }
        }
    }

    @Override public void handleComponentRemoved(int entityId, ComponentType removedComponentType) {
        if (!entities.contains(entityId)) {
            if (!contains(requiredComponentTypes, removedComponentType) &&
                contains(forbiddenComponentTypes, removedComponentType) &&
                matches(entityId)) {
                addEntity(entityId);
            }
        } else {
            if (contains(requiredComponentTypes, removedComponentType)) {
                removeEntity(entityId);
            }
        }
    }

    private boolean matches(int entityId) {
        for (int i = 0, len = requiredComponentTypes.length; i < len; i++) {
            if (!requiredComponentTypes[i].containedInEntity(entityId)) return false;
        }

        for (int i = 0, len = forbiddenComponentTypes.length; i < len; i++) {
            if (forbiddenComponentTypes[i].containedInEntity(entityId)) return false;
        }

        return true;
    }

    private void addEntity(int entityId) {
        entities.add(entityId);
        addedEntities.add(entityId);
        removedEntities.remove(entityId);
    }

    private void removeEntity(int entityId) {
        entities.remove(entityId);
        removedEntities.add(entityId);
        addedEntities.remove(entityId);
    }

    @Override public void update() {
        // Notify listeners
        removedEntities.forEach(removalNotifier);
        addedEntities.forEach(additionNotifier);

        // Clear recently added and removed sets
        removedEntities.clear();
        addedEntities.clear();
    }

    @Override public boolean matches(ComponentType[] requiredComponents) {
        return containSameComponents(requiredComponents, requiredComponentTypes) &&
               this.forbiddenComponentTypes.length == 0;
    }

    @Override public boolean matches(ComponentType[] requiredComponents, ComponentType[] forbiddenComponents) {
        return containSameComponents(requiredComponents, requiredComponentTypes) &&
               containSameComponents(forbiddenComponents, forbiddenComponentTypes);
    }

    @Override public final void addListener(EntityGroupListener listener) {
        Check.notNull(listener, "listener");
        if (listeners.contains(listener)) throw new IllegalArgumentException("The EntityGroupListener has already been added as a listener, can't add it twice");

        listeners.add(listener);
    }



    @Override public final void removeListener(EntityGroupListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies listeners about a removed entity
     */
    private final void notifyEntityRemoved(int entityId) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onEntityRemoved(this, entityId);
        }
    }


    private boolean contains(ComponentType[] array, ComponentType value) {
        for (int i = 0, len = array.length; i < len; i++) {
            if (array[i] == value) return true;
        }
        return false;
    }

    private boolean containSameComponents(final ComponentType[] as,
                                          final ComponentType[] bs) {
        if (as.length == bs.length) {
            for (ComponentType a : as) {
                if (!contains(bs, a)) return false;
            }

            for (ComponentType b : bs) {
                if (!contains(as, b)) return false;
            }
        }

        return true;
    }


}
