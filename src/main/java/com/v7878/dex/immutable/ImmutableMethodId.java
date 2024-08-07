package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseMethodId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.iface.TypeId;
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
}
