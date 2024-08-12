package com.v7878.dex.reader;

import com.v7878.dex.base.BaseFieldDef;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.io.RandomInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public class ReaderFieldDef extends BaseFieldDef {

    private final ReaderDex dexfile;
    private final ReaderClassDef class_def;
    private final int field_idx;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final int index;

    private ReaderFieldDef(ReaderDex dexfile, ReaderClassDef class_def, int index, int field_idx,
                           int access_flags, int hiddenapi_flags, boolean static_list) {
        this.dexfile = dexfile;
        this.class_def = class_def;
        this.field_idx = field_idx;
        this.access_flags = access_flags;
        this.hiddenapi_flags = hiddenapi_flags;
        this.index = static_list ? index : ~index;
    }

    public static List<ReaderFieldDef> readList(
            ReaderDex dexfile, ReaderClassDef class_def, RandomInput in,
            IntSupplier hiddenapi_iterator, int count, boolean static_list) {
        List<ReaderFieldDef> out = new ArrayList<>(count);
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += in.readULeb128();
            int access_flags = in.readULeb128();
            out.add(new ReaderFieldDef(dexfile, class_def, i, index,
                    access_flags, hiddenapi_iterator.getAsInt(), static_list));
        }
        return out;
    }

    private ReaderFieldId field;
    private EncodedValue value;
    private Set<? extends ReaderAnnotation> annotations;

    public ReaderFieldId getFieldId() {
        if (field != null) return field;
        return field = dexfile.getFieldId(field_idx);
    }

    @Override
    public String getName() {
        return getFieldId().getName();
    }

    @Override
    public TypeId getType() {
        return getFieldId().getType();
    }

    @Override
    public int getAccessFlags() {
        return access_flags;
    }

    @Override
    public int getHiddenApiFlags() {
        return hiddenapi_flags;
    }

    @Override
    public EncodedValue getInitialValue() {
        if (index < 0) return null;
        if (value != null) return value;
        var values = class_def.getStaticValues();
        if (index < values.size()) {
            return value = values.get(index);
        }
        return value = /*TODO: default value*/ null;
    }

    @Override
    public Set<? extends ReaderAnnotation> getAnnotations() {
        if (annotations != null) return annotations;
        var field_annotations = class_def.getAnnotationDirectory()
                .getFieldAnnotations().get(field_idx);
        return annotations = field_annotations == null ? Set.of() : field_annotations;
    }
}
