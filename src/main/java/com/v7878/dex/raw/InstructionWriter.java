package com.v7878.dex.raw;

import static com.v7878.dex.DexOffsets.PAYLOAD_INSTRUCTION_ALIGNMENT;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
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
import com.v7878.dex.io.RandomOutput;

import java.util.NavigableSet;
import java.util.Objects;

public class InstructionWriter {
    public static void writeInstruction(Instruction instruction, DexWriter writer, RandomOutput out) {
        var opcode = instruction.getOpcode();
        // TODO: message if null
        int op = writer.opcodes().getOpcodeValue(opcode);
        switch (opcode.format()) {
            case Format10t -> write_10t(((Instruction10t) instruction), out, op);
            case Format10x -> write_10x(((Instruction10x) instruction), out, op);
            case Format11n -> write_11n(((Instruction11n) instruction), out, op);
            case Format11x -> write_11x(((Instruction11x) instruction), out, op);
            case Format12x -> write_12x(((Instruction12x) instruction), out, op);
            // TODO
            case Format20bc -> throw new UnsupportedOperationException("Unimplemented yet!");
            case Format20t -> write_20t(((Instruction20t) instruction), out, op);
            case Format21c -> write_21c(((Instruction21c) instruction), writer, out, op);
            case Format21ih -> write_21ih(((Instruction21ih) instruction), out, op);
            case Format21lh -> write_21lh(((Instruction21lh) instruction), out, op);
            case Format21s -> write_21s(((Instruction21s) instruction), out, op);
            case Format21t -> write_21t(((Instruction21t) instruction), out, op);
            case Format22b -> write_22b(((Instruction22b) instruction), out, op);
            case Format22c22cs ->
                    write_22c_22cs(((Instruction22c22cs) instruction), writer, out, op);
            case Format22s -> write_22s(((Instruction22s) instruction), out, op);
            case Format22t -> write_22t(((Instruction22t) instruction), out, op);
            case Format22x -> write_22x(((Instruction22x) instruction), out, op);
            case Format23x -> write_23x(((Instruction23x) instruction), out, op);
            case Format30t -> write_30t(((Instruction30t) instruction), out, op);
            case Format31c -> write_31c(((Instruction31c) instruction), writer, out, op);
            case Format31i -> write_31i(((Instruction31i) instruction), out, op);
            case Format31t -> write_31t(((Instruction31t) instruction), out, op);
            case Format32x -> write_32x(((Instruction32x) instruction), out, op);
            case Format35c35mi35ms ->
                    write_35c_35ms_35mi(((Instruction35c35mi35ms) instruction), writer, out, op);
            case Format3rc3rmi3rms ->
                    write_3rc_3rms_3rmi(((Instruction3rc3rmi3rms) instruction), writer, out, op);
            case Format45cc -> write_45cc(((Instruction45cc) instruction), writer, out, op);
            case Format4rcc -> write_4rcc(((Instruction4rcc) instruction), writer, out, op);
            case Format51l -> write_51l(((Instruction51l) instruction), out, op);
            case ArrayPayload -> write_array_payload(((ArrayPayload) instruction), out, op);
            case PackedSwitchPayload ->
                    write_packed_switch_payload(((PackedSwitchPayload) instruction), out, op);
            case SparseSwitchPayload ->
                    write_sparse_switch_payload(((SparseSwitchPayload) instruction), out, op);
        }
    }

    private static int refToIndex(ReferenceType type, DexWriter context, Object value) {
        Objects.requireNonNull(value);
        return switch (type) {
            case STRING -> context.getStringIndex((String) value);
            case TYPE -> context.getTypeIndex((TypeId) value);
            case FIELD -> context.getFieldIndex((FieldId) value);
            case METHOD -> context.getMethodIndex((MethodId) value);
            case PROTO -> context.getProtoIndex((ProtoId) value);
            case CALLSITE -> context.getCallSiteIndex((CallSiteId) value);
            case METHOD_HANDLE -> context.getMethodHandleIndex((MethodHandleId) value);
            case RAW_INDEX -> (Integer) value;
        };
    }

    public static int check_unsigned(int value, int width) {
        if ((value >>> width) != 0) {
            throw new IllegalStateException("Illegal instruction unsigned value "
                    + Integer.toHexString(value) + " for width " + width);
        }
        return value & (~0 >>> (32 - width));
    }

    public static int check_signed(int value, int width) {
        int empty_width = 32 - width;
        if (value << empty_width >> empty_width != value) {
            throw new IllegalStateException("Illegal instruction signed value "
                    + Integer.toHexString(value) + " for width " + width);
        }
        return value & (~0 >>> empty_width);
    }

