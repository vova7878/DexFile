package com.v7878.dex.analysis;

import static com.v7878.dex.Opcode.AGET;
import static com.v7878.dex.Opcode.AGET_BOOLEAN;
import static com.v7878.dex.Opcode.AGET_BYTE;
import static com.v7878.dex.Opcode.AGET_CHAR;
import static com.v7878.dex.Opcode.AGET_SHORT;
import static com.v7878.dex.Opcode.AGET_WIDE;

import com.v7878.collections.IntMap;
import com.v7878.collections.IntSet;
import com.v7878.collections.LongSet;
import com.v7878.dex.Opcode;
import com.v7878.dex.analysis.AnalyzedMethod.ValueUsage;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.InstructionN1c;
import com.v7878.dex.immutable.bytecode.InstructionN2x;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.WideLiteralInstruction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

class AnalysisUtils {
    private static void iterate_sources(IntFunction<Position> code_map, Register reg,
                                        BiConsumer<Position, Register> action) {
        Objects.requireNonNull(reg);
        Objects.requireNonNull(action);
        if (reg.isUndefined()) {
            // An undefined value has no origin to walk back to
            throw new IllegalArgumentException("The register is undefined");
        }

        var todo = new ArrayDeque<Register>();
        var done = new HashSet<Register>();

        todo.add(reg);
        while (!todo.isEmpty()) {
            var tmp = todo.poll();
            if (done.contains(tmp)) {
                continue;
            }
            done.add(tmp);

            // Only the value passed in by the caller can be undefined: a
            // value reachable through register sources always has an origin
            assert !tmp.isUndefined();

            var ident = tmp.getSource();

            if (ident.isParameter()) {
                action.accept(null, tmp);
                continue;
            }

            var pos = code_map.apply(ident.address());
            if (ident.produced() && pos.outputs().contains(ident.slot())) {
                // The instruction at this address produced the value
                action.accept(pos, tmp);
            } else if (pos.isFirst()) {
                // The entry position has no predecessors: a value that is
                // not produced there flows in from the parameter line
                todo.add(pos.before().at(ident.slot()));
            } else {
                // The value was joined here or the instruction only refined
                // the value flowing into it; before() is exactly the merge
                // of the incoming edges, so those edges are all that is left
                for (var trans : pos.predecessors()) {
                    var parent = code_map.apply(trans.address());
                    var line = trans.isNormal() ? parent.after() : parent.before();
                    todo.add(line.at(ident.slot()));
                }
            }
        }
    }

    public static Set<Register> value_sources(IntFunction<Position> code_map, Register reg) {
        var values = new LinkedHashSet<Register>();
        iterate_sources(code_map, reg, (pos, target) -> values.add(target));
        return Collections.unmodifiableSet(values);
    }

    public static IntSet constant_values(IntFunction<Position> code_map, Register reg) {
        var kind = reg.getConstantKind();
        if (kind == null) {
            throw new IllegalArgumentException("Not a constant register " + reg);
        }
        if (!kind.isInt()) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected register type %s, expected integral constant", reg));
        }

        var values = new IntSet();
        iterate_sources(code_map, reg, (pos, target) -> {
            var insn = pos.instruction();
            if (insn instanceof LiteralInstruction src) {
                values.add(src.getLiteral());
            } else {
                // The value is unknown here
                assert insn.getOpcode() == AGET
                        || insn.getOpcode() == AGET_BOOLEAN
                        || insn.getOpcode() == AGET_BYTE
                        || insn.getOpcode() == AGET_CHAR
                        || insn.getOpcode() == AGET_SHORT;
            }
        });

