package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseDex;
import com.v7878.dex.iface.ClassDef;
import com.v7878.dex.iface.Dex;
import com.v7878.dex.util.ItemConverter;

import java.util.List;

public class ImmutableDex extends BaseDex {
    private final List<? extends ImmutableClassDef> classes;

    protected ImmutableDex(Iterable<? extends ClassDef> classes) {
        this.classes = ItemConverter.toList(ImmutableClassDef::of,
                value -> value instanceof ImmutableClassDef, classes);
    }

    public static ImmutableDex of(Iterable<? extends ClassDef> classes) {
        return new ImmutableDex(classes);
    }

    public static ImmutableDex of(Dex other) {
        if (other instanceof ImmutableDex immutable) return immutable;
        return new ImmutableDex(other.getClasses());
    }

    @Override
    public List<? extends ImmutableClassDef> getClasses() {
        return classes;
    }
}
