package com.v7878.dex.immutable;

import com.v7878.dex.Internal;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.MemberUtils;
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
            int register_count, List<Instruction> instructions,
            NavigableSet<TryBlock> try_blocks, List<DebugItem> debug_items) {
        this.register_count = Preconditions.checkMethodRegisterCount(register_count);
        this.instructions = Objects.requireNonNull(instructions);
        this.try_blocks = Objects.requireNonNull(try_blocks);
        checkTryBlocks(instructions, try_blocks);
        // TODO: deduplicate and remove unused AdvancePC items
        this.debug_items = Objects.requireNonNull(debug_items);
    }

    /**
     * Try blocks and handler addresses must lie inside the code: the sum
     * of the instruction unit counts (payload instructions included) is
     * the code size, every block must end at or before it, and every
     * handler address must be strictly below it.
     */
    private static void checkTryBlocks(List<Instruction> instructions,
                                       NavigableSet<TryBlock> try_blocks) {
        if (try_blocks.isEmpty()) {
            return;
        }
        int total_units = 0;
        for (var instruction : instructions) {
            total_units += instruction.getUnitCount();
        }
        for (var block : try_blocks) {
            if (block.getEndAddress() > total_units) {
                throw new IllegalArgumentException(String.format(
                        "Try block at %d with unit count %d exceeds the code size of %d units",
                        block.getStartAddress(), block.getUnitCount(), total_units));
            }
            for (var handler : block.getHandlers()) {
                if (handler.getAddress() >= total_units) {
                    throw new IllegalArgumentException(String.format(
                            "Handler for %s at address %d is beyond the code size of %d units",
                            handler.getExceptionType(), handler.getAddress(), total_units));
                }
            }
            var catch_all = block.getCatchAllAddress();
            if (catch_all != null && catch_all >= total_units) {
                throw new IllegalArgumentException(String.format(
                        "Catch-all handler at address %d is beyond the code size of %d units",
                        catch_all, total_units));
            }
        }
        if (try_blocks.size() > 1) {
            var iterator = try_blocks.iterator();
            var prev = iterator.next();
            while (iterator.hasNext()) {
                var curr = iterator.next();
                if (curr.getStartAddress() < prev.getEndAddress()) {
                    throw new IllegalArgumentException(
                            String.format("Out of order try block (%s, %<s + %s)",
                                    curr.getStartAddress(), curr.getUnitCount()));
                }
                prev = curr;
            }
        }
    }

    @Internal
    public static MethodImplementation raw(int register_count, List<Instruction> instructions,
                                           NavigableSet<TryBlock> try_blocks, List<DebugItem> debug_items) {
        return new MethodImplementation(register_count, instructions, try_blocks, debug_items);
    }

    /**
     * {@code null} is treated as an empty collection.
     */
    public static MethodImplementation of(int register_count, Iterable<Instruction> instructions,
                                          Iterable<TryBlock> try_blocks, Iterable<DebugItem> debug_items) {
        return new MethodImplementation(register_count,
                Converter.toList(instructions),
                MemberUtils.toTryBlockSet(try_blocks),
                Converter.toList(debug_items));
    }

    public int getRegisterCount() {
        return register_count;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public NavigableSet<TryBlock> getTryBlocks() {
        return try_blocks;
    }

    public TryBlock getTryBlock(int address) {
        return MemberUtils.findTryBlock(try_blocks, address);
    }

    public List<DebugItem> getDebugItems() {
        return debug_items;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegisterCount(), getInstructions(),
                CollectionUtils.setHashCode(getTryBlocks()), getDebugItems());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodImplementation other
                && getRegisterCount() == other.getRegisterCount()
                && Objects.equals(getInstructions(), other.getInstructions())
                && CollectionUtils.setEquals(getTryBlocks(), other.getTryBlocks())
                && Objects.equals(getDebugItems(), other.getDebugItems());
    }

    /**
     * Debug view, not part of the API contract.
     */
    @Override
    public String toString() {
        // TODO: replace with the smali printer
        return "MethodImplementation{registers=" + getRegisterCount()
                + " instructions=" + getInstructions().size()
                + " try-blocks=" + getTryBlocks().size()
                + " debug-items=" + getDebugItems().size()
                + "}";
    }
}
