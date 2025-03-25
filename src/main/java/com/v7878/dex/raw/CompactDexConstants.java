package com.v7878.dex.raw;

public class CompactDexConstants {
    public static final int kRegistersSizeShift = 12;
    public static final int kInsSizeShift = 8;
    public static final int kOutsSizeShift = 4;
    public static final int kTriesSizeSizeShift = 0;
    public static final int kInsnsSizeShift = 5;

    public static final int kInsnsSizeMask = 0xffff >>> kInsnsSizeShift;

    public static final int kFlagPreHeaderRegistersSize = 0b00001;
    public static final int kFlagPreHeaderInsSize = 0b00010;
    public static final int kFlagPreHeaderOutsSize = 0b00100;
    public static final int kFlagPreHeaderTriesSize = 0b01000;
    public static final int kFlagPreHeaderInsnsSize = 0b10000;

    public static final int kDebugElementsPerIndex = 16;
}
