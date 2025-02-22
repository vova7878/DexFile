package com.v7878.dex.immutable;

import com.v7878.dex.util.ItemConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Dex {
    private final List<ClassDef> classes;

    private Dex(Iterable<ClassDef> classes) {
        // TODO: check if there are classes with the same type
        this.classes = ItemConverter.toList(classes);
    }

    public static Dex of(Iterable<ClassDef> classes) {
        return new Dex(classes);
    }

    public static Dex of(ClassDef... classes) {
        return of(Arrays.asList(classes));
    }

    public List<ClassDef> getClasses() {
        return classes;
    }

    public ClassDef findClass(TypeId type) {
        Objects.requireNonNull(type);
        for (ClassDef tmp : classes) {
            if (tmp.getType().equals(type)) {
                return tmp;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClasses());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Dex other
                && Objects.equals(getClasses(), other.getClasses());
    }
}
