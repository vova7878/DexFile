package com.v7878.dex.util;

import com.v7878.dex.AccessFlag;

public class AccessFlagUtils {
    public static boolean isStatic/*Field*/(int flags) {
        return AccessFlag.STATIC.isSet(flags);
    }

    public static final int DIRECT_MASK = AccessFlag.STATIC.value()
            | AccessFlag.PRIVATE.value() | AccessFlag.CONSTRUCTOR.value();

    public static boolean isDirect/*Method*/(int flags) {
        return (flags & DIRECT_MASK) != 0;
    }
}
