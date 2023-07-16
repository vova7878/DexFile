package com.v7878.dex.bytecode;

import com.v7878.dex.DataCollector;
import com.v7878.dex.ReadContext;
import com.v7878.dex.WriteContext;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Arrays;
import java.util.Objects;

public abstract class Format {
    public static final int PAYLOAD_ALIGNMENT = 4;

    protected final Opcode opcode;
    private final int units;
    private final boolean payload;

    Format(Opcode opcode, int units) {
        this(opcode, units, false);
    }

    Format(Opcode opcode, int units, boolean payload) {
        this.opcode = opcode;
        this.units = units;
        this.payload = payload;
    }

    public Opcode opcode() {
        return opcode;
    }

    public int units() {
        return units;
    }

    public boolean isPayload() {
        return payload;
    }

    public abstract Instruction read(RandomInput in, ReadContext context, int arg);

    private static int extend_sign(int value, int width) {
        int shift = 32 - width;
        return (value << shift) >> shift;
    }

    private static long extend_sign64(long value, int width) {
        int shift = 64 - width;
        return (value << shift) >> shift;
    }

    public static class Format10x extends Format {

        public class Instance extends Instruction {

            Instance() {
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_10x(out, opcode().opcodeValue(context.getOptions()));
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname();
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode());
            }

            @Override
            public Instruction clone() {
                return new Instance();
            }
        }

        Format10x(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            return make();
        }

