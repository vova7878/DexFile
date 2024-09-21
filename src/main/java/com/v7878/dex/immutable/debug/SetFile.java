package com.v7878.dex.immutable.debug;

import java.util.Objects;

public final class SetFile extends DebugItem {
    private final String name;

    private SetFile(String name) {
        this.name = name; // may be null
    }

    public static SetFile of(String name) {
        return new SetFile(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SetFile other
                && Objects.equals(getName(), other.getName());
    }
}
