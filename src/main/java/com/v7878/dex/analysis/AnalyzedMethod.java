package com.v7878.dex.analysis;

import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.Opcode.APUT_WIDE;
import static com.v7878.dex.Opcode.ARRAY_PAYLOAD;
import static com.v7878.dex.Opcode.IF_EQ;
import static com.v7878.dex.Opcode.INVOKE_STATIC;
import static com.v7878.dex.Opcode.INVOKE_STATIC_RANGE;
import static com.v7878.dex.Opcode.PACKED_SWITCH;
import static com.v7878.dex.Opcode.PACKED_SWITCH_PAYLOAD;
import static com.v7878.dex.Opcode.SPARSE_SWITCH_PAYLOAD;
import static com.v7878.dex.Opcode.THROW;
import static com.v7878.dex.analysis.Position.EXCEPTION_REGISTER;
import static com.v7878.dex.analysis.Position.RESULT_REGISTER;
import static com.v7878.dex.immutable.TypeId.OBJECT;
import static com.v7878.dex.util.Checks.shouldNotReachHere;
import static com.v7878.dex.util.Ids.METHOD_HANDLE;
import static com.v7878.dex.util.Ids.THROWABLE;
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

import com.v7878.collections.IntMap;
import com.v7878.dex.Opcode;
import com.v7878.dex.analysis.Position.Transition;
import com.v7878.dex.analysis.Register.Constant;
import com.v7878.dex.analysis.Register.ConstantKind;
import com.v7878.dex.analysis.Register.Identifier;
import com.v7878.dex.analysis.Register.Primitive;
import com.v7878.dex.analysis.Register.Reference;
import com.v7878.dex.analysis.Register.UninitializedRef;
import com.v7878.dex.analysis.Register.WidePrimitive;
import com.v7878.dex.analysis.RegisterLine.RegisterPair;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.Instruction12x;
import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction21t;
import com.v7878.dex.immutable.bytecode.Instruction22c22cs;
import com.v7878.dex.immutable.bytecode.Instruction22t;
import com.v7878.dex.immutable.bytecode.Instruction23x;
import com.v7878.dex.immutable.bytecode.Instruction31t;
import com.v7878.dex.immutable.bytecode.Instruction35c35mi35ms;
import com.v7878.dex.immutable.bytecode.Instruction3rc3rmi3rms;
import com.v7878.dex.immutable.bytecode.Instruction45cc;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;
import com.v7878.dex.immutable.bytecode.iface.ArrayPayloadInstruction;
import com.v7878.dex.immutable.bytecode.iface.BranchOffsetInstruction;
import com.v7878.dex.immutable.bytecode.iface.LiteralInstruction;
import com.v7878.dex.immutable.bytecode.iface.OneRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.RegisterRangeInstruction;
import com.v7878.dex.immutable.bytecode.iface.SwitchPayloadInstruction;
import com.v7878.dex.immutable.bytecode.iface.ThreeRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.TwoRegisterInstruction;
import com.v7878.dex.immutable.bytecode.iface.VariableFiveRegisterInstruction;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Formatter;

import java.util.BitSet;
import java.util.Objects;
import java.util.function.ToIntFunction;

public final class AnalyzedMethod {
    private final TypeId declaring_class;
    private final MethodDef method;
    private final MethodImplementation implementation;
    private final IntMap<Position> positions;
    private final int last_offset;
    private final ProtoId call_proto;
    private final int register_count;

    private AnalyzedMethod(TypeId declaring_class, MethodDef method,
                           MethodImplementation implementation,
                           IntMap<Position> positions, int last_offset,
                           ProtoId call_proto, int register_count) {
        this.declaring_class = declaring_class;
        this.method = method;
        this.implementation = implementation;
        this.positions = positions;
        this.last_offset = last_offset;
        this.call_proto = call_proto;
        this.register_count = register_count;
    }

    public TypeId getDeclaringClass() {
        return declaring_class;
    }

    public MethodDef getMethod() {
        return method;
    }

    public IntMap<Position> getPositions() {
        return positions;
    }

    public int getLastOffset() {
        return last_offset;
    }

    public ProtoId getCallProto() {
        return call_proto;
    }

    public int getRegisterCount() {
        return register_count;
    }

    private Position position(int address) {
        var out = positions.get(address);
        if (out == null) {
            throw new AnalysisException(
                    "There is no instruction at address 0x" +
                            Integer.toHexString(address));
        }
        return out;
    }

    private Position positionAt(int index) {
        return positions.valueAt(index);
    }

    private <I extends Instruction> I instruction(Opcode expected, int address) {
        I out = position(address).instruction();
        var opcode = out.getOpcode();
        if (opcode != expected) {
            throw new AnalysisException(String.format(
                    "Unknown instruction %s at address 0x%X, expected %s",
                    opcode, address, expected));
        }
        return out;
    }

    public static AnalyzedMethod analyze(
            TypeId declaring_class, MethodDef def, boolean verify) {
        return analyze(TypeResolver.DEFAULT, declaring_class, def, verify);
    }

    public static AnalyzedMethod analyze(
            TypeResolver resolver, TypeId declaring_class,
            MethodDef def, boolean verify) {
        Objects.requireNonNull(declaring_class);
        Objects.requireNonNull(def);
        var implementation = def.getImplementation();
        if (implementation == null) {
            throw new IllegalArgumentException("The method has no implementation");
        }
        int regs = implementation.getRegisterCount();
        var instructions = implementation.getInstructions();
        int size = instructions.size();
        if (size == 0) {
            throw new AnalysisException("Zero-length code");
        }
        var code_map = new IntMap<Position>(size);
        int offset = 0;
        for (int i = 0; i < instructions.size(); i++) {
            var value = instructions.get(i);
            code_map.append(offset, new Position(value, regs, i, offset));
            offset += value.getUnitCount();
        }
        var method = new AnalyzedMethod(declaring_class,
                def, implementation, code_map, offset,
                def.callProto(declaring_class), regs);
        method.init();
        method.analyze(resolver);
        method.freeze();
        return method;
    }

    private void freeze() {
        positions.freeze();
        int count = positions.size();
        for (int i = 0; i < count; i++) {
            positions.valueAt(i).freeze();
        }
    }

    private RegisterLine firstLine() {
        final int address = -1;
        var line = new RegisterLine(register_count);
        var params = method.getParameterTypes();
        var count = params.size();
        int pos = register_count;
        for (int i = count - 1; i >= 0; i--) {
            var type = params.get(i);
            pos -= type.getRegisterCount();
            output(line, address, pos, type);
        }
        if ((method.getAccessFlags() & ACC_STATIC) == 0) {
            pos -= 1;
            var ident = new Identifier(address, pos);
            Register value;
            if (method.isInstanceInitializer()) {
                value = UninitializedRef.of(ident, declaring_class, true);
            } else {
                value = Reference.of(ident, declaring_class);
            }
            line.copy(address, pos, value);
        }
        return line;
    }

    private void init() {
        position(0).before().copy(firstLine());
        int count = positions.size();
        BitSet done = new BitSet(count);
        BitSet todo = new BitSet(count);
        for (int i = 0; i >= 0; i = todo.nextSetBit(0)) {
            todo.clear(i);
            if (done.get(i)) {
                continue;
            }
            done.set(i);
            initPosition(todo, i);
        }
    }

    private static void transition(BitSet todo, Position from,
                                   Position to, TypeId exception) {
        from.successors.add(new Transition(to.address(), exception));
        to.predecessors.add(new Transition(from.address(), exception));
        todo.set(to.index());
    }

    private static void output(Position current, int reg, char shorty) {
        if (shorty == 'J' || shorty == 'D') {
            current.wideOutput(reg);
        } else {
            current.output(reg);
        }
    }

    private static void input(Position current, int reg, char shorty) {
        if (shorty == 'J' || shorty == 'D') {
            current.wideInput(reg);
        } else {
            current.input(reg);
        }
    }

    private static void init_35c_45cc_args(
            Position current, VariableFiveRegisterInstruction tmp, ProtoId proto) {
        var reg_count = tmp.getRegisterCount();
        var sig_count = proto.countInputRegisters();

        if (reg_count != sig_count) {
            throw unexpectedRegisterCount(current, reg_count, sig_count, proto);
        }

        if (reg_count > 0) {
            current.input(tmp.getRegister1());
        }
        if (reg_count > 1) {
            current.input(tmp.getRegister2());
        }
        if (reg_count > 2) {
            current.input(tmp.getRegister3());
        }
        if (reg_count > 3) {
            current.input(tmp.getRegister4());
        }
        if (reg_count > 4) {
            assert reg_count == 5;
            current.input(tmp.getRegister5());
        }

        switch (proto.getReturnType().getRegisterCount()) {
            case 0 -> { /* nop */ }
            case 1 -> current.output(RESULT_REGISTER);
            case 2 -> current.wideOutput(RESULT_REGISTER);
            default -> throw shouldNotReachHere();
        }
    }

