package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;
import java.util.Objects;

public class CallSiteId implements PublicCloneable {

    public static final int SIZE = 0x04;

    public static final Comparator<CallSiteId> COMPARATOR =
            (a, b) -> EncodedValue.ArrayValue.COMPARATOR.compare(a.value, b.value);

    private EncodedValue.ArrayValue value;

    public CallSiteId(EncodedValue.ArrayValue value) {
        setValue(value);
    }

    public final void setValue(EncodedValue.ArrayValue value) {
        this.value = Objects.requireNonNull(value,
                "call site value can`t be null").clone();
    }

    public final EncodedValue.ArrayValue getValue() {
        return value;
    }

    public static CallSiteId read(RandomInput in, ReadContext context) {
        RandomInput in2 = in.duplicate(in.readInt());
        EncodedValue.ArrayValue value = (EncodedValue.ArrayValue) EncodedValueReader
                .readValue(in2, context, EncodedValue.EncodedValueType.ARRAY);
        return new CallSiteId(value);
    }

    public void collectData(DataCollector data) {
        data.add(value);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(context.getArrayValueOffset(value));
    }

    @Override
    public String toString() {
        return "CallSiteId" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CallSiteId) {
            CallSiteId csobj = (CallSiteId) obj;
            return Objects.equals(value, csobj.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public CallSiteId clone() {
        return new CallSiteId(value);
    }
}
