package org.entityflow2.utils;

import org.flowutils.time.Time;

/**
 * Encapsulates some update over time strategy.
 */
public interface UpdateStrategy<T extends Updating> extends Updating {

    /**
     * @param simulation the thing that should be updated by this UpdateStrategy.
     */
    void setSimulation(T simulation);

    /**
     * @return the thing being updated by this UpdateStrategy.
     */
    T getSimulation();

    /**
     * Alternative way to use the UpdateStrategy, instead of setting the simulation property of the UpdateStrategy,
     * it will update the provided simulation.
     *
     * Note that the UpdateStrategy still has one common state for all simulations, so this can not be used to update different simulations.
     *
     * @param simulation simulation to update.
     * @param externalTime time to use when updating.
     */
    void updateSimulation(T simulation, Time externalTime);

}