    public static int check_hat32(int value, int width) {
        if ((value & -1 >>> width) != 0) {
            throw new IllegalStateException("Illegal instruction hat value "
                    + Integer.toHexString(value) + " for width " + width);
        }
        return value >>> (32 - width);
    }

    public static long check_hat64(long value, int width) {
        if ((value & -1L >>> width) != 0) {
            throw new IllegalStateException("Illegal instruction hat value "
                    + Long.toHexString(value) + " for width " + width);
        }
        return value >>> (64 - width);
    }

    private static void write_base(RandomOutput out, int opcode, int arg) {
        if (opcode >>> 8 != 0) {
            throw new IllegalStateException("Illegal opcode: " + opcode);
        }
        if (arg >>> 8 != 0) {
            throw new IllegalStateException("Illegal arg: " + arg);
        }
        out.writeShort((arg << 8) | opcode);
    }

    private static void write_base(RandomOutput out, int payload_opcode) {
        if (payload_opcode >>> 16 != 0) {
            throw new IllegalStateException("Illegal payload_opcode: " + payload_opcode);
        }
        out.writeShort(payload_opcode);
    }

    public static void write_10x(RandomOutput out, int opcode) {
        write_base(out, opcode, 0);
    }

    public static void write_10x(Instruction10x value, RandomOutput out, int opcode) {
        Objects.requireNonNull(value);
        write_10x(out, opcode);
    }

    public static void write_12x(RandomOutput out, int opcode, int A, int B) {
        A = check_unsigned(A, 4);
        B = check_unsigned(B, 4);
        write_base(out, opcode, (B << 4) | A);
    }

    public static void write_12x(Instruction12x value, RandomOutput out, int opcode) {
        write_12x(out, opcode, value.getRegister1(), value.getRegister2());
    }

    public static void write_11n(RandomOutput out, int opcode, int A, int sB) {
        A = check_unsigned(A, 4);
        sB = check_signed(sB, 4);
        write_base(out, opcode, (sB << 4) | A);
    }

    public static void write_11n(Instruction11n value, RandomOutput out, int opcode) {
        write_11n(out, opcode, value.getRegister1(), value.getLiteral());
    }

    public static void write_11x(RandomOutput out, int opcode, int AA) {
        AA = check_unsigned(AA, 8);
        write_base(out, opcode, AA);
    }

    public static void write_11x(Instruction11x value, RandomOutput out, int opcode) {
        write_11x(out, opcode, value.getRegister1());
    }

    public static void write_10t(RandomOutput out, int opcode, int sAA) {
        sAA = check_signed(sAA, 8);
        write_base(out, opcode, sAA);
    }

    public static void write_10t(Instruction10t value, RandomOutput out, int opcode) {
        write_10t(out, opcode, value.getBranchOffset());
    }

    public static void write_20t(RandomOutput out, int opcode, int sAAAA) {
        sAAAA = check_signed(sAAAA, 16);
        write_base(out, opcode, 0);
        out.writeShort(sAAAA);
    }

    public static void write_20t(Instruction20t value, RandomOutput out, int opcode) {
        write_20t(out, opcode, value.getBranchOffset());
    }

    public static void write_22x_21c(RandomOutput out, int opcode, int AA, int BBBB) {
        AA = check_unsigned(AA, 8);
        BBBB = check_unsigned(BBBB, 16);
        write_base(out, opcode, AA);
        out.writeShort(BBBB);
    }

    public static void write_22x(Instruction22x value, RandomOutput out, int opcode) {
        write_22x_21c(out, opcode, value.getRegister1(), value.getRegister2());
    }

    public static void write_21c(Instruction21c value, DexWriter indexer, RandomOutput out, int opcode) {
        write_22x_21c(out, opcode, value.getRegister1(), refToIndex(value
                .getReferenceType1(), indexer, value.getReference1()));
    }

    public static void write_21t_21s(RandomOutput out, int opcode, int AA, int sBBBB) {
        AA = check_unsigned(AA, 8);
        sBBBB = check_signed(sBBBB, 16);
        write_base(out, opcode, AA);
        out.writeShort(sBBBB);
    }

    public static void write_21t(Instruction21t value, RandomOutput out, int opcode) {
        write_21t_21s(out, opcode, value.getRegister1(), value.getBranchOffset());
    }

    public static void write_21s(Instruction21s value, RandomOutput out, int opcode) {
        write_21t_21s(out, opcode, value.getRegister1(), value.getLiteral());
    }

