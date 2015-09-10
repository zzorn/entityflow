package org.entityflow2.example;

import org.entityflow2.component.ComponentType;
import org.entityflow2.component.DoubleProperty;

/**
 *
 */
public class Position extends ComponentType {

    public final DoubleProperty x;
    public final DoubleProperty y;
    public final DoubleProperty z;

    public Position() {
        x = addProperty("x", 0.0);
        y = addProperty("y", 0.0);
        z = addProperty("z", 0.0);
    }
}
