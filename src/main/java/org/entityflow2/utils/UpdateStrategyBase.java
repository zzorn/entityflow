package org.entityflow2.utils;

import org.flowutils.time.Time;

/**
 * Common functionality for UpdateStrategies.
 */
public abstract class UpdateStrategyBase implements UpdateStrategy {

    @Override public void update(Updating simulation, Time externalTime) {
        if (simulation != null) {
            doUpdate(simulation, externalTime);
        }
    }

    protected abstract void doUpdate(Updating simulation, Time externalTime);
}
