package org.entityflow2.range;

/**
 *
 */
public final class DoubleRange implements Range<Double> {

    private final double minValue;
    private final double maxValue;

    public static final DoubleRange FULL = new DoubleRange();
    public static final DoubleRange ZERO_TO_ONE = new DoubleRange(0, 1);
    public static final DoubleRange MINUS_ONE_TO_ONE = new DoubleRange(-1, 1);
    public static final DoubleRange ZERO_OR_LARGER = new DoubleRange(0, Double.POSITIVE_INFINITY);

    public DoubleRange() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public DoubleRange(final double minValue, final double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }

    @Override
    public Double clamp(final Double value) {
        return clampDouble(value);
    }

    public double clampDouble(final double value) {
        if (value < minValue) return minValue;
        else if (value > maxValue) return maxValue;
        else return value;
    }
}
