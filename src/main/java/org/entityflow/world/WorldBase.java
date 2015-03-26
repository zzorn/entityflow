package org.entityflow.world;

import org.flowutils.Check;
import org.flowutils.service.ServiceBase;
import org.flowutils.service.ServiceProvider;
import org.flowutils.time.Time;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.flowutils.Check.notNull;

/**
 * Contains general code for a world.
 * Can be used as base class for various implementations.
 */
public abstract class WorldBase extends ServiceBase implements World {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean invokeShutdownAfterStop = new AtomicBoolean(false);

    private long simulationStepMilliseconds;

    // Handles game time
    private final Time time;

    protected WorldBase(Time time) {
        notNull(time, "time");

        this.time = time;
    }

    @Override protected void doInit(ServiceProvider serviceProvider) {
        registerProcessors();

        initProcessors();

        refreshEntities();

        initWorld();

        refreshEntities();
    }

    public void setSimulationStepMilliseconds(long simulationStepMilliseconds) {
        Check.positive(simulationStepMilliseconds, "simulationStepMilliseconds");

        this.simulationStepMilliseconds = simulationStepMilliseconds;
    }

    public long getSimulationStepMilliseconds() {
        return simulationStepMilliseconds;
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public final void start(long simulationStepMilliseconds) {
        setSimulationStepMilliseconds(simulationStepMilliseconds);

        start();
    }

    @Override
    public final void start() {
        log.info("Starting.");

        running.set(true);

        // Initialize if needed
        if (!isInitialized()) init();

        // Main simulation loop
        // TODO: Better logic to timestepping
        while(running.get()) {
            time.nextStep();

            process();

            // Pause for remaining time
            time.delayMilliseconds(simulationStepMilliseconds - time.getMillisecondsSinceLastStep());
        }

        // Handle shutdown if the simulation loop was stopped by a call to shutdown
        if (invokeShutdownAfterStop.get()) {
            handleShutdown();
            invokeShutdownAfterStop.set(false);
        }
    }

    @Override public final void stop() {
        // Tell game loop to stop on the next round
        running.set(false);
    }

    @Override protected void doShutdown() {
        if (running.get()) {
            // Let game loop handle shutdown us after the next loop is ready
            invokeShutdownAfterStop.set(true);
            stop();
        }
        else {
            handleShutdown();
        }
    }

    private void handleShutdown() {
        onShutdown();
        shutdownProcessors();
    }


    @Override public final Time getTime() {
        return time;
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
     * Should execute any pending additions and removals of entities, and handle entity additions / removals from
     * systems when the components making up an entity change.
     */
    protected abstract void refreshEntities();

    /**
     * Should call init for all added systems.
     */
    protected abstract void initProcessors();

    /**
     * Should handle shutdown of the processors in the world.
     */
    protected abstract void shutdownProcessors();

}
