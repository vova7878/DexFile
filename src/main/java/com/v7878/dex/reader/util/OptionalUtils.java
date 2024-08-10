package com.v7878.dex.reader.util;

import java.util.function.IntFunction;

public class OptionalUtils {
    public static <T> T getOrDefault(int index, int no_index, IntFunction<T> getter, T default_value) {
        if (index == no_index) {
            return default_value;
        }
        return getter.apply(index);
    }
}
