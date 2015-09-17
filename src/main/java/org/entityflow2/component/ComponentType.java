package org.entityflow2.component;

import net.openhft.koloboke.collect.Equivalence;
import net.openhft.koloboke.collect.map.IntIntMap;
import net.openhft.koloboke.collect.map.ObjObjMap;
import net.openhft.koloboke.collect.map.hash.*;
import net.openhft.koloboke.collect.set.IntSet;
import org.entityflow2.EntityManager;
import org.entityflow2.range.DoubleRange;
import org.entityflow2.range.FloatRange;
import org.entityflow2.range.IntRange;
import org.entityflow2.range.Range;
import org.entityflow2.type.Type;
import org.flowutils.Check;
import org.flowutils.Symbol;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.flowutils.Check.notNull;

/**
 * Describes some type of component that can be added to an entity.
 * Actual Component classes are not used, instead the component parameters are accessed through a ComponentType.
 */
public class ComponentType {

    private final static int BLOCK_HEADER_SIZE = Integer.SIZE / 8;

    private EntityManager entityManager;

    private final Symbol id;
    private boolean inUse = false;

    private PropertyBase[] properties = new PropertyBase[0];
    private final ObjObjMap<Symbol, PropertyBase> propertiesLookup = HashObjObjMaps.getDefaultFactory()
                                                                               .withKeyEquivalence(Equivalence.identity())
                                                                               .withNullKeyAllowed(false)
                                                                               .newMutableMap();

    private int expectedNumberOfComponents = 1000;
    private ByteBuffer dataBuffer = null;
    private int dataBlockSize = 0;
    private int componentCount = 0;
    private int maxComponentIndex = -1;
    private int componentCapacity = 0;
    private double growthFactor = 2.0;
    private double expansionThreshold = 0.8;
    private double compactingThreshold = 0.3;

    private final Object dataBufferWriteLock = new Object();

    private final IntIntMap entityIdToComponentIndex = HashIntIntMaps.newMutableMap();

    protected ComponentType() {
        this(null);
    }

    /**
     * @param id unique id for this type of component, or null to use the simple class name (only allowed for derivative classes).
     */
    public ComponentType(Symbol id) {
        // Use simple class name as id if no id was specified
        if (id == null) {
            if (getClass().equals(ComponentType.class)) throw new IllegalArgumentException("id must be provided for non-derivative ComponentTypes");

            id = Symbol.get(getClass().getSimpleName());
        }

        this.id = id;
    }

    public final Symbol getId() {
        return id;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        notNull(entityManager, "entityManager");
        if (this.entityManager  != null) throw new IllegalStateException("Can not set the entity manager twice");

        this.entityManager = entityManager;
    }

    /**
     * @return expected number of components of this type.  Affects how much storage space is initially allocated for component values.
     */
    public final int getExpectedNumberOfComponents() {
        return expectedNumberOfComponents;
    }

    /**
     * @param expectedNumberOfComponents expected number of components of this type.  Affects how much storage space is initially allocated for component values.
     */
    public final void setExpectedNumberOfComponents(int expectedNumberOfComponents) {
        Check.positive(expectedNumberOfComponents, "expectedNumberOfComponents");
        this.expectedNumberOfComponents = expectedNumberOfComponents;
    }

    /**
     * @return number of components of this type that exists.
     */
    public final int getComponentCount() {
        return componentCount;
    }

    /**
     * @return maximum component index that contains a component.
     */
    public final int getMaxComponentIndex() {
        return maxComponentIndex;
    }

    /**
     * Adds an integer property with unlimited range to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @return the created property object.
     */
    public final IntProperty addProperty(String id, int defaultValue) {
        return addProperty(id, defaultValue, (IntRange) null);
    }

    /**
     * Adds a float property with unlimited range to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @return the created property object.
     */
    public final FloatProperty addProperty(String id, float defaultValue) {
        return addProperty(id, defaultValue, (FloatRange) null);
    }

    /**
     * Adds an double property with unlimited range to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @return the created property object.
     */
    public final DoubleProperty addProperty(String id, double defaultValue) {
        return addProperty(id, defaultValue, (DoubleRange) null);
    }

    /**
     * Adds an integer property to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @param range allowed range for the property.
     * @return the created property object.
     */
    public final IntProperty addProperty(String id, int defaultValue, IntRange range) {
        return addProperty(new IntProperty(Symbol.get(id), this, defaultValue, range));
    }

