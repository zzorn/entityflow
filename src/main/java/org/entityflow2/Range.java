package org.entityflow2;

/**
 *
 */
public interface Range<T> {

    Class<T> getValueClass();

    T clamp(T value);





}
