package org.entityflow.component;


import org.entityflow.entity.Entity;
import org.flowutils.Check;

/**
 * Base class for Component implementations, provides some common functionality.
 */
public abstract class ComponentBase implements Component {

    private final Class<? extends Component> baseType;
    private Entity entity;

    /**
     * Creates a new component with the component type id based on the components implementation class.
     */
    public ComponentBase() {
        this(null);
    }

    /**
     * @param baseType the type used to determine the component type id for this type of component.
     *                 Pass in null for default behaviour (uses this.getClass()), or a custom class that this class implements to use that as base type.
     *                 If two component types both use the same base type, they can not be in the same entity at the same time.
     *                 E.g. a Position interface might extend Component interface and have two implementations,
     *                 WorldPosition and InventoryPosition, either of which could be used as the position component of an entity, but not at the same time.
     */
    public ComponentBase(Class<? extends Component> baseType) {
        if (baseType == null) baseType = getClass();
        if (!baseType.isAssignableFrom(getClass()))
            throw new IllegalArgumentException("The component type should be assignable to the specified base type.");
        this.baseType = baseType;
    }

    @Override
    public Class<? extends Component> getBaseType() {
        return baseType;
    }

    @Override
    public final void setEntity(Entity entity) {
        Check.notNull(entity, "entity");
        if (this.entity != null)
            throw new IllegalStateException("Can not add a component to more than one entity!  " +
                                            "Already added to " + this.entity + ", " +
                                            "can not also add to " + entity);

        this.entity = entity;
        handleAdded(entity);
    }

    @Override
    public final void onRemoved() {
        if (entity == null)
            throw new IllegalStateException("Can not delete a component that has not been added to any entity");

        handleRemoved(entity);
        entity = null;
    }

    @Override public final Entity getEntity() {
        return entity;
    }

    /**
     * Do any component specific initialization after the component is added to an entity.
     *
     * @param entity the entity the component was added to.
     */
    protected void handleAdded(Entity entity) {}

    /**
     * Do any component specific de-initialization after the component is removed from the entity.
     *
     * @param entity the entity the component was removed from.
     */
    protected void handleRemoved(Entity entity) {}


}
