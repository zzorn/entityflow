package org.entityflow2.processor;

import org.entityflow2.component.ComponentType;
import org.flowutils.time.Time;
import org.flowutils.updating.strategies.UpdateStrategy;

/**
 * Common functionality for Processors that process entities with three specific components.
 */
public abstract class EntityProcessor3Base<C1 extends ComponentType, C2 extends ComponentType, C3 extends ComponentType> extends EntityProcessorBase {

    private final C1 componentType1;
    private final C2 componentType2;
    private final C3 componentType3;

    /**
     * @param componentType1 component type required of entities that this processor updates.
     * @param componentType2 component type required of entities that this processor updates.
     * @param componentType3 component type required of entities that this processor updates.
     */
    protected EntityProcessor3Base(C1 componentType1, C2 componentType2, C3 componentType3) {
        this(componentType1, componentType2, componentType3, null);
    }

    /**
     * @param componentType1 component type required of entities that this processor updates.
     * @param componentType2 component type required of entities that this processor updates.
     * @param componentType3 component type required of entities that this processor updates.
     * @param updateStrategy strategy used for updates.  Null to use normal variable length timesteps with the default time,
     *                       or any UpdateStrategy to use that to determine when to update and with what time.
     *                       Use e.g. to do fixed timestep updates of the processor.
     */
    protected EntityProcessor3Base(C1 componentType1, C2 componentType2, C3 componentType3, UpdateStrategy updateStrategy) {
        super(updateStrategy, componentType1, componentType2, componentType3);

        this.componentType1 = componentType1;
        this.componentType2 = componentType2;
        this.componentType3 = componentType3;
    }

    /**
     * @return first component type required of entities that this processor updates.
     */
    public final C1 getComponentType1() {
        return componentType1;
    }

    /**
     * @return second component type required of entities that this processor updates.
     */
    public final C2 getComponentType2() {
        return componentType2;
    }

    /**
     * @return third component type required of entities that this processor updates.
     */
    public final C3 getComponentType3() {
        return componentType3;
    }

    @Override protected final void updateEntity(Time time, int entityId) {
        updateEntity(time, entityId, componentType1, componentType2, componentType3);
    }

    protected abstract void updateEntity(Time time, int entityId, C1 componentType1, C2 componentType2, C3 componentType3);
}
