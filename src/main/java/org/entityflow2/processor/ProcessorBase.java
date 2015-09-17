package org.entityflow2.processor;

import org.entityflow2.EntityManager;
import org.flowutils.time.Time;
import org.flowutils.updating.UpdatingWithStrategy;
import org.flowutils.updating.strategies.UpdateStrategy;

import static org.flowutils.Check.notNull;

/**
 * Some common functionality for Processors.
 */
public abstract class ProcessorBase extends UpdatingWithStrategy implements Processor {

    private EntityManager entityManager;

    protected ProcessorBase() {
        this(null);
    }

    /**
     * @param updateStrategy strategy used for updates.  Null to use normal variable length timesteps with the default time,
     *                       or any UpdateStrategy to use that to determine when to update and with what time.
     *                       Use e.g. to do fixed timestep updates of the processor.
     */
    protected ProcessorBase(UpdateStrategy updateStrategy) {
        super(updateStrategy);
    }

    @Override public final EntityManager getEntityManager() {
        return entityManager;
    }

    @Override public final void setEntityManager(EntityManager entityManager) {
        notNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

}
