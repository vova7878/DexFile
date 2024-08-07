package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseParameter;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.Parameter;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.ItemConverter;

import java.util.NavigableSet;

public class ImmutableParameter extends BaseParameter {
    private final ImmutableTypeId type;
    private final String name;
    private final NavigableSet<? extends ImmutableAnnotation> annotations;

    protected ImmutableParameter(TypeId type, String name,
                                 Iterable<? extends Annotation> annotations) {
        this.type = ImmutableTypeId.of(type);
        this.name = name; // may be null
        this.annotations = ItemConverter.toNavigableSet(ImmutableAnnotation::of,
                value -> value instanceof ImmutableAnnotation, annotations);
    }

    public static ImmutableParameter of(TypeId type, String name,
                                        Iterable<? extends Annotation> annotations) {
        return new ImmutableParameter(type, name, annotations);
    }

    public static ImmutableParameter of(Parameter other) {
        if (other instanceof ImmutableParameter immutable) return immutable;
        return new ImmutableParameter(other.getType(), other.getName(), other.getAnnotations());
    }

    @Override
    public ImmutableTypeId getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NavigableSet<? extends ImmutableAnnotation> getAnnotations() {
        return annotations;
    }
}
