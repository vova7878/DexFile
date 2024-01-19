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

import com.v7878.dex.CodeItem;
import com.v7878.dex.FieldId;
import com.v7878.dex.MethodHandleItem;
import com.v7878.dex.MethodId;
import com.v7878.dex.ProtoId;
import com.v7878.dex.TypeId;
import com.v7878.dex.bytecode.Format.Format10t;
import com.v7878.dex.bytecode.Format.Format10x;
import com.v7878.dex.bytecode.Format.Format11n;
import com.v7878.dex.bytecode.Format.Format11x;
import com.v7878.dex.bytecode.Format.Format12x;
import com.v7878.dex.bytecode.Format.Format20t;
import com.v7878.dex.bytecode.Format.Format21c;
import com.v7878.dex.bytecode.Format.Format21t21s;
import com.v7878.dex.bytecode.Format.Format22c;
import com.v7878.dex.bytecode.Format.Format22t22s;
import com.v7878.dex.bytecode.Format.Format22x;
import com.v7878.dex.bytecode.Format.Format23x;
import com.v7878.dex.bytecode.Format.Format30t;
import com.v7878.dex.bytecode.Format.Format31i31t;
import com.v7878.dex.bytecode.Format.Format32x;
import com.v7878.dex.bytecode.Format.Format35c;
import com.v7878.dex.bytecode.Format.Format3rc;
import com.v7878.dex.bytecode.Format.Format45cc;
import com.v7878.dex.bytecode.Format.Format4rcc;
import com.v7878.dex.util.MutableList;
import com.v7878.misc.Checks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CodeBuilder {

    private static class InternalLabel {
    }

    private final int registers_size, ins_size;
    private final List<Supplier<Instruction>> instructions;
    private final Map<Object, Integer> labels;
    private final boolean has_this;

    private int current_instruction, current_unit, max_outs;

    private CodeBuilder(int registers_size, int ins_size, boolean has_hidden_this) {
        this.has_this = has_hidden_this;
        this.registers_size = Checks.checkRange(registers_size, 0,
                (1 << 16) - (has_this ? 1 : 0)) + (has_this ? 1 : 0);
        this.ins_size = Checks.checkRange(ins_size, 0,
                registers_size + 1) + (has_this ? 1 : 0);
        instructions = new ArrayList<>();
        labels = new HashMap<>();
        current_instruction = 0;
        current_unit = 0;
    }

    private CodeItem end() {
        MutableList<Instruction> out = MutableList.empty();
        out.addAll(instructions.stream().map(Supplier::get).collect(Collectors.toList()));
        return new CodeItem(registers_size, ins_size, max_outs, out, null);
    }

    public static CodeItem build(int registers_size, int ins_size,
                                 boolean has_hidden_this, Consumer<CodeBuilder> consumer) {
        CodeBuilder builder = new CodeBuilder(registers_size, ins_size, has_hidden_this);
        consumer.accept(builder);
        return builder.end();
    }

    public static CodeItem build(int registers_size, int ins_size, Consumer<CodeBuilder> consumer) {
        CodeBuilder builder = new CodeBuilder(registers_size, ins_size, false);
        consumer.accept(builder);
        return builder.end();
    }

    public int v(int reg) {
        //all registers
        return Checks.checkRange(reg, 0, registers_size);
    }

    public int l(int reg) {
        //only local registers
        int locals = registers_size - ins_size;
        return Checks.checkRange(reg, 0, locals);
    }

    private int p(int reg, boolean include_this) {
        //only parameter registers
        int locals = registers_size - ins_size;
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
        return Checks.checkRange(reg, 0, Math.min(1 << width, registers_size));
    }

    private int check_reg_pair(int reg_pair, int width) {
        Checks.checkRange(reg_pair + 1, 1, registers_size - 1);
        return check_reg(reg_pair, width);
    }

    private int check_reg_or_pair(int reg_or_pair, int width, boolean isPair) {
        return isPair ? check_reg_pair(reg_or_pair, width) : check_reg(reg_or_pair, width);
    }

    private int check_reg_range(int first_reg, int reg_width, int count, int count_width) {
        Checks.checkRange(count, 0, 1 << count_width);
        if (count > 0) {
            count--;
            Checks.checkRange(first_reg + count, count, registers_size - count);
        }
        return check_reg(first_reg, reg_width);
    }

    private void add(Instruction instruction) {
        instructions.add(() -> instruction);
        current_instruction++;
        current_unit += instruction.units();
    }

    private <F extends Format> void add(F format, Function<F, Instruction> factory) {
        if (format.isPayload()) {
            throw new AssertionError();
        }
        instructions.add(() -> factory.apply(format));
        current_instruction++;
        current_unit += format.units();
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

    public CodeBuilder if_(boolean value, Consumer<CodeBuilder> true_branch,
                           Consumer<CodeBuilder> false_branch) {
        if (value) {
            true_branch.accept(this);
        } else {
            false_branch.accept(this);
        }
        return this;
    }

    public CodeBuilder add_raw(Instruction instruction) {
        add(instruction);
        return this;
    }

    private CodeBuilder f10x(Opcode op) {
        add(op.<Format10x>format().make());
        return this;
    }

    private CodeBuilder f12x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(op.<Format12x>format().make(
                check_reg_or_pair(reg_or_pair1, 4, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 4, is_reg2_wide)));
        return this;
    }

    //TODO: 11n

    private CodeBuilder f11x(Opcode op, int reg_or_pair, boolean is_reg_wide) {
        add(op.<Format11x>format().make(check_reg_or_pair(reg_or_pair, 8, is_reg_wide)));
        return this;
    }

    //TODO: 10t
    //TODO: 20t

    private CodeBuilder f22x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(op.<Format22x>format().make(
                check_reg_or_pair(reg_or_pair1, 8, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 16, is_reg2_wide)));
        return this;
    }

    //TODO: 21t
    //TODO: 21s
    //TODO: 21ih
    //TODO: 21lh
    //TODO: 21c
    //TODO: 22c

    public CodeBuilder f23x(Opcode op, int reg_or_pair1, boolean is_reg1_wide, int reg_or_pair2,
                            boolean is_reg2_wide, int reg_or_pair3, boolean is_reg3_wide) {
        add(op.<Format23x>format().make(
                check_reg_or_pair(reg_or_pair1, 8, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 8, is_reg2_wide),
                check_reg_or_pair(reg_or_pair3, 8, is_reg3_wide)));
        return this;
    }

    //TODO: 22b
    //TODO: 22t
    //TODO: 22s
    //TODO: 30t

    private CodeBuilder f32x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(op.<Format32x>format().make(
                check_reg_or_pair(reg_or_pair1, 16, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, 16, is_reg2_wide)));
        return this;
    }

    //TODO: 31i
    //TODO: 31t
    //TODO: 31c
    //TODO: 35c
    //TODO: 3rc
    //TODO: 45cc
    //TODO: 4rcc
    //TODO: 51l

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

    public CodeBuilder move_wide(int dsr_regr_pair, int src_regr_pair) {
        return f12x(Opcode.MOVE_WIDE, dsr_regr_pair, true, src_regr_pair, true);
    }

    public CodeBuilder move_wide_from16(int dsr_regr_pair, int src_regr_pair) {
        return f22x(Opcode.MOVE_WIDE_FROM16, dsr_regr_pair, true, src_regr_pair, true);
    }

    public CodeBuilder move_wide_16(int dsr_regr_pair, int src_regr_pair) {
        return f32x(Opcode.MOVE_WIDE_16, dsr_regr_pair, true, src_regr_pair, true);
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
        InstructionWriter.check_signed(value, 4);
        add(Opcode.CONST_4.<Format11n>format().make(
                check_reg(dst_reg, 4), value));
        return this;
    }

    public CodeBuilder const_16(int dst_reg, int value) {
        InstructionWriter.check_signed(value, 16);
        add(Opcode.CONST_16.<Format21t21s>format().make(
                check_reg(dst_reg, 8), value));
        return this;
    }

    public CodeBuilder const_(int dst_reg, int value) {
        add(Opcode.CONST.<Format31i31t>format().make(
                check_reg(dst_reg, 8), value));
        return this;
    }

    public CodeBuilder const_string(int dst_reg, String value) {
        add(Opcode.CONST_STRING.<Format21c>format().make(
                check_reg(dst_reg, 8), value));
        return this;
    }

    public CodeBuilder const_class(int dst_reg, TypeId value) {
        add(Opcode.CONST_CLASS.<Format21c>format().make(
                check_reg(dst_reg, 8), value));
        return this;
    }

    public CodeBuilder monitor_enter(int ref_reg) {
        return f11x(Opcode.MONITOR_ENTER, ref_reg, false);
    }

    public CodeBuilder monitor_exit(int ref_reg) {
        return f11x(Opcode.MONITOR_EXIT, ref_reg, false);
    }

    public CodeBuilder check_cast(int ref_reg, TypeId value) {
        add(Opcode.CHECK_CAST.<Format21c>format().make(
                check_reg(ref_reg, 8), value));
        return this;
    }

    public CodeBuilder throw_(int ex_reg) {
        return f11x(Opcode.THROW, ex_reg, false);
    }

    private CodeBuilder goto_(Object label) {
        int start_unit = current_unit;
        add(Opcode.GOTO.<Format10t>format(), format -> {
            int branch_offset = getLabelBranchOffset(label, start_unit);
            InstructionWriter.check_signed(branch_offset, 8);
            return format.make(branch_offset);
        });
        return this;
    }

    public CodeBuilder goto_(String label) {
        return goto_((Object) label);
    }

    private CodeBuilder goto_16(Object label) {
        int start_unit = current_unit;
        add(Opcode.GOTO_16.<Format20t>format(), format -> {
            int branch_offset = getLabelBranchOffset(label, start_unit);
            InstructionWriter.check_signed(branch_offset, 16);
            return format.make(branch_offset);
        });
        return this;
    }

    public CodeBuilder goto_16(String label) {
        return goto_16((Object) label);
    }

    private CodeBuilder goto_32(Object label) {
        int start_unit = current_unit;
        add(Opcode.GOTO_32.<Format30t>format(), format -> {
            int branch_offset = getLabelBranchOffset(label, start_unit, true);
            InstructionWriter.check_signed(branch_offset, 32);
            return format.make(branch_offset);
        });
        return this;
    }

    public CodeBuilder goto_32(String label) {
        return goto_32((Object) label);
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

    private CodeBuilder if_test(Test test, int first_reg_to_test, int second_reg_to_test, Object label) {
        int start_unit = current_unit;
        add(test.test.<Format22t22s>format(), format -> {
            int branch_offset = getLabelBranchOffset(label, start_unit);
            InstructionWriter.check_signed(branch_offset, 16);
            return format.make(check_reg(first_reg_to_test, 4),
                    check_reg(second_reg_to_test, 4), branch_offset);
        });
        return this;
    }

    public CodeBuilder if_test(Test test, int first_reg_to_test, int second_reg_to_test, String label) {
        return if_test(test, first_reg_to_test, second_reg_to_test, (Object) label);
    }

    public CodeBuilder if_test_else(Test test, int first_reg_to_test, int second_reg_to_test,
                                    Consumer<CodeBuilder> true_branch, Consumer<CodeBuilder> false_branch) {
        InternalLabel true_ = new InternalLabel();
        InternalLabel end = new InternalLabel();

        if_test(test, first_reg_to_test, second_reg_to_test, true_);
        false_branch.accept(this);
        goto_32(end);
        putLabel(true_);
        true_branch.accept(this);
        putLabel(end);

        return this;
    }

    private CodeBuilder if_testz(Test test, int reg_to_test, Object label) {
        int start_unit = current_unit;
        add(test.testz.<Format21t21s>format(), format -> {
            int branch_offset = getLabelBranchOffset(label, start_unit);
            InstructionWriter.check_signed(branch_offset, 16);
            return format.make(check_reg(reg_to_test, 8), branch_offset);
        });
        return this;
    }

    public CodeBuilder if_testz(Test test, int reg_to_test, String label) {
        return if_testz(test, reg_to_test, (Object) label);
    }

    public CodeBuilder if_testz_else(Test test, int reg_to_test, Consumer<CodeBuilder> true_branch,
                                     Consumer<CodeBuilder> false_branch) {
        InternalLabel true_ = new InternalLabel();
        InternalLabel end = new InternalLabel();

        if_testz(test, reg_to_test, true_);
        false_branch.accept(this);
        goto_32(end);
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
        add(op.iop.<Format22c>format().make(
                check_reg_or_pair(value_reg_or_pair, 4, op.isWide),
                check_reg(object_reg, 4), instance_field));
        return this;
    }

    public CodeBuilder sop(Op op, int value_reg_or_pair, FieldId static_field) {
        add(op.sop.<Format21c>format().make(
                check_reg_or_pair(value_reg_or_pair, 8, op.isWide), static_field));
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
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(kind.regular.<Format35c>format().make(arg_count,
                method, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5));
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

    public CodeBuilder invoke_range(InvokeKind kind, MethodId method, int arg_count, int first_arg_reg) {
        check_reg_range(first_arg_reg, 16, arg_count, 8);
        add(kind.range.<Format3rc>format().make(arg_count, method, first_arg_reg));
        add_outs(arg_count);
        return this;
    }

    public enum RawUnOp {
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

        RawUnOp(Opcode opcode, boolean isDstWide, boolean isSrcWide) {
            this.opcode = opcode;
            this.isDstWide = isDstWide;
            this.isSrcWide = isSrcWide;
        }
    }

    public CodeBuilder raw_unop(RawUnOp op, int dsr_reg_or_pair, int src_reg_or_pair) {
        return f12x(op.opcode, dsr_reg_or_pair, op.isDstWide, src_reg_or_pair, op.isSrcWide);
    }

    public enum RawBinOp {
        ADD_INT(Opcode.ADD_INT, Opcode.ADD_INT_2ADDR, false, false),
        SUB_INT(Opcode.SUB_INT, Opcode.SUB_INT_2ADDR, false, false),
        MUL_INT(Opcode.MUL_INT, Opcode.MUL_INT_2ADDR, false, false),
        DIV_INT(Opcode.DIV_INT, Opcode.DIV_INT_2ADDR, false, false),
        REM_INT(Opcode.REM_INT, Opcode.REM_INT_2ADDR, false, false),
        AND_INT(Opcode.AND_INT, Opcode.AND_INT_2ADDR, false, false),
        OR_INT(Opcode.OR_INT, Opcode.OR_INT_2ADDR, false, false),
        XOR_INT(Opcode.XOR_INT, Opcode.XOR_INT_2ADDR, false, false),
        SHL_INT(Opcode.SHL_INT, Opcode.SHL_INT_2ADDR, false, false),
        SHR_INT(Opcode.SHR_INT, Opcode.SHR_INT_2ADDR, false, false),
        USHR_INT(Opcode.USHR_INT, Opcode.USHR_INT_2ADDR, false, false),

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

        private final Opcode regular, _2addr;
        private final boolean isDstAndSrc1Wide, isSrc2Wide;

        RawBinOp(Opcode regular, Opcode _2addr, boolean isDstAndSrc1Wide, boolean isSrc2Wide) {
            this.regular = regular;
            this._2addr = _2addr;
            this.isDstAndSrc1Wide = isDstAndSrc1Wide;
            this.isSrc2Wide = isSrc2Wide;
        }
    }

    public CodeBuilder raw_binop(RawBinOp op, int dsr_reg_or_pair,
                                 int first_src_reg_or_pair, int second_src_reg_or_pair) {
        return f23x(op.regular, dsr_reg_or_pair, op.isDstAndSrc1Wide,
                first_src_reg_or_pair, op.isDstAndSrc1Wide,
                second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder raw_binop_2addr(RawBinOp op, int dst_and_first_src_reg_or_pair,
                                       int second_src_reg_or_pair) {
        return f12x(op._2addr, dst_and_first_src_reg_or_pair,
                op.isDstAndSrc1Wide, second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_count, int arg_reg1,
                                          int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(Opcode.INVOKE_POLYMORPHIC.<Format45cc>format().make(arg_count,
                method, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5, proto));
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
        check_reg_range(first_arg_reg, 16, arg_count, 8);
        add(Opcode.INVOKE_POLYMORPHIC_RANGE.<Format4rcc>format()
                .make(arg_count, method, first_arg_reg, proto));
        add_outs(arg_count);
        return this;
    }

    public CodeBuilder const_method_handle(int dst_reg, MethodHandleItem value) {
        add(Opcode.CONST_METHOD_HANDLE.<Format21c>format().make(
                check_reg(dst_reg, 8), value));
        return this;
    }

    public CodeBuilder const_method_type(int dst_reg, ProtoId value) {
        add(Opcode.CONST_METHOD_TYPE.<Format21c>format().make(
                check_reg(dst_reg, 8), value));
        return this;
    }
}
