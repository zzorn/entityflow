package org.entityflow2.example;

import org.flowutils.Check;
import org.flowutils.Symbol;

import java.util.HashMap;
import java.util.Map;

import static org.flowutils.Check.notNull;

/**
 *
 */
public class Material {
    private static Map<Symbol, Material> REGISTERED_MATERIALS = new HashMap<Symbol, Material>();

    public static Material getMaterial(String materialId) {
        return REGISTERED_MATERIALS.get(Symbol.get(materialId));
    }

    public static Material registerMaterial(Material material) {
        notNull(material, "material");
        REGISTERED_MATERIALS.put(material.getId(), material);
        return material;
    }

    static {
        registerMaterial(new Material(Symbol.get("water"), 1000.0));
        registerMaterial(new Material(Symbol.get("stone"), 2500.0));
        registerMaterial(new Material(Symbol.get("wood"), 500.0));
    }


    private final Symbol id;
    private final double density;

    public Material(Symbol id, double density) {
        notNull(id, "id");
        Check.positive(density, "density");

        this.id = id;
        this.density = density;
    }

    public Symbol getId() {
        return id;
    }

    public double getDensity() {
        return density;
    }
}