    public static void write_21ih(RandomOutput out, int opcode, int AA, int BBBB0000) {
        AA = check_unsigned(AA, 8);
        int BBBB = check_hat32(BBBB0000, 16);
        write_base(out, opcode, AA);
        out.writeShort(BBBB);
    }

    public static void write_21ih(Instruction21ih value, RandomOutput out, int opcode) {
        write_21ih(out, opcode, value.getRegister1(), value.getLiteral());
    }

    public static void write_21lh(RandomOutput out, int opcode, int AA, long BBBB000000000000) {
        AA = check_unsigned(AA, 8);
        int BBBB = (int) check_hat64(BBBB000000000000, 16);
        write_base(out, opcode, AA);
        out.writeShort(BBBB);
    }

    public static void write_21lh(Instruction21lh value, RandomOutput out, int opcode) {
        write_21lh(out, opcode, value.getRegister1(), value.getWideLiteral());
    }

    public static void write_22c_22cs(RandomOutput out, int opcode, int A, int B, int cCCCC) {
        A = check_unsigned(A, 4);
        B = check_unsigned(B, 4);
        cCCCC = check_unsigned(cCCCC, 16);
        write_base(out, opcode, (B << 4) | A);
        out.writeShort(cCCCC);
    }

    public static void write_22c_22cs(Instruction22c22cs value, DexWriter indexer, RandomOutput out, int opcode) {
        write_22c_22cs(out, opcode, value.getRegister1(), value.getRegister2(),
                refToIndex(value.getReferenceType1(), indexer, value.getReference1()));
    }

    public static void write_23x(RandomOutput out, int opcode, int AA, int BB, int CC) {
        AA = check_unsigned(AA, 8);
        BB = check_unsigned(BB, 8);
        CC = check_unsigned(CC, 8);
        write_base(out, opcode, AA);
        out.writeShort((CC << 8) | BB);
    }

    public static void write_23x(Instruction23x value, RandomOutput out, int opcode) {
        write_23x(out, opcode, value.getRegister1(), value.getRegister2(), value.getRegister3());
    }

    public static void write_22b(RandomOutput out, int opcode, int AA, int BB, int sCC) {
        AA = check_unsigned(AA, 8);
        BB = check_unsigned(BB, 8);
        sCC = check_signed(sCC, 8);
        write_base(out, opcode, AA);
        out.writeShort((sCC << 8) | BB);
    }

    public static void write_22b(Instruction22b value, RandomOutput out, int opcode) {
        write_22b(out, opcode, value.getRegister1(), value.getRegister2(), value.getLiteral());
    }

    public static void write_22t_22s(RandomOutput out, int opcode, int A, int B, int sCCCC) {
        A = check_unsigned(A, 4);
        B = check_unsigned(B, 4);
        sCCCC = check_signed(sCCCC, 16);
        write_base(out, opcode, (B << 4) | A);
        out.writeShort(sCCCC);
    }

    public static void write_22t(Instruction22t value, RandomOutput out, int opcode) {
        write_22t_22s(out, opcode, value.getRegister1(), value.getRegister2(), value.getBranchOffset());
    }

    public static void write_22s(Instruction22s value, RandomOutput out, int opcode) {
        write_22t_22s(out, opcode, value.getRegister1(), value.getRegister2(), value.getLiteral());
    }

    public static void write_30t(RandomOutput out, int opcode, int AAAAAAAA) {
        // no need to check AAAAAAAA
        write_base(out, opcode, 0);
        out.writeShort(AAAAAAAA & 0xffff);
        out.writeShort(AAAAAAAA >>> 16);
    }

    public static void write_30t(Instruction30t value, RandomOutput out, int opcode) {
        write_30t(out, opcode, value.getBranchOffset());
    }

    public static void write_32x(RandomOutput out, int opcode, int AAAA, int BBBB) {
        AAAA = check_unsigned(AAAA, 16);
        BBBB = check_unsigned(BBBB, 16);
        write_base(out, opcode, 0);
        out.writeShort(AAAA);
        out.writeShort(BBBB);
    }

    public static void write_32x(Instruction32x value, RandomOutput out, int opcode) {
        write_32x(out, opcode, value.getRegister1(), value.getRegister2());
    }

    public static void write_31i_31t_31c(RandomOutput out, int opcode, int AA, int BBBBBBBB) {
        check_unsigned(AA, 8);
        // no need to check BBBBBBBB
        write_base(out, opcode, AA);
        out.writeShort(BBBBBBBB & 0xffff);
        out.writeShort(BBBBBBBB >>> 16);
    }

