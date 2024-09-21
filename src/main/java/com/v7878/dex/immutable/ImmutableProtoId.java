package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseProtoId;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public class ImmutableProtoId extends BaseProtoId {
    private final ImmutableTypeId returnType;
    private final List<ImmutableTypeId> parameters;

    protected ImmutableProtoId(TypeId returnType, Iterable<? extends TypeId> parameters) {
        this.returnType = ImmutableTypeId.of(returnType);
        this.parameters = ItemConverter.toList(ImmutableTypeId::of,
                value -> value instanceof ImmutableTypeId, parameters);
    }

    public static ImmutableProtoId of(TypeId returnType, Iterable<? extends TypeId> parameters) {
        return new ImmutableProtoId(returnType, parameters);
    }

    public static ImmutableProtoId of(ProtoId other) {
        if (other instanceof ImmutableProtoId immutable) return immutable;
        return new ImmutableProtoId(other.getReturnType(), other.getParameterTypes());
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
        return Objects.hash(getReturnType(), getParameterTypes());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ProtoId other
                && Objects.equals(getReturnType(), other.getReturnType())
                && Objects.equals(getParameterTypes(), other.getParameterTypes());
    }

    @Override
    public int compareTo(ProtoId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getReturnType(), other.getReturnType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(getParameterTypes(), other.getParameterTypes());
    }
}
