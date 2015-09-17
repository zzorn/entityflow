package org.entityflow2.component;

import org.entityflow2.range.Range;
import org.entityflow2.type.Type;
import org.flowutils.Symbol;

/**
 * A property of a component type with a reference type non-primitive value.
 */
public class Property<T> extends PropertyBase<T> {

    public Property(Symbol id,
                    ComponentType componentType,
                    T defaultValue,
                    Type<T> type,
                    Range<T> range) {
        super(id, componentType, defaultValue, type, range);
    }

    public Property(Symbol id,
                    ComponentType componentType,
                    T defaultValue,
                    Type<T> type,
                    Range<T> range,
                    int valueOffset) {
        super(id, componentType, defaultValue, type, range, valueOffset);
    }

    /**
     * @return value of the property for the specified entity
     */
    public final T get(int entityId) {
        return getObject(entityId);
    }

}
