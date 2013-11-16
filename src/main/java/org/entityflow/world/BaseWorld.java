package org.entityflow.world;

import org.entityflow.util.Ticker;
import org.flowutils.Check;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contains general code for a world.
 * Can be used as base class for various implementations.
 */
public abstract class BaseWorld implements World {

    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    protected final AtomicBoolean running = new AtomicBoolean(false);
    protected long simulationStepMilliseconds;

    @Override
    public final void init() {
        // TODO: Add logging support
        System.out.println("Initializing.");

        if (initialized.get()) throw new IllegalStateException("World was already initialized, can not initialize again");

        registerProcessors();

        initProcessors();

        initialized.set(true);

        refreshEntities();

        initWorld();

        refreshEntities();
    }

    public void setSimulationStepMilliseconds(long simulationStepMilliseconds) {
        Check.positive(simulationStepMilliseconds, "simulationStepMilliseconds");

        this.simulationStepMilliseconds = simulationStepMilliseconds;
    }

    @Override
    public final void start(long simulationStepMilliseconds) {
        setSimulationStepMilliseconds(simulationStepMilliseconds);

        start();
    }

    @Override
    public final void start() {
        running.set(true);

        // Initialize if needed
        if (!initialized.get()) init();

        // Main simulation loop
        // TODO: Better logic to timestepping?
        Ticker ticker = new Ticker();
        while(running.get()) {
            ticker.tick();

            process(ticker);

            try {
                Thread.sleep(simulationStepMilliseconds);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        // Do shutdown
        doShutdown();
    }

    @Override
    public final void shutdown() {
        if (running.get()) {
            // Let game loop call doShutdown after the next loop is ready
            running.set(false);
        }
        else {
            doShutdown();
        }
    }

    /**
     * Can be used to add systems.  Called automatically by init or start before initializing the systems.
     */
    protected void registerProcessors() {
    }

    /**
     * Can be used to initialize the world.  Called automatically by the init or start after systems have been initialized.
     */
    protected void initWorld() {
    }

    /**
     * Can be used to do any additional things before shutdown.  Called by shutdown before systems are shut down.
     */
    protected void onShutdown() {
    }

    /**
     * Should call init for all added systems.
     */
    protected abstract void initProcessors();

    /**
     * Should execute any pending additions and removals of entities, and handle entity additions / removals from
     * systems when the components making up an entity change.
     */
    protected abstract void refreshEntities();

    /**
     * Should call onShutdown, and then shutdown for all systems.
     */
    protected abstract void doShutdown();

}
