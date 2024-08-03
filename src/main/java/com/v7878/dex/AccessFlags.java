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
import static com.v7878.dex.DexConstants.ACC_SYNCHRONIZED;
import static com.v7878.dex.DexConstants.ACC_SYNTHETIC;
import static com.v7878.dex.DexConstants.ACC_TRANSIENT;
import static com.v7878.dex.DexConstants.ACC_VARARGS;
import static com.v7878.dex.DexConstants.ACC_VOLATILE;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public enum AccessFlags {
    PUBLIC(ACC_PUBLIC, true, true, false, true, "public"),
    PRIVATE(ACC_PRIVATE, true, true, false, true, "private"),
    PROTECTED(ACC_PROTECTED, true, true, false, true, "protected"),
    STATIC(ACC_STATIC, true, true, false, true, "static"),
    FINAL(ACC_FINAL, true, true, true, true, "final"),
    SYNCHRONIZED(ACC_SYNCHRONIZED, false, true, false, false, "synchronized"),
    VOLATILE(ACC_VOLATILE, false, false, false, true, "volatile"),
    BRIDGE(ACC_BRIDGE, false, true, false, false, "bridge"),
    TRANSIENT(ACC_TRANSIENT, false, false, false, true, "transient"),
    VARARGS(ACC_VARARGS, false, true, false, false, "varargs"),
    NATIVE(ACC_NATIVE, false, true, false, false, "native"),
    INTERFACE(ACC_INTERFACE, true, false, false, false, "interface"),
    ABSTRACT(ACC_ABSTRACT, true, true, false, false, "abstract"),
    STRICTFP(ACC_STRICTFP, false, true, false, false, "strictfp"),
    SYNTHETIC(ACC_SYNTHETIC, true, true, true, true, "synthetic"),
    ANNOTATION(ACC_ANNOTATION, true, false, false, false, "annotation"),
    ENUM(ACC_ENUM, true, false, false, true, "enum"),
    MANDATED(ACC_MANDATED, false, false, true, false, "mandated"),
    CONSTRUCTOR(ACC_CONSTRUCTOR, false, true, false, false, "constructor"),
    DECLARED_SYNCHRONIZED(ACC_DECLARED_SYNCHRONIZED, false, true, false, false, "declared-synchronized");

    private final int value;
    private final boolean validForClass;
    private final boolean validForMethod;
    private final boolean validForParameter;
    private final boolean validForField;
    private final String name;

    private static final AccessFlags[] CACHED_ALL_FLAGS = AccessFlags.values();

    private static final AccessFlags[] CACHED_CLASS_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForClass)
            .toArray(AccessFlags[]::new);

    public static final int VALID_CLASS_FLAGS_MASK = combine(CACHED_CLASS_FLAGS);

    public static final AccessFlags[] CACHED_METHOD_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForMethod)
            .toArray(AccessFlags[]::new);

    public static final int VALID_METHOD_FLAGS_MASK = combine(CACHED_METHOD_FLAGS);

    public static final AccessFlags[] CACHED_PARAMETER_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForParameter)
            .toArray(AccessFlags[]::new);

    public static final int VALID_PARAMETER_FLAGS_MASK = combine(CACHED_PARAMETER_FLAGS);

    public static final AccessFlags[] CACHED_FIELD_FLAGS = Arrays
            .stream(CACHED_ALL_FLAGS)
            .filter(flag -> flag.validForField)
            .toArray(AccessFlags[]::new);

    public static final int VALID_FIELD_FLAGS_MASK = combine(CACHED_FIELD_FLAGS);

    AccessFlags(int value, boolean validForClass, boolean validForMethod,
                boolean validForParameter, boolean validForField, String name) {
        this.value = value;
        this.validForClass = validForClass;
        this.validForMethod = validForMethod;
        this.validForParameter = validForParameter;
        this.validForField = validForField;
        this.name = name;
    }

    public static int combine(AccessFlags[] flags) {
        int value = 0;
        for (var flag : flags) {
            value |= flag.value;
        }
        return value;
    }

    public static boolean isValidForClass(int flags) {
        return flags == (flags & VALID_CLASS_FLAGS_MASK);
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

    private static String formatAccessFlags(AccessFlags[] flags) {
        return Arrays.stream(flags).map(Objects::toString)
                .collect(Collectors.joining(" "));
    }

    public static AccessFlags[] getAccessFlagsForClass(int flags) {
        return Arrays.stream(CACHED_CLASS_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlags[]::new);
    }

    public static String formatAccessFlagsForClass(int flags) {
        return formatAccessFlags(getAccessFlagsForClass(flags));
    }

    public static AccessFlags[] getAccessFlagsForMethod(int flags) {
        return Arrays.stream(CACHED_METHOD_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlags[]::new);
    }

    public static String formatAccessFlagsForMethod(int flags) {
        return formatAccessFlags(getAccessFlagsForMethod(flags));
    }

    public static AccessFlags[] getAccessFlagsForParameter(int flags) {
        return Arrays.stream(CACHED_PARAMETER_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlags[]::new);
    }

    public static String formatAccessFlagsForParameter(int flags) {
        return formatAccessFlags(getAccessFlagsForParameter(flags));
    }

    public static AccessFlags[] getAccessFlagsForField(int flags) {
        return Arrays.stream(CACHED_FIELD_FLAGS)
                .filter(flag -> flag.isSet(flags))
                .toArray(AccessFlags[]::new);
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
}
