package com.v7878.dex.raw;

import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public class SharedData {
    public static class StringPosition implements Comparable<StringPosition> {
        public final String value;
        public int offset;

        public StringPosition(String value) {
            this.value = Objects.requireNonNull(value);
            this.offset = -1;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof StringPosition other
                    && Objects.equals(value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public int compareTo(StringPosition o) {
            return value.compareTo(o.value);
        }
    }

    public final NavigableMap<String, StringPosition> strings;

    public int string_data_items_off;

    public SharedData() {
        strings = new TreeMap<>();
    }

    public StringPosition addString(String value) {
        return strings.computeIfAbsent(value, StringPosition::new);
    }
}
