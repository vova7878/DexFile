package com.v7878.dex.immutable.debug;

import java.util.Objects;

public class SetPrologueEnd extends DebugItem {
    public static final SetPrologueEnd INSTANCE = new SetPrologueEnd();

    protected SetPrologueEnd() {
    }

    public static SetPrologueEnd of(SetPrologueEnd other) {
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
        return obj instanceof SetPrologueEnd;
    }
}
