package com.v7878.dex.iface;

import com.v7878.dex.iface.debug.DebugItem;

import java.util.List;
import java.util.NavigableSet;

public interface MethodImplementation {
    int getRegisterCount();

    List<? extends Instruction> getInstructions();

    NavigableSet<? extends TryBlock> getTryBlocks();

    List<? extends DebugItem> getDebugItems();
}
