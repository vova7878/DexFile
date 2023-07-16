package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.PCList;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class TypeList extends PCList<TypeId> {

    public static final int ALIGNMENT = 4;

    public static final Comparator<TypeList> COMPARATOR
            = PCList.getComparator(TypeId.COMPARATOR);

    public TypeList(TypeId... types) {
        super(types);
    }

    @Override
    protected TypeId check(TypeId type) {
        return Objects.requireNonNull(type,
                "TypeList can`t contain null type");
    }

    public static TypeList read(RandomInput in, ReadContext context) {
        TypeList out = new TypeList();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            out.add(context.type(in.readUnsignedShort()));
        }
        return out;
    }

    public static TypeList empty() {
        return new TypeList();
    }

    public void collectData(DataCollector data) {
        for (TypeId tmp : this) {
            data.add(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeInt(size());
        for (TypeId tmp : this) {
            out.writeShort(context.getTypeIndex(tmp));
        }
    }

    @Override
    public String toString() {
        return stream().map(TypeId::toString)
                .collect(Collectors.joining("", "(", ")"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeList) {
            return super.equals(obj);
        }
        return false;
    }

    @Override
    public TypeList clone() {
        TypeList out = new TypeList();
        out.addAll(this);
        return out;
    }
}
