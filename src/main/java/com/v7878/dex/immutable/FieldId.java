package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Objects;

public final class FieldId extends MemberId implements Comparable<FieldId> {
    private final TypeId declaring_class;
    private final String name;
    private final TypeId type;

    private FieldId(TypeId declaring_class, String name, TypeId type) {
        this.declaring_class = declaring_class;
        this.name = name;
        this.type = type;
    }

    public static FieldId of(TypeId declaring_class, String name, TypeId type) {
        return new FieldId(Objects.requireNonNull(declaring_class),
                Objects.requireNonNull(name), Objects.requireNonNull(type));
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
}
