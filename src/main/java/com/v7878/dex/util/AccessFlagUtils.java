package com.v7878.dex.util;

import com.v7878.dex.AccessFlags;

public class AccessFlagUtils {
    public static boolean isStatic/*Field*/(int flags) {
        return AccessFlags.STATIC.isSet(flags);
    }

    public static final int DIRECT_MASK = AccessFlags.STATIC.value()
            | AccessFlags.PRIVATE.value() | AccessFlags.CONSTRUCTOR.value();

    public static boolean isDirect/*Method*/(int flags) {
        return (flags & DIRECT_MASK) != 0;
    }
}
