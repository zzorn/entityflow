package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 *
 */
public final class ShortType implements Type<Short> {

    public static final ShortType TYPE = new ShortType();

    @Override
    public Class<Short> getValueClass() {
        return Short.class;
    }

    @Override public int getDataLengthBytes() {
        return 2;
    }

    @Override public boolean isByteBufferStorable() {
        return true;
    }

    @Override
    public Short readValue(final ByteBuffer buffer, final int offset, Short out) {
        return readShortValue(buffer, offset);
    }

    public short readShortValue(final ByteBuffer buffer, final int offset) {
        return buffer.getShort(offset);
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final Short value) {
        writeShortValue(buffer, offset, value);
    }

    public void writeShortValue(final ByteBuffer buffer, final int offset, final short value) {
        buffer.putShort(offset, value);
    }

    @Override
    public Short fromString(final String source) {
        try {
            return Short.parseShort(source.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(final Short value) {
        return "" + value;
    }
}