    public static void write_31i(Instruction31i value, RandomOutput out, int opcode) {
        write_31i_31t_31c(out, opcode, value.getRegister1(), value.getLiteral());
    }

    public static void write_31t(Instruction31t value, RandomOutput out, int opcode) {
        write_31i_31t_31c(out, opcode, value.getRegister1(), value.getBranchOffset());
    }

    public static void write_31c(Instruction31c value, DexWriter indexer, RandomOutput out, int opcode) {
        write_31i_31t_31c(out, opcode, value.getRegister1(), refToIndex(value
                .getReferenceType1(), indexer, value.getReference1()));
    }

    public static void write_35c_35ms_35mi(RandomOutput out, int opcode, int A,
                                           int BBBB, int C, int D, int E, int F, int G) {
        A = check_unsigned(A, 4);
        BBBB = check_unsigned(BBBB, 16);
        C = check_unsigned(C, 4);
        D = check_unsigned(D, 4);
        E = check_unsigned(E, 4);
        F = check_unsigned(F, 4);
        G = check_unsigned(G, 4);
        write_base(out, opcode, (A << 4) | G);
        out.writeShort(BBBB);
        out.writeShort((F << 12) | (E << 8) | (D << 4) | C);
    }

    public static void write_35c_35ms_35mi(Instruction35c35mi35ms value, DexWriter indexer, RandomOutput out, int opcode) {
        write_35c_35ms_35mi(out, opcode, value.getRegisterCount(), refToIndex(value
                        .getReferenceType1(), indexer, value.getReference1()),
                value.getRegister1(), value.getRegister2(), value.getRegister3(),
                value.getRegister4(), value.getRegister5());
    }

    public static void write_3rc_3rms_3rmi(RandomOutput out, int opcode,
                                           int AA, int BBBB, int CCCC) {
        AA = check_unsigned(AA, 8);
        BBBB = check_unsigned(BBBB, 16);
        CCCC = check_unsigned(CCCC, 16);
        write_base(out, opcode, AA);
        out.writeShort(BBBB);
        out.writeShort(CCCC);
    }

    public static void write_3rc_3rms_3rmi(Instruction3rc3rmi3rms value, DexWriter indexer, RandomOutput out, int opcode) {
        write_3rc_3rms_3rmi(out, opcode, value.getRegisterCount(), refToIndex(value
                .getReferenceType1(), indexer, value.getReference1()), value.getStartRegister());
    }

    public static void write_45cc(RandomOutput out, int opcode, int A,
                                  int BBBB, int C, int D, int E, int F, int G, int HHHH) {
        A = check_unsigned(A, 4);
        BBBB = check_unsigned(BBBB, 16);
        C = check_unsigned(C, 4);
        D = check_unsigned(D, 4);
        E = check_unsigned(E, 4);
        F = check_unsigned(F, 4);
        G = check_unsigned(G, 4);
        HHHH = check_unsigned(HHHH, 16);
        write_base(out, opcode, (A << 4) | G);
        out.writeShort(BBBB);
        out.writeShort((F << 12) | (E << 8) | (D << 4) | C);
        out.writeShort(HHHH);
    }

    public static void write_45cc(Instruction45cc value, DexWriter indexer, RandomOutput out, int opcode) {
        write_45cc(out, opcode, value.getRegisterCount(), refToIndex(value
                        .getReferenceType1(), indexer, value.getReference1()),
                value.getRegister1(), value.getRegister2(), value.getRegister3(),
                value.getRegister4(), value.getRegister5(), refToIndex(value
                        .getReferenceType2(), indexer, value.getReference2()));
    }

    public static void write_4rcc(RandomOutput out, int opcode,
                                  int AA, int BBBB, int CCCC, int HHHH) {
        AA = check_unsigned(AA, 8);
        BBBB = check_unsigned(BBBB, 16);
        CCCC = check_unsigned(CCCC, 16);
        HHHH = check_unsigned(HHHH, 16);
        write_base(out, opcode, AA);
        out.writeShort(BBBB);
        out.writeShort(CCCC);
        out.writeShort(HHHH);
    }

    public static void write_4rcc(Instruction4rcc value, DexWriter indexer, RandomOutput out, int opcode) {
        write_4rcc(out, opcode, value.getRegisterCount(), refToIndex(value
                        .getReferenceType1(), indexer, value.getReference1()), value.getStartRegister(),
                refToIndex(value.getReferenceType2(), indexer, value.getReference2()));
    }

