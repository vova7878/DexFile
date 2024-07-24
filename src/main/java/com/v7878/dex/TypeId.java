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
import java.util.Objects;

public final class TypeId implements Mutable {

    public static final TypeId V = new TypeId("V");
    public static final TypeId Z = new TypeId("Z");
    public static final TypeId B = new TypeId("B");
    public static final TypeId S = new TypeId("S");
    public static final TypeId C = new TypeId("C");
    public static final TypeId I = new TypeId("I");
    public static final TypeId F = new TypeId("F");
    public static final TypeId J = new TypeId("J");
    public static final TypeId D = new TypeId("D");

    public static final TypeId OBJECT = TypeId.of(Object.class);

    public static final int SIZE = 0x04;

    public static final Comparator<TypeId> COMPARATOR =
            (a, b) -> StringId.COMPARATOR.compare(a.descriptor, b.descriptor);

    public static TypeId of(String class_name) {
        Objects.requireNonNull(class_name, "trying to get TypeId of null");
        int array_depth = 0;
        while (class_name.endsWith("[]")) {
            array_depth++;
            class_name = class_name.substring(0, class_name.length() - 2);
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < array_depth; i++) {
            out.append('[');
        }
        switch (class_name) {
            case "void" -> out.append("V");
            case "boolean" -> out.append("Z");
            case "byte" -> out.append("B");
            case "short" -> out.append("S");
            case "char" -> out.append("C");
            case "int" -> out.append("I");
            case "float" -> out.append("F");
            case "long" -> out.append("J");
            case "double" -> out.append("D");
            default -> {
                out.append("L");
                out.append(class_name.replace('.', '/'));
                out.append(";");
            }
        }
        return new TypeId(out.toString());
    }

    public static TypeId of(Class<?> clazz) {
        String class_name = clazz.getName();
        if (class_name.startsWith("[")) {
            return new TypeId(class_name.replace('.', '/'));
        }
        return of(class_name);
    }

    private String descriptor;

    public TypeId(String descriptor) {
        setDescriptor(descriptor);
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = Objects.requireNonNull(
                descriptor, "type descriptor can`t be null");
    }

    public String getDescriptor() {
        return descriptor;
    }

    public char getShorty() {
        char c = descriptor.charAt(0);
        return c == '[' ? 'L' : c;
    }

    public int getRegistersCount() {
        return equals(V) ? 0 : equals(D) || equals(J) ? 2 : 1;
    }

    public TypeId array() {
        return new TypeId("[" + descriptor);
    }

    public static TypeId read(RandomInput in, ReadContext context) {
        return new TypeId(context.string(in.readInt()));
    }

    static TypeId[] readArray(RandomInput in, ReadContext context, int size) {
        TypeId[] out = new TypeId[size];
        for (int i = 0; i < size; i++) {
            out[i] = read(in, context);
        }
        return out;
    }

    static TypeId readInArray(RandomInput in, ReadContext context, int index) {
        in.addPosition((long) index * SIZE);
        return read(in, context);
    }

    public void collectData(DataCollector data) {
        data.add(descriptor);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getStringIndex(descriptor));
    }

    static void writeSection(WriteContext context, RandomOutput out, TypeId[] types) {
        for (TypeId value : types) {
            value.write(context, out);
        }
    }

    @Override
    public String toString() {
        return descriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TypeId tobj
                && Objects.equals(descriptor, tobj.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor);
    }

    @Override
    public TypeId mutate() {
        return new TypeId(descriptor);
    }
}
