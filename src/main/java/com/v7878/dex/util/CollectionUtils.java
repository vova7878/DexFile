package com.v7878.dex.util;

import java.util.Comparator;

public class CollectionUtils {
    public static <T extends Comparable<? super T>> int compareNonNull(T left, T right) {
        return left.compareTo(right);
    }

    public static <T extends Comparable<? super T>> int compareLexicographically(
            Iterable<? extends T> left, Iterable<? extends T> right) {
        return compareLexicographically(naturalOrder(), left, right);
    }

    public static <T> int compareLexicographically(
            Comparator<? super T> comparator, Iterable<? extends T> left, Iterable<? extends T> right) {
        var i1 = left.iterator();
        var i2 = right.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            int out = comparator.compare(i1.next(), i2.next());
            if (out != 0) return out;
        }
        return Boolean.compare(i1.hasNext(), i2.hasNext());
    }

    public static <T extends Comparable<? super T>> Comparator<? super T> naturalOrder() {
        //noinspection unchecked
        return (Comparator<? super T>) NaturalOrder.INSTANCE;
    }

    private final static class NaturalOrder<T extends Comparable<? super T>> implements Comparator<T> {
        public static final NaturalOrder<?> INSTANCE = new NaturalOrder<>();

        private NaturalOrder() {
        }

        @Override
        public int compare(T left, T right) {
            return compareNonNull(left, right);
        }
    }
}
