package com.v7878.dex.bytecode;

import static com.v7878.dex.bytecode.Format.ArrayPayload;
import static com.v7878.dex.bytecode.Format.Format10t;
import static com.v7878.dex.bytecode.Format.Format10x;
import static com.v7878.dex.bytecode.Format.Format11n;
import static com.v7878.dex.bytecode.Format.Format11x;
import static com.v7878.dex.bytecode.Format.Format12x;
import static com.v7878.dex.bytecode.Format.Format20t;
import static com.v7878.dex.bytecode.Format.Format21c;
import static com.v7878.dex.bytecode.Format.Format21ih;
import static com.v7878.dex.bytecode.Format.Format21lh;
import static com.v7878.dex.bytecode.Format.Format21t21s;
import static com.v7878.dex.bytecode.Format.Format22b;
import static com.v7878.dex.bytecode.Format.Format22c;
import static com.v7878.dex.bytecode.Format.Format22t22s;
import static com.v7878.dex.bytecode.Format.Format22x;
import static com.v7878.dex.bytecode.Format.Format23x;
import static com.v7878.dex.bytecode.Format.Format30t;
import static com.v7878.dex.bytecode.Format.Format31c;
import static com.v7878.dex.bytecode.Format.Format31i31t;
import static com.v7878.dex.bytecode.Format.Format32x;
import static com.v7878.dex.bytecode.Format.Format35c35ms35mi;
import static com.v7878.dex.bytecode.Format.Format3rc3rms3rmi;
import static com.v7878.dex.bytecode.Format.Format45cc;
import static com.v7878.dex.bytecode.Format.Format4rcc;
import static com.v7878.dex.bytecode.Format.Format51l;
import static com.v7878.dex.bytecode.Format.PAYLOAD_ALIGNMENT;
import static com.v7878.dex.bytecode.Format.PackedSwitchPayload;
import static com.v7878.dex.bytecode.Format.SparseSwitchPayload;

import com.v7878.dex.ReadContext;
import com.v7878.dex.io.RandomInput;

class InstructionReader {

