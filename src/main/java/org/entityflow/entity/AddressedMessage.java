package org.entityflow.entity;

import org.flowutils.Check;

/**
 *
 */
public final class AddressedMessage {

    private Message message;
    private long entityId;
    private boolean externalSource;

    public AddressedMessage() {
    }

    public void set(Message message, long entityId, boolean externalSource) {
        this.message = message;
        this.entityId = entityId;
        this.externalSource = externalSource;
    }

    public void clear() {
        message = null;
        entityId = 0;
        externalSource = false;
    }

    public Message getMessage() {
        return message;
    }

    public long getEntityId() {
        return entityId;
    }

    public boolean isExternalSource() {
        return externalSource;
    }

    @Override public String toString() {
        return "AddressedMessage{" +
               "message=" + message +
               ", entityId=" + entityId +
               ", externalSource=" + externalSource +
               '}';
    }
}
