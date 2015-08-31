package org.entityflow2.component;

import net.openhft.koloboke.collect.map.hash.HashLongObjMaps;
import org.entityflow2.component.property.Property;
import org.entityflow2.range.Range;
import org.entityflow2.type.Type;
import org.flowutils.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ComponentType {

    private int dataBlockSize = 0;

    private final List<Property> properties = new ArrayList<Property>();

    /**
     * Adds a property to this component type.
     * Must be called before the component type is initialized.
     * Typically called from the constructor of a descendant ComponentType.
     *
     * @param id unique id of the property within this component type.
     * @param defaultValue default value for the property.
     * @param type type of the property.
     * @param range allowed range for the property.
     * @return the created property object.
     */
    protected <T> Property<T> addProperty(String id, T defaultValue, Type<T> type, Range<T> range) {

        final int offset;
        if (type.isByteBufferStorable()) {
            // Store in a buffer
            offset = dataBlockSize;
            dataBlockSize += type.getDataLengthBytes();
        }
        else {
            // Store in a map
            offset = -1;

        }

        final Property<T> property = new Property<T>(Symbol.get(id), this, defaultValue, type, range, offset);

        // TODO: Ensure id is not reused.

        properties.add(property);

        return property;

    }



}
