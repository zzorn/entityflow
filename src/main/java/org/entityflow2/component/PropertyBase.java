package org.entityflow2.component;

import net.openhft.koloboke.collect.map.IntObjMap;
import net.openhft.koloboke.collect.map.hash.HashIntObjMaps;
import org.entityflow2.range.Range;
import org.entityflow2.type.Type;
import org.flowutils.Check;
import org.flowutils.Symbol;

import java.nio.ByteBuffer;

import static org.flowutils.Check.notNull;

/**
 * A property of a component type.
 */
public abstract class PropertyBase<T> {

    private final Symbol id;
    private final ComponentType componentType;
    private final T defaultValue;
    private final Type<T> type;
    private final Range<T> range;
    private int valueOffset;

    /**
     * If the type stored in this property is a complex type (non-fixed length or large value), this map will hold the values
     * of this property for each entity (key is entity id, value is the property value for that entity).
     * If the property type is a non-complex type, this is null and the property values are stored in byte buffers in the component types.
     */
    private final IntObjMap<T> complexValues;


    /**
     * @param id unique id of this property within the component type that it belongs to.
     * @param componentType the componentType that this Property belongs to.
     * @param defaultValue default value of this property in new components.
     * @param type type of values in this Property (and serializers for that type).
     * @param range allowed value range for this property, or null if no restrictions.
     */
    public PropertyBase(Symbol id,
                        ComponentType componentType,
                        T defaultValue,
                        Type<T> type,
                        Range<T> range) {
        this(id, componentType, defaultValue, type, range, 0);
    }

    /**
     * @param id unique id of this property within the component type that it belongs to.
     * @param componentType the componentType that this Property belongs to.
     * @param defaultValue default value of this property in new components.
     * @param type type of values in this Property (and serializers for that type).
     * @param range allowed value range for this property, or null if no restrictions.
     * @param valueOffset offset of the data of this property within a component data block,
     *                    or -1 if this Property is not stored in a byteBuffer.
     */
    public PropertyBase(Symbol id,
                        ComponentType componentType,
                        T defaultValue,
                        Type<T> type,
                        Range<T> range,
                        int valueOffset) {
        notNull(id, "id");
        notNull(componentType, "componentType");
        notNull(type, "type");
        Check.greaterOrEqual(valueOffset, "valueOffset", -1, "minus one");

        this.id = id;
        this.componentType = componentType;
        this.defaultValue = range == null ? defaultValue : range.clamp(defaultValue);
        this.type = type;
        this.range = range;
        this.valueOffset = valueOffset;

        // Create map to store complex values in if needed
        if (!type.isByteBufferStorable()) {
            complexValues = HashIntObjMaps.newMutableMap();
        }
        else {
            complexValues = null;
        }
    }

    /**
     * @return unique id of this property within the component type that it belongs to.
     */
    public final Symbol getId() {
        return id;
    }

    /**
     * @return the componentType that this Property belongs to.
     */
    public final ComponentType getComponentType() {
        return componentType;
    }

    /**
     * @return type of values in this Property (and serializers for that type).
     */
    public final Type<T> getType() {
        return type;
    }

    /**
     * @return allowed value range for this property.
     */
    public final Range<T> getRange() {
        return range;
    }

    /**
     * @return default value of this parameter in new components.
     */
    public final T getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return offset of the data of this property within a component data block,
     *         or -1 if this Property is not stored in a byteBuffer.
     */
    final int getValueOffset() {
        return valueOffset;
    }

    final void setValueOffset(int valueOffset) {
        Check.greaterOrEqual(valueOffset, "valueOffset", -1, "minus one");
        if (type.isByteBufferStorable() != (valueOffset >= 0)) {
            throw new IllegalArgumentException("If the type is byteBufferStorable, the valueOffset should be zero or larger, " +
                                               "if not, the valueOffset must be -1.  However, type.isByteBufferStorable is " +
                                               type.isByteBufferStorable() + " and valueOffset is " + valueOffset);
        }

        this.valueOffset = valueOffset;
    }

    /**
     * @return value of the property for the specified entity, or defaultValue if the property has not yet been set for that entity.
     */
    public final T getObject(int entityId) {
        return get(entityId, null);
    }

    /**
     * @param out if the type is mutable and out is not null, then out will be used to write the data to, and returned as a result.
     *            Used e.g. for vector objects and similar.
     * @return value of the property for the specified entity, or defaultValue if the property has not yet been set for that entity.
     */
    public final T get(int entityId, T out) {
        ensureEntityHasComponentType(entityId);

        if (complexValues != null) {
            // Get from object storage
            return complexValues.getOrDefault(entityId, defaultValue);
        }
        else {
            // Read from data buffer
            return type.readValue(getDataBuffer(), getParameterOffset(entityId), out);
        }
    }

    /**
     * Set the value of this property for the specified entity.
     * @param entityId id of the entity whose component property we want to change.
     * @param value new value for the component property.
     */
    public final void set(int entityId, T value) {
        ensureEntityHasComponentType(entityId);

        // Clamp value if needed
        if (range != null) {
            value = range.clamp(value);
        }

        if (complexValues != null) {
            // Set to object storage
            complexValues.put(entityId, value);
        }
        else {
            // Write to data buffer
            type.writeValue(getDataBuffer(), getParameterOffset(entityId), value);
        }
    }

    /**
     * @return data buffer where non-complex values are stored.
     */
    protected final ByteBuffer getDataBuffer() {
        return componentType.getDataBuffer();
    }

    /**
     * @return offset of this parameter for the specified entity in the data buffer.
     */
    protected final int getParameterOffset(int entityId) {
        return componentType.getEntityOffset(entityId) + valueOffset;
    }

    void removeFromEntity(int entityId) {
        if (!type.isByteBufferStorable()) {
            complexValues.remove(entityId);
        }
    }


    private void ensureEntityHasComponentType(int entityId) {
        if (!componentType.containedInEntity(entityId)) throw new IllegalArgumentException("The entity with the id " + entityId + " does not have the component type " + componentType.getId());
    }
}
