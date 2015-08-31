package org.entityflow2.range;

/**
 *
 */
public interface Range<T> {

    Class<T> getValueClass();

    T clamp(T value);





}