        public Instruction make() {
            return new Instance();
        }
    }

    public static class Format12x extends Format {

        public class Instance extends Instruction {

            public final int A, B;

            Instance(int A, int B) {
                this.A = A;
                this.B = B;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_12x(out, opcode().opcodeValue(context.getOptions()), A, B);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + A + " " + B;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && A == iobj.A && B == iobj.B;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, B);
            }

            @Override
            public Instruction clone() {
                return new Instance(A, B);
            }
        }

        Format12x(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int BA) {
            return make(BA & 0xf, BA >> 4);
        }

        public Instruction make(int A, int B) {
            return new Instance(A, B);
        }
    }

    public static class Format11n extends Format {

        public class Instance extends Instruction {

            public final int A, sB;

            Instance(int A, int sB) {
                this.A = A;
                this.sB = sB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_11n(out, opcode().opcodeValue(context.getOptions()), A, sB);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + A + " " + sB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && A == iobj.A && sB == iobj.sB;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, sB);
            }

            @Override
            public Instruction clone() {
                return new Instance(A, sB);
            }
        }

        Format11n(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int BA) {
            return make(BA & 0xf, extend_sign(BA >> 4, 4));
        }

        public Instruction make(int A, int sB) {
            return new Instance(A, sB);
        }
    }

    public static class Format11x extends Format {

        public class Instance extends Instruction {

            public final int AA;

            Instance(int AA) {
                this.AA = AA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_11x(out, opcode().opcodeValue(context.getOptions()), AA);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA);
            }
        }

        Format11x(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            return make(AA);
        }

        public Instruction make(int AA) {
            return new Instance(AA);
        }
    }

    public static class Format10t extends Format {

        public class Instance extends Instruction {

            public final int sAA;

            Instance(int sAA) {
                this.sAA = sAA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_10t(out, opcode().opcodeValue(context.getOptions()), sAA);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + sAA;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && sAA == iobj.sAA;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), sAA);
            }

            @Override
            public Instruction clone() {
                return new Instance(sAA);
            }
        }

        Format10t(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            return make(extend_sign(AA, 8));
        }

        public Instruction make(int sAA) {
            return new Instance(sAA);
        }
    }

    public static class Format20t extends Format {

        public class Instance extends Instruction {

            public final int sAAAA;

            Instance(int sAAAA) {
                this.sAAAA = sAAAA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_20t(out,
                        opcode().opcodeValue(context.getOptions()), sAAAA);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + sAAAA;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && sAAAA == iobj.sAAAA;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), sAAAA);
            }

            @Override
            public Instruction clone() {
                return new Instance(sAAAA);
            }
        }

        Format20t(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            int AAAA = in.readUnsignedShort();
            return make(extend_sign(AAAA, 16));
        }

        public Instruction make(int sAAAA) {
            return new Instance(sAAAA);
        }
    }

    public static class Format22x extends Format {

        public class Instance extends Instruction {

            public final int AA, BBBB;

            Instance(int AA, int BBBB) {
                this.AA = AA;
                this.BBBB = BBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22x_21c(out,
                        opcode().opcodeValue(context.getOptions()), AA, BBBB);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + BBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && BBBB == iobj.BBBB;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, BBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, BBBB);
            }
        }

        Format22x(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBB = in.readUnsignedShort();
            return make(AA, BBBB);
        }

        public Instruction make(int AA, int BBBB) {
            return new Instance(AA, BBBB);
        }
    }

    public static class Format21t21s extends Format {

        public class Instance extends Instruction {

            public final int AA, sBBBB;

            Instance(int AA, int sBBBB) {
                this.AA = AA;
                this.sBBBB = sBBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_21t_21s(out,
                        opcode().opcodeValue(context.getOptions()), AA, sBBBB);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + sBBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && sBBBB == iobj.sBBBB;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, sBBBB);
            }
        }

        Format21t21s(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBB = in.readUnsignedShort();
            return make(AA, extend_sign(BBBB, 16));
        }

        public Instruction make(int AA, int sBBBB) {
            return new Instance(AA, sBBBB);
        }
    }

    public static class Format21ih extends Format {

        public class Instance extends Instruction {

            public final int AA, sBBBB0000;

            Instance(int AA, int sBBBB0000) {
                this.AA = AA;
                this.sBBBB0000 = sBBBB0000;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_21ih(out,
                        opcode().opcodeValue(context.getOptions()), AA, sBBBB0000);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + sBBBB0000;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && sBBBB0000 == iobj.sBBBB0000;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBB0000);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, sBBBB0000);
            }
        }

        Format21ih(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBB = in.readUnsignedShort();
            return make(AA, BBBB << 16);
        }

        public Instruction make(int AA, int sBBBB0000) {
            return new Instance(AA, sBBBB0000);
        }
    }

    public static class Format21lh extends Format {

        public class Instance extends Instruction {

            public final int AA;
            public final long sBBBB000000000000;

            Instance(int AA, long sBBBB000000000000) {
                this.AA = AA;
                this.sBBBB000000000000 = sBBBB000000000000;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_21lh(out,
                        opcode().opcodeValue(context.getOptions()), AA, sBBBB000000000000);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + sBBBB000000000000;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                            && sBBBB000000000000 == iobj.sBBBB000000000000;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBB000000000000);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, sBBBB000000000000);
            }
        }

        Format21lh(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            long BBBB = in.readUnsignedShort();
            return make(AA, BBBB << 48);
        }

        public Instruction make(int AA, long sBBBB000000000000) {
            return new Instance(AA, sBBBB000000000000);
        }
    }

    public static class Format21c extends Format {
        public final ReferenceType referenceType;

        public class Instance extends Instruction {

            public final int AA;
            public final Object cBBBB;

            Instance(int AA, Object cBBBB) {
                this.AA = AA;
                this.cBBBB = referenceType.clone(cBBBB);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22x_21c(out,
                        opcode().opcodeValue(context.getOptions()), AA,
                        referenceType.refToIndex(context, cBBBB));
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + cBBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && Objects.equals(cBBBB, iobj.cBBBB);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, referenceType.clone(cBBBB));
            }
        }

        Format21c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 2);
            this.referenceType = referenceType;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBB = in.readUnsignedShort();
            return make(AA, referenceType.indexToRef(context, BBBB));
        }

        public Instruction make(int AA, Object cBBBB) {
            return new Instance(AA, cBBBB);
        }
    }

    public static class Format22c extends Format {

        public final ReferenceType referenceType;

        public class Instance extends Instruction {

            public final int A, B;
            public final Object cCCCC;

            Instance(int A, int B, Object cCCCC) {
                this.A = A;
                this.B = B;
                this.cCCCC = referenceType.clone(cCCCC);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cCCCC);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22c(out,
                        opcode().opcodeValue(context.getOptions()), A, B,
                        referenceType.refToIndex(context, cCCCC));
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + A + " " + B + " " + cCCCC;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && A == iobj.A && B == iobj.B
                            && Objects.equals(cCCCC, iobj.cCCCC);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, B, cCCCC);
            }

            @Override
            public Instruction clone() {
                return new Instance(A, B, cCCCC);
            }
        }

        Format22c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 2);
            this.referenceType = referenceType;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int BA) {
            int CCCC = in.readUnsignedShort();
            return make(BA & 0xf, BA >> 4, referenceType.indexToRef(context, CCCC));
        }

        public Instruction make(int A, int B, Object cCCCC) {
            return new Instance(A, B, cCCCC);
        }
    }

    public static class Format23x extends Format {

        public class Instance extends Instruction {

            public final int AA, BB, CC;

            Instance(int AA, int BB, int CC) {
                this.AA = AA;
                this.BB = BB;
                this.CC = CC;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_23x(out,
                        opcode().opcodeValue(context.getOptions()), AA, BB, CC);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + BB + " " + CC;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && BB == iobj.BB && CC == iobj.CC;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, BB, CC);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, BB, CC);
            }
        }

        Format23x(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int CCBB = in.readUnsignedShort();
            return make(AA, CCBB & 0xff, CCBB >> 8);
        }

        public Instruction make(int AA, int BB, int CC) {
            return new Instance(AA, BB, CC);
        }
    }

    public static class Format22b extends Format {

        public class Instance extends Instruction {

            public final int AA, BB, sCC;

            Instance(int AA, int BB, int sCC) {
                this.AA = AA;
                this.BB = BB;
                this.sCC = sCC;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22b(out,
                        opcode().opcodeValue(context.getOptions()), AA, BB, sCC);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + BB + " " + sCC;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && BB == iobj.BB && sCC == iobj.sCC;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, BB, sCC);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, BB, sCC);
            }
        }

        Format22b(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int CCBB = in.readUnsignedShort();
            return make(AA, CCBB & 0xff, extend_sign(CCBB >> 8, 8));
        }

        public Instruction make(int AA, int BB, int sCC) {
            return new Instance(AA, BB, sCC);
        }
    }

    public static class Format22t22s extends Format {

        public class Instance extends Instruction {

            public final int A, B, sCCCC;

            Instance(int A, int B, int sCCCC) {
                this.A = A;
                this.B = B;
                this.sCCCC = sCCCC;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22t_22s(out,
                        opcode().opcodeValue(context.getOptions()), A, B, sCCCC);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + A + " " + B + " " + sCCCC;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && A == iobj.A && B == iobj.B && sCCCC == iobj.sCCCC;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, B, sCCCC);
            }

            @Override
            public Instruction clone() {
                return new Instance(A, B, sCCCC);
            }
        }

        Format22t22s(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int BA) {
            int CCCC = in.readUnsignedShort();
            return make(BA & 0xf, BA >> 4, extend_sign(CCCC, 16));
        }

        public Instruction make(int A, int B, int sCCCC) {
            return new Instance(A, B, sCCCC);
        }
    }

    public static class Format30t extends Format {

        public class Instance extends Instruction {

            public final int sAAAAAAAA;

            Instance(int sAAAAAAAA) {
                this.sAAAAAAAA = sAAAAAAAA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_30t(out,
                        opcode().opcodeValue(context.getOptions()), sAAAAAAAA);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + sAAAAAAAA;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && sAAAAAAAA == iobj.sAAAAAAAA;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), sAAAAAAAA);
            }

            @Override
            public Instruction clone() {
                return new Instance(sAAAAAAAA);
            }
        }

        Format30t(Opcode opcode) {
            super(opcode, 3);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            int AAAAlo = in.readUnsignedShort();
            int AAAAhi = in.readUnsignedShort();
            return make(AAAAlo | (AAAAhi << 16));
        }

        public Instruction make(int sAAAAAAAA) {
            return new Instance(sAAAAAAAA);
        }
    }

    public static class Format32x extends Format {

        public class Instance extends Instruction {

            public final int AAAA, BBBB;

            Instance(int AAAA, int BBBB) {
                this.AAAA = AAAA;
                this.BBBB = BBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_32x(out,
                        opcode().opcodeValue(context.getOptions()), AAAA, BBBB);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AAAA + " " + BBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AAAA == iobj.AAAA && BBBB == iobj.BBBB;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AAAA, BBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AAAA, BBBB);
            }
        }

        Format32x(Opcode opcode) {
            super(opcode, 3);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            int AAAA = in.readUnsignedShort();
            int BBBB = in.readUnsignedShort();
            return make(AAAA, BBBB);
        }

        public Instruction make(int AAAA, int BBBB) {
            return new Instance(AAAA, BBBB);
        }
    }

    public static class Format31i31t extends Format {

        public class Instance extends Instruction {

            public final int AA, sBBBBBBBB;

            Instance(int AA, int sBBBBBBBB) {
                this.AA = AA;
                this.sBBBBBBBB = sBBBBBBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_31i_31t_31c(out,
                        opcode().opcodeValue(context.getOptions()), AA, sBBBBBBBB);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + sBBBBBBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && sBBBBBBBB == iobj.sBBBBBBBB;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBBBBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, sBBBBBBBB);
            }
        }

        Format31i31t(Opcode opcode) {
            super(opcode, 3);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBBlo = in.readUnsignedShort();
            int BBBBhi = in.readUnsignedShort();
            return make(AA, BBBBlo | (BBBBhi << 16));
        }

        public Instruction make(int AA, int sBBBBBBBB) {
            return new Instance(AA, sBBBBBBBB);
        }
    }

    public static class Format31c extends Format {

        public final ReferenceType referenceType;

        public class Instance extends Instruction {

            public final int AA;
            public final Object cBBBBBBBB;

            Instance(int AA, Object cBBBBBBBB) {
                this.AA = AA;
                this.cBBBBBBBB = referenceType.clone(cBBBBBBBB);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBBBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_31i_31t_31c(out,
                        opcode().opcodeValue(context.getOptions()), AA,
                        referenceType.refToIndex(context, cBBBBBBBB));
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + cBBBBBBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                            && Objects.equals(cBBBBBBBB, iobj.cBBBBBBBB);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBBBBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, cBBBBBBBB);
            }
        }


        Format31c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 3);
            this.referenceType = referenceType;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBBlo = in.readUnsignedShort();
            int BBBBhi = in.readUnsignedShort();
            int BBBBBBBB = BBBBlo | (BBBBhi << 16);
            return make(AA, referenceType.indexToRef(context, BBBBBBBB));
        }

        public Instruction make(int AA, Object cBBBBBBBB) {
            return new Instance(AA, cBBBBBBBB);
        }
    }

    public static class Format35c extends Format {

        public final ReferenceType referenceType;

        public class Instance extends Instruction {

            public final int A, C, D, E, F, G;
            public final Object cBBBB;

            Instance(int A, Object cBBBB, int C, int D, int E, int F, int G) {
                this.A = A;
                this.cBBBB = referenceType.clone(cBBBB);
                this.C = C;
                this.D = D;
                this.E = E;
                this.F = F;
                this.G = G;
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_35c_35ms_35mi(out,
                        opcode().opcodeValue(context.getOptions()), A,
                        referenceType.refToIndex(context, cBBBB), C, D, E, F, G);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + A + " " + cBBBB
                        + " " + C + " " + D + " " + E + " " + F + " " + G;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && A == iobj.A && Objects.equals(cBBBB, iobj.cBBBB)
                            && C == iobj.C && D == iobj.D && E == iobj.E
                            && F == iobj.F && G == iobj.G;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, cBBBB, C, D, E, F, G);
            }

            @Override
            public Instruction clone() {
                return new Instance(A, cBBBB, C, D, E, F, G);
            }
        }

        Format35c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 3);
            this.referenceType = referenceType;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AG) {
            int A = AG >> 4;
            int G = AG & 0xf;
            int BBBB = in.readUnsignedShort();
            int FEDC = in.readUnsignedShort();
            int F = FEDC >> 12;
            int E = (FEDC >> 8) & 0xf;
            int D = (FEDC >> 4) & 0xf;
            int C = FEDC & 0xf;
            return make(A, referenceType.indexToRef(context, BBBB), C, D, E, F, G);
        }

        public Instruction make(int A, Object cBBBB, int C, int D, int E, int F, int G) {
            return new Instance(A, cBBBB, C, D, E, F, G);
        }
    }

    public static class Format3rc extends Format {

        public final ReferenceType referenceType;

        public class Instance extends Instruction {

            public final int AA;
            public final Object cBBBB;
            public final int CCCC;

            Instance(int AA, Object cBBBB, int CCCC) {
                this.AA = AA;
                this.cBBBB = referenceType.clone(cBBBB);
                this.CCCC = CCCC;
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_3rc_3rms_3rmi(out,
                        opcode().opcodeValue(context.getOptions()), AA,
                        referenceType.refToIndex(context, cBBBB), CCCC);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + cBBBB + " " + CCCC;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                            && Objects.equals(cBBBB, iobj.cBBBB) && CCCC == iobj.CCCC;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBB, CCCC);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, cBBBB, CCCC);
            }
        }

        Format3rc(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 3);
            this.referenceType = referenceType;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBB = in.readUnsignedShort();
            int CCCC = in.readUnsignedShort();
            return make(AA, referenceType.indexToRef(context, BBBB), CCCC);
        }

        public Instruction make(int AA, Object cBBBB, int CCCC) {
            return new Instance(AA, cBBBB, CCCC);
        }
    }

    public static class Format45cc extends Format {

        public final ReferenceType referenceType;
        public final ReferenceType referenceType2;

        public class Instance extends Instruction {

            public final int A, C, D, E, F, G;
            public final Object cBBBB, cHHHH;

            Instance(int A, Object cBBBB, int C, int D, int E, int F, int G, Object cHHHH) {
                this.A = A;
                this.cBBBB = referenceType.clone(cBBBB);
                this.C = C;
                this.D = D;
                this.E = E;
                this.F = F;
                this.G = G;
                this.cHHHH = referenceType2.clone(cHHHH);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
                referenceType2.collectData(data, cHHHH);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_45cc(out,
                        opcode().opcodeValue(context.getOptions()), A,
                        referenceType.refToIndex(context, cBBBB), C, D, E, F, G,
                        referenceType2.refToIndex(context, cHHHH));
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + A + " " + cBBBB + " " + C
                        + " " + D + " " + E + " " + F + " " + G + " " + cHHHH;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && A == iobj.A && Objects.equals(cBBBB, iobj.cBBBB)
                            && C == iobj.C && D == iobj.D && E == iobj.E
                            && F == iobj.F && G == iobj.G && Objects.equals(cHHHH, iobj.cHHHH);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, cBBBB, C, D, E, F, G, cHHHH);
            }

            @Override
            public Instruction clone() {
                return new Instance(A, cBBBB, C, D, E, F, G, cHHHH);
            }
        }

        Format45cc(Opcode opcode, ReferenceType referenceType, ReferenceType referenceType2) {
            super(opcode, 4);
            this.referenceType = referenceType;
            this.referenceType2 = referenceType2;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AG) {
            int A = AG >> 4;
            int G = AG & 0xf;
            int BBBB = in.readUnsignedShort();
            int FEDC = in.readUnsignedShort();
            int F = FEDC >> 12;
            int E = (FEDC >> 8) & 0xf;
            int D = (FEDC >> 4) & 0xf;
            int C = FEDC & 0xf;
            int HHHH = in.readUnsignedShort();
            return make(A, referenceType.indexToRef(context, BBBB), C, D,
                    E, F, G, referenceType2.indexToRef(context, HHHH));
        }

        public Instruction make(int A, Object cBBBB, int C, int D, int E, int F, int G, Object cHHHH) {
            return new Instance(A, cBBBB, C, D, E, F, G, cHHHH);
        }
    }

    public static class Format4rcc extends Format {

        public final ReferenceType referenceType;
        public final ReferenceType referenceType2;

        public class Instance extends Instruction {

            public final int AA;
            public final Object cBBBB;
            public final int CCCC;
            public final Object cHHHH;

            Instance(int AA, Object cBBBB, int CCCC, Object cHHHH) {
                this.AA = AA;
                this.cBBBB = referenceType.clone(cBBBB);
                this.CCCC = CCCC;
                this.cHHHH = referenceType2.clone(cHHHH);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
                referenceType2.collectData(data, cHHHH);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_4rcc(out,
                        opcode().opcodeValue(context.getOptions()), AA,
                        referenceType.refToIndex(context, cBBBB), CCCC,
                        referenceType2.refToIndex(context, cHHHH));
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + cBBBB + " " + CCCC + " " + cHHHH;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                            && Objects.equals(cBBBB, iobj.cBBBB) && CCCC == iobj.CCCC
                            && Objects.equals(cHHHH, iobj.cHHHH);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBB, CCCC, cHHHH);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, cBBBB, CCCC, cHHHH);
            }
        }

        Format4rcc(Opcode opcode, ReferenceType referenceType, ReferenceType referenceType2) {
            super(opcode, 4);
            this.referenceType = referenceType;
            this.referenceType2 = referenceType2;
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            int BBBB = in.readUnsignedShort();
            int CCCC = in.readUnsignedShort();
            int HHHH = in.readUnsignedShort();
            return make(AA, referenceType.indexToRef(context, BBBB),
                    CCCC, referenceType2.indexToRef(context, HHHH));
        }

        public Instruction make(int AA, Object cBBBB, int CCCC, Object cHHHH) {
            return new Instance(AA, cBBBB, CCCC, cHHHH);
        }
    }

    public static class Format51l extends Format {

        public class Instance extends Instruction {

            public final int AA;
            public final long sBBBBBBBBBBBBBBBB;

            Instance(int AA, long sBBBBBBBBBBBBBBBB) {
                this.AA = AA;
                this.sBBBBBBBBBBBBBBBB = sBBBBBBBBBBBBBBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_51l(out,
                        opcode().opcodeValue(context.getOptions()), AA, sBBBBBBBBBBBBBBBB);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + AA + " " + sBBBBBBBBBBBBBBBB;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && AA == iobj.AA && sBBBBBBBBBBBBBBBB == iobj.sBBBBBBBBBBBBBBBB;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBBBBBBBBBBBBBB);
            }

            @Override
            public Instruction clone() {
                return new Instance(AA, sBBBBBBBBBBBBBBBB);
            }
        }

        Format51l(Opcode opcode) {
            super(opcode, 5);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int AA) {
            long BBBBlolo = in.readUnsignedShort();
            long BBBBhilo = in.readUnsignedShort();
            long BBBBlohi = in.readUnsignedShort();
            long BBBBhihi = in.readUnsignedShort();
            return make(AA, (BBBBhihi << 48) | (BBBBlohi << 32)
                    | (BBBBhilo << 16) | BBBBlolo);
        }

        public Instruction make(int AA, long sBBBBBBBBBBBBBBBB) {
            return new Instance(AA, sBBBBBBBBBBBBBBBB);
        }
    }

    public static class PackedSwitchPayload extends Format {

        public class Instance extends Instruction {

            public final int first_key;
            public final int[] targets;

            Instance(int first_key, int[] targets) {
                this.first_key = first_key;
                this.targets = targets.clone();
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.packed_switch_payload(out,
                        opcode().opcodeValue(context.getOptions()),
                        first_key, targets);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public int units() {
                return targets.length * 2 + 4;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + first_key + " " + Arrays.toString(targets);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && first_key == iobj.first_key
                            && Arrays.equals(targets, iobj.targets);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), first_key, Arrays.hashCode(targets));
            }

            @Override
            public Instruction clone() {
                return new Instance(first_key, targets);
            }
        }

        PackedSwitchPayload(Opcode opcode) {
            super(opcode, -1, true);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            in.addPosition(-2); // code unit
            in.requireAlignment(PAYLOAD_ALIGNMENT);
            in.addPosition(2);

            int size = in.readUnsignedShort();
            int first_key = in.readInt();
            int[] targets = in.readIntArray(size);
            return make(first_key, targets);
        }

        public Instruction make(int first_key, int[] targets) {
            return new Instance(first_key, targets);
        }
    }

    public static class SparseSwitchPayload extends Format {

        public class Instance extends Instruction {

            public final int[] keys;
            public final int[] targets;

            Instance(int[] keys, int[] targets) {
                this.keys = keys.clone();
                this.targets = targets.clone();
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.sparse_switch_payload(out,
                        opcode().opcodeValue(context.getOptions()),
                        keys, targets);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public int units() {
                return keys.length * 4 + 2;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + Arrays.toString(keys) + " " + Arrays.toString(targets);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && Arrays.equals(keys, iobj.keys)
                            && Arrays.equals(targets, iobj.targets);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), Arrays.hashCode(keys), Arrays.hashCode(targets));
            }

            @Override
            public Instruction clone() {
                return new Instance(keys, targets);
            }
        }

        SparseSwitchPayload(Opcode opcode) {
            super(opcode, -1, true);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            in.addPosition(-2); // code unit
            in.requireAlignment(PAYLOAD_ALIGNMENT);
            in.addPosition(2);

            int size = in.readUnsignedShort();
            int[] keys = in.readIntArray(size);
            int[] targets = in.readIntArray(size);
            return make(keys, targets);
        }

        public Instruction make(int[] keys, int[] targets) {
            return new Instance(keys, targets);
        }
    }

    public static class ArrayPayload extends Format {

        public class Instance extends Instruction {

            public final int element_width;
            public final byte[] data;

            Instance(int element_width, byte[] data) {
                this.element_width = element_width;
                this.data = data.clone();
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_array_payload(out,
                        opcode().opcodeValue(context.getOptions()),
                        element_width, data);
            }

            @Override
            public Opcode opcode() {
                return opcode;
            }

            @Override
            public int units() {
                return (data.length + 1) / 2 + 4;
            }

            @Override
            public String toString() {
                return opcode().opname() + " " + element_width + " " + Arrays.toString(data);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Instance) {
                    Instance iobj = (Instance) obj;
                    return Objects.equals(opcode(), iobj.opcode())
                            && element_width == iobj.element_width
                            && Arrays.equals(data, iobj.data);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), element_width, Arrays.hashCode(data));
            }

            @Override
            public Instruction clone() {
                return new Instance(element_width, data);
            }
        }

        ArrayPayload(Opcode opcode) {
            super(opcode, -1, true);
        }

        @Override
        public Instruction read(RandomInput in, ReadContext context, int _00) {
            in.addPosition(-2); // code unit
            in.requireAlignment(PAYLOAD_ALIGNMENT);
            in.addPosition(2);

            int element_width = in.readUnsignedShort();
            if (!(element_width == 1 || element_width == 2 || element_width == 4 || element_width == 8)) {
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
            return make(element_width, data);
        }

        public Instruction make(int element_width, byte[] data) {
            return new Instance(element_width, data);
        }
    }
}
