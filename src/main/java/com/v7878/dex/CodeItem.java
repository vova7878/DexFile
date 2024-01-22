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

package com.v7878.dex;

import com.v7878.dex.bytecode.Instruction;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;
import com.v7878.dex.util.SparseArray;
import com.v7878.misc.Checks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

public final class CodeItem implements Mutable {

    public static final int ALIGNMENT = 4;

    private int registers_size;
    private int ins_size;
    private int outs_size;
    private MutableList<Instruction> insns;
    private MutableList<TryItem> tries;

    public CodeItem(int registers_size, int ins_size, int outs_size,
                    Collection<Instruction> insns, Collection<TryItem> tries) {
        setRegistersSize(registers_size);
        setInputsSize(ins_size);
        setOutputsSize(outs_size);
        setInstructions(insns);
        setTries(tries);
    }

    public void setRegistersSize(int registers_size) {
        Checks.checkRange(registers_size, 0, 1 << 16);
        this.registers_size = registers_size;
    }

    public int getRegistersSize() {
        return registers_size;
    }

    public void setInputsSize(int ins_size) {
        Checks.checkRange(ins_size, 0, 1 << 16);
        this.ins_size = ins_size;
    }

    public int getInputsSize() {
        return ins_size;
    }

    public void setOutputsSize(int outs_size) {
        Checks.checkRange(ins_size, 0, 1 << 8);
        this.outs_size = outs_size;
    }

    public int getOutputsSize() {
        return outs_size;
    }

    public void setInstructions(Collection<Instruction> insns) {
        this.insns = insns == null
                ? MutableList.empty() : new MutableList<>(insns);
    }

    public MutableList<Instruction> getInstructions() {
        return insns;
    }

    public void setTries(Collection<TryItem> tries) {
        this.tries = tries == null
                ? MutableList.empty() : new MutableList<>(tries);
    }

    public MutableList<TryItem> getTries() {
        return tries;
    }

    public static CodeItem read(RandomInput in, ReadContext context) {
        int registers_size = in.readUnsignedShort();
        int ins_size = in.readUnsignedShort();
        int outs_size = in.readUnsignedShort();
        int tries_size = in.readUnsignedShort();
        in.readInt(); //TODO: out.debug_info_off = in.readInt();

        CodeItem out = new CodeItem(registers_size, ins_size, outs_size, null, null);

        out.insns = Instruction.readArray(in, context);

        if (tries_size > 0) {
            in.alignPosition(TryItem.ALIGNMENT);
            long tries_pos = in.position();
            in.addPosition((long) tries_size * TryItem.SIZE);

            long handlers_start = in.position();
            int handlers_size = in.readULeb128();

            SparseArray<CatchHandler> handlers = new SparseArray<>(handlers_size);
            for (int i = 0; i < handlers_size; i++) {
                int handler_offset = (int) (in.position() - handlers_start);
                handlers.put(handler_offset, CatchHandler.read(in, context));
            }

            in.position(tries_pos);
            for (int i = 0; i < tries_size; i++) {
                out.tries.add(TryItem.read(in, handlers));
            }
        }
        return out;
    }

    public void collectData(DataCollector data) {
        for (Instruction tmp : insns) {
            data.fill(tmp);
        }
        for (TryItem tmp : tries) {
            data.fill(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeShort(registers_size);
        out.writeShort(ins_size);
        out.writeShort(outs_size);

        int tries_size = tries.size();
        out.writeShort(tries_size);

        out.writeInt(0); // TODO: debug_info_off

        long insns_size_pos = out.position();
        out.addPosition(4);

        long insns_start = out.position();
        for (Instruction tmp : insns) {
            tmp.write(context, out);
        }
        int insns_size = (int) (out.position() - insns_start);
        if ((insns_size & 1) != 0) {
            throw new IllegalStateException("insns_size is odd");
        }

        out.position(insns_size_pos);
        out.writeInt(insns_size / 2); // size in code units
        out.position(insns_start + insns_size);

        if (tries_size != 0) {
            out.alignPositionAndFillZeros(TryItem.ALIGNMENT);

            RandomOutput tries_out = out.duplicate(out.position());
            out.addPosition((long) TryItem.SIZE * tries_size);

            HashMap<CatchHandler, Integer> handlers = new HashMap<>(tries_size);
            for (TryItem tmp : tries) {
                handlers.put(tmp.getHandler(), null);
            }

            long handlers_start = out.position();
            out.writeULeb128(handlers.size());

            for (CatchHandler tmp : handlers.keySet()) {
                int handler_offset = (int) (out.position() - handlers_start);
                tmp.write(context, out);
                handlers.replace(tmp, handler_offset);
            }
            for (TryItem tmp : tries) {
                tmp.write(tries_out, handlers);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodeItem) {
            CodeItem ciobj = (CodeItem) obj;
            return registers_size == ciobj.registers_size
                    && ins_size == ciobj.ins_size
                    && outs_size == ciobj.outs_size
                    && Objects.equals(insns, ciobj.insns)
                    && Objects.equals(tries, ciobj.tries);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(registers_size, ins_size,
                outs_size, insns, tries);
    }

    @Override
    public CodeItem mutate() {
        return new CodeItem(registers_size, ins_size, outs_size, insns, tries);
    }
}
