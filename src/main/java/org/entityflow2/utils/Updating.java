package org.entityflow2.utils;

import org.flowutils.time.Time;

/**
 * Something that is updated over time.
 */
// TODO: Move this and UpdateStrategies to FlowUtils.
public interface Updating {

    /**
     * @param time current simulation time.  Also contains the duration of the last time step.
     */
    void update(Time time);

}
