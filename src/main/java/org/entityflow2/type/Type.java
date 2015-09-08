package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 * Holds information about how to serialize a type.
 */
// TODO: Should we also support serializing to/from xml?
public interface Type<T> {

    Class<T> getValueClass();

    /**
     * Read a value of this type from a byteBuffer if this type is byteBufferStorable.
     * @param buffer buffer to read the value from.
     * @param offset offset in the buffer to start from.
     * @param out if the type is mutable and out is not null, then out will be used to write the data to, and returned as a result.
     *            Used e.g. for vector objects and similar.
     * @return value read from buffer.
     */
    T readValue(ByteBuffer buffer, int offset, T out);

    /**
     * Write a value of this type to a byteBuffer if this type is byteBufferStorable.
     * @param buffer buffer to store the value to.
     * @param offset offset to first byte to store the value at.
     * @param value value to store.
     */
    void writeValue(ByteBuffer buffer, int offset, T value);

    /**
     * Parse the value from a string.
     */
    T fromString(String source);

    /**
     * Convert the value to a string that can be parsed with fromString().
     */
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
