package org.entityflow2;

import org.entityflow2.type.ComplexTypeBase;
import org.flowutils.Symbol;

import java.util.HashMap;
import java.util.Map;

import static org.flowutils.Check.notNull;

/**
 *
 */
public class MaterialType extends ComplexTypeBase<Material> {

    public static final MaterialType TYPE = new MaterialType();

    @Override public Class<Material> getValueClass() {
        return Material.class;
    }

    @Override public Material fromString(String source) {
        return Material.getMaterial(source);
    }

    @Override public String toString(Material value) {
        return value.getId().toString();
    }
}
