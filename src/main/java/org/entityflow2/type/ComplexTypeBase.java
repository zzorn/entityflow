package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 * Base class for complex or variable length types that can not be stored in a fixed sized chunk in a byte buffer.
 */
public abstract class ComplexTypeBase<T> implements Type<T> {

    @Override public final int getDataLengthBytes() {
        return -1;
    }

    @Override public final boolean isByteBufferStorable() {
        return false;
    }

    @Override
    public T readValue(final ByteBuffer buffer, final int offset, T out) {
        throw new IllegalStateException("Can not read a value of type '"+getValueClass()+"' from a byteBuffer, as it does not support buffer storage.");
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final T value) {
        throw new IllegalStateException("Can not write a value of type '"+getValueClass()+"' to a byteBuffer, as it does not support buffer storage.");
    }

}
