package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public final class ProtoId implements Comparable<ProtoId> {
    private final TypeId returnType;
    private final List<TypeId> parameters;

    private ProtoId(TypeId returnType, Iterable<TypeId> parameters) {
        this.returnType = Objects.requireNonNull(returnType);
        this.parameters = ItemConverter.toList(parameters);
    }

    public static ProtoId of(TypeId returnType, Iterable<TypeId> parameters) {
        return new ProtoId(returnType, parameters);
    }

    public TypeId getReturnType() {
        return returnType;
    }

    public List<TypeId> getParameterTypes() {
        return parameters;
    }

    public String getShorty() {
        var parameters = getParameterTypes();
        var out = new StringBuilder(parameters.size() + 1);
        out.append(getReturnType().getShorty());
        for (TypeId tmp : parameters) {
            out.append(tmp.getShorty());
        }
        return out.toString();
    }

    public int getInputRegisterCount() {
        int out = 0;
        for (TypeId tmp : getParameterTypes()) {
            out += tmp.getRegisterCount();
        }
        return out;
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
