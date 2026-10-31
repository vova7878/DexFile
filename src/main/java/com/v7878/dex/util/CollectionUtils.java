package com.v7878.dex.util;

import java.util.AbstractList;
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

    public static <T extends Comparable<? super T>> int compareNullable(T left, T right) {
        return left == null ?
                (right == null ? 0 : -1) :
                (right == null ? 1 : left.compareTo(right));
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

    public static <T> List<T> reverseList(List<T> list) {
        if (list == null) return null;
        return new AbstractList<>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public T get(int index) {
                return list.get(list.size() - index - 1);
            }
        };
    }

    public static <E extends Comparable<E>> E findValue(NavigableSet<E> set, E e) {
        Objects.requireNonNull(e);

        var floor = set.floor(e);
        if (floor == null) return null;
        return e.compareTo(floor) == 0 ? floor : null;
    }

    public static <T> List<T> toUnmodifiableList(Stream<T> stream) {
        //noinspection FuseStreamOperations
        return Collections.unmodifiableList(stream.collect(Collectors.toList()));
    }

    public static <T> void replaceAll(NavigableSet<T> set, Iterable<T> iterable) {
        if (iterable == null) return;
        for (T tmp : iterable) {
            Objects.requireNonNull(tmp);
            set.remove(tmp);
            set.add(tmp);
        }
    }

    public static <T> void addAll(NavigableSet<T> set, Iterable<T> iterable) {
        if (iterable == null) return;
        for (T tmp : iterable) {
            set.add(Objects.requireNonNull(tmp));
        }
    }

    public static <T> void addAll(List<T> list, Iterable<T> iterable) {
        if (iterable == null) return;
        for (T tmp : iterable) {
            list.add(Objects.requireNonNull(tmp));
        }
    }

    public static <T> void removeAll(NavigableSet<T> set, Iterable<T> iterable) {
        if (iterable == null) return;
        // You cannot use removeAll here because it
        //  compares objects using equals() instead of compare()
        for (T tmp : iterable) {
            set.remove(tmp);
        }
    }

    public static <T> void removeAll(List<T> list, Iterable<T> iterable) {
        if (iterable == null) return;
        if (iterable instanceof Collection<T> collection) {
            list.removeAll(collection);
        } else for (T tmp : iterable) {
            while (list.remove(tmp)) { /* nop */ }
        }
    }

    /**
     * Compares two ordered sets by {@link Object#equals(Object)}, never through
     * {@link java.util.Set#contains(Object)}, {@link java.util.Set#equals(Object)}
     * or {@link java.util.Set#hashCode()}.
     * <p>
     * Both arguments must be sets with natural ordering, so equal content always
     * produces the same iteration order. The elements are walked pairwise in
     * iteration order and compared with {@code equals}; equality is element-wise
     * structural equality and never uses {@code compareTo}.
     * <p>
     * A set with a custom comparator is a misuse in the model and is rejected
     * with {@link IllegalArgumentException}.
     */
    public static boolean setEquals(NavigableSet<?> left, NavigableSet<?> right) {
        checkNaturalOrder(left);
        checkNaturalOrder(right);
        if (left == right) return true;
        if (left == null || right == null || left.size() != right.size()) {
            return false;
        }
        var leftIterator = left.iterator();
        var rightIterator = right.iterator();
        while (leftIterator.hasNext() && rightIterator.hasNext()) {
            if (!Objects.equals(leftIterator.next(), rightIterator.next())) {
                return false;
            }
        }
        return true;
    }

    private static void checkNaturalOrder(NavigableSet<?> set) {
        if (set != null && set.comparator() != null) {
            throw new IllegalArgumentException(
                    "Custom comparators are not supported: the model requires natural ordering");
        }
    }

    /**
     * Hashes the elements of a naturally ordered set in iteration order.
     * Consistent with {@link #setEquals(NavigableSet, NavigableSet)}: naturally
     * ordered sets with equal content always have the same iteration order, so
     * they always produce the same hash.
     * <p>
     * The combination is order-dependent, which is only well-defined for
     * ordered sets: a plain hash set has no deterministic iteration order and
     * must not be passed here.
     * <p>
     * The argument must be a set with natural ordering; a set with a custom
     * comparator is rejected with {@link IllegalArgumentException}, because
     * otherwise {@code equals} and {@code hashCode} could disagree.
     */
    public static int setHashCode(NavigableSet<?> set) {
        checkNaturalOrder(set);
        int hash = 0;
        for (var value : set) {
            hash = 31 * hash + Objects.hashCode(value);
        }
        return hash;
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