    private InstructionReader() {
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

    public static Format10x.Instance read_10x(Format10x format, int _00) {
        check_zero_arg(_00);
        return format.make();
    }

    public static Format12x.Instance read_12x(Format12x format, int BA) {
        return format.make(BA & 0xf, BA >> 4);
    }

    public static Format11n.Instance read_11n(Format11n format, int BA) {
        return format.make(BA & 0xf, extend_sign32(BA >> 4, 4));
    }

    public static Format11x.Instance read_11x(Format11x format, int AA) {
        return format.make(AA);
    }

    public static Format10t.Instance read_10t(Format10t format, int AA) {
        return format.make(extend_sign32(AA, 8));
    }

    public static Format20t.Instance read_20t(Format20t format, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAA = in.readUnsignedShort();
        return format.make(extend_sign32(AAAA, 16));
    }

    public static Format22x.Instance read_22x(Format22x format, RandomInput in, int AA) {
        int BBBB = in.readUnsignedShort();
        return format.make(AA, BBBB);
    }

    public static Format21t21s.Instance read_21t_21s(Format21t21s format, RandomInput in, int AA) {
        int BBBB = in.readUnsignedShort();
        return format.make(AA, extend_sign32(BBBB, 16));
    }

    public static Format21ih.Instance read_21ih(Format21ih format, RandomInput in, int AA) {
        int BBBB = in.readUnsignedShort();
        return format.make(AA, BBBB << 16);
    }

    public static Format21lh.Instance read_21lh(Format21lh format, RandomInput in, int AA) {
        long BBBB = in.readUnsignedShort();
        return format.make(AA, BBBB << 48);
    }

    public static Format21c.Instance read_21c(
            Format21c format, RandomInput in, ReadContext context, int AA) {
        int BBBB = in.readUnsignedShort();
        return format.make(AA, format.referenceType.indexToRef(context, BBBB));
    }

    public static Format22c.Instance read_22c(
            Format22c format, RandomInput in, ReadContext context, int BA) {
        int CCCC = in.readUnsignedShort();
        return format.make(BA & 0xf, BA >> 4, format.referenceType.indexToRef(context, CCCC));
    }

    public static Format23x.Instance read_23x(Format23x format, RandomInput in, int AA) {
        int CCBB = in.readUnsignedShort();
        return format.make(AA, CCBB & 0xff, CCBB >> 8);
    }

    public static Format22b.Instance read_22b(Format22b format, RandomInput in, int AA) {
        int CCBB = in.readUnsignedShort();
        return format.make(AA, CCBB & 0xff, extend_sign32(CCBB >> 8, 8));
    }

    public static Format22t22s.Instance read_22t_22s(Format22t22s format, RandomInput in, int BA) {
        int CCCC = in.readUnsignedShort();
        return format.make(BA & 0xf, BA >> 4, extend_sign32(CCCC, 16));
    }

    public static Format30t.Instance read_30t(Format30t format, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAAlo = in.readUnsignedShort();
        int AAAAhi = in.readUnsignedShort();
        return format.make(AAAAlo | (AAAAhi << 16));
    }

    public static Format32x.Instance read_32x(Format32x format, RandomInput in, int _00) {
        check_zero_arg(_00);
        int AAAA = in.readUnsignedShort();
        int BBBB = in.readUnsignedShort();
        return format.make(AAAA, BBBB);
    }

    public static Format31i31t.Instance read_31i_31t(Format31i31t format, RandomInput in, int AA) {
        int BBBBlo = in.readUnsignedShort();
        int BBBBhi = in.readUnsignedShort();
        return format.make(AA, BBBBlo | (BBBBhi << 16));
    }

    public static Format31c.Instance read_31c(
            Format31c format, RandomInput in, ReadContext context, int AA) {
        int BBBBlo = in.readUnsignedShort();
        int BBBBhi = in.readUnsignedShort();
        int BBBBBBBB = BBBBlo | (BBBBhi << 16);
        return format.make(AA, format.referenceType.indexToRef(context, BBBBBBBB));
    }

    public static Format35c35ms35mi.Instance read_35c_35ms_35mi(
            Format35c35ms35mi format, RandomInput in, ReadContext context, int AG) {
        int A = AG >> 4;
        int G = AG & 0xf;
        int BBBB = in.readUnsignedShort();
        int FEDC = in.readUnsignedShort();
        int F = FEDC >> 12;
        int E = (FEDC >> 8) & 0xf;
        int D = (FEDC >> 4) & 0xf;
        int C = FEDC & 0xf;
        return format.make(A, format.referenceType.indexToRef(context, BBBB), C, D, E, F, G);
    }

    public static Format3rc3rms3rmi.Instance read_3rc_3rms_3rmi(
            Format3rc3rms3rmi format, RandomInput in, ReadContext context, int AA) {
        int BBBB = in.readUnsignedShort();
        int CCCC = in.readUnsignedShort();
        return format.make(AA, format.referenceType.indexToRef(context, BBBB), CCCC);
    }

    public static Format45cc.Instance read_45cc(
            Format45cc format, RandomInput in, ReadContext context, int AG) {
        int A = AG >> 4;
        int G = AG & 0xf;
        int BBBB = in.readUnsignedShort();
        int FEDC = in.readUnsignedShort();
        int F = FEDC >> 12;
        int E = (FEDC >> 8) & 0xf;
        int D = (FEDC >> 4) & 0xf;
        int C = FEDC & 0xf;
        int HHHH = in.readUnsignedShort();
        return format.make(A, format.referenceType.indexToRef(context, BBBB),
                C, D, E, F, G, format.referenceType2.indexToRef(context, HHHH));
    }

    public static Format4rcc.Instance read_4rcc(
            Format4rcc format, RandomInput in, ReadContext context, int AA) {
        int BBBB = in.readUnsignedShort();
        int CCCC = in.readUnsignedShort();
        int HHHH = in.readUnsignedShort();
        return format.make(AA, format.referenceType.indexToRef(context, BBBB),
                CCCC, format.referenceType2.indexToRef(context, HHHH));
    }

    public static Format51l.Instance read_51l(Format51l format, RandomInput in, int AA) {
        long BBBBlolo = in.readUnsignedShort();
        long BBBBhilo = in.readUnsignedShort();
        long BBBBlohi = in.readUnsignedShort();
        long BBBBhihi = in.readUnsignedShort();
        return format.make(AA, (BBBBhihi << 48) | (BBBBlohi << 32)
                | (BBBBhilo << 16) | BBBBlolo);
    }

    public static PackedSwitchPayload.Instance read_packed_switch_payload(
            PackedSwitchPayload format, RandomInput in, int _00) {
        check_zero_arg(_00);
        in.addPosition(-2); // code unit
        in.requireAlignment(PAYLOAD_ALIGNMENT);
        in.addPosition(2);

        int size = in.readUnsignedShort();
        int first_key = in.readInt();
        int[] targets = in.readIntArray(size);
        return format.make(first_key, targets);
    }

    public static SparseSwitchPayload.Instance read_sparse_switch_payload(
            SparseSwitchPayload format, RandomInput in, int _00) {
        check_zero_arg(_00);
        in.addPosition(-2); // code unit
        in.requireAlignment(PAYLOAD_ALIGNMENT);
        in.addPosition(2);

        int size = in.readUnsignedShort();
        int[] keys = in.readIntArray(size);
        int[] targets = in.readIntArray(size);
        return format.make(keys, targets);
    }

    public static ArrayPayload.Instance read_array_payload(
            ArrayPayload format, RandomInput in, int _00) {
        check_zero_arg(_00);
        in.addPosition(-2); // code unit
        in.requireAlignment(PAYLOAD_ALIGNMENT);
        in.addPosition(2);

        int element_width = in.readUnsignedShort();
        if (!(element_width == 1 || element_width == 2
                || element_width == 4 || element_width == 8)) {
            throw new IllegalStateException("unsupported element_width: " + element_width);
        }
        int size = in.readInt();
        if (size < 0) {
            throw new IllegalStateException("negative size: " + size);
        }
        byte[] data = in.readByteArray(size * element_width);
        if ((data.length & 1) != 0) {
            in.readByte(); // padding
        }
        return format.make(element_width, data);
    }
}
