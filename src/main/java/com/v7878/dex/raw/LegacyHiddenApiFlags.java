package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.ACC_NATIVE;

/*
 * First bit is encoded as inversion of visibility flags (public/private/protected).
 * At most one can be set for any given class member. If two or three are set,
 * this is interpreted as the first bit being set and actual visibility flags
 * being the complement of the encoded flags.
 *
 * Second bit is either encoded as bit 5 for fields and non-native methods, where
 * it carries no other meaning. If a method is native (bit 8 set), bit 9 is used.
 *
 * Bits were selected so that they never increase the length of unsigned LEB-128
 * encoding of the access flags.
 */
public class LegacyHiddenApiFlags {
    // Analogue of HIDDENAPI_FLAG_SDK from newer versions of Android
    public static final int kWhitelist = 0;
    // Analogue of HIDDENAPI_FLAG_UNSUPPORTED from newer versions of Android
    public static final int kLightGreylist = 1;
    // It does not have a full analogue in newer versions of Android,
    //  but we will accept it as HIDDENAPI_FLAG_MAX_TARGET_O, since this is a closer meaning
    public static final int kDarkGreylist = 2;
    // Analogue of HIDDENAPI_FLAG_BLOCKED from newer versions of Android
    public static final int kBlacklist = 3;

    public static int getSecondFlag(int access_flags) {
        return (access_flags & ACC_NATIVE) == 0 ? 0x20 : 0x200;
    }
}
