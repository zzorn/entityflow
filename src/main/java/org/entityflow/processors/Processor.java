package org.entityflow.processors;

import org.entityflow.entity.Entity;
import org.entityflow.world.World;
import org.flowutils.time.Time;

/**
 * A processors that is specialized at simulating some aspect of the World.  Processes entities that contain some
 * set of Components that the Processor is interested in.
 */
public interface Processor {

    /**
     * The base type used when retrieving processors.
     * Typically a processor specific interface that a processor implementation implements.
     * There can only be one processor with each base type registered in a World.
     */
    Class<? extends Processor> getBaseType();

    /**
     * Called when the application starts up.
     */
    void init(World world);

    /**
     * Called when the processors is shut down, e.g. because the application is closing.
     * Can free any resources, flush disks, etc.
     */
    void shutdown();

    /**
     * Processes all entities registered with this processors.
     * Should only be called by World.
     *
     * @param time contains information on time passed since the last call to process.
     */
    void process(Time time);

    /**
     * Called when an entity is added to the world.
     * Should only be called by World.
     */
    void onEntityAdded(Entity entity);

    /**
     * Called when an entity is removed from the world.
     * Should only be called by World.
     */
    void onEntityRemoved(Entity entity);

    /**
     * Called when the components in an entity change.
     * Should only be called by World.
     */
    void onEntityComponentsChanged(Entity entity);
}
