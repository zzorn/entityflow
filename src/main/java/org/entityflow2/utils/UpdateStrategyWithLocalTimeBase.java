package org.entityflow2.utils;

import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * Common functionality for UpdateStrategies that use a localTime Time instance to keep track of simulation time.
 */
public abstract class UpdateStrategyWithLocalTimeBase<T extends Updating> extends UpdateStrategyBase<T> {

    private ManualTime localTime;

    public UpdateStrategyWithLocalTimeBase() {
        this(null);
    }

    public UpdateStrategyWithLocalTimeBase(T simulation) {
        super(simulation);
    }

    @Override public final void update(T simulation, Time externalTime) {
        // Initialize local time if needed
        if (localTime == null) {
            localTime = createLocalTime(externalTime);
        }

        update(simulation, localTime, externalTime);
    }

    protected abstract void update(T simulation, ManualTime localTime, Time externalTime);

    protected ManualTime createLocalTime(Time externalTime) {
        return new ManualTime(externalTime.getMillisecondsSinceStart(), externalTime.getStepCount());
    }
}
