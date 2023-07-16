package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;
import java.util.Objects;

public class AnnotationElement implements PublicCloneable {

    public static final Comparator<AnnotationElement> COMPARATOR = (a, b) -> {
        int out = StringId.COMPARATOR.compare(a.name, b.name);
        if (out != 0) {
            return out;
        }

        // a.name == b.name
        //TODO?
        throw new IllegalStateException(
                "can`t compare annotation elements with same name: " + a + " " + b);
    };

    private String name;
    private EncodedValue value;

    public AnnotationElement(String name, EncodedValue value) {
        setName(name);
        setValue(value);
    }

    public final void setName(String name) {
        this.name = Objects.requireNonNull(name, "name can`t be null");
    }

    public final String getName() {
        return name;
    }

    public final void setValue(EncodedValue value) {
        this.value = Objects.requireNonNull(value,
                "value can`t be null").clone();
    }

    public final EncodedValue getValue() {
        return value;
    }

    public static AnnotationElement read(RandomInput in, ReadContext context) {
        String name = context.string(in.readULeb128());
        EncodedValue value = EncodedValueReader.readValue(in, context);
        return new AnnotationElement(name, value);
    }

    public void collectData(DataCollector data) {
        data.add(name);
        data.fill(value);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(context.getStringIndex(name));
        value.write(context, out);
    }

    @Override
    public String toString() {
        return "AnnotationElement{" + name + " = " + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationElement) {
            AnnotationElement aeobj = (AnnotationElement) obj;
            return Objects.equals(name, aeobj.name)
                    && Objects.equals(value, aeobj.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public AnnotationElement clone() {
        return new AnnotationElement(name, value);
    }
}
