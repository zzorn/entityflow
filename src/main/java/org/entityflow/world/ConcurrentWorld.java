package org.entityflow.world;


import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;
import org.entityflow.util.Ticker;
import org.entityflow.component.Component;
import org.entityflow.entity.ConcurrentEntity;
import org.entityflow.entity.Entity;
import org.entityflow.system.EntitySystem;
import org.flowutils.Check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages all entities and systems in a game/simulation.
 */
public class ConcurrentWorld extends BaseWorld {

    // All systems registered with the world
    private final List<EntitySystem> entitySystems = new ArrayList<EntitySystem>();

    // The entities list is not modified while a system is processing entities, so processing can be done with multiple threads.
    private final List<Entity> entities = new ArrayList<Entity>();

    // Lookup map for entities based on entity id
    private final ConcurrentMap<Long, Entity> entityLookup = new ConcurrentHashMap<Long, Entity>();

    // Lookup map for systems based on class
    private final Map<Class, EntitySystem> systemLookup = new HashMap<Class, EntitySystem>();

    // Added and removed entities are first stored in concurrent collections, and then applied to the world at the start of world processing.
    private final ConcurrentMap<Entity, Boolean> addedAndRemovedEntities = new ConcurrentHashMap<Entity, Boolean>();

    // Keeps track of changed entities, that is, entities whose components changed, and that may need to be added or removed from systems.
    private final ConcurrentMap<Entity, Boolean> changedEntities = new ConcurrentHashMap<Entity, Boolean>();

    // Next free id for a new entity
    private final AtomicLong nextFreeEntityId = new AtomicLong(1);

    // Object pool for recycling entity references
    private final ObjectPool<Entity> entityPool = new SoftReferenceObjectPool<Entity>(new BasePoolableObjectFactory<Entity>() {
        @Override public Entity makeObject() throws Exception {
            return new ConcurrentEntity();
        }
    });


    public ConcurrentWorld() {
        this(1);
    }

    public ConcurrentWorld(long simulationStepMilliseconds) {
        setSimulationStepMilliseconds(simulationStepMilliseconds);
    }

    public long getSimulationStepMilliseconds() {
        return simulationStepMilliseconds;
    }

    @Override
    public final <T extends EntitySystem> T addSystem(T entitySystem) {
        Check.notContained(entitySystem, entitySystems, "entitySystems");
        if (initialized.get()) throw new IllegalStateException("addSystem must be called before init is called.");

        Class<? extends EntitySystem> baseType = entitySystem.getBaseType();
        if (systemLookup.containsKey(baseType)) throw new IllegalStateException("A system using the base type '"+baseType+"' is already added!");

        systemLookup.put(baseType, entitySystem);

        entitySystems.add(entitySystem);

        return entitySystem;
    }

    @Override
    public final <T extends EntitySystem> T getSystem(Class<T> systemType) {
        EntitySystem entitySystem = systemLookup.get(systemType);
        if (entitySystem == null) throw new IllegalArgumentException("No entity system with the base type " + systemType + " found!");
        return (T) entitySystem;
    }

    @Override protected void initSystems() {
        for (EntitySystem entitySystem : entitySystems) {
            entitySystem.init(this);
        }
    }

    @Override
    public void deleteEntity(Entity entity) {
        Check.notNull(entity, "entity");
        Check.equalRef(entity.getWorld(), "world of the removed entity", this, "the world it is removed from.");

        addedAndRemovedEntities.put(entity, false);
    }

    @Override
    public Entity getEntity(long entityId) {
        return entityLookup.get(entityId);
    }

    @Override
    public void onEntityComponentsChanged(Entity entity) {
        changedEntities.put(entity, true);
    }


    @Override
    public void process(Ticker ticker) {
        refreshEntities();

        // Process entities with systems
        for (EntitySystem entitySystem : entitySystems) {
            entitySystem.process();
        }

    }

    @Override
    public Entity createEntity(Component... components) {
        // Get id
        final long entityId = nextFreeEntityId.getAndIncrement();

        // Create entity class, or reuse a previous one
        final Entity entity;
        try {
            entity = entityPool.borrowObject();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a new entity, problem creating entity object with pool: " + e.getMessage(), e);
        }
        entity.init(entityId, this);
        entity.addComponents(components);

        // Schedule for addition
        addedAndRemovedEntities.put(entity, true);

        return entity;
    }


    @Override protected void doShutdown() {
        if (initialized.get()) {
            onShutdown();

            // Shutdown in reverse order of initialization
            for (int i = entitySystems.size() - 1; i >= 0; i--) {
                entitySystems.get(i).shutdown();
            }

            initialized.set(false);
        }
        else {
            throw new IllegalStateException("World was not initialized, can not shut down");
        }
    }

    @Override protected void refreshEntities() {
        // Add and delete entities marked for addition or removal.
        for (Map.Entry<Entity, Boolean> entry : addedAndRemovedEntities.entrySet()) {
            boolean add = entry.getValue();
            Entity entity = entry.getKey();

            if (add) {
                // Add entity
                entities.add(entity);
                entityLookup.put(entity.getEntityId(), entity);

                // Notify systems
                for (EntitySystem entitySystem : entitySystems) {
                    entitySystem.onEntityAdded(entity);
                }
            }
            else {
                // Remove entity (if contained)
                final boolean wasRemoved = entities.remove(entity);

                // Do not process any outstanding changes to the entity
                changedEntities.remove(entity);

                if (wasRemoved) {
                    // Notify systems
                    for (EntitySystem entitySystem : entitySystems) {
                        entitySystem.onEntityRemoved(entity);
                    }

                    // Cleanup entity
                    long entityId = entity.getEntityId();
                    entity.onDeleted();
                    entityLookup.remove(entityId);

                    // Recycle entity
                    try {
                        entityPool.returnObject(entity);
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not recycle entity " + entity + ": " + e.getMessage(), e);
                    }
                }
            }
        }
        addedAndRemovedEntities.clear();

        // Notify about changed entities
        for (Map.Entry<Entity, Boolean> entry : changedEntities.entrySet()) {
            final Entity entity = entry.getKey();

            // Notify systems
            for (EntitySystem entitySystem : entitySystems) {
                entitySystem.onEntityComponentsChanged(entity);
            }
        }
    }


}
