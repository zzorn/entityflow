package org.entityflow.system;

import org.entityflow.entity.Entity;
import org.entityflow.entity.Message;

/**
 *
 */
public interface MessageHandler<T extends Message> {

    /**
     * Handles the specified message to the specified entity.
     * @return true if the message was handled, false if not.
     */
    boolean handleMessage(Entity entity, T message);

}