    /**
     * Adds a float property to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @param range allowed range for the property.
     * @return the created property object.
     */
    public final FloatProperty addProperty(String id, float defaultValue, FloatRange range) {
        return addProperty(new FloatProperty(Symbol.get(id), this, defaultValue, range));
    }

    /**
     * Adds a double property to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @param range allowed range for the property.
     * @return the created property object.
     */
    public final DoubleProperty addProperty(String id, double defaultValue, DoubleRange range) {
        return addProperty(new DoubleProperty(Symbol.get(id), this, defaultValue, range));
    }

    /**
     * Adds a property to this component type with no restrictions on the value range.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @param type type of the property.
     * @return the created property object.
     */
    public final <T> Property<T> addProperty(String id, T defaultValue, Type<T> type) {
        return addProperty(id, defaultValue, type, null);
    }

    /**
     * Adds a property to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Must be called before this ComponentType is applied to any entity.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @param type type of the property.
     * @param range allowed range for the property.
     * @return the created property object.
     */
    public final <T> Property<T> addProperty(String id, T defaultValue, Type<T> type, Range<T> range) {
        return addProperty(new Property<T>(Symbol.get(id), this, defaultValue, type, range));
    }

    private <T extends PropertyBase> T addProperty(T property) {

        // Check if we can still add properties
        if (inUse) throw new IllegalStateException("Can not add a property anymore after a component of this type has been added to an entity");

        // Ensure id is not reused.
        if (propertiesLookup.containsKey(property.getId())) throw new IllegalArgumentException("There already exists a property with the id '"+property.getId()+"' in this component");

        // Reserve space from the data block if we should store the value there.
        // Also determine the offset of the property in the data block.
        final int offset;
        if (property.getType().isByteBufferStorable()) {
            // Store in a buffer
            offset = dataBlockSize;
            dataBlockSize += property.getType().getDataLengthBytes();
        }
        else {
            // Store in a map
            offset = -1;
        }

        // Set offset
        property.setValueOffset(offset);

        // Store property
        properties = Arrays.copyOf(properties, properties.length + 1);
        properties[properties.length - 1] = property;
        propertiesLookup.put(property.getId(), property);

        return property;
    }

    /**
     * @return a set with the ids of the entities that have this component.
     *         The returned set should not be modified!
     */
    public final IntSet getEntities() {
        return entityIdToComponentIndex.keySet();
    }

    /**
     * @return true if the specified entity contains a component of this type.
     */
    public final boolean containedInEntity(int entityId) {
        return entityIdToComponentIndex.containsKey(entityId);
    }

    /**
     * @return data buffer with stored data values of entities of this component type.
     */
    protected final ByteBuffer getDataBuffer() {
        return dataBuffer;
    }

    /**
     * @return base offset for the specified entity in the data buffer for this component type.
     */
    protected final int getEntityOffset(int entity) {
        if (dataBlockSize <= 0) throw new UnsupportedOperationException("This component has no parameters stored in the data buffer.");

        final int componentIndex = entityIdToComponentIndex.getOrDefault(entity, -2);
        if (componentIndex < 0) throw new IllegalArgumentException("The specified entity "+entity+" does not have the component "+getId()+".");

        return componentIndex * (dataBlockSize + BLOCK_HEADER_SIZE) + BLOCK_HEADER_SIZE;
    }

    /**
     * @param componentIndex component index to get entity id for.  Ranges from 0 to getMaxComponentIndex() (inclusive).
     * @return the id of the entity at the specified component index, 0 if there is currently no entity at the specified index
     *         or -1 if the component index is out of range.
     */
    public final int getEntityAtComponentIndex(int componentIndex) {
        if (componentIndex < 0 ||
            componentIndex > maxComponentIndex) {
            return -1;
        }
        else {
            return getEntityIdAtComponentIndex(componentIndex);
        }
    }

