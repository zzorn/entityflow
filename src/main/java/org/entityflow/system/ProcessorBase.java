package org.entityflow.system;


import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.flowutils.time.ManualTime;
import org.flowutils.time.Time;

/**
 * Base class for processor implementations, does not do any entity management.
 */
// TODO: Better timestep handling?
public abstract class ProcessorBase implements Processor {

    protected final Class<? extends Processor> baseType;
    protected double minProcessingIntervalSeconds = 0;
    protected final ManualTime time = new ManualTime();

    private World world = null;

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
     * @return an approximate minimum interval in seconds between each time that the system is processed.
     *                                  Zero if the system is processed every time process() is called.
     *                                  Does at most one processing call each time World.process() is called.
     */
    public final double getMinProcessingIntervalSeconds() {
        return minProcessingIntervalSeconds;
    }

    /**
     * @param minProcessingIntervalSeconds an approximate minimum interval in seconds between each time that the system is processed.
     *                                  Set to zero to process the system every time process() is called.
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
        this.world = world;
        time.reset();
        onInit();
    }

    /**
     * @return the world the system is added to, or null if not yet initialized.
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
     * Processes this entity system.
     *
     * @param systemTime a time with information on how long since this system was last processed.
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

    /**
     * Called when the system is initialized.
     */
    protected void onInit() {
    }

    @Override
    public void shutdown() {
    }
}
