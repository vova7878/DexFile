package com.v7878.dex.immutable;

import com.v7878.dex.util.ItemConverter;

import java.util.NavigableSet;
import java.util.Objects;

public class Parameter implements Annotatable {
    private final TypeId type;
    private final String name;
    private final NavigableSet<Annotation> annotations;

    protected Parameter(TypeId type, String name, Iterable<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.name = name; // may be null
        this.annotations = ItemConverter.toNavigableSet(annotations);
    }

    public static Parameter of(TypeId type, String name, Iterable<Annotation> annotations) {
        return new Parameter(type, name, annotations);
    }

    public TypeId getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public NavigableSet<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), getAnnotations());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Parameter other
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getAnnotations(), other.getAnnotations());
    }
}
