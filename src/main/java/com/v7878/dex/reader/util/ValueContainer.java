package com.v7878.dex.reader.util;

public record ValueContainer<T>(T value) {
    private static final ValueContainer<?> NULL = new ValueContainer<>(null);

    public static <T> ValueContainer<T> of(T value) {
        if (value == null) {
            //noinspection unchecked
            return (ValueContainer<T>) NULL;
        }
        return new ValueContainer<>(value);
    }
}
