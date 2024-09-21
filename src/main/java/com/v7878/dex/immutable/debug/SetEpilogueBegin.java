package com.v7878.dex.immutable.debug;

import java.util.Objects;

public class SetEpilogueBegin extends DebugItem {
    public static final SetEpilogueBegin INSTANCE = new SetEpilogueBegin();

    protected SetEpilogueBegin() {
    }

    public static SetEpilogueBegin of(SetEpilogueBegin other) {
        Objects.requireNonNull(other);
        return INSTANCE;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SetEpilogueBegin;
    }
}
