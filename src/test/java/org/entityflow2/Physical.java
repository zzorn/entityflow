package org.entityflow2;

import org.entityflow2.component.ComponentType;
import org.entityflow2.component.DoubleProperty;
import org.entityflow2.range.DoubleRange;

/**
 *
 */
public class Physical extends ComponentType {

    public final DoubleProperty mass;

    public Physical() {
        mass = addProperty("mass", 1, DoubleRange.ZERO_OR_LARGER);
    }
}
