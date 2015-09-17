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
        super(position, physical, new FixedTimestepStrategy(0.01));
    }

    @Override protected void updateEntity(Time time, int entityId, Position position, Physical physical) {
        System.out.println("PhysicsProcessor.updateEntity");

        double x = position.x.get(entityId);
        double y = position.y.get(entityId);
        double z = position.z.get(entityId);

        System.out.println("  x = " + x);

        double mass = physical.mass.get(entityId);
        Material material = physical.material.get(entityId);

        // Nonsensical example physics
        x *= y;
        x -= mass;
        x *= material.getDensity();
        position.x.set(entityId, x);
    }

}
