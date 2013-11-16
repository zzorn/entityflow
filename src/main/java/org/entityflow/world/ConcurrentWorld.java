package org.entityflow.world;


import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;
import org.entityflow.system.Processor;
import org.entityflow.util.Ticker;
import org.entityflow.component.Component;
import org.entityflow.entity.ConcurrentEntity;
import org.entityflow.entity.Entity;
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
    private final List<Processor> processors = new ArrayList<Processor>();

    // The entities list is not modified while a system is processing entities, so processing can be done with multiple threads.
    private final List<Entity> entities = new ArrayList<Entity>();

    // Lookup map for entities based on entity id
    private final ConcurrentMap<Long, Entity> entityLookup = new ConcurrentHashMap<Long, Entity>();

    // Lookup map for systems based on class
    private final Map<Class, Processor> processorLookup = new HashMap<Class, Processor>();

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
    public final <T extends Processor> T addProcessor(T processor) {
        Check.notContained(processor, processors, "processors");
        if (initialized.get()) throw new IllegalStateException("addProcessor must be called before init is called.");

        Class<? extends Processor> baseType = processor.getBaseType();
        if (processorLookup.containsKey(baseType)) throw new IllegalStateException("A system using the base type '"+baseType+"' is already added!");

        processorLookup.put(baseType, processor);

        processors.add(processor);

        return processor;
    }

    @Override
    public final <T extends Processor> T getProcessor(Class<T> processorType) {
        Processor processor = processorLookup.get(processorType);
        if (processor == null) throw new IllegalArgumentException("No entity system with the base type " +
                                                                  processorType + " found!");
        return (T) processor;
    }

    @Override protected void initProcessors() {
        for (Processor processor : processors) {
            processor.init(this);
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
        if (!initialized.get()) throw new IllegalStateException("World was not yet initialized, can not process world before init is called.");

        refreshEntities();

        // Process entities with systems
        for (Processor processor : processors) {
            processor.process();
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
            for (int i = processors.size() - 1; i >= 0; i--) {
                processors.get(i).shutdown();
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
                for (Processor processor : processors) {
                    processor.onEntityAdded(entity);
                }
            }
            else {
                // Remove entity (if contained)
                final boolean wasRemoved = entities.remove(entity);

                // Do not process any outstanding changes to the entity
                changedEntities.remove(entity);

                if (wasRemoved) {
                    // Notify systems
                    for (Processor processor : processors) {
                        processor.onEntityRemoved(entity);
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
            for (Processor processor : processors) {
                processor.onEntityComponentsChanged(entity);
            }
        }
    }


}
