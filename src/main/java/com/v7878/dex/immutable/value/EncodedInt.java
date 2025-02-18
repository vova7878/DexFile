package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedInt extends EncodedValue {
    private final int value;

    private EncodedInt(int value) {
        this.value = value;
    }

    public static EncodedInt of(int value) {
        if (value >= Cache.BEGIN && value < Cache.END) {
            return Cache.cache[value - Cache.BEGIN];
        }
        return new EncodedInt(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.INT;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedInt other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Integer.compare(getValue(), ((EncodedInt) other).getValue());
    }

    private static final class Cache {
        static final int BEGIN = -128;
        static final int SIZE = 256;
        static final int END = BEGIN + SIZE;
        static final EncodedInt[] cache;

        private Cache() {
        }

        static {
            var array = new EncodedInt[SIZE];

            int value = BEGIN;
            for (int i = 0; i < SIZE; i++) {
                array[i] = new EncodedInt(value++);
            }

            cache = array;
        }
    }
}
