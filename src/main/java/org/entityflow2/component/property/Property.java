package org.entityflow2.component.property;

import net.openhft.koloboke.collect.map.hash.HashLongObjMap;
import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.entityflow2.component.ComponentType;
import org.entityflow2.range.Range;
import org.entityflow2.type.Type;
import org.flowutils.Check;
import org.flowutils.Symbol;

import static org.flowutils.Check.notNull;

/**
 * A property of a component type.
 */
public final class Property<T> {

    private final Symbol id;
    private final ComponentType hostComponentType;
    private final T defaultValue;
    private final Type<T> type;
    private final Range<T> range;
    private final int valueOffset;

    private final HashLongObjMap<T> complexValues;


    /**
     * @param id unique id of this property within the component type that it belongs to.
     * @param hostComponentType the componentType that this Property belongs to.
     * @param defaultValue default value of this property in new components.
     * @param type type of values in this Property (and serializers for that type).
     * @param range allowed value range for this property, or null if no restrictions.
     * @param valueOffset offset of the data of this property within a component data block,
     *                    or -1 if this Property is not stored in a byteBuffer.
     */
    public Property(Symbol id,
                    ComponentType hostComponentType,
                    T defaultValue,
                    Type<T> type,
                    Range<T> range,
                    int valueOffset) {
        notNull(id, "id");
        notNull(hostComponentType, "hostComponentType");
        notNull(type, "type");
        notNull(range, "range");
        Check.greaterOrEqual(valueOffset, "valueOffset", -1, "minus one");
        if (type.isByteBufferStorable() != (valueOffset >= 0)) {
            throw new IllegalArgumentException("If the type is byteBufferStorable, the valueOffset should be zero or larger, " +
                                               "if not, the valueOffset must be -1.  However, type.isByteBufferStorable is " +
                                               type.isByteBufferStorable() + " and valueOffset is " + valueOffset);
        }

        this.id = id;
        this.hostComponentType = hostComponentType;
        this.defaultValue = defaultValue;
        this.type = type;
        this.range = range;
        this.valueOffset = valueOffset;

        // Create map to store complex values in if needed
        if (!type.isByteBufferStorable()) {
            complexValues = HashLongObjMaps.newMutableMap();
        }
        else {
            complexValues = null;
        }
    }

    /**
     * @return unique id of this property within the component type that it belongs to.
     */
    public Symbol getId() {
        return id;
    }

    /**
     * @return the componentType that this Property belongs to.
     */
    public ComponentType getHostComponentType() {
        return hostComponentType;
    }

    /**
     * @return type of values in this Property (and serializers for that type).
     */
    public Type<T> getType() {
        return type;
    }

    /**
     * @return allowed value range for this property.
     */
    public Range<T> getRange() {
        return range;
    }

    /**
     * @return offset of the data of this property within a component data block,
     *         or -1 if this Property is not stored in a byteBuffer.
     */
    public int getValueOffset() {
        return valueOffset;
    }

    /**
     * @return value of the property for the specified entity, or defaultValue if the property has not yet been set for that entity.
     */
    T get(long entityId) {
        if (complexValues != null) {
            // Get from object storage
            return complexValues.getOrDefault(entityId, defaultValue);
        }
        else {
            // Read from byte buffer
            // IMPLEMENT: Get byte buffer and read it
        }
    }

    /**
     * Set the value of this property for the specified entity.
     * @param entityId id of the entity whose component property we want to change.
     * @param value new value for the component property.
     */
    void set(long entityId, T value) {
        // Clamp value if needed
        if (range != null) {
            value = range.clamp(value);
        }

        if (complexValues != null) {
            // Set to object storage
            complexValues.put(entityId, value);
        }
        else {
            // Write to byte buffer
            // IMPLEMENT: Get byte buffer and write to it
        }

    }

}
