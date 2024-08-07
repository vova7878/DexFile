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

import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public final class EncodedField implements Mutable {

    public static final Comparator<EncodedField> COMPARATOR = (a, b) -> {
        int out = FieldId.COMPARATOR.compare(a.field, b.field);
        if (out != 0) {
            return out;
        }

        // a.field == b.field, but a != b
        throw new IllegalStateException(
                "can`t compare encoded fields with same field id" + a + " " + b);
    };

    private FieldId field;
    private int access_flags;
    private int hiddenapi_flag;
    private AnnotationSet annotations;
    private EncodedValue value;

    public EncodedField(FieldId field, int access_flags, int hiddenapi_flag,
                        Collection<AnnotationItem> annotations, EncodedValue value) {
        setField(field);
        setAccessFlags(access_flags);
        setHiddenapiFlag(hiddenapi_flag);
        setAnnotations(annotations);
        setValue(value);
    }

    public EncodedField(FieldId field, int access_flags,
                        Collection<AnnotationItem> annotations, EncodedValue value) {
        this(field, access_flags, 0, annotations, value);
    }

    public EncodedField(FieldId field, int access_flags,
                        AnnotationSet annotations) {
        this(field, access_flags, annotations, null);
    }

    public EncodedField(FieldId field, int access_flags) {
        this(field, access_flags, null);
    }

    public void setField(FieldId field) {
        this.field = Objects.requireNonNull(field,
                "field can`t be null").mutate();
    }

    public FieldId getField() {
        return field;
    }

    public void setAccessFlags(int access_flags) {
        this.access_flags = access_flags;
    }

    public int getAccessFlags() {
        return access_flags;
    }

    public void setHiddenapiFlag(int hiddenapi_flag) {
        this.hiddenapi_flag = hiddenapi_flag;
    }

    public int getHiddenapiFlag() {
        return hiddenapi_flag;
    }

    public boolean isStatic() {
        return (access_flags & ACC_STATIC) != 0;
    }

    public void setAnnotations(Collection<AnnotationItem> annotations) {
        this.annotations = annotations == null
                ? AnnotationSet.empty() : new AnnotationSet(annotations);
    }

    public AnnotationSet getAnnotations() {
        return annotations;
    }

    public void setValue(EncodedValue value) {
        this.value = value == null ? null : value.mutate();
    }

    public EncodedValue getValue() {
        return value == null ? EncodedValue.defaultValue(field.getType()) : value;
    }

    public boolean hasValue() {
        return value != null;
    }

    public static EncodedField read(RandomInput in, FieldId field,
                                    Map<FieldId, AnnotationSet> annotated_fields) {
        return new EncodedField(field, in.readULeb128(), annotated_fields.get(field));
    }

    public static MutableList<EncodedField> readArray(RandomInput in,
                                                      ReadContext context, int size,
                                                      Map<FieldId, AnnotationSet> annotated_fields) {
        MutableList<EncodedField> out = MutableList.empty();
        int index = 0;
        for (int i = 0; i < size; i++) {
            index += in.readULeb128();
            out.add(read(in, context.field(index), annotated_fields));
        }
        return out;
    }

    public void collectData(DataCollector data) {
        data.add(field);
        if (!annotations.isEmpty()) {
            data.add(annotations);
        }
        if (value != null && !value.isDefault()) {
            data.fill(value);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(access_flags);
    }

    private static void check(boolean is_static_list, EncodedField encoded_field) {
        if (is_static_list) {
            if (!encoded_field.isStatic()) {
                throw new IllegalStateException("field must be static: " + encoded_field);
            }
        } else {
            if (encoded_field.isStatic()) {
                throw new IllegalStateException("field must not be static: " + encoded_field);
            }
            if (encoded_field.hasValue()) {
                throw new IllegalStateException("instance field can`t have value: " + encoded_field);
            }
        }
    }

    public static void writeArray(boolean is_static_list,
                                  WriteContext context, RandomOutput out,
                                  EncodedField[] encoded_fields) {
        Arrays.sort(encoded_fields, COMPARATOR);
        int fieldIndex = 0;
        for (EncodedField tmp : encoded_fields) {
            check(is_static_list, tmp);
            int diff = context.getFieldIndex(tmp.field) - fieldIndex;
            fieldIndex += diff;
            out.writeULeb128(diff);
            tmp.write(context, out);
        }
    }

    public static void writeArray(boolean is_static_list,
                                  WriteContext context, RandomOutput out,
                                  Collection<EncodedField> encoded_fields) {
        writeArray(is_static_list, context, out, encoded_fields.toArray(new EncodedField[0]));
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        String flags = DexModifier.printFieldFlags(access_flags);
        out.append(flags);
        if (!flags.isEmpty()) {
            out.append(' ');
        }
        if (hiddenapi_flag != 0) {
            out.append(HiddenApi.printFlag(hiddenapi_flag)).append(' ');
        }
        out.append(field);
        if (value != null) {
            out.append(" = ").append(value);
        }
        return out.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedField efobj
                && access_flags == efobj.access_flags
                && hiddenapi_flag == efobj.hiddenapi_flag
                && Objects.equals(field, efobj.field)
                && Objects.equals(annotations, efobj.annotations)
                //Note: getValue() method used instead value field
                && Objects.equals(getValue(), efobj.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, access_flags, hiddenapi_flag, annotations, value);
    }

    @Override
    public EncodedField mutate() {
        return new EncodedField(field, access_flags, hiddenapi_flag, annotations, value);
    }
}
