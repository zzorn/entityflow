package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 *
 */
public final class IntType implements Type<Integer> {

    public static final IntType TYPE = new IntType();

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }

    @Override public int getDataLengthBytes() {
        return 4;
    }

    @Override public boolean isByteBufferStorable() {
        return true;
    }

    @Override
    public Integer readValue(final ByteBuffer buffer, final int offset, Integer out) {
        return readIntValue(buffer, offset);
    }

    public int readIntValue(final ByteBuffer buffer, final int offset) {
        return buffer.getInt(offset);
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final Integer value) {
        writeIntValue(buffer, offset, value);
    }

    public void writeIntValue(final ByteBuffer buffer, final int offset, final int value) {
        buffer.putInt(offset, value);
    }

    @Override
    public Integer fromString(final String source) {
        try {
            return Integer.parseInt(source.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(final Integer value) {
        return "" + value;
    }
}
