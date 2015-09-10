package org.entityflow2;

import org.entityflow2.component.ComponentType;
import org.entityflow2.component.DoubleProperty;
import org.entityflow2.component.Property;
import org.entityflow2.range.DoubleRange;
import org.entityflow2.type.ComplexTypeBase;
import org.entityflow2.type.Type;

/**
 *
 */
public class Physical extends ComponentType {

    public final DoubleProperty mass;
    public final Property<Material> material;

    public Physical() {
        mass = addProperty("mass", 1, DoubleRange.ZERO_OR_LARGER);
        material = addProperty("material", Material.getMaterial("stone"), MaterialType.TYPE);
    }
}
