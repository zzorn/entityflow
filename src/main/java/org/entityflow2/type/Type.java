package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 *
 */
public interface Type<T> {

    Class<T> getValueClass();

    /**
     * Read a value of this type from a byteBuffer if this type is byteBufferStorable.
     * @param buffer buffer to read the value from.
     * @param offset offset in the buffer to start from.
     * @return value read from buffer.
     */
    T readValue(ByteBuffer buffer, int offset);

    /**
     * Write a value of this type to a byteBuffer if this type is byteBufferStorable.
     * @param buffer buffer to store the value to.
     * @param offset offset to first byte to store the value at.
     * @param value value to store.
     */
    void writeValue(ByteBuffer buffer, int offset, T value);

    T fromString(String source);

    String toString(T value);

    /**
     * @return number of bytes that readValue and writeValue use when reading or storing this type of value.
     *         Must be a constant.  For complex types (when isByteBufferStorable is false), return 0.
     */
    int getDataLengthBytes();

    /**
     * @return true if this type can be stored in a byteBuffer in a fixed size.
     *         false for complex types that require a class, list, string, variable length array, etc.
     */
    boolean isByteBufferStorable();


}
