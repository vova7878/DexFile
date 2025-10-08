package com.v7878.dex.immutable;

import com.v7878.dex.Internal;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.ShortyUtils;

import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public final class ProtoId implements Comparable<ProtoId> {
    private final TypeId return_type;
    private final List<TypeId> parameters;

    private ProtoId(TypeId return_type, List<TypeId> parameters) {
        this.return_type = Objects.requireNonNull(return_type);
        this.parameters = Objects.requireNonNull(parameters);
    }

    @Internal
    public static ProtoId raw(TypeId return_type, List<TypeId> parameters) {
        return new ProtoId(return_type, parameters);
    }

    public static ProtoId of(TypeId return_type, Iterable<TypeId> parameters) {
        return new ProtoId(return_type, Converter.toList(parameters));
    }

    public static ProtoId of(TypeId return_type, TypeId... parameters) {
        return new ProtoId(return_type, Converter.toList(parameters));
    }

    public static ProtoId of(Executable value) {
        Objects.requireNonNull(value);
        Class<?> return_type = value instanceof Method m ? m.getReturnType() : void.class;
        return new ProtoId(TypeId.of(return_type),
                Converter.transform(value.getParameterTypes(), TypeId::of));
    }

    public static ProtoId of(MethodType value) {
        Objects.requireNonNull(value);
        return new ProtoId(TypeId.of(value.returnType()),
                Converter.transform(value.parameterList(), TypeId::of));
    }

    public TypeId getReturnType() {
        return return_type;
    }

    public List<TypeId> getParameterTypes() {
        return parameters;
    }

    public String computeShorty() {
        return ShortyUtils.getShorty(return_type, parameters);
    }

    public int countInputRegisters() {
        return ShortyUtils.getInputRegisterCount(parameters);
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
