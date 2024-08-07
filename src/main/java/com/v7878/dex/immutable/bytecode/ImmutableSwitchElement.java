package com.v7878.dex.immutable.bytecode;

import com.v7878.dex.base.bytecode.BaseSwitchElement;
import com.v7878.dex.iface.bytecode.SwitchElement;

public class ImmutableSwitchElement extends BaseSwitchElement {
    private final int key;
    private final int offset;

    protected ImmutableSwitchElement(int key, int offset) {
        this.key = key;
        this.offset = offset;
    }

    public static ImmutableSwitchElement of(int key, int offset) {
        return new ImmutableSwitchElement(key, offset);
    }

    public static ImmutableSwitchElement of(SwitchElement other) {
        if (other instanceof ImmutableSwitchElement immutable) return immutable;
        return new ImmutableSwitchElement(other.getKey(), other.getOffset());
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public int getOffset() {
        return offset;
    }
}
