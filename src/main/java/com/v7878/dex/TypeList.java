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
import com.v7878.dex.util.MutableList;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TypeList extends MutableList<TypeId> {

    public static final int ALIGNMENT = 4;

    public static final Comparator<TypeList> COMPARATOR
            = MutableList.getComparator(TypeId.COMPARATOR);

    public TypeList(int initialCapacity) {
        super(initialCapacity);
    }

    public TypeList(TypeId... types) {
        super(types);
    }

    public TypeList(Collection<TypeId> types) {
        super(types);
    }

    public static TypeList empty() {
        return new TypeList();
    }

    @Override
    protected TypeId check(TypeId type) {
        return Objects.requireNonNull(type,
                "TypeList can`t contain null type");
    }

    public static TypeList read(RandomInput in, ReadContext context) {
        TypeList out = new TypeList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            out.add(context.type(in.readUnsignedShort()));
        }
        return out;
    }

    public void collectData(DataCollector data) {
        for (TypeId tmp : this) {
            data.add(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(size());
        for (TypeId tmp : this) {
            out.writeShort(context.getTypeIndex(tmp));
        }
    }

    static void writeSection(WriteContextImpl context, FileMap map,
                             RandomOutput out, TypeList[] lists) {
        if (lists.length != 0) {
            out.alignPosition(ALIGNMENT);
            map.type_lists_off = (int) out.position();
            map.type_lists_size = lists.length;
        }
        for (TypeList tmp : lists) {
            out.alignPosition(ALIGNMENT);
            int start = (int) out.position();
            tmp.write(context, out);
            context.addTypeList(tmp, start);
        }
    }

    @Override
    public String toString() {
        return stream().map(TypeId::toString)
                .collect(Collectors.joining("", "(", ")"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TypeList && super.equals(obj);
    }

    // hashcode from super

    @Override
    public TypeList mutate() {
        return new TypeList(this);
    }
}
