package org.entityflow2;

import org.flowutils.time.Time;

/**
 *
 */
public interface EntityManager {


    void registerComponentType();

    void registerProcessor();

    void update(Time time);


}
