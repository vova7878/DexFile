package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseClassDef;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.ClassDef;
import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;

public class ImmutableClassDef extends BaseClassDef {
    private final ImmutableTypeId type;
    private final int access_flags;
    private final ImmutableTypeId superclass;
    private final NavigableSet<? extends ImmutableTypeId> interfaces;
    private final String source_file;
    private final NavigableSet<? extends ImmutableFieldDef> fields;
    private final NavigableSet<? extends ImmutableMethodDef> methods;
    private final NavigableSet<? extends ImmutableAnnotation> annotations;

    protected ImmutableClassDef(
            TypeId type, int access_flags, TypeId superclass, Iterable<? extends TypeId> interfaces,
            String source_file, Iterable<? extends FieldDef> fields,
            Iterable<? extends MethodDef> methods, Iterable<? extends Annotation> annotations) {
        this.type = ImmutableTypeId.of(type);
        this.access_flags = Preconditions.checkClassAccessFlags(access_flags);
        this.superclass = superclass == null ? null : ImmutableTypeId.of(superclass);
        this.interfaces = ItemConverter.toNavigableSet(ImmutableTypeId::of,
                value -> value instanceof ImmutableTypeId, interfaces);
        this.source_file = source_file; // may be null
        this.fields = ItemConverter.toNavigableSet(ImmutableFieldDef::of,
                value -> value instanceof ImmutableFieldDef, fields);
        this.methods = ItemConverter.toNavigableSet(ImmutableMethodDef::of,
                value -> value instanceof ImmutableMethodDef, methods);
        this.annotations = ItemConverter.toNavigableSet(ImmutableAnnotation::of,
                value -> value instanceof ImmutableAnnotation, annotations);
    }

    public static ImmutableClassDef of(
            TypeId type, int access_flags, TypeId superclass, Iterable<? extends TypeId> interfaces,
            String source_file, Iterable<? extends FieldDef> fields,
            Iterable<? extends MethodDef> methods, Iterable<? extends Annotation> annotations) {
        return new ImmutableClassDef(type, access_flags, superclass,
                interfaces, source_file, fields, methods, annotations);
    }

    public static ImmutableClassDef of(ClassDef other) {
        if (other instanceof ImmutableClassDef immutable) return immutable;
        return new ImmutableClassDef(other.getType(), other.getAccessFlags(),
                other.getSuperclass(), other.getInterfaces(), other.getSourceFile(),
                other.getFields(), other.getMethods(), other.getAnnotations());
    }

    @Override
    public ImmutableTypeId getType() {
        return type;
    }

    @Override
    public int getAccessFlags() {
        return access_flags;
    }

    @Override
    public ImmutableTypeId getSuperclass() {
        return superclass;
    }

    @Override
    public NavigableSet<? extends ImmutableTypeId> getInterfaces() {
        return interfaces;
    }

    @Override
    public String getSourceFile() {
        return source_file;
    }

    @Override
    public NavigableSet<? extends ImmutableFieldDef> getFields() {
        return fields;
    }

    @Override
    public NavigableSet<? extends ImmutableMethodDef> getMethods() {
        return methods;
    }

    @Override
    public NavigableSet<? extends ImmutableAnnotation> getAnnotations() {
        return annotations;
    }
}
