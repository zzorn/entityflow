package org.entityflow.component;

import org.entityflow.entity.Entity;

/**
 * Part of an Entity.  Holds data for some specific aspect of the entity.
 */
public interface Component {

    /**
     * @return A base type of this component, indicating the type.  Can be the same as the component type.
     */
    Class<? extends Component> getBaseType();

    /**
     * Called when the component instance is added to an entity.
     * @param entity the entity it is added to.
     */
    void setEntity(Entity entity);

    /**
     * @return the entity that this component has been added to, or null if it has not yet been added, or has already been removed.
     */
    Entity getEntity();

    /**
     * Called when a component is removed from an entity.
     * May do any needed memory de-allocation.
     */
    void onRemoved();
}
