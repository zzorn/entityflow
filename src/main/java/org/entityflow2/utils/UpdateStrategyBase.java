package org.entityflow2.utils;

import org.flowutils.time.Time;

/**
 * Common functionality for UpdateStrategies.
 */
public abstract class UpdateStrategyBase<T extends Updating> implements UpdateStrategy<T> {

    private T simulation;

    public UpdateStrategyBase() {
        this(null);
    }

    public UpdateStrategyBase(T simulation) {
        setSimulation(simulation);
    }

    @Override public final void setSimulation(T simulation) {
        this.simulation = simulation;
    }

    @Override public final T getSimulation() {
        return simulation;
    }

    @Override public final void updateSimulation(T simulation, Time externalTime) {
        if (simulation != null) {
            update(simulation, externalTime);
        }
    }

    @Override public final void update(Time externalTime) {
        final T simulation = this.simulation;
        if (simulation != null) {
            update(simulation, externalTime);
        }
    }

    protected abstract void update(T simulation, Time externalTime);
}
