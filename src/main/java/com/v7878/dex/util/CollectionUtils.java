package com.v7878.dex.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static <T> List<T> toUnmodifiableList(Stream<T> stream) {
        //noinspection FuseStreamOperations
        return Collections.unmodifiableList(stream.collect(Collectors.toList()));
    }

    public static <T> void removeAll(NavigableSet<T> set, Iterable<T> iterable) {
        // You cannot use removeAll here because it
        //  compares objects using equals() instead of compare()
        for (T tmp : iterable) {
            set.remove(tmp);
        }
    }

    public static <T> void removeAll(List<T> list, Iterable<T> iterable) {
        if (iterable instanceof Collection<T> collection) {
            list.removeAll(collection);
        } else {
            for (T tmp : iterable) {
                while (list.remove(tmp)) { /* nop */ }
            }
        }
    }

    public static <A extends Comparable<? super V>, V> int
    binarySearch(A[] a, V key) {
        return binarySearch0(a, 0, a.length, key);
    }

    public static <A extends Comparable<? super V>, V> int
    binarySearch(A[] a, int fromIndex, int toIndex, V key) {
        Objects.checkFromToIndex(fromIndex, toIndex, a.length);
        return binarySearch0(a, fromIndex, toIndex, key);
    }

    private static <A extends Comparable<? super V>, V> int
    binarySearch0(A[] a, int fromIndex, int toIndex, V key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = a[mid].compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    public static <A, V> int binarySearch(A[] a, V key, ToIntBiFunction<? super A, ? super V> c) {
        return binarySearch0(a, 0, a.length, key, c);
    }

    public static <A, V> int binarySearch(A[] a, int fromIndex, int toIndex,
                                          V key, ToIntBiFunction<? super A, ? super V> c) {
        Objects.checkFromToIndex(fromIndex, toIndex, a.length);
        return binarySearch0(a, fromIndex, toIndex, key, c);
    }

    private static <A, V> int binarySearch0(A[] a, int fromIndex, int toIndex,
                                            V key, ToIntBiFunction<? super A, ? super V> c) {
        Objects.requireNonNull(c);
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = c.applyAsInt(a[mid], key);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
}
