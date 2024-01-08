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

public final class MethodHandleItem implements Mutable {

    public static final int SIZE = 0x08;

    public static final Comparator<MethodHandleItem> COMPARATOR = (a, b) -> {
        int out = Integer.compare(a.type.value, b.type.value);
        if (out != 0) {
            return out;
        }

        // now a.type == b.type
        if (a.type.isMethodAccess()) {
            return MethodId.COMPARATOR.compare((MethodId) a.field_or_method,
                    (MethodId) b.field_or_method);
        } else {
            return FieldId.COMPARATOR.compare((FieldId) a.field_or_method,
                    (FieldId) b.field_or_method);
        }
    };

    //TODO: move values to DexConstants
    public enum MethodHandleType {
        STATIC_PUT(0x00, false),
        STATIC_GET(0x01, false),
        INSTANCE_PUT(0x02, false),
        INSTANCE_GET(0x03, false),
        INVOKE_STATIC(0x04, true),
        INVOKE_INSTANCE(0x05, true),
        INVOKE_CONSTRUCTOR(0x06, true),
        INVOKE_DIRECT(0x07, true),
        INVOKE_INTERFACE(0x08, true);

        private final int value;
        private final boolean isMethod;

        MethodHandleType(int value, boolean isMethod) {
            this.value = value;
            this.isMethod = isMethod;
        }

        public boolean isMethodAccess() {
            return isMethod;
        }

        public boolean isFieldAccess() {
            return !isMethod;
        }

        public static MethodHandleType of(int int_type) {
            for (MethodHandleType type : values()) {
                if (int_type == type.value) {
                    return type;
                }
            }
            throw new IllegalStateException("unknown method handle type: " + int_type);
        }
    }

    private MethodHandleType type;
    private FieldOrMethodId field_or_method;

    public MethodHandleItem(MethodHandleType type, FieldOrMethodId field_or_method) {
        setType(type);
        setFieldOrMethod(field_or_method);
    }

    public void setType(MethodHandleType type) {
        this.type = Objects.requireNonNull(type, "method handle type can`n be null");
    }

    public MethodHandleType getType() {
        return type;
    }

    public void setFieldOrMethod(FieldOrMethodId field_or_method) {
        this.field_or_method = Objects.requireNonNull(field_or_method,
                "field_or_method can`t be null").mutate();
    }

    public FieldOrMethodId getFieldOrMethod() {
        return field_or_method;
    }

    public static MethodHandleItem read(RandomInput in, ReadContext context) {
        MethodHandleType type = MethodHandleType.of(in.readUnsignedShort());
        in.addPosition(2); //unused
        int field_or_method_id = in.readUnsignedShort();
        in.addPosition(2); //unused
        FieldOrMethodId field_or_method = type.isMethodAccess()
                ? context.method(field_or_method_id)
                : context.field(field_or_method_id);
        return new MethodHandleItem(type, field_or_method);
    }

    public void collectData(DataCollector data) {
        if (type.isMethodAccess()) {
            data.add((MethodId) field_or_method);
        } else {
            data.add((FieldId) field_or_method);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeShort(type.value);
        out.writeShort(0);
        out.writeShort(type.isMethodAccess()
                ? context.getMethodIndex((MethodId) field_or_method)
                : context.getFieldIndex((FieldId) field_or_method));
        out.writeShort(0);
    }

    @Override
    public String toString() {
        return "MethodHandle{" + "type = " + type + "; " + field_or_method + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodHandleItem) {
            MethodHandleItem mhobj = (MethodHandleItem) obj;
            return Objects.equals(type, mhobj.type)
                    && Objects.equals(field_or_method, mhobj.field_or_method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, field_or_method);
    }

    @Override
    public MethodHandleItem mutate() {
        return new MethodHandleItem(type, field_or_method);
    }
}