    private static void init_3rc_4rcc_args(
            Position current, RegisterRangeInstruction tmp, ProtoId proto) {
        var reg_count = tmp.getRegisterCount();
        var sig_count = proto.countInputRegisters();

        if (reg_count != sig_count) {
            throw unexpectedRegisterCount(
                    current, reg_count, sig_count, proto);
        }

        var reg_start = tmp.getStartRegister();
        current.rangeInput(reg_start, reg_count);

        switch (proto.getReturnType().getRegisterCount()) {
            case 0 -> { /* nop */ }
            case 1 -> current.output(RESULT_REGISTER);
            case 2 -> current.wideOutput(RESULT_REGISTER);
            default -> throw shouldNotReachHere();
        }
    }

    private void initPosition(BitSet todo, int index) {
        var current = positionAt(index);
        if (index == 0) {
            // The first instruction is always reachable
            current.setRuntimeReachable();
        }
        int address = current.address();
        var insn = current.instruction();
        var opcode = insn.getOpcode();
        switch (opcode) {
            case NOP -> current.setNopExact(true);
            case MOVE, MOVE_FROM16, MOVE_16, MOVE_OBJECT,
                 MOVE_OBJECT_FROM16, MOVE_OBJECT_16 -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.input(isrc);
                current.output(idst);
            }
            case MOVE_WIDE, MOVE_WIDE_FROM16, MOVE_WIDE_16 -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.wideInput(isrc);
                current.wideOutput(idst);
            }
            case MOVE_RESULT, MOVE_RESULT_OBJECT -> {
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                current.input(RESULT_REGISTER);
                current.output(idst);
            }
            case MOVE_RESULT_WIDE -> {
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                current.wideInput(RESULT_REGISTER);
                current.wideOutput(idst);
            }
            case MOVE_EXCEPTION -> {
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                current.input(EXCEPTION_REGISTER);
                current.output(idst);
            }
            case RETURN_VOID -> {
                var rtype = method.getReturnType();
                if (!rtype.isVoid()) {
                    throw unexpectedReturnType(current, rtype);
                }
            }
            case RETURN -> {
                var tmp = (OneRegisterInstruction) insn;
                var isrc = tmp.getRegister1();

                var rtype = method.getReturnType();
                if (!rtype.isThinPrimitive()) {
                    throw unexpectedReturnType(current, rtype);
                }

                current.input(isrc);
            }
            case RETURN_WIDE -> {
                var tmp = (OneRegisterInstruction) insn;
                var isrc = tmp.getRegister1();

                var rtype = method.getReturnType();
                if (!rtype.isWidePrimitive()) {
                    throw unexpectedReturnType(current, rtype);
                }

                current.wideInput(isrc);
            }
            case RETURN_OBJECT -> {
                var tmp = (OneRegisterInstruction) insn;
                var isrc = tmp.getRegister1();

                var rtype = method.getReturnType();
                if (!rtype.isReference()) {
                    throw unexpectedReturnType(current, rtype);
                }

                current.input(isrc);
            }
            case CONST_4, CONST_16, CONST, CONST_HIGH16,
                 CONST_STRING, CONST_STRING_JUMBO, CONST_CLASS,
                 CONST_METHOD_TYPE, CONST_METHOD_HANDLE -> {
                var tmp = (OneRegisterInstruction) insn;
                var ireg = tmp.getRegister1();

                current.output(ireg);
            }
            case CONST_WIDE_16, CONST_WIDE_32, CONST_WIDE, CONST_WIDE_HIGH16 -> {
                var tmp = (OneRegisterInstruction) insn;
                var ireg = tmp.getRegister1();

                current.wideOutput(ireg);
            }
            case MONITOR_ENTER, MONITOR_EXIT, THROW -> {
                var tmp = (OneRegisterInstruction) insn;
                var isrc = tmp.getRegister1();

                current.input(isrc);
            }
            case CHECK_CAST -> {
                var tmp = (Instruction21c) insn;
                var ireg = tmp.getRegister1();
                var ref = (TypeId) tmp.getReference1();

                if (ref.isPrimitive()) {
                    throw unexpectedType(current, ref);
                }

                current.input(ireg);
                current.output(ireg);
            }
            case INSTANCE_OF -> {
                var tmp = (Instruction22c22cs) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();
                var ref = (TypeId) tmp.getReference1();

                if (ref.isPrimitive()) {
                    throw unexpectedType(current, ref);
                }

                current.input(isrc);
                current.output(idst);
            }
            case ARRAY_LENGTH ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.input(isrc);
                current.output(idst);
            }
            case NEW_INSTANCE -> {
                var tmp = (Instruction21c) insn;
                var ireg = tmp.getRegister1();
                var ref = (TypeId) tmp.getReference1();

                if (ref.isPrimitive() || ref.isArray()) {
                    throw unexpectedType(current, ref);
                }

                current.output(ireg);
            }
            case NEW_ARRAY -> {
                var tmp = (Instruction22c22cs) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();
                var ref = (TypeId) tmp.getReference1();

                if (ref.isPrimitive() || !ref.isArray()) {
                    throw unexpectedType(current, ref);
                }

                current.input(isrc);
                current.output(idst);
            }
            case FILLED_NEW_ARRAY -> {
                var tmp = (Instruction35c35mi35ms) insn;
                var ref = (TypeId) tmp.getReference1();
                var reg_count = tmp.getRegisterCount();

                var component = ref.componentType();
                if (component == null || component.isWidePrimitive()) {
                    throw unexpectedType(current, ref);
                }

                var proto = ProtoId.raw(ref, Converter.listOf(reg_count, component));
                current.accessProto(proto);

                init_35c_45cc_args(current, tmp, proto);
            }
            case FILLED_NEW_ARRAY_RANGE -> {
                var tmp = (Instruction3rc3rmi3rms) insn;
                var ref = (TypeId) tmp.getReference1();
                var reg_count = tmp.getRegisterCount();

                var component = ref.componentType();
                if (component == null || component.isWidePrimitive()) {
                    throw unexpectedType(current, ref);
                }

                var proto = ProtoId.raw(ref, Converter.listOf(reg_count, component));
                current.accessProto(proto);

                init_3rc_4rcc_args(current, tmp, proto);
            }
            case FILL_ARRAY_DATA -> {
                var tmp = (Instruction31t) insn;
                var ireg = tmp.getRegister1();

                current.payload(instruction(ARRAY_PAYLOAD,
                        address + tmp.getBranchOffset()));

                current.input(ireg);
            }
            case GOTO, GOTO_16, GOTO_32 -> {
                var tmp = (BranchOffsetInstruction) insn;
                var target = position(address + tmp.getBranchOffset());

                current.setNopExact(tmp.getUnitCount() == tmp.getBranchOffset());

                transition(todo, current, target, null);
            }
            case PACKED_SWITCH, SPARSE_SWITCH -> {
                var tmp = (Instruction31t) insn;
                var unit_count = tmp.getUnitCount();
                var ireg = tmp.getRegister1();

                var expected = opcode == PACKED_SWITCH ?
                        PACKED_SWITCH_PAYLOAD : SPARSE_SWITCH_PAYLOAD;
                SwitchPayloadInstruction payload = instruction(
                        expected, address + tmp.getBranchOffset());
                current.payload(payload);

                boolean nop = true;
                for (var entry : payload.getSwitchElements()) {
                    var offset = entry.getOffset();
                    nop = nop && (unit_count == offset);
                    var target = position(address + offset);
                    transition(todo, current, target, null);
                }
                current.setNopExact(nop);

                current.input(ireg);
            }
            case CMPL_FLOAT, CMPG_FLOAT -> {
                var tmp = (Instruction23x) insn;
                var idst = tmp.getRegister1();
                var isrc1 = tmp.getRegister2();
                var isrc2 = tmp.getRegister3();

                current.input(isrc1);
                current.input(isrc2);
                current.output(idst);
            }
            case CMPL_DOUBLE, CMPG_DOUBLE, CMP_LONG -> {
                var tmp = (Instruction23x) insn;
                var idst = tmp.getRegister1();
                var isrc1 = tmp.getRegister2();
                var isrc2 = tmp.getRegister3();

                current.wideInput(isrc1);
                current.wideInput(isrc2);
                current.output(idst);
            }
            case IF_EQZ, IF_NEZ, IF_LTZ, IF_GEZ, IF_GTZ, IF_LEZ -> {
                var tmp = (Instruction21t) insn;
                var ireg = tmp.getRegister1();

                var target = position(address + tmp.getBranchOffset());
                transition(todo, current, target, null);

                current.setNopExact(tmp.getUnitCount() == tmp.getBranchOffset());

                current.input(ireg);
            }
            case IF_EQ, IF_NE, IF_LT, IF_GE, IF_GT, IF_LE -> {
                var tmp = (Instruction22t) insn;
                var ireg1 = tmp.getRegister1();
                var ireg2 = tmp.getRegister2();

                var target = position(address + tmp.getBranchOffset());
                transition(todo, current, target, null);

                current.setNopExact(tmp.getUnitCount() == tmp.getBranchOffset());

                current.input(ireg1);
                current.input(ireg2);
            }
            case AGET, AGET_OBJECT, AGET_BOOLEAN,
                 AGET_BYTE, AGET_CHAR, AGET_SHORT ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (Instruction23x) insn;
                var ival = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var iidx = tmp.getRegister3();

                current.input(iarr);
                current.input(iidx);
                current.output(ival);
            }
            case AGET_WIDE -> {
                var tmp = (Instruction23x) insn;
                var ival = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var iidx = tmp.getRegister3();

                current.input(iarr);
                current.input(iidx);
                current.wideOutput(ival);
            }
            case APUT, APUT_OBJECT, APUT_BOOLEAN,
                 APUT_BYTE, APUT_CHAR, APUT_SHORT -> {
                var tmp = (Instruction23x) insn;
                var ival = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var iidx = tmp.getRegister3();

                current.input(iarr);
                current.input(iidx);
                current.input(ival);
            }
            case APUT_WIDE -> {
                var tmp = (Instruction23x) insn;
                var ival = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var iidx = tmp.getRegister3();

                current.input(iarr);
                current.input(iidx);
                current.wideInput(ival);
            }
            case IGET, IGET_OBJECT, IGET_WIDE, IGET_BOOLEAN,
                 IGET_BYTE, IGET_CHAR, IGET_SHORT -> {
                var tmp = (Instruction22c22cs) insn;
                var ival = tmp.getRegister1();
                var iobj = tmp.getRegister2();

                var ref = (FieldId) tmp.getReference1();
                var ftype = ref.getType();
                var shorty = ftype.getShorty();

                if (!switch (opcode) {
                    case IGET_BOOLEAN -> shorty == 'Z';
                    case IGET_BYTE -> shorty == 'B';
                    case IGET_SHORT -> shorty == 'S';
                    case IGET_CHAR -> shorty == 'C';
                    case IGET -> shorty == 'I' || shorty == 'F';
                    case IGET_WIDE -> shorty == 'J' || shorty == 'D';
                    case IGET_OBJECT -> shorty == 'L';
                    default -> throw shouldNotReachHere();
                }) {
                    throw unexpectedType(current, ftype);
                }

                current.input(iobj);
                output(current, ival, shorty);
            }
            case IPUT, IPUT_OBJECT, IPUT_WIDE, IPUT_BOOLEAN,
                 IPUT_BYTE, IPUT_CHAR, IPUT_SHORT -> {
                var tmp = (Instruction22c22cs) insn;
                var ival = tmp.getRegister1();
                var iobj = tmp.getRegister2();

                var ref = (FieldId) tmp.getReference1();
                var ftype = ref.getType();
                var shorty = ftype.getShorty();

                if (!switch (opcode) {
                    case IPUT_BOOLEAN -> shorty == 'Z';
                    case IPUT_BYTE -> shorty == 'B';
                    case IPUT_SHORT -> shorty == 'S';
                    case IPUT_CHAR -> shorty == 'C';
                    case IPUT -> shorty == 'I' || shorty == 'F';
                    case IPUT_WIDE -> shorty == 'J' || shorty == 'D';
                    case IPUT_OBJECT -> shorty == 'L';
                    default -> throw shouldNotReachHere();
                }) {
                    throw unexpectedType(current, ftype);
                }

                current.input(iobj);
                input(current, ival, shorty);
            }
            case SGET, SGET_OBJECT, SGET_WIDE, SGET_BOOLEAN,
                 SGET_BYTE, SGET_CHAR, SGET_SHORT -> {
                var tmp = (Instruction21c) insn;
                var ival = tmp.getRegister1();

                var ref = (FieldId) tmp.getReference1();
                var ftype = ref.getType();
                var shorty = ftype.getShorty();

                if (!switch (opcode) {
                    case SGET_BOOLEAN -> shorty == 'Z';
                    case SGET_BYTE -> shorty == 'B';
                    case SGET_SHORT -> shorty == 'S';
                    case SGET_CHAR -> shorty == 'C';
                    case SGET -> shorty == 'I' || shorty == 'F';
                    case SGET_WIDE -> shorty == 'J' || shorty == 'D';
                    case SGET_OBJECT -> shorty == 'L';
                    default -> throw shouldNotReachHere();
                }) {
                    throw unexpectedType(current, ftype);
                }

                output(current, ival, shorty);
            }
            case SPUT, SPUT_OBJECT, SPUT_WIDE, SPUT_BOOLEAN,
                 SPUT_BYTE, SPUT_CHAR, SPUT_SHORT -> {
                var tmp = (Instruction21c) insn;
                var ival = tmp.getRegister1();

                var ref = (FieldId) tmp.getReference1();
                var ftype = ref.getType();
                var shorty = ftype.getShorty();

                if (!switch (opcode) {
                    case SPUT_BOOLEAN -> shorty == 'Z';
                    case SPUT_BYTE -> shorty == 'B';
                    case SPUT_SHORT -> shorty == 'S';
                    case SPUT_CHAR -> shorty == 'C';
                    case SPUT -> shorty == 'I' || shorty == 'F';
                    case SPUT_WIDE -> shorty == 'J' || shorty == 'D';
                    case SPUT_OBJECT -> shorty == 'L';
                    default -> throw shouldNotReachHere();
                }) {
                    throw unexpectedType(current, ftype);
                }

                input(current, ival, shorty);
            }
            case INVOKE_VIRTUAL, INVOKE_SUPER, INVOKE_DIRECT,
                 INVOKE_STATIC, INVOKE_INTERFACE -> {
                var tmp = (Instruction35c35mi35ms) insn;
                var ref = (MethodId) tmp.getReference1();

                var proto = (opcode == INVOKE_STATIC) ?
                        ref.getProto() : ref.instanceProto();
                current.accessProto(proto);

                init_35c_45cc_args(current, tmp, proto);
            }
            case INVOKE_VIRTUAL_RANGE, INVOKE_SUPER_RANGE,
                 INVOKE_DIRECT_RANGE, INVOKE_STATIC_RANGE,
                 INVOKE_INTERFACE_RANGE -> {
                var tmp = (Instruction3rc3rmi3rms) insn;
                var ref = (MethodId) tmp.getReference1();

                var proto = (opcode == INVOKE_STATIC_RANGE) ?
                        ref.getProto() : ref.instanceProto();
                current.accessProto(proto);

                init_3rc_4rcc_args(current, tmp, proto);
            }
            case INVOKE_POLYMORPHIC -> {
                var tmp = (Instruction45cc) insn;
                var ref = (ProtoId) tmp.getReference2();

                var proto = ref.insertThis(METHOD_HANDLE);
                current.accessProto(proto);

                init_35c_45cc_args(current, tmp, proto);
            }
            case INVOKE_POLYMORPHIC_RANGE -> {
                var tmp = (Instruction4rcc) insn;
                var ref = (ProtoId) tmp.getReference2();

                var proto = ref.insertThis(METHOD_HANDLE);
                current.accessProto(proto);

                init_3rc_4rcc_args(current, tmp, proto);
            }
            case INVOKE_CUSTOM -> {
                var tmp = (Instruction35c35mi35ms) insn;
                // TODO? verify callsite
                var ref = (CallSiteId) tmp.getReference1();

                var proto = ref.getMethodProto();
                current.accessProto(proto);

                init_35c_45cc_args(current, tmp, proto);
            }
            case INVOKE_CUSTOM_RANGE -> {
                var tmp = (Instruction3rc3rmi3rms) insn;
                // TODO? verify callsite
                var ref = (CallSiteId) tmp.getReference1();

                var proto = ref.getMethodProto();
                current.accessProto(proto);

                init_3rc_4rcc_args(current, tmp, proto);
            }
            case NEG_INT, NOT_INT, NEG_FLOAT, INT_TO_FLOAT, FLOAT_TO_INT,
                 INT_TO_BYTE, INT_TO_CHAR, INT_TO_SHORT -> {
                var tmp = (Instruction12x) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.input(isrc);
                current.output(idst);
            }
            case NEG_LONG, NOT_LONG, NEG_DOUBLE, LONG_TO_DOUBLE, DOUBLE_TO_LONG -> {
                var tmp = (Instruction12x) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.wideInput(isrc);
                current.wideOutput(idst);
            }
            case INT_TO_LONG, INT_TO_DOUBLE, FLOAT_TO_LONG, FLOAT_TO_DOUBLE -> {
                var tmp = (Instruction12x) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.input(isrc);
                current.wideOutput(idst);
            }
            case LONG_TO_INT, LONG_TO_FLOAT, DOUBLE_TO_INT, DOUBLE_TO_FLOAT -> {
                var tmp = (Instruction12x) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.wideInput(isrc);
                current.output(idst);
            }
            case ADD_INT, SUB_INT, MUL_INT, DIV_INT, REM_INT, AND_INT,
                 OR_INT, XOR_INT, SHL_INT, SHR_INT, USHR_INT,
                 ADD_FLOAT, SUB_FLOAT, MUL_FLOAT, DIV_FLOAT, REM_FLOAT ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (Instruction23x) insn;
                var idst = tmp.getRegister1();
                var isrc1 = tmp.getRegister2();
                var isrc2 = tmp.getRegister3();

                current.input(isrc1);
                current.input(isrc2);
                current.output(idst);
            }
            case ADD_LONG, SUB_LONG, MUL_LONG, DIV_LONG,
                 REM_LONG, AND_LONG, OR_LONG, XOR_LONG,
                 ADD_DOUBLE, SUB_DOUBLE, MUL_DOUBLE,
                 DIV_DOUBLE, REM_DOUBLE -> {
                var tmp = (Instruction23x) insn;
                var idst = tmp.getRegister1();
                var isrc1 = tmp.getRegister2();
                var isrc2 = tmp.getRegister3();

                current.wideInput(isrc1);
                current.wideInput(isrc2);
                current.wideOutput(idst);
            }
            case SHL_LONG, SHR_LONG, USHR_LONG -> {
                var tmp = (Instruction23x) insn;
                var idst = tmp.getRegister1();
                var isrc1 = tmp.getRegister2();
                var isrc2 = tmp.getRegister3();

                current.wideInput(isrc1);
                current.input(isrc2);
                current.wideOutput(idst);
            }
            case ADD_INT_2ADDR, SUB_INT_2ADDR, MUL_INT_2ADDR, DIV_INT_2ADDR,
                 REM_INT_2ADDR, AND_INT_2ADDR, OR_INT_2ADDR, XOR_INT_2ADDR,
                 SHL_INT_2ADDR, SHR_INT_2ADDR, USHR_INT_2ADDR,
                 ADD_FLOAT_2ADDR, SUB_FLOAT_2ADDR, MUL_FLOAT_2ADDR,
                 DIV_FLOAT_2ADDR, REM_FLOAT_2ADDR ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (Instruction12x) insn;
                var idst_src1 = tmp.getRegister1();
                var isrc2 = tmp.getRegister2();

                current.input(isrc2);
                current.output(idst_src1);
            }
            case ADD_LONG_2ADDR, SUB_LONG_2ADDR, MUL_LONG_2ADDR, DIV_LONG_2ADDR,
                 REM_LONG_2ADDR, AND_LONG_2ADDR, OR_LONG_2ADDR, XOR_LONG_2ADDR,
                 ADD_DOUBLE_2ADDR, SUB_DOUBLE_2ADDR, MUL_DOUBLE_2ADDR,
                 DIV_DOUBLE_2ADDR, REM_DOUBLE_2ADDR ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (Instruction12x) insn;
                var idst_src1 = tmp.getRegister1();
                var isrc2 = tmp.getRegister2();

                current.wideInput(isrc2);
                current.wideOutput(idst_src1);
            }
            case SHL_LONG_2ADDR, SHR_LONG_2ADDR, USHR_LONG_2ADDR ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (Instruction12x) insn;
                var idst_src1 = tmp.getRegister1();
                var isrc2 = tmp.getRegister2();

                current.input(isrc2);
                current.wideOutput(idst_src1);
            }
            case ADD_INT_LIT16, RSUB_INT, MUL_INT_LIT16, DIV_INT_LIT16,
                 REM_INT_LIT16, AND_INT_LIT16, OR_INT_LIT16, XOR_INT_LIT16,
                 ADD_INT_LIT8, RSUB_INT_LIT8, MUL_INT_LIT8, DIV_INT_LIT8,
                 REM_INT_LIT8, AND_INT_LIT8, OR_INT_LIT8, XOR_INT_LIT8,
                 SHL_INT_LIT8, SHR_INT_LIT8, USHR_INT_LIT8 ->
            //noinspection DuplicateBranchesInSwitch
            {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();

                current.input(isrc);
                current.output(idst);
            }
            default -> {
                if (opcode.isPayload()) {
                    throw new AnalysisException("Encountered data table in instruction stream");
                }
                if (opcode.odexOnly()) {
                    throw new AnalysisException("Encountered odex instruction");
                }
                if (opcode.isRaw()) {
                    throw new AnalysisException("Encountered raw instruction");
                }
                throw shouldNotReachHere();
            }
        }
        if (opcode.canContinue()) {
            if (index + 1 >= positions.size()) {
                throw new AnalysisException("Can flow through to end of code area");
            }
            var target = positionAt(index + 1);
            transition(todo, current, target, null);
        }
        if (opcode.canThrow()) {
            var block = implementation.getTryBlock(address);
            if (block != null) {
                for (var handler : block.getHandlers()) {
                    var target = position(handler.getAddress());
                    transition(todo, current, target, handler.getExceptionType());
                }
                var catch_all = block.getCatchAllAddress();
                if (catch_all != null) {
                    var target = position(catch_all);
                    transition(todo, current, target, THROWABLE);
                }
            }
        }
    }

    private void analyze(TypeResolver resolver) {
        int count = positions.size();
        BitSet touched = new BitSet(count);
        touched.set(0);
        BitSet todo = new BitSet(count);
        for (int i = 0; i >= 0; i = todo.nextSetBit(0)) {
            todo.clear(i);
            analyzePosition(resolver, touched, todo, i);
        }
    }

    private static String describe(Position pos) {
        return Formatter.unsignedHex(pos.address()) + ": " + pos.instruction();
    }

    private static RuntimeException unexpectedReg(Position current, int slot, Register reg) {
        throw new AnalysisException(
                String.format("Register v%s has unexpected type %s as arg to %s", slot, reg, describe(current))
        );
    }

    private static RuntimeException unexpectedRegPair(
            Position current, int slot_lo, int slot_hi, Register reg_lo, Register reg_hi) {
        throw new AnalysisException(
                String.format("Register pair v%s/v%s has unexpected type %s/%s as arg to %s",
                        slot_lo, slot_hi, reg_lo, reg_hi, describe(current))
        );
    }

    private static RuntimeException unexpectedRegPair(Position current, int slot_lo, RegisterPair reg) {
        int slot_hi = slot_lo + 1;
        throw unexpectedRegPair(current, slot_lo, slot_hi, reg.lo(), reg.hi());
    }

    private static RuntimeException unexpectedRegPair(
            Position current, int index, int slot_lo, int slot_hi) {
        throw new AnalysisException(String.format(
                "Rejecting invocation, long or double parameter at index %s is not a pair v%s/v%s in %s",
                index, slot_lo, slot_hi, describe(current)
        ));
    }

    private static RuntimeException unexpectedType(Position current, TypeId type) {
        throw new AnalysisException(
                String.format("Unexpected type %s in %s", type, describe(current))
        );
    }

    private static RuntimeException unexpectedReturnType(Position current, TypeId type) {
        throw new AnalysisException(
                String.format("Unexpected return type %s for %s", type, describe(current))
        );
    }

    private static RuntimeException unexpectedRegisterCount(
            Position current, int reg_count, int sig_count, ProtoId proto) {
        throw new AnalysisException(String.format(
                "Rejecting invocation, expected %s argument registers, but method signature %s has %s in %s",
                reg_count, proto, sig_count, describe(current)
        ));
    }

    private static RuntimeException invalidBranchTarget(Position current) {
        throw new AnalysisException(String.format(
                "Invalid use of %s as branch target", describe(current)
        ));
    }

    private static void merge(TypeResolver resolver, BitSet touched, BitSet todo,
                              Position current, Position target,
                              RegisterLine line, boolean reachable_from_current) {
        int index = target.index();
        if (touched.get(index)) {
            if (target.merge(resolver, line)) {
                todo.set(index);
            }
        } else {
            target.copy(line);
            todo.set(index);
            touched.set(index);
        }
        if (current.isRuntimeReachable()) {
            if (reachable_from_current && !target.isRuntimeReachable()) {
                target.setRuntimeReachable();
                todo.set(index);
            }
        }
    }

    private TypeId getResultType(Position current) {
        var predecessors = current.predecessors;
        if (predecessors.size() != 1) {
            throw invalidBranchTarget(current);
        }
        var transition = predecessors.first();
        if (!transition.isNormal()) {
            throw new AnalysisException(
                    "Exception handler begins with " + describe(current));
        }
        var position = position(transition.address());
        if (position.index() + 1 != current.index()) {
            throw invalidBranchTarget(current);
        }
        var proto = position.accessProto();
        if (proto == null) {
            // TODO: msg
            throw new AnalysisException();
        }
        return proto.getReturnType();
    }

    private TypeInfo getExceptionType(TypeResolver resolver, Position current) {
        var predecessors = current.predecessors;
        TypeInfo common = null;
        for (var transition : predecessors) {
            var exception = transition.exception();
            if (exception == null) {
                throw new AnalysisException("Can flow through to " + describe(current));
            }
            if (exception.isPrimitive() || !TypeResolver._instanceOf(
                    resolver, exception, THROWABLE, true)) {
                throw new AnalysisException("Unexpected non-throwable class target "
                        + exception + " for " + describe(current));
            }
            if (common == null) {
                common = TypeInfo.of(exception);
            } else {
                common = TypeResolver._join(resolver, common, exception);
            }
        }
        if (common == null) {
            // How could this even happen?
            throw shouldNotReachHere();
        }
        return common;
    }

    private static void markInitialized(Position current, int ithis_reg, Register this_reg) {
        var type_info = this_reg.getTypeInfo();
        assert type_info != null;
        assert !type_info.isArray();
        assert type_info.base() != null;
        var type = type_info.base();
        var ident = new Identifier(current.address(), ithis_reg);
        var value = Reference.of(ident, type);
        var line = current.after();
        for (int i = 0; i < line.registerCount(); i++) {
            if (Objects.equals(line.at(i), this_reg)) {
                line.replace(i, value);
            }
        }
    }

    private static void output(RegisterLine line, int address, int slot, TypeId type) {
        if (type != null && type.isWidePrimitive()) {
            var value_lo = WidePrimitive.of(new Identifier(address, slot), type, true);
            var value_hi = WidePrimitive.of(new Identifier(address, slot + 1), type, false);
            line.copyWide(address, slot, value_lo, value_hi);
        } else {
            var ident = new Identifier(address, slot);
            var value = (type == null || type.isReference()) ?
                    Reference.of(ident, type) :
                    Primitive.of(ident, type);
            line.copy(address, slot, value);
        }
    }

    private static void output(Position current, int slot, TypeId type) {
        output(current.after(), current.address(), slot, type);
    }

    private static void output(Position current, int slot, TypeInfo type) {
        var shorty = type.getShorty();
        int address = current.address();
        if (shorty == 'J' || shorty == 'D') {
            var base = type.base();
            var value_lo = WidePrimitive.of(new Identifier(address, slot), base, true);
            var value_hi = WidePrimitive.of(new Identifier(address, slot + 1), base, false);
            current.after().copyWide(address, slot, value_lo, value_hi);
        } else {
            var ident = new Identifier(address, slot);
            var value = type.isReference() ?
                    Reference.of(ident, type) :
                    Primitive.of(ident, type.base());
            current.after().copy(address, slot, value);
        }
    }

    private static void wideConstant(Position current, int slot_lo) {
        int address = current.address();
        int slot_hi = slot_lo + 1;
        var value_lo = Constant.of(new Identifier(address, slot_lo), true);
        var value_hi = Constant.of(new Identifier(address, slot_hi), false);
        current.after().copyWide(address, slot_lo, value_lo, value_hi);
    }

    private static void constant(Position current, int slot, ConstantKind kind) {
        assert !kind.isWide();
        int address = current.address();
        var ident = new Identifier(address, slot);
        var value = Constant.of(ident, kind);
        current.after().copy(address, slot, value);
    }

    private static void constant(Position current, int slot, int value) {
        constant(current, slot, Register.intKind(value));
    }

    private static void verifyReg(TypeResolver resolver, Position current, int ireg, TypeId type) {
        var shorty = type.getShorty();
        if (type.isWidePrimitive()) {
            var reg = current.before().pairAt(ireg);
            if (!switch (shorty) {
                case 'J' -> reg.isLongPair();
                case 'D' -> reg.isDoublePair();
                default -> throw invalidShorty(shorty);
            }) {
                throw unexpectedRegPair(current, ireg, reg);
            }
        } else {
            var reg = current.before().at(ireg);
            if (!switch (shorty) {
                case 'Z', 'B', 'S', 'C', 'I' -> reg.isInt();
                case 'F' -> reg.isFloat();
                case 'L' -> reg.instanceOf(resolver, type, false, true);
                default -> throw invalidShorty(shorty);
            }) {
                throw unexpectedReg(current, ireg, reg);
            }
        }
    }

    private static void unop(Position current, TypeId tdst, TypeId tsrc) {
        assert tdst.isPrimitive() && tsrc.isPrimitive();
        TwoRegisterInstruction insn = current.instruction();
        var idst = insn.getRegister1();
        var isrc = insn.getRegister2();
        verifyReg(null, current, isrc, tsrc);
        output(current, idst, tdst);
    }

    private static void binop(Position current, TypeId tdst, TypeId tsrc1,
                              TypeId tsrc2, boolean check_bool_op) {
        assert tdst.isPrimitive() && tsrc1.isPrimitive() && tsrc2.isPrimitive();
        ThreeRegisterInstruction insn = current.instruction();
        var idst = insn.getRegister1();
        var isrc1 = insn.getRegister2();
        verifyReg(null, current, isrc1, tsrc1);
        var isrc2 = insn.getRegister3();
        verifyReg(null, current, isrc2, tsrc2);
        if (check_bool_op
                && current.before().at(isrc1).isBool()
                && current.before().at(isrc2).isBool()) {
            tdst = TypeId.Z;
        }
        output(current, idst, tdst);
    }

    private static void binop_2addr(Position current, TypeId tdst_src1,
                                    TypeId tsrc2, boolean check_bool_op) {
        assert tdst_src1.isPrimitive() && tsrc2.isPrimitive();
        TwoRegisterInstruction insn = current.instruction();
        var idst_src1 = insn.getRegister1();
        verifyReg(null, current, idst_src1, tdst_src1);
        var isrc2 = insn.getRegister2();
        verifyReg(null, current, isrc2, tsrc2);
        if (check_bool_op
                && current.before().at(idst_src1).isBool()
                && current.before().at(isrc2).isBool()) {
            tdst_src1 = TypeId.Z;
        }
        output(current, idst_src1, tdst_src1);
    }

    private static void binop_lit_int(Position current, boolean check_bool_op) {
        TwoRegisterInstruction insn = current.instruction();
        var idst = insn.getRegister1();
        var isrc = insn.getRegister2();
        verifyReg(null, current, isrc, TypeId.I);
        var type = TypeId.I;
        if (check_bool_op && current.before().at(isrc).isBool()) {
            LiteralInstruction lit = current.instruction();
            if ((lit.getLiteral() & ~1) == 0) {
                type = TypeId.Z;
            }
        }
        output(current, idst, type);
    }

    private static void verify_35c_45cc_args(TypeResolver resolver, Position current, boolean thiz, boolean check_this) {
        VariableFiveRegisterInstruction tmp = current.instruction();
        var proto = current.accessProto();

        var regs = new int[]{
                tmp.getRegister1(),
                tmp.getRegister2(),
                tmp.getRegister3(),
                tmp.getRegister4(),
                tmp.getRegister5()
        };

        int reg_index = 0;
        for (var arg : proto.getParameterTypes()) {
            if (thiz) {
                thiz = false;
                assert arg.isReference();
                if (check_this) {
                    int ithis_reg = regs[reg_index];
                    var this_reg = current.before().at(ithis_reg);
                    // TODO: In Android there is a rather complex
                    //  check here depending on the type of call
                    if (!this_reg.isInitializedRef()) {
                        throw unexpectedReg(current, ithis_reg, this_reg);
                    }
                }
            } else {
                int ireg;
                if (arg.isWidePrimitive()) {
                    int ireg_lo = regs[reg_index];
                    int ireg_hi = regs[reg_index + 1];
                    if (ireg_hi != ireg_lo + 1) {
                        throw unexpectedRegPair(current, reg_index, ireg_lo, ireg_hi);
                    }
                    ireg = ireg_lo;
                } else {
                    ireg = regs[reg_index];
                }
                verifyReg(resolver, current, ireg, arg);
            }
            reg_index += arg.getRegisterCount();
        }
    }

    private static void verify_3rc_4rcc_args(TypeResolver resolver, Position current, boolean thiz, boolean check_this) {
        RegisterRangeInstruction tmp = current.instruction();
        var proto = current.accessProto();

        int ireg = tmp.getStartRegister();
        for (var arg : proto.getParameterTypes()) {
            if (thiz) {
                thiz = false;
                assert arg.isReference();
                if (check_this) {
                    var this_reg = current.before().at(ireg);
                    // TODO: In Android there is a rather complex
                    //  check here depending on the type of call
                    if (!this_reg.isInitializedRef()) {
                        throw unexpectedReg(current, ireg, this_reg);
                    }
                }
            } else {
                verifyReg(resolver, current, ireg, arg);
            }
            ireg += arg.getRegisterCount();
        }
    }

    // At this stage, there is no need to check the boundaries of
    // register indices, as they have already been checked earlier
    private void analyzePosition(TypeResolver resolver, BitSet touched, BitSet todo, int index) {
        var current = positionAt(index);
        int address = current.address();
        var insn = current.instruction();
        var opcode = insn.getOpcode();
        if (opcode == THROW || opcode.isReturn()) {
            // Note: We don't call current.passRegs() here because
            // these instructions never complete execution 'normally'
        } else {
            current.passRegs();
        }
        boolean next_reachable = true;
        boolean is_nop = current.isNopExact();
        boolean is_nnop = current.isNarrowingNop();
        switch (opcode) {
            case NOP, GOTO, GOTO_16, GOTO_32 -> {
                // No effect on or use of registers
            }
            case MOVE, MOVE_FROM16, MOVE_16 -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var dst = current.before().at(idst);
                var isrc = tmp.getRegister2();
                var src = current.before().at(isrc);

                if (!src.isIntOrFloat() && !src.isConflict()) {
                    throw unexpectedReg(current, isrc, src);
                }
                is_nop = (isrc == idst) || Objects.equals(src, dst);

                current.after().copy(address, idst, src);
            }
            case MOVE_WIDE, MOVE_WIDE_FROM16, MOVE_WIDE_16 -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var dst = current.before().pairAt(idst);
                var isrc = tmp.getRegister2();
                var src = current.before().pairAt(isrc);

                if (!src.isWidePair()) {
                    throw unexpectedRegPair(current, isrc, src);
                }
                is_nop = (isrc == idst) || Objects.equals(src, dst);

                current.after().copyWide(address, idst, src);
            }
            case MOVE_OBJECT, MOVE_OBJECT_FROM16, MOVE_OBJECT_16 -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var dst = current.before().at(idst);
                var isrc = tmp.getRegister2();
                var src = current.before().at(isrc);

                if (!src.isRef() && !src.isConflict()) {
                    throw unexpectedReg(current, isrc, src);
                }
                is_nop = (isrc == idst) || Objects.equals(src, dst);

                current.after().copy(address, idst, src);
            }
            // TODO: separate stage for move_result/exception verification
            case MOVE_RESULT -> {
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                var type = getResultType(current);
                if (!type.isThinPrimitive()) {
                    throw unexpectedType(current, type);
                }

                output(current, idst, type);
            }
            case MOVE_RESULT_WIDE -> {
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                var type = getResultType(current);
                if (!type.isWidePrimitive()) {
                    throw unexpectedType(current, type);
                }

                output(current, idst, type);
            }
            case MOVE_RESULT_OBJECT -> {
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                var type = getResultType(current);
                if (!type.isReference()) {
                    throw unexpectedType(current, type);
                }

                output(current, idst, type);
            }
            case MOVE_EXCEPTION -> {
                if (address == 0) {
                    throw new AnalysisException("move-exception at pc 0x0");
                }
                var tmp = (OneRegisterInstruction) insn;
                var idst = tmp.getRegister1();

                var type = getExceptionType(resolver, current);
                assert type.isReference();

                output(current, idst, type);
            }
            case RETURN_VOID -> {
                // TODO: Check for constructors that 'this' is initialized
            }
            case RETURN, RETURN_WIDE, RETURN_OBJECT -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                verifyReg(resolver, current, ireg, method.getReturnType());
            }
            // Could be boolean, int, float, or a null reference
            case CONST_4, CONST_16, CONST, CONST_HIGH16 -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                var reg = current.before().at(ireg);
                var lit = ((LiteralInstruction) insn).getLiteral();
                constant(current, ireg, lit);
                is_nop = lit == 0 && reg.isZero();
            }
            // Could be long or double
            case CONST_WIDE_16, CONST_WIDE_32, CONST_WIDE, CONST_WIDE_HIGH16 -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                wideConstant(current, ireg);
            }
            case CONST_STRING, CONST_STRING_JUMBO -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                constant(current, ireg, ConstantKind.STRING);
            }
            case CONST_CLASS -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                constant(current, ireg, ConstantKind.CLASS);
            }
            case CONST_METHOD_TYPE -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                constant(current, ireg, ConstantKind.METHOD_TYPE);
            }
            case CONST_METHOD_HANDLE -> {
                var ireg = ((OneRegisterInstruction) insn).getRegister1();
                constant(current, ireg, ConstantKind.METHOD_HANDLE);
            }
            case MONITOR_ENTER, MONITOR_EXIT -> {
                var tmp = (OneRegisterInstruction) insn;

                var ireg = tmp.getRegister1();
                var reg = current.before().at(ireg);
                if (!reg.isRef()) {
                    throw unexpectedReg(current, ireg, reg);
                }
            }
            case CHECK_CAST -> {
                var tmp = (Instruction21c) insn;
                var ireg = tmp.getRegister1();
                var reg = current.before().at(ireg);
                var ref = (TypeId) tmp.getReference1();

                if (!reg.isInitializedRef()) {
                    throw unexpectedReg(current, ireg, reg);
                }

                if (reg.isZeroOrNull()) {
                    is_nnop = true;
                    is_nop = false;
                } else {
                    var type = reg.getTypeInfo();
                    assert type != null;
                    if (Objects.equals(type.exactType(), ref)) {
                        is_nnop = false;
                        is_nop = true;
                    } else if (!TypeResolver._instanceOf(resolver, type, ref, true)) {
                        is_nnop = false;
                        is_nop = false;
                        next_reachable = false;
                    } else if (TypeResolver._instanceOf(resolver, type, ref, false)) {
                        is_nnop = true;
                        is_nop = false;
                    } else {
                        is_nnop = false;
                        is_nop = false;
                    }
                }

                output(current, ireg, ref);
            }
            case INSTANCE_OF -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var isrc = tmp.getRegister2();
                var src = current.before().at(isrc);

                if (!src.isInitializedRef()) {
                    throw unexpectedReg(current, isrc, src);
                }

                output(current, idst, TypeId.Z);
            }
            case ARRAY_LENGTH -> {
                var tmp = (TwoRegisterInstruction) insn;
                var idst = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var arr = current.before().at(iarr);

                if (!arr.isArray() && !arr.isZeroOrNull()) {
                    throw unexpectedReg(current, iarr, arr);
                }

                output(current, idst, TypeId.I);
            }
            case NEW_INSTANCE -> {
                var tmp = (Instruction21c) insn;
                var ireg = tmp.getRegister1();
                var ref = (TypeId) tmp.getReference1();

                var ident = new Identifier(address, ireg);
                var value = UninitializedRef.of(ident, ref, false);
                current.after().copy(address, ireg, value);
            }
            case NEW_ARRAY -> {
                var tmp = (Instruction22c22cs) insn;
                var idst = tmp.getRegister1();
                var isz = tmp.getRegister2();
                var sz = current.before().at(isz);
                var ref = (TypeId) tmp.getReference1();

                if (!sz.isInt()) {
                    throw unexpectedReg(current, isz, sz);
                }

                output(current, idst, ref);
            }
            case FILLED_NEW_ARRAY -> verify_35c_45cc_args(resolver, current, false, false);
            case FILLED_NEW_ARRAY_RANGE -> verify_3rc_4rcc_args(resolver, current, false, false);
            case FILL_ARRAY_DATA -> {
                var tmp = (Instruction31t) insn;

                int ireg = tmp.getRegister1();
                var reg = current.before().at(ireg);
                if (reg.isZeroOrNull()) {
                    // Runtime exception
                } else {
                    var info = reg.getTypeInfo();
                    if (info == null || !info.isComponentPrimitive()) {
                        throw unexpectedReg(current, ireg, reg);
                    }
                    var shorty = info.getComponentShorty();
                    var payload = (ArrayPayloadInstruction) current.payload();
                    int elem_width_data = payload.getElementWidth();
                    int elem_width_reg = switch (shorty) {
                        case 'Z', 'B' -> 1;
                        case 'S', 'C' -> 2;
                        case 'I', 'F' -> 4;
                        case 'J', 'D' -> 8;
                        default -> throw invalidShorty(shorty);
                    };
                    if (elem_width_data != elem_width_reg) {
                        throw new AnalysisException("array-data size mismatch: "
                                + elem_width_data + " vs " +
                                elem_width_reg + " in " + describe(current));
                    }
                }
            }
            case THROW -> {
                var tmp = (OneRegisterInstruction) insn;
                var ireg = tmp.getRegister1();

                verifyReg(resolver, current, ireg, THROWABLE);
            }
            case PACKED_SWITCH, SPARSE_SWITCH -> {
                var tmp = (Instruction31t) insn;
                int ireg = tmp.getRegister1();
                var reg = current.before().at(ireg);

                if (!reg.isInt()) {
                    throw unexpectedReg(current, ireg, reg);
                }

                var work_line = current.after();
                var payload = (SwitchPayloadInstruction) current.payload();

                // TODO: reachability test
                for (var entry : payload.getSwitchElements()) {
                    var target = position(address + entry.getOffset());
                    merge(resolver, touched, todo, current, target, work_line, true);
                }

                var target = positionAt(index + 1);
                merge(resolver, touched, todo, current, target, work_line, true);
            }
            case CMPL_FLOAT, CMPG_FLOAT -> binop(current, TypeId.I, TypeId.F, TypeId.F, false);
            case CMPL_DOUBLE, CMPG_DOUBLE -> binop(current, TypeId.I, TypeId.D, TypeId.D, false);
            case CMP_LONG -> binop(current, TypeId.I, TypeId.J, TypeId.J, false);
            case IF_LTZ, IF_GEZ, IF_GTZ, IF_LEZ -> {
                var tmp = (Instruction21t) insn;
                int ireg = tmp.getRegister1();
                var reg = current.before().at(ireg);

                if (!reg.isInt()) {
                    throw unexpectedReg(current, ireg, reg);
                }

                var work_line = current.after();

                // TODO: reachability test

                var target = position(address + tmp.getBranchOffset());
                merge(resolver, touched, todo, current, target, work_line, true);

                target = positionAt(index + 1);
                merge(resolver, touched, todo, current, target, work_line, true);
            }
            case IF_EQZ, IF_NEZ -> {
                var tmp = (Instruction21t) insn;
                int ireg = tmp.getRegister1();
                var reg = current.before().at(ireg);

                if (!reg.isInt() && !reg.isRef()) {
                    throw unexpectedReg(current, ireg, reg);
                }

                // TODO: reachability test
                // TODO?:
                //  Check for peep-hole pattern of:
                //     ...;
                //     instance-of vX, vY, T;
                //     ifXXX vX, label ;
                //     ...;
                //  label:
                //     ...;
                //  and sharpen the type of vY to be type T

                var work_line = current.after();
                var target = position(address + tmp.getBranchOffset());
                merge(resolver, touched, todo, current, target, work_line, true);

                target = positionAt(index + 1);
                merge(resolver, touched, todo, current, target, work_line, true);
            }
            case IF_LT, IF_GE, IF_GT, IF_LE -> {
                var tmp = (Instruction22t) insn;
                int ireg1 = tmp.getRegister1();
                var reg1 = current.before().at(ireg1);
                int ireg2 = tmp.getRegister2();
                var reg2 = current.before().at(ireg2);

                if (!reg1.isInt()) {
                    throw unexpectedReg(current, ireg1, reg1);
                }
                if (!reg2.isInt()) {
                    throw unexpectedReg(current, ireg2, reg2);
                }

                var work_line = current.after();

                // TODO: reachability test

                var target = position(address + tmp.getBranchOffset());
                merge(resolver, touched, todo, current, target, work_line, true);

                target = positionAt(index + 1);
                merge(resolver, touched, todo, current, target, work_line, true);
            }
            case IF_EQ, IF_NE -> {
                var tmp = (Instruction22t) insn;
                int ireg1 = tmp.getRegister1();
                var reg1 = current.before().at(ireg1);
                int ireg2 = tmp.getRegister2();
                var reg2 = current.before().at(ireg2);

                ToIntFunction<Register> classifier = reg -> {
                    if (reg1.isZeroOrNull()) {
                        return 0b00; // 0
                    } else if (reg1.isInt()) {
                        return 0b01; // 1
                    } else if (reg1.isRef()) {
                        return 0b10; // 2
                    }
                    return 0b11; // 3
                };

                int reg1t = classifier.applyAsInt(reg1);
                int reg2t = classifier.applyAsInt(reg2);
                int argt = reg1t | reg2t;

                if (argt == 0b11) {
                    throw new AnalysisException(
                            "Register v" + ireg1 + " of type " + reg1 + " and register v" +
                                    ireg2 + " of type " + reg2 + " can`t be args to " + describe(current)
                    );
                }

                boolean true_pass = argt < 0b11; // always true
                boolean false_pass = (argt != 0b00) && (ireg1 != ireg2)
                        && !Objects.equals(reg1, reg2);

                // Swap eq and non-eq branches
                if (opcode != IF_EQ) {
                    var tmp_bool = true_pass;
                    true_pass = false_pass;
                    false_pass = tmp_bool;
                }

                var work_line = current.after();
                var target = position(address + tmp.getBranchOffset());
                merge(resolver, touched, todo, current, target, work_line, true_pass);

                target = positionAt(index + 1);
                merge(resolver, touched, todo, current, target, work_line, false_pass);
            }
            case AGET, AGET_BOOLEAN, AGET_BYTE, AGET_CHAR,
                 AGET_SHORT, AGET_WIDE, AGET_OBJECT -> {
                var tmp = (Instruction23x) insn;
                var ival = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var arr = current.before().at(iarr);
                var iidx = tmp.getRegister3();
                var idx = current.before().at(iidx);

                if (!idx.isInt()) {
                    throw unexpectedReg(current, iidx, idx);
                }

                if (arr.isZeroOrNull()) {
                    switch (opcode) {
                        // Pick a non-zero constant (to distinguish with null) that can fit in any primitive
                        case AGET, AGET_BOOLEAN, AGET_BYTE, AGET_CHAR,
                             AGET_SHORT -> constant(current, ival, 1);
                        case AGET_WIDE -> wideConstant(current, ival);
                        case AGET_OBJECT -> constant(current, ival, ConstantKind.NULL);
                        default -> throw shouldNotReachHere();
                    }
                    next_reachable = false;
                } else {
                    var info = arr.getTypeInfo();
                    if (info == null || !info.isArray()) {
                        throw unexpectedReg(current, iarr, arr);
                    }
                    var component = info.getComponentType();
                    var shorty = component.getShorty();
                    if (!switch (opcode) {
                        case AGET_BOOLEAN -> shorty == 'Z';
                        case AGET_BYTE -> shorty == 'B';
                        case AGET_SHORT -> shorty == 'S';
                        case AGET_CHAR -> shorty == 'C';
                        case AGET -> shorty == 'I' || shorty == 'F';
                        case AGET_WIDE -> shorty == 'J' || shorty == 'D';
                        case AGET_OBJECT -> shorty == 'L';
                        default -> throw shouldNotReachHere();
                    }) {
                        throw unexpectedReg(current, iarr, arr);
                    }
                    output(current, ival, component);
                }
            }
            case APUT, APUT_BOOLEAN, APUT_BYTE, APUT_CHAR,
                 APUT_SHORT, APUT_WIDE, APUT_OBJECT -> {
                var tmp = (Instruction23x) insn;
                var ival = tmp.getRegister1();
                var iarr = tmp.getRegister2();
                var arr = current.before().at(iarr);
                var iidx = tmp.getRegister3();
                var idx = current.before().at(iidx);

                if (!idx.isInt()) {
                    throw unexpectedReg(current, iidx, idx);
                }

                if (arr.isZeroOrNull()) {
                    if (opcode == APUT_WIDE) {
                        var val = current.before().pairAt(ival);
                        if (!val.isWidePair()) {
                            throw unexpectedRegPair(current, ival, val);
                        }
                    } else {
                        var val = current.before().at(ival);
                        if (!switch (opcode) {
                            case APUT_BOOLEAN, APUT_BYTE, APUT_CHAR,
                                 APUT_SHORT -> val.isInt();
                            case APUT -> val.isIntOrFloat();
                            case APUT_OBJECT -> val.isInitializedRef();
                            default -> throw shouldNotReachHere();
                        }) {
                            throw unexpectedReg(current, ival, val);
                        }
                    }
                    next_reachable = false;
                } else {
                    var info = arr.getTypeInfo();
                    if (info == null || !info.isArray()) {
                        throw unexpectedReg(current, iarr, arr);
                    }
                    var shorty = info.getComponentShorty();
                    if (!switch (opcode) {
                        case APUT_BOOLEAN -> shorty == 'Z';
                        case APUT_BYTE -> shorty == 'B';
                        case APUT_SHORT -> shorty == 'S';
                        case APUT_CHAR -> shorty == 'C';
                        case APUT -> shorty == 'I' || shorty == 'F';
                        case APUT_WIDE -> shorty == 'J' || shorty == 'D';
                        case APUT_OBJECT -> shorty == 'L';
                        default -> throw shouldNotReachHere();
                    }) {
                        throw unexpectedReg(current, iarr, arr);
                    }
                    // Note: The instanceof check for iput-object occurs at runtime
                    var type = shorty == 'L' ? OBJECT : info.base();
                    verifyReg(resolver, current, ival, type);
                }
            }
            case IGET, IGET_BOOLEAN, IGET_BYTE, IGET_CHAR,
                 IGET_SHORT, IGET_OBJECT, IGET_WIDE -> {
                var tmp = (Instruction22c22cs) insn;
                var ival = tmp.getRegister1();
                var iobj = tmp.getRegister2();
                var obj = current.before().at(iobj);
                var ref = (FieldId) tmp.getReference1();

                if (!obj.instanceOf(resolver, ref.getDeclaringClass(), true, true)) {
                    throw unexpectedReg(current, iobj, obj);
                }
                if (obj.isUninitializedRef()) {
                    // Access to fields of an uninitialized object can only
                    // be done in the constructor and only relative to 'this'
                    if (!obj.isUninitializedThis()) {
                        throw unexpectedReg(current, iobj, obj);
                    }
                } else if (obj.isZeroOrNull()) {
                    next_reachable = false;
                }

                output(current, ival, ref.getType());
            }
            case IPUT, IPUT_BOOLEAN, IPUT_BYTE, IPUT_CHAR,
                 IPUT_SHORT, IPUT_OBJECT, IPUT_WIDE -> {
                var tmp = (Instruction22c22cs) insn;
                var ival = tmp.getRegister1();
                var iobj = tmp.getRegister2();
                var obj = current.before().at(iobj);
                var ref = (FieldId) tmp.getReference1();

                if (!obj.instanceOf(resolver, ref.getDeclaringClass(), true, true)) {
                    throw unexpectedReg(current, iobj, obj);
                }

                if (obj.isUninitializedRef()) {
                    // Access to fields of an uninitialized object can only
                    // be done in the constructor and only relative to 'this'
                    if (!obj.isUninitializedThis()) {
                        throw unexpectedReg(current, iobj, obj);
                    }
                } else if (obj.isZeroOrNull()) {
                    next_reachable = false;
                }

                verifyReg(resolver, current, ival, ref.getType());
            }
            case SGET, SGET_BOOLEAN, SGET_BYTE, SGET_CHAR,
                 SGET_SHORT, SGET_OBJECT, SGET_WIDE -> {
                var tmp = (Instruction21c) insn;
                var ireg = tmp.getRegister1();
                var ref = (FieldId) tmp.getReference1();

                output(current, ireg, ref.getType());
            }
            case SPUT, SPUT_BOOLEAN, SPUT_BYTE, SPUT_CHAR,
                 SPUT_SHORT, SPUT_OBJECT, SPUT_WIDE -> {
                var tmp = (Instruction21c) insn;
                var ireg = tmp.getRegister1();
                var ref = (FieldId) tmp.getReference1();

                verifyReg(resolver, current, ireg, ref.getType());
            }
            case INVOKE_DIRECT -> {
                var tmp = (Instruction35c35mi35ms) insn;
                var ref = (MethodId) tmp.getReference1();

                var check_this = true;
                if (ref.isInstanceInitializer()) {
                    assert tmp.getRegisterCount() > 0;
                    int ithis_reg = tmp.getRegister1();
                    var this_reg = current.before().at(ithis_reg);
                    if (!this_reg.isUninitializedRef()) {
                        throw unexpectedReg(current, ithis_reg, this_reg);
                    }
                    markInitialized(current, ithis_reg, this_reg);
                    check_this = false;
                }

                verify_35c_45cc_args(resolver, current, true, check_this);
            }
            case INVOKE_DIRECT_RANGE -> {
                var tmp = (Instruction3rc3rmi3rms) insn;
                var ref = (MethodId) tmp.getReference1();

                var check_this = true;
                if (ref.isInstanceInitializer()) {
                    assert tmp.getRegisterCount() > 0;
                    int ithis_reg = tmp.getStartRegister();
                    var this_reg = current.before().at(ithis_reg);
                    if (!this_reg.isUninitializedRef()) {
                        throw unexpectedReg(current, ithis_reg, this_reg);
                    }
                    markInitialized(current, ithis_reg, this_reg);
                    check_this = false;
                }

                verify_3rc_4rcc_args(resolver, current, true, check_this);
            }
            case INVOKE_VIRTUAL, INVOKE_SUPER, INVOKE_INTERFACE, INVOKE_POLYMORPHIC ->
                    verify_35c_45cc_args(resolver, current, true, true);
            case INVOKE_VIRTUAL_RANGE, INVOKE_SUPER_RANGE, INVOKE_INTERFACE_RANGE,
                 INVOKE_POLYMORPHIC_RANGE -> verify_3rc_4rcc_args(resolver, current, true, true);
            case INVOKE_STATIC, INVOKE_CUSTOM ->
                //noinspection DuplicateBranchesInSwitch
                    verify_35c_45cc_args(resolver, current, false, false);
            case INVOKE_STATIC_RANGE, INVOKE_CUSTOM_RANGE ->
                //noinspection DuplicateBranchesInSwitch
                    verify_3rc_4rcc_args(resolver, current, false, false);
            case NEG_INT, NOT_INT -> unop(current, TypeId.I, TypeId.I);
            case NEG_LONG, NOT_LONG -> unop(current, TypeId.J, TypeId.J);
            case NEG_FLOAT -> unop(current, TypeId.F, TypeId.F);
            case NEG_DOUBLE -> unop(current, TypeId.D, TypeId.D);
            case INT_TO_LONG -> unop(current, TypeId.J, TypeId.I);
            case INT_TO_FLOAT -> unop(current, TypeId.F, TypeId.I);
            case INT_TO_DOUBLE -> unop(current, TypeId.D, TypeId.I);
            case LONG_TO_INT -> unop(current, TypeId.I, TypeId.J);
            case LONG_TO_FLOAT -> unop(current, TypeId.F, TypeId.J);
            case LONG_TO_DOUBLE -> unop(current, TypeId.D, TypeId.J);
            case FLOAT_TO_INT -> unop(current, TypeId.I, TypeId.F);
            case FLOAT_TO_LONG -> unop(current, TypeId.J, TypeId.F);
            case FLOAT_TO_DOUBLE -> unop(current, TypeId.D, TypeId.F);
            case DOUBLE_TO_INT -> unop(current, TypeId.I, TypeId.D);
            case DOUBLE_TO_LONG -> unop(current, TypeId.J, TypeId.D);
            case DOUBLE_TO_FLOAT -> unop(current, TypeId.F, TypeId.D);
            // TODO: Mark as nop if the required type is already in the register
            case INT_TO_BYTE -> unop(current, TypeId.B, TypeId.I);
            case INT_TO_CHAR -> unop(current, TypeId.C, TypeId.I);
            case INT_TO_SHORT -> unop(current, TypeId.S, TypeId.I);
            // TODO: Mark division by zero as unreachable
            case ADD_INT, SUB_INT, MUL_INT, DIV_INT,
                 REM_INT, SHL_INT, SHR_INT, USHR_INT ->
                    binop(current, TypeId.I, TypeId.I, TypeId.I, false);
            case AND_INT, OR_INT, XOR_INT -> binop(current, TypeId.I, TypeId.I, TypeId.I, true);
            case ADD_LONG, SUB_LONG, MUL_LONG, DIV_LONG,
                 REM_LONG, AND_LONG, OR_LONG, XOR_LONG ->
                    binop(current, TypeId.J, TypeId.J, TypeId.J, false);
            case SHL_LONG, SHR_LONG, USHR_LONG ->
                    binop(current, TypeId.J, TypeId.J, TypeId.I, false);
            case ADD_FLOAT, SUB_FLOAT, MUL_FLOAT, DIV_FLOAT, REM_FLOAT ->
                    binop(current, TypeId.F, TypeId.F, TypeId.F, false);
            case ADD_DOUBLE, SUB_DOUBLE, MUL_DOUBLE, DIV_DOUBLE, REM_DOUBLE ->
                    binop(current, TypeId.D, TypeId.D, TypeId.D, false);
            case ADD_INT_2ADDR, SUB_INT_2ADDR, MUL_INT_2ADDR, DIV_INT_2ADDR,
                 REM_INT_2ADDR, SHL_INT_2ADDR, SHR_INT_2ADDR, USHR_INT_2ADDR ->
                    binop_2addr(current, TypeId.I, TypeId.I, false);
            case AND_INT_2ADDR, OR_INT_2ADDR, XOR_INT_2ADDR ->
                    binop_2addr(current, TypeId.I, TypeId.I, true);
            case ADD_LONG_2ADDR, SUB_LONG_2ADDR, MUL_LONG_2ADDR, DIV_LONG_2ADDR,
                 REM_LONG_2ADDR, AND_LONG_2ADDR, OR_LONG_2ADDR, XOR_LONG_2ADDR ->
                    binop_2addr(current, TypeId.J, TypeId.J, false);
            case SHL_LONG_2ADDR, SHR_LONG_2ADDR, USHR_LONG_2ADDR ->
                    binop_2addr(current, TypeId.J, TypeId.I, false);
            case ADD_FLOAT_2ADDR, SUB_FLOAT_2ADDR, MUL_FLOAT_2ADDR,
                 DIV_FLOAT_2ADDR, REM_FLOAT_2ADDR ->
                    binop_2addr(current, TypeId.F, TypeId.F, false);
            case ADD_DOUBLE_2ADDR, SUB_DOUBLE_2ADDR, MUL_DOUBLE_2ADDR,
                 DIV_DOUBLE_2ADDR, REM_DOUBLE_2ADDR ->
                    binop_2addr(current, TypeId.D, TypeId.D, false);
            case ADD_INT_LIT16, RSUB_INT, MUL_INT_LIT16, DIV_INT_LIT16, REM_INT_LIT16,
                 ADD_INT_LIT8, RSUB_INT_LIT8, MUL_INT_LIT8, DIV_INT_LIT8, REM_INT_LIT8,
                 SHL_INT_LIT8, SHR_INT_LIT8, USHR_INT_LIT8 -> binop_lit_int(current, false);
            case AND_INT_LIT16, OR_INT_LIT16, XOR_INT_LIT16,
                 AND_INT_LIT8, OR_INT_LIT8, XOR_INT_LIT8 -> binop_lit_int(current, true);
            default -> throw shouldNotReachHere();
        }
        current.setNopExact(is_nop);
        current.setNarrowingNop(is_nnop);
        if (opcode.isConditionalBranch() || opcode.isSwitch()) {
            // 'if' and 'switch' instructions have special handling because they
            // provide information about the state of the register in different branches
            assert !opcode.canThrow();
        } else {
            for (var transition : current.successors) {
                var target = position(transition.address());
                // If this is a transition to catch handler, then registers in the
                // state before execution of the current instruction are placed there
                var line = transition.isCatch() ? current.before() : current.after();
                var reachable = transition.isCatch() || next_reachable;
                merge(resolver, touched, todo, current, target, line, reachable);
            }
        }
    }
}
