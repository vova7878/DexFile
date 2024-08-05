package com.v7878.dex.base;

import com.v7878.dex.iface.TryBlock;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseTryBlock implements TryBlock {
    @Override
    public int hashCode() {
        return Objects.hash(getStartAddress(), getUnitCount(), getCatchAllAddress(), getHandlers());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TryBlock other
                && Objects.equals(getStartAddress(), other.getStartAddress())
                && Objects.equals(getUnitCount(), other.getUnitCount())
                && Objects.equals(getCatchAllAddress(), other.getCatchAllAddress())
                && Objects.equals(getHandlers(), other.getHandlers());
    }

    @Override
    public int compareTo(TryBlock other) {
        if (other == this) return 0;
        // Note: TryBlocks should never intersects
        int out = CollectionUtils.compareNonNull(getStartAddress(), other.getStartAddress());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getUnitCount(), other.getUnitCount());
    }
}
