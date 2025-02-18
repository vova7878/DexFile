package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedByte extends EncodedValue {
    private final byte value;

    private EncodedByte(byte value) {
        this.value = value;
    }

    public static EncodedByte of(byte value) {
        return Cache.cache[value - Cache.BEGIN];
    }

    @Override
    public ValueType getValueType() {
        return ValueType.BYTE;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedByte other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Byte.compare(getValue(), ((EncodedByte) other).getValue());
    }

    private static final class Cache {
        static final byte BEGIN = -128;
        static final int SIZE = 256;
        static final EncodedByte[] cache;

        private Cache() {
        }

        static {
            var array = new EncodedByte[SIZE];

            byte value = BEGIN;
            for (int i = 0; i < SIZE; i++) {
                array[i] = new EncodedByte(value++);
            }

            cache = array;
        }
    }
}
