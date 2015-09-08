package org.entityflow2;

import net.openhft.koloboke.collect.Equivalence;
import net.openhft.koloboke.collect.map.ObjObjMap;
import net.openhft.koloboke.collect.map.hash.HashObjObjMaps;
import net.openhft.koloboke.collect.set.IntSet;
import net.openhft.koloboke.collect.set.hash.HashIntSets;
import net.openhft.koloboke.function.IntConsumer;
import org.entityflow2.component.ComponentType;
import org.entityflow2.group.EntityGroup;
import org.entityflow2.group.EntityGroupImpl;
import org.entityflow2.processor.Processor;
import org.flowutils.Symbol;
import org.flowutils.service.ServiceBase;
import org.flowutils.service.ServiceProvider;
import org.flowutils.time.Time;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.flowutils.Check.notContained;
import static org.flowutils.Check.notNull;

/**
 *
 */
public final class ConcurrentEntityManager extends ServiceBase implements EntityManager {

    private static final int EXPECTED_ENTITY_COUNT = 100000;
    private static final int EXPECTED_ADD_DELETE_PER_UPDATE = 1000;

    private static final int MIN_ENTITY_ID = 1;
    private static final int MAX_ENTITY_ID = Integer.MAX_VALUE - 3;

    private final List<ComponentType> componentTypes = new ArrayList<ComponentType>();
    private final ObjObjMap<Symbol, ComponentType> componentTypeLookup = HashObjObjMaps.getDefaultFactory().withKeyEquivalence(Equivalence.identity()).newMutableMap();

    private final List<Processor> processors = new ArrayList<Processor>();

    private final IntSet existingEntities = HashIntSets.newMutableSet(EXPECTED_ENTITY_COUNT);
    private int lastCreatedEntityId = 0;
    private final Object createDeleteEntityLock = new Object();

    private final Set<Integer> entitiesToRemove = new ConcurrentSkipListSet<Integer>();

    private final List<EntityGroup> groups = new ArrayList<EntityGroup>();


    @Override public <T extends ComponentType> T registerComponentType(T componentType) {
        notNull(componentType, "componentType");
        notContained(componentType, componentTypes, "componentTypes");
        notContained(componentType.getId(), componentTypeLookup, "componentTypeLookup");

        componentTypes.add(componentType);
        componentTypeLookup.put(componentType.getId(), componentType);
        componentType.setEntityManager(this);

        return componentType;
    }

    @Override public <T extends Processor> T registerProcessor(T processor) {
        notNull(processor, "processor");
        notContained(processor, processors, "processors");

        processors.add(processor);
        processor.setEntityManager(this);

        return processor;
    }

    @Override public ComponentType getComponentType(Symbol componentTypeId) {
        final ComponentType componentType = componentTypeLookup.get(componentTypeId);
        if (componentType == null) throw new IllegalArgumentException("No component type with id '"+componentTypeId+"' found");
        return componentType;
    }

    @Override public final int createEntity(List<ComponentType> initialComponentTypes) {
        notNull(initialComponentTypes, "initialComponentTypes");
        return createEntity(initialComponentTypes.toArray(new ComponentType[initialComponentTypes.size()]));
    }

    @Override public int createEntity(ComponentType ... initialComponentTypes) {
        // Get id for the new entity
        final int entityId = getNextFreeEntityId();

        // Add the components to the entity
        for (int i = 0; i < initialComponentTypes.length; i++) {
            initialComponentTypes[i].addToEntity(entityId);
        }

        // Update groups
        for (EntityGroup group : groups) {
            group.handleEntityCreated(entityId, initialComponentTypes);
        }

        // Return id of created entity
        return entityId;
    }

    @Override public void removeEntity(int entityId) {
        entitiesToRemove.add(entityId);
    }

    @Override public EntityGroup getEntityGroup(ComponentType... componentTypes) {
        // Find group that matches the criteria
        for (EntityGroup group : groups) {
            if (group.matches(componentTypes)) {
                return group;
            }
        }

        // No existing matching group found, create new
        final EntityGroup group = new EntityGroupImpl(componentTypes);
        existingEntities.forEach(new IntConsumer() {
            @Override public void accept(int value) {
                group.handleExistingEntity(value);
            }
        });
        groups.add(group);

        return group;
    }

    @Override public void update(Time time) {
        // Update processors
        for (int i = 0; i < processors.size(); i++) {
            processors.get(i).update(time);
        }

        removeEntitiesScheduledForRemoval();

        // Notify group listeners (mainly processors) of the removed and added entities in each group
        for (EntityGroup group : groups) {
            group.update();
        }
    }

    @Override protected void doInit(ServiceProvider serviceProvider) {
        // Initialize processors
        for (Processor processor : processors) {
            processor.init(this);
        }
    }

    @Override protected void doShutdown() {
        // Shutdown processors
        for (Processor processor : processors) {
            processor.shutdown(this);
        }
    }

    @Override public void onComponentAdded(int entityId, ComponentType componentType) {
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).handleComponentAdded(entityId, componentType);
        }
    }

    @Override public void onComponentRemoved(int entityId, ComponentType componentType) {
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).handleComponentRemoved(entityId, componentType);
        }
    }

    private void removeEntitiesScheduledForRemoval() {
        for (int entityId : entitiesToRemove) {
            if (existingEntities.contains(entityId)) {
                // Remove components
                for (int i = 0; i < componentTypes.size(); i++) {
                    ComponentType componentType = componentTypes.get(i);
                    componentType.removeFromEntity(entityId);
                }

                // Remove from set of entities
                existingEntities.remove(entityId);

                // Remove entity from groups
                for (int i = 0; i < groups.size(); i++) {
                    groups.get(i).handleEntityRemoved(entityId);
                }
            }
        }

        entitiesToRemove.clear();
    }

    /**
     * @return next unused entity id.  Wraps around before Integer.MAX_VALUE, and reuses ids of old removed objects.
     *         0 or negative values are never returned.
     *         Thread safe (uses a lock).
     * @throws IllegalStateException if all entity ids are in use.
     */
    private int getNextFreeEntityId() {
        int entityId;
        synchronized (createDeleteEntityLock) {
            // By default use the next id from the previous one
            entityId = lastCreatedEntityId + 1;

            // Loop around at end of entity range
            if (entityId > MAX_ENTITY_ID) entityId = MIN_ENTITY_ID;

            // Use the next entity id if this one is already in use
            while (existingEntities.contains(entityId)) {
                // Move to the next
                entityId++;

                // Loop around at end of entity range
                if (entityId > MAX_ENTITY_ID) entityId = MIN_ENTITY_ID;

                // Check if we looped through all ids
                if (entityId == lastCreatedEntityId) throw new IllegalStateException("Out of entity ids.  Number of current entities: " + existingEntities.size());
            }

            // We found a free entity id
            lastCreatedEntityId = entityId;
            existingEntities.add(entityId);
        }
        return entityId;
    }
}
