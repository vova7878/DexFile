package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;
import java.util.Objects;

public class TypeId implements PublicCloneable {

    public static final TypeId V = new TypeId("V");
    public static final TypeId Z = new TypeId("Z");
    public static final TypeId B = new TypeId("B");
    public static final TypeId S = new TypeId("S");
    public static final TypeId C = new TypeId("C");
    public static final TypeId I = new TypeId("I");
    public static final TypeId F = new TypeId("F");
    public static final TypeId J = new TypeId("J");
    public static final TypeId D = new TypeId("D");

    public static final int SIZE = 0x04;

    public static final Comparator<TypeId> COMPARATOR =
            (a, b) -> StringId.COMPARATOR.compare(a.descriptor, b.descriptor);

    public static TypeId of(String class_name) {
        Objects.requireNonNull(class_name, "trying to get TypeId of null");
        int array_depth = 0;
        while (class_name.endsWith("[]")) {
            array_depth++;
            class_name = class_name.substring(0, class_name.length() - 2);
        }
        switch (class_name) {
            case "void":
                class_name = "V";
                break;
            case "boolean":
                class_name = "Z";
                break;
            case "byte":
                class_name = "B";
                break;
            case "short":
                class_name = "S";
                break;
            case "char":
                class_name = "C";
                break;
            case "int":
                class_name = "I";
                break;
            case "float":
                class_name = "F";
                break;
            case "long":
                class_name = "J";
                break;
            case "double":
                class_name = "D";
                break;
            default:
                class_name = "L" + class_name.replace('.', '/') + ";";
                break;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < array_depth; i++) {
            out.append('[');
        }
        out.append(class_name);
        return new TypeId(out.toString());
    }

    public static TypeId of(Class<?> clazz) {
        String class_name = clazz.getName();
        if (class_name.startsWith("[")) {
            return new TypeId(class_name.replace('.', '/'));
        }
        return of(class_name);
    }

    private String descriptor;

    public TypeId(String descriptor) {
        setDescriptor(descriptor);
    }

    public final void setDescriptor(String descriptor) {
        this.descriptor = Objects.requireNonNull(
                descriptor, "type descriptor can`t be null");
    }

    public final String getDescriptor() {
        return descriptor;
    }

    public final char getShorty() {
        char c = descriptor.charAt(0);
        return c == '[' ? 'L' : c;
    }

    public final int getRegistersCount() {
        return equals(V) ? 0 : equals(D) || equals(J) ? 2 : 1;
    }

    public static TypeId read(RandomInput in, ReadContext context) {
        return new TypeId(context.string(in.readInt()));
    }

    public void collectData(DataCollector data) {
        data.add(descriptor);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getStringIndex(descriptor));
    }

    @Override
    public String toString() {
        return descriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeId) {
            TypeId tobj = (TypeId) obj;
            return Objects.equals(descriptor, tobj.descriptor);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor);
    }

    @Override
    public TypeId clone() {
        return new TypeId(descriptor);
    }
}
