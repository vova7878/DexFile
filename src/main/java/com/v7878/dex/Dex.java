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

import static com.v7878.misc.Math.roundUp;

import com.v7878.dex.EncodedValue.ArrayValue;
import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.RandomIO;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;
import com.v7878.misc.Checks;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class Dex extends MutableList<ClassDef> {

    public Dex(int initialCapacity) {
        super(initialCapacity);
    }

    public Dex(ClassDef... class_defs) {
        super(class_defs);
    }

    public Dex(Collection<ClassDef> class_defs) {
        super(class_defs);
    }

    @Override
    protected ClassDef check(ClassDef class_def) {
        return Objects.requireNonNull(class_def,
                "Dex can`t contain null class def");
    }

    public static Dex read(RandomInput src) {
        return read(src, null);
    }


    public static Dex read(RandomInput src, int[] class_def_ids) {
        return read(src, DexOptions.defaultOptions(), class_def_ids);
    }

    public static Dex read(RandomInput src, DexOptions options, int[] class_def_ids) {
        RandomInput in = src.position() == 0 ? src : src.slice();

        FileMap map = FileMap.read(in, options);

        if (class_def_ids == null) {
            class_def_ids = new int[map.class_defs_size];
            for (int i = 0; i < map.class_defs_size; i++) {
                class_def_ids[i] = i;
            }
        } else {
            //TODO: check unique
            for (int id : class_def_ids) {
                Checks.checkIndex(id, map.class_defs_size);
            }
        }

        if (class_def_ids.length == 0) {
            return new Dex();
        }

        ReadContextImpl context = new ReadContextImpl(options);

        String[] strings = new String[map.string_ids_size];
        if (map.string_ids_size != 0) {
            RandomInput in2 = in.duplicate(map.string_ids_off);
            for (int i = 0; i < map.string_ids_size; i++) {
                strings[i] = StringId.read(in2);
            }
        }
        context.setStrings(strings);

        TypeId[] types = new TypeId[map.type_ids_size];
        if (map.type_ids_size != 0) {
            RandomInput in2 = in.duplicate(map.type_ids_off);
            for (int i = 0; i < map.type_ids_size; i++) {
                types[i] = TypeId.read(in2, context);
            }
        }
        context.setTypes(types);

        ProtoId[] protos = new ProtoId[map.proto_ids_size];
        if (map.proto_ids_size != 0) {
            RandomInput in2 = in.duplicate(map.proto_ids_off);
            for (int i = 0; i < map.proto_ids_size; i++) {
                protos[i] = ProtoId.read(in2, context);
            }
        }
        context.setProtos(protos);

        FieldId[] fields = new FieldId[map.field_ids_size];
        if (map.field_ids_size != 0) {
            RandomInput in2 = in.duplicate(map.field_ids_off);
            for (int i = 0; i < map.field_ids_size; i++) {
                fields[i] = FieldId.read(in2, context);
            }
        }
        context.setFields(fields);

        MethodId[] methods = new MethodId[map.method_ids_size];
        if (map.method_ids_size != 0) {
            RandomInput in2 = in.duplicate(map.method_ids_off);
            for (int i = 0; i < map.method_ids_size; i++) {
                methods[i] = MethodId.read(in2, context);
            }
        }
        context.setMethods(methods);

        MethodHandleItem[] method_handles = new MethodHandleItem[map.method_handles_size];
        if (map.method_handles_size != 0) {
            RandomInput in2 = in.duplicate(map.method_handles_off);
            for (int i = 0; i < map.method_handles_size; i++) {
                method_handles[i] = MethodHandleItem.read(in2, context);
            }
        }
        context.setMethodHandles(method_handles);

        CallSiteId[] call_sites = new CallSiteId[map.call_site_ids_size];
        if (map.call_site_ids_size != 0) {
            RandomInput in2 = in.duplicate(map.call_site_ids_off);
            for (int i = 0; i < map.call_site_ids_size; i++) {
                call_sites[i] = CallSiteId.read(in2, context);
            }
        }
        context.setCallSites(call_sites);

        ClassDef[] class_defs = new ClassDef[class_def_ids.length];
        for (int i = 0; i < class_def_ids.length; i++) {
            int offset = map.class_defs_off + ClassDef.SIZE * class_def_ids[i];
            RandomInput in2 = in.duplicate(offset);
            class_defs[i] = ClassDef.read(in2, context);
        }
        return new Dex(class_defs);
    }

    public void collectData(DataCollector data) {
        for (ClassDef tmp : this) {
            data.add(tmp);
        }
    }

    public void write(RandomIO dst) {
        write(dst, DexOptions.defaultOptions());
    }

    public void write(RandomIO dst, DexOptions options) {
        RandomIO out = dst.position() == 0 ? dst : dst.slice();

        DataSet data = new DataSet();
        collectData(data);

        WriteContextImpl context = new WriteContextImpl(data, options);

        FileMap map = new FileMap();

        int offset = FileMap.HEADER_SIZE;

        map.string_ids_off = offset;
        map.string_ids_size = context.getStringsCount();
        offset += map.string_ids_size * StringId.SIZE;

        map.type_ids_off = offset;
        map.type_ids_size = context.getTypesCount();
        offset += map.type_ids_size * TypeId.SIZE;

        map.proto_ids_off = offset;
        map.proto_ids_size = context.getProtosCount();
        offset += map.proto_ids_size * ProtoId.SIZE;

        map.field_ids_off = offset;
        map.field_ids_size = context.getFieldsCount();
        offset += map.field_ids_size * FieldId.SIZE;

        map.method_ids_off = offset;
        map.method_ids_size = context.getMethodsCount();
        offset += map.method_ids_size * MethodId.SIZE;

        map.class_defs_off = offset;
        map.class_defs_size = context.getClassDefsCount();
        offset += map.class_defs_size * ClassDef.SIZE;

        map.call_site_ids_off = offset;
        map.call_site_ids_size = context.getCallSitesCount();
        offset += map.call_site_ids_size * CallSiteId.SIZE;

        map.method_handles_off = offset;
        map.method_handles_size = context.getMethodHandlesCount();
        offset += map.method_handles_size * MethodHandleItem.SIZE;

        // writing
        //TODO: all sections
        map.data_off = offset;

        map.string_data_items_off = offset;
        map.string_data_items_size = map.string_ids_size;

        RandomOutput data_out = out.duplicate(offset);

        out.position(map.string_ids_off);
        context.stringsStream().forEachOrdered(value -> StringId.write(value, context, out, data_out));
        offset = (int) data_out.position();

        TypeList[] lists = data.getTypeLists();
        if (lists.length != 0) {
            offset = roundUp(offset, TypeList.ALIGNMENT);
            map.type_lists_off = offset;
            map.type_lists_size = lists.length;
            for (TypeList tmp : lists) {
                offset = roundUp(offset, TypeList.ALIGNMENT);
                data_out.position(offset);
                tmp.write(context, data_out);
                context.addTypeList(tmp, offset);
                offset = (int) data_out.position();
            }
        }

        AnnotationItem[] annotations = data.getAnnotations();
        map.annotations_off = offset;
        map.annotations_size = annotations.length;
        for (AnnotationItem tmp : annotations) {
            tmp.write(context, data_out);
            context.addAnnotation(tmp, offset);
            offset = (int) data_out.position();
        }

        AnnotationSet[] annotation_sets = data.getAnnotationSets();
        if (annotation_sets.length != 0) {
            offset = roundUp(offset, AnnotationSet.ALIGNMENT);
            map.annotation_sets_off = offset;
            map.annotation_sets_size = annotation_sets.length;
            data_out.position(offset);

            for (AnnotationSet tmp : annotation_sets) {
                tmp.write(context, data_out);
                context.addAnnotationSet(tmp, offset);
                offset = (int) data_out.position();
            }
        }

        AnnotationSetList[] annotation_set_lists = data.getAnnotationSetLists();
        if (annotation_set_lists.length != 0) {
            offset = roundUp(offset, AnnotationSet.ALIGNMENT);
            map.annotation_set_refs_off = offset;
            map.annotation_set_refs_size = annotation_set_lists.length;
            data_out.position(offset);

            for (AnnotationSetList tmp : annotation_set_lists) {
                tmp.write(context, data_out);
                context.addAnnotationSetList(tmp, offset);
                offset = (int) data_out.position();
            }
        }

        CodeItem[] code_items = data.getCodeItems();
        if (code_items.length != 0) {
            offset = roundUp(offset, CodeItem.ALIGNMENT);
            map.code_items_off = offset;
            map.code_items_size = code_items.length;
            for (CodeItem tmp : code_items) {
                offset = roundUp(offset, CodeItem.ALIGNMENT);
                data_out.position(offset);
                tmp.write(context, data_out);
                context.addCodeItem(tmp, offset);
                offset = (int) data_out.position();
            }
        }

        ClassData[] class_data_items = data.getClassDataItems();
        map.class_data_items_off = offset;
        map.class_data_items_size = class_data_items.length;
        for (ClassData tmp : class_data_items) {
            tmp.write(context, data_out);
            context.addClassData(tmp, offset);
            offset = (int) data_out.position();
        }

        //TODO: delete duplicates
        Map<ClassDef, AnnotationsDirectory> annotations_directories
                = data.getAnnotationsDirectories();
        if (!annotations_directories.isEmpty()) {
            offset = roundUp(offset, AnnotationsDirectory.ALIGNMENT);
            map.annotations_directories_off = offset;
            map.annotations_directories_size = 0;
            for (Map.Entry<ClassDef, AnnotationsDirectory> tmp
                    : annotations_directories.entrySet()) {
                AnnotationsDirectory ad = tmp.getValue();
                if (!ad.isEmpty()) {
                    map.annotations_directories_size++;
                    offset = roundUp(offset, AnnotationsDirectory.ALIGNMENT);
                    data_out.position(offset);
                    ad.write(context, data_out);
                    context.addAnnotationsDirectory(tmp.getKey(), offset);
                    offset = (int) data_out.position();
                } else {
                    context.addAnnotationsDirectory(tmp.getKey(), 0);
                }
            }
        }

        ArrayValue[] array_values = data.getArrayValues();
        map.encoded_arrays_off = offset;
        map.encoded_arrays_size = array_values.length;
        for (ArrayValue tmp : array_values) {
            tmp.writeData(context, data_out);
            context.addArrayValue(tmp, offset);
            offset = (int) data_out.position();
        }

        offset = roundUp(offset, FileMap.MAP_ALIGNMENT);
        data_out.position(offset);
        map.writeMap(data_out);
        offset = (int) data_out.position();

        map.data_size = offset - map.data_off;

        int file_size = offset;

        out.position(map.type_ids_off);
        context.typesStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.field_ids_off);
        context.fieldsStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.proto_ids_off);
        context.protosStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.method_ids_off);
        context.methodsStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.class_defs_off);
        context.classDefsStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.call_site_ids_off);
        context.callSitesStream().forEachOrdered((value) -> value.write(context, out));

        out.position(map.method_handles_off);
        context.methodHandlesStream().forEachOrdered((value) -> value.write(context, out));

        map.writeHeader(out, options, file_size);
    }

    public ClassDef findClassDef(TypeId type) {
        for (ClassDef tmp : this) {
            if (tmp.getType().equals(type)) {
                return tmp;
            }
        }
        return null;
    }

    public byte[] compile(DexOptions options) {
        ByteArrayIO out = new ByteArrayIO();
        write(out, options);
        return out.toByteArray();
    }

    public byte[] compile() {
        return compile(DexOptions.defaultOptions());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dex && super.equals(obj);
    }

    // hashcode from super

    @Override
    public Dex mutate() {
        return new Dex(this);
    }
}
