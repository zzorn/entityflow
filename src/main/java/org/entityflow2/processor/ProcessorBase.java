package org.entityflow2.processor;

import org.entityflow2.EntityManager;

import static org.flowutils.Check.notNull;

/**
 * Some common functionality for Processors.
 */
public abstract class ProcessorBase implements Processor {

    private EntityManager entityManager;

    @Override public final EntityManager getEntityManager() {
        return entityManager;
    }

    @Override public final void setEntityManager(EntityManager entityManager) {
        notNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }


}
