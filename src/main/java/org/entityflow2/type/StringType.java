package org.entityflow2.type;

/**
 *
 */
public final class StringType extends ComplexTypeBase<String> {

    public static final StringType TYPE = new StringType();

    @Override public Class<String> getValueClass() {
        return String.class;
    }

    @Override public String fromString(String source) {
        return source;
    }

    @Override public String toString(String value) {
        return value;
    }
}
