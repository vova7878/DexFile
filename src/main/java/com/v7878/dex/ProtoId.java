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

import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public final class ProtoId implements Mutable {

    public static final int SIZE = 0x0c;

    public static final Comparator<ProtoId> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.return_type, b.return_type);
        if (out != 0) {
            return out;
        }

        return TypeList.COMPARATOR.compare(a.parameters, b.parameters);
    };

    public static ProtoId of(Executable ex) {
        Objects.requireNonNull(ex, "trying to get ProtoId of null");
        Class<?> return_type = ex instanceof Method
                ? ((Method) ex).getReturnType() : void.class;
        return new ProtoId(TypeId.of(return_type),
                Arrays.stream(ex.getParameterTypes())
                        .map(TypeId::of).toArray(TypeId[]::new));
    }

    public static ProtoId of(MethodType proto) {
        Objects.requireNonNull(proto, "trying to get ProtoId of null");
        return new ProtoId(TypeId.of(proto.returnType()),
                proto.parameterList().stream()
                        .map(TypeId::of).toArray(TypeId[]::new));
    }

    private TypeId return_type;
    private TypeList parameters;

    public ProtoId(TypeId return_type, Collection<TypeId> parameters) {
        setReturnType(return_type);
        setParameters(parameters);
    }

    public ProtoId(TypeId return_type, TypeId... parameters) {
        this(return_type, new TypeList(parameters));
    }

    public void setReturnType(TypeId return_type) {
        this.return_type = Objects.requireNonNull(return_type,
                "return_type can`t be null").mutate();
    }

    public TypeId getReturnType() {
        return return_type;
    }

    public void setParameters(Collection<TypeId> parameters) {
        this.parameters = parameters == null
                ? TypeList.empty() : new TypeList(parameters);
    }

    public TypeList getParameters() {
        return parameters;
    }

    public String getShorty() {
        StringBuilder out = new StringBuilder(parameters.size() + 1);
        out.append(return_type.getShorty());
        for (TypeId tmp : parameters) {
            out.append(tmp.getShorty());
        }
        return out.toString();
    }

    public int getInputRegistersCount() {
        int out = 0;
        for (TypeId tmp : parameters) {
            out += tmp.getRegistersCount();
        }
        return out;
    }

    public static ProtoId read(RandomInput in, ReadContext context) {
        in.readInt(); // shorty
        TypeId return_type = context.type(in.readInt());
        int parameters_off = in.readInt();
        TypeList parameters = null;
        if (parameters_off != 0) {
            parameters = TypeList.read(context.data(parameters_off), context);
        }
        return new ProtoId(return_type, parameters);
    }

    static ProtoId[] readArray(RandomInput in, ReadContext context, int size) {
        ProtoId[] out = new ProtoId[size];
        for (int i = 0; i < size; i++) {
            out[i] = read(in, context);
        }
        return out;
    }

    static ProtoId readInArray(RandomInput in, ReadContext context, int index) {
        in.addPosition((long) index * SIZE);
        return read(in, context);
    }

    public void collectData(DataCollector data) {
        data.add(getShorty());
        data.add(return_type);
        if (!parameters.isEmpty()) {
            data.add(parameters);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getStringIndex(getShorty()));
        out.writeInt(context.getTypeIndex(return_type));
        out.writeInt(parameters.isEmpty() ? 0
                : context.getTypeListOffset(parameters));
    }

    static void writeSection(WriteContext context, RandomOutput out, ProtoId[] protos) {
        for (ProtoId value : protos) {
            value.write(context, out);
        }
    }

    @Override
    public String toString() {
        return "" + parameters + return_type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ProtoId pobj
                && Objects.equals(return_type, pobj.return_type)
                && Objects.equals(parameters, pobj.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(return_type, parameters);
    }

    @Override
    public ProtoId mutate() {
        return new ProtoId(return_type, parameters);
    }
}
