/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.v7878.dex.bytecode;

import com.v7878.dex.CallSiteId;
import com.v7878.dex.CatchHandler;
import com.v7878.dex.CatchHandlerElement;
import com.v7878.dex.CodeItem;
import com.v7878.dex.FieldId;
import com.v7878.dex.MethodHandleItem;
import com.v7878.dex.MethodId;
import com.v7878.dex.ProtoId;
import com.v7878.dex.TryItem;
import com.v7878.dex.TypeId;
import com.v7878.dex.bytecode.Format.ArrayPayload;
import com.v7878.dex.bytecode.Format.Format10t;
import com.v7878.dex.bytecode.Format.Format10x;
import com.v7878.dex.bytecode.Format.Format11n;
import com.v7878.dex.bytecode.Format.Format11x;
import com.v7878.dex.bytecode.Format.Format12x;
import com.v7878.dex.bytecode.Format.Format20t;
import com.v7878.dex.bytecode.Format.Format21c;
import com.v7878.dex.bytecode.Format.Format21ih;
import com.v7878.dex.bytecode.Format.Format21lh;
import com.v7878.dex.bytecode.Format.Format21t21s;
import com.v7878.dex.bytecode.Format.Format22b;
import com.v7878.dex.bytecode.Format.Format22c;
import com.v7878.dex.bytecode.Format.Format22t22s;
import com.v7878.dex.bytecode.Format.Format22x;
import com.v7878.dex.bytecode.Format.Format23x;
import com.v7878.dex.bytecode.Format.Format30t;
import com.v7878.dex.bytecode.Format.Format31c;
import com.v7878.dex.bytecode.Format.Format31i31t;
import com.v7878.dex.bytecode.Format.Format32x;
import com.v7878.dex.bytecode.Format.Format35c35ms35mi;
import com.v7878.dex.bytecode.Format.Format3rc3rms3rmi;
import com.v7878.dex.bytecode.Format.Format45cc;
import com.v7878.dex.bytecode.Format.Format4rcc;
import com.v7878.dex.bytecode.Format.Format51l;
import com.v7878.dex.bytecode.Format.PackedSwitchPayload;
import com.v7878.dex.bytecode.Format.SparseSwitchPayload;
import com.v7878.misc.Checks;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CodeBuilder {

    private static class InternalLabel {
    }

    private class BuilderTryBlock {
        private final Object startLabel, endLabel, handlerLabel;
        private final TypeId exceptionType;
        private final int index;
        private int start = -1, end = -1, handler = -1;

        BuilderTryBlock(int index, Object startLabel, Object endLabel, TypeId exceptionType, Object handlerLabel) {
            this.startLabel = startLabel;
            this.endLabel = endLabel;
            this.handlerLabel = handlerLabel;
            this.exceptionType = exceptionType;
            this.index = index;
        }

        public int start() {
            if (start < 0) {
                start = getLabelUnit(startLabel);
            }
            return start;
        }

        public int end() {
            if (end < 0) {
                end = getLabelUnit(endLabel);
            }
            return end;
        }

        public int handler() {
            if (handler < 0) {
                handler = getLabelUnit(handlerLabel);
            }
            return handler;
        }

        public int index() {
            return index;
        }

        public TypeId exceptionType() {
            return exceptionType;
        }
    }

    private static final Comparator<BuilderTryBlock> TRY_BLOCK_COMPARATOR = (a, b) -> {
        int out = Integer.compare(a.start(), b.start());
        if (out != 0) return out;
        out = Integer.compare(a.end(), b.end());
        if (out != 0) return out;
        if (a.exceptionType() != null && b.exceptionType() == null) {
            return 1;
        }
        return Integer.compare(a.index(), b.index());
    };

    private final int regs_size, ins_size;
    private final List<Supplier<Instruction>> instructions;
    private final List<Runnable> payload_actions;
    private final List<BuilderTryBlock> try_blocks;
    private final Map<Object, Integer> labels;
    private final boolean has_this;

    private int current_unit, max_outs;

    private CodeBuilder(int regs_size, int ins_size, boolean add_hidden_this) {
        this.has_this = add_hidden_this;
        int this_reg = add_hidden_this ? 1 : 0;
        this.regs_size = Checks.checkRange(regs_size, 0, (1 << 16) - this_reg) + this_reg;
        this.ins_size = Checks.checkRange(ins_size, 0, regs_size + 1) + this_reg;
        this.instructions = new ArrayList<>();
        this.payload_actions = new ArrayList<>();
        this.try_blocks = new ArrayList<>();
        this.labels = new HashMap<>();
        this.current_unit = 0;
    }

    private List<TryItem> getTryItems() {
        try_blocks.sort(TRY_BLOCK_COMPARATOR);

        List<TryItem> out = new ArrayList<>(try_blocks.size());

        for (int i = 0; i < try_blocks.size(); ) {
            BuilderTryBlock block = try_blocks.get(i);
            int start = block.start(), end = block.end();
            CatchHandler handler = new CatchHandler();
            for (; i < try_blocks.size(); i++) {
                block = try_blocks.get(i);
                if (block.start() != start || block.end() != end) {
                    break;
                }
                TypeId ex = block.exceptionType();
                int address = block.handler();
                if (ex == null) {
                    handler.setCatchAllAddress(address);
                } else {
                    handler.getHandlers().add(new CatchHandlerElement(ex, address));
                }
            }
            out.add(new TryItem(start, end, handler));
        }

        return out;
    }

    private CodeItem end() {
        payload_actions.forEach(Runnable::run);

        List<Instruction> insns = instructions.stream()
                .map(Supplier::get).collect(Collectors.toList());

        List<TryItem> try_items = getTryItems();

        return new CodeItem(regs_size, ins_size, max_outs, insns, try_items);
    }

    public static CodeItem build(int regs_size, int ins_size,
                                 boolean add_hidden_this, Consumer<CodeBuilder> consumer) {
        CodeBuilder builder = new CodeBuilder(regs_size, ins_size, add_hidden_this);
        consumer.accept(builder);
        return builder.end();
    }

    public static CodeItem build(int regs_size, int ins_size, Consumer<CodeBuilder> consumer) {
        CodeBuilder builder = new CodeBuilder(regs_size, ins_size, false);
        consumer.accept(builder);
        return builder.end();
    }

    public int v(int reg) {
        //all registers
        return Checks.checkRange(reg, 0, regs_size);
    }

    public int l(int reg) {
        //only local registers
        int locals = regs_size - ins_size;
        return Checks.checkRange(reg, 0, locals);
    }

    private int p(int reg, boolean include_this) {
        //only parameter registers
        int locals = regs_size - ins_size;
        return locals + Checks.checkRange(reg, 0,
                ins_size - (include_this ? 1 : 0)) + (include_this ? 1 : 0);
    }

    public int p(int reg) {
        //only parameter registers without hidden this
        return p(reg, has_this);
    }

    public int this_() {
        if (!has_this) {
            throw new IllegalArgumentException("builder has no 'this' register");
        }
        return p(0, false);
    }

    private int check_reg(int reg, int width) {
        return Checks.checkRange(reg, 0, Math.min(1 << width, regs_size));
    }

    private int check_reg_pair(int reg_pair, int width) {
        Checks.checkRange(reg_pair + 1, 1, regs_size - 1);
        return check_reg(reg_pair, width);
    }

    private int check_reg_or_pair(int reg_or_pair, int width, boolean isPair) {
        return isPair ? check_reg_pair(reg_or_pair, width) : check_reg(reg_or_pair, width);
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private int check_reg_range(int first_reg, int reg_width, int count, int count_width) {
        if (count == 0) {
            if (first_reg != 0) {
                throw new IllegalArgumentException("count == 0, but first_reg != 0");
            }
            return first_reg;
        }
        Checks.checkRange(count, 0, 1 << count_width);
        count--;
        Checks.checkRange(first_reg + count, count, regs_size - count);
        return check_reg(first_reg, reg_width);
    }

    private void add(Instruction instruction) {
        instructions.add(() -> instruction);
        current_unit += instruction.units();
    }

    private <F extends Format> void add(F format, Function<F, Instruction> factory) {
        if (format.isPayload()) {
            throw new AssertionError();
        }
        instructions.add(() -> factory.apply(format));
        current_unit += format.units();
    }

    private void addPayloadAction(Runnable action) {
        payload_actions.add(action);
    }

    private void putLabel(Object label) {
        if (labels.putIfAbsent(Objects.requireNonNull(label), current_unit) != null) {
            throw new IllegalArgumentException("label " + label + " already exists");
        }
    }

    private int getLabelUnit(Object label) {
        Integer unit = labels.get(label);
        if (unit == null) {
            throw new IllegalStateException("can`t find label: " + label);
        }
        return unit;
    }

    private int getLabelBranchOffset(Object label, int start_unit) {
        return getLabelBranchOffset(label, start_unit, false);
    }

    private int getLabelBranchOffset(Object label, int start_unit, boolean allow_zero) {
        int offset = getLabelUnit(label) - start_unit;
        if (offset == 0) {
            if (allow_zero) {
                return 0;
            }
            throw new IllegalStateException("zero branch offset is not allowed");
        }
        return offset;
    }

    public CodeBuilder label(String label) {
        putLabel(label);
        return this;
    }

    private void addTryBlock(Object start, Object end, TypeId exceptionType, Object handler) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(handler);
        try_blocks.add(new BuilderTryBlock(try_blocks.size(), start, end, exceptionType, handler));
    }

    private CodeBuilder try_catch_internal(Object start, Object end, TypeId exceptionType, Object handler) {
        Objects.requireNonNull(exceptionType);
        addTryBlock(start, end, exceptionType, handler);
        return this;
    }

    public CodeBuilder try_catch(String start, String end, TypeId exceptionType, String handler) {
        return try_catch_internal(start, end, exceptionType, handler);
    }

    public CodeBuilder try_catch(String start, String end, TypeId exceptionType) {
        InternalLabel handler = new InternalLabel();
        putLabel(handler);
        return try_catch_internal(start, end, exceptionType, handler);
    }

    public CodeBuilder try_catch_all_internal(Object start, Object end, Object handler) {
        addTryBlock(start, end, null, handler);
        return this;
    }

    public CodeBuilder try_catch_all(String start, String end, String handler) {
        return try_catch_all_internal(start, end, handler);
    }

    public CodeBuilder try_catch_all(String start, String end) {
        InternalLabel handler = new InternalLabel();
        putLabel(handler);
        return try_catch_all_internal(start, end, handler);
    }

    public CodeBuilder if_(boolean value, Consumer<CodeBuilder> true_branch,
                           Consumer<CodeBuilder> false_branch) {
        if (value) {
            true_branch.accept(this);
        } else {
            false_branch.accept(this);
        }
        return this;
    }

    public CodeBuilder commit(Consumer<CodeBuilder> branch) {
        branch.accept(this);
        return this;
    }

    private void format_35c_checks(int arg_count, int arg_reg1, int arg_reg2,
                                   int arg_reg3, int arg_reg4, int arg_reg5) {
        Checks.checkRange(arg_count, 0, 6);
        if (arg_count == 5) check_reg(arg_reg5, 4);
        else if (arg_reg5 != 0) throw new IllegalArgumentException(
                "arg_count < 5, but arg_reg5 != 0");

        if (arg_count >= 4) check_reg(arg_reg4, 4);
        else if (arg_reg4 != 0) throw new IllegalArgumentException(
                "arg_count < 4, but arg_reg4 != 0");

        if (arg_count >= 3) check_reg(arg_reg3, 4);
        else if (arg_reg3 != 0) throw new IllegalArgumentException(
                "arg_count < 3, but arg_reg3 != 0");

        if (arg_count >= 2) check_reg(arg_reg2, 4);
        else if (arg_reg2 != 0) throw new IllegalArgumentException(
                "arg_count < 2, but arg_reg2 != 0");

        if (arg_count >= 1) check_reg(arg_reg1, 4);
        else if (arg_reg1 != 0) throw new IllegalArgumentException(
                "arg_count == 0, but arg_reg1 != 0");
    }

    public CodeBuilder add_raw(Instruction instruction) {
        add(instruction);
        return this;
    }

    // <ØØ|op> op
    private CodeBuilder f10x(Opcode op) {
        add(op.<Format10x>format().make());
        return this;
    }

    // <B|A|op> op vA, vB
    private CodeBuilder f12x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(op.<Format12x>format().make(
                check_reg_or_pair(reg_or_pair1, 4, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 4, is_reg2_wide)));
        return this;
    }

    // <B|A|op> op vA, #+B
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f11n(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
        InstructionWriter.check_signed(value, 4);
        add(op.<Format11n>format().make(
                check_reg_or_pair(reg_or_pair, 4, is_reg_wide), value));
        return this;
    }

    // <AA|op> op vAA
    private CodeBuilder f11x(Opcode op, int reg_or_pair, boolean is_reg_wide) {
        add(op.<Format11x>format().make(check_reg_or_pair(reg_or_pair, 8, is_reg_wide)));
        return this;
    }

    // <AA|op> op +AA
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f10t(Opcode op, IntSupplier value) {
        add(op.<Format10t>format(), format -> {
            int branch_offset = value.getAsInt();
            InstructionWriter.check_signed(branch_offset, 8);
            return format.make(branch_offset);
        });
        return this;
    }

    // <ØØ|op AAAA> op +AAAA
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f20t(Opcode op, IntSupplier value) {
        add(op.<Format20t>format(), format -> {
            int branch_offset = value.getAsInt();
            InstructionWriter.check_signed(branch_offset, 16);
            return format.make(branch_offset);
        });
        return this;
    }

    // <AA|op BBBB> op AA, @BBBB
    //TODO: 20bc

    // <AA|op BBBB> op vAA, vBBBB
    private CodeBuilder f22x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(op.<Format22x>format().make(
                check_reg_or_pair(reg_or_pair1, 8, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 16, is_reg2_wide)));
        return this;
    }

    // <AA|op BBBB> op vAA, +BBBB
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f21t(Opcode op, int reg_or_pair, boolean is_reg_wide, IntSupplier value) {
        add(op.<Format21t21s>format(), format -> {
            int branch_offset = value.getAsInt();
            InstructionWriter.check_signed(branch_offset, 16);
            return format.make(check_reg_or_pair(reg_or_pair, 8, is_reg_wide), branch_offset);
        });
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB
    private CodeBuilder f21s(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
        InstructionWriter.check_signed(value, 16);
        add(op.<Format21t21s>format().make(
                check_reg_or_pair(reg_or_pair, 8, is_reg_wide), value));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB0000
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f21ih(Opcode op, int reg, int value) {
        InstructionWriter.check_hat32(value, 16);
        add(op.<Format21ih>format().make(check_reg(reg, 8), value));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB000000000000
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f21lh(Opcode op, int reg_pair, long value) {
        InstructionWriter.check_hat64(value, 16);
        add(op.<Format21lh>format().make(check_reg_pair(reg_pair, 8), value));
        return this;
    }

    // <AA|op BBBB> op vAA, @BBBB
    private CodeBuilder f21c(Opcode op, int reg_or_pair, boolean is_reg_wide, Object constant) {
        add(op.<Format21c>format().make(
                check_reg_or_pair(reg_or_pair, 8, is_reg_wide), constant));
        return this;
    }

    // <AA|op CC|BB> op vAA, vBB, vCC
    private CodeBuilder f23x(Opcode op, int reg_or_pair1, boolean is_reg1_wide, int reg_or_pair2,
                             boolean is_reg2_wide, int reg_or_pair3, boolean is_reg3_wide) {
        add(op.<Format23x>format().make(
                check_reg_or_pair(reg_or_pair1, 8, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 8, is_reg2_wide),
                check_reg_or_pair(reg_or_pair3, 8, is_reg3_wide)));
        return this;
    }

    // <AA|op CC|BB> op vAA, vBB, #+CC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22b(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, int value) {
        InstructionWriter.check_signed(value, 8);
        add(op.<Format22b>format().make(
                check_reg_or_pair(reg_or_pair1, 8, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 8, is_reg2_wide), value));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, +CCCC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22t(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, IntSupplier value) {
        add(op.<Format22t22s>format(), format -> {
            int branch_offset = value.getAsInt();
            InstructionWriter.check_signed(branch_offset, 16);
            return format.make(check_reg_or_pair(reg_or_pair1, 4, is_reg1_wide),
                    check_reg_or_pair(reg_or_pair2, 4, is_reg2_wide), branch_offset);
        });
        return this;
    }

    // <B|A|op CCCC> op vA, vB, #+CCCC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22s(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, int value) {
        InstructionWriter.check_signed(value, 16);
        add(op.<Format22t22s>format().make(
                check_reg_or_pair(reg_or_pair1, 4, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 4, is_reg2_wide), value));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, @CCCC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22c(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, Object constant) {
        add(op.<Format22c>format().make(
                check_reg_or_pair(reg_or_pair1, 4, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 4, is_reg2_wide), constant));
        return this;
    }

    // <ØØ|op AAAAlo AAAAhi> op +AAAAAAAA
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f30t(Opcode op, IntSupplier value) {
        add(op.<Format30t>format(), format -> {
            int branch_offset = value.getAsInt();
            return format.make(branch_offset);
        });
        return this;
    }

    // <ØØ|op AAAA BBBB> op vAAAA, vBBBB
    private CodeBuilder f32x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(op.<Format32x>format().make(
                check_reg_or_pair(reg_or_pair1, 16, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 16, is_reg2_wide)));
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, #+BBBBBBBB
    private CodeBuilder f31i(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
        add(op.<Format31i31t>format().make(
                check_reg_or_pair(reg_or_pair, 8, is_reg_wide), value));
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, +BBBBBBBB
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f31t(Opcode op, int reg_or_pair, boolean is_reg_wide, IntSupplier value) {
        add(op.<Format31i31t>format(), format -> {
            int branch_offset = value.getAsInt();
            return format.make(check_reg_or_pair(reg_or_pair, 8, is_reg_wide), branch_offset);
        });
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, @BBBBBBBB
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f31c(Opcode op, int reg_or_pair, boolean is_reg_wide, Object constant) {
        add(op.<Format31c>format().make(
                check_reg_or_pair(reg_or_pair, 8, is_reg_wide), constant));
        return this;
    }

    // <A|G|op BBBB F|E|D|C> [A] op {vC, vD, vE, vF, vG}, @BBBB
    private CodeBuilder f35c(Opcode op, Object constant, int arg_count, int arg_reg1,
                             int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(op.<Format35c35ms35mi>format().make(arg_count,
                constant, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5));
        return this;
    }

    // <AA|op BBBB CCCC> op {vCCCC .. vNNNN}, @BBBB (where NNNN = CCCC+AA-1)
    private CodeBuilder f3rc(Opcode op, Object constant, int arg_count, int first_arg_reg) {
        check_reg_range(first_arg_reg, 16, arg_count, 8);
        add(op.<Format3rc3rms3rmi>format().make(arg_count, constant, first_arg_reg));
        return this;
    }

    // <A|G|op BBBB F|E|D|C HHHH> [A] op {vC, vD, vE, vF, vG}, @BBBB
    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private CodeBuilder f45cc(Opcode op, Object constant1, Object constant2, int arg_count,
                              int arg_reg1, int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(op.<Format45cc>format().make(arg_count, constant1,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5, constant2));
        return this;
    }

    // <AA|op BBBB CCCC HHHH> op {vCCCC .. vNNNN}, @BBBB, @HHHH (where NNNN = CCCC+AA-1)
    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private CodeBuilder f4rcc(Opcode op, Object constant1,
                              Object constant2, int arg_count, int first_arg_reg) {
        check_reg_range(first_arg_reg, 16, arg_count, 8);
        add(op.<Format4rcc>format().make(arg_count, constant1, first_arg_reg, constant2));
        return this;
    }

    // <AA|op BBBBlolo BBBBlohi BBBBhilo BBBBhihi> op vAA, #+BBBBBBBBBBBBBBBB
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f51l(Opcode op, int reg_pair, long value) {
        add(op.<Format51l>format().make(check_reg_pair(reg_pair, 8), value));
        return this;
    }

    private void align_current_unit2() {
        if ((current_unit & 1) != 0) {
            nop();
        }
    }

    private void check_current_unit_alignment2() {
        if ((current_unit & 1) != 0) {
            throw new IllegalStateException("current position is not aligned by 2 code units");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private CodeBuilder packed_switch_payload(int first_key, int[] branch_offsets) {
        check_current_unit_alignment2();
        add(Opcode.PACKED_SWITCH_PAYLOAD.<PackedSwitchPayload>format()
                .make(first_key, branch_offsets));
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    private CodeBuilder sparse_switch_payload(int[] keys, int[] branch_offsets) {
        check_current_unit_alignment2();
        add(Opcode.SPARSE_SWITCH_PAYLOAD.<SparseSwitchPayload>format()
                .make(keys, branch_offsets));
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    private CodeBuilder fill_array_data_payload(int element_width, byte[] data) {
        check_current_unit_alignment2();
        add(Opcode.ARRAY_PAYLOAD.<ArrayPayload>format().make(element_width, data));
        return this;
    }

    public CodeBuilder nop() {
        return f10x(Opcode.NOP);
    }

    public CodeBuilder move(int dsr_reg, int src_reg) {
        return f12x(Opcode.MOVE, dsr_reg, false, src_reg, false);
    }

    public CodeBuilder move_from16(int dsr_reg, int src_reg) {
        return f22x(Opcode.MOVE_FROM16, dsr_reg, false, src_reg, false);
    }

    public CodeBuilder move_16(int dsr_reg, int src_reg) {
        return f32x(Opcode.MOVE_16, dsr_reg, false, src_reg, false);
    }

    public CodeBuilder move_auto(int dsr_reg, int src_reg) {
        if (src_reg < 1 << 4 && dsr_reg < 1 << 4) {
            return move(dsr_reg, src_reg);
        }
        if (src_reg < 1 << 8) {
            return move_from16(dsr_reg, src_reg);
        }
        return move_16(dsr_reg, src_reg);
    }

    public CodeBuilder move_wide(int dsr_reg_pair, int src_reg_pair) {
        return f12x(Opcode.MOVE_WIDE, dsr_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder move_wide_from16(int dsr_reg_pair, int src_reg_pair) {
        return f22x(Opcode.MOVE_WIDE_FROM16, dsr_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder move_wide_16(int dsr_reg_pair, int src_reg_pair) {
        return f32x(Opcode.MOVE_WIDE_16, dsr_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder move_wide_auto(int dsr_reg, int src_reg) {
        if (src_reg < 1 << 4 && dsr_reg < 1 << 4) {
            return move_wide(dsr_reg, src_reg);
        }
        if (src_reg < 1 << 8) {
            return move_wide_from16(dsr_reg, src_reg);
        }
        return move_wide_16(dsr_reg, src_reg);
    }

    public CodeBuilder move_object(int dsr_reg, int src_reg) {
        return f12x(Opcode.MOVE_OBJECT, dsr_reg, false, src_reg, false);
    }

    public CodeBuilder move_object_from16(int dsr_reg, int src_reg) {
        return f22x(Opcode.MOVE_OBJECT_FROM16, dsr_reg, false, src_reg, false);
    }

    public CodeBuilder move_object_16(int dsr_reg, int src_reg) {
        return f32x(Opcode.MOVE_OBJECT_16, dsr_reg, false, src_reg, false);
    }

    public CodeBuilder move_object_auto(int dsr_reg, int src_reg) {
        if (src_reg < 1 << 4 && dsr_reg < 1 << 4) {
            return move_object(dsr_reg, src_reg);
        }
        if (src_reg < 1 << 8) {
            return move_object_from16(dsr_reg, src_reg);
        }
        return move_object_16(dsr_reg, src_reg);
    }

    public CodeBuilder move_result(int dst_reg) {
        return f11x(Opcode.MOVE_RESULT, dst_reg, false);
    }

    public CodeBuilder move_result_wide(int dst_reg_peir) {
        return f11x(Opcode.MOVE_RESULT_WIDE, dst_reg_peir, true);
    }

    public CodeBuilder move_result_object(int dst_reg) {
        return f11x(Opcode.MOVE_RESULT_OBJECT, dst_reg, false);
    }

    public CodeBuilder move_exception(int dst_reg) {
        return f11x(Opcode.MOVE_EXCEPTION, dst_reg, false);
    }

    public CodeBuilder return_void() {
        return f10x(Opcode.RETURN_VOID);
    }

    public CodeBuilder return_(int return_value_reg) {
        return f11x(Opcode.RETURN, return_value_reg, false);
    }

    public CodeBuilder return_wide(int return_value_reg_peir) {
        return f11x(Opcode.RETURN_WIDE, return_value_reg_peir, true);
    }

    public CodeBuilder return_object(int return_value_reg) {
        return f11x(Opcode.RETURN_OBJECT, return_value_reg, false);
    }

    public CodeBuilder const_4(int dst_reg, int value) {
        return f11n(Opcode.CONST_4, dst_reg, false, value);
    }

    public CodeBuilder const_16(int dst_reg, int value) {
        return f21s(Opcode.CONST_16, dst_reg, false, value);
    }

    public CodeBuilder const_(int dst_reg, int value) {
        return f31i(Opcode.CONST, dst_reg, false, value);
    }

    public CodeBuilder const_high16(int dst_reg, int value) {
        return f21ih(Opcode.CONST_HIGH16, dst_reg, value);
    }

    private static boolean check_width_int(int value, int width) {
        int empty_width = 32 - width;
        return value << empty_width >> empty_width == value;
    }

    public CodeBuilder const_auto(int dst_reg, int value) {
        if (dst_reg < 1 << 4 && check_width_int(value, 4)) {
            return const_4(dst_reg, value);
        }
        if (check_width_int(value, 16)) {
            return const_16(dst_reg, value);
        }
        if ((value & 0xffff) == 0) {
            return const_high16(dst_reg, value);
        }
        return const_(dst_reg, value);
    }

    public CodeBuilder const_wide_16(int dst_reg_pair, int value) {
        return f21s(Opcode.CONST_WIDE_16, dst_reg_pair, true, value);
    }

    public CodeBuilder const_wide_32(int dst_reg_pair, int value) {
        return f31i(Opcode.CONST_WIDE_32, dst_reg_pair, true, value);
    }

    public CodeBuilder const_wide(int dst_reg_pair, long value) {
        return f51l(Opcode.CONST_WIDE, dst_reg_pair, value);
    }

    public CodeBuilder const_wide_high16(int dst_reg_pair, long value) {
        return f21lh(Opcode.CONST_WIDE_HIGH16, dst_reg_pair, value);
    }

    private static boolean check_width_long(long value, int width) {
        int empty_width = 64 - width;
        return value << empty_width >> empty_width == value;
    }

    public CodeBuilder const_wide_auto(int dst_reg_pair, long value) {
        if (check_width_long(value, 16)) {
            return const_wide_16(dst_reg_pair, (int) value);
        }
        if (check_width_long(value, 32)) {
            return const_wide_32(dst_reg_pair, (int) value);
        }
        if ((value & 0xffff_ffff_ffffL) == 0) {
            return const_wide_high16(dst_reg_pair, value);
        }
        return const_wide(dst_reg_pair, value);
    }

    public CodeBuilder const_string(int dst_reg, String value) {
        return f21c(Opcode.CONST_STRING, dst_reg, false, value);
    }

    public CodeBuilder const_string_jumbo(int dst_reg, String value) {
        return f31c(Opcode.CONST_STRING_JUMBO, dst_reg, false, value);
    }

    public CodeBuilder const_class(int dst_reg, TypeId value) {
        return f21c(Opcode.CONST_CLASS, dst_reg, false, value);
    }

    public CodeBuilder monitor_enter(int ref_reg) {
        return f11x(Opcode.MONITOR_ENTER, ref_reg, false);
    }

    public CodeBuilder monitor_exit(int ref_reg) {
        return f11x(Opcode.MONITOR_EXIT, ref_reg, false);
    }

    public CodeBuilder check_cast(int ref_reg, TypeId value) {
        return f21c(Opcode.CHECK_CAST, ref_reg, false, value);
    }

    public CodeBuilder instance_of(int dst_reg, int ref_reg, TypeId value) {
        return f22c(Opcode.INSTANCE_OF, dst_reg, false, ref_reg, false, value);
    }

    public CodeBuilder new_instance(int dst_reg, TypeId value) {
        return f21c(Opcode.NEW_INSTANCE, dst_reg, false, value);
    }

    public CodeBuilder new_array(int dst_reg, int size_reg, TypeId value) {
        return f22c(Opcode.NEW_ARRAY, dst_reg, false, size_reg, false, value);
    }

    public CodeBuilder filled_new_array(TypeId type, int arr_size, int arg_reg1,
                                        int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return f35c(Opcode.FILLED_NEW_ARRAY, type, arr_size,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
    }

    public CodeBuilder filled_new_array(TypeId type, int arg_reg1, int arg_reg2,
                                        int arg_reg3, int arg_reg4, int arg_reg5) {
        return filled_new_array(type, 5, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
    }

    public CodeBuilder filled_new_array(TypeId type, int arg_reg1, int arg_reg2,
                                        int arg_reg3, int arg_reg4) {
        return filled_new_array(type, 4, arg_reg1, arg_reg2, arg_reg3, arg_reg4, 0);
    }

    public CodeBuilder filled_new_array(TypeId type,
                                        int arg_reg1, int arg_reg2, int arg_reg3) {
        return filled_new_array(type, 3, arg_reg1, arg_reg2, arg_reg3, 0, 0);
    }

    public CodeBuilder filled_new_array(TypeId type, int arg_reg1, int arg_reg2) {
        return filled_new_array(type, 2, arg_reg1, arg_reg2, 0, 0, 0);
    }

    public CodeBuilder filled_new_array(TypeId type, int arg_reg1) {
        return filled_new_array(type, 1, arg_reg1, 0, 0, 0, 0);
    }

    public CodeBuilder filled_new_array(TypeId type) {
        return filled_new_array(type, 0, 0, 0, 0, 0, 0);
    }

    public CodeBuilder filled_new_array_range(TypeId type, int arr_size, int first_arg_reg) {
        return f3rc(Opcode.FILLED_NEW_ARRAY_RANGE, type, arr_size, first_arg_reg);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, int element_width, byte[] data) {
        InstructionWriter.check_array_payload(element_width, data);
        int start_unit = current_unit;
        InternalLabel payload = new InternalLabel();
        addPayloadAction(() -> {
            align_current_unit2();
            putLabel(payload);
            fill_array_data_payload(element_width, data);
        });
        return f31t(Opcode.FILL_ARRAY_DATA, arr_ref_reg, false,
                () -> getLabelBranchOffset(payload, start_unit));
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, boolean[] data) {
        byte[] byte_data = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte_data[i] = (byte) (data[i] ? 1 : 0);
        }
        return fill_array_data(arr_ref_reg, 1, byte_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, byte[] data) {
        return fill_array_data(arr_ref_reg, 1, data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, short[] data) {
        byte[] byte_data = new byte[data.length * 2];
        ByteBuffer.wrap(byte_data).asShortBuffer().put(data);
        return fill_array_data(arr_ref_reg, 2, byte_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, char[] data) {
        byte[] byte_data = new byte[data.length * 2];
        ByteBuffer.wrap(byte_data).asCharBuffer().put(data);
        return fill_array_data(arr_ref_reg, 2, byte_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, int[] data) {
        byte[] byte_data = new byte[data.length * 4];
        ByteBuffer.wrap(byte_data).asIntBuffer().put(data);
        return fill_array_data(arr_ref_reg, 4, byte_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, float[] data) {
        byte[] byte_data = new byte[data.length * 4];
        ByteBuffer.wrap(byte_data).asFloatBuffer().put(data);
        return fill_array_data(arr_ref_reg, 4, byte_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, long[] data) {
        byte[] byte_data = new byte[data.length * 8];
        ByteBuffer.wrap(byte_data).asLongBuffer().put(data);
        return fill_array_data(arr_ref_reg, 8, byte_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, double[] data) {
        byte[] byte_data = new byte[data.length * 8];
        ByteBuffer.wrap(byte_data).asDoubleBuffer().put(data);
        return fill_array_data(arr_ref_reg, 8, byte_data);
    }

    public CodeBuilder throw_(int ex_reg) {
        return f11x(Opcode.THROW, ex_reg, false);
    }

    private CodeBuilder goto_internal(Object label) {
        int start_unit = current_unit;
        return f10t(Opcode.GOTO, () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder goto_(String label) {
        return goto_internal(label);
    }

    private CodeBuilder goto_16_internal(Object label) {
        int start_unit = current_unit;
        return f20t(Opcode.GOTO_16, () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder goto_16(String label) {
        return goto_16_internal(label);
    }

    private CodeBuilder goto_32_internal(Object label) {
        int start_unit = current_unit;
        return f30t(Opcode.GOTO_32, () -> getLabelBranchOffset(label, start_unit, true));
    }

    public CodeBuilder goto_32(String label) {
        return goto_32_internal(label);
    }

    private CodeBuilder packed_switch_internal(int reg_to_test, int first_key, Object... labels) {
        Objects.requireNonNull(labels);
        int start_unit = current_unit;
        InternalLabel payload = new InternalLabel();
        addPayloadAction(() -> {
            int[] offsets = new int[labels.length];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = getLabelBranchOffset(labels[i], start_unit, true);
            }
            align_current_unit2();
            putLabel(payload);
            packed_switch_payload(first_key, offsets);
        });
        return f31t(Opcode.PACKED_SWITCH, reg_to_test, false,
                () -> getLabelBranchOffset(payload, start_unit));
    }

    public CodeBuilder packed_switch(int reg_to_test, int first_key, String... labels) {
        return packed_switch_internal(reg_to_test, first_key, (Object[]) labels);
    }

    private CodeBuilder sparse_switch_internal(int reg_to_test, int[] keys, Object... labels) {
        if (keys.length != labels.length) {
            throw new IllegalArgumentException("sparse_switch: keys.length != labels.length");
        }
        int start_unit = current_unit;
        InternalLabel payload = new InternalLabel();
        addPayloadAction(() -> {
            int[] offsets = new int[labels.length];
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] = getLabelBranchOffset(labels[i], start_unit, true);
            }
            align_current_unit2();
            putLabel(payload);
            sparse_switch_payload(keys, offsets);
        });
        return f31t(Opcode.SPARSE_SWITCH, reg_to_test, false,
                () -> getLabelBranchOffset(payload, start_unit));
    }

    public CodeBuilder sparse_switch(int reg_to_test, int[] keys, String... labels) {
        return sparse_switch_internal(reg_to_test, keys, (Object[]) labels);
    }

    public enum Cmp {
        CMPL_FLOAT(Opcode.CMPL_FLOAT, false),
        CMPG_FLOAT(Opcode.CMPG_FLOAT, false),
        CMPL_DOUBLE(Opcode.CMPL_DOUBLE, true),
        CMPG_DOUBLE(Opcode.CMPG_DOUBLE, true),
        CMP_LONG(Opcode.CMP_LONG, true);

        private final Opcode opcode;
        private final boolean isWide;

        Cmp(Opcode opcode, boolean isWide) {
            this.opcode = opcode;
            this.isWide = isWide;
        }
    }

    public CodeBuilder cmp_kind(Cmp kind, int dst_reg, int first_src_reg_or_pair,
                                int second_src_reg_or_pair) {
        return f23x(kind.opcode, dst_reg, false,
                first_src_reg_or_pair, kind.isWide, second_src_reg_or_pair, kind.isWide);
    }

    public enum Test {
        EQ(Opcode.IF_EQ, Opcode.IF_EQZ),
        NE(Opcode.IF_NE, Opcode.IF_NEZ),
        LT(Opcode.IF_LT, Opcode.IF_LTZ),
        GE(Opcode.IF_GE, Opcode.IF_GEZ),
        GT(Opcode.IF_GT, Opcode.IF_GTZ),
        LE(Opcode.IF_LE, Opcode.IF_LEZ);

        private final Opcode test, testz;

        Test(Opcode test, Opcode testz) {
            this.test = test;
            this.testz = testz;
        }
    }

    private CodeBuilder if_test_internal(Test test, int first_reg_to_test, int second_reg_to_test, Object label) {
        int start_unit = current_unit;
        return f22t(test.test, first_reg_to_test, false,
                second_reg_to_test, false,
                () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder if_test(Test test, int first_reg_to_test, int second_reg_to_test, String label) {
        return if_test_internal(test, first_reg_to_test, second_reg_to_test, label);
    }

    public CodeBuilder if_test_else(Test test, int first_reg_to_test, int second_reg_to_test,
                                    Consumer<CodeBuilder> true_branch, Consumer<CodeBuilder> false_branch) {
        InternalLabel true_ = new InternalLabel();
        InternalLabel end = new InternalLabel();

        if_test_internal(test, first_reg_to_test, second_reg_to_test, true_);
        false_branch.accept(this);
        goto_32_internal(end);
        putLabel(true_);
        true_branch.accept(this);
        putLabel(end);

        return this;
    }

    private CodeBuilder if_testz_internal(Test test, int reg_to_test, Object label) {
        int start_unit = current_unit;
        return f21t(test.testz, reg_to_test, false,
                () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder if_testz(Test test, int reg_to_test, String label) {
        return if_testz_internal(test, reg_to_test, label);
    }

    public CodeBuilder if_testz_else(Test test, int reg_to_test, Consumer<CodeBuilder> true_branch,
                                     Consumer<CodeBuilder> false_branch) {
        InternalLabel true_ = new InternalLabel();
        InternalLabel end = new InternalLabel();

        if_testz_internal(test, reg_to_test, true_);
        false_branch.accept(this);
        goto_32_internal(end);
        putLabel(true_);
        true_branch.accept(this);
        putLabel(end);

        return this;
    }

    public enum Op {
        GET(Opcode.AGET, Opcode.IGET, Opcode.SGET, false),
        GET_WIDE(Opcode.AGET_WIDE, Opcode.IGET_WIDE, Opcode.SGET_WIDE, true),
        GET_OBJECT(Opcode.AGET_OBJECT, Opcode.IGET_OBJECT, Opcode.SGET_OBJECT, false),
        GET_BOOLEAN(Opcode.AGET_BOOLEAN, Opcode.IGET_BOOLEAN, Opcode.SGET_BOOLEAN, false),
        GET_BYTE(Opcode.AGET_BYTE, Opcode.IGET_BYTE, Opcode.SGET_BYTE, false),
        GET_CHAR(Opcode.AGET_CHAR, Opcode.IGET_CHAR, Opcode.SGET_CHAR, false),
        GET_SHORT(Opcode.AGET_SHORT, Opcode.IGET_SHORT, Opcode.SGET_SHORT, false),
        PUT(Opcode.APUT, Opcode.IPUT, Opcode.SPUT, false),
        PUT_WIDE(Opcode.APUT_WIDE, Opcode.IPUT_WIDE, Opcode.SPUT_WIDE, true),
        PUT_OBJECT(Opcode.APUT_OBJECT, Opcode.IPUT_OBJECT, Opcode.SPUT_OBJECT, false),
        PUT_BOOLEAN(Opcode.APUT_BOOLEAN, Opcode.IPUT_BOOLEAN, Opcode.SPUT_BOOLEAN, false),
        PUT_BYTE(Opcode.APUT_BYTE, Opcode.IPUT_BYTE, Opcode.SPUT_BYTE, false),
        PUT_CHAR(Opcode.APUT_CHAR, Opcode.IPUT_CHAR, Opcode.SPUT_CHAR, false),
        PUT_SHORT(Opcode.APUT_SHORT, Opcode.IPUT_SHORT, Opcode.SPUT_SHORT, false);

        private final Opcode aop, iop, sop;
        private final boolean isWide;

        Op(Opcode aop, Opcode iop, Opcode sop, boolean isWide) {
            this.aop = aop;
            this.iop = iop;
            this.sop = sop;
            this.isWide = isWide;
        }
    }

    public CodeBuilder aop(Op op, int value_reg_or_pair, int array_reg, int index_reg) {
        return f23x(op.aop, value_reg_or_pair, op.isWide,
                array_reg, false, index_reg, false);
    }

    public CodeBuilder iop(Op op, int value_reg_or_pair, int object_reg, FieldId instance_field) {
        return f22c(op.iop, value_reg_or_pair, op.isWide, object_reg, false, instance_field);
    }

    public CodeBuilder sop(Op op, int value_reg_or_pair, FieldId static_field) {
        return f21c(op.sop, value_reg_or_pair, op.isWide, static_field);
    }

    private void add_outs(int outs_count) {
        Checks.checkRange(outs_count, 0, 1 << 8);
        max_outs = Math.max(max_outs, outs_count);
    }

    public enum InvokeKind {
        VIRTUAL(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE),
        SUPER(Opcode.INVOKE_SUPER, Opcode.INVOKE_SUPER_RANGE),
        DIRECT(Opcode.INVOKE_DIRECT, Opcode.INVOKE_DIRECT_RANGE),
        STATIC(Opcode.INVOKE_STATIC, Opcode.INVOKE_STATIC_RANGE),
        INTERFACE(Opcode.INVOKE_INTERFACE, Opcode.INVOKE_INTERFACE_RANGE);

        private final Opcode regular, range;

        InvokeKind(Opcode regular, Opcode range) {
            this.regular = regular;
            this.range = range;
        }
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method, int arg_count, int arg_reg1,
                              int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        f35c(kind.regular, method, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add_outs(arg_count);
        return this;
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method, int arg_reg1,
                              int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return invoke(kind, method, 5, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method, int arg_reg1,
                              int arg_reg2, int arg_reg3, int arg_reg4) {
        return invoke(kind, method, 4, arg_reg1, arg_reg2, arg_reg3, arg_reg4, 0);
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method,
                              int arg_reg1, int arg_reg2, int arg_reg3) {
        return invoke(kind, method, 3, arg_reg1, arg_reg2, arg_reg3, 0, 0);
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method, int arg_reg1, int arg_reg2) {
        return invoke(kind, method, 2, arg_reg1, arg_reg2, 0, 0, 0);
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method, int arg_reg1) {
        return invoke(kind, method, 1, arg_reg1, 0, 0, 0, 0);
    }

    public CodeBuilder invoke(InvokeKind kind, MethodId method) {
        return invoke(kind, method, 0, 0, 0, 0, 0, 0);
    }

    public CodeBuilder invoke_range(InvokeKind kind, MethodId method,
                                    int arg_count, int first_arg_reg) {
        f3rc(kind.range, method, arg_count, first_arg_reg);
        add_outs(arg_count);
        return this;
    }

    public enum UnOp {
        NEG_INT(Opcode.NEG_INT, false, false),
        NOT_INT(Opcode.NOT_INT, false, false),
        NEG_LONG(Opcode.NEG_LONG, true, true),
        NOT_LONG(Opcode.NOT_LONG, true, true),
        NEG_FLOAT(Opcode.NEG_FLOAT, false, false),
        NEG_DOUBLE(Opcode.NEG_DOUBLE, true, true),

        INT_TO_LONG(Opcode.INT_TO_LONG, false, true),
        INT_TO_FLOAT(Opcode.INT_TO_FLOAT, false, false),
        INT_TO_DOUBLE(Opcode.INT_TO_DOUBLE, false, true),

        LONG_TO_INT(Opcode.LONG_TO_INT, true, false),
        LONG_TO_FLOAT(Opcode.LONG_TO_FLOAT, true, false),
        LONG_TO_DOUBLE(Opcode.LONG_TO_DOUBLE, true, true),

        FLOAT_TO_INT(Opcode.FLOAT_TO_INT, false, false),
        FLOAT_TO_LONG(Opcode.FLOAT_TO_LONG, false, true),
        FLOAT_TO_DOUBLE(Opcode.FLOAT_TO_DOUBLE, false, true),

        DOUBLE_TO_INT(Opcode.DOUBLE_TO_INT, true, false),
        DOUBLE_TO_LONG(Opcode.DOUBLE_TO_LONG, true, true),
        DOUBLE_TO_DOUBLE(Opcode.DOUBLE_TO_FLOAT, true, false),

        INT_TO_BYTE(Opcode.INT_TO_BYTE, false, false),
        INT_TO_CHAR(Opcode.INT_TO_CHAR, false, false),
        INT_TO_SHORT(Opcode.INT_TO_SHORT, false, false);

        private final Opcode opcode;
        private final boolean isDstWide, isSrcWide;

        UnOp(Opcode opcode, boolean isDstWide, boolean isSrcWide) {
            this.opcode = opcode;
            this.isDstWide = isDstWide;
            this.isSrcWide = isSrcWide;
        }
    }

    public CodeBuilder unop(UnOp op, int dsr_reg_or_pair, int src_reg_or_pair) {
        return f12x(op.opcode, dsr_reg_or_pair, op.isDstWide, src_reg_or_pair, op.isSrcWide);
    }

    public enum BinOp {
        ADD_INT(Opcode.ADD_INT, Opcode.ADD_INT_2ADDR, Opcode.ADD_INT_LIT16, Opcode.ADD_INT_LIT8, false, false),
        RSUB_INT(null, null, Opcode.RSUB_INT, Opcode.RSUB_INT_LIT8, false, false),
        SUB_INT(Opcode.SUB_INT, Opcode.SUB_INT_2ADDR, null, null, false, false),
        MUL_INT(Opcode.MUL_INT, Opcode.MUL_INT_2ADDR, Opcode.MUL_INT_LIT16, Opcode.MUL_INT_LIT8, false, false),
        DIV_INT(Opcode.DIV_INT, Opcode.DIV_INT_2ADDR, Opcode.DIV_INT_LIT16, Opcode.DIV_INT_LIT8, false, false),
        REM_INT(Opcode.REM_INT, Opcode.REM_INT_2ADDR, Opcode.REM_INT_LIT16, Opcode.REM_INT_LIT8, false, false),
        AND_INT(Opcode.AND_INT, Opcode.AND_INT_2ADDR, Opcode.AND_INT_LIT16, Opcode.AND_INT_LIT8, false, false),
        OR_INT(Opcode.OR_INT, Opcode.OR_INT_2ADDR, Opcode.OR_INT_LIT16, Opcode.OR_INT_LIT8, false, false),
        XOR_INT(Opcode.XOR_INT, Opcode.XOR_INT_2ADDR, Opcode.XOR_INT_LIT16, Opcode.XOR_INT_LIT8, false, false),
        SHL_INT(Opcode.SHL_INT, Opcode.SHL_INT_2ADDR, null, Opcode.SHL_INT_LIT8, false, false),
        SHR_INT(Opcode.SHR_INT, Opcode.SHR_INT_2ADDR, null, Opcode.SHR_INT_LIT8, false, false),
        USHR_INT(Opcode.USHR_INT, Opcode.USHR_INT_2ADDR, null, Opcode.USHR_INT_LIT8, false, false),

        ADD_LONG(Opcode.ADD_LONG, Opcode.ADD_LONG_2ADDR, true, true),
        SUB_LONG(Opcode.SUB_LONG, Opcode.SUB_LONG_2ADDR, true, true),
        MUL_LONG(Opcode.MUL_LONG, Opcode.MUL_LONG_2ADDR, true, true),
        DIV_LONG(Opcode.DIV_LONG, Opcode.DIV_LONG_2ADDR, true, true),
        REM_LONG(Opcode.REM_LONG, Opcode.REM_LONG_2ADDR, true, true),
        AND_LONG(Opcode.AND_LONG, Opcode.AND_LONG_2ADDR, true, true),
        OR_LONG(Opcode.OR_LONG, Opcode.OR_LONG_2ADDR, true, true),
        XOR_LONG(Opcode.XOR_LONG, Opcode.XOR_LONG_2ADDR, true, true),
        SHL_LONG(Opcode.SHL_LONG, Opcode.SHL_LONG_2ADDR, true, false),
        SHR_LONG(Opcode.SHR_LONG, Opcode.SHR_LONG_2ADDR, true, false),
        USHR_LONG(Opcode.USHR_LONG, Opcode.USHR_LONG_2ADDR, true, false),

        ADD_FLOAT(Opcode.ADD_FLOAT, Opcode.ADD_FLOAT_2ADDR, false, false),
        SUB_FLOAT(Opcode.SUB_FLOAT, Opcode.SUB_FLOAT_2ADDR, false, false),
        MUL_FLOAT(Opcode.MUL_FLOAT, Opcode.MUL_FLOAT_2ADDR, false, false),
        DIV_FLOAT(Opcode.DIV_FLOAT, Opcode.DIV_FLOAT_2ADDR, false, false),
        REM_FLOAT(Opcode.REM_FLOAT, Opcode.REM_FLOAT_2ADDR, false, false),

        ADD_DOUBLE(Opcode.ADD_DOUBLE, Opcode.ADD_DOUBLE_2ADDR, true, true),
        SUB_DOUBLE(Opcode.SUB_DOUBLE, Opcode.SUB_DOUBLE_2ADDR, true, true),
        MUL_DOUBLE(Opcode.MUL_DOUBLE, Opcode.MUL_DOUBLE_2ADDR, true, true),
        DIV_DOUBLE(Opcode.DIV_DOUBLE, Opcode.DIV_DOUBLE_2ADDR, true, true),
        REM_DOUBLE(Opcode.REM_DOUBLE, Opcode.REM_DOUBLE_2ADDR, true, true);

        private final Opcode regular, _2addr, lit16, lit8;
        private final boolean isDstAndSrc1Wide, isSrc2Wide;

        BinOp(Opcode regular, Opcode _2addr, Opcode lit16, Opcode lit8, boolean isDstAndSrc1Wide, boolean isSrc2Wide) {
            this.regular = regular;
            this._2addr = _2addr;
            this.lit16 = lit16;
            this.lit8 = lit8;
            this.isDstAndSrc1Wide = isDstAndSrc1Wide;
            this.isSrc2Wide = isSrc2Wide;
        }

        BinOp(Opcode regular, Opcode _2addr, boolean isDstAndSrc1Wide, boolean isSrc2Wide) {
            this(regular, _2addr, null, null, isDstAndSrc1Wide, isSrc2Wide);
        }
    }

    public CodeBuilder binop(BinOp op, int dsr_reg_or_pair,
                             int first_src_reg_or_pair, int second_src_reg_or_pair) {
        if (op.regular == null) {
            throw new IllegalArgumentException("there is no regular version of " + op);
        }
        return f23x(op.regular, dsr_reg_or_pair, op.isDstAndSrc1Wide,
                first_src_reg_or_pair, op.isDstAndSrc1Wide,
                second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder binop_2addr(BinOp op, int dst_and_first_src_reg_or_pair,
                                   int second_src_reg_or_pair) {
        if (op._2addr == null) {
            throw new IllegalArgumentException("there is no 2addr version of " + op);
        }
        return f12x(op._2addr, dst_and_first_src_reg_or_pair,
                op.isDstAndSrc1Wide, second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder binop_lit16(BinOp op, int dst_reg, int src_reg, int value) {
        if (op.lit16 == null) {
            throw new IllegalArgumentException("there is no lit16 version of " + op);
        }
        return f22s(op.lit16, dst_reg, false, src_reg, false, value);
    }

    public CodeBuilder binop_lit8(BinOp op, int dst_reg, int src_reg, int value) {
        if (op.lit8 == null) {
            throw new IllegalArgumentException("there is no lit8 version of " + op);
        }
        return f22b(op.lit8, dst_reg, false, src_reg, false, value);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_count, int arg_reg1,
                                          int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        f45cc(Opcode.INVOKE_POLYMORPHIC, method, proto, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add_outs(arg_count);
        return this;
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_reg1,
                                          int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return invoke_polymorphic(method, proto, 5, arg_reg1,
                arg_reg2, arg_reg3, arg_reg4, arg_reg5);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_reg1,
                                          int arg_reg2, int arg_reg3, int arg_reg4) {
        return invoke_polymorphic(method, proto, 4, arg_reg1,
                arg_reg2, arg_reg3, arg_reg4, 0);
    }

    public CodeBuilder invoke_polymorphic(
            MethodId method, ProtoId proto, int arg_reg1, int arg_reg2, int arg_reg3) {
        return invoke_polymorphic(method, proto, 3, arg_reg1,
                arg_reg2, arg_reg3, 0, 0);
    }

    public CodeBuilder invoke_polymorphic(
            MethodId method, ProtoId proto, int arg_reg1, int arg_reg2) {
        return invoke_polymorphic(method, proto, 2, arg_reg1,
                arg_reg2, 0, 0, 0);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_reg1) {
        return invoke_polymorphic(method, proto, 1, arg_reg1,
                0, 0, 0, 0);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto) {
        return invoke_polymorphic(method, proto, 0, 0,
                0, 0, 0, 0);
    }

    public CodeBuilder invoke_polymorphic_range(
            MethodId method, ProtoId proto, int arg_count, int first_arg_reg) {
        f4rcc(Opcode.INVOKE_POLYMORPHIC_RANGE, method, proto, arg_count, first_arg_reg);
        add_outs(arg_count);
        return this;
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_count, int arg_reg1,
                                     int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        f35c(Opcode.INVOKE_CUSTOM, callsite, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add_outs(arg_count);
        return this;
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_reg1, int arg_reg2,
                                     int arg_reg3, int arg_reg4, int arg_reg5) {
        return invoke_custom(callsite, 5,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_reg1,
                                     int arg_reg2, int arg_reg3, int arg_reg4) {
        return invoke_custom(callsite, 4,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, 0);
    }

    public CodeBuilder invoke_custom(
            CallSiteId callsite, int arg_reg1, int arg_reg2, int arg_reg3) {
        return invoke_custom(callsite, 3,
                arg_reg1, arg_reg2, arg_reg3, 0, 0);
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_reg1, int arg_reg2) {
        return invoke_custom(callsite, 2,
                arg_reg1, arg_reg2, 0, 0, 0);
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_reg1) {
        return invoke_custom(callsite, 1,
                arg_reg1, 0, 0, 0, 0);
    }

    public CodeBuilder invoke_custom(CallSiteId callsite) {
        return invoke_custom(callsite, 0,
                0, 0, 0, 0, 0);
    }

    public CodeBuilder invoke_custom_range(CallSiteId callsite, int arg_count, int first_arg_reg) {
        f3rc(Opcode.INVOKE_POLYMORPHIC_RANGE, callsite, arg_count, first_arg_reg);
        add_outs(arg_count);
        return this;
    }

    public CodeBuilder const_method_handle(int dst_reg, MethodHandleItem value) {
        return f21c(Opcode.CONST_METHOD_HANDLE, dst_reg, false, value);
    }

    public CodeBuilder const_method_type(int dst_reg, ProtoId value) {
        return f21c(Opcode.CONST_METHOD_TYPE, dst_reg, false, value);
    }
}
