package com.v7878.dex.util;

import static com.v7878.dex.DexConstants.ACC_NATIVE;
import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.util.AccessFlagUtils.DIRECT_MASK;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.value.EncodedNull;

import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class MemberUtils {
    private static FieldDef searchableField(String name, TypeId type, boolean is_static) {
        return FieldDef.of(name, type, is_static ? ACC_STATIC : 0, 0, null, null);
    }

    private static MethodDef searchableMethod(String name, TypeId ret, List<Parameter> parameters, boolean is_direct) {
        return MethodDef.of(name, ret, parameters,
                is_direct ? DIRECT_MASK : ACC_NATIVE, 0, null, null);
    }

    private static Annotation searchableAnnotation(TypeId type) {
        return Annotation.of(AnnotationVisibility.RUNTIME, type, (NavigableSet<AnnotationElement>) null);
    }

    private static AnnotationElement searchableAnnotationElement(String name) {
        return AnnotationElement.of(name, EncodedNull.INSTANCE);
    }

    private static final TypeId FIRST_TYPE = TypeId.of("");

    private static final FieldDef FIRST_INSTANCE_FIELD =
            searchableField("", FIRST_TYPE, false);

    public static NavigableSet<FieldDef> getStaticFieldsSubset(NavigableSet<FieldDef> set) {
        return set.headSet(FIRST_INSTANCE_FIELD, false);
    }

    public static NavigableSet<FieldDef> getInstanceFieldsSubset(NavigableSet<FieldDef> set) {
        return set.tailSet(FIRST_INSTANCE_FIELD, true);
    }

    private static final MethodDef FIRST_VIRTUAL_METHOD =
            searchableMethod("", FIRST_TYPE, Collections.emptyList(), false);

    public static NavigableSet<MethodDef> getDirectMethodsSubset(NavigableSet<MethodDef> set) {
        return set.headSet(FIRST_VIRTUAL_METHOD, false);
    }

    public static NavigableSet<MethodDef> getVirtualMethodsSubset(NavigableSet<MethodDef> set) {
        return set.tailSet(FIRST_VIRTUAL_METHOD, true);
    }

    public static NavigableSet<FieldDef> mergeFields(List<FieldDef> static_, List<FieldDef> instance) {
        var out = new TreeSet<FieldDef>();
        if (static_ != null) out.addAll(static_);
        if (instance != null) out.addAll(instance);
        return out;
    }

    public static NavigableSet<MethodDef> mergeMethods(List<MethodDef> direct, List<MethodDef> virtual) {
        var out = new TreeSet<MethodDef>();
        if (direct != null) out.addAll(direct);
        if (virtual != null) out.addAll(virtual);
        return out;
    }

    public static FieldDef findField(NavigableSet<FieldDef> fields, boolean is_static, String name, TypeId type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        return CollectionUtils.findValue(fields, searchableField(name, type, is_static));
    }

    public static MethodDef findMethod(NavigableSet<MethodDef> methods, boolean is_direct, String name, ProtoId proto) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(proto);
        return CollectionUtils.findValue(methods, searchableMethod(name, proto.getReturnType(),
                Parameter.listOf(proto.getParameterTypes()), is_direct));
    }

    public static Annotation findAnnotation(NavigableSet<Annotation> annotations, TypeId type) {
        Objects.requireNonNull(type);
        return CollectionUtils.findValue(annotations, searchableAnnotation(type));
    }

    public static AnnotationElement findElement(NavigableSet<AnnotationElement> annotations, String name) {
        Objects.requireNonNull(name);
        return CollectionUtils.findValue(annotations, searchableAnnotationElement(name));
    }
}
