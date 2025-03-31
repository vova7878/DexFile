package com.v7878.dex.immutable;

import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class TryBlock implements Comparable<TryBlock> {
    private final int start_address;
    private final int unit_count;
    private final Integer catch_all_address;
    private final List<ExceptionHandler> handlers;

    private TryBlock(int start_address, int unit_count, Integer catch_all_address,
                     Iterable<ExceptionHandler> handlers) {
        this.start_address = Preconditions.checkCodeAddress(start_address);
        this.unit_count = Preconditions.checkUnitCount(unit_count);
        this.handlers = ItemConverter.toList(handlers);
        this.catch_all_address = catch_all_address == null ? null :
                Preconditions.checkCodeAddress(catch_all_address);
        if (this.handlers.isEmpty() && this.catch_all_address == null) {
            throw new IllegalArgumentException("Empty try block");
        }
    }

    public static TryBlock of(int start_address, int unit_count,
                              Integer catch_all_address,
                              Iterable<ExceptionHandler> handlers) {
        return new TryBlock(start_address, unit_count,
                catch_all_address, handlers);
    }

    public static TryBlock of(int start_address, int unit_count,
                              Integer catch_all_address,
                              ExceptionHandler... handlers) {
        return new TryBlock(start_address, unit_count,
                catch_all_address, Arrays.asList(handlers));
    }

    public static TryBlock of(int start_address, int unit_count, int catch_all_address) {
        return new TryBlock(start_address, unit_count,
                catch_all_address, null);
    }

    public int getStartAddress() {
        return start_address;
    }

    public int getUnitCount() {
        return unit_count;
    }

    public Integer getCatchAllAddress() {
        return catch_all_address;
    }

    public List<ExceptionHandler> getHandlers() {
        return handlers;
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
