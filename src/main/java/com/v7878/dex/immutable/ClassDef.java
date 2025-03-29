package com.v7878.dex.immutable;

import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;

public final class ClassDef implements Annotatable {
    private final TypeId type;
    private final int access_flags;
    private final TypeId superclass;
    // In some cases, the order of interfaces may be
    //  important (for example when redefining classes)
    private final List<TypeId> interfaces;
    private final String source_file;
    private final NavigableSet<FieldDef> fields;
    private final NavigableSet<MethodDef> methods;
    private final NavigableSet<Annotation> annotations;

    // Remove duplicates but keep order
    private static List<TypeId> toInterfacesList(Iterable<TypeId> interfaces) {
        if (interfaces == null) {
            return Collections.emptyList();
        }
        int capacity = interfaces instanceof Collection<?> c ? c.size() : 0;
        var list = new ArrayList<TypeId>(capacity);
        var set = new HashSet<TypeId>(capacity);

        for (var value : interfaces) {
            Objects.requireNonNull(value);
            if (set.add(value)) {
                list.add(value);
            }
        }

        list.trimToSize();
        return Collections.unmodifiableList(list);
    }

    private ClassDef(
            TypeId type, int access_flags, TypeId superclass, Iterable<TypeId> interfaces,
            String source_file, Iterable<FieldDef> fields,
            Iterable<MethodDef> methods, Iterable<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.access_flags = Preconditions.checkClassAccessFlags(access_flags);
        this.superclass = superclass; // may be null
        this.interfaces = toInterfacesList(interfaces);
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

    // TODO?: Add simpler constructor?

    public TypeId getType() {
        return type;
    }

    public int getAccessFlags() {
        return access_flags;
    }

    public TypeId getSuperclass() {
        return superclass;
    }

    public List<TypeId> getInterfaces() {
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

    public FieldDef findStaticField(String name, TypeId type) {
        return MemberUtils.findField(getStaticFields(), true, name, type);
    }

    public FieldDef findStaticField(FieldId id) {
        if (!getType().equals(id.getDeclaringClass())) {
            return null;
        }
        return findStaticField(id.getName(), id.getType());
    }

    public NavigableSet<FieldDef> getInstanceFields() {
        return MemberUtils.getInstanceFieldsSubset(getFields());
    }

    public FieldDef findInstanceField(String name, TypeId type) {
        return MemberUtils.findField(getInstanceFields(), false, name, type);
    }

    public FieldDef findInstanceField(FieldId id) {
        if (!getType().equals(id.getDeclaringClass())) {
            return null;
        }
        return findInstanceField(id.getName(), id.getType());
    }

    public FieldDef findField(String name, TypeId type) {
        var out = findInstanceField(name, type);
        if (out != null) return out;
        return findStaticField(name, type);
    }

    public FieldDef findField(FieldId id) {
        if (!getType().equals(id.getDeclaringClass())) {
            return null;
        }
        return findField(id.getName(), id.getType());
    }

    public NavigableSet<MethodDef> getMethods() {
        return methods;
    }

    public NavigableSet<MethodDef> getDirectMethods() {
        return MemberUtils.getDirectMethodsSubset(getMethods());
    }

    public MethodDef findDirectMethod(String name, ProtoId proto) {
        return MemberUtils.findMethod(getDirectMethods(), true, name, proto);
    }

    public MethodDef findDirectMethod(MethodId id) {
        if (!getType().equals(id.getDeclaringClass())) {
            return null;
        }
        return findDirectMethod(id.getName(), id.getProto());
    }

    public NavigableSet<MethodDef> getVirtualMethods() {
        return MemberUtils.getVirtualMethodsSubset(getMethods());
    }

    public MethodDef findVirtualMethod(String name, ProtoId proto) {
        return MemberUtils.findMethod(getVirtualMethods(), false, name, proto);
    }

    public MethodDef findVirtualMethod(MethodId id) {
        if (!getType().equals(id.getDeclaringClass())) {
            return null;
        }
        return findVirtualMethod(id.getName(), id.getProto());
    }

    public MethodDef findMethod(String name, ProtoId proto) {
        var out = findVirtualMethod(name, proto);
        if (out != null) return out;
        return findDirectMethod(name, proto);
    }

    public MethodDef findMethod(MethodId id) {
        if (!getType().equals(id.getDeclaringClass())) {
            return null;
        }
        return findMethod(id.getName(), id.getProto());
    }

    @Override
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
