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
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class ClassData implements Mutable {

    private MutableList<EncodedField> static_fields;
    private MutableList<EncodedField> instance_fields;
    private MutableList<EncodedMethod> direct_methods;
    private MutableList<EncodedMethod> virtual_methods;

    public ClassData(Collection<EncodedField> static_fields,
                     Collection<EncodedField> instance_fields,
                     Collection<EncodedMethod> direct_methods,
                     Collection<EncodedMethod> virtual_methods) {
        setStaticFields(static_fields);
        setInstanceFields(instance_fields);
        setDirectMethods(direct_methods);
        setVirtualMethods(virtual_methods);
    }

    public void setStaticFields(Collection<EncodedField> static_fields) {
        this.static_fields = static_fields == null
                ? MutableList.empty() : new MutableList<>(static_fields);
    }

    public MutableList<EncodedField> getStaticFields() {
        return static_fields;
    }

    public void setInstanceFields(Collection<EncodedField> instance_fields) {
        this.instance_fields = instance_fields == null
                ? MutableList.empty() : new MutableList<>(instance_fields);
    }

    public MutableList<EncodedField> getInstanceFields() {
        return instance_fields;
    }

    private EncodedField findField(boolean static_, FieldId field) {
        var fields = static_ ? static_fields : instance_fields;
        for (var tmp : fields) {
            if (tmp.getField().equals(field)) {
                return tmp;
            }
        }
        return null;
    }

    public EncodedField findStaticField(FieldId field) {
        return findField(true, field);
    }

    public EncodedField findInstanceField(FieldId field) {
        return findField(false, field);
    }

    public void setDirectMethods(Collection<EncodedMethod> direct_methods) {
        this.direct_methods = direct_methods == null
                ? MutableList.empty() : new MutableList<>(direct_methods);
    }

    public MutableList<EncodedMethod> getDirectMethods() {
        return direct_methods;
    }

    public void setVirtualMethods(Collection<EncodedMethod> virtual_methods) {
        this.virtual_methods = virtual_methods == null
                ? MutableList.empty() : new MutableList<>(virtual_methods);
    }

    public MutableList<EncodedMethod> getVirtualMethods() {
        return virtual_methods;
    }

    private EncodedMethod findMethod(boolean direct, MethodId method) {
        var methods = direct ? direct_methods : virtual_methods;
        for (var tmp : methods) {
            if (tmp.getMethod().equals(method)) {
                return tmp;
            }
        }
        return null;
    }

    public EncodedMethod findDirectMethod(MethodId method) {
        return findMethod(true, method);
    }

    public EncodedMethod findVirtualMethod(MethodId method) {
        return findMethod(false, method);
    }

    public boolean isEmpty() {
        return static_fields.isEmpty() && instance_fields.isEmpty()
                && direct_methods.isEmpty() && virtual_methods.isEmpty();
    }

    public static ClassData empty() {
        return new ClassData(null, null, null, null);
    }

    public static ClassData read(RandomInput in, ReadContext context, ArrayValue static_values,
                                 Map<FieldId, AnnotationSet> annotated_fields,
                                 Map<MethodId, AnnotationSet> annotated_methods,
                                 Map<MethodId, AnnotationSetList> annotated_parameters) {
        ClassData out = empty();
        int static_fields_size = in.readULeb128();
        int instance_fields_size = in.readULeb128();
        int direct_methods_size = in.readULeb128();
        int virtual_methods_size = in.readULeb128();
        out.static_fields = EncodedField.readArray(in, context,
                static_fields_size, annotated_fields);
        for (int i = 0; i < static_values.size(); i++) {
            out.static_fields.get(i).setValue(static_values.get(i));
        }
        out.instance_fields = EncodedField.readArray(in, context,
                instance_fields_size, annotated_fields);
        out.direct_methods = EncodedMethod.readArray(in, context,
                direct_methods_size, annotated_methods, annotated_parameters);
        out.virtual_methods = EncodedMethod.readArray(in, context,
                virtual_methods_size, annotated_methods, annotated_parameters);
        return out;
    }

    public void collectData(DataCollector data) {
        for (EncodedField tmp : static_fields) {
            data.fill(tmp);
        }
        for (EncodedField tmp : instance_fields) {
            data.fill(tmp);
        }
        for (EncodedMethod tmp : direct_methods) {
            data.fill(tmp);
        }
        for (EncodedMethod tmp : virtual_methods) {
            data.fill(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(static_fields.size());
        out.writeULeb128(instance_fields.size());
        out.writeULeb128(direct_methods.size());
        out.writeULeb128(virtual_methods.size());

        EncodedField.writeArray(true, context, out, static_fields);
        EncodedField.writeArray(false, context, out, instance_fields);

        EncodedMethod.writeArray(true, context, out, direct_methods);
        EncodedMethod.writeArray(false, context, out, virtual_methods);
    }

    static void writeSection(WriteContextImpl context, FileMap map,
                             RandomOutput out, ClassData[] class_data_items) {
        if (class_data_items.length != 0) {
            map.class_data_items_off = (int) out.position();
            map.class_data_items_size = class_data_items.length;
        }
        for (ClassData tmp : class_data_items) {
            int start = (int) out.position();
            tmp.write(context, out);
            context.addClassData(tmp, start);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ClassData cdobj
                && Objects.equals(static_fields, cdobj.static_fields)
                && Objects.equals(instance_fields, cdobj.instance_fields)
                && Objects.equals(direct_methods, cdobj.direct_methods)
                && Objects.equals(virtual_methods, cdobj.virtual_methods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(static_fields, instance_fields, direct_methods, virtual_methods);
    }

    @Override
    public ClassData mutate() {
        return new ClassData(static_fields, instance_fields, direct_methods, virtual_methods);
    }
}