    public static void write_51l(RandomOutput out, int opcode, int AA, long BBBBBBBBBBBBBBBB) {
        AA = check_unsigned(AA, 8);
        // no need to check BBBBBBBBBBBBBBBB
        write_base(out, opcode, AA);
        out.writeShort((int) (BBBBBBBBBBBBBBBB & 0xffff));
        out.writeShort((int) ((BBBBBBBBBBBBBBBB >>> 16) & 0xffff));
        out.writeShort((int) ((BBBBBBBBBBBBBBBB >>> 32) & 0xffff));
        out.writeShort((int) (BBBBBBBBBBBBBBBB >>> 48));
    }

    public static void write_51l(Instruction51l value, RandomOutput out, int opcode) {
        write_51l(out, opcode, value.getRegister1(), value.getWideLiteral());
    }

    public static void write_packed_switch_payload(RandomOutput out, int opcode,
                                                   int first_key, int[] targets) {
        Objects.requireNonNull(targets);
        int size = targets.length;
        if ((size & 0xffff) != size) {
            throw new IllegalStateException("size is too big: " + size);
        }
        out.requireAlignment(PAYLOAD_INSTRUCTION_ALIGNMENT);
        write_base(out, opcode);
        out.writeShort(size);
        out.writeInt(first_key);
        out.writeIntArray(targets);
    }

    public static void write_packed_switch_payload(PackedSwitchPayload value, RandomOutput out, int opcode) {
        var elements = value.getSwitchElements();
        int[] targets = elements.stream().mapToInt(SwitchElement::getOffset).toArray();
        write_packed_switch_payload(out, opcode, targets.length == 0 ? 0 : elements.first().getKey(), targets);
    }

    public static void write_sparse_switch_payload(RandomOutput out, int opcode,
                                                   NavigableSet<SwitchElement> elements) {
        Objects.requireNonNull(elements);
        int size = elements.size();
        if ((size & 0xffff) != size) {
            throw new IllegalStateException("size is too big: " + size);
        }
        int[] keys = elements.stream().mapToInt(SwitchElement::getKey).toArray();
        int[] targets = elements.stream().mapToInt(SwitchElement::getOffset).toArray();
        out.requireAlignment(PAYLOAD_INSTRUCTION_ALIGNMENT);
        write_base(out, opcode);
        out.writeShort(size);
        out.writeIntArray(keys);
        out.writeIntArray(targets);
    }

    public static void write_sparse_switch_payload(SparseSwitchPayload value, RandomOutput out, int opcode) {
        write_sparse_switch_payload(out, opcode, value.getSwitchElements());
    }

    public static void write_array_payload(RandomOutput out, int opcode,
                                           int element_width, Object data) {
        Objects.requireNonNull(data);
        out.requireAlignment(PAYLOAD_INSTRUCTION_ALIGNMENT);
        write_base(out, opcode);
        out.writeShort(element_width);
        switch (element_width) {
            case 1 -> {
                byte[] raw_data = (byte[]) data;
                out.writeInt(raw_data.length);
                out.writeByteArray(raw_data);
            }
            case 2 -> {
                short[] raw_data = (short[]) data;
                out.writeInt(raw_data.length);
                out.writeShortArray(raw_data);
            }
            case 4 -> {
                int[] raw_data = (int[]) data;
                out.writeInt(raw_data.length);
                out.writeIntArray(raw_data);
            }
            case 8 -> {
                long[] raw_data = (long[]) data;
                out.writeInt(raw_data.length);
                out.writeLongArray(raw_data);
            }
            default -> throw new AssertionError();
        }
        out.fillZerosToAlignment(2); // unit size
    }

    // TODO: simplify
    public static void write_array_payload(ArrayPayload value, RandomOutput out, int opcode) {
        var elements = value.getArrayElements();
        var element_width = value.getElementWidth();
        Object data;
        switch (element_width) {
            case 1 -> {
                byte[] raw_data = new byte[elements.size()];
                for (int i = 0; i < raw_data.length; i++) {
                    raw_data[i] = (Byte) elements.get(i);
                }
                data = raw_data;
            }
            case 2 -> {
                short[] raw_data = new short[elements.size()];
                for (int i = 0; i < raw_data.length; i++) {
                    raw_data[i] = (Short) elements.get(i);
                }
                data = raw_data;
            }
            case 4 -> data = elements.stream().mapToInt(
                    element -> (Integer) element).toArray();
            case 8 -> data = elements.stream().mapToLong(
                    element -> (Long) element).toArray();
            default -> throw new AssertionError();
        }
        write_array_payload(out, opcode, element_width, data);
    }
}
