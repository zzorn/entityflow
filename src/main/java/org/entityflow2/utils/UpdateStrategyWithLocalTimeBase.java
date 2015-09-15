package org.entityflow2.utils;

import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * Common functionality for UpdateStrategies that use a localTime Time instance to keep track of simulation time.
 */
public abstract class UpdateStrategyWithLocalTimeBase extends UpdateStrategyBase {

    private ManualTime localTime;

    @Override protected final void doUpdate(Updating simulation, Time externalTime) {
        // Initialize local time if needed
        if (localTime == null) {
            localTime = createLocalTime(externalTime);
        }

        doUpdate(simulation, localTime, externalTime);
    }

    /**
     *
     * @param simulation simulation to update
     * @param localTime local time managed by this UpdateStrategy, and usually passed to the simulation
     * @param externalTime time passed in as a parameter when the update on this UpdateStrategy was called.
     */
    protected abstract void doUpdate(Updating simulation, ManualTime localTime, Time externalTime);

    /**
     * @param externalTime time passed in as a parameter when the update on this UpdateStrategy was called the first time.
     * @return a new ManualTime instance, initialized based on the specified external time.
     */
    protected ManualTime createLocalTime(Time externalTime) {
        return new ManualTime(externalTime.getMillisecondsSinceStart(), externalTime.getStepCount());
    }
}
