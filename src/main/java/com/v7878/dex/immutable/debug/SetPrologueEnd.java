package com.v7878.dex.immutable.debug;

public final class SetPrologueEnd extends DebugItem {
    public static final SetPrologueEnd INSTANCE = new SetPrologueEnd();

    private SetPrologueEnd() {
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
