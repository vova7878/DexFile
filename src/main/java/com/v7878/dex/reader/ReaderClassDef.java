package com.v7878.dex.reader;

import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;

import com.v7878.dex.base.BaseClassDef;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.reader.raw.AnnotationDirectory;
import com.v7878.dex.reader.util.FixedSizeSet;
import com.v7878.dex.reader.util.OptionalUtils;
import com.v7878.dex.reader.util.ValueContainer;

import java.util.List;
import java.util.Set;

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
    private final int index;
    private final int offset;

    public ReaderClassDef(ReaderDex dexfile, int index, int class_defs_off) {
        this.dexfile = dexfile;
        this.index = index;
        this.offset = class_defs_off + index * ITEM_SIZE;
    }

    private ReaderTypeId type;
    private Integer access_flags;
    private ValueContainer<ReaderTypeId> superclass;
    private Set<ReaderTypeId> interfaces;
    private ValueContainer<String> source_file;
    private AnnotationDirectory annotations;

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

    @Override
    public Set<? extends FieldDef> getFields() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<? extends FieldDef> getStaticFields() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<? extends FieldDef> getInstanceFields() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<? extends MethodDef> getMethods() {
        //TODO
        throw new UnsupportedOperationException();
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
}
