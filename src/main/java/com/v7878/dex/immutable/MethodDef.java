package com.v7878.dex.immutable;

import static com.v7878.dex.DexConstants.ACC_ABSTRACT;
import static com.v7878.dex.DexConstants.ACC_DIRECT_MASK;
import static com.v7878.dex.DexConstants.ACC_NATIVE;

import com.v7878.dex.Internal;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;
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
            String name, TypeId return_type, List<Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            NavigableSet<Annotation> annotations) {
        if ((access_flags & (ACC_ABSTRACT | ACC_NATIVE)) != 0) {
            if (implementation != null) {
                throw new IllegalArgumentException("Abstract or native methods should not have implementation");
            }
        } else {
            if (implementation == null) {
                throw new IllegalArgumentException("Implementation is null");
            }
            int ins = ShortyUtils.getDefInputRegisterCount(parameters, access_flags);
            int regs = implementation.getRegisterCount();
            if (regs < ins) {
                throw new IllegalArgumentException(String.format(
                        "Not enough registers for parameters. Required: %d, available: %d", ins, regs));
            }
        }
        this.name = Objects.requireNonNull(name);
        this.return_type = Objects.requireNonNull(return_type);
        this.parameters = Objects.requireNonNull(parameters);
        this.access_flags = Preconditions.checkMethodAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        this.implementation = implementation; // may be null
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Internal
    public static MethodDef raw(
            String name, TypeId return_type, List<Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            NavigableSet<Annotation> annotations) {
        return new MethodDef(name, return_type, parameters,
                access_flags, hiddenapi_flags, implementation, annotations);
    }

    public static MethodDef of(
            String name, TypeId return_type, Iterable<Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            Iterable<Annotation> annotations) {
        return new MethodDef(name, return_type, Converter.toList(parameters), access_flags,
                hiddenapi_flags, implementation, Converter.toNavigableSet(annotations));
    }

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
        return Converter.transformList(parameters, Parameter::getType);
    }

    public ProtoId getProto() {
        return ProtoId.raw(return_type, getParameterTypes());
    }

    public String computeShorty() {
        return ShortyUtils.getDefShorty(return_type, parameters);
    }

    public String computeFullShorty() {
        return ShortyUtils.getDefShorty(return_type, parameters, access_flags);
    }

    public int countInputRegisters() {
        return ShortyUtils.getDefInputRegisterCount(parameters);
    }

    public int countFullInputRegisters() {
        return ShortyUtils.getDefInputRegisterCount(parameters, access_flags);
    }

    @Override
    public int getAccessFlags() {
        return access_flags;
    }

    public boolean isDirect() {
        return (getAccessFlags() & ACC_DIRECT_MASK) != 0;
    }

    public boolean isVirtual() {
        return !isDirect();
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
        int out = -Boolean.compare(isDirect(), other.isDirect());
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
