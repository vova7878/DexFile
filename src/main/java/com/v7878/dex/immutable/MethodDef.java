package com.v7878.dex.immutable;

import com.v7878.dex.util.AccessFlagUtils;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

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
        this.parameters = ItemConverter.toList(parameters);
        this.access_flags = Preconditions.checkMethodAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        // TODO: check number of registers (at least as many as required for parameters)
        // TODO: check that abstract and native methods should not have implementation
        this.implementation = implementation;
        this.annotations = ItemConverter.toNavigableSet(annotations);
    }

    public static MethodDef of(
            String name, TypeId return_type, Iterable<Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            Iterable<Annotation> annotations) {
        return new MethodDef(name, return_type, parameters,
                access_flags, hiddenapi_flags, implementation, annotations);
    }

    @Override
    public String getName() {
        return name;
    }

    public TypeId getReturnType() {
        return return_type;
    }

    public List<? extends Parameter> getParameters() {
        return parameters;
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
        int out = Boolean.compare(AccessFlagUtils.isDirect(getAccessFlags()),
                AccessFlagUtils.isDirect(other.getAccessFlags()));
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
