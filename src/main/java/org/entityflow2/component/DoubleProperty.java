package org.entityflow2.component;

import org.entityflow2.range.DoubleRange;
import org.entityflow2.range.IntRange;
import org.entityflow2.type.DoubleType;
import org.entityflow2.type.IntType;
import org.flowutils.Symbol;

/**
 * Primitive property accessor.
 */
public final class DoubleProperty extends Property<Double> {

    /**
     * @param id                unique id of this property within the component type that it belongs to.
     * @param hostComponentType the componentType that this Property belongs to.
     * @param defaultValue      default value of this property in new components.
     * @param range             allowed value range for this property, or null if no restrictions.
     */
    public DoubleProperty(Symbol id,
                          ComponentType hostComponentType,
                          double defaultValue,
                          DoubleRange range) {
        super(id, hostComponentType, defaultValue, DoubleType.TYPE, range);
    }

    /**
     * @return value of the property for the specified entity
     */
    public final double get(int entityId) {
        // Read value from buffer
        return DoubleType.TYPE.readDoubleValue(getDataBuffer(), getParameterOffset(entityId));
    }

    /**
     * Set the value of this property for the specified entity.
     * @param entityId id of the entity whose component property we want to change.
     * @param value new value for the component property.
     */
    public final void set(int entityId, double value) {
        // Clamp value if needed
        final DoubleRange range = (DoubleRange) getRange();
        if (range != null) {
            value = range.clampDouble(value);
        }

        // Write value to buffer
        DoubleType.TYPE.writeDoubleValue(getDataBuffer(), getParameterOffset(entityId), value);
    }

}
