package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.ShortyUtils;

import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class ProtoId implements Comparable<ProtoId> {
    private final TypeId return_type;
    private final List<TypeId> parameters;

    private ProtoId(TypeId return_type, List<TypeId> parameters) {
        this.return_type = return_type;
        this.parameters = parameters;
    }

    public static ProtoId of(TypeId return_type, Iterable<TypeId> parameters) {
        return new ProtoId(Objects.requireNonNull(return_type),
                ItemConverter.toList(parameters));
    }

    public static ProtoId of(TypeId return_type, TypeId... parameters) {
        return of(return_type, Arrays.asList(parameters));
    }

    public static ProtoId of(Executable value) {
        Objects.requireNonNull(value);
        Class<?> return_type = value instanceof Method m ? m.getReturnType() : void.class;
        return new ProtoId(TypeId.of(return_type),
                Arrays.stream(value.getParameterTypes()).map(TypeId::of).toList());
    }

    public static ProtoId of(MethodType value) {
        Objects.requireNonNull(value);
        return new ProtoId(TypeId.of(value.returnType()),
                value.parameterList().stream().map(TypeId::of).toList());
    }

    public TypeId getReturnType() {
        return return_type;
    }

    public List<TypeId> getParameterTypes() {
        return parameters;
    }

    public String getShorty() {
        return ShortyUtils.getShorty(return_type, parameters);
    }

    public int getInputRegisterCount() {
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
