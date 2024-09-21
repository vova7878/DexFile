package com.v7878.dex.util;

import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.TypeId;

import java.util.List;
import java.util.NavigableSet;

public class MemberUtils {
    private static final FieldDef LAST_STATIC_FIELD = FieldDef.of(
            "", TypeId.of(""), ACC_STATIC, 0, null, null);

    public static NavigableSet<FieldDef> getStaticFieldsSubset(NavigableSet<FieldDef> set) {
        return set.tailSet(LAST_STATIC_FIELD, true);
    }

    public static NavigableSet<FieldDef> getInstanceFieldsSubset(NavigableSet<FieldDef> set) {
        return set.headSet(LAST_STATIC_FIELD, false);
    }

    private static final MethodDef LAST_DIRECT_METHOD = MethodDef.of(
            "", TypeId.of(""), List.of(), AccessFlagUtils.DIRECT_MASK, 0, null, null);

    public static NavigableSet<MethodDef> getDirectMethodsSubset(NavigableSet<MethodDef> set) {
        return set.tailSet(LAST_DIRECT_METHOD, true);
    }

    public static NavigableSet<MethodDef> getVirtualMethodsSubset(NavigableSet<MethodDef> set) {
        return set.headSet(LAST_DIRECT_METHOD, false);
    }
}
