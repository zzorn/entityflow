package org.entityflow2;

/**
 *
 */
public class DefaultProperty<T> implements Property<T> {

    @Override
    public ComponentType getComponentType() {
        return null;
    }

    @Override
    public T get(final long entityId) {
        return null;
    }

    @Override
    public void set(final long entityId, final T value) {

    }
}
