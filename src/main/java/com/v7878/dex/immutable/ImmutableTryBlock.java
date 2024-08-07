package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseTryBlock;
import com.v7878.dex.iface.ExceptionHandler;
import com.v7878.dex.iface.TryBlock;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;

public class ImmutableTryBlock extends BaseTryBlock {
    private final int start_address;
    private final int unit_count;
    private final List<? extends ImmutableExceptionHandler> handlers;
    private final Integer catch_all_address;

    protected ImmutableTryBlock(int start_addres, int unit_count,
                                Iterable<? extends ExceptionHandler> handlers,
                                Integer catch_all_address) {
        this.start_address = Preconditions.checkCodeAddress(start_addres);
        this.unit_count = Preconditions.checkUnitCount(unit_count);
        this.handlers = ItemConverter.toList(ImmutableExceptionHandler::of,
                value -> value instanceof ImmutableExceptionHandler, handlers);
        this.catch_all_address = catch_all_address == null ? null :
                Preconditions.checkCodeAddress(catch_all_address);
    }

    public static ImmutableTryBlock of(int start_addres, int unit_count,
                                       Iterable<? extends ExceptionHandler> handlers,
                                       Integer catch_all_address) {
        return new ImmutableTryBlock(start_addres, unit_count, handlers, catch_all_address);
    }

    public static ImmutableTryBlock of(TryBlock other) {
        if (other instanceof ImmutableTryBlock immutable) return immutable;
        return new ImmutableTryBlock(other.getStartAddress(), other.getUnitCount(),
                other.getHandlers(), other.getCatchAllAddress());
    }

    @Override
    public int getStartAddress() {
        return start_address;
    }

    @Override
    public int getUnitCount() {
        return unit_count;
    }

    @Override
    public List<? extends ImmutableExceptionHandler> getHandlers() {
        return handlers;
    }

    @Override
    public Integer getCatchAllAddress() {
        return catch_all_address;
    }
}
