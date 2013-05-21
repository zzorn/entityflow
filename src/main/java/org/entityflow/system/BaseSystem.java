package org.entityflow.system;


import org.entityflow.entity.Entity;
import org.entityflow.util.Ticker;
import org.entityflow.world.World;

/**
 * Base class for system implementations, does not do any entity management.
 */
public abstract class BaseSystem implements EntitySystem {

    protected final Class<? extends EntitySystem> baseType;
    protected double processingIntervalSeconds = 0;
    protected final Ticker ticker = new Ticker();

    private World world = null;

    protected BaseSystem() {
        this(null);
    }

    protected BaseSystem(Class<? extends EntitySystem> baseType) {
        this(baseType, 0);
    }

    protected BaseSystem(Class<? extends EntitySystem> baseType, double processingIntervalSeconds) {
        if (baseType == null) this.baseType = getClass();
        else this.baseType = baseType;

        setProcessingIntervalSeconds(processingIntervalSeconds);
    }

    /**
     * @return an approximate interval in seconds between each time that the system is processed.
     *                                  Zero if the system is processed every time process() is called.
     */
    public final double getProcessingIntervalSeconds() {
        return processingIntervalSeconds;
    }

    /**
     * @param processingIntervalSeconds an approximate interval in seconds between each time that the system is processed.
     *                                  Set to zero to process the system every time process() is called.
     */
    public final void setProcessingIntervalSeconds(double processingIntervalSeconds) {
        this.processingIntervalSeconds = processingIntervalSeconds;
    }

    @Override
    public Class<? extends EntitySystem> getBaseType() {
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
        if (ticker.getSecondsSinceLastTick() >= processingIntervalSeconds) {
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
