package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;
import java.util.Objects;

public class MethodHandleItem implements PublicCloneable {

    public static final int SIZE = 0x08;

    public static final Comparator<MethodHandleItem> COMPARATOR = (a, b) -> {
        int out = Integer.compare(a.type.value, b.type.value);
        if (out != 0) {
            return out;
        }

        // now a.type == b.type
        if (a.type.isMethodAccess()) {
            return MethodId.COMPARATOR.compare((MethodId) a.field_or_method,
                    (MethodId) b.field_or_method);
        } else {
            return FieldId.COMPARATOR.compare((FieldId) a.field_or_method,
                    (FieldId) b.field_or_method);
        }
    };

    public enum MethodHandleType {
        STATIC_PUT(0x00, false),
        STATIC_GET(0x01, false),
        INSTANCE_PUT(0x02, false),
        INSTANCE_GET(0x03, false),
        INVOKE_STATIC(0x04, true),
        INVOKE_INSTANCE(0x05, true),
        INVOKE_CONSTRUCTOR(0x06, true),
        INVOKE_DIRECT(0x07, true),
        INVOKE_INTERFACE(0x08, true);

        private final int value;
        private final boolean isMethod;

        MethodHandleType(int value, boolean isMethod) {
            this.value = value;
            this.isMethod = isMethod;
        }

        public boolean isMethodAccess() {
            return isMethod;
        }

        public boolean isFieldAccess() {
            return !isMethod;
        }

        public static MethodHandleType of(int int_type) {
            for (MethodHandleType type : values()) {
                if (int_type == type.value) {
                    return type;
                }
            }
            throw new IllegalStateException("unknown method handle type: " + int_type);
        }
    }

    private MethodHandleType type;
    private FieldOrMethodId field_or_method;

    public MethodHandleItem(MethodHandleType type, FieldOrMethodId field_or_method) {
        setType(type);
        setFieldOrMethod(field_or_method);
    }

    public final void setType(MethodHandleType type) {
        this.type = Objects.requireNonNull(type, "method handle type can`n be null");
    }

    public final MethodHandleType getType() {
        return type;
    }

    public final void setFieldOrMethod(FieldOrMethodId field_or_method) {
        this.field_or_method = Objects.requireNonNull(field_or_method,
                "field_or_method can`t be null").clone();
    }

    public final FieldOrMethodId getFieldOrMethod() {
        return field_or_method;
    }

    public static MethodHandleItem read(RandomInput in, ReadContext context) {
        MethodHandleType type = MethodHandleType.of(in.readUnsignedShort());
        in.addPosition(2); //unused
        int field_or_method_id = in.readUnsignedShort();
        in.addPosition(2); //unused
        FieldOrMethodId field_or_method = type.isMethodAccess()
                ? context.method(field_or_method_id)
                : context.field(field_or_method_id);
        return new MethodHandleItem(type, field_or_method);
    }

    public void collectData(DataCollector data) {
        if (type.isMethodAccess()) {
            data.add((MethodId) field_or_method);
        } else {
            data.add((FieldId) field_or_method);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeShort(type.value);
        out.writeShort(0);
        out.writeShort(type.isMethodAccess()
                ? context.getMethodIndex((MethodId) field_or_method)
                : context.getFieldIndex((FieldId) field_or_method));
        out.writeShort(0);
    }

    @Override
    public String toString() {
        return "MethodHandle{" + "type = " + type + "; " + field_or_method + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodHandleItem) {
            MethodHandleItem mhobj = (MethodHandleItem) obj;
            return Objects.equals(type, mhobj.type)
                    && Objects.equals(field_or_method, mhobj.field_or_method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, field_or_method);
    }

    @Override
    public MethodHandleItem clone() {
        return new MethodHandleItem(type, field_or_method);
    }
}
