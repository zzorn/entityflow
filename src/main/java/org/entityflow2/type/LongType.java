package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 *
 */
public final class LongType implements Type<Long> {

    public static final LongType TYPE = new LongType();

    @Override
    public Class<Long> getValueClass() {
        return Long.class;
    }

    @Override public int getDataLengthBytes() {
        return 8;
    }

    @Override public boolean isByteBufferStorable() {
        return true;
    }

    @Override
    public Long readValue(final ByteBuffer buffer, final int offset, Long out) {
        return readLongValue(buffer, offset);
    }

    public long readLongValue(final ByteBuffer buffer, final int offset) {
        return buffer.getLong(offset);
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final Long value) {
        writeLongValue(buffer, offset, value);
    }

    public void writeLongValue(final ByteBuffer buffer, final int offset, final long value) {
        buffer.putLong(offset, value);
    }

    @Override
    public Long fromString(final String source) {
        try {
            return Long.parseLong(source.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(final Long value) {
        return "" + value;
    }
}
