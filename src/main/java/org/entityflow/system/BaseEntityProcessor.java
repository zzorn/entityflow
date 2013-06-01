package org.entityflow.system;

import org.entityflow.component.Component;
import org.entityflow.entity.Entity;
import org.entityflow.util.Ticker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base implementation of an Processor, provides functionality that is common for most Processors that handle entities.
 */
// TODO: Support concurrent processing of entities if a flag to that effect is passed in the constructor.
// Use a thread pool to process a part of the handled entities in each thread (concurrent reading of the handledEntities list is ok, as it is not modified during processing).
// If concurrent processing is on, the processing of one entity by a system should never modify another handled by the same system.
public abstract class BaseEntityProcessor extends BaseProcessor {

    // The types of components an entity should have for this processor to handle it.
    private final Set<Class<? extends Component>> handledComponentTypes = new HashSet<Class<? extends Component>>();

    // Updated by onEntityAdded, onEntityRemoved and onEntityChanged,
    // these are called by World during the common process phase, and do not need to be thread safe.
    private final List<Entity> handledEntities = new ArrayList<Entity>();


    /**
     * Creates a new BaseProcessor.
     */
    protected BaseEntityProcessor() {
        this(null);
    }

    /**
     * Creates a new BaseProcessor, that is interested in entities with the specified types of components.
     * Only entities with all the specified component types are processed by default.
     *
     * @param baseType the base type for this entity system, or the default one if null.
     * @param handledComponentTypes entities with the component types listed here will be handled by this system.
     */
    protected BaseEntityProcessor(Class<? extends Processor> baseType,
                                  Class<? extends Component>... handledComponentTypes) {
        this(baseType, 0, handledComponentTypes);
    }

    /**
     * Creates a new BaseProcessor, that is interested in entities with the specified types of components.
     * Only entities with all the specified component types are processed by default.
     *
     * @param baseType the base type for this entity system, or the default one if null.
     * @param processingIntervalSeconds number of seconds between each process pass of this system, or zero to process as often as process() is called.
     * @param handledComponentTypes entities with the component types listed here will be handled by this system.
     */
    protected BaseEntityProcessor(Class<? extends Processor> baseType,
                                  double processingIntervalSeconds,
                                  Class<? extends Component>... handledComponentTypes) {
        super(baseType, processingIntervalSeconds);
        for (Class<? extends Component> handledComponentType : handledComponentTypes) {
            this.handledComponentTypes.add(handledComponentType);
        }
    }

    @Override
    public final void onEntityAdded(Entity entity) {
        if (!handlesEntity(entity)) {
            addEntityIfWeShould(entity);
        }
    }

    @Override
    public final void onEntityRemoved(Entity entity) {
        if (handlesEntity(entity)) {
            removeEntity(entity);
        }
    }

    @Override
    public final void onEntityComponentsChanged(Entity entity) {
        if (!handlesEntity(entity)) {
            addEntityIfWeShould(entity);
        } else {
            removeEntityIfWeShould(entity);
        }

    }

    protected void doProcess(Ticker systemTicker) {
        preProcess(systemTicker);

        for (Entity handledEntity : handledEntities) {
            processEntity(ticker, handledEntity);
        }

        postProcess(systemTicker);
    }

    /**
     * Called before entity processing begins.
     * @param ticker contains delta time and total simulation time.
     */
    protected void preProcess(Ticker ticker) {}

    /**
     * Called after entity processing ends.
     * @param ticker contains delta time and total simulation time.
     */
    protected void postProcess(Ticker ticker) {}

    /**
     * Called to process a specific entity
     * @param ticker contains delta time and total simulation time.
     * @param entity entity to process.
     */
    protected void processEntity(Ticker ticker, Entity entity) {}

    /**
     * Called after an entity is added to this system.
     */
    protected void handleAddedEntity(Entity entity) {}

    /**
     * Called before an entity is removed from this system.
     */
    protected void handleRemovedEntity(Entity entity) {}


    /**
     * @return true if this system should keep track of the specified entity and process it on each process call.
     */
    protected boolean shouldHandle(Entity entity) {
        return entity.containsAllComponents(handledComponentTypes);
    }

    private void addEntityIfWeShould(Entity entity) {
        if (shouldHandle(entity)) {
            handledEntities.add(entity);
            handleAddedEntity(entity);
        }
    }

    private void removeEntityIfWeShould(Entity entity) {
        if (!shouldHandle(entity)) {
            removeEntity(entity);
        }
    }

    private void removeEntity(Entity entity) {
        handleRemovedEntity(entity);
        handledEntities.remove(entity);
    }

    private boolean handlesEntity(Entity entity) {
        for (Entity handledEntity : handledEntities) {
            if (handledEntity == entity) return true;
        }

        return false;
    }

}
