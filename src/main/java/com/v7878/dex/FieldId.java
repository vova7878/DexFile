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

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;

public final class FieldId extends FieldOrMethodId {

    public static final int SIZE = 0x08;

    public static final Comparator<FieldId> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.getDeclaringClass(), b.getDeclaringClass());
        if (out != 0) {
            return out;
        }

        out = StringId.COMPARATOR.compare(a.getName(), b.getName());
        if (out != 0) {
            return out;
        }

        return TypeId.COMPARATOR.compare(a.type, b.type);
    };

    public static FieldId of(Field field) {
        Objects.requireNonNull(field, "trying to get FieldId of null");
        return new FieldId(TypeId.of(field.getDeclaringClass()),
                TypeId.of(field.getType()), field.getName());
    }

    public static FieldId of(Enum<?> e) {
        Objects.requireNonNull(e, "trying to get FieldId of null");
        TypeId declaring_class = TypeId.of(e.getDeclaringClass());
        return new FieldId(declaring_class, declaring_class, e.name());
    }

    private TypeId type;

    public FieldId(TypeId declaring_class, TypeId type, String name) {
        super(declaring_class, name);
        setType(type);
    }

    public void setType(TypeId type) {
        this.type = Objects.requireNonNull(type,
                "type can`t be null").mutate();
    }

    public TypeId getType() {
        return type;
    }

    public static FieldId read(RandomInput in, ReadContext context) {
        return new FieldId(
                context.type(in.readUnsignedShort()),
                context.type(in.readUnsignedShort()),
                context.string(in.readInt())
        );
    }

    static FieldId[] readArray(RandomInput in, ReadContext context, int size) {
        FieldId[] out = new FieldId[size];
        for (int i = 0; i < size; i++) {
            out[i] = read(in, context);
        }
        return out;
    }

    static FieldId readInArray(RandomInput in, ReadContext context, int index) {
        in.addPosition((long) index * SIZE);
        return read(in, context);
    }

    @Override
    public void collectData(DataCollector data) {
        data.add(type);
        super.collectData(data);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeShort(context.getTypeIndex(getDeclaringClass()));
        out.writeShort(context.getTypeIndex(type));
        out.writeInt(context.getStringIndex(getName()));
    }

    static void writeSection(WriteContext context, RandomOutput out, FieldId[] fields) {
        for (FieldId value : fields) {
            value.write(context, out);
        }
    }

    @Override
    public String toString() {
        return getDeclaringClass() + "." + getName() + ":" + type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!super.equals(obj)) return false;
        return obj instanceof FieldId fobj
                && Objects.equals(type, fobj.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    public FieldId mutate() {
        return new FieldId(getDeclaringClass(), type, getName());
    }
}
