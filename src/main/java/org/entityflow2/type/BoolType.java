package org.entityflow2.type;

import java.nio.ByteBuffer;

/**
 *
 */
public final class BoolType implements Type<Boolean> {

    public static final BoolType TYPE = new BoolType();

    @Override
    public Class<Boolean> getValueClass() {
        return Boolean.class;
    }

    @Override public int getDataLengthBytes() {
        return 1;
    }

    @Override public boolean isByteBufferStorable() {
        return true;
    }

    @Override
    public Boolean readValue(final ByteBuffer buffer, final int offset, Boolean out) {
        return readBoolValue(buffer, offset);
    }

    public boolean readBoolValue(final ByteBuffer buffer, final int offset) {
        return buffer.get(offset) != 0;
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final Boolean value) {
        writeBoolValue(buffer, offset, value);
    }

    public void writeBoolValue(final ByteBuffer buffer, final int offset, final boolean value) {
        buffer.put(offset, value ? (byte) 1 : (byte) 0);
    }

    @Override
    public Boolean fromString(final String source) {
        try {
            return Boolean.parseBoolean(source.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(final Boolean value) {
        return "" + value;
    }
}
