package com.v7878.dex.immutable;

import static com.v7878.dex.DexConstants.ACC_DIRECT_MASK;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;
import com.v7878.dex.util.ShortyUtils;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;

public final class MethodDef extends MemberDef implements Comparable<MethodDef> {
    private final String name;
    private final TypeId return_type;
    private final List<Parameter> parameters;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final MethodImplementation implementation;
    private final NavigableSet<Annotation> annotations;

    private MethodDef(
            String name, TypeId return_type, Iterable<Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            Iterable<Annotation> annotations) {
        this.name = Objects.requireNonNull(name);
        this.return_type = Objects.requireNonNull(return_type);
        // TODO: check that parameters do not contain names if implementation is null
        this.parameters = ItemConverter.toList(parameters);
        this.access_flags = Preconditions.checkMethodAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        // TODO: check number of registers (at least as many as required for parameters)
        // TODO: check that abstract and native methods should not have implementation
        this.implementation = implementation; // may be null
        this.annotations = ItemConverter.toNavigableSet(annotations);
    }

    public static MethodDef of(
            String name, TypeId return_type, Iterable<Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            Iterable<Annotation> annotations) {
        return new MethodDef(name, return_type, parameters,
                access_flags, hiddenapi_flags, implementation, annotations);
    }

    // TODO?: Add simpler constructor?

    @Override
    public String getName() {
        return name;
    }

    public TypeId getReturnType() {
        return return_type;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public List<TypeId> getParameterTypes() {
        return ItemConverter.transformList(parameters, Parameter::getType);
    }

    public ProtoId getProto() {
        return ProtoId.ofInternal(return_type, getParameterTypes());
    }

    public String computeShorty() {
        return ShortyUtils.getDefShorty(return_type, parameters);
    }

    public int countInputRegisters() {
        return ShortyUtils.getDefInputRegisterCount(parameters);
    }

    @Override
    public int getAccessFlags() {
        return access_flags;
    }

    @Override
    public int getHiddenApiFlags() {
        return hiddenapi_flags;
    }

    public MethodImplementation getImplementation() {
        return implementation;
    }

    @Override
    public NavigableSet<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAccessFlags(), getHiddenApiFlags(),
                getReturnType(), getParameters(), getImplementation());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodDef other
                && getAccessFlags() == other.getAccessFlags()
                && getHiddenApiFlags() == other.getHiddenApiFlags()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getReturnType(), other.getReturnType())
                && Objects.equals(getParameters(), other.getParameters())
                && Objects.equals(getImplementation(), other.getImplementation());
    }

    @Override
    public int compareTo(MethodDef other) {
        if (other == this) return 0;
        // First direct methods, then virtual methods
        int out = -Boolean.compare((getAccessFlags() & ACC_DIRECT_MASK) != 0,
                (other.getAccessFlags() & ACC_DIRECT_MASK) != 0);
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getReturnType(), other.getReturnType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(
                Comparator.comparing(Parameter::getType),
                getParameters(), other.getParameters());
    }
}
