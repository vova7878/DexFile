package com.v7878.dex.util;

import static com.v7878.dex.DexConstants.ACC_DIRECT_MASK;
import static com.v7878.dex.DexConstants.ACC_STATIC;

public class AccessFlagUtils {
    public static boolean isStatic/*Field*/(int flags) {
        return (flags & ACC_STATIC) != 0;
    }

    public static boolean isDirect/*Method*/(int flags) {
        return (flags & ACC_DIRECT_MASK) != 0;
    }
}
