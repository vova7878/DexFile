package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;

public class FieldId extends FieldOrMethodId {

    public static final int SIZE = 0x08;

    public static final Comparator<FieldId> COMPARATOR = (a, b) -> {
        int out = TypeId.COMPARATOR.compare(a.getDeclaringClass(), b.getDeclaringClass());
        if (out != 0) {
            return out;
        }

        out = StringId.COMPARATOR.compare(a.getName(), b.getName());
        if (out != 0) {
            return out;
        }

        return TypeId.COMPARATOR.compare(a.type, b.type);
    };

    public static FieldId of(Field field) {
        Objects.requireNonNull(field, "trying to get FieldId of null");
        return new FieldId(TypeId.of(field.getDeclaringClass()),
                TypeId.of(field.getType()), field.getName());
    }

    public static FieldId of(Enum<?> e) {
        Objects.requireNonNull(e, "trying to get FieldId of null");
        TypeId declaring_class = TypeId.of(e.getDeclaringClass());
        return new FieldId(declaring_class, declaring_class, e.name());
    }

    private TypeId type;

    public FieldId(TypeId declaring_class, TypeId type, String name) {
        super(declaring_class, name);
        setType(type);
    }

    public final void setType(TypeId type) {
        this.type = Objects.requireNonNull(type,
                "type can`t be null").clone();
    }

    public final TypeId getType() {
        return type;
    }

    public static FieldId read(RandomInput in, ReadContext context) {
        return new FieldId(
                context.type(in.readUnsignedShort()),
                context.type(in.readUnsignedShort()),
                context.string(in.readInt())
        );
    }

    @Override
    public void collectData(DataCollector data) {
        data.add(type);
        super.collectData(data);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeShort(context.getTypeIndex(getDeclaringClass()));
        out.writeShort(context.getTypeIndex(type));
        out.writeInt(context.getStringIndex(getName()));
    }

    @Override
    public String toString() {
        return getDeclaringClass() + "." + getName() + ":" + type;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof FieldId) {
            FieldId fobj = (FieldId) obj;
            return Objects.equals(type, fobj.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    public FieldId clone() {
        return new FieldId(getDeclaringClass(), type, getName());
    }
}
