package com.v7878.dex.immutable;

import com.v7878.dex.Internal;
import com.v7878.dex.util.Converter;

import java.util.List;
import java.util.Objects;

public final class Dex {
    private final List<ClassDef> classes;

    private Dex(List<ClassDef> classes) {
        this.classes = Objects.requireNonNull(classes);
    }

    /**
     * No copy; an ordered list of classes, duplicates allowed.
     */
    @Internal
    public static Dex raw(List<ClassDef> classes) {
        return new Dex(classes);
    }

    /**
     * An ordered list of classes, duplicates allowed.
     * {@code null} is treated as an empty collection.
     */
    public static Dex of(Iterable<ClassDef> classes) {
        return new Dex(Converter.toList(classes));
    }

    /**
     * An ordered list of classes, duplicates allowed.
     * {@code null} is treated as an empty collection.
     */
    public static Dex of(ClassDef... classes) {
        return new Dex(Converter.toList(classes));
    }

    public List<ClassDef> getClasses() {
        return classes;
    }

    /**
     * First matching class, or {@code null}.
     */
    public ClassDef findClass(TypeId type) {
        Objects.requireNonNull(type);
        for (ClassDef tmp : classes) {
            if (tmp.getType().equals(type)) {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Debug view, not part of the API contract.
     */
    @Override
    public String toString() {
        // TODO: replace with the smali printer
        var builder = new StringBuilder("Dex{classes=[");
        for (int i = 0; i < getClasses().size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append(getClasses().get(i).getType());
        }
        return builder.append("]}").toString();
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
