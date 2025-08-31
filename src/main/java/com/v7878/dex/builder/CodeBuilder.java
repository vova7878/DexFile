package com.v7878.dex.builder;

import static com.v7878.dex.builder.CodeBuilder.Op.GET;
import static com.v7878.dex.builder.CodeBuilder.Op.GET_BOOLEAN;
import static com.v7878.dex.builder.CodeBuilder.Op.GET_BYTE;
import static com.v7878.dex.builder.CodeBuilder.Op.GET_CHAR;
import static com.v7878.dex.builder.CodeBuilder.Op.GET_OBJECT;
import static com.v7878.dex.builder.CodeBuilder.Op.GET_SHORT;
import static com.v7878.dex.builder.CodeBuilder.Op.GET_WIDE;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT_BOOLEAN;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT_BYTE;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT_CHAR;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT_OBJECT;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT_SHORT;
import static com.v7878.dex.builder.CodeBuilder.Op.PUT_WIDE;
import static com.v7878.dex.util.ShortyUtils.unrecognizedShorty;

import com.v7878.dex.Format;
import com.v7878.dex.Opcode;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ExceptionHandler;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.ArrayPayload;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.Instruction10t;
import com.v7878.dex.immutable.bytecode.Instruction10x;
import com.v7878.dex.immutable.bytecode.Instruction11n;
import com.v7878.dex.immutable.bytecode.Instruction11x;
import com.v7878.dex.immutable.bytecode.Instruction12x;
import com.v7878.dex.immutable.bytecode.Instruction20t;
import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction21ih;
import com.v7878.dex.immutable.bytecode.Instruction21lh;
import com.v7878.dex.immutable.bytecode.Instruction21s;
import com.v7878.dex.immutable.bytecode.Instruction21t;
import com.v7878.dex.immutable.bytecode.Instruction22b;
import com.v7878.dex.immutable.bytecode.Instruction22c22cs;
import com.v7878.dex.immutable.bytecode.Instruction22s;
import com.v7878.dex.immutable.bytecode.Instruction22t;
import com.v7878.dex.immutable.bytecode.Instruction22x;
import com.v7878.dex.immutable.bytecode.Instruction23x;
import com.v7878.dex.immutable.bytecode.Instruction30t;
import com.v7878.dex.immutable.bytecode.Instruction31c;
import com.v7878.dex.immutable.bytecode.Instruction31i;
import com.v7878.dex.immutable.bytecode.Instruction31t;
import com.v7878.dex.immutable.bytecode.Instruction32x;
import com.v7878.dex.immutable.bytecode.Instruction35c35mi35ms;
import com.v7878.dex.immutable.bytecode.Instruction3rc3rmi3rms;
import com.v7878.dex.immutable.bytecode.Instruction45cc;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;
import com.v7878.dex.immutable.bytecode.Instruction51l;
import com.v7878.dex.immutable.bytecode.PackedSwitchPayload;
import com.v7878.dex.immutable.bytecode.SparseSwitchPayload;
import com.v7878.dex.immutable.bytecode.SwitchElement;
import com.v7878.dex.immutable.debug.AdvancePC;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.immutable.debug.EndLocal;
import com.v7878.dex.immutable.debug.LineNumber;
import com.v7878.dex.immutable.debug.RestartLocal;
import com.v7878.dex.immutable.debug.SetEpilogueBegin;
import com.v7878.dex.immutable.debug.SetFile;
import com.v7878.dex.immutable.debug.SetPrologueEnd;
import com.v7878.dex.immutable.debug.StartLocal;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;
import com.v7878.dex.util.ShortyUtils;
import com.v7878.dex.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CodeBuilder {
    private static class InternalLabel {
    }

    private class BuilderTryItem {
        private final Object label1, label2, handlerLabel;
        private final TypeId exceptionType;
        private int start = -1, end = -1, handler = -1;

        BuilderTryItem(Object label1, Object label2,
                       TypeId exceptionType, Object handlerLabel) {
            this.label1 = label1;
            this.label2 = label2;
            this.handlerLabel = handlerLabel;
            this.exceptionType = exceptionType;
        }

        private void initLabels() {
            if (start < 0 || end < 0 || handler < 0) {
                int l1 = getLabelUnit(label1);
                int l2 = getLabelUnit(label2);
                int hl = getLabelUnit(handlerLabel);
                if (l1 < l2) {
                    start = l1;
                    end = l2;
                } else {
                    start = l2;
                    end = l1;
                }
                handler = hl;
            }
        }

        public int start() {
            initLabels();
            return start;
        }

        public int end() {
            initLabels();
            return end;
        }

        public int handler() {
            initLabels();
            return handler;
        }

        public TypeId exceptionType() {
            return exceptionType;
        }
    }

    private static int checkRange(int value, int start, int length) {
        if (length < 0 || value < start || value >= start + length) {
            throw new IndexOutOfBoundsException(
                    String.format("value %s out of range [%s, %<s + %s)",
                            value, start, length));
        }
        return value;
    }

    private final int regs_size, ins_size;
    private final List<Supplier<Instruction>> instructions;
    private final List<Runnable> delayed_actions;
    private final List<BuilderTryItem> try_items;
    private final List<DebugItem> debug_items;
    private final Map<Object, Integer> labels;
    private final boolean has_this;

    private int current_unit, current_debug_unit;

    private boolean generate_lines;

    private CodeBuilder(int regs_size, int ins_size, boolean add_hidden_this) {
        this.has_this = add_hidden_this;
        int this_reg = add_hidden_this ? 1 : 0;
        this.regs_size = checkRange(regs_size, 0, (1 << 16) - this_reg) + this_reg;
        this.ins_size = checkRange(ins_size, 0, regs_size + 1) + this_reg;
        this.instructions = new ArrayList<>();
        this.delayed_actions = new ArrayList<>();
        this.try_items = new ArrayList<>();
        this.debug_items = new ArrayList<>();
        this.labels = new HashMap<>();
        this.current_debug_unit = this.current_unit = 0;
        this.generate_lines = false;
    }

    // <------------------>
    //           <--->
    //           <------------->
    // <------------->
    //           <-------->
    //             |
    //             | (Find all borders)
    //             v
    // /--------|/---|/---|/---|
    //             |
    //             | (Cut at begin of each range)
    //             v
    // <--------//--->
    //           <--->
    //           <---//---//--->
    // <--------//---//--->
    //           <---//--->
    //             |
    //             | (merge elements with same start)
    //             v
    // <========><***><+++><--->

    private static class TryContainer {
        final Set<TypeId> exceptions = new HashSet<>();
        final List<ExceptionHandler> handlers = new ArrayList<>();
        Integer catch_all_address = null;
    }

    private static List<TryBlock> mergeTryItems(List<BuilderTryItem> try_items) {
        int[] borders;
        {
            // TODO: Maybe there is something better for storing integers?
            var borders_set = new TreeSet<Integer>();
            for (int i = 0; i < try_items.size(); ) {
                BuilderTryItem block = try_items.get(i);
                int start = block.start(), end = block.end();
                if (end <= start) {
                    assert start == end; // It`s always true, but I want to make it explicit
                    try_items.remove(i);
                    continue;
                }
                borders_set.add(start);
                borders_set.add(end);
                i++;
            }
            borders = borders_set.stream().mapToInt(v -> v).toArray();
        }
        if (borders.length == 0) return Collections.emptyList();
        int elements_size = borders.length - 1;

        var elements = new SparseArray<TryContainer>();
        for (int i = 0; i < elements_size; i++) {
            elements.put(borders[i], new TryContainer());
        }

        for (var item : try_items) {
            var exception = item.exceptionType();
            var handler_address = item.handler();
            var handler = exception == null ? null :
                    ExceptionHandler.of(exception, handler_address);

            int start_index = Arrays.binarySearch(borders, item.start());
            int end_index = Arrays.binarySearch(borders, item.end());
            assert start_index >= 0 && end_index > start_index;

            for (int i = start_index; i < end_index; i++) {
                var position = borders[i];
                var container = elements.get(position);
                assert container != null;
                if (exception == null) {
                    if (container.catch_all_address != null) {
                        throw new IllegalArgumentException(String.format(
                                "More than one catch-all handler for code position %d", position));
                    }
                    container.catch_all_address = handler_address;
                } else {
                    if (container.exceptions.contains(exception)) {
                        throw new IllegalArgumentException(String.format(
                                "More than one catch handler of type %s for code position %d",
                                exception.getDescriptor(), position));
                    }
                    container.exceptions.add(exception);
                    container.handlers.add(handler);
                }
            }
        }

        var out = new ArrayList<TryBlock>(elements_size);
        for (int i = 0; i < elements_size; i++) {
            var container = elements.valueAt(i);
            if (container.catch_all_address != null || container.handlers.size() > 0) {
                out.add(TryBlock.of(borders[i], borders[i + 1] - borders[i],
                        container.catch_all_address, container.handlers));
            }
        }
        return out;
    }

    private MethodImplementation finish() {
        delayed_actions.forEach(Runnable::run);

        List<Instruction> insns = instructions.stream()
                .map(Supplier::get).collect(Collectors.toList());

        List<TryBlock> try_blocks = mergeTryItems(try_items);

        return MethodImplementation.of(regs_size, insns, try_blocks, debug_items);
    }

    public static MethodImplementation build(int regs_size, int ins_size, boolean add_hidden_this,
                                             Consumer<CodeBuilder> consumer) {
        var builder = new CodeBuilder(regs_size, ins_size, add_hidden_this);
        consumer.accept(builder);
        return builder.finish();
    }

    public static MethodImplementation build(int regs_size, int ins_size,
                                             Consumer<CodeBuilder> consumer) {
        return build(regs_size, 0, false, consumer);
    }

    public static MethodImplementation build(int regs_size, Consumer<CodeBuilder> consumer) {
        return build(regs_size, 0, consumer);
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

    public CodeBuilder if_(boolean value, Consumer<CodeBuilder> true_branch) {
        if (value) {
            true_branch.accept(this);
        }
        return this;
    }

    public CodeBuilder commit(Consumer<CodeBuilder> branch) {
        branch.accept(this);
        return this;
    }

    public int v(int reg) {
        //all registers
        return checkRange(reg, 0, regs_size);
    }

    public int l(int reg) {
        //only local registers
        int locals = regs_size - ins_size;
        return checkRange(reg, 0, locals);
    }

    private int p(int reg, boolean include_this) {
        //only parameter registers
        int this_reg = include_this ? 1 : 0;
        int locals = regs_size - ins_size;
        return locals + checkRange(reg, 0, ins_size - this_reg) + this_reg;
    }

    public int p(int reg) {
        //only parameter registers without hidden this
        return p(reg, has_this);
    }

    public int this_() {
        if (!has_this) {
            throw new IllegalArgumentException("Builder has no 'this' register");
        }
        return p(0, false);
    }

    private int check_reg(int reg) {
        return checkRange(reg, 0, regs_size);
    }

    private int check_reg_pair(int reg_pair) {
        checkRange(reg_pair + 1, 1, regs_size - 1);
        return check_reg(reg_pair);
    }

    private int check_reg_or_pair(int reg_or_pair, boolean isPair) {
        return isPair ? check_reg_pair(reg_or_pair) : check_reg(reg_or_pair);
    }

    private int check_reg_empty_range(int first_reg) {
        return checkRange(first_reg, 0, regs_size + 1);
    }

    @SuppressWarnings("UnusedReturnValue")
    private int check_reg_range(int first_reg, int count) {
        if (count == 0) {
            return check_reg_empty_range(first_reg);
        }
        count--;
        checkRange(first_reg + count, count, regs_size - count);
        return check_reg(first_reg);
    }

    private void add(Supplier<Instruction> instruction, int unit_count) {
        if (generate_lines) {
            line(instructions.size() + 1);
        }
        instructions.add(instruction);
        current_unit += unit_count;
    }

    private void add(Instruction instruction) {
        add(() -> instruction, instruction.getUnitCount());
    }

    private void add(Format format, Supplier<Instruction> factory) {
        if (format.isPayload()) {
            throw new AssertionError();
        }
        add(() -> {
            var instruction = factory.get();
            assert instruction.getOpcode().format() == format;
            return instruction;
        }, format.getUnitCount());
    }

    private void addDelayedAction(Runnable action) {
        delayed_actions.add(action);
    }

    private void putLabel(Object label) {
        if (labels.putIfAbsent(Objects.requireNonNull(label), current_unit) != null) {
            throw new IllegalArgumentException("Label " + label + " already exists");
        }
    }

    private int getLabelUnit(Object label) {
        Integer unit = labels.get(label);
        if (unit == null) {
            throw new IllegalStateException("Can`t find label: " + label);
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
            throw new IllegalStateException("Zero branch offset is not allowed");
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
        try_items.add(new BuilderTryItem(start, end, exceptionType, handler));
    }

    private void try_catch_internal(Object start, Object end, TypeId exceptionType, Object handler) {
        Objects.requireNonNull(exceptionType);
        addTryBlock(start, end, exceptionType, handler);
    }

    public CodeBuilder try_catch(String label1, String label2, TypeId exceptionType, String handler) {
        try_catch_internal(label1, label2, exceptionType, handler);
        return this;
    }

    public CodeBuilder try_catch(String label1, String label2, TypeId exceptionType) {
        InternalLabel handler = new InternalLabel();
        try_catch_internal(label1, label2, exceptionType, handler);
        putLabel(handler);
        return this;
    }

    private void try_catch_all_internal(Object start, Object end, Object handler) {
        addTryBlock(start, end, null, handler);
    }

    public CodeBuilder try_catch_all(String start, String end, String handler) {
        try_catch_all_internal(start, end, handler);
        return this;
    }

    public CodeBuilder try_catch_all(String start, String end) {
        InternalLabel handler = new InternalLabel();
        try_catch_all_internal(start, end, handler);
        putLabel(handler);
        return this;
    }

    private CodeBuilder addDebugItem(DebugItem item) {
        Objects.requireNonNull(item);
        if (current_debug_unit != current_unit) {
            debug_items.add(AdvancePC.of(current_unit - current_debug_unit));
            current_debug_unit = current_unit;
        }
        debug_items.add(item);
        return this;
    }

    public CodeBuilder line(int line) {
        return addDebugItem(LineNumber.of(line));
    }

    public CodeBuilder prologue() {
        return addDebugItem(SetPrologueEnd.INSTANCE);
    }

    public CodeBuilder epilogue() {
        return addDebugItem(SetEpilogueBegin.INSTANCE);
    }

    public CodeBuilder source(String name) {
        return addDebugItem(SetFile.of(name));
    }

    public CodeBuilder local(int register, String name, TypeId type, String signature) {
        return addDebugItem(StartLocal.of(register, name, type, signature));
    }

    public CodeBuilder local(int register, String name, TypeId type) {
        return local(register, name, type, null);
    }

    public CodeBuilder end_local(int register) {
        return addDebugItem(EndLocal.of(register));
    }

    public CodeBuilder restart_local(int register) {
        return addDebugItem(RestartLocal.of(register));
    }

    public CodeBuilder generate_lines() {
        generate_lines = true;
        return this;
    }

    private void format_35c_checks(int arg_count, int arg_reg1, int arg_reg2,
                                   int arg_reg3, int arg_reg4, int arg_reg5) {
        checkRange(arg_count, 0, 6);
        if (arg_count == 5) check_reg(arg_reg5);
        else if (arg_reg5 != 0) throw new IllegalArgumentException(
                "arg_count < 5, but arg_reg5 != 0");

        if (arg_count >= 4) check_reg(arg_reg4);
        else if (arg_reg4 != 0) throw new IllegalArgumentException(
                "arg_count < 4, but arg_reg4 != 0");

        if (arg_count >= 3) check_reg(arg_reg3);
        else if (arg_reg3 != 0) throw new IllegalArgumentException(
                "arg_count < 3, but arg_reg3 != 0");

        if (arg_count >= 2) check_reg(arg_reg2);
        else if (arg_reg2 != 0) throw new IllegalArgumentException(
                "arg_count < 2, but arg_reg2 != 0");

        if (arg_count >= 1) check_reg(arg_reg1);
        else if (arg_reg1 != 0) throw new IllegalArgumentException(
                "arg_count == 0, but arg_reg1 != 0");
    }

    public CodeBuilder add_raw(Instruction instruction) {
        add(instruction);
        return this;
    }

    // <ØØ|op> op
    private CodeBuilder f10x(Opcode op) {
        add(Instruction10x.of(op));
        return this;
    }

    // <B|A|op> op vA, vB
    private CodeBuilder f12x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(Instruction12x.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide)));
        return this;
    }

    // <B|A|op> op vA, #+B
    private CodeBuilder f11n(@SuppressWarnings("SameParameterValue") Opcode op, int reg_or_pair,
                             @SuppressWarnings("SameParameterValue") boolean is_reg_wide, int value) {
        add(Instruction11n.of(op,
                check_reg_or_pair(reg_or_pair, is_reg_wide), value));
        return this;
    }

    // <AA|op> op vAA
    private CodeBuilder f11x(Opcode op, int reg_or_pair, boolean is_reg_wide) {
        add(Instruction11x.of(op, check_reg_or_pair(reg_or_pair, is_reg_wide)));
        return this;
    }

    // <AA|op> op +AA
    private CodeBuilder f10t(@SuppressWarnings("SameParameterValue") Opcode op, IntSupplier value) {
        add(op.format(), () -> Instruction10t.of(op, value.getAsInt()));
        return this;
    }

    // <ØØ|op AAAA> op +AAAA
    private CodeBuilder f20t(@SuppressWarnings("SameParameterValue") Opcode op, IntSupplier value) {
        add(op.format(), () -> Instruction20t.of(op, value.getAsInt()));
        return this;
    }

    // <AA|op BBBB> op AA, @BBBB
    // TODO: 20bc

    // <AA|op BBBB> op vAA, vBBBB
    private CodeBuilder f22x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(Instruction22x.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide)));
        return this;
    }

    // <AA|op BBBB> op vAA, +BBBB
    private CodeBuilder f21t(Opcode op, int reg_or_pair, @SuppressWarnings("SameParameterValue") boolean is_reg_wide, IntSupplier value) {
        check_reg_or_pair(reg_or_pair, is_reg_wide);
        add(op.format(), () -> Instruction21t.of(op, reg_or_pair, value.getAsInt()));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB
    private CodeBuilder f21s(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
        add(Instruction21s.of(op,
                check_reg_or_pair(reg_or_pair, is_reg_wide), value));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB0000
    private CodeBuilder f21ih(@SuppressWarnings("SameParameterValue") Opcode op, int reg, int value) {
        add(Instruction21ih.of(op, check_reg(reg), value));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB000000000000
    private CodeBuilder f21lh(@SuppressWarnings("SameParameterValue") Opcode op, int reg_pair, long value) {
        add(Instruction21lh.of(op, check_reg_pair(reg_pair), value));
        return this;
    }

    // <AA|op BBBB> op vAA, @BBBB
    private CodeBuilder f21c(Opcode op, int reg_or_pair, boolean is_reg_wide, Object constant) {
        add(Instruction21c.of(op,
                check_reg_or_pair(reg_or_pair, is_reg_wide), constant));
        return this;
    }

    // <AA|op CC|BB> op vAA, vBB, vCC
    private CodeBuilder f23x(Opcode op, int reg_or_pair1, boolean is_reg1_wide, int reg_or_pair2,
                             boolean is_reg2_wide, int reg_or_pair3, boolean is_reg3_wide) {
        add(Instruction23x.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide),
                check_reg_or_pair(reg_or_pair3, is_reg3_wide)));
        return this;
    }

    // <AA|op CC|BB> op vAA, vBB, #+CC
    private CodeBuilder f22b(Opcode op, int reg_or_pair1, @SuppressWarnings("SameParameterValue") boolean is_reg1_wide,
                             int reg_or_pair2, @SuppressWarnings("SameParameterValue") boolean is_reg2_wide, int value) {
        add(Instruction22b.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide), value));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, +CCCC
    private CodeBuilder f22t(Opcode op, int reg_or_pair1, @SuppressWarnings("SameParameterValue") boolean is_reg1_wide,
                             int reg_or_pair2, @SuppressWarnings("SameParameterValue") boolean is_reg2_wide, IntSupplier value) {
        check_reg_or_pair(reg_or_pair1, is_reg1_wide);
        check_reg_or_pair(reg_or_pair2, is_reg2_wide);
        add(op.format(), () -> Instruction22t.of(op, reg_or_pair1, reg_or_pair2, value.getAsInt()));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, #+CCCC
    private CodeBuilder f22s(Opcode op, int reg_or_pair1, @SuppressWarnings("SameParameterValue") boolean is_reg1_wide,
                             int reg_or_pair2, @SuppressWarnings("SameParameterValue") boolean is_reg2_wide, int value) {
        add(Instruction22s.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide), value));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, @CCCC
    private CodeBuilder f22c(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, @SuppressWarnings("SameParameterValue") boolean is_reg2_wide, Object constant) {
        add(Instruction22c22cs.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide), constant));
        return this;
    }

    // <ØØ|op AAAAlo AAAAhi> op +AAAAAAAA
    private CodeBuilder f30t(@SuppressWarnings("SameParameterValue") Opcode op, IntSupplier value) {
        add(op.format(), () -> Instruction30t.of(op, value.getAsInt()));
        return this;
    }

    // <ØØ|op AAAA BBBB> op vAAAA, vBBBB
    private CodeBuilder f32x(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide) {
        add(Instruction32x.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide)));
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, #+BBBBBBBB
    private CodeBuilder f31i(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
        add(Instruction31i.of(op,
                check_reg_or_pair(reg_or_pair, is_reg_wide), value));
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, +BBBBBBBB
    private CodeBuilder f31t(Opcode op, int reg_or_pair, @SuppressWarnings("SameParameterValue") boolean is_reg_wide, IntSupplier value) {
        check_reg_or_pair(reg_or_pair, is_reg_wide);
        add(op.format(), () -> Instruction31t.of(op, reg_or_pair, value.getAsInt()));
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, @BBBBBBBB
    private CodeBuilder f31c(@SuppressWarnings("SameParameterValue") Opcode op, int reg_or_pair,
                             @SuppressWarnings("SameParameterValue") boolean is_reg_wide, Object constant) {
        add(Instruction31c.of(op,
                check_reg_or_pair(reg_or_pair, is_reg_wide), constant));
        return this;
    }

    // <A|G|op BBBB F|E|D|C> [A] op {vC, vD, vE, vF, vG}, @BBBB
    private CodeBuilder f35c(Opcode op, Object constant, int arg_count, int arg_reg1,
                             int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(Instruction35c35mi35ms.of(op, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5, constant));
        return this;
    }

    // <AA|op BBBB CCCC> op {vCCCC .. vNNNN}, @BBBB (where NNNN = CCCC+AA-1)
    private CodeBuilder f3rc(Opcode op, Object constant, int arg_count, int first_arg_reg) {
        check_reg_range(first_arg_reg, arg_count);
        add(Instruction3rc3rmi3rms.of(op, arg_count, first_arg_reg, constant));
        return this;
    }

    // <A|G|op BBBB F|E|D|C HHHH> [A] op {vC, vD, vE, vF, vG}, @BBBB
    private CodeBuilder f45cc(@SuppressWarnings("SameParameterValue") Opcode op,
                              Object constant1, Object constant2, int arg_count, int arg_reg1,
                              int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(Instruction45cc.of(op, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5, constant1, constant2));
        return this;
    }

    // <AA|op BBBB CCCC HHHH> op {vCCCC .. vNNNN}, @BBBB, @HHHH (where NNNN = CCCC+AA-1)
    private CodeBuilder f4rcc(@SuppressWarnings("SameParameterValue") Opcode op, Object constant1,
                              Object constant2, int arg_count, int first_arg_reg) {
        check_reg_range(first_arg_reg, arg_count);
        add(Instruction4rcc.of(op, arg_count, first_arg_reg, constant1, constant2));
        return this;
    }

    // <AA|op BBBBlolo BBBBlohi BBBBhilo BBBBhihi> op vAA, #+BBBBBBBBBBBBBBBB
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f51l(Opcode op, int reg_pair, long value) {
        add(Instruction51l.of(op, check_reg_pair(reg_pair), value));
        return this;
    }

    private void align_current_unit2() {
        if ((current_unit & 1) != 0) {
            nop();
        }
    }

    private void check_current_unit_alignment2() {
        if ((current_unit & 1) != 0) {
            throw new IllegalStateException("Current position is not aligned by 2 code units");
        }
    }

    private void packed_switch_payload(NavigableSet<SwitchElement> elements) {
        check_current_unit_alignment2();
        // TODO: Add constructor variant without checks?
        add(PackedSwitchPayload.of(elements));
    }

    private void sparse_switch_payload(NavigableSet<SwitchElement> elements) {
        check_current_unit_alignment2();
        // TODO: Add constructor variant without checks?
        add(SparseSwitchPayload.of(elements));
    }

    private void fill_array_data_payload(int element_width, List<? extends Number> data) {
        check_current_unit_alignment2();
        // TODO: Add constructor variant without checks?
        add(ArrayPayload.of(element_width, data));
    }

    @SuppressWarnings("UnusedReturnValue")
    public CodeBuilder nop() {
        return f10x(Opcode.NOP);
    }

    public CodeBuilder raw_move(int dst_reg, int src_reg) {
        return f12x(Opcode.MOVE, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_from16(int dst_reg, int src_reg) {
        return f22x(Opcode.MOVE_FROM16, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_16(int dst_reg, int src_reg) {
        return f32x(Opcode.MOVE_16, dst_reg, false, src_reg, false);
    }

    public CodeBuilder move(int dst_reg, int src_reg) {
        if (src_reg < 1 << 4 && dst_reg < 1 << 4) {
            return raw_move(dst_reg, src_reg);
        }
        if (src_reg < 1 << 8) {
            return raw_move_from16(dst_reg, src_reg);
        }
        return raw_move_16(dst_reg, src_reg);
    }

    public CodeBuilder raw_move_wide(int dst_reg_pair, int src_reg_pair) {
        return f12x(Opcode.MOVE_WIDE, dst_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder raw_move_wide_from16(int dst_reg_pair, int src_reg_pair) {
        return f22x(Opcode.MOVE_WIDE_FROM16, dst_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder raw_move_wide_16(int dst_reg_pair, int src_reg_pair) {
        return f32x(Opcode.MOVE_WIDE_16, dst_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder move_wide(int dst_reg_pair, int src_reg_pair) {
        if (src_reg_pair < 1 << 4 && dst_reg_pair < 1 << 4) {
            return raw_move_wide(dst_reg_pair, src_reg_pair);
        }
        if (src_reg_pair < 1 << 8) {
            return raw_move_wide_from16(dst_reg_pair, src_reg_pair);
        }
        return raw_move_wide_16(dst_reg_pair, src_reg_pair);
    }

    public CodeBuilder raw_move_object(int dst_reg, int src_reg) {
        return f12x(Opcode.MOVE_OBJECT, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_object_from16(int dst_reg, int src_reg) {
        return f22x(Opcode.MOVE_OBJECT_FROM16, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_object_16(int dst_reg, int src_reg) {
        return f32x(Opcode.MOVE_OBJECT_16, dst_reg, false, src_reg, false);
    }

    public CodeBuilder move_object(int dst_reg, int src_reg) {
        if (src_reg < 1 << 4 && dst_reg < 1 << 4) {
            return raw_move_object(dst_reg, src_reg);
        }
        if (src_reg < 1 << 8) {
            return raw_move_object_from16(dst_reg, src_reg);
        }
        return raw_move_object_16(dst_reg, src_reg);
    }

    public CodeBuilder move_shorty(char shorty, int dst_reg_or_pair, int src_reg_or_pair) {
        return switch (shorty) {
            case 'V' -> {
                check_reg_empty_range(dst_reg_or_pair);
                check_reg_empty_range(src_reg_or_pair);
                yield this;
            }
            case 'Z', 'B', 'C', 'S', 'I', 'F' -> move(dst_reg_or_pair, src_reg_or_pair);
            case 'J', 'D' -> move_wide(dst_reg_or_pair, src_reg_or_pair);
            case 'L' -> move_object(dst_reg_or_pair, src_reg_or_pair);
            default -> throw unrecognizedShorty(shorty);
        };
    }

    public CodeBuilder move_range(String shorty, int first_dst_reg, int first_src_reg) {
        char[] chars = shorty.toCharArray();
        int size = 0;
        for (char value : chars) {
            size += ShortyUtils.getRegisterCountWithCheck(value);
        }
        check_reg_range(first_dst_reg, size);
        check_reg_range(first_src_reg, size);
        int offset = 0;
        for (char value : chars) {
            move_shorty(value, first_dst_reg + offset,
                    first_src_reg + offset);
            offset += ShortyUtils.getRegisterCount(value);
        }
        return this;
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

    public CodeBuilder move_result_shorty(char shorty, int dst_reg_or_pair) {
        return switch (shorty) {
            case 'V' -> {
                check_reg_empty_range(dst_reg_or_pair);
                yield this;
            }
            case 'Z', 'B', 'C', 'S', 'I', 'F' -> move_result(dst_reg_or_pair);
            case 'J', 'D' -> move_result_wide(dst_reg_or_pair);
            case 'L' -> move_result_object(dst_reg_or_pair);
            default -> throw unrecognizedShorty(shorty);
        };
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

    public CodeBuilder return_shorty(char shorty, int return_value_reg_or_pair) {
        return switch (shorty) {
            case 'V' -> {
                check_reg_empty_range(return_value_reg_or_pair);
                yield return_void();
            }
            case 'Z', 'B', 'C', 'S', 'I', 'F' -> return_(return_value_reg_or_pair);
            case 'J', 'D' -> return_wide(return_value_reg_or_pair);
            case 'L' -> return_object(return_value_reg_or_pair);
            default -> throw unrecognizedShorty(shorty);
        };
    }

    public CodeBuilder raw_const_4(int dst_reg, int value) {
        return f11n(Opcode.CONST_4, dst_reg, false, value);
    }

    public CodeBuilder raw_const_16(int dst_reg, int value) {
        return f21s(Opcode.CONST_16, dst_reg, false, value);
    }

    public CodeBuilder raw_const(int dst_reg, int value) {
        return f31i(Opcode.CONST, dst_reg, false, value);
    }

    public CodeBuilder raw_const_high16(int dst_reg, int value) {
        return f21ih(Opcode.CONST_HIGH16, dst_reg, value);
    }

    private static boolean check_width_int(int value, int width) {
        int empty_width = 32 - width;
        return value << empty_width >> empty_width == value;
    }

    public CodeBuilder const_(int dst_reg, int value) {
        if (dst_reg < 1 << 4 && check_width_int(value, 4)) {
            return raw_const_4(dst_reg, value);
        }
        if (check_width_int(value, 16)) {
            return raw_const_16(dst_reg, value);
        }
        if ((value & 0xffff) == 0) {
            return raw_const_high16(dst_reg, value);
        }
        return raw_const(dst_reg, value);
    }

    public CodeBuilder raw_const_wide_16(int dst_reg_pair, int value) {
        return f21s(Opcode.CONST_WIDE_16, dst_reg_pair, true, value);
    }

    public CodeBuilder raw_const_wide_32(int dst_reg_pair, int value) {
        return f31i(Opcode.CONST_WIDE_32, dst_reg_pair, true, value);
    }

    public CodeBuilder raw_const_wide(int dst_reg_pair, long value) {
        return f51l(Opcode.CONST_WIDE, dst_reg_pair, value);
    }

    public CodeBuilder raw_const_wide_high16(int dst_reg_pair, long value) {
        return f21lh(Opcode.CONST_WIDE_HIGH16, dst_reg_pair, value);
    }

    private static boolean check_width_long(long value, int width) {
        int empty_width = 64 - width;
        return value << empty_width >> empty_width == value;
    }

    public CodeBuilder const_wide(int dst_reg_pair, long value) {
        if (check_width_long(value, 16)) {
            return raw_const_wide_16(dst_reg_pair, (int) value);
        }
        if (check_width_long(value, 32)) {
            return raw_const_wide_32(dst_reg_pair, (int) value);
        }
        if ((value & 0xffff_ffff_ffffL) == 0) {
            return raw_const_wide_high16(dst_reg_pair, value);
        }
        return raw_const_wide(dst_reg_pair, value);
    }

    public CodeBuilder const_string(int dst_reg, String value) {
        return f21c(Opcode.CONST_STRING, dst_reg, false, value);
    }

    // TODO: Should this instruction be explicitly stated here?
    //  When the constant number is exceeded,
    //  there should be an automatic correction of the instruction
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

    private CodeBuilder fill_array_data_internal(int arr_ref_reg, int element_width, List<? extends Number> data) {
        int start_unit = current_unit;
        InternalLabel payload = new InternalLabel();
        var insn = f31t(Opcode.FILL_ARRAY_DATA, arr_ref_reg, false,
                () -> getLabelBranchOffset(payload, start_unit));
        addDelayedAction(() -> {
            align_current_unit2();
            putLabel(payload);
            fill_array_data_payload(element_width, data);
        });
        return insn;
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, int element_width, Iterable<? extends Number> data) {
        var checked_data = Preconditions.checkArrayPayloadElements(element_width, ItemConverter.toList(data));
        return fill_array_data_internal(arr_ref_reg, element_width, checked_data);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, boolean[] data) {
        List<Byte> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add((byte) (value ? 1 : 0));
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 1, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, byte[] data) {
        List<Byte> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add(value);
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 1, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, short[] data) {
        List<Short> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add(value);
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 2, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, char[] data) {
        List<Short> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add((short) value);
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 2, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, int[] data) {
        List<Integer> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add(value);
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 4, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, float[] data) {
        List<Integer> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add(Float.floatToRawIntBits(value));
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 4, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, long[] data) {
        List<Long> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add(value);
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 8, list);
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, double[] data) {
        List<Long> list = new ArrayList<>(data.length);
        for (var value : data) {
            list.add(Double.doubleToRawLongBits(value));
        }
        list = Collections.unmodifiableList(list);
        return fill_array_data_internal(arr_ref_reg, 8, list);
    }

    public CodeBuilder throw_(int ex_reg) {
        return f11x(Opcode.THROW, ex_reg, false);
    }

    private CodeBuilder goto_internal(Object label) {
        //TODO: Generate smaller instructions if possible
        return raw_goto_32_internal(label);
    }

    public CodeBuilder goto_(String label) {
        return goto_internal(label);
    }

    private CodeBuilder raw_goto_internal(Object label) {
        int start_unit = current_unit;
        return f10t(Opcode.GOTO, () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder raw_goto(String label) {
        return raw_goto_internal(label);
    }

    private CodeBuilder raw_goto_16_internal(Object label) {
        int start_unit = current_unit;
        return f20t(Opcode.GOTO_16, () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder raw_goto_16(String label) {
        return raw_goto_16_internal(label);
    }

    private CodeBuilder raw_goto_32_internal(Object label) {
        int start_unit = current_unit;
        return f30t(Opcode.GOTO_32, () -> getLabelBranchOffset(label, start_unit, true));
    }

    public CodeBuilder raw_goto_32(String label) {
        return raw_goto_32_internal(label);
    }

    private CodeBuilder packed_switch_internal(int reg_to_test, int first_key, Object... labels) {
        assert first_key + labels.length >= first_key;
        int start_unit = current_unit;
        InternalLabel payload = new InternalLabel();
        f31t(Opcode.PACKED_SWITCH, reg_to_test, false,
                () -> getLabelBranchOffset(payload, start_unit));
        addDelayedAction(() -> {
            NavigableSet<SwitchElement> elements = new TreeSet<>();
            for (int i = 0; i < labels.length; i++) {
                elements.add(SwitchElement.of(first_key + i,
                        getLabelBranchOffset(labels[i], start_unit, true)));
            }
            elements = Collections.unmodifiableNavigableSet(elements);
            align_current_unit2();
            putLabel(payload);
            packed_switch_payload(elements);
        });
        return this;
    }

    private CodeBuilder sparse_switch_internal(int reg_to_test, int[] keys, Object... labels) {
        assert keys.length == labels.length;
        int start_unit = current_unit;
        InternalLabel payload = new InternalLabel();
        f31t(Opcode.SPARSE_SWITCH, reg_to_test, false,
                () -> getLabelBranchOffset(payload, start_unit));
        addDelayedAction(() -> {
            NavigableSet<SwitchElement> elements = new TreeSet<>();
            for (int i = 0; i < labels.length; i++) {
                elements.add(SwitchElement.of(keys[i],
                        getLabelBranchOffset(labels[i], start_unit, true)));
            }
            elements = Collections.unmodifiableNavigableSet(elements);
            align_current_unit2();
            putLabel(payload);
            sparse_switch_payload(elements);
        });
        return this;
    }

    public CodeBuilder switch_(int reg_to_test, Map<Integer, String> table) {
        check_reg(reg_to_test);
        var map = new SparseArray<String>(table.size());
        table.forEach((key, value) -> map.put(key, Objects.requireNonNull(value)));
        if (map.size() == 0) {
            return this;
        }
        if (map.size() <= 1 || (map.lastKey() - map.firstKey()) == (map.size() - 1)) {
            return packed_switch_internal(reg_to_test, map.firstKey(), map.valuesArray());
        }
        return sparse_switch_internal(reg_to_test, map.keysArray(), map.valuesArray());
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

    private CodeBuilder if_testz_internal(Test test, int reg_to_test, Object label) {
        int start_unit = current_unit;
        return f21t(test.testz, reg_to_test, false,
                () -> getLabelBranchOffset(label, start_unit));
    }

    public CodeBuilder if_testz(Test test, int reg_to_test, String label) {
        return if_testz_internal(test, reg_to_test, label);
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

    private static Op op_get_shorty(char shorty) {
        return switch (shorty) {
            case 'Z' -> GET_BOOLEAN;
            case 'B' -> GET_BYTE;
            case 'S' -> GET_SHORT;
            case 'C' -> GET_CHAR;
            case 'I', 'F' -> GET;
            case 'J', 'D' -> GET_WIDE;
            case 'L' -> GET_OBJECT;
            default -> throw unrecognizedShorty(shorty);
        };
    }

    private static Op op_put_shorty(char shorty) {
        return switch (shorty) {
            case 'Z' -> PUT_BOOLEAN;
            case 'B' -> PUT_BYTE;
            case 'S' -> PUT_SHORT;
            case 'C' -> PUT_CHAR;
            case 'I', 'F' -> PUT;
            case 'J', 'D' -> PUT_WIDE;
            case 'L' -> PUT_OBJECT;
            default -> throw unrecognizedShorty(shorty);
        };
    }

    public CodeBuilder aget(char shorty, int value_reg_or_pair, int array_reg, int index_reg) {
        return aop(op_get_shorty(shorty), value_reg_or_pair, array_reg, index_reg);
    }

    public CodeBuilder aput(char shorty, int value_reg_or_pair, int array_reg, int index_reg) {
        return aop(op_put_shorty(shorty), value_reg_or_pair, array_reg, index_reg);
    }

    public CodeBuilder iget(int value_reg_or_pair, int object_reg, FieldId instance_field) {
        return iop(op_get_shorty(instance_field.getType().getShorty()),
                value_reg_or_pair, object_reg, instance_field);
    }

    public CodeBuilder iput(int value_reg_or_pair, int object_reg, FieldId instance_field) {
        return iop(op_put_shorty(instance_field.getType().getShorty()),
                value_reg_or_pair, object_reg, instance_field);
    }

    public CodeBuilder sget(int value_reg_or_pair, FieldId static_field) {
        return sop(op_get_shorty(static_field.getType().getShorty()),
                value_reg_or_pair, static_field);
    }

    public CodeBuilder sput(int value_reg_or_pair, FieldId static_field) {
        return sop(op_put_shorty(static_field.getType().getShorty()),
                value_reg_or_pair, static_field);
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
        return f35c(kind.regular, method, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
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
        return f3rc(kind.range, method, arg_count, first_arg_reg);
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

    public CodeBuilder unop(UnOp op, int dst_reg_or_pair, int src_reg_or_pair) {
        return f12x(op.opcode, dst_reg_or_pair, op.isDstWide, src_reg_or_pair, op.isSrcWide);
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

    public CodeBuilder binop(BinOp op, int dst_reg_or_pair,
                             int first_src_reg_or_pair, int second_src_reg_or_pair) {
        if (op.regular == null) {
            throw new IllegalArgumentException("There is no regular version of " + op);
        }
        return f23x(op.regular, dst_reg_or_pair, op.isDstAndSrc1Wide,
                first_src_reg_or_pair, op.isDstAndSrc1Wide,
                second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder binop_2addr(BinOp op, int dst_and_first_src_reg_or_pair,
                                   int second_src_reg_or_pair) {
        if (op._2addr == null) {
            throw new IllegalArgumentException("There is no 2addr version of " + op);
        }
        return f12x(op._2addr, dst_and_first_src_reg_or_pair,
                op.isDstAndSrc1Wide, second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder raw_binop_lit16(BinOp op, int dst_reg, int src_reg, int value) {
        if (op.lit16 == null) {
            throw new IllegalArgumentException("There is no lit16 version of " + op);
        }
        return f22s(op.lit16, dst_reg, false, src_reg, false, value);
    }

    public CodeBuilder raw_binop_lit8(BinOp op, int dst_reg, int src_reg, int value) {
        if (op.lit8 == null) {
            throw new IllegalArgumentException("There is no lit8 version of " + op);
        }
        return f22b(op.lit8, dst_reg, false, src_reg, false, value);
    }

    public CodeBuilder binop_lit(BinOp op, int dst_reg, int src_reg, int value) {
        if (check_width_int(value, 8)) {
            return raw_binop_lit8(op, dst_reg, src_reg, value);
        }
        return raw_binop_lit16(op, dst_reg, src_reg, value);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_count, int arg_reg1,
                                          int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return f45cc(Opcode.INVOKE_POLYMORPHIC, method, proto, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
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
        return f4rcc(Opcode.INVOKE_POLYMORPHIC_RANGE, method, proto, arg_count, first_arg_reg);
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_count, int arg_reg1,
                                     int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return f35c(Opcode.INVOKE_CUSTOM, callsite, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
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
        return f3rc(Opcode.INVOKE_POLYMORPHIC_RANGE, callsite, arg_count, first_arg_reg);
    }

    public CodeBuilder const_method_handle(int dst_reg, MethodHandleId value) {
        return f21c(Opcode.CONST_METHOD_HANDLE, dst_reg, false, value);
    }

    public CodeBuilder const_method_type(int dst_reg, ProtoId value) {
        return f21c(Opcode.CONST_METHOD_TYPE, dst_reg, false, value);
    }
}
