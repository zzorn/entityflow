package org.entityflow2.range;

/**
 *
 */
public final class IntRange implements Range<Integer> {

    private final int minValue;
    private final int maxValue;

    public static final IntRange FULL = new IntRange();
    public static final IntRange ZERO_TO_ONE = new IntRange(0, 1);
    public static final IntRange MINUS_ONE_TO_ONE = new IntRange(-1, 1);
    public static final IntRange ZERO_OR_LARGER = new IntRange(0, Integer.MAX_VALUE);
    public static final IntRange ONE_OR_LARGER = new IntRange(1, Integer.MAX_VALUE);

    public IntRange() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public IntRange(final int minValue, final int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }

    @Override
    public Integer clamp(final Integer value) {
        return clampInt(value);
    }

    public int clampInt(final int value) {
        if (value < minValue) return minValue;
        else if (value > maxValue) return maxValue;
        else return value;
    }
}
