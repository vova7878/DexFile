package com.v7878.dex.immutable;

import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;

public final class ClassDef implements Annotatable {
    private final TypeId type;
    private final int access_flags;
    private final TypeId superclass;
    private final NavigableSet<TypeId> interfaces;
    private final String source_file;
    private final NavigableSet<FieldDef> fields;
    private final NavigableSet<MethodDef> methods;
    private final NavigableSet<Annotation> annotations;

    private ClassDef(
            TypeId type, int access_flags, TypeId superclass, Iterable<TypeId> interfaces,
            String source_file, Iterable<FieldDef> fields,
            Iterable<MethodDef> methods, Iterable<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.access_flags = Preconditions.checkClassAccessFlags(access_flags);
        this.superclass = superclass; // may be null
        this.interfaces = ItemConverter.toNavigableSet(interfaces);
        this.source_file = source_file; // may be null
        this.fields = ItemConverter.toNavigableSet(fields);
        this.methods = ItemConverter.toNavigableSet(methods);
        this.annotations = ItemConverter.toNavigableSet(annotations);
    }

    public static ClassDef of(
            TypeId type, int access_flags, TypeId superclass, Iterable<TypeId> interfaces,
            String source_file, Iterable<FieldDef> fields,
            Iterable<MethodDef> methods, Iterable<Annotation> annotations) {
        return new ClassDef(type, access_flags, superclass,
                interfaces, source_file, fields, methods, annotations);
    }

    public TypeId getType() {
        return type;
    }

    public int getAccessFlags() {
        return access_flags;
    }

    public TypeId getSuperclass() {
        return superclass;
    }

    public NavigableSet<TypeId> getInterfaces() {
        return interfaces;
    }

    public String getSourceFile() {
        return source_file;
    }

    public NavigableSet<FieldDef> getFields() {
        return fields;
    }

    public NavigableSet<FieldDef> getStaticFields() {
        return MemberUtils.getStaticFieldsSubset(getFields());
    }

    public NavigableSet<FieldDef> getInstanceFields() {
        return MemberUtils.getInstanceFieldsSubset(getFields());
    }

    public NavigableSet<MethodDef> getMethods() {
        return methods;
    }

    public NavigableSet<MethodDef> getDirectMethods() {
        return MemberUtils.getDirectMethodsSubset(getMethods());
    }

    public NavigableSet<MethodDef> getVirtualMethods() {
        return MemberUtils.getVirtualMethodsSubset(getMethods());
    }

    public NavigableSet<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getAccessFlags(), getSuperclass(),
                getInterfaces(), getSourceFile(), getFields(), getMethods());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof ClassDef other
                && getAccessFlags() == other.getAccessFlags()
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getSuperclass(), other.getSuperclass())
                && Objects.equals(getSourceFile(), other.getSourceFile())
                && Objects.equals(getInterfaces(), other.getInterfaces())
                && Objects.equals(getFields(), other.getFields())
                && Objects.equals(getMethods(), other.getMethods());
    }
}
