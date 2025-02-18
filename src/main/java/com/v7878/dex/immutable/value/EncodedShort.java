package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedShort extends EncodedValue {
    private final short value;

    private EncodedShort(short value) {
        this.value = value;
    }

    public static EncodedShort of(short value) {
        if (value >= Cache.BEGIN && value < Cache.END) {
            return Cache.cache[value - Cache.BEGIN];
        }
        return new EncodedShort(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.SHORT;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

    public short getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedShort other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Short.compare(getValue(), ((EncodedShort) other).getValue());
    }

    private static final class Cache {
        static final short BEGIN = -128;
        static final int SIZE = 256;
        static final short END = BEGIN + SIZE;
        static final EncodedShort[] cache;

        private Cache() {
        }

        static {
            var array = new EncodedShort[SIZE];

            short value = BEGIN;
            for (int i = 0; i < SIZE; i++) {
                array[i] = new EncodedShort(value++);
            }

            cache = array;
        }
    }
}
