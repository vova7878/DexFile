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

public class Converter {
    @SafeVarargs
    public static <T> List<T> toList(T... elements) {
        Objects.requireNonNull(elements);
        return List.of(elements);
    }

    public static <T> List<T> mutableList(Iterable<T> iterable) {
        if (iterable == null) {
            return new ArrayList<>();
        }

        ArrayList<T> list;
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

    public static <R, P> List<R> mutableTransform(List<P> list, Function<P, R> transformer) {
        var out = new ArrayList<R>(list.size());
        for (var tmp : list) {
            out.add(transformer.apply(tmp));
        }
        return out;
    }

    public static <R, P> List<R> transform(List<P> list, Function<P, R> transformer) {
        return Collections.unmodifiableList(mutableTransform(list, transformer));
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
}
