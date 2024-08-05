package com.v7878.dex.base;

import com.v7878.dex.iface.MethodImplementation;

import java.util.Objects;

public abstract class BaseMethodImplementation implements MethodImplementation {
    @Override
    public int hashCode() {
        return Objects.hash(getRegisterCount(), getInstructions(), getTryBlocks(), getDebugItems());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodImplementation other
                && getRegisterCount() == other.getRegisterCount()
                && Objects.equals(getInstructions(), other.getInstructions())
                && Objects.equals(getTryBlocks(), other.getTryBlocks())
                && Objects.equals(getDebugItems(), other.getDebugItems());
    }
}
