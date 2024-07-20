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

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.SparseArray;
import com.v7878.misc.Checks;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public final class TryItem implements Mutable {

    public static final Comparator<TryItem> COMPARATOR = (a, b) -> {
        int out = Integer.compare(a.start_addr, b.start_addr);
        if (out != 0) {
            return out;
        }

        // a.start_addr == b.start_addr, but a != b
        throw new IllegalStateException("try items are overlapping: " + a + " " + b);
    };

    public static final int SIZE = 8;
    public static final int ALIGNMENT = 4;

    public int start_addr;
    public int insn_count;
    public CatchHandler handler;

    public TryItem(int start_addr, int insn_count, CatchHandler handler) {
        setStartAddress(start_addr);
        setInstructionCount(insn_count);
        setHandler(handler);
    }

    public void setStartAddress(int start_addr) {
        if (start_addr < 0) {
            throw new IllegalArgumentException("start address can`t be negative");
        }
        this.start_addr = start_addr;
    }

    public int getStartAddress() {
        return start_addr;
    }

    public void setInstructionCount(int insn_count) {
        Checks.checkRange(insn_count, 0, 1 << 16);
        this.insn_count = insn_count;
    }

    public int getInstructionCount() {
        return insn_count;
    }

    public void setHandler(CatchHandler handler) {
        this.handler = Objects.requireNonNull(handler,
                "catch handler can`t be null").mutate();
    }

    public CatchHandler getHandler() {
        return handler;
    }

    public static TryItem read(RandomInput in, SparseArray<CatchHandler> handlers) {
        int start_addr = in.readInt(); // in code units
        int insn_count = in.readUnsignedShort(); // in code units

        int handler_off = in.readUnsignedShort();
        CatchHandler handler = handlers.get(handler_off);
        if (handler == null) {
            throw new IllegalStateException(
                    "unable to find catch handler with offset " + handler_off);
        }
        return new TryItem(start_addr, insn_count, handler);
    }

    public void collectData(DataCollector data) {
        data.fill(handler);
    }

    public void write(RandomOutput out, Map<CatchHandler, Integer> handlers) {
        out.writeInt(start_addr);
        out.writeShort(insn_count);
        Integer offset = handlers.get(handler);
        if (offset == null) {
            throw new IllegalStateException(
                    "unable to find offset for catch handler");
        }
        out.writeShort(offset);
    }

    @Override
    public String toString() {
        return "try item " + start_addr + " " + insn_count + " " + handler;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TryItem tobj
                && start_addr == tobj.start_addr
                && insn_count == tobj.insn_count
                && Objects.equals(handler, tobj.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start_addr, insn_count, handler);
    }

    @Override
    public TryItem mutate() {
        return new TryItem(start_addr, insn_count, handler);
    }
}
