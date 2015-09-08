package org.entityflow2.component;

import org.entityflow2.range.IntRange;
import org.entityflow2.type.IntType;
import org.flowutils.Symbol;

/**
 * Primitive property accessor.
 */
public final class IntProperty extends Property<Integer> {

    /**
     * @param id                unique id of this property within the component type that it belongs to.
     * @param hostComponentType the componentType that this Property belongs to.
     * @param defaultValue      default value of this property in new components.
     * @param range             allowed value range for this property, or null if no restrictions.
     */
    public IntProperty(Symbol id,
                       ComponentType hostComponentType,
                       int defaultValue,
                       IntRange range) {
        super(id, hostComponentType, defaultValue, IntType.TYPE, range);
    }

    /**
     * @return value of the property for the specified entity
     */
    public final int get(int entityId) {
        // Read value from buffer
        return IntType.TYPE.readIntValue(getDataBuffer(), getParameterOffset(entityId));
    }

    /**
     * Set the value of this property for the specified entity.
     * @param entityId id of the entity whose component property we want to change.
     * @param value new value for the component property.
     */
    public final void set(int entityId, int value) {
        // Clamp value if needed
        final IntRange range = (IntRange) getRange();
        if (range != null) {
            value = range.clampInt(value);
        }

        // Write value to buffer
        IntType.TYPE.writeIntValue(getDataBuffer(), getParameterOffset(entityId), value);
    }

}
