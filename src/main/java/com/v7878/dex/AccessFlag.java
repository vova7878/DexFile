package com.v7878.dex;

import static com.v7878.dex.DexConstants.ACC_ABSTRACT;
import static com.v7878.dex.DexConstants.ACC_ANNOTATION;
import static com.v7878.dex.DexConstants.ACC_BRIDGE;
import static com.v7878.dex.DexConstants.ACC_CONSTRUCTOR;
import static com.v7878.dex.DexConstants.ACC_DECLARED_SYNCHRONIZED;
import static com.v7878.dex.DexConstants.ACC_ENUM;
import static com.v7878.dex.DexConstants.ACC_FINAL;
import static com.v7878.dex.DexConstants.ACC_INTERFACE;
import static com.v7878.dex.DexConstants.ACC_MANDATED;
import static com.v7878.dex.DexConstants.ACC_NATIVE;
import static com.v7878.dex.DexConstants.ACC_PRIVATE;
import static com.v7878.dex.DexConstants.ACC_PROTECTED;
import static com.v7878.dex.DexConstants.ACC_PUBLIC;
import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.DexConstants.ACC_STRICTFP;
import static com.v7878.dex.DexConstants.ACC_SUPER;
import static com.v7878.dex.DexConstants.ACC_SYNCHRONIZED;
import static com.v7878.dex.DexConstants.ACC_SYNTHETIC;
import static com.v7878.dex.DexConstants.ACC_TRANSIENT;
import static com.v7878.dex.DexConstants.ACC_VARARGS;
import static com.v7878.dex.DexConstants.ACC_VOLATILE;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AccessFlag {
    PUBLIC(ACC_PUBLIC, "public", true, true, true, false, true),
    PRIVATE(ACC_PRIVATE, "private", false, true, true, false, true),
    PROTECTED(ACC_PROTECTED, "protected", false, true, true, false, true),
    STATIC(ACC_STATIC, "static", false, true, true, false, true),
    FINAL(ACC_FINAL, "final", true, true, true, true, true),
    SYNCHRONIZED(ACC_SYNCHRONIZED, "synchronized", false, false, true, false, false),
    // unused legacy flag
    SUPER(ACC_SUPER, "super", true, true, false, false, false),
    VOLATILE(ACC_VOLATILE, "volatile", false, false, false, false, true),
    BRIDGE(ACC_BRIDGE, "bridge", false, false, true, false, false),
    TRANSIENT(ACC_TRANSIENT, "transient", false, false, false, false, true),
    VARARGS(ACC_VARARGS, "varargs", false, false, true, false, false),
    NATIVE(ACC_NATIVE, "native", false, false, true, false, false),
    INTERFACE(ACC_INTERFACE, "interface", true, true, false, false, false),
    ABSTRACT(ACC_ABSTRACT, "abstract", true, true, true, false, false),
    STRICTFP(ACC_STRICTFP, "strictfp", false, false, true, false, false),
    SYNTHETIC(ACC_SYNTHETIC, "synthetic", true, true, true, true, true),
    ANNOTATION(ACC_ANNOTATION, "annotation", true, true, false, false, false),
    ENUM(ACC_ENUM, "enum", true, true, false, false, true),
    MANDATED(ACC_MANDATED, "mandated", false, false, false, true, false),
    CONSTRUCTOR(ACC_CONSTRUCTOR, "constructor", false, false, true, false, false),
    DECLARED_SYNCHRONIZED(ACC_DECLARED_SYNCHRONIZED, "declared-synchronized", false, false, true, false, false);

    private final int value;
    private final boolean validForClass;
    private final boolean validForInnerClass;
    private final boolean validForMethod;
    private final boolean validForParameter;
    private final boolean validForField;
    private final String name;

    private static final AccessFlag[] CACHED_ALL_FLAGS = AccessFlag.values();

    private static final AccessFlag[] CACHED_CLASS_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForClass)
            .toArray(AccessFlag[]::new);
    public static final int VALID_CLASS_FLAGS_MASK = combine(CACHED_CLASS_FLAGS);

    private static final AccessFlag[] CACHED_INNER_CLASS_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForInnerClass)
            .toArray(AccessFlag[]::new);
    public static final int VALID_INNER_CLASS_FLAGS_MASK = combine(CACHED_INNER_CLASS_FLAGS);

    public static final AccessFlag[] CACHED_METHOD_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForMethod)
            .toArray(AccessFlag[]::new);
    public static final int VALID_METHOD_FLAGS_MASK = combine(CACHED_METHOD_FLAGS);

    public static final AccessFlag[] CACHED_PARAMETER_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForParameter)
            .toArray(AccessFlag[]::new);
    public static final int VALID_PARAMETER_FLAGS_MASK = combine(CACHED_PARAMETER_FLAGS);

    public static final AccessFlag[] CACHED_FIELD_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForField)
            .toArray(AccessFlag[]::new);
    public static final int VALID_FIELD_FLAGS_MASK = combine(CACHED_FIELD_FLAGS);

    AccessFlag(int value, String name, boolean validForClass,
               boolean validForInnerClass, boolean validForMethod,
               boolean validForParameter, boolean validForField) {
        this.value = value;
        this.name = name;
        this.validForClass = validForClass;
        this.validForInnerClass = validForInnerClass;
        this.validForMethod = validForMethod;
        this.validForParameter = validForParameter;
        this.validForField = validForField;
    }

    public static int combine(AccessFlag[] flags) {
        int value = 0;
        for (var flag : flags) {
            value |= flag.value;
        }
        return value;
    }

    public static boolean isValidForClass(int flags) {
        return flags == (flags & VALID_CLASS_FLAGS_MASK);
    }

    public static boolean isValidForInnerClass(int flags) {
        return flags == (flags & VALID_INNER_CLASS_FLAGS_MASK);
    }

    public static boolean isValidForMethod(int flags) {
        return flags == (flags & VALID_METHOD_FLAGS_MASK);
    }

    public static boolean isValidForParameter(int flags) {
        return flags == (flags & VALID_PARAMETER_FLAGS_MASK);
    }

    public static boolean isValidForField(int flags) {
        return flags == (flags & VALID_FIELD_FLAGS_MASK);
    }

    private static String formatAccessFlags(AccessFlag[] flags) {
        return Arrays.stream(flags).map(Objects::toString)
                .collect(Collectors.joining(" "));
    }

    public static AccessFlag[] getAccessFlagsForClass(int flags) {
        return Arrays.stream(CACHED_CLASS_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlag[]::new);
    }

    public static String formatAccessFlagsForClass(int flags) {
        return formatAccessFlags(getAccessFlagsForClass(flags));
    }

    public static AccessFlag[] getAccessFlagsForInnerClass(int flags) {
        return Arrays.stream(CACHED_INNER_CLASS_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlag[]::new);
    }

    public static String formatAccessFlagsForInnerClass(int flags) {
        return formatAccessFlags(getAccessFlagsForInnerClass(flags));
    }

    public static AccessFlag[] getAccessFlagsForMethod(int flags) {
        return Arrays.stream(CACHED_METHOD_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlag[]::new);
    }

    public static String formatAccessFlagsForMethod(int flags) {
        return formatAccessFlags(getAccessFlagsForMethod(flags));
    }

    public static AccessFlag[] getAccessFlagsForParameter(int flags) {
        return Arrays.stream(CACHED_PARAMETER_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlag[]::new);
    }

    public static String formatAccessFlagsForParameter(int flags) {
        return formatAccessFlags(getAccessFlagsForParameter(flags));
    }

    public static AccessFlag[] getAccessFlagsForField(int flags) {
        return Arrays.stream(CACHED_FIELD_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlag[]::new);
    }

    public static String formatAccessFlagsForField(int flags) {
        return formatAccessFlags(getAccessFlagsForField(flags));
    }

    public boolean isSet(int flags) {
        return (this.value & flags) != 0;
    }

    public int value() {
        return value;
    }

    public String toString() {
        return name;
    }

    // TODO: valueof(String name)
}
