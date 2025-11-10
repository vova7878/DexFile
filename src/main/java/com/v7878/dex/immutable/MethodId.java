package com.v7878.dex.immutable;

import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
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
        return new MethodId(declaring_class, name, ProtoId.of(returnType, parameters));
    }

    public static MethodId of(TypeId declaring_class, String name,
                              TypeId returnType, TypeId... parameters) {
        return new MethodId(declaring_class, name, ProtoId.of(returnType, parameters));
    }

    public static MethodId constructor(TypeId declaring_class, Iterable<TypeId> parameters) {
        return of(declaring_class, "<init>", TypeId.V, parameters);
    }

    public static MethodId constructor(TypeId declaring_class, TypeId... parameters) {
        return of(declaring_class, "<init>", TypeId.V, parameters);
    }

    public static MethodId static_constructor(TypeId declaring_class) {
        return of(declaring_class, "<clinit>", ProtoId.of(TypeId.V));
    }

    private static String getName(Executable ex) {
        if (ex instanceof Constructor) {
            return (ex.getModifiers() & ACC_STATIC) == 0 ? "<init>" : "<clinit>";
        }
        return ex.getName();
    }

    public static MethodId of(Executable ex) {
        Objects.requireNonNull(ex);
        return new MethodId(TypeId.of(ex.getDeclaringClass()), getName(ex), ProtoId.of(ex));
    }

    public static MethodId of(String descriptor) {
        Objects.requireNonNull(descriptor);
        int point = descriptor.indexOf('.');
        int bracket = descriptor.lastIndexOf('(');
        if ((point | bracket) < 0 || !(point < bracket)) {
            throw new IllegalArgumentException(
                    "Not a method descriptor: " + descriptor);
        }
        return MethodId.of(
                TypeId.of(descriptor.substring(0, point)),
                descriptor.substring(point + 1, bracket),
                ProtoId.of(descriptor.substring(bracket))
        );
    }

    public String getDescriptor() {
        return getDeclaringClass() + "." + getName() + getProto();
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

    public String computeShorty() {
        return proto.computeShorty();
    }

    public int countInputRegisters() {
        return proto.countInputRegisters();
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

    @Override
    public String toString() {
        return getDescriptor();
    }
}
