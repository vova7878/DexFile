package com.v7878.dex.util;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Objects;

public class CollectionUtils {
    public static <T extends Comparable<? super T>> int compareNonNull(T left, T right) {
        return left.compareTo(right);
    }

    public static <T extends Comparable<? super T>> int compareLexicographically(
            Iterable<? extends T> left, Iterable<? extends T> right) {
        var i1 = left.iterator();
        var i2 = right.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            int out = compareNonNull(i1.next(), i2.next());
            if (out != 0) return out;
        }
        return Boolean.compare(i1.hasNext(), i2.hasNext());
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


    @SuppressWarnings("unchecked")
    public static <E> E findValue(NavigableSet<E> set, E e) {
        Objects.requireNonNull(e);

        var floor = set.floor(e);
        if (floor == null) return null;
        return ((Comparable<E>) e).compareTo(floor) == 0 ? floor : null;
    }
}
