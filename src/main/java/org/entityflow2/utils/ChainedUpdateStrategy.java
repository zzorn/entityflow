package org.entityflow2.utils;

import org.flowutils.time.Time;

import java.util.Collection;

import static org.flowutils.Check.notNull;

/**
 * Contains zero or more update strategies that are executed in reverse order of addition (first added strategy is executed last).
 */
public final class ChainedUpdateStrategy extends UpdateStrategyBase {

    private UpdatingWithStrategy lastUpdating;

    private final ThreadLocal<UpdatingWithStrategy> terminalUpdating = new ThreadLocal<UpdatingWithStrategy>() {
        @Override protected UpdatingWithStrategy initialValue() {
            return new UpdatingWithStrategy(null);
        }
    };

    /**
     * Creates a chained strategy.
     * @param initialStrategies initial strategies to add.
     *                          The strategies are executed before existing strategies, in reverse order (last added strategy is executed first).
     */
    public ChainedUpdateStrategy(UpdateStrategy ... initialStrategies) {
        lastUpdating = terminalUpdating.get();
        addStrategies(initialStrategies);
    }

    /**
     * @param updateStrategies strategies to add.  The strategies are executed before existing strategies, in reverse order (last added strategy is executed first).
     */
    public void addStrategies(UpdateStrategy ... updateStrategies) {
        for (UpdateStrategy updateStrategy : updateStrategies) {
            addStrategy(updateStrategy);
        }
    }

    /**
     * @param updateStrategies strategies to add.  The strategies are executed before existing strategies, in reverse order (last added strategy is executed first).
     */
    public void addStrategies(Collection<UpdateStrategy> updateStrategies) {
        for (UpdateStrategy updateStrategy : updateStrategies) {
            addStrategy(updateStrategy);
        }
    }

    /**
     * @param updateStrategy strategy to add.  The strategy is executed before existing strategies (last added strategy is executed first).
     */
    public void addStrategy(UpdateStrategy updateStrategy) {
        notNull(updateStrategy, "updateStrategy");

        lastUpdating = new UpdatingWithStrategy(lastUpdating, updateStrategy);
    }

    /**
     * Removes all strategies from this chained strategies.
     */
    public void removeAllStrategies() {
        lastUpdating = terminalUpdating.get();
    }

    @Override protected void doUpdate(Updating simulation, Time externalTime) {
        terminalUpdating.get().setSimulation(simulation);
        lastUpdating.update(externalTime);
    }
}
