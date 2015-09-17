package org.entityflow2.processor;

import org.entityflow2.component.ComponentType;
import org.flowutils.time.Time;
import org.flowutils.updating.strategies.UpdateStrategy;

/**
 * Common functionality for Processors that process entities with five specific components.
 */
public abstract class EntityProcessor6Base<
        C1 extends ComponentType,
        C2 extends ComponentType,
        C3 extends ComponentType,
        C4 extends ComponentType,
        C5 extends ComponentType,
        C6 extends ComponentType
        > extends EntityProcessorBase {

    private final C1 componentType1;
    private final C2 componentType2;
    private final C3 componentType3;
    private final C4 componentType4;
    private final C5 componentType5;
    private final C6 componentType6;

    /**
     * @param componentType1 component type required of entities that this processor updates.
     * @param componentType2 component type required of entities that this processor updates.
     * @param componentType3 component type required of entities that this processor updates.
     * @param componentType4 component type required of entities that this processor updates.
     * @param componentType5 component type required of entities that this processor updates.
     * @param componentType6 component type required of entities that this processor updates.
     */
    protected EntityProcessor6Base(C1 componentType1,
                                   C2 componentType2,
                                   C3 componentType3,
                                   C4 componentType4,
                                   C5 componentType5,
                                   C6 componentType6) {
        this(componentType1, componentType2, componentType3, componentType4, componentType5, componentType6, null);
    }

    /**
     * @param componentType1 component type required of entities that this processor updates.
     * @param componentType2 component type required of entities that this processor updates.
     * @param componentType3 component type required of entities that this processor updates.
     * @param componentType4 component type required of entities that this processor updates.
     * @param componentType5 component type required of entities that this processor updates.
     * @param componentType6 component type required of entities that this processor updates.
     * @param updateStrategy strategy used for updates.  Null to use normal variable length timesteps with the default time,
     *                       or any UpdateStrategy to use that to determine when to update and with what time.
     *                       Use e.g. to do fixed timestep updates of the processor.
     */
    protected EntityProcessor6Base(C1 componentType1,
                                   C2 componentType2,
                                   C3 componentType3,
                                   C4 componentType4,
                                   C5 componentType5,
                                   C6 componentType6,
                                   UpdateStrategy updateStrategy) {
        super(updateStrategy, componentType1, componentType2, componentType3, componentType4, componentType5, componentType6);

        this.componentType1 = componentType1;
        this.componentType2 = componentType2;
        this.componentType3 = componentType3;
        this.componentType4 = componentType4;
        this.componentType5 = componentType5;
        this.componentType6 = componentType6;
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

    /**
     * @return fourth component type required of entities that this processor updates.
     */
    public final C4 getComponentType4() {
        return componentType4;
    }

    /**
     * @return fifth component type required of entities that this processor updates.
     */
    public final C5 getComponentType5() {
        return componentType5;
    }

    /**
     * @return sixth component type required of entities that this processor updates.
     */
    public final C6 getComponentType6() {
        return componentType6;
    }

    @Override protected final void updateEntity(Time time, int entityId) {
        updateEntity(time, entityId, componentType1, componentType2, componentType3, componentType4, componentType5, componentType6);
    }

    protected abstract void updateEntity(Time time, int entityId, C1 componentType1, C2 componentType2, C3 componentType3, C4 componentType4, C5 componentType5, C6 componentType6);
}
