package com.v7878.dex.immutable.bytecode;

import java.util.Objects;

public class SwitchElement {
    private final int key;
    private final int offset;

    protected SwitchElement(int key, int offset) {
        this.key = key;
        this.offset = offset;
    }

    public static SwitchElement of(int key, int offset) {
        return new SwitchElement(key, offset);
    }

    public int getKey() {
        return key;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getOffset());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof SwitchElement other
                && getKey() == other.getKey()
                && getOffset() == other.getOffset();
    }
}
