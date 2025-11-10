package com.v7878.dex.immutable;

import static com.v7878.dex.util.CollectionUtils.toUnmodifiableList;
import static com.v7878.dex.util.ShortyUtils.invalidType;

import com.v7878.dex.Internal;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ShortyUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public final class TypeId implements Comparable<TypeId> {
    public static final TypeId V = new TypeId(0, "V");
    public static final TypeId Z = new TypeId(0, "Z");
    public static final TypeId B = new TypeId(0, "B");
    public static final TypeId S = new TypeId(0, "S");
    public static final TypeId C = new TypeId(0, "C");
    public static final TypeId I = new TypeId(0, "I");
    public static final TypeId F = new TypeId(0, "F");
    public static final TypeId J = new TypeId(0, "J");
    public static final TypeId D = new TypeId(0, "D");
    public static final TypeId OBJECT = new TypeId(0, "Ljava/lang/Object;");

    private final int array_depth;
    private final String descriptor;

    private TypeId(int array_depth, String descriptor) {
        this.array_depth = array_depth;
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    @Internal
    public static TypeId raw(int array_depth, String descriptor) {
        return switch (descriptor) {
            case "V" -> V;
            case "Z" -> Z;
            case "B" -> B;
            case "S" -> S;
            case "C" -> C;
            case "I" -> I;
            case "F" -> F;
            case "J" -> J;
            case "D" -> D;
            case "Ljava/lang/Object;" -> OBJECT;
            default -> new TypeId(array_depth, descriptor);
        };
    }

    @Internal
    public static TypeId raw(String descriptor) {
        int length = descriptor.length();
        int array_depth = 0;
        while (array_depth < length && descriptor.charAt(array_depth) == '[') {
            array_depth++;
        }
        return raw(array_depth, descriptor);
    }

    // Validates only special characters and descriptor shape
    private static int checkValidDescriptor(String descriptor) {
        if (descriptor == null || descriptor.isEmpty()) {
            return -1;
        }

        int length = descriptor.length();
        int array_depth = 0;
        while (descriptor.charAt(array_depth) == '[') {
            if (++array_depth >= length) {
                return -1;
            }
        }

        /*
         * We are looking for a descriptor. Either validate it as a
         * single-character primitive type, or continue on to check the
         * embedded class name (bracketed by "L" and ";")
         */
        int position = array_depth;
        switch (descriptor.charAt(position)) {
            case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z' -> {
                // These are all single-character descriptors for primitive types
                return (position + 1 == length) ? array_depth : -1;
            }
            case 'V' -> {
                // Non-array void is valid, but you can't have an array of void
                return (array_depth == 0 && length == 1) ? array_depth : -1;
            }
            case 'L' -> {
                // Class name: continue below
            }
            default -> {
                // Oddball descriptor character
                return -1;
            }
        }
        position++;

        /*
         * We just consumed the 'L' that introduces a class name as part
         * of a type descriptor, or we are looking for an unadorned class
         * name
         */

        // First character or just encountered a separator
        boolean sep_or_first = true;
        for (; position < length; position++) {
            switch (descriptor.charAt(position)) {
                case ';' -> {
                    /*
                     * Invalid character for a class name, but the
                     * legitimate end of a type descriptor. In the latter
                     * case, make sure that this is the end of the string
                     * and that it doesn't end with an empty component
                     * (including the degenerate case of "L;")
                     */
                    return (!sep_or_first && position + 1 == length) ? array_depth : -1;
                }
                case '.' -> {
                    // The wrong separator character
                    return -1;
                }
                case '/' -> {
                    if (sep_or_first) {
                        // Separator at start or two separators in a row
                        return -1;
                    }
                    sep_or_first = true;
                }
                default -> sep_or_first = false;
            }
        }
        /*
         * Premature end for a type descriptor, but valid for
         * a class name as long as we haven't encountered an
         * empty component (including the degenerate case of
         * the empty string "")
         */
        return sep_or_first ? -1 : array_depth;
    }

    public static TypeId of(String descriptor) {
        Objects.requireNonNull(descriptor);
        int depth = checkValidDescriptor(descriptor);
        if (depth < 0) {
            throw invalidType(descriptor);
        }
        return raw(depth, descriptor);
    }

    public static TypeId ofBinaryName(String name) {
        Objects.requireNonNull(name);
        if (name.startsWith("[")) {
            return of(name.replace('.', '/'));
        }
        return switch (name) {
            case "void" -> V;
            case "boolean" -> Z;
            case "byte" -> B;
            case "short" -> S;
            case "char" -> C;
            case "int" -> I;
            case "float" -> F;
            case "long" -> J;
            case "double" -> D;
            default -> of("L" + name.replace('.', '/') + ";");
        };
    }

    public static TypeId ofName(String name) {
        Objects.requireNonNull(name);
        int array_depth = 0;
        while (name.endsWith("[]")) {
            array_depth++;
            name = name.substring(0, name.length() - 2);
        }
        return ofBinaryName(name).array(array_depth);
    }

    public static TypeId of(Class<?> clazz) {
        String class_name = clazz.getName();
        if (clazz.isArray()) {
            return raw(class_name.replace('.', '/'));
        }
        return switch (class_name) {
            case "void" -> V;
            case "boolean" -> Z;
            case "byte" -> B;
            case "short" -> S;
            case "char" -> C;
            case "int" -> I;
            case "float" -> F;
            case "long" -> J;
            case "double" -> D;
            default -> raw(0, "L" + class_name.replace('.', '/') + ";");
        };
    }

    public static List<TypeId> listOf(Iterable<String> descriptors) {
        // TODO: improve performance
        return toUnmodifiableList(StreamSupport
                .stream(descriptors.spliterator(), false)
                .map(TypeId::of));
    }

    public static List<TypeId> listOf(String... descriptors) {
        // TODO: improve performance
        return listOf(Arrays.asList(descriptors));
    }

    public String getDescriptor() {
        return descriptor;
    }

    public char getShorty() {
        return ShortyUtils.getTypeShorty(this);
    }

    public boolean isPrimitive() {
        return getShorty() != 'L';
    }

    public int getRegisterCount() {
        return ShortyUtils.getRegisterCount(this);
    }

    public boolean isArray() {
        return array_depth > 0;
    }

    public int getArrayDepth() {
        return array_depth;
    }

    public TypeId array(int depth) {
        return raw(array_depth + depth, "[".repeat(depth) + descriptor);
    }

    public TypeId array() {
        return array(1);
    }

    public TypeId componentType() {
        if (array_depth == 0) {
            return null;
        }
        return raw(array_depth - 1, descriptor.substring(1));
    }

    public TypeId baseType() {
        if (array_depth == 0) {
            return this;
        }
        return raw(0, descriptor.substring(array_depth));
    }

    public String getBinaryName() {
        if (isArray()) {
            return descriptor.replace('/', '.');
        }
        return switch (descriptor) {
            case "V" -> "void";
            case "Z" -> "boolean";
            case "B" -> "byte";
            case "S" -> "short";
            case "C" -> "char";
            case "I" -> "int";
            case "F" -> "float";
            case "J" -> "long";
            case "D" -> "double";
            default -> descriptor.replace('/', '.')
                    .substring(1, descriptor.length() - 1);
        };
    }

    public String getName() {
        return baseType().getBinaryName() + "[]".repeat(array_depth);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDescriptor());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof TypeId other
                && Objects.equals(getDescriptor(), other.getDescriptor());
    }

    @Override
    public int compareTo(TypeId other) {
        if (other == this) return 0;
        return CollectionUtils.compareNonNull(getDescriptor(), other.getDescriptor());
    }

    @Override
    public String toString() {
        return getDescriptor();
    }
}
