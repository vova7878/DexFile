package com.v7878.dex.util;

import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;

import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class MemberUtils {
    private static final Comparable<FieldDef> BORDER_FIELD = CollectionUtils
            .comparable(value -> AccessFlagUtils.isStatic(value.getAccessFlags()) ? -1 : 1);

    public static NavigableSet<FieldDef> getStaticFieldsSubset(NavigableSet<FieldDef> set) {
        return CollectionUtils.headSet(set, BORDER_FIELD, false);
    }

    public static NavigableSet<FieldDef> getInstanceFieldsSubset(NavigableSet<FieldDef> set) {
        return CollectionUtils.tailSet(set, BORDER_FIELD, false);
    }

    private static final Comparable<MethodDef> BORDER_METHOD = CollectionUtils
            .comparable(value -> AccessFlagUtils.isDirect(value.getAccessFlags()) ? -1 : 1);

    public static NavigableSet<MethodDef> getDirectMethodsSubset(NavigableSet<MethodDef> set) {
        return CollectionUtils.headSet(set, BORDER_METHOD, false);
    }

    public static NavigableSet<MethodDef> getVirtualMethodsSubset(NavigableSet<MethodDef> set) {
        return CollectionUtils.tailSet(set, BORDER_METHOD, false);
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

    public static FieldDef findField(NavigableSet<FieldDef> fields, String name, TypeId type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        return CollectionUtils.findValue(fields, CollectionUtils.comparable(value -> {
            int out = CollectionUtils.compareNonNull(value.getName(), name);
            if (out != 0) return out;
            return CollectionUtils.compareNonNull(value.getType(), type);
        }));
    }

    public static MethodDef findMethod(NavigableSet<MethodDef> methods, String name, ProtoId proto) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(proto);
        return CollectionUtils.findValue(methods, CollectionUtils.comparable(value -> {
            int out = CollectionUtils.compareNonNull(value.getName(), name);
            if (out != 0) return out;
            return CollectionUtils.compareNonNull(value.getProto(), proto);
        }));
    }
}
