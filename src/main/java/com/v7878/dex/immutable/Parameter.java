package com.v7878.dex.immutable;

import static com.v7878.dex.util.CollectionUtils.toUnmodifiableList;

import com.v7878.dex.Internal;
import com.v7878.dex.util.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.stream.StreamSupport;

public final class Parameter implements Annotatable {
    private final TypeId type;
    private final String name;
    private final NavigableSet<Annotation> annotations;

    private Parameter(TypeId type, String name, NavigableSet<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.name = name; // may be null
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Internal
    public static Parameter raw(TypeId type, String name, NavigableSet<Annotation> annotations) {
        return new Parameter(type, name, annotations);
    }

    public static Parameter of(TypeId type, String name, Iterable<Annotation> annotations) {
        return new Parameter(type, name, Converter.toNavigableSet(annotations));
    }

    public static Parameter of(TypeId type, String name) {
        return new Parameter(type, name, Collections.emptyNavigableSet());
    }

    public static Parameter of(TypeId type) {
        return of(type, null);
    }

    public static List<Parameter> listOf(Iterable<TypeId> types) {
        // TODO: improve performance
        return toUnmodifiableList(StreamSupport
                .stream(types.spliterator(), false)
                .map(Parameter::of));
    }

    public static List<Parameter> listOf(TypeId... types) {
        // TODO: improve performance
        return listOf(Arrays.asList(types));
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
