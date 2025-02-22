package com.v7878.dex.immutable;

import static com.v7878.dex.util.CollectionUtils.toUnmodifiableList;

import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.StreamSupport;

public final class Parameter implements Annotatable {
    private final TypeId type;
    private final String name;
    private final NavigableSet<Annotation> annotations;

    private Parameter(TypeId type, String name, Iterable<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.name = name; // may be null
        this.annotations = ItemConverter.toNavigableSet(annotations);
    }

    public static Parameter of(TypeId type, String name, Iterable<Annotation> annotations) {
        return new Parameter(type, name, annotations);
    }

    public static Parameter of(TypeId type, String name) {
        return of(type, name, null);
    }

    public static Parameter of(TypeId type) {
        return of(type, null, null);
    }

    public static List<Parameter> listOf(Iterable<TypeId> types) {
        return toUnmodifiableList(StreamSupport
                .stream(types.spliterator(), false)
                .map(Parameter::of));
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
