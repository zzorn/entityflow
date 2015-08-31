package org.entityflow2;

/**
 * A property of a component type.
 */
public interface Property<T> {

    /**
     * @return the component type that this property belongs to.
     */
    ComponentType getComponentType();

    /**
     * @return value of the property for the specified entity.
     */
    T get(long entityId);

    /**
     * Set the value of this property for the specified entity.
     * @param entityId id of the entity whose component property we want to change.
     * @param value new value for the component property.
     */
    void set(long entityId, T value);

}
