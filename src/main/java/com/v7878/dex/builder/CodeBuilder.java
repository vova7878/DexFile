package com.v7878.dex.builder;

import static com.v7878.dex.Format.Format21t;
import static com.v7878.dex.Format.Format22t;
import static com.v7878.dex.Opcode.*;
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
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

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
import com.v7878.dex.immutable.bytecode.InstructionRaw;
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
import com.v7878.dex.util.Converter;
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
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

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
                int l1 = findUnit(label1);
                int l2 = findUnit(label2);
                int hl = findUnit(handlerLabel);
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

    private class BuilderDebugItem implements Comparable<BuilderDebugItem> {
        private final Object label;
        private final DebugItem item;
        private final int index;
        private int position = -1;

        BuilderDebugItem(int index, Object label, DebugItem item) {
            this.index = index;
            this.label = label;
            this.item = item;
        }

        private void initLabel() {
            if (position < 0) {
                position = findUnit(label);
            }
        }

        public int position() {
            initLabel();
            return position;
        }

        public DebugItem item() {
            return item;
        }

        @Override
        public int compareTo(BuilderDebugItem other) {
            if (other == this) return 0;
            int out = Integer.compare(position(), other.position());
            if (out != 0) return out;
            return Integer.compare(index, other.index);
        }
    }

    private interface BuilderNode {
        default void update(int position) {
        }

        default int label_offset() {
            return 0;
        }

        int units();

        List<Instruction> generate();
    }

    private static class BuilderPosition {
        public int position, units, label_offset;
        public BuilderPosition next;
        public BuilderNode node;

        public BuilderPosition(int position) {
            this.position = position;
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
    private final boolean has_this;

    private final List<Runnable> delayed_actions;
    private final List<BuilderTryItem> try_items;
    private final List<BuilderDebugItem> debug_items;
    private final Map<Object, BuilderPosition> labels;
    private final BuilderPosition first;
    private BuilderPosition last;

    private boolean generate_lines;

    private CodeBuilder(int regs_size, int ins_size, boolean add_hidden_this) {
        this.has_this = add_hidden_this;
        int this_reg = add_hidden_this ? 1 : 0;
        this.regs_size = checkRange(regs_size, 0, (1 << 16) - this_reg) + this_reg;
        this.ins_size = checkRange(ins_size, 0, regs_size + 1) + this_reg;
        this.delayed_actions = new ArrayList<>();
        this.try_items = new ArrayList<>();
        this.debug_items = new ArrayList<>();
        this.labels = new HashMap<>();
        this.first = this.last = new BuilderPosition(0);
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

    private static List<TryBlock> mergeTryItems(List<BuilderTryItem> try_items) {
        class TryContainer {
            final Set<TypeId> exceptions = new HashSet<>();
            final List<ExceptionHandler> handlers = new ArrayList<>();
            Integer catch_all_address = null;
        }

        int[] borders;
        {
            // TODO: IntSet
            var borders_set = new SparseArray<>();
            for (int i = 0; i < try_items.size(); ) {
                BuilderTryItem block = try_items.get(i);
                int start = block.start(), end = block.end();
                if (end <= start) {
                    assert start == end;
                    try_items.remove(i);
                    continue;
                }
                borders_set.append(start, null);
                borders_set.append(end, null);
                i++;
            }
            borders = borders_set.keysArray();
        }
        if (borders.length == 0) return Collections.emptyList();
        int elements_size = borders.length - 1;

        var elements = new SparseArray<TryContainer>(elements_size);
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
            if (container.catch_all_address != null || !container.handlers.isEmpty()) {
                out.add(TryBlock.of(borders[i], borders[i + 1] - borders[i],
                        container.catch_all_address, container.handlers));
            }
        }
        return out;
    }

    private static List<DebugItem> mergeDebugItems(List<BuilderDebugItem> debug_items) {
        Collections.sort(debug_items);

        var out = new ArrayList<DebugItem>(debug_items.size());
        int pc = 0;
        for (var item : debug_items) {
            int position = item.position();
            if (pc != position) {
                out.add(AdvancePC.of(position - pc));
                pc = position;
            }
            out.add(item.item());
        }
        return out;
    }

    private List<Instruction> mergeInstructions() {
        var begin = first;
        var end = last;

        boolean changed;
        do {
            changed = false;
            for (var current = begin; current != end; current = current.next) {
                var node = current.node;
                node.update(current.position);
                var units = node.units();
                if (current.units != units) {
                    changed = true;
                    int diff = units - current.units;
                    // The node size may decrease if it is a payload whose position has become even.
                    // Since all payloads are located after the main instructions,
                    // such change cannot lead to an infinite loop of corrections
                    assert diff > 0 || diff == -1;
                    current.units = units;
                    current.label_offset = node.label_offset();
                    for (var tmp = current.next; tmp != end; tmp = tmp.next) {
                        tmp.position += diff;
                    }
                }
            }
        } while (changed);

        var out = new ArrayList<Instruction>();
        for (var current = begin; current != end; current = current.next) {
            out.addAll(current.node.generate());
        }
        return out;
    }

    private MethodImplementation finish() {
        delayed_actions.forEach(Runnable::run);

        List<Instruction> insns = mergeInstructions();
        List<TryBlock> try_blocks = mergeTryItems(try_items);
        List<DebugItem> debug_info = mergeDebugItems(debug_items);

        return MethodImplementation.of(regs_size, insns, try_blocks, debug_info);
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

    private void add(BuilderNode node, int initial_units) {
        if (generate_lines) {
            // Note: Numbering starts from one
            line(currentUnit() + 1);
        }

        var current = last;
        current.units = initial_units;
        current.node = node;

        var next = new BuilderPosition(current.position + current.units);
        current.next = next;
        last = next;
    }

    private void add(Instruction instruction) {
        int units = instruction.getUnitCount();
        add(new BuilderNode() {
            @Override
            public int units() {
                return units;
            }

            @Override
            public List<Instruction> generate() {
                return List.of(instruction);
            }
        }, units);
    }

    private void add(Format format, IntFunction<Instruction> factory) {
        if (format.isPayload()) {
            throw new AssertionError();
        }

        int units = format.getUnitCount();
        add(new BuilderNode() {
            int position;

            @Override
            public void update(int position) {
                this.position = position;
            }

            @Override
            public int units() {
                return units;
            }

            @Override
            public List<Instruction> generate() {
                var instruction = factory.apply(position);
                assert instruction.getOpcode().format() == format;
                return List.of(instruction);
            }
        }, units);
    }

    private void addPayload(int unit_count, Supplier<Instruction> factory) {
        add(new BuilderNode() {
            boolean odd_position;

            @Override
            public void update(int position) {
                odd_position = (position & 0x1) == 1;
            }

            @Override
            public int label_offset() {
                return odd_position ? 1 : 0;
            }

            @Override
            public int units() {
                return unit_count + label_offset();
            }

            @Override
            public List<Instruction> generate() {
                var value = factory.get();
                assert value.getOpcode().isPayload();
                if (odd_position) {
                    return List.of(Instruction10x.of(NOP), value);
                }
                return List.of(value);
            }
        }, unit_count);
    }

    private void addDelayedAction(Runnable action) {
        delayed_actions.add(action);
    }

    private void putLabel(Object label) {
        if (labels.putIfAbsent(Objects.requireNonNull(label), last) != null) {
            throw new IllegalArgumentException("Label " + label + " already exists");
        }
    }

    private int currentUnit() {
        return last.position;
    }

    private int findUnit(Object label) {
        var pos = labels.get(label);
        if (pos == null) {
            throw new IllegalStateException("Can`t find label: " + label);
        }
        return pos.position + pos.label_offset;
    }

    private int branchOffset(int from, Object to, boolean allow_zero) {
        int offset = findUnit(to) - from;
        if (!allow_zero && offset == 0) {
            throw new IllegalStateException("Zero branch offset is not allowed");
        }
        return offset;
    }

    private int branchOffset(int from, Object to) {
        return branchOffset(from, to, false);
    }

    @SuppressWarnings("SameParameterValue")
    private int branchOffset(Object from, Object to, boolean allow_zero) {
        return branchOffset(findUnit(from), to, allow_zero);
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

    private CodeBuilder addDebugItem(Object label, DebugItem item) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(item);
        debug_items.add(new BuilderDebugItem(debug_items.size(), label, item));
        return this;
    }

    public CodeBuilder generate_lines() {
        generate_lines = true;
        return this;
    }

    public CodeBuilder line_internal(Object label, int line) {
        return addDebugItem(label, LineNumber.of(line));
    }

    public CodeBuilder prologue_internal(Object label) {
        return addDebugItem(label, SetPrologueEnd.INSTANCE);
    }

    public CodeBuilder epilogue_internal(Object label) {
        return addDebugItem(label, SetEpilogueBegin.INSTANCE);
    }

    public CodeBuilder source_internal(Object label, String name) {
        return addDebugItem(label, SetFile.of(name));
    }

    public CodeBuilder local_internal(Object label, int register, String name, TypeId type, String signature) {
        return addDebugItem(label, StartLocal.of(register, name, type, signature));
    }

    public CodeBuilder end_local_internal(Object label, int register) {
        return addDebugItem(label, EndLocal.of(register));
    }

    public CodeBuilder restart_local_internal(Object label, int register) {
        return addDebugItem(label, RestartLocal.of(register));
    }

    public CodeBuilder line(String label, int line) {
        return line_internal(label, line);
    }

    public CodeBuilder prologue(String label) {
        return prologue_internal(label);
    }

    public CodeBuilder epilogue(String label) {
        return epilogue_internal(label);
    }

    public CodeBuilder source(String label, String name) {
        return source_internal(label, name);
    }

    public CodeBuilder local(String label, int register, String name, TypeId type, String signature) {
        return local_internal(label, register, name, type, signature);
    }

    public CodeBuilder local(String label, int register, String name, TypeId type) {
        return local(label, register, name, type, null);
    }

    public CodeBuilder end_local(String label, int register) {
        return end_local_internal(label, register);
    }

    public CodeBuilder restart_local(String label, int register) {
        return restart_local_internal(label, register);
    }

    public CodeBuilder line(int line) {
        InternalLabel label = new InternalLabel();
        line_internal(label, line);
        putLabel(label);
        return this;
    }

    public CodeBuilder prologue() {
        InternalLabel label = new InternalLabel();
        prologue_internal(label);
        putLabel(label);
        return this;
    }

    public CodeBuilder epilogue() {
        InternalLabel label = new InternalLabel();
        epilogue_internal(label);
        putLabel(label);
        return this;
    }

    public CodeBuilder source(String name) {
        InternalLabel label = new InternalLabel();
        source_internal(label, name);
        putLabel(label);
        return this;
    }

    public CodeBuilder local(int register, String name, TypeId type, String signature) {
        InternalLabel label = new InternalLabel();
        local_internal(label, register, name, type, signature);
        putLabel(label);
        return this;
    }

    public CodeBuilder local(int register, String name, TypeId type) {
        return local(register, name, type, null);
    }

    public CodeBuilder end_local(int register) {
        InternalLabel label = new InternalLabel();
        end_local_internal(label, register);
        putLabel(label);
        return this;
    }

    public CodeBuilder restart_local(int register) {
        InternalLabel label = new InternalLabel();
        restart_local_internal(label, register);
        putLabel(label);
        return this;
    }

    private void format_35c_checks(int arg_count, int arg_reg1, int arg_reg2,
                                   int arg_reg3, int arg_reg4, int arg_reg5) {
        checkRange(arg_count, 0, 6);
        if (arg_count >= 5) check_reg(arg_reg5);
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

    public CodeBuilder raw(Instruction instruction) {
        add(instruction);
        return this;
    }

    public CodeBuilder raw(short instruction) {
        return raw(InstructionRaw.of(instruction));
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
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f11n(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
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
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f10t(Opcode op, IntUnaryOperator value) {
        add(op.format(), position -> Instruction10t.of(op, value.applyAsInt(position)));
        return this;
    }

    // <ØØ|op AAAA> op +AAAA
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f20t(Opcode op, IntUnaryOperator value) {
        add(op.format(), position -> Instruction20t.of(op, value.applyAsInt(position)));
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
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f21t(Opcode op, int reg_or_pair, boolean is_reg_wide, IntUnaryOperator value) {
        check_reg_or_pair(reg_or_pair, is_reg_wide);
        add(op.format(), position -> Instruction21t.of(op, reg_or_pair, value.applyAsInt(position)));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB
    private CodeBuilder f21s(Opcode op, int reg_or_pair, boolean is_reg_wide, int value) {
        add(Instruction21s.of(op,
                check_reg_or_pair(reg_or_pair, is_reg_wide), value));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB0000
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f21ih(Opcode op, int reg, int value) {
        add(Instruction21ih.of(op, check_reg(reg), value));
        return this;
    }

    // <AA|op BBBB> op vAA, #+BBBB000000000000
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f21lh(Opcode op, int reg_pair, long value) {
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
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22b(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, int value) {
        add(Instruction22b.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide), value));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, +CCCC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22t(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, IntUnaryOperator value) {
        check_reg_or_pair(reg_or_pair1, is_reg1_wide);
        check_reg_or_pair(reg_or_pair2, is_reg2_wide);
        add(op.format(), position -> Instruction22t.of(
                op, reg_or_pair1, reg_or_pair2, value.applyAsInt(position)));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, #+CCCC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22s(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, int value) {
        add(Instruction22s.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide), value));
        return this;
    }

    // <B|A|op CCCC> op vA, vB, @CCCC
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f22c(Opcode op, int reg_or_pair1, boolean is_reg1_wide,
                             int reg_or_pair2, boolean is_reg2_wide, Object constant) {
        add(Instruction22c22cs.of(op,
                check_reg_or_pair(reg_or_pair1, is_reg1_wide),
                check_reg_or_pair(reg_or_pair2, is_reg2_wide), constant));
        return this;
    }

    // <ØØ|op AAAAlo AAAAhi> op +AAAAAAAA
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f30t(Opcode op, IntUnaryOperator value) {
        add(op.format(), position -> Instruction30t.of(op, value.applyAsInt(position)));
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
    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private CodeBuilder f31t(Opcode op, int reg_or_pair, boolean is_reg_wide, IntUnaryOperator value) {
        check_reg_or_pair(reg_or_pair, is_reg_wide);
        add(op.format(), position -> Instruction31t.of(
                op, reg_or_pair, value.applyAsInt(position)));
        return this;
    }

    // <AA|op BBBBlo BBBBhi> op vAA, @BBBBBBBB
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f31c(Opcode op, int reg_or_pair, boolean is_reg_wide, Object constant) {
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
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f45cc(Opcode op, Object constant1, Object constant2, int arg_count,
                              int arg_reg1, int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        format_35c_checks(arg_count, arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5);
        add(Instruction45cc.of(op, arg_count,
                arg_reg1, arg_reg2, arg_reg3, arg_reg4, arg_reg5, constant1, constant2));
        return this;
    }

    // <AA|op BBBB CCCC HHHH> op {vCCCC .. vNNNN}, @BBBB, @HHHH (where NNNN = CCCC+AA-1)
    @SuppressWarnings("SameParameterValue")
    private CodeBuilder f4rcc(Opcode op, Object constant1,
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

    private void packed_switch_payload(int first_key, Object target, Object[] labels) {
        int units = Preconditions.getPackedSwitchPayloadUnitCount(labels.length);
        addPayload(units, () -> {
            NavigableSet<SwitchElement> elements = new TreeSet<>();
            for (int i = 0; i < labels.length; i++) {
                elements.add(SwitchElement.of(first_key + i,
                        branchOffset(target, labels[i], true)));
            }
            elements = Collections.unmodifiableNavigableSet(elements);
            return PackedSwitchPayload.raw(elements);
        });
    }

    private void sparse_switch_payload(int[] keys, Object target, Object[] labels) {
        int units = Preconditions.getSparseSwitchPayloadUnitCount(labels.length);
        addPayload(units, () -> {
            NavigableSet<SwitchElement> elements = new TreeSet<>();
            for (int i = 0; i < labels.length; i++) {
                elements.add(SwitchElement.of(keys[i],
                        branchOffset(target, labels[i], true)));
            }
            elements = Collections.unmodifiableNavigableSet(elements);
            return SparseSwitchPayload.raw(elements);
        });
    }

    private void fill_array_data_payload(int element_width, List<? extends Number> data) {
        int units = Preconditions.getArrayPayloadUnitCount(element_width, data.size());
        addPayload(units, () -> ArrayPayload.raw(element_width, data));
    }

    @SuppressWarnings("UnusedReturnValue")
    public CodeBuilder nop() {
        return f10x(NOP);
    }

    public CodeBuilder raw_move(int dst_reg, int src_reg) {
        return f12x(MOVE, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_from16(int dst_reg, int src_reg) {
        return f22x(MOVE_FROM16, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_16(int dst_reg, int src_reg) {
        return f32x(MOVE_16, dst_reg, false, src_reg, false);
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
        return f12x(MOVE_WIDE, dst_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder raw_move_wide_from16(int dst_reg_pair, int src_reg_pair) {
        return f22x(MOVE_WIDE_FROM16, dst_reg_pair, true, src_reg_pair, true);
    }

    public CodeBuilder raw_move_wide_16(int dst_reg_pair, int src_reg_pair) {
        return f32x(MOVE_WIDE_16, dst_reg_pair, true, src_reg_pair, true);
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
        return f12x(MOVE_OBJECT, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_object_from16(int dst_reg, int src_reg) {
        return f22x(MOVE_OBJECT_FROM16, dst_reg, false, src_reg, false);
    }

    public CodeBuilder raw_move_object_16(int dst_reg, int src_reg) {
        return f32x(MOVE_OBJECT_16, dst_reg, false, src_reg, false);
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
            default -> throw invalidShorty(shorty);
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
        return f11x(MOVE_RESULT, dst_reg, false);
    }

    public CodeBuilder move_result_wide(int dst_reg_peir) {
        return f11x(MOVE_RESULT_WIDE, dst_reg_peir, true);
    }

    public CodeBuilder move_result_object(int dst_reg) {
        return f11x(MOVE_RESULT_OBJECT, dst_reg, false);
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
            default -> throw invalidShorty(shorty);
        };
    }

    public CodeBuilder move_exception(int dst_reg) {
        return f11x(MOVE_EXCEPTION, dst_reg, false);
    }

    public CodeBuilder return_void() {
        return f10x(RETURN_VOID);
    }

    public CodeBuilder return_(int return_value_reg) {
        return f11x(RETURN, return_value_reg, false);
    }

    public CodeBuilder return_wide(int return_value_reg_peir) {
        return f11x(RETURN_WIDE, return_value_reg_peir, true);
    }

    public CodeBuilder return_object(int return_value_reg) {
        return f11x(RETURN_OBJECT, return_value_reg, false);
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
            default -> throw invalidShorty(shorty);
        };
    }

    public CodeBuilder raw_const_4(int dst_reg, int value) {
        return f11n(CONST_4, dst_reg, false, value);
    }

    public CodeBuilder raw_const_16(int dst_reg, int value) {
        return f21s(CONST_16, dst_reg, false, value);
    }

    public CodeBuilder raw_const(int dst_reg, int value) {
        return f31i(CONST, dst_reg, false, value);
    }

    public CodeBuilder raw_const_high16(int dst_reg, int value) {
        return f21ih(CONST_HIGH16, dst_reg, value);
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
        return f21s(CONST_WIDE_16, dst_reg_pair, true, value);
    }

    public CodeBuilder raw_const_wide_32(int dst_reg_pair, int value) {
        return f31i(CONST_WIDE_32, dst_reg_pair, true, value);
    }

    public CodeBuilder raw_const_wide(int dst_reg_pair, long value) {
        return f51l(CONST_WIDE, dst_reg_pair, value);
    }

    public CodeBuilder raw_const_wide_high16(int dst_reg_pair, long value) {
        return f21lh(CONST_WIDE_HIGH16, dst_reg_pair, value);
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

    public CodeBuilder raw_const_string(int dst_reg, String value) {
        return f21c(CONST_STRING, dst_reg, false, value);
    }

    public CodeBuilder raw_const_string_jumbo(int dst_reg, String value) {
        return f31c(CONST_STRING_JUMBO, dst_reg, false, value);
    }

    public CodeBuilder const_string(int dst_reg, String value) {
        //TODO: Generate smaller instructions if possible
        return raw_const_string_jumbo(dst_reg, value);
    }

    public CodeBuilder const_class(int dst_reg, TypeId value) {
        return f21c(CONST_CLASS, dst_reg, false, value);
    }

    public CodeBuilder monitor_enter(int ref_reg) {
        return f11x(MONITOR_ENTER, ref_reg, false);
    }

    public CodeBuilder monitor_exit(int ref_reg) {
        return f11x(MONITOR_EXIT, ref_reg, false);
    }

    public CodeBuilder check_cast(int ref_reg, TypeId value) {
        return f21c(CHECK_CAST, ref_reg, false, value);
    }

    public CodeBuilder instance_of(int dst_reg, int ref_reg, TypeId value) {
        return f22c(INSTANCE_OF, dst_reg, false, ref_reg, false, value);
    }

    public CodeBuilder new_instance(int dst_reg, TypeId value) {
        return f21c(NEW_INSTANCE, dst_reg, false, value);
    }

    public CodeBuilder new_array(int dst_reg, int size_reg, TypeId value) {
        return f22c(NEW_ARRAY, dst_reg, false, size_reg, false, value);
    }

    public CodeBuilder filled_new_array(TypeId type, int arr_size, int arg_reg1,
                                        int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return f35c(FILLED_NEW_ARRAY, type, arr_size,
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
        return f3rc(FILLED_NEW_ARRAY_RANGE, type, arr_size, first_arg_reg);
    }

    private CodeBuilder fill_array_data_internal(int arr_ref_reg, int element_width, List<? extends Number> data) {
        InternalLabel payload = new InternalLabel();

        f31t(FILL_ARRAY_DATA, arr_ref_reg, false,
                self -> branchOffset(self, payload));

        addDelayedAction(() -> {
            putLabel(payload);
            fill_array_data_payload(element_width, data);
        });

        return this;
    }

    public CodeBuilder fill_array_data(int arr_ref_reg, int element_width, Iterable<? extends Number> data) {
        var checked_data = Preconditions.checkArrayPayloadElements(element_width, Converter.toList(data));
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
        return f11x(THROW, ex_reg, false);
    }

    private CodeBuilder goto_internal(Object label) {
        add(new BuilderNode() {
            int position;
            int target;

            @Override
            public void update(int position) {
                this.position = position;
                this.target = findUnit(label);
            }

            @Override
            public int units() {
                int diff = target - position;
                if (diff == 0 || !check_width_int(diff, 16)) {
                    return GOTO_32.getUnitCount();
                }
                if (!check_width_int(diff, 8)) {
                    return GOTO_16.getUnitCount();
                }
                return GOTO.getUnitCount();
            }

            @Override
            public List<Instruction> generate() {
                int diff = target - position;
                if (diff == 0 || !check_width_int(diff, 16)) {
                    return List.of(Instruction30t.of(GOTO_32, diff));
                }
                if (!check_width_int(diff, 8)) {
                    return List.of(Instruction20t.of(GOTO_16, diff));
                }
                return List.of(Instruction10t.of(GOTO, diff));
            }
        }, GOTO.getUnitCount());
        return this;
    }

    public CodeBuilder goto_(String label) {
        return goto_internal(label);
    }

    private CodeBuilder raw_goto_internal(Object label) {
        return f10t(GOTO, self -> branchOffset(self, label));
    }

    public CodeBuilder raw_goto(String label) {
        return raw_goto_internal(label);
    }

    private CodeBuilder raw_goto_16_internal(Object label) {
        return f20t(GOTO_16, self -> branchOffset(self, label));
    }

    public CodeBuilder raw_goto_16(String label) {
        return raw_goto_16_internal(label);
    }

    private CodeBuilder raw_goto_32_internal(Object label) {
        return f30t(GOTO_32, self -> branchOffset(self, label, true));
    }

    public CodeBuilder raw_goto_32(String label) {
        return raw_goto_32_internal(label);
    }

    private CodeBuilder packed_switch_internal(int reg_to_test, int first_key, Object... labels) {
        assert first_key + labels.length >= first_key;
        InternalLabel current = new InternalLabel();
        InternalLabel payload = new InternalLabel();

        putLabel(current);
        f31t(PACKED_SWITCH, reg_to_test, false,
                self -> branchOffset(self, payload));

        addDelayedAction(() -> {
            putLabel(payload);
            packed_switch_payload(first_key, current, labels);
        });

        return this;
    }

    private CodeBuilder sparse_switch_internal(int reg_to_test, int[] keys, Object... labels) {
        assert keys.length == labels.length;
        InternalLabel current = new InternalLabel();
        InternalLabel payload = new InternalLabel();

        putLabel(current);
        f31t(SPARSE_SWITCH, reg_to_test, false,
                self -> branchOffset(self, payload));

        addDelayedAction(() -> {
            putLabel(payload);
            sparse_switch_payload(keys, current, labels);
        });

        return this;
    }

    public CodeBuilder switch_(int reg_to_test, Map<Integer, String> table) {
        check_reg(reg_to_test);
        if (table.isEmpty()) {
            return this;
        }
        var map = new SparseArray<String>(table.size());
        table.forEach((key, value) -> map.put(key, Objects.requireNonNull(value)));
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
        EQ(IF_EQ, IF_EQZ), // ==
        NE(IF_NE, IF_NEZ), // !=
        LT(IF_LT, IF_LTZ), // <
        GE(IF_GE, IF_GEZ), // >=
        GT(IF_GT, IF_GTZ), // >
        LE(IF_LE, IF_LEZ); // <=

        private final Opcode test, testz;

        Test(Opcode test, Opcode testz) {
            this.test = test;
            this.testz = testz;
        }

        public Test inverse() {
            return switch (this) {
                case EQ -> NE; // == -> !=
                case NE -> EQ; // != -> ==
                case LT -> GE; // < -> >=
                case GE -> LT; // >= -> <
                case GT -> LE; // > -> <=
                case LE -> GT; // <= -> >
            };
        }
    }

    public CodeBuilder if_test(Test test, int first_reg_to_test, int second_reg_to_test,
                               Consumer<CodeBuilder> true_branch, Consumer<CodeBuilder> false_branch) {
        var true_label = new InternalLabel();
        var end_label = new InternalLabel();
        if_test_internal(test, first_reg_to_test, second_reg_to_test, true_label);
        false_branch.accept(this);
        goto_internal(end_label);
        putLabel(true_label);
        true_branch.accept(this);
        putLabel(end_label);
        return this;
    }

    public CodeBuilder if_test(Test test, int first_reg_to_test,
                               int second_reg_to_test, Consumer<CodeBuilder> true_branch) {
        var end_label = new InternalLabel();
        if_test_internal(test.inverse(), first_reg_to_test, second_reg_to_test, end_label);
        true_branch.accept(this);
        putLabel(end_label);
        return this;
    }

    private CodeBuilder if_test_internal(Test test, int first_reg_to_test, int second_reg_to_test, Object label) {
        check_reg_or_pair(first_reg_to_test, false);
        check_reg_or_pair(second_reg_to_test, false);
        add(new BuilderNode() {
            int position;
            int target;

            @Override
            public void update(int position) {
                this.position = position;
                this.target = findUnit(label);
            }

            @Override
            public int units() {
                int diff = target - position;
                int units = Format22t.getUnitCount();
                if (diff == 0) {
                    return units + GOTO.getUnitCount();
                }
                if (!check_width_int(diff, 16)) {
                    return units + GOTO_32.getUnitCount();
                }
                return units;
            }

            @Override
            public List<Instruction> generate() {
                int diff = target - position;
                if (diff == 0) {
                    return List.of(
                            Instruction22t.of(test.inverse().test, first_reg_to_test,
                                    second_reg_to_test, Format22t.getUnitCount() + GOTO.getUnitCount()),
                            Instruction10t.of(GOTO, -Format22t.getUnitCount())
                    );
                }
                if (!check_width_int(diff, 16)) {
                    return List.of(
                            Instruction22t.of(test.inverse().test, first_reg_to_test,
                                    second_reg_to_test, Format22t.getUnitCount() + GOTO_32.getUnitCount()),
                            Instruction30t.of(GOTO_32, diff - Format22t.getUnitCount())
                    );
                }
                return List.of(Instruction22t.of(test.test,
                        first_reg_to_test, second_reg_to_test, diff));
            }
        }, Format22t.getUnitCount());
        return this;
    }

    public CodeBuilder if_test(Test test, int first_reg_to_test, int second_reg_to_test, String label) {
        return if_test_internal(test, first_reg_to_test, second_reg_to_test, label);
    }

    private CodeBuilder raw_if_test_internal(Test test, int first_reg_to_test, int second_reg_to_test, Object label) {
        return f22t(test.test, first_reg_to_test, false,
                second_reg_to_test, false,
                self -> branchOffset(self, label));
    }

    public CodeBuilder raw_if_test(Test test, int first_reg_to_test, int second_reg_to_test, String label) {
        return raw_if_test_internal(test, first_reg_to_test, second_reg_to_test, label);
    }

    public CodeBuilder if_testz(Test test, int reg_to_test,
                                Consumer<CodeBuilder> true_branch,
                                Consumer<CodeBuilder> false_branch) {
        var true_label = new InternalLabel();
        var end_label = new InternalLabel();
        if_testz_internal(test, reg_to_test, true_label);
        false_branch.accept(this);
        goto_internal(end_label);
        putLabel(true_label);
        true_branch.accept(this);
        putLabel(end_label);
        return this;
    }

    public CodeBuilder if_testz(Test test, int reg_to_test, Consumer<CodeBuilder> true_branch) {
        var end_label = new InternalLabel();
        if_testz_internal(test.inverse(), reg_to_test, end_label);
        true_branch.accept(this);
        putLabel(end_label);
        return this;
    }

    private CodeBuilder if_testz_internal(Test test, int reg_to_test, Object label) {
        check_reg_or_pair(reg_to_test, false);
        add(new BuilderNode() {
            int position;
            int target;

            @Override
            public void update(int position) {
                this.position = position;
                this.target = findUnit(label);
            }

            @Override
            public int units() {
                int diff = target - position;
                int units = Format21t.getUnitCount();
                if (diff == 0) {
                    return units + GOTO.getUnitCount();
                }
                if (!check_width_int(diff, 16)) {
                    return units + GOTO_32.getUnitCount();
                }
                return units;
            }

            @Override
            public List<Instruction> generate() {
                int diff = target - position;
                if (diff == 0) {
                    return List.of(
                            Instruction21t.of(test.inverse().testz,
                                    reg_to_test, Format21t.getUnitCount() + GOTO.getUnitCount()),
                            Instruction10t.of(GOTO, -Format21t.getUnitCount())
                    );
                }
                if (!check_width_int(diff, 16)) {
                    return List.of(
                            Instruction21t.of(test.inverse().testz,
                                    reg_to_test, Format21t.getUnitCount() + GOTO_32.getUnitCount()),
                            Instruction30t.of(GOTO_32, diff - Format21t.getUnitCount())
                    );
                }
                return List.of(Instruction21t.of(test.testz, reg_to_test, diff));
            }
        }, Format21t.getUnitCount());
        return this;
    }

    public CodeBuilder if_testz(Test test, int reg_to_test, String label) {
        return if_testz_internal(test, reg_to_test, label);
    }

    private CodeBuilder raw_if_testz_internal(Test test, int reg_to_test, Object label) {
        return f21t(test.testz, reg_to_test, false,
                self -> branchOffset(self, label));
    }

    public CodeBuilder raw_if_testz(Test test, int reg_to_test, String label) {
        return raw_if_testz_internal(test, reg_to_test, label);
    }

    public enum Op {
        GET(AGET, IGET, SGET, false),
        GET_WIDE(AGET_WIDE, IGET_WIDE, SGET_WIDE, true),
        GET_OBJECT(AGET_OBJECT, IGET_OBJECT, SGET_OBJECT, false),
        GET_BOOLEAN(AGET_BOOLEAN, IGET_BOOLEAN, SGET_BOOLEAN, false),
        GET_BYTE(AGET_BYTE, IGET_BYTE, SGET_BYTE, false),
        GET_CHAR(AGET_CHAR, IGET_CHAR, SGET_CHAR, false),
        GET_SHORT(AGET_SHORT, IGET_SHORT, SGET_SHORT, false),
        PUT(APUT, IPUT, SPUT, false),
        PUT_WIDE(APUT_WIDE, IPUT_WIDE, SPUT_WIDE, true),
        PUT_OBJECT(APUT_OBJECT, IPUT_OBJECT, SPUT_OBJECT, false),
        PUT_BOOLEAN(APUT_BOOLEAN, IPUT_BOOLEAN, SPUT_BOOLEAN, false),
        PUT_BYTE(APUT_BYTE, IPUT_BYTE, SPUT_BYTE, false),
        PUT_CHAR(APUT_CHAR, IPUT_CHAR, SPUT_CHAR, false),
        PUT_SHORT(APUT_SHORT, IPUT_SHORT, SPUT_SHORT, false);

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
            default -> throw invalidShorty(shorty);
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
            default -> throw invalidShorty(shorty);
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
        VIRTUAL(INVOKE_VIRTUAL, INVOKE_VIRTUAL_RANGE),
        SUPER(INVOKE_SUPER, INVOKE_SUPER_RANGE),
        DIRECT(INVOKE_DIRECT, INVOKE_DIRECT_RANGE),
        STATIC(INVOKE_STATIC, INVOKE_STATIC_RANGE),
        INTERFACE(INVOKE_INTERFACE, INVOKE_INTERFACE_RANGE);

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
        DOUBLE_TO_FLOAT(Opcode.DOUBLE_TO_FLOAT, true, false),

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

    public CodeBuilder neg(char shorty, int dst_reg_or_pair, int src_reg_or_pair) {
        return switch (shorty) {
            case 'B' -> unop(UnOp.NEG_INT, dst_reg_or_pair, src_reg_or_pair).
                    unop(UnOp.INT_TO_BYTE, dst_reg_or_pair, dst_reg_or_pair);
            case 'S' -> unop(UnOp.NEG_INT, dst_reg_or_pair, src_reg_or_pair).
                    unop(UnOp.INT_TO_SHORT, dst_reg_or_pair, dst_reg_or_pair);
            case 'C' -> unop(UnOp.NEG_INT, dst_reg_or_pair, src_reg_or_pair).
                    unop(UnOp.INT_TO_CHAR, dst_reg_or_pair, dst_reg_or_pair);
            case 'I' -> unop(UnOp.NEG_INT, dst_reg_or_pair, src_reg_or_pair);
            case 'F' -> unop(UnOp.NEG_FLOAT, dst_reg_or_pair, src_reg_or_pair);
            case 'J' -> unop(UnOp.NEG_LONG, dst_reg_or_pair, src_reg_or_pair);
            case 'D' -> unop(UnOp.NEG_DOUBLE, dst_reg_or_pair, src_reg_or_pair);
            default -> throw invalidShorty(shorty);
        };
    }

    public CodeBuilder not(char shorty, int dst_reg_or_pair, int src_reg_or_pair) {
        return switch (shorty) {
            case 'Z' -> binop_lit(BinOp.XOR_INT, dst_reg_or_pair, src_reg_or_pair, 0x1);
            case 'B' -> unop(UnOp.NOT_INT, dst_reg_or_pair, src_reg_or_pair).
                    unop(UnOp.INT_TO_BYTE, dst_reg_or_pair, dst_reg_or_pair);
            case 'S' -> unop(UnOp.NOT_INT, dst_reg_or_pair, src_reg_or_pair).
                    unop(UnOp.INT_TO_SHORT, dst_reg_or_pair, dst_reg_or_pair);
            case 'C' -> unop(UnOp.NOT_INT, dst_reg_or_pair, src_reg_or_pair).
                    unop(UnOp.INT_TO_CHAR, dst_reg_or_pair, dst_reg_or_pair);
            case 'I' -> unop(UnOp.NOT_INT, dst_reg_or_pair, src_reg_or_pair);
            case 'J' -> unop(UnOp.NOT_LONG, dst_reg_or_pair, src_reg_or_pair);
            default -> throw invalidShorty(shorty);
        };
    }

    public CodeBuilder cast_numeric(char dst_shorty, int dst_reg_or_pair,
                                    char src_shorty, int src_reg_or_pair) {
        return switch (dst_shorty) {
            case 'Z' -> switch (src_shorty) {
                case 'Z' -> this;
                case 'B', 'S', 'C', 'I', 'F', 'J', 'D' ->
                        cast_numeric('I', dst_reg_or_pair, src_shorty, src_reg_or_pair)
                                .binop_lit(BinOp.AND_INT, dst_reg_or_pair, dst_reg_or_pair, 0x1);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'B' -> switch (src_shorty) {
                case 'Z', 'B' -> this;
                case 'S', 'C', 'I' -> unop(UnOp.INT_TO_BYTE, dst_reg_or_pair, src_reg_or_pair);
                case 'F' -> unop(UnOp.FLOAT_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_BYTE, dst_reg_or_pair, dst_reg_or_pair);
                case 'J' -> unop(UnOp.LONG_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_BYTE, dst_reg_or_pair, dst_reg_or_pair);
                case 'D' -> unop(UnOp.DOUBLE_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_BYTE, dst_reg_or_pair, dst_reg_or_pair);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'S' -> switch (src_shorty) {
                case 'Z', 'B', 'S' -> this;
                case 'C', 'I' -> unop(UnOp.INT_TO_SHORT, dst_reg_or_pair, src_reg_or_pair);
                case 'F' -> unop(UnOp.FLOAT_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_SHORT, dst_reg_or_pair, dst_reg_or_pair);
                case 'J' -> unop(UnOp.LONG_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_SHORT, dst_reg_or_pair, dst_reg_or_pair);
                case 'D' -> unop(UnOp.DOUBLE_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_SHORT, dst_reg_or_pair, dst_reg_or_pair);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'C' -> switch (src_shorty) {
                case 'Z', 'C' -> this;
                case 'B', 'S', 'I' -> unop(UnOp.INT_TO_CHAR, dst_reg_or_pair, src_reg_or_pair);
                case 'F' -> unop(UnOp.FLOAT_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_CHAR, dst_reg_or_pair, dst_reg_or_pair);
                case 'J' -> unop(UnOp.LONG_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_CHAR, dst_reg_or_pair, dst_reg_or_pair);
                case 'D' -> unop(UnOp.DOUBLE_TO_INT, dst_reg_or_pair, src_reg_or_pair)
                        .unop(UnOp.INT_TO_CHAR, dst_reg_or_pair, dst_reg_or_pair);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'I' -> switch (src_shorty) {
                case 'Z', 'B', 'S', 'C', 'I' -> this;
                case 'F' -> unop(UnOp.FLOAT_TO_INT, dst_reg_or_pair, src_reg_or_pair);
                case 'J' -> unop(UnOp.LONG_TO_INT, dst_reg_or_pair, src_reg_or_pair);
                case 'D' -> unop(UnOp.DOUBLE_TO_INT, dst_reg_or_pair, src_reg_or_pair);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'F' -> switch (src_shorty) {
                case 'Z', 'B', 'S', 'C', 'I' ->
                        unop(UnOp.INT_TO_FLOAT, dst_reg_or_pair, src_reg_or_pair);
                case 'F' -> this;
                case 'J' -> unop(UnOp.LONG_TO_FLOAT, dst_reg_or_pair, src_reg_or_pair);
                case 'D' -> unop(UnOp.DOUBLE_TO_FLOAT, dst_reg_or_pair, src_reg_or_pair);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'J' -> switch (src_shorty) {
                case 'Z', 'B', 'S', 'C', 'I' ->
                        unop(UnOp.INT_TO_LONG, dst_reg_or_pair, src_reg_or_pair);
                case 'F' -> unop(UnOp.FLOAT_TO_LONG, dst_reg_or_pair, src_reg_or_pair);
                case 'J' -> this;
                case 'D' -> unop(UnOp.DOUBLE_TO_LONG, dst_reg_or_pair, src_reg_or_pair);
                default -> throw invalidShorty(dst_shorty);
            };
            case 'D' -> switch (src_shorty) {
                case 'Z', 'B', 'S', 'C', 'I' ->
                        unop(UnOp.INT_TO_DOUBLE, dst_reg_or_pair, src_reg_or_pair);
                case 'F' -> unop(UnOp.FLOAT_TO_DOUBLE, dst_reg_or_pair, src_reg_or_pair);
                case 'J' -> unop(UnOp.LONG_TO_DOUBLE, dst_reg_or_pair, src_reg_or_pair);
                case 'D' -> this;
                default -> throw invalidShorty(dst_shorty);
            };
            default -> throw invalidShorty(dst_shorty);
        };
    }

    public enum BinOp {
        ADD_INT(Opcode.ADD_INT, ADD_INT_2ADDR, ADD_INT_LIT16, ADD_INT_LIT8, false, false),
        RSUB_INT(null, null, Opcode.RSUB_INT, RSUB_INT_LIT8, false, false),
        SUB_INT(Opcode.SUB_INT, SUB_INT_2ADDR, null, null, false, false),
        MUL_INT(Opcode.MUL_INT, MUL_INT_2ADDR, MUL_INT_LIT16, MUL_INT_LIT8, false, false),
        DIV_INT(Opcode.DIV_INT, DIV_INT_2ADDR, DIV_INT_LIT16, DIV_INT_LIT8, false, false),
        REM_INT(Opcode.REM_INT, REM_INT_2ADDR, REM_INT_LIT16, REM_INT_LIT8, false, false),
        AND_INT(Opcode.AND_INT, AND_INT_2ADDR, AND_INT_LIT16, AND_INT_LIT8, false, false),
        OR_INT(Opcode.OR_INT, OR_INT_2ADDR, OR_INT_LIT16, OR_INT_LIT8, false, false),
        XOR_INT(Opcode.XOR_INT, XOR_INT_2ADDR, XOR_INT_LIT16, XOR_INT_LIT8, false, false),
        SHL_INT(Opcode.SHL_INT, SHL_INT_2ADDR, null, SHL_INT_LIT8, false, false),
        SHR_INT(Opcode.SHR_INT, SHR_INT_2ADDR, null, SHR_INT_LIT8, false, false),
        USHR_INT(Opcode.USHR_INT, USHR_INT_2ADDR, null, USHR_INT_LIT8, false, false),

        ADD_LONG(Opcode.ADD_LONG, ADD_LONG_2ADDR, true, true),
        SUB_LONG(Opcode.SUB_LONG, SUB_LONG_2ADDR, true, true),
        MUL_LONG(Opcode.MUL_LONG, MUL_LONG_2ADDR, true, true),
        DIV_LONG(Opcode.DIV_LONG, DIV_LONG_2ADDR, true, true),
        REM_LONG(Opcode.REM_LONG, REM_LONG_2ADDR, true, true),
        AND_LONG(Opcode.AND_LONG, AND_LONG_2ADDR, true, true),
        OR_LONG(Opcode.OR_LONG, OR_LONG_2ADDR, true, true),
        XOR_LONG(Opcode.XOR_LONG, XOR_LONG_2ADDR, true, true),
        SHL_LONG(Opcode.SHL_LONG, SHL_LONG_2ADDR, true, false),
        SHR_LONG(Opcode.SHR_LONG, SHR_LONG_2ADDR, true, false),
        USHR_LONG(Opcode.USHR_LONG, USHR_LONG_2ADDR, true, false),

        ADD_FLOAT(Opcode.ADD_FLOAT, ADD_FLOAT_2ADDR, false, false),
        SUB_FLOAT(Opcode.SUB_FLOAT, SUB_FLOAT_2ADDR, false, false),
        MUL_FLOAT(Opcode.MUL_FLOAT, MUL_FLOAT_2ADDR, false, false),
        DIV_FLOAT(Opcode.DIV_FLOAT, DIV_FLOAT_2ADDR, false, false),
        REM_FLOAT(Opcode.REM_FLOAT, REM_FLOAT_2ADDR, false, false),

        ADD_DOUBLE(Opcode.ADD_DOUBLE, ADD_DOUBLE_2ADDR, true, true),
        SUB_DOUBLE(Opcode.SUB_DOUBLE, SUB_DOUBLE_2ADDR, true, true),
        MUL_DOUBLE(Opcode.MUL_DOUBLE, MUL_DOUBLE_2ADDR, true, true),
        DIV_DOUBLE(Opcode.DIV_DOUBLE, DIV_DOUBLE_2ADDR, true, true),
        REM_DOUBLE(Opcode.REM_DOUBLE, REM_DOUBLE_2ADDR, true, true);

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

    public CodeBuilder raw_binop(BinOp op, int dst_reg_or_pair,
                                 int first_src_reg_or_pair, int second_src_reg_or_pair) {
        if (op.regular == null) {
            throw new IllegalArgumentException("There is no regular version of " + op);
        }
        return f23x(op.regular, dst_reg_or_pair, op.isDstAndSrc1Wide,
                first_src_reg_or_pair, op.isDstAndSrc1Wide,
                second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder raw_binop_2addr(BinOp op, int dst_and_first_src_reg_or_pair,
                                       int second_src_reg_or_pair) {
        if (op._2addr == null) {
            throw new IllegalArgumentException("There is no 2addr version of " + op);
        }
        return f12x(op._2addr, dst_and_first_src_reg_or_pair,
                op.isDstAndSrc1Wide, second_src_reg_or_pair, op.isSrc2Wide);
    }

    public CodeBuilder binop(BinOp op, int dst_reg_or_pair,
                             int first_src_reg_or_pair, int second_src_reg_or_pair) {
        if (dst_reg_or_pair == first_src_reg_or_pair
                && check_width_int(first_src_reg_or_pair, 4)
                && check_width_int(second_src_reg_or_pair, 4)) {
            return raw_binop_2addr(op, first_src_reg_or_pair, second_src_reg_or_pair);
        }
        return raw_binop(op, dst_reg_or_pair, first_src_reg_or_pair, second_src_reg_or_pair);
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
        if (op == BinOp.SHL_INT || op == BinOp.SHR_INT || op == BinOp.USHR_INT) {
            value &= 0x1f;
        }
        if (check_width_int(value, 8)) {
            return raw_binop_lit8(op, dst_reg, src_reg, value);
        }
        return raw_binop_lit16(op, dst_reg, src_reg, value);
    }

    public CodeBuilder invoke_polymorphic(MethodId method, ProtoId proto, int arg_count, int arg_reg1,
                                          int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return f45cc(INVOKE_POLYMORPHIC, method, proto, arg_count,
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
        return f4rcc(INVOKE_POLYMORPHIC_RANGE, method, proto, arg_count, first_arg_reg);
    }

    public CodeBuilder invoke_custom(CallSiteId callsite, int arg_count, int arg_reg1,
                                     int arg_reg2, int arg_reg3, int arg_reg4, int arg_reg5) {
        return f35c(INVOKE_CUSTOM, callsite, arg_count,
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
        return f3rc(INVOKE_POLYMORPHIC_RANGE, callsite, arg_count, first_arg_reg);
    }

    public CodeBuilder const_method_handle(int dst_reg, MethodHandleId value) {
        return f21c(CONST_METHOD_HANDLE, dst_reg, false, value);
    }

    public CodeBuilder const_method_type(int dst_reg, ProtoId value) {
        return f21c(CONST_METHOD_TYPE, dst_reg, false, value);
    }
}
