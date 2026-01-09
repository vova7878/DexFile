package com.v7878.dex.analysis;

import com.v7878.collections.IntSet;
import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.iface.PayloadInstruction;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Formatter;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public final class Position {
    public static final int RESULT_REGISTER = -2; // -1 for high wide half
    public static final int EXCEPTION_REGISTER = -3;

    public record Transition(int address, TypeId exception) implements Comparable<Transition> {
        public boolean isCatch() {
            return exception != null;
        }

        public boolean isNormal() {
            return !isCatch();
        }

        @Override
        public int compareTo(Transition other) {
            if (other == this) return 0;
            int out = Integer.compare(address(), other.address());
            if (out != 0) return out;
            return CollectionUtils.compareNullable(exception(), other.exception());
        }

        @Override
        public String toString() {
            return Formatter.unsignedHex(address) + (exception == null ? "" : " -> " + exception);
        }
    }

    private final Instruction instruction;
    private int flags;
    private PayloadInstruction payload;
    private ProtoId proto;
    private final int index, address;
    private final int register_count;
    private final RegisterLine before, after;

    // TODO
    final NavigableSet<Transition> predecessors;

    final NavigableSet<Transition> successors;

    private final IntSet inputs;

    private final IntSet outputs;

    /* package */ Position(Instruction instruction,
                           int register_count,
                           int index, int address) {
        this.instruction = instruction;
        this.index = index;
        this.address = address;
        this.register_count = register_count;
        this.before = new RegisterLine(register_count);
        this.after = new RegisterLine(register_count);
        this.predecessors = new TreeSet<>();
        this.successors = new TreeSet<>();
        this.inputs = new IntSet();
        this.outputs = new IntSet();
    }

    public int getRegisterCount() {
        return register_count;
    }

    /* package */ void copy(RegisterLine line) {
        before.copy(line);
    }

    /* package */ boolean merge(TypeResolver resolver, RegisterLine line) {
        return before.merge(resolver, address, line);
    }

    /* package */ void passRegs() {
        after.copy(before);
    }

    /* package */ void freeze() {
        assert instruction.getOpcode().hasPayload() == (payload != null);
        assert instruction.getOpcode().isVariableRegister() == (proto != null);
        inputs.freeze();
        outputs.freeze();
    }

    public boolean isFirst() {
        return index == 0;
    }

    public boolean isStructurallyReachable() {
        return isFirst() || !predecessors.isEmpty();
    }

    private static final int REACHABLE = 0x1;

    /* package */ void setRuntimeReachable() {
        flags |= REACHABLE;
    }

    public boolean isRuntimeReachable() {
        assert isStructurallyReachable();
        return (flags & REACHABLE) != 0;
    }

    @SuppressWarnings("unchecked")
    public <I extends Instruction> I instruction() {
        return (I) instruction;
    }

    public Opcode opcode() {
        return instruction.getOpcode();
    }

    public PayloadInstruction payload() {
        return payload;
    }

    /* package */ void payload(PayloadInstruction payload) {
        assert instruction.getOpcode().hasPayload();
        this.payload = Objects.requireNonNull(payload);
    }

    public ProtoId accessProto() {
        return proto;
    }

    /* package */ void accessProto(ProtoId proto) {
        assert instruction.getOpcode().isVariableRegister();
        this.proto = Objects.requireNonNull(proto);
    }

    public RegisterLine before() {
        return before;
    }

    public RegisterLine after() {
        return after;
    }

    public int index() {
        return index;
    }

    public int address() {
        return address;
    }

    public NavigableSet<Transition> predecessors() {
        return Collections.unmodifiableNavigableSet(predecessors);
    }

    public NavigableSet<Transition> successors() {
        return Collections.unmodifiableNavigableSet(successors);
    }

    public IntSet inputs() {
        return inputs;
    }

    /* package */ void input(int reg) {
        if (reg != RESULT_REGISTER && reg != EXCEPTION_REGISTER) {
            Objects.checkIndex(reg, register_count);
        }
        // TODO: check bounds
        inputs.add(reg);
    }

    /* package */ void wideInput(int reg) {
        if (reg != RESULT_REGISTER && reg != EXCEPTION_REGISTER) {
            Objects.checkFromIndexSize(reg, 2, register_count);
        }
        // TODO: check bounds
        inputs.add(reg);
        inputs.add(reg + 1);
    }

    /* package */ void rangeInput(int start_reg, int count) {
        for (int i = 0; i < count; i++) {
            // TODO: add range
            input(start_reg + i);
        }
    }

    public IntSet outputs() {
        return outputs;
    }

    /* package */ void output(int reg) {
        if (reg != RESULT_REGISTER && reg != EXCEPTION_REGISTER) {
            Objects.checkIndex(reg, register_count);
        }
        // TODO: check bounds
        outputs.add(reg);
    }

    /* package */ void wideOutput(int reg) {
        if (reg != RESULT_REGISTER && reg != EXCEPTION_REGISTER) {
            Objects.checkFromIndexSize(reg, 2, register_count);
        }
        // TODO: check bounds
        outputs.add(reg);
        outputs.add(reg + 1);
    }

    // for example const 0 to register with zero constant
    // or move of any kind to self
    // or goto/if/switch to next
    // or cast to itself
    public boolean isNopExact() {
        // TODO
        return false;
    }

    // for example cast to j.l.Object
    // or cast zero to anything
    public boolean isNarrowingNop() {
        // TODO
        return false;
    }

    public String describe() {
        return String.format("""
                        %08X: %s
                        structurally reachable: %s
                        runtime reachable: %s
                        inputs: %s
                        outputs: %s
                        state before: %s
                        state after: %s
                        predecessors: %s
                        successors: %s
                        """.trim(),
                address, instruction, isStructurallyReachable(), isRuntimeReachable(),
                Formatter.registers(inputs), Formatter.registers(outputs),
                before.describe(), after.describe(), predecessors, successors);
    }
}
