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
import com.v7878.dex.Mutable;
import com.v7878.dex.ReadContext;
import com.v7878.dex.WriteContext;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;

public abstract class Instruction implements Mutable {
    Instruction() {
    }

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

    //TODO: find a way to read code with incorrect instructions (which used to protect dex from reading)
    public static MutableList<Instruction> readArray(RandomInput in, ReadContext context) {
        MutableList<Instruction> insns = MutableList.empty();

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
    public abstract Instruction mutate();
}
