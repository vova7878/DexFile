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
import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.RandomIO;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;
import com.v7878.misc.Checks;

import java.util.Collection;
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
        return read(src, ReadOptions.defaultOptions(), class_def_ids);
    }

    public static Dex read(RandomInput src, ReadOptions options, int[] class_def_ids) {
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

        return readInternal(in, options, class_def_ids, map);
    }

    private static Dex readInternal(RandomInput in, ReadOptions options,
                                    int[] class_def_ids, FileMap map) {

        ReadContextImpl context = new ReadContextImpl(options);

        context.setStrings(StringId.readArray(in.duplicate(map.string_ids_off), map.string_ids_size));
        context.setTypes(TypeId.readArray(in.duplicate(map.type_ids_off), context, map.type_ids_size));
        context.setProtos(ProtoId.readArray(in.duplicate(map.proto_ids_off), context, map.proto_ids_size));
        context.setFields(FieldId.readArray(in.duplicate(map.field_ids_off), context, map.field_ids_size));
        context.setMethods(MethodId.readArray(in.duplicate(map.method_ids_off), context, map.method_ids_size));
        context.setMethodHandles(MethodHandleItem.readArray(in.duplicate(map.method_handles_off), context, map.method_handles_size));
        context.setCallSites(CallSiteId.readArray(in.duplicate(map.call_site_ids_off), context, map.call_site_ids_size));
        ClassDef[] class_defs = ClassDef.readArray(in.duplicate(map.class_defs_off), context, class_def_ids);

        return new Dex(class_defs);
    }

    public void collectData(DataCollector data) {
        for (ClassDef tmp : this) {
            data.add(tmp);
        }
    }

    public void write(RandomIO dst) {
        write(dst, WriteOptions.defaultOptions());
    }

    public void write(RandomIO dst, WriteOptions options) {
        RandomIO out = dst.position() == 0 ? dst : dst.slice();
        writeInternal(out, options);
    }

    private void writeInternal(RandomIO out, WriteOptions options) {
        DataSet sections = new DataSet();
        collectData(sections);

        WriteContextImpl context = new WriteContextImpl(sections, options);

        FileMap map = new FileMap();
        map.computeHeaderOffsets(context);

        RandomOutput data = out.duplicate(map.data_off);

        TypeList.writeSection(context, map, data, sections.getTypeLists());
        AnnotationItem.writeSection(context, map, data, sections.getAnnotations());
        AnnotationSet.writeSection(context, map, data, sections.getAnnotationSets());
        AnnotationSetList.writeSection(context, map, data, sections.getAnnotationSetLists());
        AnnotationsDirectory.writeSection(context, map, data, sections.getAnnotationsDirectories());
        //TODO: DebugInfo.writeSection(context, map, data, sections.getDebugInfos());
        CodeItem.writeSection(context, map, data, sections.getCodeItems());
        ClassData.writeSection(context, map, data, sections.getClassDataItems());
        ArrayValue.writeSection(context, map, data, sections.getArrayValues());
        //TODO: HiddenApi.writeSection(context, map, data, ...);

        StringId.writeSection(context, map, out.duplicate(map.string_ids_off), data, context.strings());

        map.writeMap(data);

        int file_size = (int) data.position();

        map.data_size = file_size - map.data_off;

        TypeId.writeSection(context, out.duplicate(map.type_ids_off), context.types());
        FieldId.writeSection(context, out.duplicate(map.field_ids_off), context.fields());
        ProtoId.writeSection(context, out.duplicate(map.proto_ids_off), context.protos());
        MethodId.writeSection(context, out.duplicate(map.method_ids_off), context.methods());
        ClassDef.writeSection(context, out.duplicate(map.class_defs_off), context.classDefs());
        CallSiteId.writeSection(context, out.duplicate(map.call_site_ids_off), context.callSites());
        MethodHandleItem.writeSection(context, out.duplicate(map.method_handles_off), context.methodHandles());

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

    public byte[] compile(WriteOptions options) {
        ByteArrayIO out = new ByteArrayIO();
        write(out, options);
        return out.toByteArray();
    }

    public byte[] compile() {
        return compile(WriteOptions.defaultOptions());
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
