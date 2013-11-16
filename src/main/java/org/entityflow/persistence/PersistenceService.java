package org.entityflow.persistence;

import org.entityflow.entity.Message;

/**
 * Persistence service, takes care of saving world state for later recovery or reload.
 */
public interface PersistenceService {

    /**
     * Log a message that was received from outside the simulation, to allow replaying the simulation on restart.
     *
     * Needs to accept concurrent calls.
     *
     * @param simulationTick the simulation round (tick) that the message was received at.
     * @param recipientEntity id of entity the message was sent to.
     * @param message the message.
     */
    void storeExternalMessage(long simulationTick, long recipientEntity, Message message);


}
