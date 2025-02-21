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

public class ItemConverter {
    public static <T> List<T> toMutableList(Iterable<T> iterable) {
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
                Objects.requireNonNull(tmp);
                list.add(tmp);
            }
            list.trimToSize();
        }

        return list;
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        ArrayList<T> list;
        if (iterable instanceof Collection<T> collection) {
            list = new ArrayList<>(collection);
            list.forEach(Objects::requireNonNull);
        } else {
            list = new ArrayList<>();
            for (T tmp : iterable) {
                Objects.requireNonNull(tmp);
                list.add(tmp);
            }
            list.trimToSize();
        }

        return Collections.unmodifiableList(list);
    }

    public static <T extends Comparable<? super T>> NavigableSet<T>
    toMutableNavigableSet(Iterable<T> iterable) {
        NavigableSet<T> set = new TreeSet<>();

        if (iterable == null) {
            return set;
        }

        for (T tmp : iterable) {
            set.add(Objects.requireNonNull(tmp));
        }

        return set;
    }

    public static <T extends Comparable<? super T>> NavigableSet<T>
    toNavigableSet(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<T> set = new TreeSet<>();
        for (T tmp : iterable) {
            set.add(Objects.requireNonNull(tmp));
        }

        return Collections.unmodifiableNavigableSet(set);
    }

    public static <R, P> List<R> transformList(List<P> list, Function<P, R> transformer) {
        return new AbstractList<R>() {
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
}
