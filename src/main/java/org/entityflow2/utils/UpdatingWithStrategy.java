package org.entityflow2.utils;

import org.flowutils.time.Time;

/**
 * An Updating simulation, along with an update strategy that is used for it.
 */
public final class UpdatingWithStrategy implements Updating {

    private Updating simulation;
    private UpdateStrategy updateStrategy;

    /**
     * Uses no special strategy, just updates the simulation directly with the provided time.
     * @param simulation simulation to update
     */
    public UpdatingWithStrategy(Updating simulation) {
        this(simulation, null);
    }

    /**
     * @param simulation simulation to update
     * @param updateStrategy strategy to use when updating, or null to not use any special strategy,
     *                       just update the simulation directly with the provided time.
     */
    public UpdatingWithStrategy(Updating simulation, UpdateStrategy updateStrategy) {
        this.simulation = simulation;
        this.updateStrategy = updateStrategy;
    }

    /**
     * Create a StrategizedUpdating with two or more update strategies, applied in reverse order (last added strategies are executed first).
     * @param simulation simulation to update
     */
    public UpdatingWithStrategy(Updating simulation,
                                UpdateStrategy firstUpdateStrategy,
                                UpdateStrategy secondUpdateStrategy,
                                UpdateStrategy... additionalUpdateStrategies) {
        this(simulation);

        final ChainedUpdateStrategy chainedStrategy = new ChainedUpdateStrategy();
        chainedStrategy.addStrategy(firstUpdateStrategy);
        chainedStrategy.addStrategy(secondUpdateStrategy);
        chainedStrategy.addStrategies(additionalUpdateStrategies);
        setUpdateStrategy(chainedStrategy);
    }

    public Updating getSimulation() {
        return simulation;
    }

    public void setSimulation(Updating simulation) {
        this.simulation = simulation;
    }

    public UpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(UpdateStrategy updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    @Override public void update(Time time) {
        if (simulation != null) {
            if (updateStrategy != null) {
                updateStrategy.update(simulation, time);
            }
            else {
                simulation.update(time);
            }
        }
    }
}
