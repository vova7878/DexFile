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

import java.util.Comparator;

public final class StringId {

    private StringId() {
    }

    public static final int SIZE = 0x04;

    public static final Comparator<String> COMPARATOR = String::compareTo;

    public static String read(RandomInput in, ReadContext context) {
        int data_off = in.readInt();
        //noinspection resource
        return context.data(data_off).readMUTF8();
    }

    static String[] readArray(RandomInput in, ReadContext context, int size) {
        String[] out = new String[size];
        for (int i = 0; i < size; i++) {
            out[i] = read(in, context);
        }
        return out;
    }

    static String readInArray(RandomInput in, ReadContext context, int index) {
        in.addPosition((long) index * SIZE);
        return read(in, context);
    }

    public static void write(String value, WriteContext context,
                             RandomOutput ids_out, RandomOutput data_out) {
        ids_out.writeInt((int) data_out.position());
        data_out.writeMUtf8(value);
    }

    static void writeSection(WriteContext context, FileMap map, RandomOutput ids_out,
                             RandomOutput data_out, String[] strings) {
        if (strings.length != 0) {
            map.string_data_items_off = (int) data_out.position();
            map.string_data_items_size = map.string_ids_size;
        }
        for (String value : strings) {
            StringId.write(value, context, ids_out, data_out);
        }
    }
}
