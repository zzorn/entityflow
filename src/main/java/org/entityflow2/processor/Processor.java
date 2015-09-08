package org.entityflow2.processor;

import org.entityflow2.ConcurrentEntityManager;
import org.entityflow2.EntityManager;
import org.flowutils.time.Time;

/**
 *
 */
public interface Processor {

    void init(EntityManager entityManager);

    void update(Time time);

    void shutdown(EntityManager entityManager);

    EntityManager getEntityManager();

    void setEntityManager(EntityManager entityManager);
}
