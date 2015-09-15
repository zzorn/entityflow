package org.entityflow2.utils;

import org.flowutils.Check;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * An UpdateStrategy that caps the maximum duration of time steps.
 *
 * This helps limit physics and other artifacts in case the application experiences lag spikes or slowness
 * (e.g. caused by temporary system overload due to unrelated things, or things like disk io).
 */
public final class CappedTimestepStrategy<T extends Updating> extends UpdateStrategyWithLocalTimeBase<T> {

    private long maximumStepDuration_milliseconds;

    /**
     * Creates a CappedStepDurationStrategy with the default maximum step duration of 100 milliseconds,
     * corresponding to a slowest allowed simulation framerate of 10fps.
     */
    public CappedTimestepStrategy() {
        this(100);
    }

    /**
     * @param maximumStepDuration_milliseconds maximum allowed update duration in milliseconds.  If an update would be slower, the time is capped to this value.
     */
    public CappedTimestepStrategy(long maximumStepDuration_milliseconds) {
        this.maximumStepDuration_milliseconds = maximumStepDuration_milliseconds;
    }

    /**
     * @param maximumStepDuration_milliseconds maximum allowed update duration in milliseconds.  If an update would be slower, the time is capped to this value.
     * @param simulation simulation to update.
     */
    public CappedTimestepStrategy(long maximumStepDuration_milliseconds,
                                  T simulation) {
        super(simulation);
        this.maximumStepDuration_milliseconds = maximumStepDuration_milliseconds;
    }

    /**
     * @return maximum allowed update duration.  If an update would be slower, the time is capped to this value.
     */
    public long getMaximumStepDuration_milliseconds() {
        return maximumStepDuration_milliseconds;
    }

    /**
     * @param maximumStepDuration_milliseconds maximum allowed update duration in milliseconds.  If an update would be slower, the time is capped to this value.
     */
    public void setMaximumStepDuration_milliseconds(long maximumStepDuration_milliseconds) {
        Check.positive(maximumStepDuration_milliseconds, "maximumStepDuration_milliseconds");
        this.maximumStepDuration_milliseconds = maximumStepDuration_milliseconds;
    }

    @Override protected void update(T simulation, ManualTime localTime, Time externalTime) {

        // Cap the elapsed time
        final long elapsedTime_ms = Math.min(maximumStepDuration_milliseconds, externalTime.getLastStepDurationMs());

        // Update local time
        localTime.advanceTime(elapsedTime_ms);
        localTime.nextStep();

        // Update simulation
        simulation.update(localTime);
    }
}
