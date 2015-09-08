package org.entityflow2.component;

import net.openhft.koloboke.collect.Equivalence;
import net.openhft.koloboke.collect.map.IntIntMap;
import net.openhft.koloboke.collect.map.LongIntMap;
import net.openhft.koloboke.collect.map.ObjObjMap;
import net.openhft.koloboke.collect.map.hash.*;
import net.openhft.koloboke.collect.set.IntSet;
import net.openhft.koloboke.collect.set.LongSet;
import net.openhft.koloboke.collect.set.hash.HashLongSet;
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
import java.util.Set;

import static org.flowutils.Check.notNull;

/**
 * Describes some type of component that can be added to an entity.
 * Actual Component classes are not used, instead the component parameters are accessed through a ComponentType.
 */
public class ComponentType {

    private EntityManager entityManager;

    private final Symbol id;
    private boolean inUse = false;

    private Property[] properties = new Property[0];
    private final ObjObjMap<Symbol, Property> propertiesLookup = HashObjObjMaps.getDefaultFactory()
                                                                               .withKeyEquivalence(Equivalence.identity())
                                                                               .withNullKeyAllowed(false)
                                                                               .newMutableMap();

    private int expectedNumberOfComponents = 1000;
    private ByteBuffer dataBuffer = null;
    private int dataBlockSize = 0;
    private int componentCount = 0;
    private int componentCapacity = 0;
    private double growthFactor = 2.0;
    private double compactingThreshold = 0.5;

    // Keeps component offsets for components that are stored in the data buffer, still contains an entry for other components as well,
    // so that we know if an entity has this component added.
    private final IntIntMap entityToComponentOffset = HashIntIntMaps.newMutableMap();


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
     * Adds an integer property with unlimited range to this component type.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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
     * Note that this method is not thread safe (but getters and setters for property values generally are).
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

    private final <T extends Property> T addProperty(T property) {

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
        return entityToComponentOffset.keySet();
    }

    /**
     * @return true if the specified entity contains a component of this type.
     */
    public final boolean containedInEntity(int entityId) {
        return entityToComponentOffset.containsKey(entityId);
    }


    /**
     * Adds a new component of this type to the specified entity.
     * Note that this method is not thread safe, it should not be called while getBuffer or add/remove component calls may be called
     * from other threads.
     * @param entityId id of the entity to add the component to.
     */
    public void addToEntity(int entityId) {
        // TODO: Get buffer write lock

        inUse = true;

        if (entityToComponentOffset.containsKey(entityId)) {
            throw new IllegalArgumentException("The entity " + entityId + " already has a '"+getId()+"' component, can not add another.");
        }

        // Ensure we have enough space in the data buffer, if we need one
        if (dataBlockSize <= 0) {
            entityToComponentOffset.put(entityId, -1);
        } else {
            // Reserve the data buffer if needed
            if (dataBuffer == null) {
                // Not yet initialized
                componentCapacity = expectedNumberOfComponents;
                dataBuffer = ByteBuffer.allocateDirect(componentCapacity * dataBlockSize);
            }
            else if (componentCount >= componentCapacity * compactingThreshold) {
                // Buffer, filled, create new larger buffer
                final int newComponentCapacity = (int) (componentCapacity * growthFactor);
                final ByteBuffer newDataBuffer = ByteBuffer.allocateDirect(newComponentCapacity * dataBlockSize);

                // Copy over existing values
                dataBuffer.clear();
                newDataBuffer.put(dataBuffer);
                newDataBuffer.clear();
                dataBuffer = newDataBuffer;
                componentCapacity = newComponentCapacity;
            }

            // Add component
            // Find location where this entityId should be added
            // TODO
            // Find closest empty slot next to the place for the entity id
            // TODO

            final int componentOffset = correctComponentPlace * dataBlockSize;

            // Prefix entity id to the component data block
            // TODO

            entityToComponentOffset.put(entityId, componentOffset);

            // Initialize to default values
            for (Property property : properties) {
                if (property.getType().isByteBufferStorable()) {
                    property.set(entityId, property.getDefaultValue());
                }
            }
        }

        // Notify entity manager about the component addition
        entityManager.onComponentAdded(entityId, this);

        componentCount++;
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

        final int offset = entityToComponentOffset.getOrDefault(entity, -2);
        if (offset < 0) throw new IllegalArgumentException("The specified entity "+entity+" does not have the component "+getId()+".");

        return offset;
    }

    public void removeFromEntity(int entityId) {
        // TODO: Get buffer write lock
        if (entityToComponentOffset.containsKey(entityId)) {
            // Remove any value mappings for complex types
            for (Property property : properties) {
                property.removeFromEntity(entityId);
            }

            // Mark the data buffer entry as free
            // TODO

            // TODO: compact the data buffers if the buffer where the entity was removed from and the next buffer have a combined fill size of less than half the max buffer size.

            entityToComponentOffset.remove(entityId);
        }
    }

    // TODO: Add primitive accessor methods to read the buffer, and get a read lock to the buffer in each






}
