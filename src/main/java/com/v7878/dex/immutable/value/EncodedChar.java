package com.v7878.dex.immutable.value;

import com.v7878.dex.ValueType;

public final class EncodedChar extends EncodedValue {
    private final char value;

    private EncodedChar(char value) {
        this.value = value;
    }

    public static EncodedChar of(char value) {
        if (value < Cache.END) {
            return Cache.cache[value - Cache.BEGIN];
        }
        return new EncodedChar(value);
    }

    @Override
    public ValueType getValueType() {
        return ValueType.CHAR;
    }

    @Override
    public boolean isDefault() {
        return value == 0;
    }

    public char getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Character.hashCode(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedChar other
                && getValue() == other.getValue();
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        return Character.compare(getValue(), ((EncodedChar) other).getValue());
    }

    private static final class Cache {
        static final char BEGIN = 0;
        static final int SIZE = 256;
        static final char END = BEGIN + SIZE;
        static final EncodedChar[] cache;

        private Cache() {
        }

        static {
            var array = new EncodedChar[SIZE];

            char value = BEGIN;
            for (int i = 0; i < SIZE; i++) {
                array[i] = new EncodedChar(value++);
            }

            cache = array;
        }
    }
}
