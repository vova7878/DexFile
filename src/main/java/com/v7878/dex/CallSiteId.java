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

import com.v7878.dex.EncodedValue.ArrayValue;
import com.v7878.dex.EncodedValue.EncodedValueType;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;
import java.util.Objects;

public final class CallSiteId implements Mutable {

    public static final int SIZE = 0x04;

    public static final Comparator<CallSiteId> COMPARATOR =
            (a, b) -> ArrayValue.COMPARATOR.compare(a.value, b.value);

    private ArrayValue value;

    public CallSiteId(ArrayValue value) {
        setValue(value);
    }

    public void setValue(ArrayValue value) {
        this.value = Objects.requireNonNull(value,
                "call site value can`t be null").mutate();
    }

    public ArrayValue getValue() {
        return value;
    }

    public static CallSiteId read(RandomInput in, ReadContext context) {
        return new CallSiteId((ArrayValue) EncodedValue.readValue(
                context.data(in.readInt()), context, EncodedValueType.ARRAY));
    }

    static CallSiteId[] readArray(RandomInput in, ReadContext context, int size) {
        CallSiteId[] out = new CallSiteId[size];
        for (int i = 0; i < size; i++) {
            out[i] = read(in, context);
        }
        return out;
    }

    public void collectData(DataCollector data) {
        data.add(value);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getArrayValueOffset(value));
    }

    static void writeSection(WriteContext context, RandomOutput out, CallSiteId[] callSites) {
        for (CallSiteId value : callSites) {
            value.write(context, out);
        }
    }

    @Override
    public String toString() {
        return "CallSiteId" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof CallSiteId) {
            CallSiteId csobj = (CallSiteId) obj;
            return Objects.equals(value, csobj.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public CallSiteId mutate() {
        return new CallSiteId(value);
    }
}
