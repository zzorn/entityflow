package org.entityflow2.type;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 */
public final class FloatType implements Type<Float> {

    public static final FloatType TYPE = new FloatType();

    private final ThreadLocal<NumberFormat> format = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {
            return new DecimalFormat("#.##########");
        }
    };

    @Override
    public Class<Float> getValueClass() {
        return Float.class;
    }

    @Override public int getDataLengthBytes() {
        return 4;
    }

    @Override public boolean isByteBufferStorable() {
        return true;
    }

    @Override
    public Float readValue(final ByteBuffer buffer, final int offset, Float out) {
        return readFloatValue(buffer, offset);
    }

    public float readFloatValue(final ByteBuffer buffer, final int offset) {
        return buffer.getFloat(offset);
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final Float value) {
        writeFloatValue(buffer, offset, value);
    }

    public void writeFloatValue(final ByteBuffer buffer, final int offset, final float value) {
        buffer.putFloat(offset, value);
    }

    @Override
    public Float fromString(final String source) {
        try {
            return Float.parseFloat(source.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(final Float value) {
        return format.get().format(value);
    }
}
