package org.entityflow.processors;

import org.apache.commons.collections4.set.CompositeSet;
import org.entityflow.component.Component;
import org.entityflow.entity.Entity;
import org.entityflow.utils.BalancingCompositeSetMutator;
import org.flowutils.service.ServiceProvider;
import org.flowutils.time.Time;

import java.util.*;
import java.util.concurrent.*;

import static org.flowutils.Check.notNull;

/**
 * Base implementation of an Processor, provides functionality that is common for most Processors that handle entities.
 */
public abstract class EntityProcessorBase extends ProcessorBase {

    private static final int INITIAL_CAPACITY = 100;

    // The types of components an entity should have for this processor to handle it.
    private final Set<Class<? extends Component>> handledComponentTypes = new HashSet<Class<? extends Component>>();

    // Updated by onEntityAdded, onEntityRemoved and onEntityChanged,
    // these are called by World during the common process phase, and do not need to be thread safe.
    private final CompositeSet<Entity> handledEntities = new CompositeSet<Entity>();
    private final Set<Entity> unmodifiableViewOfHandledEntities = Collections.unmodifiableSet(handledEntities);

    // Fields for handling concurrent processing:
    private boolean concurrentProcessing = false;
    private final int threadCount;
    private final List<Thread> processorThreads = new ArrayList<Thread>();
    private final CyclicBarrier processingStartBarrier;
    private final CyclicBarrier processingEndBarrier;
    private boolean stopProcessingThreads = false;

    /**
     * Creates a new single-threaded non-concurrent BaseProcessor, that is interested in entities with the specified types of components.
     * Only entities with all the specified component types are processed by default.
     *
     * @param handledComponentTypes entities with the component types listed here will be handled by this processors.
     */
    protected EntityProcessorBase(Class<? extends Component>... handledComponentTypes) {
        this(0, handledComponentTypes);
    }

    /**
     * Creates a new BaseProcessor, that is interested in entities with the specified types of components.
     * Only entities with all the specified component types are processed by default.
     *
     * @param processingIntervalSeconds number of seconds between each process pass of this processors, or zero to process as often as process() is called.
     * @param handledComponentTypes entities with the component types listed here will be handled by this processors.
     */
    protected EntityProcessorBase(double processingIntervalSeconds,
                                  Class<? extends Component>... handledComponentTypes) {
        this(processingIntervalSeconds, false, handledComponentTypes);
    }

    /**
     * Creates a new BaseProcessor, that is interested in entities with the specified types of components.
     * Only entities with all the specified component types are processed by default.
     *
     * @param processingIntervalSeconds number of seconds between each process pass of this processors, or zero to process as often as process() is called.
     * @param concurrentProcessing if true, entities may be processed in several threads concurrently, if false, entities are processed sequentially in the same thread.
     *                             If the processor does not modify any other entities or modify other shared data this can usually be set to true to gain some processing speed.
     *                             There is only a speed gain if the update logic for each entity is heavey,
     *                             or the number of entities is large.
     * @param handledComponentTypes entities with the component types listed here will be handled by this processors.
     */
    protected EntityProcessorBase(double processingIntervalSeconds,
                                  boolean concurrentProcessing,
                                  Class<? extends Component>... handledComponentTypes) {
        this(null, processingIntervalSeconds, concurrentProcessing, handledComponentTypes);
    }

