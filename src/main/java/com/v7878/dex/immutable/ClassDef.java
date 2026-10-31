package com.v7878.dex.immutable;

import static com.v7878.dex.DexConstants.ACC_INTERFACE;
import static com.v7878.dex.DexIO.ClassHeader;
import static com.v7878.dex.immutable.TypeId.OBJECT;
import static com.v7878.dex.immutable.TypeId.V;

import com.v7878.dex.AccessFlag;
import com.v7878.dex.Internal;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;

public final class ClassDef implements Annotatable, ClassHeader {
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
        var set = new HashSet<TypeId>(capacity); // The order does not matter

        for (var value : interfaces) {
            Objects.requireNonNull(value);
            if (set.add(value)) {
                list.add(value);
            }
        }

        list.trimToSize();
        return Collections.unmodifiableList(list);
    }

    private ClassDef(TypeId type, int access_flags, TypeId superclass, List<TypeId> interfaces,
                     String source_file, NavigableSet<FieldDef> fields,
                     NavigableSet<MethodDef> methods, NavigableSet<Annotation> annotations) {
        this.type = Objects.requireNonNull(type);
        this.access_flags = Preconditions.checkClassAccessFlags(access_flags);
        if (isInterface() && !OBJECT.equals(superclass)) {
            throw new IllegalArgumentException("Interface " + type +
                    " must have java.lang.Object as superclass, got " + superclass);
        }
        if (isJavaLangObject() && superclass != null) {
            throw new IllegalArgumentException(
                    "java.lang.Object must not have a superclass, got " + superclass);
        }
        if (superclass == null && !isJavaLangObject()) {
            throw new IllegalArgumentException(
                    "Missing superclass for " + type);
        }
        this.superclass = superclass; // may be null
        this.interfaces = Objects.requireNonNull(interfaces);
        this.source_file = source_file; // may be null
        this.fields = Objects.requireNonNull(fields);
        this.methods = Objects.requireNonNull(methods);
        this.annotations = Objects.requireNonNull(annotations);
    }

    @Internal
    public static ClassDef raw(
            TypeId type, int access_flags, TypeId superclass, List<TypeId> interfaces,
            String source_file, NavigableSet<FieldDef> fields,
            NavigableSet<MethodDef> methods, NavigableSet<Annotation> annotations) {
        Objects.requireNonNull(interfaces);
        // raw() skips the copying of the collections, but the interface list
        // is rebuilt anyway: duplicates there would produce an invalid class_def.
        return new ClassDef(type, access_flags, superclass, toInterfacesList(interfaces),
                source_file, fields, methods, annotations);
    }

    /**
     * {@code null} is treated as an empty collection.
     */
    public static ClassDef of(
            TypeId type, int access_flags, TypeId superclass, Iterable<TypeId> interfaces,
            String source_file, Iterable<FieldDef> fields,
            Iterable<MethodDef> methods, Iterable<Annotation> annotations) {
        return new ClassDef(type, access_flags, superclass, toInterfacesList(interfaces),
                source_file, MemberUtils.toFieldSet(fields),
                MemberUtils.toMethodSet(methods), MemberUtils.toAnnotationSet(annotations));
    }

    @Override
    public TypeId getType() {
        return type;
    }

    public boolean isJavaLangObject() {
        return OBJECT.equals(type);
    }

    @Override
    public int getAccessFlags() {
        return access_flags;
    }

    public boolean isInterface() {
        return (access_flags & ACC_INTERFACE) != 0;
    }

    @Override
    public TypeId getSuperclass() {
        return superclass;
    }

    @Override
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

    /**
     * Instance list first; cross-list duplicates are invalid in a dex but are not checked.
     */
    public FieldDef findField(String name, TypeId type) {
        var out = findInstanceField(name, type);
        if (out != null) return out;
        return findStaticField(name, type);
    }

    /**
     * @see #findField(String, TypeId)
     */
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

    public MethodDef findClassInitializer() {
        return findDirectMethod("<clinit>", ProtoId.of(V));
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

    /**
     * Virtual list first; cross-list duplicates are invalid in a dex but are not checked.
     */
    public MethodDef findMethod(String name, ProtoId proto) {
        var out = findVirtualMethod(name, proto);
        if (out != null) return out;
        return findDirectMethod(name, proto);
    }

    /**
     * @see #findMethod(String, ProtoId)
     */
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

    /**
     * Debug view, not part of the API contract.
     */
    @Override
    public String toString() {
        // TODO: replace with the smali printer
        var builder = new StringBuilder("ClassDef{type=").append(getType());
        builder.append(" flags=[").append(
                AccessFlag.formatAccessFlagsForClass(getAccessFlags())).append(']');
        if (getSuperclass() != null) {
            builder.append(" superclass=").append(getSuperclass());
        }
        if (!getInterfaces().isEmpty()) {
            builder.append(" interfaces=").append(getInterfaces());
        }
        if (getSourceFile() != null) {
            builder.append(" source=").append(getSourceFile());
        }
        builder.append(" fields=").append(getFields().size());
        builder.append(" methods=").append(getMethods().size());
        builder.append(" annotations=").append(getAnnotations().size());
        return builder.append('}').toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getAccessFlags(), getSuperclass(),
                getInterfaces(), getSourceFile(),
                CollectionUtils.setHashCode(getFields()),
                CollectionUtils.setHashCode(getMethods()),
                CollectionUtils.setHashCode(getAnnotations()));
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
                && CollectionUtils.setEquals(getFields(), other.getFields())
                && CollectionUtils.setEquals(getMethods(), other.getMethods())
                && CollectionUtils.setEquals(getAnnotations(), other.getAnnotations());
    }
}