        return values.freeze();
    }

    public static LongSet wide_constant_values(IntFunction<Position> code_map, Register reg) {
        var kind = reg.getConstantKind();
        if (kind == null) {
            throw new IllegalArgumentException("Not a constant register " + reg);
        }
        if (!kind.isWide()) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected register type %s, expected wide constant", reg));
        }

        var values = new LongSet();
        iterate_sources(code_map, reg, (pos, target) -> {
            Instruction insn = pos.instruction();
            if (insn instanceof WideLiteralInstruction src) {
                values.add(src.getWideLiteral());
            } else if (insn instanceof LiteralInstruction src) {
                // const-wide/16 and const-wide/32
                values.add(src.getLiteral());
            } else {
                // The value is unknown here
                assert insn.getOpcode() == AGET_WIDE;
            }
        });

        return values.freeze();
    }

    public static Set<Object> ref_constant_values(IntFunction<Position> code_map, Register reg) {
        var kind = reg.getConstantKind();
        if (kind == null) {
            throw new IllegalArgumentException("Not a constant register " + reg);
        }
        if (!kind.isRef()) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected register type %s, expected reference constant", reg));
        }

        var values = new LinkedHashSet<>();
        iterate_sources(code_map, reg, (pos, target) -> {
            if (target.isZeroOrNull()) {
                values.add(null);
            } else {
                InstructionN1c src = pos.instruction();
                values.add(src.getReference1());
            }
        });

        return Collections.unmodifiableSet(values);
    }

    /**
     * Checks whether the register is read on some execution path
     * starting at the instruction at address {@code start}.
     * <p>
     * A register without content (undefined, conflict, wide piece)
     * ends the path. An overwritten register skips the normal edges:
     * catch edges carry the state before the instruction, where the
     * overwrite did not happen.
     *
     * @param start address of the instruction the branch starts at
     * @param slot  index of the inspected register
     * @return true if the register is read in some branch from start
     */
    public static boolean is_used_in_branch(IntFunction<Position> code_map, int start, int slot) {
        BitSet todo = new BitSet();
        BitSet done = new BitSet();
        todo.set(start);
        for (int i; (i = todo.nextSetBit(0)) >= 0; ) {
            todo.clear(i);
            if (done.get(i)) {
                continue;
            }
            done.set(i);
            var position = code_map.apply(i);
            if (!position.before().at(slot).hasContent()) {
                continue;
            }
            if (position.inputs().contains(slot)) {
                // a move counts as usage
                return true;
            }
            // normal edges only: an overwritten register lost our value
            var skip_normal = position.outputs().contains(slot);
            for (var branch : position.successors()) {
                if (skip_normal && branch.isNormal()) {
                    continue;
                }
                todo.set(branch.address());
            }
        }
        return false;
    }

    private record Move(int src, int dst, boolean wide) {
    }

    /**
     * Finds all usages of the value held in register {@code slot}
     * when the instruction at address {@code start} is reached; the
     * start instruction itself counts when it reads the register.
     * A move does not end the search — the value lives on in the
     * destination — so to find the users of an instruction's output,
     * search from every position it can jump to.
     * <p>
     * The search walks the successors tracking (instruction,
     * register) pairs. Only a move changes the value's register:
     * move carries it to the destination, move-wide the lo+hi pair
     * ({@code src -> dst, src+1 -> dst+1}); every other instruction
     * writes a new value, so the un-overwritten register is the only
     * other continuation. A usage is the tracked register listed in
     * {@code inputs()}. A merge is not a usage — the merged value
     * contains the tracked contribution and the search passes it by
     * register — and a branch refinement keeps the register. A
     * check-cast reads the original value (a usage) and writes a new
     * one, so usages of the cast result do not count. An overwritten
     * register ends the path on normal edges; catch edges carry the
     * state before the instruction, where the overwrite did not
     * happen. A register without content (undefined, conflict, wide
     * piece) cannot be used. Negative pseudo-registers are never
     * tracked: only the start register and move destinations are.
     *
     * @param code_map the method's positions: instruction address -> position
     * @param start    address of the instruction the search starts at
     * @param slot     index of the register holding the value at start
     * @return usages sorted by instruction address, then register
     */
    public static List<ValueUsage> value_usages(IntFunction<Position> code_map, int start, int slot) {
        record State(int address, int slot) {
        }

        var first = code_map.apply(start);
        if (!first.before().at(slot).hasContent()) {
            return List.of();
        }
        var usages = new ArrayList<ValueUsage>();
        var todo = new ArrayDeque<State>();
        // visited states: address -> bits of visited registers
        var done = new IntMap<BitSet>();
        todo.add(new State(start, slot));
        while (!todo.isEmpty()) {
            var state = todo.poll();
            var visited = done.get(state.address());
            if (visited == null) {
                visited = new BitSet();
                done.put(state.address(), visited);
            }
            if (visited.get(state.slot())) {
                continue;
            }
            visited.set(state.slot());
            var position = code_map.apply(state.address());
            var value = position.before().at(state.slot());
            if (!value.hasContent()) {
                continue;
            }
            if (position.inputs().contains(state.slot())) {
                usages.add(new ValueUsage(position, state.slot()));
            }
            var overwritten = position.outputs().contains(state.slot());
            // only a move carries the value to another register
            var move = move_of(position);
            for (var transition : position.successors()) {
                if (transition.isCatch()) {
                    // a catch edge carries the state before the
                    // instruction: the overwrite did not happen
                    todo.add(new State(transition.address(), state.slot()));
                    continue;
                }
                // the value stays in the un-overwritten register...
                if (!overwritten) {
                    todo.add(new State(transition.address(), state.slot()));
                }
                // ...and a move carries it to the destination
                // (move-wide: the whole lo+hi pair)
                if (move != null) {
                    if (state.slot() == move.src()) {
                        todo.add(new State(transition.address(), move.dst()));
                    } else if (move.wide() && state.slot() == move.src() + 1) {
                        todo.add(new State(transition.address(), move.dst() + 1));
                    }
                }
            }
        }
        usages.sort(Comparator
                .comparingInt((ValueUsage a) -> a.position().address())
                .thenComparingInt(ValueUsage::slot));
        return List.copyOf(usages);
    }

    /**
     * This position's move, or null for a non-move instruction:
     * only moves transfer the value to the written register.
     */
    private static Move move_of(Position position) {
        var opcode = position.opcode();
        if (!is_move(opcode)) {
            return null;
        }
        InstructionN2x insn = position.instruction();
        return new Move(insn.getRegister2(), insn.getRegister1(),
                is_wide_move(opcode));
    }

    /**
     * The move family: the only instructions that transfer the value
     * to the written register — the analysis calls
     * RegisterLine.copy/copyWide only for them.
     */
    private static boolean is_move(Opcode opcode) {
        return opcode == Opcode.MOVE
                || opcode == Opcode.MOVE_FROM16
                || opcode == Opcode.MOVE_16
                || opcode == Opcode.MOVE_OBJECT
                || opcode == Opcode.MOVE_OBJECT_FROM16
                || opcode == Opcode.MOVE_OBJECT_16
                || opcode == Opcode.MOVE_WIDE
                || opcode == Opcode.MOVE_WIDE_FROM16
                || opcode == Opcode.MOVE_WIDE_16;
    }

    /**
     * move-wide: moves the lo+hi register pair.
     */
    private static boolean is_wide_move(Opcode opcode) {
        return opcode == Opcode.MOVE_WIDE
                || opcode == Opcode.MOVE_WIDE_FROM16
                || opcode == Opcode.MOVE_WIDE_16;
    }
}
