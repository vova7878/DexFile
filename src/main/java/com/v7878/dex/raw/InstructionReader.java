package com.v7878.dex.raw;

import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
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
        }

        var opcode = reader.opcodes().getOpcodeByValue(raw_opcode);

        return switch (opcode.format()) {
            case Format10t -> read_10t(opcode, arg);
            case Format10x -> read_10x(opcode, arg);
            case Format11n -> read_11n(opcode, arg);
            case Format11x -> read_11x(opcode, arg);
            case Format12x -> read_12x(opcode, arg);
            // TODO
            case Format20bc -> throw new UnsupportedOperationException("Unimplemented yet!");
            case Format20t -> read_20t(opcode, in, arg);
            case Format21c -> read_21c(opcode, in, reader, arg);
            case Format21ih -> read_21ih(opcode, in, arg);
            case Format21lh -> read_21lh(opcode, in, arg);
            case Format21s -> read_21s(opcode, in, arg);
            case Format21t -> read_21t(opcode, in, arg);
            case Format22b -> read_22b(opcode, in, arg);
            case Format22c22cs -> read_22c22cs(opcode, in, reader, arg);
            case Format22s -> read_22s(opcode, in, arg);
            case Format22t -> read_22t(opcode, in, arg);
            case Format22x -> read_22x(opcode, in, arg);
            case Format23x -> read_23x(opcode, in, arg);
            case Format30t -> read_30t(opcode, in, arg);
            case Format31c -> read_31c(opcode, in, reader, arg);
            case Format31i -> read_31i(opcode, in, arg);
            case Format31t -> read_31t(opcode, in, arg);
            case Format32x -> read_32x(opcode, in, arg);
            case Format35c35mi35ms -> read_35c_35ms_35mi(opcode, in, reader, arg);
            case Format3rc3rmi3rms -> read_3rc_3rms_3rmi(opcode, in, reader, arg);
            case Format45cc -> read_45cc(opcode, in, reader, arg);
            case Format4rcc -> read_4rcc(opcode, in, reader, arg);
            case Format51l -> read_51l(opcode, in, arg);
            case ArrayPayload -> read_array_payload(opcode, in, arg);
            case PackedSwitchPayload -> read_packed_switch_payload(opcode, in, arg);
            case SparseSwitchPayload -> read_sparse_switch_payload(opcode, in, arg);
            case FormatRaw -> throw shouldNotReachHere();
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
            throw new IllegalStateException("Read more code units than expected");
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

    private static int extend_sign32(int value, int width) {
        int shift = 32 - width;
        return (value << shift) >> shift;
    }

    public static Instruction10x read_10x(Opcode opcode, int _00) {
        check_zero_arg(_00);
        return Instruction10x.of(opcode);
    }

    public static Instruction12x read_12x(Opcode opcode, int BA) {
        return Instruction12x.of(opcode, BA & 0xf, BA >> 4);
    }

    public static Instruction11n read_11n(Opcode opcode, int BA) {
        return Instruction11n.of(opcode, BA & 0xf,
                extend_sign32(BA >> 4, 4));
    }

    public static Instruction11x read_11x(Opcode opcode, int AA) {
        return Instruction11x.of(opcode, AA);
    }

    public static Instruction10t read_10t(Opcode opcode, int AA) {
        return Instruction10t.of(opcode, extend_sign32(AA, 8));
    }

    public static Instruction20t read_20t(Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAA = in.readUShort();
        return Instruction20t.of(opcode, extend_sign32(AAAA, 16));
    }

    public static Instruction22x read_22x(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return Instruction22x.of(opcode, AA, BBBB);
    }

    public static Instruction21t read_21t(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return Instruction21t.of(opcode, AA, extend_sign32(BBBB, 16));
    }

    public static Instruction21s read_21s(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return Instruction21s.of(opcode, AA, extend_sign32(BBBB, 16));
    }

    public static Instruction21ih read_21ih(Opcode opcode, RandomInput in, int AA) {
        int BBBB = in.readUShort();
        return Instruction21ih.of(opcode, AA, BBBB << 16);
    }

    public static Instruction21lh read_21lh(Opcode opcode, RandomInput in, int AA) {
        long BBBB = in.readUShort();
        return Instruction21lh.of(opcode, AA, BBBB << 48);
    }

    public static Instruction21c read_21c(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        return Instruction21c.of(opcode, AA, indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static Instruction22c22cs read_22c22cs(
            Opcode opcode, RandomInput in, DexReader context, int BA) {
        int CCCC = in.readUShort();
        return Instruction22c22cs.of(opcode, BA & 0xf, BA >> 4,
                indexToRef(opcode.getReferenceType1(), context, CCCC));
    }

    public static Instruction23x read_23x(Opcode opcode, RandomInput in, int AA) {
        int CCBB = in.readUShort();
        return Instruction23x.of(opcode, AA, CCBB & 0xff, CCBB >> 8);
    }

    public static Instruction22b read_22b(Opcode opcode, RandomInput in, int AA) {
        int CCBB = in.readUShort();
        return Instruction22b.of(opcode, AA, CCBB & 0xff,
                extend_sign32(CCBB >> 8, 8));
    }

    public static Instruction22t read_22t(Opcode opcode, RandomInput in, int BA) {
        int CCCC = in.readUShort();
        return Instruction22t.of(opcode, BA & 0xf,
                BA >> 4, extend_sign32(CCCC, 16));
    }

    public static Instruction22s read_22s(Opcode opcode, RandomInput in, int BA) {
        int CCCC = in.readUShort();
        return Instruction22s.of(opcode, BA & 0xf,
                BA >> 4, extend_sign32(CCCC, 16));
    }

    public static Instruction30t read_30t(Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAAlo = in.readUShort();
        int AAAAhi = in.readUShort();
        return Instruction30t.of(opcode, AAAAlo | (AAAAhi << 16));
    }

    public static Instruction32x read_32x(Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAA = in.readUShort();
        int BBBB = in.readUShort();
        return Instruction32x.of(opcode, AAAA, BBBB);
    }

    public static Instruction31i read_31i(Opcode opcode, RandomInput in, int AA) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        return Instruction31i.of(opcode, AA, BBBBlo | (BBBBhi << 16));
    }

    public static Instruction31t read_31t(Opcode opcode, RandomInput in, int AA) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        return Instruction31t.of(opcode, AA, BBBBlo | (BBBBhi << 16));
    }

    public static Instruction31c read_31c(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBBlo = in.readUShort();
        int BBBBhi = in.readUShort();
        int BBBBBBBB = BBBBlo | (BBBBhi << 16);
        return Instruction31c.of(opcode, AA, indexToRef(opcode.getReferenceType1(), context, BBBBBBBB));
    }

    public static Instruction35c35mi35ms read_35c_35ms_35mi(
            Opcode opcode, RandomInput in, DexReader context, int AG) {
        int A = AG >> 4;
        int G = AG & 0xf;
        int BBBB = in.readUShort();
        int FEDC = in.readUShort();
        int F = FEDC >> 12;
        int E = (FEDC >> 8) & 0xf;
        int D = (FEDC >> 4) & 0xf;
        int C = FEDC & 0xf;
        return Instruction35c35mi35ms.of(opcode, A, C, D, E, F, G,
                indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static Instruction3rc3rmi3rms read_3rc_3rms_3rmi(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        int CCCC = in.readUShort();
        return Instruction3rc3rmi3rms.of(opcode, AA, CCCC,
                indexToRef(opcode.getReferenceType1(), context, BBBB));
    }

    public static Instruction45cc read_45cc(
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
        return Instruction45cc.of(opcode, A, C, D, E, F, G,
                indexToRef(opcode.getReferenceType1(), context, BBBB),
                indexToRef(opcode.getReferenceType2(), context, HHHH));
    }

    public static Instruction4rcc read_4rcc(
            Opcode opcode, RandomInput in, DexReader context, int AA) {
        int BBBB = in.readUShort();
        int CCCC = in.readUShort();
        int HHHH = in.readUShort();
        return Instruction4rcc.of(opcode, AA, CCCC,
                indexToRef(opcode.getReferenceType1(), context, BBBB),
                indexToRef(opcode.getReferenceType2(), context, HHHH));
    }

    public static Instruction51l read_51l(Opcode opcode, RandomInput in, int AA) {
        long BBBBlolo = in.readUShort();
        long BBBBhilo = in.readUShort();
        long BBBBlohi = in.readUShort();
        long BBBBhihi = in.readUShort();
        return Instruction51l.of(opcode, AA, (BBBBhihi << 48) | (BBBBlohi << 32)
                | (BBBBhilo << 16) | BBBBlolo);
    }

    public static PackedSwitchPayload read_packed_switch_payload(
            Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int size = in.readUShort();
        int first_key = in.readInt();
        int[] targets = in.readIntArray(size);
        var elements = new ArrayList<SwitchElement>(targets.length);
        for (int i = 0; i < targets.length; i++) {
            elements.add(i, SwitchElement.of(first_key + i, targets[i]));
        }
        return PackedSwitchPayload.of(elements);
    }

    public static SparseSwitchPayload read_sparse_switch_payload(
            Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
        int size = in.readUShort();
        int[] keys = in.readIntArray(size);
        int[] targets = in.readIntArray(size);
        var elements = new ArrayList<SwitchElement>(size);
        for (int i = 0; i < targets.length; i++) {
            elements.add(i, SwitchElement.of(keys[i], targets[i]));
        }
        return SparseSwitchPayload.of(elements);
    }

    public static ArrayPayload read_array_payload(
            Opcode opcode, RandomInput in, int _00) {
        check_zero_arg(_00);
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
}
