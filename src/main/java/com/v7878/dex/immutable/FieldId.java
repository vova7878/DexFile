package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public final class FieldId extends MemberId implements Comparable<FieldId> {
    private final TypeId declaring_class;
    private final String name;
    private final TypeId type;

    private FieldId(TypeId declaring_class, String name, TypeId type) {
        this.declaring_class = Objects.requireNonNull(declaring_class);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        checkDeclaringClass();
    }

    public static FieldId of(TypeId declaring_class, String name, TypeId type) {
        return new FieldId(declaring_class, name, type);
    }

    public static FieldId of(Field field) {
        Objects.requireNonNull(field);
        return new FieldId(TypeId.of(field.getDeclaringClass()),
                field.getName(), TypeId.of(field.getType()));
    }

    public static FieldId of(Enum<?> e) {
        Objects.requireNonNull(e);
        TypeId declaring_class = TypeId.of(e.getDeclaringClass());
        // TODO: Is this really true?
        return new FieldId(declaring_class, e.name(), declaring_class);
    }

    public static FieldId of(String descriptor) {
        Objects.requireNonNull(descriptor);
        int arrow = descriptor.indexOf("->");
        int colon = descriptor.lastIndexOf(':');
        if ((arrow | colon) < 0 || arrow > colon) {
            throw new IllegalArgumentException(
                    "Not a field reference: " + descriptor);
        }
        if (arrow == 0) {
            throw new IllegalArgumentException(
                    "Empty declaring class: " + descriptor);
        }
        if (colon == arrow + 2) {
            throw new IllegalArgumentException(
                    "Empty field name: " + descriptor);
        }
        return FieldId.of(
                TypeId.of(descriptor.substring(0, arrow)),
                descriptor.substring(arrow + 2, colon),
                TypeId.of(descriptor.substring(colon + 1))
        );
    }

    public String getDescriptor() {
        return getDeclaringClass() + "->" + getName() + ":" + getType();
    }

    @Override
    public TypeId getDeclaringClass() {
        return declaring_class;
    }

    @Override
    public String getName() {
        return name;
    }

    public TypeId getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeclaringClass(), getName(), getType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof FieldId other
                && Objects.equals(getDeclaringClass(), other.getDeclaringClass())
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType());
    }

    @Override
    public int compareTo(FieldId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getDeclaringClass(), other.getDeclaringClass());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }

    @Override
    public String toString() {
        return getDescriptor();
    }
}
