package com.v7878.dex.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class Converter {
    @SafeVarargs
    public static <T> List<T> toList(T... elements) {
        Objects.requireNonNull(elements);
        return List.of(elements);
    }

    public static <T> List<T> mutableList(Iterable<T> iterable) {
        var list = new ArrayList<T>();

        if (iterable == null) {
            return list;
        }

        if (iterable instanceof Collection<T> collection) {
            list = new ArrayList<>(collection);
            list.forEach(Objects::requireNonNull);
        } else {
            list = new ArrayList<>();
            for (T tmp : iterable) {
                list.add(Objects.requireNonNull(tmp));
            }
            list.trimToSize();
        }

        return list;
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                mutableList(iterable));
    }

    public static <T extends Comparable<? super T>>
    NavigableSet<T> mutableNavigableSet(Iterable<T> iterable) {
        NavigableSet<T> set = new TreeSet<>();

        if (iterable == null) {
            return set;
        }

        if (iterable instanceof Collection<T> collection) {
            set.addAll(collection);
            set.forEach(Objects::requireNonNull);
        } else {
            for (T tmp : iterable) {
                set.add(Objects.requireNonNull(tmp));
            }
        }

        return set;
    }

    public static <T extends Comparable<? super T>>
    NavigableSet<T> toNavigableSet(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyNavigableSet();
        }

        return Collections.unmodifiableNavigableSet(
                mutableNavigableSet(iterable));
    }

    public static <R, P> List<R> transformList(List<P> list, Function<P, R> transformer) {
        return new AbstractList<>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public R get(int i) {
                return transformer.apply(list.get(i));
            }
        };
    }

    public static <T> List<T> listOf(int size, T value) {
        return new AbstractList<>() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public T get(int i) {
                Objects.checkIndex(i, size);
                return value;
            }
        };
    }

    public static <R, P> List<R> mutableTransform(Collection<P> data, Function<P, R> transformer) {
        var out = new ArrayList<R>(data.size());
        for (var tmp : data) {
            out.add(transformer.apply(tmp));
        }
        return out;
    }

    public static <R, P> List<R> transform(Collection<P> data, Function<P, R> transformer) {
        return Collections.unmodifiableList(mutableTransform(data, transformer));
    }

    public static <R, P> List<R> mutableTransform(P[] array, Function<P, R> transformer) {
        var out = new ArrayList<R>(array.length);
        for (var tmp : array) {
            out.add(transformer.apply(tmp));
        }
        return out;
    }

    public static <R, P> List<R> transform(P[] array, Function<P, R> transformer) {
        return Collections.unmodifiableList(mutableTransform(array, transformer));
    }

    public static <R, P> R[] transform(Collection<P> data, Function<P, R> transformer, IntFunction<R[]> arr) {
        R[] out = arr.apply(data.size());
        int i = 0;
        for (var value : data) {
            out[i] = transformer.apply(value);
            i++;
        }
        return out;
    }

    public static <R, I, P> List<R> minimize(List<P> data, Function<P, I> transformer1,
                                             Function<I, R> transformer2, Predicate<I> checker) {
        int size = data.size();
        for (; size > 0; size--) {
            var tmp = transformer1.apply(data.get(size - 1));
            if (!checker.test(tmp)) break;
        }
        if (size == 0) {
            return Collections.emptyList();
        }
        var out = new ArrayList<R>(size);
        for (int i = 0; i < size; i++) {
            var tmp = transformer1.apply(data.get(i));
            out.add(transformer2.apply(tmp));
        }
        return Collections.unmodifiableList(out);
    }
}
