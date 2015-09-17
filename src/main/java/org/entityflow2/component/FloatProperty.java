package org.entityflow2.component;

import org.entityflow2.range.FloatRange;
import org.entityflow2.type.FloatType;
import org.flowutils.Symbol;

/**
 * Primitive property accessor.
 */
public final class FloatProperty extends PropertyBase<Float> {

    /**
     * @param id                unique id of this property within the component type that it belongs to.
     * @param hostComponentType the componentType that this Property belongs to.
     * @param defaultValue      default value of this property in new components.
     * @param range             allowed value range for this property, or null if no restrictions.
     */
    public FloatProperty(Symbol id,
                         ComponentType hostComponentType,
                         float defaultValue,
                         FloatRange range) {
        super(id, hostComponentType, defaultValue, FloatType.TYPE, range);
    }

    /**
     * @return value of the property for the specified entity
     */
    public final float get(int entityId) {
        // Read value from buffer
        return FloatType.TYPE.readFloatValue(getDataBuffer(), getParameterOffset(entityId));
    }

    /**
     * Set the value of this property for the specified entity.
     * @param entityId id of the entity whose component property we want to change.
     * @param value new value for the component property.
     */
    public final void set(int entityId, float value) {
        // Clamp value if needed
        final FloatRange range = (FloatRange) getRange();
        if (range != null) {
            value = range.clampFloat(value);
        }

        // Write value to buffer
        FloatType.TYPE.writeFloatValue(getDataBuffer(), getParameterOffset(entityId), value);
    }

}
