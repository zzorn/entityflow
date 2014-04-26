package org.entityflow.persistence;

import org.entityflow.entity.Message;

/**
 * A persistence service that just discards data instead of storing it.
 */
public final class NoPersistence implements PersistenceService {
    @Override public void storeExternalMessage(long simulationTick, long recipientEntity, Message message) {
        // Discard message
    }
}
