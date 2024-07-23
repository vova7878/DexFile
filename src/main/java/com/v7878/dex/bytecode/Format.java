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

    public static final class Format10x extends Format {

        public final class Instance extends Instruction {

            Instance() {
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_10x(out, opcode().opcodeValue(context));
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode());
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode());
            }

            @Override
            public Instance mutate() {
                return new Instance();
            }
        }

        Format10x(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_10x(this, _00);
        }

        public Instance make() {
            return new Instance();
        }
    }

    public static final class Format12x extends Format {

        public final class Instance extends Instruction {

            public final int A, B;

            Instance(int A, int B) {
                this.A = A;
                this.B = B;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_12x(out, opcode().opcodeValue(context), A, B);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && A == iobj.A && B == iobj.B;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, B);
            }

            @Override
            public Instance mutate() {
                return new Instance(A, B);
            }
        }

        Format12x(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int BA) {
            return InstructionReader.read_12x(this, BA);
        }

        public Instance make(int A, int B) {
            return new Instance(A, B);
        }
    }

    public static final class Format11n extends Format {

        public final class Instance extends Instruction {

            public final int A, sB;

            Instance(int A, int sB) {
                this.A = A;
                this.sB = sB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_11n(out, opcode().opcodeValue(context), A, sB);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && A == iobj.A && sB == iobj.sB;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, sB);
            }

            @Override
            public Instance mutate() {
                return new Instance(A, sB);
            }
        }

        Format11n(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int BA) {
            return InstructionReader.read_11n(this, BA);
        }

        public Instance make(int A, int sB) {
            return new Instance(A, sB);
        }
    }

    public static final class Format11x extends Format {

        public final class Instance extends Instruction {

            public final int AA;

            Instance(int AA) {
                this.AA = AA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_11x(out, opcode().opcodeValue(context), AA);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA);
            }
        }

        Format11x(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_11x(this, AA);
        }

        public Instance make(int AA) {
            return new Instance(AA);
        }
    }

    public static final class Format10t extends Format {

        public final class Instance extends Instruction {

            public final int sAA;

            Instance(int sAA) {
                this.sAA = sAA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_10t(out, opcode().opcodeValue(context), sAA);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && sAA == iobj.sAA;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), sAA);
            }

            @Override
            public Instance mutate() {
                return new Instance(sAA);
            }
        }

        Format10t(Opcode opcode) {
            super(opcode, 1);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_10t(this, AA);
        }

        public Instance make(int sAA) {
            return new Instance(sAA);
        }
    }

    public static final class Format20t extends Format {

        public final class Instance extends Instruction {

            public final int sAAAA;

            Instance(int sAAAA) {
                this.sAAAA = sAAAA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_20t(out,
                        opcode().opcodeValue(context), sAAAA);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && sAAAA == iobj.sAAAA;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), sAAAA);
            }

            @Override
            public Instance mutate() {
                return new Instance(sAAAA);
            }
        }

        Format20t(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_20t(this, in, _00);
        }

        public Instance make(int sAAAA) {
            return new Instance(sAAAA);
        }
    }

    public static final class Format22x extends Format {

        public final class Instance extends Instruction {

            public final int AA, BBBB;

            Instance(int AA, int BBBB) {
                this.AA = AA;
                this.BBBB = BBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22x_21c(out,
                        opcode().opcodeValue(context), AA, BBBB);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && BBBB == iobj.BBBB;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, BBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, BBBB);
            }
        }

        Format22x(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_22x(this, in, AA);
        }

        public Instance make(int AA, int BBBB) {
            return new Instance(AA, BBBB);
        }
    }

    public static final class Format21t21s extends Format {

        public final class Instance extends Instruction {

            public final int AA, sBBBB;

            Instance(int AA, int sBBBB) {
                this.AA = AA;
                this.sBBBB = sBBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_21t_21s(out,
                        opcode().opcodeValue(context), AA, sBBBB);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && sBBBB == iobj.sBBBB;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, sBBBB);
            }
        }

        Format21t21s(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_21t_21s(this, in, AA);
        }

        public Instance make(int AA, int sBBBB) {
            return new Instance(AA, sBBBB);
        }
    }

    public static final class Format21ih extends Format {

        public final class Instance extends Instruction {

            public final int AA, sBBBB0000;

            Instance(int AA, int sBBBB0000) {
                this.AA = AA;
                this.sBBBB0000 = sBBBB0000;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_21ih(out,
                        opcode().opcodeValue(context), AA, sBBBB0000);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && sBBBB0000 == iobj.sBBBB0000;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBB0000);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, sBBBB0000);
            }
        }

        Format21ih(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_21ih(this, in, AA);
        }

        public Instance make(int AA, int sBBBB0000) {
            return new Instance(AA, sBBBB0000);
        }
    }

    public static final class Format21lh extends Format {

        public final class Instance extends Instruction {

            public final int AA;
            public final long sBBBB000000000000;

            Instance(int AA, long sBBBB000000000000) {
                this.AA = AA;
                this.sBBBB000000000000 = sBBBB000000000000;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_21lh(out,
                        opcode().opcodeValue(context), AA, sBBBB000000000000);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                        && sBBBB000000000000 == iobj.sBBBB000000000000;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBB000000000000);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, sBBBB000000000000);
            }
        }

        Format21lh(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_21lh(this, in, AA);
        }

        public Instance make(int AA, long sBBBB000000000000) {
            return new Instance(AA, sBBBB000000000000);
        }
    }

    public static final class Format21c extends Format {
        public final ReferenceType referenceType;

        public final class Instance extends Instruction {

            public final int AA;
            public final Object cBBBB;

            Instance(int AA, Object cBBBB) {
                this.AA = AA;
                this.cBBBB = referenceType.mutate(cBBBB);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22x_21c(out,
                        opcode().opcodeValue(context), AA,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && Objects.equals(cBBBB, iobj.cBBBB);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, referenceType.mutate(cBBBB));
            }
        }

        Format21c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 2);
            this.referenceType = referenceType;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_21c(this, in, context, AA);
        }

        public Instance make(int AA, Object cBBBB) {
            return new Instance(AA, cBBBB);
        }
    }

    public static final class Format22c extends Format {

        public final ReferenceType referenceType;

        public final class Instance extends Instruction {

            public final int A, B;
            public final Object cCCCC;

            Instance(int A, int B, Object cCCCC) {
                this.A = A;
                this.B = B;
                this.cCCCC = referenceType.mutate(cCCCC);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cCCCC);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22c(out,
                        opcode().opcodeValue(context), A, B,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && A == iobj.A && B == iobj.B
                        && Objects.equals(cCCCC, iobj.cCCCC);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, B, cCCCC);
            }

            @Override
            public Instance mutate() {
                return new Instance(A, B, cCCCC);
            }
        }

        Format22c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 2);
            this.referenceType = referenceType;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int BA) {
            return InstructionReader.read_22c(this, in, context, BA);
        }

        public Instance make(int A, int B, Object cCCCC) {
            return new Instance(A, B, cCCCC);
        }
    }

    public static final class Format23x extends Format {

        public final class Instance extends Instruction {

            public final int AA, BB, CC;

            Instance(int AA, int BB, int CC) {
                this.AA = AA;
                this.BB = BB;
                this.CC = CC;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_23x(out,
                        opcode().opcodeValue(context), AA, BB, CC);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && BB == iobj.BB && CC == iobj.CC;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, BB, CC);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, BB, CC);
            }
        }

        Format23x(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_23x(this, in, AA);
        }

        public Instance make(int AA, int BB, int CC) {
            return new Instance(AA, BB, CC);
        }
    }

    public static final class Format22b extends Format {

        public final class Instance extends Instruction {

            public final int AA, BB, sCC;

            Instance(int AA, int BB, int sCC) {
                this.AA = AA;
                this.BB = BB;
                this.sCC = sCC;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22b(out,
                        opcode().opcodeValue(context), AA, BB, sCC);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && BB == iobj.BB && sCC == iobj.sCC;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, BB, sCC);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, BB, sCC);
            }
        }

        Format22b(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_22b(this, in, AA);
        }

        public Instance make(int AA, int BB, int sCC) {
            return new Instance(AA, BB, sCC);
        }
    }

    public static final class Format22t22s extends Format {

        public final class Instance extends Instruction {

            public final int A, B, sCCCC;

            Instance(int A, int B, int sCCCC) {
                this.A = A;
                this.B = B;
                this.sCCCC = sCCCC;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_22t_22s(out,
                        opcode().opcodeValue(context), A, B, sCCCC);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && A == iobj.A && B == iobj.B && sCCCC == iobj.sCCCC;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, B, sCCCC);
            }

            @Override
            public Instance mutate() {
                return new Instance(A, B, sCCCC);
            }
        }

        Format22t22s(Opcode opcode) {
            super(opcode, 2);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int BA) {
            return InstructionReader.read_22t_22s(this, in, BA);
        }

        public Instance make(int A, int B, int sCCCC) {
            return new Instance(A, B, sCCCC);
        }
    }

    public static final class Format30t extends Format {

        public final class Instance extends Instruction {

            public final int sAAAAAAAA;

            Instance(int sAAAAAAAA) {
                this.sAAAAAAAA = sAAAAAAAA;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_30t(out,
                        opcode().opcodeValue(context), sAAAAAAAA);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && sAAAAAAAA == iobj.sAAAAAAAA;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), sAAAAAAAA);
            }

            @Override
            public Instance mutate() {
                return new Instance(sAAAAAAAA);
            }
        }

        Format30t(Opcode opcode) {
            super(opcode, 3);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_30t(this, in, _00);
        }

        public Instance make(int sAAAAAAAA) {
            return new Instance(sAAAAAAAA);
        }
    }

    public static final class Format32x extends Format {

        public final class Instance extends Instruction {

            public final int AAAA, BBBB;

            Instance(int AAAA, int BBBB) {
                this.AAAA = AAAA;
                this.BBBB = BBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_32x(out,
                        opcode().opcodeValue(context), AAAA, BBBB);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AAAA == iobj.AAAA && BBBB == iobj.BBBB;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AAAA, BBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AAAA, BBBB);
            }
        }

        Format32x(Opcode opcode) {
            super(opcode, 3);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_32x(this, in, _00);
        }

        public Instance make(int AAAA, int BBBB) {
            return new Instance(AAAA, BBBB);
        }
    }

    public static final class Format31i31t extends Format {

        public final class Instance extends Instruction {

            public final int AA, sBBBBBBBB;

            Instance(int AA, int sBBBBBBBB) {
                this.AA = AA;
                this.sBBBBBBBB = sBBBBBBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_31i_31t_31c(out,
                        opcode().opcodeValue(context), AA, sBBBBBBBB);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && sBBBBBBBB == iobj.sBBBBBBBB;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBBBBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, sBBBBBBBB);
            }
        }

        Format31i31t(Opcode opcode) {
            super(opcode, 3);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_31i_31t(this, in, AA);
        }

        public Instance make(int AA, int sBBBBBBBB) {
            return new Instance(AA, sBBBBBBBB);
        }
    }

    public static final class Format31c extends Format {

        public final ReferenceType referenceType;

        public final class Instance extends Instruction {

            public final int AA;
            public final Object cBBBBBBBB;

            Instance(int AA, Object cBBBBBBBB) {
                this.AA = AA;
                this.cBBBBBBBB = referenceType.mutate(cBBBBBBBB);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBBBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_31i_31t_31c(out,
                        opcode().opcodeValue(context), AA,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                        && Objects.equals(cBBBBBBBB, iobj.cBBBBBBBB);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBBBBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, cBBBBBBBB);
            }
        }


        Format31c(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 3);
            this.referenceType = referenceType;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_31c(this, in, context, AA);
        }

        public Instance make(int AA, Object cBBBBBBBB) {
            return new Instance(AA, cBBBBBBBB);
        }
    }

    public static final class Format35c35ms35mi extends Format {

        public final ReferenceType referenceType;

        public final class Instance extends Instruction {

            public final int A, C, D, E, F, G;
            public final Object cBBBB;

            Instance(int A, Object cBBBB, int C, int D, int E, int F, int G) {
                this.A = A;
                this.cBBBB = referenceType.mutate(cBBBB);
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
                        opcode().opcodeValue(context), A,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && A == iobj.A && Objects.equals(cBBBB, iobj.cBBBB)
                        && C == iobj.C && D == iobj.D && E == iobj.E
                        && F == iobj.F && G == iobj.G;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, cBBBB, C, D, E, F, G);
            }

            @Override
            public Instance mutate() {
                return new Instance(A, cBBBB, C, D, E, F, G);
            }
        }

        Format35c35ms35mi(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 3);
            this.referenceType = referenceType;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AG) {
            return InstructionReader.read_35c_35ms_35mi(this, in, context, AG);
        }

        public Instance make(int A, Object cBBBB, int C, int D, int E, int F, int G) {
            return new Instance(A, cBBBB, C, D, E, F, G);
        }
    }

    public static final class Format3rc3rms3rmi extends Format {

        public final ReferenceType referenceType;

        public final class Instance extends Instruction {

            public final int AA;
            public final Object cBBBB;
            public final int CCCC;

            Instance(int AA, Object cBBBB, int CCCC) {
                this.AA = AA;
                this.cBBBB = referenceType.mutate(cBBBB);
                this.CCCC = CCCC;
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_3rc_3rms_3rmi(out,
                        opcode().opcodeValue(context), AA,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                        && Objects.equals(cBBBB, iobj.cBBBB) && CCCC == iobj.CCCC;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBB, CCCC);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, cBBBB, CCCC);
            }
        }

        Format3rc3rms3rmi(Opcode opcode, ReferenceType referenceType) {
            super(opcode, 3);
            this.referenceType = referenceType;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_3rc_3rms_3rmi(this, in, context, AA);
        }

        public Instance make(int AA, Object cBBBB, int CCCC) {
            return new Instance(AA, cBBBB, CCCC);
        }
    }

    public static final class Format45cc extends Format {

        public final ReferenceType referenceType;
        public final ReferenceType referenceType2;

        public final class Instance extends Instruction {

            public final int A, C, D, E, F, G;
            public final Object cBBBB, cHHHH;

            Instance(int A, Object cBBBB, int C, int D, int E, int F, int G, Object cHHHH) {
                this.A = A;
                this.cBBBB = referenceType.mutate(cBBBB);
                this.C = C;
                this.D = D;
                this.E = E;
                this.F = F;
                this.G = G;
                this.cHHHH = referenceType2.mutate(cHHHH);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
                referenceType2.collectData(data, cHHHH);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_45cc(out,
                        opcode().opcodeValue(context), A,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && A == iobj.A && Objects.equals(cBBBB, iobj.cBBBB)
                        && C == iobj.C && D == iobj.D && E == iobj.E
                        && F == iobj.F && G == iobj.G && Objects.equals(cHHHH, iobj.cHHHH);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), A, cBBBB, C, D, E, F, G, cHHHH);
            }

            @Override
            public Instance mutate() {
                return new Instance(A, cBBBB, C, D, E, F, G, cHHHH);
            }
        }

        Format45cc(Opcode opcode, ReferenceType referenceType, ReferenceType referenceType2) {
            super(opcode, 4);
            this.referenceType = referenceType;
            this.referenceType2 = referenceType2;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AG) {
            return InstructionReader.read_45cc(this, in, context, AG);
        }

        public Instance make(int A, Object cBBBB, int C, int D, int E, int F, int G, Object cHHHH) {
            return new Instance(A, cBBBB, C, D, E, F, G, cHHHH);
        }
    }

    public static final class Format4rcc extends Format {

        public final ReferenceType referenceType;
        public final ReferenceType referenceType2;

        public final class Instance extends Instruction {

            public final int AA;
            public final Object cBBBB;
            public final int CCCC;
            public final Object cHHHH;

            Instance(int AA, Object cBBBB, int CCCC, Object cHHHH) {
                this.AA = AA;
                this.cBBBB = referenceType.mutate(cBBBB);
                this.CCCC = CCCC;
                this.cHHHH = referenceType2.mutate(cHHHH);
            }

            @Override
            public void collectData(DataCollector data) {
                referenceType.collectData(data, cBBBB);
                referenceType2.collectData(data, cHHHH);
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_4rcc(out,
                        opcode().opcodeValue(context), AA,
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode()) && AA == iobj.AA
                        && Objects.equals(cBBBB, iobj.cBBBB) && CCCC == iobj.CCCC
                        && Objects.equals(cHHHH, iobj.cHHHH);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, cBBBB, CCCC, cHHHH);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, cBBBB, CCCC, cHHHH);
            }
        }

        Format4rcc(Opcode opcode, ReferenceType referenceType, ReferenceType referenceType2) {
            super(opcode, 4);
            this.referenceType = referenceType;
            this.referenceType2 = referenceType2;
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_4rcc(this, in, context, AA);
        }

        public Instance make(int AA, Object cBBBB, int CCCC, Object cHHHH) {
            return new Instance(AA, cBBBB, CCCC, cHHHH);
        }
    }

    public static final class Format51l extends Format {

        public final class Instance extends Instruction {

            public final int AA;
            public final long sBBBBBBBBBBBBBBBB;

            Instance(int AA, long sBBBBBBBBBBBBBBBB) {
                this.AA = AA;
                this.sBBBBBBBBBBBBBBBB = sBBBBBBBBBBBBBBBB;
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_51l(out,
                        opcode().opcodeValue(context), AA, sBBBBBBBBBBBBBBBB);
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && AA == iobj.AA && sBBBBBBBBBBBBBBBB == iobj.sBBBBBBBBBBBBBBBB;
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), AA, sBBBBBBBBBBBBBBBB);
            }

            @Override
            public Instance mutate() {
                return new Instance(AA, sBBBBBBBBBBBBBBBB);
            }
        }

        Format51l(Opcode opcode) {
            super(opcode, 5);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int AA) {
            return InstructionReader.read_51l(this, in, AA);
        }

        public Instance make(int AA, long sBBBBBBBBBBBBBBBB) {
            return new Instance(AA, sBBBBBBBBBBBBBBBB);
        }
    }

    public static final class PackedSwitchPayload extends Format {

        public final class Instance extends Instruction {

            public final int first_key;
            public final int[] targets;

            Instance(int first_key, int[] targets) {
                this.first_key = first_key;
                this.targets = targets.clone();
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_packed_switch_payload(out,
                        opcode().opcodeValue(context),
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && first_key == iobj.first_key
                        && Arrays.equals(targets, iobj.targets);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), first_key, Arrays.hashCode(targets));
            }

            @Override
            public Instance mutate() {
                return new Instance(first_key, targets);
            }
        }

        PackedSwitchPayload(Opcode opcode) {
            super(opcode, -1, true);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_packed_switch_payload(this, in, _00);
        }

        public Instance make(int first_key, int[] targets) {
            return new Instance(first_key, targets);
        }
    }

    public static final class SparseSwitchPayload extends Format {

        public final class Instance extends Instruction {

            public final int[] keys;
            public final int[] targets;

            Instance(int[] keys, int[] targets) {
                this.keys = keys.clone();
                this.targets = targets.clone();
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_sparse_switch_payload(out,
                        opcode().opcodeValue(context),
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && Arrays.equals(keys, iobj.keys)
                        && Arrays.equals(targets, iobj.targets);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), Arrays.hashCode(keys), Arrays.hashCode(targets));
            }

            @Override
            public Instance mutate() {
                return new Instance(keys, targets);
            }
        }

        SparseSwitchPayload(Opcode opcode) {
            super(opcode, -1, true);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_sparse_switch_payload(this, in, _00);
        }

        public Instance make(int[] keys, int[] targets) {
            return new Instance(keys, targets);
        }
    }

    public static final class ArrayPayload extends Format {

        public final class Instance extends Instruction {

            public final int element_width;
            public final byte[] data;

            Instance(int element_width, byte[] data) {
                this.element_width = element_width;
                this.data = data.clone();
            }

            @Override
            public void write(WriteContext context, RandomOutput out) {
                InstructionWriter.write_array_payload(out,
                        opcode().opcodeValue(context),
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
                if (obj == this) return true;
                return obj instanceof Instance iobj
                        && Objects.equals(opcode(), iobj.opcode())
                        && element_width == iobj.element_width
                        && Arrays.equals(data, iobj.data);
            }

            @Override
            public int hashCode() {
                return Objects.hash(opcode(), element_width, Arrays.hashCode(data));
            }

            @Override
            public Instance mutate() {
                return new Instance(element_width, data);
            }
        }

        ArrayPayload(Opcode opcode) {
            super(opcode, -1, true);
        }

        @Override
        public Instance read(RandomInput in, ReadContext context, int _00) {
            return InstructionReader.read_array_payload(this, in, _00);
        }

        public Instance make(int element_width, byte[] data) {
            return new Instance(element_width, data);
        }
    }
}
