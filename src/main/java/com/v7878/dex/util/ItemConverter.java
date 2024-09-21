package com.v7878.dex.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class ItemConverter {
    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        if (iterable instanceof List<T> list) {
            return Collections.unmodifiableList(list);
        }

        ArrayList<T> list;
        if (iterable instanceof Collection<T> collection) {
            list = new ArrayList<>(collection);
        } else {
            list = new ArrayList<>();
            for (T tmp : iterable) {
                list.add(tmp);
            }
        }

        return Collections.unmodifiableList(list);
    }

    public static <T> Set<T> toSet(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptySet();
        }

        if (iterable instanceof Set<T> list) {
            return Collections.unmodifiableSet(list);
        }

        Set<T> set;
        if (iterable instanceof Collection<T> collection) {
            set = new HashSet<>(collection);
        } else {
            set = new HashSet<>();
            for (T tmp : iterable) {
                set.add(tmp);
            }
        }

        return Collections.unmodifiableSet(set);
    }

    public static <T> NavigableSet<T> toNavigableSet(
            Comparator<T> comparator, Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyNavigableSet();
        }

        if (iterable instanceof NavigableSet<T> ns
                && comparator.equals(ns.comparator())) {
            return Collections.unmodifiableNavigableSet(ns);
        }

        NavigableSet<T> set;
        set = new TreeSet<>(comparator);
        for (T tmp : iterable) {
            set.add(tmp);
        }

        return Collections.unmodifiableNavigableSet(set);
    }

    public static <T extends Comparable<? super T>> NavigableSet<T>
    toNavigableSet(Iterable<T> iterable) {
        return toNavigableSet(CollectionUtils.naturalOrder(), iterable);
    }
}
