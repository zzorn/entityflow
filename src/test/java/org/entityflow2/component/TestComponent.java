package org.entityflow2.component;

/**
 *
 */
public class TestComponent extends ComponentType {

    public final IntProperty tentacleCount;
    public final Property<String> homeDimension;
    public final FloatProperty speed;
    public final IntProperty number;
    public final DoubleProperty intelligence;
    public final DoubleProperty horror;
    public final Property<String> name;

    public TestComponent() {
        tentacleCount = addProperty("tentacleCount", 41);
        homeDimension = addProperty("homeDimension", "fifth");
        speed = addProperty("speed", 13.3f);
        number = addProperty("number", 0);
        intelligence = addProperty("intelligence", 321.32);
        horror = addProperty("horror", 5.5);
        name = addProperty("name", "Igrixr");
    }
}
