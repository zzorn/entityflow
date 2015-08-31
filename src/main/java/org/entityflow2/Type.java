package org.entityflow2;

import java.nio.ByteBuffer;

/**
 *
 */
public interface Type<T> {

    Class<T> getType();

    T readValue(ByteBuffer buffer, int offset);

    void writeValue(ByteBuffer buffer, int offset, T value);

    T fromString(String source);

    String toString(T value);



}