    /**
     * Creates a new BaseProcessor, that is interested in entities with the specified types of components.
     * Only entities with all the specified component types are processed by default.
     *
     * @param baseType the base type for this entity processors, or the default one if null.
     * @param processingIntervalSeconds number of seconds between each process pass of this processors, or zero to process as often as process() is called.
     * @param concurrentProcessing if true, entities may be processed in several threads concurrently, if false, entities are processed sequentially in the same thread.
     *                             If the processor does not modify any other entities or modify other shared data this can usually be set to true to gain some processing speed.
     * @param handledComponentTypes entities with the component types listed here will be handled by this processors.
     */
    protected EntityProcessorBase(Class<? extends Processor> baseType,
                                  double processingIntervalSeconds,
                                  boolean concurrentProcessing,
                                  Class<? extends Component>... handledComponentTypes) {
        super(baseType, processingIntervalSeconds);

        // Store handled component types
        Collections.addAll(this.handledComponentTypes, handledComponentTypes);

        // Initialize entity collection
        handledEntities.setMutator(new BalancingCompositeSetMutator<Entity>());

        this.concurrentProcessing = concurrentProcessing;
        if (concurrentProcessing) {
            // Initialize concurrent processing
            threadCount = getThreadCount();
            processingStartBarrier = new CyclicBarrier(threadCount + 1);
            processingEndBarrier = new CyclicBarrier(threadCount + 1);
            createEntitySets();
        }
        else {
            // Single thread, just one set of entities
            threadCount = 1;
            processingStartBarrier =null;
            processingEndBarrier =null;
            handledEntities.addComposited(new LinkedHashSet<Entity>(INITIAL_CAPACITY));
        }
    }

    private void createEntitySets() {
        for (int i = 0; i < threadCount; i++) {
            // Entities processed by different threads are stored in different sets
            final LinkedHashSet<Entity> entitySet = new LinkedHashSet<Entity>(INITIAL_CAPACITY / threadCount);

            // Add to composite set so that we have an unified view of all entities
            handledEntities.addComposited(entitySet);
        }
    }


    @Override
    public final void onEntityAdded(Entity entity) {
        if (!handlesEntity(entity)) {
            addEntityIfWeShould(entity);
        }
    }

    @Override
    public final void onEntityRemoved(Entity entity) {
        if (handlesEntity(entity)) {
            removeEntity(entity);
        }
    }

    @Override
    public final void onEntityComponentsChanged(Entity entity) {
        if (handlesEntity(entity)) {
            removeEntityIfWeShould(entity);
        } else {
            addEntityIfWeShould(entity);
        }

    }

    /**
     * @return true if entities are processed concurrently by this entity processor.
     */
    public final boolean isConcurrentProcessing() {
        return concurrentProcessing;
    }

    protected void doProcess(Time systemTime) {
        preProcess(systemTime);

        if (concurrentProcessing) {
            processConcurrently();
        }
        else {
            processSequentially();
        }

        postProcess(systemTime);
    }

    protected final void processConcurrently() {
        // Notify processing threads to start processing
        if (!waitAtBarrierAndResetIt(processingStartBarrier)) return;

        // Wait until all entities have been processed
        waitAtBarrierAndResetIt(processingEndBarrier);
    }

    protected final void processSequentially() {
        // Just process all entities one at a time
        for (Entity handledEntity : handledEntities) {
            processEntity(time, handledEntity);
        }
    }

    /**
     * Called before entity processing begins.
     * @param time contains delta time and total simulation time.
     */
    protected void preProcess(Time time) {}

    /**
     * Called after entity processing ends.
     * @param time contains delta time and total simulation time.
     */
    protected void postProcess(Time time) {}

    /**
     * Called to process a specific entity
     * @param time contains delta time and total simulation time.
     * @param entity entity to process.
     */
    protected abstract void processEntity(Time time, Entity entity);

    /**
     * Called after an entity is added to this processors.
     */
    protected void handleAddedEntity(Entity entity) {}

    /**
     * Called before an entity is removed from this processors.
     */
    protected void handleRemovedEntity(Entity entity) {}


    /**
     * @return true if this processors should keep track of the specified entity and process it on each process call.
     */
    protected boolean shouldHandle(Entity entity) {
        return entity.hasAll(handledComponentTypes);
    }

    /**
     * @return read only set with the entities that are currently handled by this entity processor.
     */
    protected final Set<Entity> getHandledEntities() {
        return unmodifiableViewOfHandledEntities;
    }

    /**
     * @return read only set with the types of components that this entity processor handles.
     */
    protected final Set<Class<? extends Component>> getHandledComponentTypes() {
        return Collections.unmodifiableSet(handledComponentTypes);
    }

    /**
     * Number of threads to use for processing entities.
     * Defaults to the number of processors available.
     */
    protected int getThreadCount() {
        return Math.max(2, Runtime.getRuntime().availableProcessors());
    }

