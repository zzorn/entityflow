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
// IDEA: Refactor processing update strategy to enum or external strategy
public abstract class ProcessorBase extends ServiceBase implements Processor {

    private final Class<? extends Processor> baseType;
    private long processingIntervalMilliseconds = 10;
    private final ManualTime time = new ManualTime();

    private boolean fixedTimeStep = true;
    private int maxTimeStepsToAdvance = 10;
    private boolean discardSimulationStepsWhenProcessorOverworked = false; // If true, the simulation will not be deterministic.

    private long  excessMillisecondsFromLastStep;

    private World world = null;

    protected ProcessorBase() {
        this(null);
    }

    protected ProcessorBase(Class<? extends Processor> baseType) {
        this(baseType, 0.01);
    }

    protected ProcessorBase(Class<? extends Processor> baseType, double processingIntervalSeconds) {
        // No need to shut down processors on JVM exit, the game should take care of shutting down the world (and other resources) on JVM exit.
        setAutomaticallyShutDownWhenJVMClosing(false);

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

    public final long getProcessingIntervalMilliseconds() {
        return processingIntervalMilliseconds;
    }

    public final void setProcessingIntervalMilliseconds(long processingIntervalMilliseconds) {
        Check.positiveOrZero(processingIntervalMilliseconds, "processingIntervalMilliseconds");

        this.processingIntervalMilliseconds = processingIntervalMilliseconds;
    }

    /**
     * @return time used by this processor.
     */
    public final Time getTime() {
        return time;
    }

    /**
     * @return if true, the time of this processor is always advanced by the processingInterval,
     * instead of possibly larger increments if more time has passed.
     * If more time than one processing interval has passed, the update methods may be called at most maxTimeStepsToAdvance times.
     * If false, the time is advanced by processingInterval or more, if more time has passed.
     */
    public final boolean isFixedTimeStep() {
        return fixedTimeStep;
    }

    /**
     * @param fixedTimeStep if true, the time of this processor is always advanced by the processingInterval,
     * instead of possibly larger increments if more time has passed.
     * If more time than one processing interval has passed, the update methods may be called at most maxTimeStepsToAdvance times.
     * If false, the time is advanced by processingInterval or more, if more time has passed.
     */
    public final void setFixedTimeStep(boolean fixedTimeStep) {
        this.fixedTimeStep = fixedTimeStep;
    }

    /**
     * @return if true, simulation steps will be discarded if more than maxTimeStepsToAdvance would be needed during an update.
     *         When true, the simulation will run slower but not freeze the computer if the processing is slow,
     *         but the simulation will not be deterministic (re-running it with the same start state will result in different states).
     *         If false, the simulation will run slower and the machine will run slower, but the simulation will be deterministic.
     */
    public final boolean isDiscardSimulationStepsWhenProcessorOverworked() {
        return discardSimulationStepsWhenProcessorOverworked;
    }

    /**
     * @param discardSimulationStepsWhenProcessorOverworked if true, simulation steps will be discarded if more than maxTimeStepsToAdvance would be needed during an update.
     *         When true, the simulation will run slower but not freeze the computer if the processing is slow,
     *         but the simulation will not be deterministic (re-running it with the same start state will result in different states).
     *         If false, the simulation will run slower and the machine will run slower, but the simulation will be deterministic.     */
    public final void setDiscardSimulationStepsWhenProcessorOverworked(boolean discardSimulationStepsWhenProcessorOverworked) {
        this.discardSimulationStepsWhenProcessorOverworked = discardSimulationStepsWhenProcessorOverworked;
    }

    /**
     * @return if fixedTimeStep and discardSimulationStepsWhenProcessorOverworked is true,
     *         this tells how many time steps we can advance at most on one call.  Otherwise, this is not used.
     */
    public final int getMaxTimeStepsToAdvance() {
        return maxTimeStepsToAdvance;
    }

    /**
     * @param maxTimeStepsToAdvance if fixedTimeStep and discardSimulationStepsWhenProcessorOverworked is true,
     *         this tells how many time steps we can advance at most on one call.  Otherwise, this is not used.
     */
    public final void setMaxTimeStepsToAdvance(int maxTimeStepsToAdvance) {
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
        if (processingIntervalMilliseconds <= 0) {
            // Process every time process is called
            time.advanceTimeSeconds(worldTime.getLastStepDurationSeconds());
            doProcess(time);
        }
        else {
            if (fixedTimeStep) {
                long millisecondsToAdvance = worldTime.getLastStepDurationMilliseconds() + excessMillisecondsFromLastStep;

                long steps = millisecondsToAdvance / processingIntervalMilliseconds;

                // Clamp number of steps to do, if we should discardSimulationStepsWhenProcessorOverworked
                if (discardSimulationStepsWhenProcessorOverworked && steps > maxTimeStepsToAdvance) {
                    // Clamp the excess steps, so that simulation speed is limited if we run out of processing power, instead of bogging down the system
                    // This will hoverer lead to non-deterministic simulations (loading a state and stepping forward will not always result in the same situation)
                    steps = maxTimeStepsToAdvance;
                }

                excessMillisecondsFromLastStep = millisecondsToAdvance - steps * processingIntervalMilliseconds;

                // Process the steps
                for (int i = 0; i < steps; i++) {
                    time.advanceTimeMilliseconds(processingIntervalMilliseconds);
                    doProcess(time);
                    time.nextStep();
                }
            }
            else {
                // Variable timestep

                // Update own time with world time
                time.advanceTimeSeconds(worldTime.getLastStepDurationSeconds());

                // Do normal processing if enough time has passed since the last time
                if (time.getMillisecondsSinceLastStep() >= processingIntervalMilliseconds) {
                    doProcess(time);
                    time.nextStep();
                }
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
