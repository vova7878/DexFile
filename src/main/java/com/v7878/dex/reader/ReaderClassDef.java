package com.v7878.dex.reader;

import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.base.BaseClassDef;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.reader.raw.AnnotationDirectory;
import com.v7878.dex.reader.util.ChainedSet;
import com.v7878.dex.reader.util.FixedSizeSet;
import com.v7878.dex.reader.util.OptionalUtils;
import com.v7878.dex.reader.util.ValueContainer;

import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public class ReaderClassDef extends BaseClassDef {
    public static final int ITEM_SIZE = 32;

    public static final int TYPE_OFFSET = 0;
    public static final int ACCESS_FLAGS_OFFSET = 4;
    public static final int SUPERCLASS_OFFSET = 8;
    public static final int INTERFACES_OFFSET = 12;
    public static final int SOURCE_FILE_OFFSET = 16;
    public static final int ANNOTATIONS_OFFSET = 20;
    public static final int CLASS_DATA_OFFSET = 24;
    public static final int STATIC_VALUES_OFFSET = 28;

    private final ReaderDex dexfile;
    private final int offset;

    private final Set<ReaderFieldDef> static_fields;
    private final Set<ReaderFieldDef> instance_fields;

    public ReaderClassDef(ReaderDex dexfile, int index, int class_defs_off) {
        this.dexfile = dexfile;
        this.offset = class_defs_off + index * ITEM_SIZE;
        //TODO: maybe cache?
        {
            int class_data_offset = dexfile.mainAt(offset + CLASS_DATA_OFFSET).readSmallUInt();
            if (class_data_offset == NO_OFFSET) {
                static_fields = Set.of();
                instance_fields = Set.of();
                return;
            }
            RandomInput class_data = dexfile.dataAt(class_data_offset);
            IntSupplier hiddenapi_iterator = dexfile.getHiddenApiIterator(index);
            int static_fields_size = class_data.readSmallULeb128();
            int instance_fields_size = class_data.readSmallULeb128();
            int direct_methods_size = class_data.readSmallULeb128();
            int virtual_methods_size = class_data.readSmallULeb128();
            static_fields = FixedSizeSet.ofList(ReaderFieldDef.readList(dexfile, this,
                    class_data, hiddenapi_iterator, static_fields_size, true));
            instance_fields = FixedSizeSet.ofList(ReaderFieldDef.readList(dexfile, this,
                    class_data, hiddenapi_iterator, instance_fields_size, false));
            // TODO: methods
        }
    }

    private ReaderTypeId type;
    private Integer access_flags;
    private ValueContainer<ReaderTypeId> superclass;
    private Set<ReaderTypeId> interfaces;
    private ValueContainer<String> source_file;
    private AnnotationDirectory annotations;
    private List<? extends EncodedValue> static_values;

    @Override
    public TypeId getType() {
        if (type != null) return type;
        return type = dexfile.getTypeId(dexfile.mainAt(
                offset + TYPE_OFFSET).readSmallUInt());
    }

    @Override
    public int getAccessFlags() {
        if (access_flags != null) return access_flags;
        return access_flags = dexfile.mainAt(
                offset + ACCESS_FLAGS_OFFSET).readSmallUInt();
    }

    @Override
    public TypeId getSuperclass() {
        if (superclass != null) return superclass.value();
        return (superclass = ValueContainer.of(OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + SUPERCLASS_OFFSET).readSmallUInt(),
                NO_INDEX, dexfile::getTypeId, null))).value();
    }

    @Override
    public Set<? extends TypeId> getInterfaces() {
        if (interfaces != null) return interfaces;
        return interfaces = FixedSizeSet.ofList(OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + INTERFACES_OFFSET).readSmallUInt(),
                NO_OFFSET, dexfile::getTypeList, List.of()));
    }

    @Override
    public String getSourceFile() {
        if (source_file != null) return source_file.value();
        return (source_file = ValueContainer.of(OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + SOURCE_FILE_OFFSET).readSmallUInt(),
                NO_INDEX, dexfile::getString, null))).value();
    }

    public AnnotationDirectory getAnnotationDirectory() {
        if (annotations != null) return annotations;
        return annotations = OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + ANNOTATIONS_OFFSET).readSmallUInt(),
                NO_OFFSET, dexfile::getAnnotationDirectory, AnnotationDirectory.EMPTY);
    }

    @Override
    public Set<? extends Annotation> getAnnotations() {
        return getAnnotationDirectory().getClassAnnotations();
    }

    public List<? extends EncodedValue> getStaticValues() {
        if (static_values != null) return static_values;
        return static_values = OptionalUtils.getOrDefault(
                dexfile.mainAt(offset + STATIC_VALUES_OFFSET).readSmallUInt(),
                NO_OFFSET, i -> dexfile.getEncodedArray(i).getValue(), List.of());
    }

    @Override
    public Set<? extends FieldDef> getStaticFields() {
        return static_fields;
    }

    @Override
    public Set<? extends FieldDef> getInstanceFields() {
        return instance_fields;
    }

    @Override
    public Set<? extends FieldDef> getFields() {
        return new ChainedSet<>(getStaticFields(), getInstanceFields());
    }

    @Override
    public Set<? extends MethodDef> getDirectMethods() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<? extends MethodDef> getVirtualMethods() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<? extends MethodDef> getMethods() {
        return new ChainedSet<>(getDirectMethods(), getVirtualMethods());
    }
}
