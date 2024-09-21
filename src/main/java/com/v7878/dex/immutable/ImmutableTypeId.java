package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseTypeId;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class ImmutableTypeId extends BaseTypeId {
    public static final ImmutableTypeId V = of("V");
    public static final ImmutableTypeId Z = of("Z");
    public static final ImmutableTypeId B = of("B");
    public static final ImmutableTypeId S = of("S");
    public static final ImmutableTypeId C = of("C");
    public static final ImmutableTypeId I = of("I");
    public static final ImmutableTypeId F = of("F");
    public static final ImmutableTypeId J = of("J");
    public static final ImmutableTypeId D = of("D");
    public static final ImmutableTypeId OBJECT = of(Object.class);

    private final String descriptor;

    protected ImmutableTypeId(String descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    public static ImmutableTypeId of(String descriptor) {
        return new ImmutableTypeId(descriptor);
    }

    public static ImmutableTypeId of(TypeId other) {
        if (other instanceof ImmutableTypeId immutable) return immutable;
        return new ImmutableTypeId(other.getDescriptor());
    }

    public static ImmutableTypeId of(Class<?> clazz) {
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

    @Override
    public String getDescriptor() {
        return descriptor;
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
