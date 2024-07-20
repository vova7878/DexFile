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
    public static final int COMPACT_ALIGNMENT = 2;

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

    private static final int kRegistersSizeShift = 12;
    private static final int kInsSizeShift = 8;
    private static final int kOutsSizeShift = 4;
    private static final int kTriesSizeSizeShift = 0;
    private static final int kInsnsSizeShift = 5;

    private static final int kFlagPreHeaderRegistersSize = 0b00001;
    private static final int kFlagPreHeaderInsSize = 0b00010;
    private static final int kFlagPreHeaderOutsSize = 0b00100;
    private static final int kFlagPreHeaderTriesSize = 0b01000;
    private static final int kFlagPreHeaderInsnsSize = 0b10000;

    private static int readUnsignedShortBackward(RandomInput in) {
        in.addPosition(-2);
        int out = in.readUnsignedShort();
        in.addPosition(-2);
        return out;
    }

    public static CodeItem read(RandomInput in, ReadContext context) {
        int registers_size;
        int ins_size;
        int outs_size;
        int tries_size;
        int insns_count; // in 2-byte code units

        if (context.getDexVersion().isCompact()) {
            RandomInput preheader = in.duplicate(in.position());

            int fields = in.readUnsignedShort();
            int insns_count_and_flags = in.readUnsignedShort();

            insns_count = insns_count_and_flags >> kInsnsSizeShift;
            registers_size = (fields >> kRegistersSizeShift) & 0xF;
            ins_size = (fields >> kInsSizeShift) & 0xF;
            outs_size = (fields >> kOutsSizeShift) & 0xF;
            tries_size = (fields >> kTriesSizeSizeShift) & 0xF;

            if ((insns_count_and_flags & kFlagPreHeaderInsnsSize) != 0) {
                insns_count += readUnsignedShortBackward(preheader) +
                        (readUnsignedShortBackward(preheader) << 16);
            }
            if ((insns_count_and_flags & kFlagPreHeaderRegistersSize) != 0) {
                registers_size += readUnsignedShortBackward(preheader);
            }
            if ((insns_count_and_flags & kFlagPreHeaderInsSize) != 0) {
                ins_size += readUnsignedShortBackward(preheader);
            }
            if ((insns_count_and_flags & kFlagPreHeaderOutsSize) != 0) {
                outs_size += readUnsignedShortBackward(preheader);
            }
            if ((insns_count_and_flags & kFlagPreHeaderTriesSize) != 0) {
                tries_size += readUnsignedShortBackward(preheader);
            }

            registers_size += ins_size;
        } else {
            registers_size = in.readUnsignedShort();
            ins_size = in.readUnsignedShort();
            outs_size = in.readUnsignedShort();
            tries_size = in.readUnsignedShort();
            in.readInt(); //TODO: debug_info_off = in.readInt();
            insns_count = in.readInt();
        }

        CodeItem out = new CodeItem(registers_size, ins_size, outs_size,
                Instruction.readArray(in, context, insns_count), null);

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
            tries.sort(TryItem.COMPARATOR);

            out.fillZerosToAlignment(TryItem.ALIGNMENT);

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

    static void writeSection(WriteContextImpl context, FileMap map,
                             RandomOutput out, CodeItem[] code_items) {
        int alignment = context.getDexVersion().isCompact() ? COMPACT_ALIGNMENT : ALIGNMENT;
        if (code_items.length != 0) {
            out.alignPosition(alignment);
            map.code_items_off = (int) out.position();
            map.code_items_size = code_items.length;
        }
        for (CodeItem tmp : code_items) {
            out.alignPosition(alignment);
            int start = (int) out.position();
            tmp.write(context, out);
            context.addCodeItem(tmp, start);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
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
