package com.v7878.dex.bytecode;

import com.v7878.dex.DataCollector;
import com.v7878.dex.PublicCloneable;
import com.v7878.dex.ReadContext;
import com.v7878.dex.WriteContext;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.PCList;

public abstract class Instruction implements PublicCloneable {

    public static Instruction read(RandomInput in, ReadContext context) {
        int unit = in.readUnsignedShort();

        int opcode = unit & 0xff;
        int arg = unit >> 8;

        if (opcode == 0x00 && arg != 0) {
            opcode = unit;
            arg = 0;
        }

        return context.opcode(opcode).format().read(in, context, arg);
    }

    public static PCList<Instruction> readArray(RandomInput in, ReadContext context) {
        PCList<Instruction> insns = PCList.empty();

        int insns_size = in.readInt(); // in 2-byte code units
        insns.ensureCapacity(insns_size);

        int insns_bytes = insns_size * 2;
        long start = in.position();

        while (in.position() - start < insns_bytes) {
            if ((in.position() - start & 1) != 0) {
                throw new IllegalStateException("Unaligned code unit");
            }
            insns.add(read(in, context));
        }

        if (in.position() - start != insns_bytes) {
            throw new IllegalStateException("Read more code units than expected");
        }

        insns.trimToSize();
        return insns;
    }

    public void collectData(DataCollector data) {
    }

    public abstract void write(WriteContext context, RandomOutput out);

    public abstract Opcode opcode();

    public int units() {
        return opcode().format().units();
    }

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract Instruction clone();
}
