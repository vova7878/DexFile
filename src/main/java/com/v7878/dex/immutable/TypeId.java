package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ShortyUtils;

import java.util.Objects;

public final class TypeId implements Comparable<TypeId> {
    public static final TypeId V = of("V");
    public static final TypeId Z = of("Z");
    public static final TypeId B = of("B");
    public static final TypeId S = of("S");
    public static final TypeId C = of("C");
    public static final TypeId I = of("I");
    public static final TypeId F = of("F");
    public static final TypeId J = of("J");
    public static final TypeId D = of("D");
    public static final TypeId OBJECT = of(Object.class);

    private final String descriptor;

    private TypeId(String descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    public static TypeId of(String descriptor) {
        return new TypeId(descriptor);
    }

    public static TypeId of(Class<?> clazz) {
        String class_name = clazz.getName();
        if (clazz.isArray()) {
            return of(class_name.replace('.', '/'));
        }
        return switch (class_name) {
            case "void" -> V;
            case "boolean" -> Z;
            case "byte" -> B;
            case "short" -> S;
            case "char" -> C;
            case "int" -> I;
            case "float" -> F;
            case "long" -> J;
            case "double" -> D;
            default -> of("L" + class_name.replace('.', '/') + ";");
        };
    }

    public String getDescriptor() {
        return descriptor;
    }

    public char getShorty() {
        return ShortyUtils.getTypeShorty(this);
    }

    public int getRegisterCount() {
        return ShortyUtils.getRegisterCount(this);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDescriptor());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TypeId other
                && Objects.equals(getDescriptor(), other.getDescriptor());
    }

    @Override
    public int compareTo(TypeId other) {
        if (other == this) return 0;
        return CollectionUtils.compareNonNull(getDescriptor(), other.getDescriptor());
    }
}
