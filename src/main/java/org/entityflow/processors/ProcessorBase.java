package org.entityflow.processors;


import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.flowutils.LogUtils;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;
import org.slf4j.Logger;

/**
 * Base class for processor implementations, does not do any entity management.
 */
// TODO: Better timestep handling?
public abstract class ProcessorBase implements Processor {

    protected final Class<? extends Processor> baseType;
    protected double minProcessingIntervalSeconds = 0;
    protected final ManualTime time = new ManualTime();

    private World world = null;

    protected final Logger logger = LogUtils.getLogger();

    protected ProcessorBase() {
        this(null);
    }

    protected ProcessorBase(Class<? extends Processor> baseType) {
        this(baseType, 0);
    }

    protected ProcessorBase(Class<? extends Processor> baseType, double minProcessingIntervalSeconds) {
        if (baseType == null) this.baseType = getClass();
        else this.baseType = baseType;

        setMinProcessingIntervalSeconds(minProcessingIntervalSeconds);
    }

    /**
     * @return an approximate minimum interval in seconds between each time that the processors is processed.
     *                                  Zero if the processors is processed every time process() is called.
     *                                  Does at most one processing call each time World.process() is called.
     */
    public final double getMinProcessingIntervalSeconds() {
        return minProcessingIntervalSeconds;
    }

    /**
     * @param minProcessingIntervalSeconds an approximate minimum interval in seconds between each time that the processors is processed.
     *                                  Set to zero to process the processors every time process() is called.
     *                                  Does at most one processing call each time World.process() is called.
     */
    public final void setMinProcessingIntervalSeconds(double minProcessingIntervalSeconds) {
        this.minProcessingIntervalSeconds = minProcessingIntervalSeconds;
    }

    @Override
    public Class<? extends Processor> getBaseType() {
        return baseType;
    }

    @Override
    public final void init(World world) {
        logger.info("Initializing " + getName());
        this.world = world;
        time.reset();
        onInit();
    }

    /**
     * @return User readable name of the processor, as used in logging etc.
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * @return the world the processors is added to, or null if not yet initialized.
     */
    public final World getWorld() {
        return world;
    }

    @Override
    public final void process(Time worldTime) {
        // Update own time with world time
        time.advanceTime(worldTime.getLastStepDurationMs());

        // Do normal processing if enough time has passed since the last time
        if (time.getSecondsSinceLastStep() >= minProcessingIntervalSeconds) {
            doProcess(time);
            time.nextStep();
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

    @Override
    public final void shutdown() {
        logger.info("Shutting down " + getName());
        onShutdown();
    }

    /**
     * Called when the processor is initialized.
     */
    protected void onInit() {
    }

    /**
     * Called when the processor is shutting down.
     */
    protected void onShutdown() {
    }
}
