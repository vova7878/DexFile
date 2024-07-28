/*
 * Copyright (c) 2024 Vladimir Kozelkov
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

import static com.v7878.dex.DexConstants.NO_INDEX;

import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;
import com.v7878.dex.util.SparseArray;

import java.util.Collection;
import java.util.Objects;

public final class DebugInfo implements Mutable {

    private SparseArray<String> parameter_names;
    private MutableList<DebugItem> items;

    public DebugInfo(SparseArray<String> parameter_names,
                     Collection<DebugItem> items) {
        setParameterNames(parameter_names);
        setItems(items);
    }

    public DebugInfo() {
        this(null, null);
    }

    public void setParameterNames(SparseArray<String> parameter_names) {
        this.parameter_names = parameter_names == null ?
                new SparseArray<>() : new SparseArray<>(parameter_names);
    }

    public SparseArray<String> getParameterNames() {
        return parameter_names;
    }

    public void setItems(Collection<DebugItem> items) {
        this.items = items == null ?
                MutableList.empty() : new MutableList<>(items);
    }

    public MutableList<DebugItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return parameter_names.isEmpty() && items.isEmpty();
    }

    public static DebugInfo read(RandomInput in, ReadContext context) {
        int line_start = in.readULeb128();
        var parameter_names = new SparseArray<String>();
        {
            int parameters_size = in.readULeb128();
            for (int i = 0; i < parameters_size; i++) {
                int name_idx = in.readULeb128() - 1;
                if (name_idx == NO_INDEX) continue;
                parameter_names.put(i, context.string(name_idx));
            }
        }
        var items = DebugItem.readArray(in, context, line_start);
        return new DebugInfo(parameter_names, items);
    }

    public void collectData(DataCollector data) {
        var pnames = parameter_names;
        var pnames_size = pnames.size();
        for (int i = 0; i < pnames_size; i++) {
            var value = pnames.valueAt(i);
            if (value != null) data.add(value);
        }
        for (DebugItem item : items) {
            data.fill(item);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        var pnames = parameter_names;
        var pnames_size = pnames.size();
        var pnames_out_length = 0;
        if (pnames_size > 0) {
            int key = pnames.keyAt(0);
            if (key < 0) {
                // TODO: disallow negative indexes in container class
                throw new IllegalStateException("parameter_names contains negative indexes: " + key);
            }
            pnames_out_length = pnames.keyAt(pnames_size - 1) + 1;
        }
        int[] line_start = {1};
        ByteArrayIO dbg_sequence = new ByteArrayIO();
        DebugItem.writeArray(dbg_sequence, context, items, line_start);
        dbg_sequence.position(0);

        out.writeULeb128(line_start[0]);
        out.writeULeb128(pnames_out_length);
        for (int i = 0; i < pnames_out_length; i++) {
            var value = pnames.get(i);
            out.writeULeb128((value == null ?
                    NO_INDEX : context.getStringIndex(value)) + 1);
        }
        dbg_sequence.readTo(out);
    }

    static void writeSection(WriteContextImpl context, FileMap map,
                             RandomOutput out, DebugInfo[] debug_infos) {
        if (debug_infos.length != 0) {
            map.debug_info_items_off = (int) out.position();
            map.debug_info_items_size = debug_infos.length;
        }
        for (DebugInfo tmp : debug_infos) {
            int start = (int) out.position();
            tmp.write(context, out);
            context.addDebugInfo(tmp, start);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof DebugInfo other
                && Objects.equals(parameter_names, other.parameter_names)
                && Objects.equals(items, other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter_names, items);
    }

    @Override
    public DebugInfo mutate() {
        return new DebugInfo(parameter_names, items);
    }
}
