package org.entityflow2.type;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 */
public final class DoubleType implements Type<Double> {

    public static final DoubleType TYPE = new DoubleType();

    private final ThreadLocal<NumberFormat> format = new ThreadLocal<NumberFormat>() {
        @Override
        protected NumberFormat initialValue() {
            return new DecimalFormat("#.##########");
        }
    };

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }

    @Override public int getDataLengthBytes() {
        return 8;
    }

    @Override public boolean isByteBufferStorable() {
        return true;
    }

    @Override
    public Double readValue(final ByteBuffer buffer, final int offset, Double out) {
        return readDoubleValue(buffer, offset);
    }

    public double readDoubleValue(final ByteBuffer buffer, final int offset) {
        return buffer.getDouble(offset);
    }

    @Override
    public void writeValue(final ByteBuffer buffer, final int offset, final Double value) {
        writeDoubleValue(buffer, offset, value);
    }

    public void writeDoubleValue(final ByteBuffer buffer, final int offset, final double value) {
        buffer.putDouble(offset, value);
    }

    @Override
    public Double fromString(final String source) {
        try {
            return Double.parseDouble(source.trim());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString(final Double value) {
        return format.get().format(value);
    }
}
