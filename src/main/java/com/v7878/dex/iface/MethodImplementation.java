package com.v7878.dex.iface;

import com.v7878.dex.iface.bytecode.Instruction;
import com.v7878.dex.iface.debug.DebugItem;

import java.util.List;
import java.util.Set;

public interface MethodImplementation {
    int getRegisterCount();

    List<? extends Instruction> getInstructions();

    Set<? extends TryBlock> getTryBlocks();

    List<? extends DebugItem> getDebugItems();
}
