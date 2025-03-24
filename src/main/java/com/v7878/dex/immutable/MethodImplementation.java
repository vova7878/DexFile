package com.v7878.dex.immutable;

import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;

public final class MethodImplementation {
    private final int register_count;
    private final List<Instruction> instructions;
    private final NavigableSet<TryBlock> try_blocks;
    private final List<DebugItem> debug_items;

    private MethodImplementation(
            int register_count, Iterable<Instruction> instructions,
            Iterable<TryBlock> try_blocks, Iterable<DebugItem> debug_items) {
        this.register_count = Preconditions.checkMethodRegisterCount(register_count);
        this.instructions = ItemConverter.toList(instructions);
        // TODO: try blocks must not overlap
        this.try_blocks = ItemConverter.toNavigableSet(try_blocks);
        // TODO: deduplicate and remove unused AdvancePC items
        this.debug_items = ItemConverter.toList(debug_items);
    }

    public static MethodImplementation of(int register_count, Iterable<Instruction> instructions,
                                          Iterable<TryBlock> try_blocks, Iterable<DebugItem> debug_items) {
        return new MethodImplementation(register_count, instructions, try_blocks, debug_items);
    }

    // TODO?: Add simpler constructor?

    public int getRegisterCount() {
        return register_count;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public NavigableSet<TryBlock> getTryBlocks() {
        return try_blocks;
    }

    public List<DebugItem> getDebugItems() {
        return debug_items;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegisterCount(), getInstructions(), getTryBlocks(), getDebugItems());
    }

    public int hashCodeNoDebug() {
        return Objects.hash(getRegisterCount(), getInstructions(), getTryBlocks());
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

    public boolean equalsNoDebug(MethodImplementation other) {
        if (other == this) return true;
        return getRegisterCount() == other.getRegisterCount()
                && Objects.equals(getInstructions(), other.getInstructions())
                && Objects.equals(getTryBlocks(), other.getTryBlocks());
    }
}
