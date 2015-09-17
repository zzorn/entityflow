package org.entityflow2.processor;

import org.entityflow2.component.ComponentType;
import org.flowutils.time.Time;
import org.flowutils.updating.strategies.UpdateStrategy;

/**
 * Common functionality for Processors that process entities with one specific component.
 */
public abstract class EntityProcessor1Base<C extends ComponentType> extends EntityProcessorBase {

    private final C componentType;

    /**
     * @param componentType component type required of entities that this processor updates.
     */
    protected EntityProcessor1Base(C componentType) {
        this(componentType, null);
    }

    /**
     * @param componentType component type required of entities that this processor updates.
     * @param updateStrategy strategy used for updates.  Null to use normal variable length timesteps with the default time,
     *                       or any UpdateStrategy to use that to determine when to update and with what time.
     *                       Use e.g. to do fixed timestep updates of the processor.
     */
    protected EntityProcessor1Base(C componentType, UpdateStrategy updateStrategy) {
        super(updateStrategy, componentType);

        this.componentType = componentType;
    }

    /**
     * @return component type required of entities that this processor updates.
     */
    public final C getComponentType() {
        return componentType;
    }

    @Override protected final void loopEntities(final Time time) {
        // This may be a bit faster than iterating the entities using the entityGroup, and produces a bit less garbage per frame
        int currentEntityId = 0;
        int componentIndex = 0;
        while (currentEntityId >= 0) {
            currentEntityId = componentType.getEntityAtComponentIndex(componentIndex++);

            if (currentEntityId > 0) {
                updateEntity(time, currentEntityId, componentType);
            }
        }
    }

    @Override protected final void updateEntity(Time time, int entityId) {
        // Not used
    }

    protected abstract void updateEntity(Time time, int entityId, C componentType);

}