    /**
     * Adds a new component of this type to the specified entity.
     *
     * Note that this method is not thread safe, it should not be called while properties of this component may be read or written,
     * or this component added or removed to entities, in other threads.
     *
     * @param entityId id of the entity to add the component to.
     */
    public final void addToEntity(int entityId) {
        inUse = true;

        // Get buffer write lock
        synchronized (dataBufferWriteLock) {
            if (entityIdToComponentIndex.containsKey(entityId)) {
                throw new IllegalArgumentException("The entity " + entityId + " already has a '"+getId()+"' component, can not add another.");
            }

            // Reserve the data buffer if needed
            if (dataBuffer == null) {
                // Not yet initialized
                componentCapacity = expectedNumberOfComponents;
                dataBuffer = ByteBuffer.allocateDirect(componentCapacity * (BLOCK_HEADER_SIZE + dataBlockSize));
            }
            else if (componentCount >= componentCapacity * expansionThreshold) {
                // Buffer filled, create new larger buffer
                final int newComponentCapacity = (int) (componentCapacity * growthFactor);
                reallocateDataBuffer(newComponentCapacity);
            }

            // Add component:

            // Find location where this entityId should be added
            int componentIndexForNewEntity = findLocationForNewEntityComponent(entityId);

            // Prefix entity id to the component data block
            dataBuffer.putInt(componentIndexForNewEntity * (dataBlockSize + BLOCK_HEADER_SIZE), entityId);

            // Store mapping
            entityIdToComponentIndex.put(entityId, componentIndexForNewEntity);

            // Initialize to default values
            for (PropertyBase property : properties) {
                if (property.getType().isByteBufferStorable()) {
                    property.set(entityId, property.getDefaultValue());
                }
            }

            componentCount++;
        }

        // Notify entity manager about the component addition
        entityManager.onComponentAdded(entityId, this);
    }

    /**
     * Removes the component of this type from the specified entity.
     *
     * Note that this method is not thread safe, it should not be called while properties of this component may be read or written,
     * or this component added or removed to entities, in other threads.
     *
     * @param entityId id of the entity to remove the component from.
     */
    public final void removeFromEntity(int entityId) {
        // Get buffer write lock
        boolean containedComponent;
        synchronized (dataBufferWriteLock) {
            containedComponent = entityIdToComponentIndex.containsKey(entityId);
            if (containedComponent) {
                // Remove any value mappings for complex types
                for (PropertyBase property : properties) {
                    property.removeFromEntity(entityId);
                }

                // Mark the data buffer entry as free
                final int componentIndex = entityIdToComponentIndex.get(entityId);
                dataBuffer.putInt(componentIndex * (dataBlockSize + BLOCK_HEADER_SIZE), 0);

                // Update maxComponentIndex
                if (componentIndex >= 0 && componentIndex >= maxComponentIndex) {
                    maxComponentIndex = componentIndex - 1;
                    while (maxComponentIndex >= 0 && isFreeComponentIndex(maxComponentIndex)) {
                        maxComponentIndex--;
                    }
                }

                // Remove mapping for the entity, marking that this component is not present in the entity
                entityIdToComponentIndex.remove(entityId);

                // Update number of components
                componentCount--;

                // Reduce size of data buffer if we drop below some fill fraction of it
                compactDataBufferIfNecessary();
            }
        }

        if (containedComponent) {
            // Notify entity manager about the component removal
            entityManager.onComponentRemoved(entityId, this);
        }
    }

    private int findLocationForNewEntityComponent(int entityId) {
        // Find correct place for the entity, keeping the entities sorted by id
        int componentIndexForNewEntity = findComponentIndexForNewEntity(entityId);

        // Ensure we have space to add the component for the entity
        if (componentIndexForNewEntity >= componentCapacity) {
            // Compact by moving components back from the end of the buffer towards the first free space
            componentIndexForNewEntity = makeSpaceForComponentAt(componentCapacity - 1);
        }
        else if (getEntityIdAtComponentIndex(componentIndexForNewEntity) != 0) {
            // Compact from the specified index in the shortest direction to a space
            componentIndexForNewEntity = makeSpaceForComponentAt(componentIndexForNewEntity);
        }

        // Sanity check
        if (getEntityIdAtComponentIndex(componentIndexForNewEntity) != 0) throw new IllegalStateException("Space should have been cleared for the component");

        // Update max component index
        maxComponentIndex = Math.max(maxComponentIndex, componentIndexForNewEntity);

        // Return found empty spot
        return componentIndexForNewEntity;
    }

    private int findComponentIndexForNewEntity(int entityId) {
        // NOTE: This is a linear search, if the data is tightly packed, a binary search can be more efficient.
        // We start from the end, as typically components are added to entities in order of increasing entity id.
        for (int componentIndex = maxComponentIndex; componentIndex >= 0; componentIndex--) {
            final int entityIdAtComponentIndex = dataBuffer.getInt(componentIndex * (BLOCK_HEADER_SIZE + dataBlockSize));

            if (entityIdAtComponentIndex > 0 && entityIdAtComponentIndex <= entityId) return componentIndex;
        }

        return 0;
    }

