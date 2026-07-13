package com.v7878.dex.raw;

import static com.v7878.dex.util.Checks.shouldNotReachHere;
import static com.v7878.dex.util.MathUtils.extend_sign32;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.bytecode.ArrayPayload;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.InstructionN0t;
import com.v7878.dex.immutable.bytecode.InstructionN0x;
import com.v7878.dex.immutable.bytecode.InstructionN1c;
import com.v7878.dex.immutable.bytecode.InstructionN1i;
import com.v7878.dex.immutable.bytecode.InstructionN1l;
import com.v7878.dex.immutable.bytecode.InstructionN1p;
import com.v7878.dex.immutable.bytecode.InstructionN1t;
import com.v7878.dex.immutable.bytecode.InstructionN1x;
import com.v7878.dex.immutable.bytecode.InstructionN2c;
import com.v7878.dex.immutable.bytecode.InstructionN2i;
import com.v7878.dex.immutable.bytecode.InstructionN2t;
import com.v7878.dex.immutable.bytecode.InstructionN2x;
import com.v7878.dex.immutable.bytecode.InstructionN3x;
import com.v7878.dex.immutable.bytecode.InstructionNrc;
import com.v7878.dex.immutable.bytecode.InstructionNrcc;
import com.v7878.dex.immutable.bytecode.InstructionNv4c;
import com.v7878.dex.immutable.bytecode.InstructionNv5c;
import com.v7878.dex.immutable.bytecode.InstructionNv5cc;
import com.v7878.dex.immutable.bytecode.InstructionRaw0c;
import com.v7878.dex.immutable.bytecode.InstructionRaw0x;
import com.v7878.dex.immutable.bytecode.SwitchElement;
import com.v7878.dex.immutable.bytecode.SwitchPayload;
import com.v7878.dex.io.RandomInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InstructionReader {
    public static Instruction read(DexReader reader, RandomInput in) {
        int unit = in.readUShort();

        int raw_opcode = unit & 0xff;
        int arg = unit >> 8;

        if (raw_opcode == 0x00 && arg != 0) {
            raw_opcode = unit;
            arg = 0;
        } else if (raw_opcode == 0xff) {
            if (reader.options().hasExpandedInstructions()) {
                raw_opcode = unit;
                arg = 0;
            }
        }

        var opcode = reader.opcodes().getOpcodeByValue(raw_opcode);

        return switch (opcode.format()) {
            case Format10t -> read_10t(opcode, arg);
            case Format10x -> read_10x(opcode, arg);
            case Format11n -> read_11n(opcode, arg);
            case Format11p -> read_11p(opcode, arg);
            case Format11x -> read_11x(opcode, arg);
            case Format12x -> read_12x(opcode, arg);
            // TODO
            case Format20bc -> throw new UnsupportedOperationException("Unimplemented yet!");
            case Format20t -> read_20t_16(opcode, in, arg);
            case Format20t_24 -> read_20t_24(opcode, in, arg);
            case Format21c -> read_21c(opcode, in, reader, arg);
            case Format21ih -> read_21ih(opcode, in, arg);
            case Format21lh -> read_21lh(opcode, in, arg);
            case Format21s -> read_21s(opcode, in, arg);
            case Format21t -> read_21t(opcode, in, arg);
            case Format22b -> read_22b(opcode, in, arg);
            case Format22c -> read_22c(opcode, in, reader, arg);
            case Format22s -> read_22s(opcode, in, arg);
            case Format22t -> read_22t(opcode, in, arg);
            case Format22x -> read_22x(opcode, in, arg);
            case Format23x -> read_23x(opcode, in, arg);
            case Format30t -> read_30t(opcode, in, arg);
            case Format31c -> read_31c(opcode, in, reader, arg);
            case Format31i -> read_31i(opcode, in, arg);
            case Format31t -> read_31t(opcode, in, arg);
            case Format32x -> read_32x(opcode, in, arg);
            case Format34c -> read_34c(opcode, in, reader, arg);
            case Format35c -> read_35c(opcode, in, reader, arg);
            case Format3rc -> read_3rc(opcode, in, reader, arg);
            // TODO
            case Format40cs -> //noinspection DuplicateBranchesInSwitch
                    throw new UnsupportedOperationException("Unimplemented yet!");
            case Format41c -> read_41c(opcode, in, reader);
            case Format45cc -> read_45cc(opcode, in, reader, arg);
            case Format4rcc -> read_4rcc(opcode, in, reader, arg);
            case Format51l -> read_51l(opcode, in, arg);
            case Format52c -> read_52c(opcode, in, reader);
            case Format5rc -> read_5rc(opcode, in, reader);
            case ArrayPayload -> read_array_payload(opcode, in);
            case PackedSwitchPayload -> read_packed_switch_payload(opcode, in);
            case SparseSwitchPayload -> read_sparse_switch_payload(opcode, in);
            case MPackedSwitchPayload -> read_m_packed_switch_payload(opcode, in);
            case MSparseSwitchPayload -> read_m_sparse_switch_payload(opcode, in);
            case FormatRaw10x, FormatRaw10c, FormatRaw20c -> throw shouldNotReachHere();
            case FormatWrapper20x -> read_wrapper_20x(opcode, in);
            case FormatWrapper40ci -> read_wrapper_40ci(opcode, in, reader);
        };
    }

    //TODO: find a way to read code with incorrect instructions (which used to protect dex from reading)
    public static List<Instruction> readArray(DexReader reader, RandomInput in, int insns_count) {
        var insns = new ArrayList<Instruction>(insns_count);

        int insns_bytes = insns_count * 2;
        int start = in.position();

        int readed;
        while ((readed = in.position() - start) < insns_bytes) {
            if ((readed & 1) != 0) {
                throw new IllegalStateException("Unaligned code unit");
            }
            insns.add(read(reader, in));
        }

        if (readed != insns_bytes) {
            throw new IllegalStateException(
                    String.format("Read more code units (%s) than expected (%s)", readed, insns_bytes)
            );
        }

        insns.trimToSize();
        return Collections.unmodifiableList(insns);
    }

    private static Object indexToRef(ReferenceType type, DexReader context, int index) {
        return switch (type) {
            case STRING -> context.getString(index);
            case TYPE -> context.getType(index);
            case FIELD -> context.getField(index);
            case METHOD -> context.getMethod(index);
            case PROTO -> context.getProto(index);
            case CLASS_DEF -> context.getClassDefHeader(index).type();
            case CALLSITE -> context.getCallSite(index);
            case METHOD_HANDLE -> context.getMethodHandle(index);
            case RAW_INDEX -> index;
        };
    }

    private static void check_zero_arg(int _00) {
        if (_00 != 0) {
            throw new IllegalStateException("arg should be zero, but actual value is " + _00);
        }
    }

    public static InstructionN0x read_10x(Opcode opcode, int _00) {
        check_zero_arg(_00);
        return InstructionN0x.of(opcode);
    }

    public static InstructionN2x read_12x(Opcode opcode, int BA) {
        return InstructionN2x.of(opcode, BA & 0xf, BA >> 4);
    }

    public static InstructionN1i read_11n(Opcode opcode, int BA) {
        return InstructionN1i.of(opcode, BA & 0xf,
                extend_sign32(BA >> 4, 4));
    }

    public static InstructionN1p read_11p(Opcode opcode, int BA) {
        return InstructionN1p.of(opcode, BA & 0xf, BA >> 4);
    }

    public static InstructionN1x read_11x(Opcode opcode, int AA) {
        return InstructionN1x.of(opcode, AA);
    }

    public static InstructionN0t read_10t(Opcode opcode, int AA) {
        return InstructionN0t.of(opcode, extend_sign32(AA, 8));
    }

    public static InstructionN0t read_20t_16(Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAA = in.readUShort();
        return InstructionN0t.of(opcode, extend_sign32(AAAA, 16));
    }

    public static InstructionN0t read_20t_24(Opcode opcode, RandomInput in, int AAh) {
        int AAAAl = in.readUShort();
        int AAAAAA = AAAAl | (AAh << 16);
        return InstructionN0t.of(opcode, extend_sign32(AAAAAA, 24));
    }

    public static InstructionN2x read_22x(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return InstructionN2x.of(opcode, AA, BBBB);
    }

    public static InstructionN1t read_21t(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return InstructionN1t.of(opcode, AA, extend_sign32(BBBB, 16));
    }

    public static InstructionN1i read_21s(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return InstructionN1i.of(opcode, AA, extend_sign32(BBBB, 16));
    }

    public static InstructionN1i read_21ih(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return InstructionN1i.of(opcode, AA, BBBB << 16);
    }

    public static InstructionN1l read_21lh(Opcode opcode, RandomInput in, int AA) {
        long BBBB = in.readUShort();
        return InstructionN1l.of(opcode, AA, BBBB << 48);
    }

    public static InstructionN1c read_21c(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        return InstructionN1c.of(opcode, AA, indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static InstructionN2c read_22c(
            Opcode opcode, RandomInput in, DexReader context, int BA) {
        int CCCC = in.readUShort();
        return InstructionN2c.of(opcode, BA & 0xf, BA >> 4,
                indexToRef(opcode.getReferenceType1(), context, CCCC));
    }

    public static InstructionN3x read_23x(Opcode opcode, RandomInput in, int AA) {
        int CCBB = in.readUShort();
        return InstructionN3x.of(opcode, AA, CCBB & 0xff, CCBB >> 8);
    }

    public static InstructionN2i read_22b(Opcode opcode, RandomInput in, int AA) {
        int CCBB = in.readUShort();
        return InstructionN2i.of(opcode, AA, CCBB & 0xff,
                extend_sign32(CCBB >> 8, 8));
    }

    public static InstructionN2t read_22t(Opcode opcode, RandomInput in, int BA) {
        int CCCC = in.readUShort();
        return InstructionN2t.of(opcode, BA & 0xf,
                BA >> 4, extend_sign32(CCCC, 16));
    }

    public static InstructionN2i read_22s(Opcode opcode, RandomInput in, int BA) {
        int CCCC = in.readUShort();
        return InstructionN2i.of(opcode, BA & 0xf,
                BA >> 4, extend_sign32(CCCC, 16));
    }

    public static InstructionN0t read_30t(Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAAlo = in.readUShort();
        int AAAAhi = in.readUShort();
        return InstructionN0t.of(opcode, AAAAlo | (AAAAhi << 16));
    }

    public static InstructionN2x read_32x(Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAA = in.readUShort();
        int BBBB = in.readUShort();
        return InstructionN2x.of(opcode, AAAA, BBBB);
    }

    public static InstructionN1i read_31i(Opcode opcode, RandomInput in, int AA) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        return InstructionN1i.of(opcode, AA, BBBBlo | (BBBBhi << 16));
    }

    public static InstructionN1t read_31t(Opcode opcode, RandomInput in, int AA) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        return InstructionN1t.of(opcode, AA, BBBBlo | (BBBBhi << 16));
    }

    public static InstructionN1c read_31c(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        int BBBBBBBB = BBBBlo | (BBBBhi << 16);
        return InstructionN1c.of(opcode, AA, indexToRef(opcode.getReferenceType1(), context, BBBBBBBB));
    }

    public static InstructionNv4c read_34c(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        int FEDC = in.readUShort();
        int F = FEDC >> 12;
        int E = (FEDC >> 8) & 0xf;
        int D = (FEDC >> 4) & 0xf;
        int C = FEDC & 0xf;
        return InstructionNv4c.of(opcode, AA, C, D, E, F,
                indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static InstructionNv5c read_35c(
            Opcode opcode, RandomInput in, DexReader context, int AG) {
        int A = AG >> 4;
        int G = AG & 0xf;
        int BBBB = in.readUShort();
        int FEDC = in.readUShort();
        int F = FEDC >> 12;
        int E = (FEDC >> 8) & 0xf;
        int D = (FEDC >> 4) & 0xf;
        int C = FEDC & 0xf;
        return InstructionNv5c.of(opcode, A, C, D, E, F, G,
                indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static InstructionNrc read_3rc(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        int CCCC = in.readUShort();
        return InstructionNrc.of(opcode, AA, CCCC,
                indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static InstructionN1c read_41c(
            Opcode opcode, RandomInput in, DexReader context) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        int BBBBBBBB = BBBBlo | (BBBBhi << 16);
        int AAAA = in.readUShort();
        return InstructionN1c.of(opcode, AAAA, indexToRef(opcode.getReferenceType1(), context, BBBBBBBB));
    }

    public static InstructionNv5cc read_45cc(
            Opcode opcode, RandomInput in, DexReader context, int AG) {
        int A = AG >> 4;
        int G = AG & 0xf;
        int BBBB = in.readUShort();
        int FEDC = in.readUShort();
        int F = FEDC >> 12;
        int E = (FEDC >> 8) & 0xf;
        int D = (FEDC >> 4) & 0xf;
        int C = FEDC & 0xf;
        int HHHH = in.readUShort();
        return InstructionNv5cc.of(opcode, A, C, D, E, F, G,
                indexToRef(opcode.getReferenceType1(), context, BBBB),
                indexToRef(opcode.getReferenceType2(), context, HHHH));
    }

    public static InstructionNrcc read_4rcc(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        int CCCC = in.readUShort();
        int HHHH = in.readUShort();
        return InstructionNrcc.of(opcode, AA, CCCC,
                indexToRef(opcode.getReferenceType1(), context, BBBB),
                indexToRef(opcode.getReferenceType2(), context, HHHH));
    }

    public static InstructionN1l read_51l(Opcode opcode, RandomInput in, int AA) {
        long BBBBlolo = in.readUShort();
        long BBBBhilo = in.readUShort();
        long BBBBlohi = in.readUShort();
        long BBBBhihi = in.readUShort();
        return InstructionN1l.of(opcode, AA, (BBBBhihi << 48) | (BBBBlohi << 32)
                | (BBBBhilo << 16) | BBBBlolo);
    }

    public static InstructionN2c read_52c(
            Opcode opcode, RandomInput in, DexReader context) {
        int CCCClo = in.readUShort();
        int CCCChi = in.readUShort();
        int CCCCCCCC = CCCClo | (CCCChi << 16);
        int AAAA = in.readUShort();
        int BBBB = in.readUShort();
        return InstructionN2c.of(opcode, AAAA, BBBB,
                indexToRef(opcode.getReferenceType1(), context, CCCCCCCC));
    }

    public static InstructionNrc read_5rc(
            Opcode opcode, RandomInput in, DexReader context) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        int BBBBBBBB = BBBBlo | (BBBBhi << 16);
        int AAAA = in.readUShort();
        int CCCC = in.readUShort();
        return InstructionNrc.of(opcode, AAAA, CCCC,
                indexToRef(opcode.getReferenceType1(), context, BBBBBBBB));
    }

    public static SwitchPayload read_packed_switch_payload(
            Opcode opcode, RandomInput in) {
        int size = in.readUShort();
        int first_key = in.readInt();
        int[] targets = in.readIntArray(size);
        var elements = new ArrayList<SwitchElement>(targets.length);
        for (int i = 0; i < targets.length; i++) {
            elements.add(i, SwitchElement.of(first_key + i, targets[i]));
        }
        return SwitchPayload.of(opcode, elements);
    }

    public static SwitchPayload read_sparse_switch_payload(
            Opcode opcode, RandomInput in) {
        int size = in.readUShort();
        int[] keys = in.readIntArray(size);
        int[] targets = in.readIntArray(size);
        var elements = new ArrayList<SwitchElement>(size);
        for (int i = 0; i < targets.length; i++) {
            elements.add(i, SwitchElement.of(keys[i], targets[i]));
        }
        return SwitchPayload.of(opcode, elements);
    }

    public static ArrayPayload read_array_payload(
            Opcode opcode, RandomInput in) {
        int element_width = in.readUShort();
        int size = in.readSmallUInt();
        var data = new ArrayList<Number>(size);
        switch (element_width) {
            case 1 -> {
                for (int i = 0; i < size; i++) {
                    data.add(i, in.readByte());
                }
            }
            case 2 -> {
                for (int i = 0; i < size; i++) {
                    data.add(i, in.readShort());
                }
            }
            case 4 -> {
                for (int i = 0; i < size; i++) {
                    data.add(i, in.readInt());
                }
            }
            case 8 -> {
                for (int i = 0; i < size; i++) {
                    data.add(i, in.readLong());
                }
            }
            default -> throw new IllegalStateException(
                    "Invalid element width:" + element_width);
        }
        in.alignPosition(2); // code unit
        return ArrayPayload.of(element_width, data);
    }

    public static SwitchPayload read_m_packed_switch_payload(
            Opcode opcode, RandomInput in) {
        int size = in.readUShort();
        int first_key = in.readInt();
        short[] targets = in.readShortArray(size);
        var elements = new ArrayList<SwitchElement>(targets.length);
        for (int i = 0; i < targets.length; i++) {
            elements.add(i, SwitchElement.of(first_key + i, targets[i]));
        }
        return SwitchPayload.of(opcode, elements);
    }

    public static SwitchPayload read_m_sparse_switch_payload(
            Opcode opcode, RandomInput in) {
        int size = in.readUShort();
        int[] keys = in.readIntArray(size);
        short[] targets = in.readShortArray(size);
        var elements = new ArrayList<SwitchElement>(size);
        for (int i = 0; i < targets.length; i++) {
            elements.add(i, SwitchElement.of(keys[i], targets[i]));
        }
        return SwitchPayload.of(opcode, elements);
    }

    public static InstructionRaw0x read_wrapper_20x(
            Opcode opcode, RandomInput in) {
        int AAAA = in.readUShort();
        return InstructionRaw0x.of(opcode, (short) AAAA);
    }

    public static InstructionRaw0c read_wrapper_40ci(
            Opcode opcode, RandomInput in, DexReader context) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        int BBBBBBBB = BBBBlo | (BBBBhi << 16);
        int AAAA = in.readUShort();
        var ref_type = ReferenceType.values()[AAAA];
        return InstructionRaw0c.of(opcode, ref_type, indexToRef(ref_type, context, BBBBBBBB));
    }
}
