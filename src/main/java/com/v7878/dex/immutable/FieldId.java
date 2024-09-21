package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public class FieldId extends MemberId implements Comparable<FieldId> {
    private final TypeId declaring_class;
    private final String name;
    private final TypeId type;

    protected FieldId(TypeId declaring_class, String name, TypeId type) {
        this.declaring_class = Objects.requireNonNull(declaring_class);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
    }

    public static FieldId of(TypeId declaring_class, String name, TypeId type) {
        return new FieldId(declaring_class, name, type);
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
