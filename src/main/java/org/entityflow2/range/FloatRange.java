package org.entityflow2.range;

/**
 *
 */
public final class FloatRange implements Range<Float> {

    private final float minValue;
    private final float maxValue;

    public static final FloatRange FULL = new FloatRange();
    public static final FloatRange ZERO_TO_ONE = new FloatRange(0, 1);
    public static final FloatRange MINUS_ONE_TO_ONE = new FloatRange(-1, 1);
    public static final FloatRange ZERO_OR_LARGER = new FloatRange(0, Float.POSITIVE_INFINITY);

    public FloatRange() {
        this(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }

    public FloatRange(final float minValue, final float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Class<Float> getValueClass() {
        return Float.class;
    }

    @Override
    public Float clamp(final Float value) {
        return clampFloat(value);
    }

    public float clampFloat(final float value) {
        if (value < minValue) return minValue;
        else if (value > maxValue) return maxValue;
        else return value;
    }
}
