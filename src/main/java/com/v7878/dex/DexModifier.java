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
import static com.v7878.dex.DexConstants.ACC_STRICT;
import static com.v7878.dex.DexConstants.ACC_SYNCHRONIZED;
import static com.v7878.dex.DexConstants.ACC_SYNTHETIC;
import static com.v7878.dex.DexConstants.ACC_TRANSIENT;
import static com.v7878.dex.DexConstants.ACC_VARARGS;
import static com.v7878.dex.DexConstants.ACC_VOLATILE;

import com.v7878.dex.util.SparseArray;

import java.util.StringJoiner;

public class DexModifier {

    private static String printBits(int mod, SparseArray<String> names) {
        StringJoiner sj = new StringJoiner(" ");

        for (int i = 0; i < 32; i++) {
            int flag = 1 << i;
            if ((mod & flag) != 0) {
                String name = names.get(flag);
                sj.add(name != null ? name : "[1<<" + i + "]");
            }
        }

        return sj.toString();
    }

    private static final SparseArray<String> class_flags = new SparseArray<>();

    static {
        class_flags.put(ACC_PUBLIC, "public");
        class_flags.put(ACC_FINAL, "final");
        class_flags.put(ACC_INTERFACE, "interface");
        class_flags.put(ACC_ABSTRACT, "abstract");
        class_flags.put(ACC_SYNTHETIC, "synthetic");
        class_flags.put(ACC_ANNOTATION, "annotation");
        class_flags.put(ACC_ENUM, "enum");
    }

    public static String printClassFlags(int mod) {
        return printBits(mod, class_flags);
    }

    private static final SparseArray<String> inner_class_flags = new SparseArray<>();

    static {
        inner_class_flags.put(ACC_PUBLIC, "public");
        inner_class_flags.put(ACC_PRIVATE, "private");
        inner_class_flags.put(ACC_PROTECTED, "protected");
        inner_class_flags.put(ACC_STATIC, "static");
        inner_class_flags.put(ACC_FINAL, "final");
        inner_class_flags.put(ACC_INTERFACE, "interface");
        inner_class_flags.put(ACC_ABSTRACT, "abstract");
        inner_class_flags.put(ACC_SYNTHETIC, "synthetic");
        inner_class_flags.put(ACC_ANNOTATION, "annotation");
        inner_class_flags.put(ACC_ENUM, "enum");
    }

    public static String printInnerClassFlags(int mod) {
        return printBits(mod, inner_class_flags);
    }

    private static final SparseArray<String> executable_flags = new SparseArray<>();

    static {
        executable_flags.put(ACC_PUBLIC, "public");
        executable_flags.put(ACC_PRIVATE, "private");
        executable_flags.put(ACC_PROTECTED, "protected");
        executable_flags.put(ACC_STATIC, "static");
        executable_flags.put(ACC_FINAL, "final");
        executable_flags.put(ACC_SYNCHRONIZED, "synchronized");
        executable_flags.put(ACC_BRIDGE, "bridge");
        executable_flags.put(ACC_VARARGS, "varargs");
        executable_flags.put(ACC_NATIVE, "native");
        executable_flags.put(ACC_ABSTRACT, "abstract");
        executable_flags.put(ACC_STRICT, "strictfp");
        executable_flags.put(ACC_SYNTHETIC, "synthetic");
        executable_flags.put(ACC_CONSTRUCTOR, "constructor");
        executable_flags.put(ACC_DECLARED_SYNCHRONIZED, "declared_synchronized");
    }

    public static String printExecutableFlags(int mod) {
        return printBits(mod, executable_flags);
    }

    private static final SparseArray<String> field_flags = new SparseArray<>();

    static {
        field_flags.put(ACC_PUBLIC, "public");
        field_flags.put(ACC_PRIVATE, "private");
        field_flags.put(ACC_PROTECTED, "protected");
        field_flags.put(ACC_STATIC, "static");
        field_flags.put(ACC_FINAL, "final");
        field_flags.put(ACC_VOLATILE, "volative");
        field_flags.put(ACC_TRANSIENT, "transient");
        field_flags.put(ACC_SYNTHETIC, "synthetic");
        field_flags.put(ACC_ENUM, "enum");
    }

    public static String printFieldFlags(int mod) {
        return printBits(mod, field_flags);
    }

    private static final SparseArray<String> parameter_flags = new SparseArray<>();

    static {
        parameter_flags.put(ACC_FINAL, "final");
        parameter_flags.put(ACC_SYNTHETIC, "synthetic");
        parameter_flags.put(ACC_MANDATED, "mandated");
    }

    public static String printParameterFlags(int mod) {
        return printBits(mod, field_flags);
    }
}
