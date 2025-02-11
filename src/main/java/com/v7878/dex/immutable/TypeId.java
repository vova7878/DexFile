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
        return switch (descriptor) {
            case "V" -> V;
            case "Z" -> Z;
            case "B" -> B;
            case "S" -> S;
            case "C" -> C;
            case "I" -> I;
            case "F" -> F;
            case "J" -> J;
            case "D" -> D;
            case "Ljava/lang/Object;" -> OBJECT;
            default -> new TypeId(descriptor);
        };
    }

    public static TypeId ofName(String class_name) {
        Objects.requireNonNull(class_name);
        int array_depth = 0;
        while (class_name.endsWith("[]")) {
            array_depth++;
            class_name = class_name.substring(0, class_name.length() - 2);
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < array_depth; i++) {
            out.append('[');
        }
        switch (class_name) {
            case "void" -> out.append("V");
            case "boolean" -> out.append("Z");
            case "byte" -> out.append("B");
            case "short" -> out.append("S");
            case "char" -> out.append("C");
            case "int" -> out.append("I");
            case "float" -> out.append("F");
            case "long" -> out.append("J");
            case "double" -> out.append("D");
            default -> {
                out.append("L");
                out.append(class_name.replace('.', '/'));
                out.append(";");
            }
        }
        return new TypeId(out.toString());
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
