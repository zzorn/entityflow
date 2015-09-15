package org.entityflow2.utils;

import org.flowutils.Check;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * A UpdateStrategy that calls the enclosed simulation at a specific fixed rate.
 *
 * For each update call to the strategy, the simulation may be called zero or more times, depending on how long
 * time has elapsed since the last call to the simulation.
 *
 * The Time instance provided in the update method is used to determine times, so the provided time does not have to
 * agree with real time.
 */
// TODO: Convert this to use milliseconds as timestep unit,
// TODO  or change Time to keep track of the elapsed time with seconds & doubles instead of milliseconds & longs.
// TODO: If Time is not changed, at least add a method to get the smallest possible timestep in seconds.
public final class FixedTimestepStrategy extends UpdateStrategyWithLocalTimeBase {

    private static final double DEFAULT_TIMESTEP_SECONDS = 0.01;
    private static final double MINIMUM_TIMESTEP = 0.001;

    private double timestepSeconds;

    private double timeSinceLastUpdateCall_seconds = 0;

    /**
     * Creates a FixedTimestepStrategy with a default timestep of 0.01 seconds.
     */
    public FixedTimestepStrategy() {
        this(DEFAULT_TIMESTEP_SECONDS);
    }

    /**
     * @param timestepSeconds the time in seconds between update calls to the simulation.
     */
    public FixedTimestepStrategy(double timestepSeconds) {
        setTimestepSeconds(timestepSeconds);
    }

    /**
     * @return the time in seconds between update calls to the simulation.
     */
    public double getTimestepSeconds() {
        return timestepSeconds;
    }

    /**
     * @param timestepSeconds the time in seconds between update calls to the simulation.
     */
    public void setTimestepSeconds(double timestepSeconds) {
        Check.greater(timestepSeconds,
                      "timestepSeconds",
                      MINIMUM_TIMESTEP,
                      "smallest possible timestep (a millisecond)");

        this.timestepSeconds = timestepSeconds;
    }

    @Override protected void doUpdate(Updating simulation, ManualTime localTime, Time externalTime) {
        // Add elapsed time since the last update call to the backlog
        timeSinceLastUpdateCall_seconds += externalTime.getLastStepDurationSeconds();

        final double timestepSeconds = this.timestepSeconds;
        while (timeSinceLastUpdateCall_seconds > timestepSeconds) {
            // Decrease built up time backlog by one simulation step size
            timeSinceLastUpdateCall_seconds -= timestepSeconds;

            // Update local time
            localTime.advanceTime((long) (timestepSeconds / 1000.0));
            localTime.nextStep();

            // Update simulation
            simulation.update(localTime);
        }
    }
}
