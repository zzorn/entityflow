package org.entityflow2.example;

import org.entityflow2.EntityManager;
import org.entityflow2.processor.EntityProcessor2Base;
import org.flowutils.time.Time;
import org.flowutils.updating.strategies.FixedTimestepStrategy;

/**
 *
 */
public class PhysicsProcessor extends EntityProcessor2Base<Position, Physical> {

    public PhysicsProcessor(Position position, Physical physical) {
        super(position, physical, new FixedTimestepStrategy(0.02));
    }

    @Override protected void updateEntity(Time time, int entityId, Position position, Physical physical) {
        double x = position.x.get(entityId);
        double y = position.y.get(entityId);
        double z = position.z.get(entityId);

        /*
        // Debug prints
        System.out.println("  PhysicsProcessor.updateEntity");
        System.out.println("    entityId = " + entityId);
        System.out.println("    x = " + x);
        System.out.println("    y = " + y);
        System.out.println("    time.getSecondsSinceStart() = " + time.getSecondsSinceStart());
        System.out.println("    time.getMillisecondsSinceStart() = " + time.getMillisecondsSinceStart());
        System.out.println("    time.getStepCount() = " + time.getStepCount());
        */

        double mass = physical.mass.get(entityId);
        Material material = physical.material.get(entityId);

        // Nonsensical example physics
        x += Math.sqrt(x);
        y += Math.sqrt(x-y/material.getDensity());
        z += Math.sqrt(x*y+mass);

        position.x.set(entityId, x);
        position.y.set(entityId, y);
        position.z.set(entityId, z);
    }

}