    private int makeSpaceForComponentAt(int componentIndex) {

        // Moves indexes simultaneously back and forward from the ideal insertion spot, trying to find free component blocks

        int backIndex = componentIndex;
        int forwardIndex = componentIndex + 1;

        while (backIndex >= 0 || forwardIndex < componentCapacity) {
            if (backIndex >= 0) {
                final int entityIdAtBackIndex = getEntityIdAtComponentIndex(backIndex);
                if (entityIdAtBackIndex == 0) {
                    // Move components back
                    for (int i = backIndex; i < componentIndex; i++) {
                        moveComponent(i + 1, i);
                    }

                    // Space is now cleared for the component
                    return componentIndex;
                }
            }

            if (forwardIndex < componentCapacity) {
                final int entityIdAtForwardIndex = getEntityIdAtComponentIndex(forwardIndex);
                if (entityIdAtForwardIndex == 0) {
                    // Move components forward
                    for (int i = forwardIndex; i > componentIndex + 1; i--) {
                        moveComponent(i - 1, i);
                    }

                    // Space is now cleared for the component
                    return componentIndex + 1;
                }
            }

            backIndex--;
            forwardIndex++;
        }

        // If we got here there is no more space available
        throw new IllegalStateException("Could not make space for component");
    }

    private void compactDataBufferIfNecessary() {
        // Compact and shrink the data buffer if the buffer has a component count of less than some constant of the max buffer size
        int compactedSize = (int) (componentCapacity / growthFactor);
        if (compactedSize > expectedNumberOfComponents &&
            componentCount < compactingThreshold * componentCapacity &&
            componentCount < compactedSize) {
            // Compact the data buffer
            if (maxComponentIndex >= compactedSize) {
                int targetIndex = 0;
                while (true) {
                    // Find next free index to copy to
                    while (!isFreeComponentIndex(targetIndex) &&
                           targetIndex < compactedSize &&
                           targetIndex < maxComponentIndex) targetIndex++;
                    int sourceIndex = targetIndex + 1;

                    // Find next source index to copy from
                    while (isFreeComponentIndex(sourceIndex) && sourceIndex <= maxComponentIndex) sourceIndex++;
                    if (sourceIndex > maxComponentIndex) break; // Nothing more to move

                    // Move component
                    moveComponent(sourceIndex, targetIndex);
                }

                // Update maxComponentIndex
                maxComponentIndex = compactedSize - 1;
                while (maxComponentIndex >= 0 && isFreeComponentIndex(maxComponentIndex)) maxComponentIndex--;
            }

            // Reallocate smaller buffer
            reallocateDataBuffer(compactedSize);
        }
    }

    private void moveComponent(int sourceComponentIndex, int targetComponentIndex) {
        final int blockSize = dataBlockSize + BLOCK_HEADER_SIZE;

        final int movedEntityId = getEntityIdAtComponentIndex(sourceComponentIndex);
        for (int dataIndex = 0; dataIndex < blockSize; dataIndex++) {
            dataBuffer.put(targetComponentIndex * blockSize + dataIndex, dataBuffer.get(sourceComponentIndex * blockSize + dataIndex));
        }
        if (movedEntityId != 0) {
            entityIdToComponentIndex.put(movedEntityId, targetComponentIndex);
        }
    }

    private boolean isFreeComponentIndex(final int componentIndex) {
        return getEntityIdAtComponentIndex(componentIndex) == 0;
    }

    private int getEntityIdAtComponentIndex(int index) {
        return dataBuffer.getInt(index * (BLOCK_HEADER_SIZE + dataBlockSize));
    }


    private void reallocateDataBuffer(int newComponentCapacity) {
        if (newComponentCapacity < maxComponentIndex) throw new IllegalArgumentException("Existing components will not fit");

        // Allocate new buffer
        final ByteBuffer newDataBuffer = ByteBuffer.allocateDirect(newComponentCapacity * (BLOCK_HEADER_SIZE + dataBlockSize));

        // Copy over existing values
        dataBuffer.clear();
        newDataBuffer.put(dataBuffer);
        newDataBuffer.clear();
        dataBuffer = newDataBuffer;
        componentCapacity = newComponentCapacity;
    }


}
