package org.entityflow2.range;

/**
 *
 */
public final class LongRange implements Range<Long> {

    private final long minValue;
    private final long maxValue;

    public static final LongRange FULL = new LongRange();
    public static final LongRange ZERO_TO_ONE = new LongRange(0, 1);
    public static final LongRange MINUS_ONE_TO_ONE = new LongRange(-1, 1);
    public static final LongRange ZERO_OR_LARGER = new LongRange(0, Long.MAX_VALUE);
    public static final LongRange ONE_OR_LARGER = new LongRange(1, Long.MAX_VALUE);

    public LongRange() {
        this(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public LongRange(final long minValue, final long maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public Class<Long> getValueClass() {
        return Long.class;
    }

    @Override
    public Long clamp(final Long value) {
        return clampLong(value);
    }

    public long clampLong(final long value) {
        if (value < minValue) return minValue;
        else if (value > maxValue) return maxValue;
        else return value;
    }
}
