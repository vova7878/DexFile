package com.v7878.dex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class ItemConverter {
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
        }

        return Collections.unmodifiableList(list);
    }

    public static <T> Set<T> toSet(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptySet();
        }

        Set<T> set;
        if (iterable instanceof Collection<T> collection) {
            set = new HashSet<>(collection.size());
        } else {
            set = new HashSet<>();
        }
        for (T tmp : iterable) {
            Objects.requireNonNull(tmp);
            set.add(tmp);
        }

        return Collections.unmodifiableSet(set);
    }

    public static <T> NavigableSet<T> toNavigableSet(
            Comparator<? super T> comparator, Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyNavigableSet();
        }

        NavigableSet<T> set;
        set = new TreeSet<>(comparator);
        for (T tmp : iterable) {
            Objects.requireNonNull(tmp);
            set.add(tmp);
        }

        return Collections.unmodifiableNavigableSet(set);
    }

    public static <T extends Comparable<? super T>> NavigableSet<T>
    toNavigableSet(Iterable<T> iterable) {
        return toNavigableSet(CollectionUtils.naturalOrder(), iterable);
    }
}
