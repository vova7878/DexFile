package com.v7878.dex.immutable.debug;

public final class SetEpilogueBegin extends DebugItem {
    public static final SetEpilogueBegin INSTANCE = new SetEpilogueBegin();

    private SetEpilogueBegin() {
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
