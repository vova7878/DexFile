package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseSetFile;
import com.v7878.dex.iface.debug.SetFile;

public class ImmutableSetFile extends BaseSetFile implements ImmutableDebugItem {
    private final String name;

    protected ImmutableSetFile(String name) {
        this.name = name; // may be null
    }

    public static ImmutableSetFile of(String name) {
        return new ImmutableSetFile(name);
    }

    public static ImmutableSetFile of(SetFile other) {
        if (other instanceof ImmutableSetFile immutable) return immutable;
        return new ImmutableSetFile(other.getName());
    }

    @Override
    public String getName() {
        return name;
    }
}
