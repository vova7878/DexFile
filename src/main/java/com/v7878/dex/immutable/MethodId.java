package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public final class MethodId extends MemberId implements Comparable<MethodId> {
    private final TypeId declaring_class;
    private final String name;
    private final ProtoId proto;

    private MethodId(TypeId declaring_class, String name, ProtoId proto) {
        this.declaring_class = Objects.requireNonNull(declaring_class);
        this.name = Objects.requireNonNull(name);
        this.proto = Objects.requireNonNull(proto);
    }

    public static MethodId of(TypeId declaring_class, String name, ProtoId proto) {
        return new MethodId(declaring_class, name, proto);
    }

    public static MethodId of(TypeId declaring_class, String name,
                              TypeId returnType, Iterable<TypeId> parameters) {
        return of(declaring_class, name, ProtoId.of(returnType, parameters));
    }

    @Override
    public TypeId getDeclaringClass() {
        return declaring_class;
    }

    @Override
    public String getName() {
        return name;
    }

    public ProtoId getProto() {
        return proto;
    }

    public TypeId getReturnType() {
        return proto.getReturnType();
    }

    public List<TypeId> getParameterTypes() {
        return proto.getParameterTypes();
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
                && Objects.equals(getProto(), other.getProto());
    }

    @Override
    public int compareTo(MethodId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getDeclaringClass(), other.getDeclaringClass());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getProto(), other.getProto());
    }
}
