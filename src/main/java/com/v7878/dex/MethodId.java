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

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Objects;

public final class MethodId extends FieldOrMethodId {

    public static final int SIZE = 0x08;

    public static final Comparator<MethodId> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.getDeclaringClass(), b.getDeclaringClass());
        if (out != 0) {
            return out;
        }

        out = StringId.COMPARATOR
                .compare(a.getName(), b.getName());
        if (out != 0) {
            return out;
        }

        return ProtoId.COMPARATOR.compare(a.proto, b.proto);
    };

    private static String getName(Executable ex) {
        if (ex instanceof Constructor) {
            return (ex.getModifiers() & Modifier.STATIC) == 0 ? "<init>" : "<clinit>";
        }
        return ex.getName();
    }

    public static MethodId of(Executable ex) {
        Objects.requireNonNull(ex, "trying to get MethodId of null");
        return new MethodId(TypeId.of(ex.getDeclaringClass()),
                ProtoId.of(ex), getName(ex));
    }

    public static MethodId constructor(TypeId declaring_class, TypeId... parameters) {
        return new MethodId(declaring_class,
                new ProtoId(TypeId.V, parameters), "<init>");
    }

    private ProtoId proto;

    public MethodId(TypeId declaring_class, ProtoId proto, String name) {
        super(declaring_class, name);
        setProto(proto);
    }

    public void setProto(ProtoId proto) {
        this.proto = Objects.requireNonNull(proto,
                "proto can`t be null").mutate();
    }

    public ProtoId getProto() {
        return proto;
    }

    public static MethodId read(RandomInput in, ReadContext context) {
        return new MethodId(
                context.type(in.readUnsignedShort()),
                context.proto(in.readUnsignedShort()),
                context.string(in.readInt())
        );
    }

    static MethodId[] readArray(RandomInput in, ReadContext context, int size) {
        MethodId[] out = new MethodId[size];
        for (int i = 0; i < size; i++) {
            out[i] = read(in, context);
        }
        return out;
    }

    static MethodId readInArray(RandomInput in, ReadContext context, int index) {
        in.addPosition((long) index * SIZE);
        return read(in, context);
    }

    @Override
    public void collectData(DataCollector data) {
        data.add(proto);
        super.collectData(data);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeShort(context.getTypeIndex(getDeclaringClass()));
        out.writeShort(context.getProtoIndex(proto));
        out.writeInt(context.getStringIndex(getName()));
    }

    static void writeSection(WriteContext context, RandomOutput out, MethodId[] methods) {
        for (MethodId value : methods) {
            value.write(context, out);
        }
    }

    @Override
    public String toString() {
        return getDeclaringClass() + "." + getName() + proto;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!super.equals(obj)) return false;
        return obj instanceof MethodId mobj
                && Objects.equals(proto, mobj.proto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), proto);
    }

    @Override
    public MethodId mutate() {
        return new MethodId(getDeclaringClass(), proto, getName());
    }
}
