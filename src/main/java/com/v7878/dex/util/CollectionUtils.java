package com.v7878.dex.util;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.function.ToIntFunction;

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

    private record RedirectedComparable<T>(ToIntFunction<T> comparator) implements Comparable<T> {
        @Override
        public int compareTo(T value) {
            // Inversion, since it is the reverse comparison:
            //  We compare "this" with the external value, but it should be the opposite
            return -comparator.applyAsInt(value);
        }
    }

    public static <T> Comparable<T> comparable(ToIntFunction<T> comparator) {
        return new RedirectedComparable<>(comparator);
    }

    @SuppressWarnings("unchecked")
    public static <E> E lower(NavigableSet<E> set, Comparable<E> e) {
        return set.lower((E) e);
    }

    @SuppressWarnings("unchecked")
    public static <E> E floor(NavigableSet<E> set, Comparable<E> e) {
        return set.floor((E) e);
    }

    @SuppressWarnings("unchecked")
    public static <E> E ceiling(NavigableSet<E> set, Comparable<E> e) {
        return set.ceiling((E) e);
    }

    @SuppressWarnings("unchecked")
    public static <E> E higher(NavigableSet<E> set, Comparable<E> e) {
        return set.higher((E) e);
    }

    public static <E> E findValue(NavigableSet<E> set, Comparable<E> e) {
        Objects.requireNonNull(e);
        var floor = floor(set, e);
        return e.compareTo(floor) == 0 ? floor : null;
    }

    @SuppressWarnings("unchecked")
    public static <E> NavigableSet<E> subSet(NavigableSet<E> set,
                                             Comparable<E> fromElement, boolean fromInclusive,
                                             Comparable<E> toElement, boolean toInclusive) {
        return set.subSet((E) fromElement, fromInclusive, (E) toElement, toInclusive);
    }

    @SuppressWarnings("unchecked")
    public static <E> NavigableSet<E> headSet(NavigableSet<E> set, Comparable<E> toElement, boolean inclusive) {
        return set.headSet((E) toElement, inclusive);
    }

    @SuppressWarnings("unchecked")
    public static <E> NavigableSet<E> tailSet(NavigableSet<E> set, Comparable<E> fromElement, boolean inclusive) {
        return set.tailSet((E) fromElement, inclusive);
    }
}
