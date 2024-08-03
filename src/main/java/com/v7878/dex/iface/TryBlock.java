package com.v7878.dex.iface;

import java.util.List;

public interface TryBlock extends Comparable<TryBlock> {
    int getStartAddress();

    int getUnitCount();

    List<? extends ExceptionHandler> getHandlers();

    Integer getCatchAllAddress();
}
