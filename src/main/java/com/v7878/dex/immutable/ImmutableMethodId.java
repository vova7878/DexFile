package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseMethodId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public class ImmutableMethodId extends BaseMethodId implements ImmutableMemberId {
    private final ImmutableTypeId declaring_class;
    private final String name;
    private final ImmutableTypeId returnType;
    private final List<ImmutableTypeId> parameters;

    protected ImmutableMethodId(TypeId declaring_class, String name, TypeId returnType,
                                Iterable<? extends TypeId> parameters) {
        this.declaring_class = ImmutableTypeId.of(declaring_class);
        this.name = Objects.requireNonNull(name);
        this.returnType = ImmutableTypeId.of(returnType);
        this.parameters = ItemConverter.toList(ImmutableTypeId::of,
                value -> value instanceof ImmutableTypeId, parameters);
    }

    public static ImmutableMethodId of(TypeId declaring_class, String name, TypeId returnType,
                                       Iterable<? extends TypeId> parameters) {
        return new ImmutableMethodId(declaring_class, name, returnType, parameters);
    }

    public static ImmutableMethodId of(MethodId other) {
        if (other instanceof ImmutableMethodId immutable) return immutable;
        return new ImmutableMethodId(other.getDeclaringClass(), other.getName(),
                other.getReturnType(), other.getParameterTypes());
    }

    @Override
    public ImmutableTypeId getDeclaringClass() {
        return declaring_class;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableTypeId getReturnType() {
        return returnType;
    }

    @Override
    public List<? extends ImmutableTypeId> getParameterTypes() {
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
