package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseMethodImplementation;
import com.v7878.dex.iface.MethodImplementation;
import com.v7878.dex.iface.TryBlock;
import com.v7878.dex.iface.bytecode.Instruction;
import com.v7878.dex.iface.debug.DebugItem;
import com.v7878.dex.immutable.bytecode.ImmutableInstruction;
import com.v7878.dex.immutable.debug.ImmutableDebugItem;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.NavigableSet;

public class ImmutableMethodImplementation extends BaseMethodImplementation {
    private final int register_count;
    private final List<? extends ImmutableInstruction> instructions;
    private final NavigableSet<? extends ImmutableTryBlock> try_blocks;
    private final List<? extends ImmutableDebugItem> debug_items;

    protected ImmutableMethodImplementation(
            int register_count, Iterable<? extends Instruction> instructions,
            Iterable<? extends TryBlock> try_blocks, Iterable<? extends DebugItem> debug_items) {
        this.register_count = Preconditions.checkMethodRegisterCount(register_count);
        this.instructions = ItemConverter.toList(ImmutableInstruction::of,
                value -> value instanceof ImmutableInstruction, instructions);
        this.try_blocks = ItemConverter.toNavigableSet(ImmutableTryBlock::of,
                value -> value instanceof ImmutableTryBlock, try_blocks);
        // TODO: deduplicate and remove unused AdvancePC items
        this.debug_items = ItemConverter.toList(ImmutableDebugItem::of,
                value -> value instanceof ImmutableDebugItem, debug_items);
    }

    public static ImmutableMethodImplementation of(
            int register_count, Iterable<? extends Instruction> instructions,
            Iterable<? extends TryBlock> try_blocks, Iterable<? extends DebugItem> debug_items) {
        return new ImmutableMethodImplementation(register_count, instructions, try_blocks, debug_items);
    }

    public static ImmutableMethodImplementation of(MethodImplementation other) {
        if (other instanceof ImmutableMethodImplementation immutable) return immutable;
        return new ImmutableMethodImplementation(other.getRegisterCount(),
                other.getInstructions(), other.getTryBlocks(), other.getDebugItems());
    }

    @Override
    public int getRegisterCount() {
        return register_count;
    }

    @Override
    public List<? extends ImmutableInstruction> getInstructions() {
        return instructions;
    }

    @Override
    public NavigableSet<? extends ImmutableTryBlock> getTryBlocks() {
        return try_blocks;
    }

    @Override
    public List<? extends ImmutableDebugItem> getDebugItems() {
        return debug_items;
    }
}
