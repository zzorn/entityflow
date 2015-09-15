package org.entityflow2.utils;

import org.flowutils.time.Time;

/**
 * Encapsulates some update over time.
 *
 * Note that this strategy may store state information related to things like timesteps,
 * so it should only be used to update one simulation.
 */
public interface UpdateStrategy {

    /**
     * Updates the specified simulation with the specified time, using this UpdateStrategy.
     *
     * @param simulation simulation to update.
     * @param externalTime time to use when updating.
     */
    void update(Updating simulation, Time externalTime);

}
