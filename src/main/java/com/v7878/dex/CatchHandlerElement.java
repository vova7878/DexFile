package com.v7878.dex;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Objects;

public class CatchHandlerElement implements PublicCloneable {

    private TypeId type;
    private int address;

    public CatchHandlerElement(TypeId type, int address) {
        setType(type);
        setAddress(address);
    }

    public final void setType(TypeId type) {
        this.type = Objects.requireNonNull(type,
                "catch handler type can`t be null").clone();
    }

    public final TypeId getType() {
        return type;
    }

    public final void setAddress(int address) {
        if (address < 0) {
            throw new IllegalArgumentException("instruction address can`t be negative");
        }
        this.address = address;
    }

    public final int getAddress() {
        return address;
    }

    public static CatchHandlerElement read(RandomInput in, ReadContext context) {
        return new CatchHandlerElement(context.type(in.readULeb128()), in.readULeb128());
    }

    public void collectData(DataCollector data) {
        data.add(type);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(context.getTypeIndex(type));
        out.writeULeb128(address);
    }

    @Override
    public String toString() {
        return type + " " + address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CatchHandlerElement) {
            CatchHandlerElement eobj = (CatchHandlerElement) obj;
            return address == eobj.address
                    && Objects.equals(type, eobj.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, type);
    }

    @Override
    public CatchHandlerElement clone() {
        return new CatchHandlerElement(type, address);
    }
}
