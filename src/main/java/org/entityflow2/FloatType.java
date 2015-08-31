package org.entityflow2;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 */
public final class FloatType<T> implements Type<Float> {

    private final ThreadLocal<NumberFormat> format = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {
            return new DecimalFormat("#.##########");
        }
    };

    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public Float readValue(final ByteBuffer buffer, final int offset) {
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
