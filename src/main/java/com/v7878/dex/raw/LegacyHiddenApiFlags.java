package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.ACC_NATIVE;
import static com.v7878.dex.DexConstants.ACC_VISIBILITY_MASK;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_BLOCKED;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_O;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_SDK;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_UNSUPPORTED;
import static com.v7878.dex.util.Checks.shouldNotReachHere;
import static com.v7878.dex.util.MathUtils.isPowerOfTwo;

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
class LegacyHiddenApiFlags {
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

    public static int encrypt(int access_flags, int hiddenapi_flags) {
        hiddenapi_flags = switch (hiddenapi_flags) {
            case HIDDENAPI_FLAG_SDK -> kWhitelist;
            case HIDDENAPI_FLAG_UNSUPPORTED -> kLightGreylist;
            case HIDDENAPI_FLAG_MAX_TARGET_O -> kDarkGreylist;
            case HIDDENAPI_FLAG_BLOCKED -> kBlacklist;
            default -> throw new IllegalArgumentException(String.format(
                    "Invalid hidden api flag: %d", hiddenapi_flags));
        };
        if ((hiddenapi_flags & 0x1) != 0) {
            access_flags ^= ACC_VISIBILITY_MASK;
        }
        if ((hiddenapi_flags & 0x2) != 0) {
            int second_flag = getSecondFlag(access_flags);
            access_flags |= second_flag;
        }
        return access_flags;
    }

    public static long decrypt(int access_flags) {
        int hiddenapi_flags = 0;
        // First bit
        {
            if (!isPowerOfTwo(access_flags & ACC_VISIBILITY_MASK)) {
                access_flags ^= ACC_VISIBILITY_MASK;
                hiddenapi_flags |= 0x1;
            }
        }
        // Second bit
        {
            int second_flag = getSecondFlag(access_flags);
            if ((access_flags & second_flag) != 0) {
                access_flags &= ~second_flag;
                hiddenapi_flags |= 0x2;
            }
        }
        hiddenapi_flags = switch (hiddenapi_flags) {
            case kWhitelist -> HIDDENAPI_FLAG_SDK;
            case kLightGreylist -> HIDDENAPI_FLAG_UNSUPPORTED;
            case kDarkGreylist -> HIDDENAPI_FLAG_MAX_TARGET_O;
            case kBlacklist -> HIDDENAPI_FLAG_BLOCKED;
            default -> throw shouldNotReachHere();
        };
        return access_flags & 0xffffffffL |
                (hiddenapi_flags & 0xffffffffL) << 32;
    }
}
