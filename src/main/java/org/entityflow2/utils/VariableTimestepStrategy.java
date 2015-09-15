package org.entityflow2.utils;

import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * Simple UpdateStrategy that simply uses the provided time for updating.
 *
 * If the provided time is e.g. updated every frame, it leads to a variable timestep strategy where the
 * timestep is the duration of the last frame.
 *
 * It is not very suitable for most physics simulations, but will suffice well for non-time sensitive things.
 */
public final class VariableTimestepStrategy<T extends Updating> extends UpdateStrategyBase<T> {

    public VariableTimestepStrategy() {
    }

    public VariableTimestepStrategy(T simulation) {
        super(simulation);
    }

    @Override protected void update(T simulation, Time externalTime) {
        simulation.update(externalTime);
    }

}
