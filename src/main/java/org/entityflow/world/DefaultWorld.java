package org.entityflow.world;


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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages all entities and systems in a game/simulation.
 */
public class DefaultWorld implements World {

    private final List<EntitySystem> entitySystems = new ArrayList<EntitySystem>();
    private final AtomicBoolean      initialized   = new AtomicBoolean(false);
    private final AtomicBoolean      running       = new AtomicBoolean(false);

    // The entities list is not modified while a system is processing entities, so processing can be done with multiple threads.
    private final List<Entity> entities = new ArrayList<Entity>();

    private final ConcurrentMap<Long, Entity> entityLookup = new ConcurrentHashMap<Long, Entity>();
    private final Map<Class, EntitySystem>    systemLookup = new HashMap<Class, EntitySystem>();

    // Added and removed entities are first stored in concurrent collections, and then applied to the world at the start of world processing.
    private final ConcurrentMap<Entity, Boolean> addedAndRemovedEntities = new ConcurrentHashMap<Entity, Boolean>();

    // Keeps track of changed entities, that is, entities whose components changed, and that may need to be added or removed from systems.
    private final ConcurrentMap<Entity, Boolean> changedEntities = new ConcurrentHashMap<Entity, Boolean>();

    private final AtomicLong nextFreeEntityId = new AtomicLong(1);

    private long simulationStepMilliseconds;

    public DefaultWorld() {
        this(1);
    }

    public DefaultWorld(long simulationStepMilliseconds) {
        setSimulationStepMilliseconds(simulationStepMilliseconds);
    }

    public long getSimulationStepMilliseconds() {
        return simulationStepMilliseconds;
    }

    public void setSimulationStepMilliseconds(long simulationStepMilliseconds) {
        Check.positive(simulationStepMilliseconds, "simulationStepMilliseconds");

        this.simulationStepMilliseconds = simulationStepMilliseconds;
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

    @Override
    public final void init() {
        // TODO: Add logging support
        System.out.println("Initializing.");

        if (initialized.get()) throw new IllegalStateException("World was already initialized, can not initialize again");

        registerSystems();

        for (EntitySystem entitySystem : entitySystems) {
            entitySystem.init(this);
        }

        initialized.set(true);

        refreshEntities();

        initWorld();

        refreshEntities();
    }


    @Override
    public final void start(long simulationStepMilliseconds) {
        setSimulationStepMilliseconds(simulationStepMilliseconds);

        start();
    }

    @Override
    public final void start() {
        // Initialize if needed
        if (!initialized.get()) init();

        // Main simulation loop
        Ticker ticker = new Ticker();
        running.set(true);
        while(running.get()) {
            ticker.tick();

            process(ticker);

            try {
                Thread.sleep(simulationStepMilliseconds);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        // Do shutdown
        doShutdown();
    }

    @Override
    public final void shutdown() {
        if (running.get()) {
            // Let game loop call doShutdown after the next loop is ready
            running.set(false);
        }
        else {
            doShutdown();
        }
    }

    @Override
    public void removeEntity(Entity entity) {
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

        // Create entity class
        Entity entity = new ConcurrentEntity(entityId, this, components);

        // Schedule for addition
        addedAndRemovedEntities.put(entity, true);

        return entity;
    }

    /**
     * Can be used to add systems.  Called automatically by init or start before initializing the systems.
     */
    protected void registerSystems() {
    }

    /**
     * Can be used to initialize the world.  Called automatically by the init or start after systems have been initialized.
     */
    protected void initWorld() {
    }

    /**
     * Can be used to do any additional things before shutdown.  Called by shutdown before systems are shut down.
     */
    protected void onShutdown() {
    }


    private void doShutdown() {
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

    private void refreshEntities() {
        // Add and remove entities marked for addition or removal.
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

                if (wasRemoved) {
                    // Notify systems
                    for (EntitySystem entitySystem : entitySystems) {
                        entitySystem.onEntityRemoved(entity);
                    }

                    long entityId = entity.getEntityId();

                    // Cleanup entity
                    entity.onRemoved();

                    entityLookup.remove(entityId);

                    // TODO: Recycle entity
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
