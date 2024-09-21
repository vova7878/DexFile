package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseMethodDef;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.iface.MethodImplementation;
import com.v7878.dex.iface.Parameter;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.AccessFlagUtils;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;

public class ImmutableMethodDef extends BaseMethodDef {
    private final String name;
    private final ImmutableTypeId return_type;
    private final List<? extends ImmutableParameter> parameters;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final ImmutableMethodImplementation implementation;
    private final NavigableSet<? extends ImmutableAnnotation> annotations;

    protected ImmutableMethodDef(
            String name, TypeId return_type, Iterable<? extends Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            Iterable<? extends Annotation> annotations) {
        this.name = Objects.requireNonNull(name);
        this.return_type = ImmutableTypeId.of(return_type);
        this.parameters = ItemConverter.toList(ImmutableParameter::of,
                value -> value instanceof ImmutableParameter, parameters);
        this.access_flags = Preconditions.checkMethodAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        // TODO: check number of registers (at least as many as required for parameters)
        // TODO: check that abstract and native methods should not have implementation
        this.implementation = implementation == null ? null :
                ImmutableMethodImplementation.of(implementation);
        this.annotations = ItemConverter.toNavigableSet(ImmutableAnnotation::of,
                value -> value instanceof ImmutableAnnotation, annotations);
    }

    public static ImmutableMethodDef of(
            String name, TypeId return_type, Iterable<? extends Parameter> parameters,
            int access_flags, int hiddenapi_flags, MethodImplementation implementation,
            Iterable<? extends Annotation> annotations) {
        return new ImmutableMethodDef(name, return_type, parameters,
                access_flags, hiddenapi_flags, implementation, annotations);
    }

    public static ImmutableMethodDef of(MethodDef other) {
        if (other instanceof ImmutableMethodDef immutable) return immutable;
        return new ImmutableMethodDef(other.getName(), other.getReturnType(),
                other.getParameters(), other.getAccessFlags(), other.getHiddenApiFlags(),
                other.getImplementation(), other.getAnnotations());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableTypeId getReturnType() {
        return return_type;
    }

    @Override
    public List<? extends ImmutableParameter> getParameters() {
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

    @Override
    public MethodImplementation getImplementation() {
        return implementation;
    }

    @Override
    public NavigableSet<? extends ImmutableAnnotation> getAnnotations() {
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
