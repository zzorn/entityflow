package org.entityflow2.processor;

import org.entityflow2.ConcurrentEntityManager;
import org.entityflow2.EntityManager;
import org.flowutils.time.Time;
import org.flowutils.updating.Updating;

/**
 * Processors are registered with an EntityManager, and typically process entities with certain component types,
 * updating the component states when update is called.
 */
public interface Processor extends Updating {

    /**
     * Called after the processor has been registered with an entity manager, and before update is called.
     * @param entityManager the EntityManager that the processor is registered with.
     */
    void init(EntityManager entityManager);

    /**
     * Called each frame / simulation update step by the EntityManager that this processor is registered with.
     * @param time simulation time (may be real-time).  Contains information on time elapsed since the last call to update.
     */
    void update(Time time);

    /**
     * Called when the EntityManager that this processor is registered with shuts down.
     */
    void shutdown(EntityManager entityManager);

    /**
     * @return the EntityManager that this Processor is registered with.
     */
    EntityManager getEntityManager();

    /**
     * @param entityManager the EntityManager that this processor is registered with.
     *                      Called by the EntityManager when the processor is registered.
     */
    void setEntityManager(EntityManager entityManager);
}
