package org.entityflow.system;


import org.entityflow.entity.Entity;
import org.entityflow.util.Ticker;
import org.entityflow.world.World;

/**
 * Base class for processor implementations, does not do any entity management.
 */
// TODO: Better timestep handling?
public abstract class BaseProcessor implements Processor {

    protected final Class<? extends Processor> baseType;
    protected double minProcessingIntervalSeconds = 0;
    protected final Ticker ticker = new Ticker();

    private World world = null;

    protected BaseProcessor() {
        this(null);
    }

    protected BaseProcessor(Class<? extends Processor> baseType) {
        this(baseType, 0);
    }

    protected BaseProcessor(Class<? extends Processor> baseType, double minProcessingIntervalSeconds) {
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
        ticker.reset();
        onInit();
    }

    /**
     * @return the world the system is added to, or null if not yet initialized.
     */
    public final World getWorld() {
        return world;
    }

    @Override
    public final void process() {
        // Do normal processing
        if (ticker.getSecondsSinceLastTick() >= minProcessingIntervalSeconds) {
            doProcess(ticker);
            ticker.tick();
        }
    }

    /**
     * Processes this entity system.
     *
     * @param systemTicker a ticker with information on how long since this system was last processed.
     */
    protected void doProcess(Ticker systemTicker) {
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
