package org.entityflow.util;

import java.lang.Math;import java.lang.System;

/**
 * Used for something that happens at certain intervals, to keep track of time since the last tick.
 */
public class Ticker {

    private long millisecondsSinceStart = 0;
    private long lastFrameTimeStamp;
    private long lastTickDurationMs;
    private long tickCount = 0;
    private double lastTickDurationSeconds;

    public Ticker() {
        reset();
    }

    /**
     * Call to start the ticker from zero.
     */
    public void reset() {
        lastFrameTimeStamp = System.currentTimeMillis();
        millisecondsSinceStart = 0;
        lastTickDurationMs = 0;
        lastTickDurationSeconds = 0;
        tickCount = 0;
    }

    /**
     * Call this every frame.
     */
    public void tick() {
        long time = System.currentTimeMillis();
        tickCount++;
        lastTickDurationMs = Math.max(0, time - lastFrameTimeStamp);
        millisecondsSinceStart += lastTickDurationMs;
        lastTickDurationSeconds = lastTickDurationMs * 0.001;
        lastFrameTimeStamp = time;
    }

    public double getSecondsSinceLastTick() {
        return 1000.0 * (System.currentTimeMillis() - lastFrameTimeStamp);
    }

    public double getLastTickDurationSeconds() {
        return lastTickDurationSeconds;
    }

    public long getLastTickDurationMs() {
        return lastTickDurationMs;
    }

    public double getLastTicksPerSecond() {
        if (lastTickDurationMs == 0) return 0;
        else return 1.0 / (lastTickDurationMs * 1000);
    }

    public long getMillisecondsSinceStart() {
        return millisecondsSinceStart;
    }

    public long getTickCount() {
        return tickCount;
    }
}
