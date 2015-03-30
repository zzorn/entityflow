package org.entityflow.processors;


import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.flowutils.Check;
import org.flowutils.MathUtils;
import org.flowutils.service.ServiceBase;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * Base class for processor implementations, does not do any entity management.
 */
// TODO: Better timestep handling?
public abstract class ProcessorBase extends ServiceBase implements Processor {

    private final Class<? extends Processor> baseType;
    private long processingIntervalMilliseconds = 10;
    private final ManualTime time = new ManualTime();

    private boolean fixedTimeStep = true;
    private int maxTimeStepsToAdvance = 10;

    private long  excessMillisecondsFromLastStep;

    private World world = null;

    protected ProcessorBase() {
        this(null);
    }

    protected ProcessorBase(Class<? extends Processor> baseType) {
        this(baseType, 0.01);
    }

    protected ProcessorBase(Class<? extends Processor> baseType, double processingIntervalSeconds) {
        if (baseType == null) this.baseType = getClass();
        else this.baseType = baseType;

        setProcessingIntervalSeconds(processingIntervalSeconds);
    }

    /**
     * @return an approximate minimum interval in seconds between each time that the processors is processed.
     *                                  Zero if the processors is processed every time process() is called.
     *                                  Does at most one processing call each time World.process() is called.
     */
    public final double getProcessingIntervalSeconds() {
        return processingIntervalMilliseconds / 1000.0;
    }

    /**
     * @param processingIntervalSeconds an approximate minimum interval in seconds between each time that the processors is processed.
     *                                  Set to zero to process the processors every time process() is called.
     *                                  Does at most one processing call each time World.process() is called.
     */
    public final void setProcessingIntervalSeconds(double processingIntervalSeconds) {
        setProcessingIntervalMilliseconds((long) (processingIntervalSeconds * 1000));
    }

    public long getProcessingIntervalMilliseconds() {
        return processingIntervalMilliseconds;
    }

    public void setProcessingIntervalMilliseconds(long processingIntervalMilliseconds) {
        Check.positive(processingIntervalMilliseconds, "processingIntervalMilliseconds");

        this.processingIntervalMilliseconds = processingIntervalMilliseconds;
    }

    /**
     * @return time used by this processor.
     */
    public Time getTime() {
        return time;
    }

    /**
     * @return if true, the time of this processor is always advanced by the processingInterval,
     * instead of possibly larger increments if more time has passed.
     * If more time than one processing interval has passed, the update methods may be called at most maxTimeStepsToAdvance times.
     * If false, the time is advanced by processingInterval or more, if more time has passed.
     */
    public boolean isFixedTimeStep() {
        return fixedTimeStep;
    }

    /**
     * @param fixedTimeStep if true, the time of this processor is always advanced by the processingInterval,
     * instead of possibly larger increments if more time has passed.
     * If more time than one processing interval has passed, the update methods may be called at most maxTimeStepsToAdvance times.
     * If false, the time is advanced by processingInterval or more, if more time has passed.
     */
    public void setFixedTimeStep(boolean fixedTimeStep) {
        this.fixedTimeStep = fixedTimeStep;
    }

    /**
     * @return if fixedTimeStep is true, this tells how many time steps we can advance at most on one call.
     * If it is false, this is not used.
     */
    public int getMaxTimeStepsToAdvance() {
        return maxTimeStepsToAdvance;
    }

    /**
     * @param maxTimeStepsToAdvance if fixedTimeStep is true, this tells how many time steps we can advance at most on one call.
     * If it is false, this is not used.
     */
    public void setMaxTimeStepsToAdvance(int maxTimeStepsToAdvance) {
        this.maxTimeStepsToAdvance = maxTimeStepsToAdvance;
    }

    @Override
    public Class<? extends Processor> getBaseType() {
        return baseType;
    }

    @Override
    public final void init(World world) {
        this.world = world;
        time.reset();
        excessMillisecondsFromLastStep = 0;

        init();
    }

    /**
     * @return the world the processors is added to, or null if not yet initialized.
     */
    public final World getWorld() {
        return world;
    }

    @Override
    public final void process(Time worldTime) {

        if (fixedTimeStep) {
            long millisecondsToAdvance = worldTime.getLastStepDurationMs() + excessMillisecondsFromLastStep;

            long steps = millisecondsToAdvance / processingIntervalMilliseconds;

            // Clamp number of steps to do
            if (steps > maxTimeStepsToAdvance) {
                steps = maxTimeStepsToAdvance;
            }

            excessMillisecondsFromLastStep = millisecondsToAdvance - steps * processingIntervalMilliseconds;

            // Clamp the excess time, so that simulation speed is limited if we run out of processing power, instead of bogging down the system
            final long maxExcessTime = maxTimeStepsToAdvance * processingIntervalMilliseconds;
            if (excessMillisecondsFromLastStep < 0) excessMillisecondsFromLastStep = 0;
            else if (excessMillisecondsFromLastStep > maxExcessTime) excessMillisecondsFromLastStep = maxExcessTime;

            // Process the steps
            for (int i = 0; i < steps; i++) {
                time.advanceTime(processingIntervalMilliseconds);
                doProcess(time);
                time.nextStep();
            }
        }
        else {
            // Variable timestep

            // Update own time with world time
            time.advanceTime(worldTime.getLastStepDurationMs());

            // Do normal processing if enough time has passed since the last time
            if (time.getMillisecondsSinceLastStep() >= processingIntervalMilliseconds) {
                doProcess(time);
                time.nextStep();
            }
        }
    }

    /**
     * Processes this entity processors.
     *
     * @param systemTime a time with information on how long since this processors was last processed.
     */
    protected void doProcess(Time systemTime) {
    }

    @Override
    public void onEntityAdded(Entity entity) {
    }

    @Override
    public void onEntityRemoved(Entity entity) {
    }

    @Override
    public void onEntityComponentsChanged(Entity entity) {
    }



}
