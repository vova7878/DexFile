package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.Objects;

public final class TryBlock implements Comparable<TryBlock> {
    private final int start_address;
    private final int unit_count;
    private final List<ExceptionHandler> handlers;
    private final Integer catch_all_address;

    private TryBlock(int start_address, int unit_count, Integer catch_all_address,
                     Iterable<ExceptionHandler> handlers) {
        this.start_address = Preconditions.checkCodeAddress(start_address);
        this.unit_count = Preconditions.checkUnitCount(unit_count);
        // TODO: check what catch_all_address != null or handlers not empty
        this.handlers = ItemConverter.toList(handlers);
        this.catch_all_address = catch_all_address == null ? null :
                Preconditions.checkCodeAddress(catch_all_address);
    }

    public static TryBlock of(int start_address, int unit_count, Integer catch_all_address,
                              Iterable<ExceptionHandler> handlers) {
        return new TryBlock(start_address, unit_count, catch_all_address, handlers);
    }

    // TODO?: of(int start_address, int unit_count, Integer catch_all_address, ExceptionHandler... handlers)
    // TODO?: of(int start_address, int unit_count, int catch_all_address)

    public int getStartAddress() {
        return start_address;
    }

    public int getUnitCount() {
        return unit_count;
    }

    public List<ExceptionHandler> getHandlers() {
        return handlers;
    }

    public Integer getCatchAllAddress() {
        return catch_all_address;
    }

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
