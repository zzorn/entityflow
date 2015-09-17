package org.entityflow2.processor;

import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import org.entityflow2.EntityManager;
import org.entityflow2.component.ComponentType;
import org.entityflow2.group.EntityGroup;
import org.flowutils.time.Time;
import org.flowutils.updating.strategies.UpdateStrategy;

import static org.flowutils.Check.notNull;

/**
 * Some common functionality for Processors that process entities with certain components.
 */
public abstract class EntityProcessorBase extends ProcessorBase {

    private ComponentType[] componentTypes;
    private EntityGroup processedEntities;

    public EntityProcessorBase(UpdateStrategy updateStrategy, ComponentType ... componentTypes) {
        super(updateStrategy);

        // Ensure component types are not null
        for (int i = 0; i < componentTypes.length; i++) {
            notNull(componentTypes[i], "componentType " + i);
        }

        this.componentTypes = componentTypes;
    }

    /**
     * @return component types required of entities that this processor updates.
     *         Do not modify the returned array.
     */
    protected final ComponentType[] getComponentTypes() {
        return componentTypes;
    }

    /**
     * @return the group of entities that this processor updates.
     *         Only available after init has been called.
     */
    public final EntityGroup getProcessedEntities() {
        return processedEntities;
    }

    @Override public final void init(EntityManager entityManager) {
        processedEntities = entityManager.getEntityGroup(getComponentTypes());
    }

    @Override protected final void doUpdate(final Time time) {
        // Do any pre-loop work
        beforeEntityUpdate(time);

        // Loop the entities that have the component
        loopEntities(time);

        // Do any post-loop work
        afterEntityUpdate(time);
    }

    protected void loopEntities(final Time time) {
        for (IntBidirectionalIterator iterator = getProcessedEntities().getEntities().iterator(); iterator.hasNext(); ) {
            updateEntity(time, iterator.nextInt());
        }
    }

    protected abstract void updateEntity(Time time, int entityId);

    protected void beforeEntityUpdate(Time time) {
    }

    protected void afterEntityUpdate(Time time) {
    }

    // Override if needed
    @Override public void shutdown(EntityManager entityManager) {
    }
}
