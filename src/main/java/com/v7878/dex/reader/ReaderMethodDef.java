package com.v7878.dex.reader;

import com.v7878.dex.base.BaseMethodDef;
import com.v7878.dex.iface.MethodImplementation;
import com.v7878.dex.iface.Parameter;
import com.v7878.dex.io.RandomInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public class ReaderMethodDef extends BaseMethodDef {
    private final ReaderDex dexfile;
    private final ReaderClassDef class_def;
    private final int method_idx;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final int code_offset;

    private ReaderMethodDef(ReaderDex dexfile, ReaderClassDef class_def, int method_idx,
                            int access_flags, int hiddenapi_flags, int code_offset) {
        this.dexfile = dexfile;
        this.class_def = class_def;
        this.method_idx = method_idx;
        this.access_flags = access_flags;
        this.hiddenapi_flags = hiddenapi_flags;
        this.code_offset = code_offset;
    }

    public static List<ReaderMethodDef> readList(
            ReaderDex dexfile, ReaderClassDef class_def, RandomInput in,
            IntSupplier hiddenapi_iterator, int count) {
        List<ReaderMethodDef> out = new ArrayList<>(count);
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += in.readULeb128();
            int access_flags = in.readULeb128();
            int code_off = in.readULeb128();
            out.add(new ReaderMethodDef(dexfile, class_def, index,
                    access_flags, hiddenapi_iterator.getAsInt(), code_off));
        }
        return out;
    }

    private ReaderMethodId method;
    private Set<ReaderAnnotation> annotations;
    private List<Set<ReaderAnnotation>> parameter_annotations;

    public ReaderMethodId getMethodId() {
        if (method != null) return method;
        return method = dexfile.getMethodId(method_idx);
    }

    @Override
    public String getName() {
        return getMethodId().getName();
    }

    @Override
    public ReaderTypeId getReturnType() {
        return getMethodId().getReturnType();
    }

    public List<ReaderTypeId> getParameterTypes() {
        return getMethodId().getParameterTypes();
    }

    @Override
    public List<? extends Parameter> getParameters() {
        // TODO
        throw new UnsupportedOperationException();
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
    public MethodImplementation getImplementation() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<ReaderAnnotation> getAnnotations() {
        if (annotations != null) return annotations;
        var method_annotations = class_def.getAnnotationDirectory()
                .getMethodAnnotations().get(method_idx);
        return annotations = method_annotations == null ? Set.of() : method_annotations;
    }

    public List<Set<ReaderAnnotation>> getParameterAnnotations() {
        if (parameter_annotations != null) return parameter_annotations;
        var annotations = class_def.getAnnotationDirectory()
                .getParameterAnnotations().get(method_idx);
        return parameter_annotations = annotations == null ? List.of() : annotations;
    }
}