    @Override protected void doInit(ServiceProvider serviceProvider) {
        initializeProcessingThreads();

        onInit(serviceProvider);
    }

    @Override protected final void doShutdown() {
        stopProcessingThreads();

        onShutdown();
    }

    /**
     * Called when the processor is initialized.
     * @param serviceProvider can be queried for other services.  Not all services have necessarily been initialized yet.
     */
    protected void onInit(ServiceProvider serviceProvider) {
    }

    /**
     * Called when the processor is shut down.
     */
    protected void onShutdown() {
    }

    private void initializeProcessingThreads() {
        if (concurrentProcessing) {
            int id = 0;

            // Loop the sets of entities that should be handled by different threads
            for (Set<Entity> entitySet : handledEntities.getSets()) {

                // Create processor that will process one set of entities
                final ConcurrentEntityProcessor partialEntityProcessor = new ConcurrentEntityProcessor(entitySet);

                // Create processing thread
                final Thread processingThread = new Thread(partialEntityProcessor);
                processingThread.setDaemon(true);

                // Provide easy to understand name for the thread, for easier debugging
                id++;
                final String threadName = getName() + "_entity_processing_thread_" + id + "_of_" + handledEntities.getSets().size();
                processingThread.setName(threadName);

                // Store thread for future access
                processorThreads.add(processingThread);

                // Start processing thread
                processingThread.start();
            }
        }
    }

    private void stopProcessingThreads() {
        if (concurrentProcessing) {
            // Notify for threads to stop with a flag (maybe superfluous)
            stopProcessingThreads = true;

            // Notify for threads to stop by interrupting them (interruption should be enough)
            for (Thread processorThread : processorThreads) {
                processorThread.interrupt();
            }
        }
    }

    private void addEntityIfWeShould(Entity entity) {
        if (shouldHandle(entity)) {
            handledEntities.add(entity);
            handleAddedEntity(entity);
        }
    }

    private void removeEntityIfWeShould(Entity entity) {
        if (!shouldHandle(entity)) {
            removeEntity(entity);
        }
    }

    private void removeEntity(Entity entity) {
        handleRemovedEntity(entity);
        handledEntities.remove(entity);
    }

    /**
     * @return true if we are currently handling the specified entity.
     */
    public final boolean handlesEntity(Entity entity) {
        return handledEntities.contains(entity);
    }

    /**
     * Used for processing a range of entities from the list of handled entities when processing entities in multiple threads.
     */
    private final class ConcurrentEntityProcessor implements Runnable {
        private final Set<Entity> entitiesToProcess;

        private ConcurrentEntityProcessor(Set<Entity> entitiesToProcess) {
            notNull(entitiesToProcess, "entitiesToProcess");
            this.entitiesToProcess = entitiesToProcess;
        }

        @Override public void run() {
            while (!stopProcessingThreads) {
                // Wait until everyone (most importantly the main thread) is ready to process
                if (!waitAtBarrier(processingStartBarrier)) return;

                // Process the specified set of entities
                for (Entity entity : entitiesToProcess) {
                    processEntity(time, entity);
                }

                // Signal that we are ready (and wait until everyone is ready)
                if (!waitAtBarrier(processingEndBarrier)) return;
            }
        }
    }


    /**
     * @return true if we should continue
     */
    private boolean waitAtBarrierAndResetIt(final CyclicBarrier barrier) {
        if (!waitAtBarrier(barrier)) return false;

        barrier.reset();

        return true;
    }

    /**
     * @return true if we should continue
     */
    private boolean waitAtBarrier(final CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException e) {
            return false;
        } catch (BrokenBarrierException e) {
            return false;
            //throw new IllegalStateException("Problem when processing entities concurrently in " + getServiceName() + ": " + e.getMessage(), e);
        }

        return true;
    }

    /**
     * @return sets that the handled components are stored (there will be more than one if concurrency is activated).
     *         Mainly provided for testing purposes.
     */
    protected List<Set<Entity>> getHandledComponentSets() {
        return handledEntities.getSets();
    }
}
