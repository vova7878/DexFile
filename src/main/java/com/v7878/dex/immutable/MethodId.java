package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public class MethodId extends MemberId implements Comparable<MethodId> {
    private final TypeId declaring_class;
    private final String name;
    private final TypeId returnType;
    private final List<TypeId> parameters;

    protected MethodId(TypeId declaring_class, String name,
                       TypeId returnType, Iterable<TypeId> parameters) {
        this.declaring_class = Objects.requireNonNull(declaring_class);
        this.name = Objects.requireNonNull(name);
        this.returnType = Objects.requireNonNull(returnType);
        this.parameters = ItemConverter.toList(parameters);
    }

    public static MethodId of(TypeId declaring_class, String name,
                              TypeId returnType, Iterable<TypeId> parameters) {
        return new MethodId(declaring_class, name, returnType, parameters);
    }

    @Override
    public TypeId getDeclaringClass() {
        return declaring_class;
    }

    @Override
    public String getName() {
        return name;
    }

    public TypeId getReturnType() {
        return returnType;
    }

    public List<TypeId> getParameterTypes() {
        return parameters;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeclaringClass(), getName(), getReturnType(), getParameterTypes());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodId other
                && Objects.equals(getDeclaringClass(), other.getDeclaringClass())
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getReturnType(), other.getReturnType())
                && Objects.equals(getParameterTypes(), other.getParameterTypes());
    }

    @Override
    public int compareTo(MethodId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getDeclaringClass(), other.getDeclaringClass());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getReturnType(), other.getReturnType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(getParameterTypes(), other.getParameterTypes());
    }
}
