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
import java.util.function.Function;
import java.util.function.Predicate;

public class ItemConverter {
    public static <To, From> List<To> toList(
            Function<From, To> transform,
            Predicate<From> is_transformed,
            Iterable<? extends From> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        boolean needsCopy = false;
        if (iterable instanceof List) {
            for (From element : iterable) {
                if (!is_transformed.test(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            //noinspection unchecked
            return Collections.unmodifiableList((List<To>) iterable);
        }

        var iter = iterable.iterator();
        ArrayList<To> list = iter instanceof Collection<?> col ?
                new ArrayList<>(col.size()) : new ArrayList<>();
        while (iter.hasNext()) {
            list.add(transform.apply(iter.next()));
        }

        return Collections.unmodifiableList(list);
    }

    public static <To, From> Set<To> toSet(
            Function<From, To> transform,
            Predicate<From> is_transformed,
            Iterable<? extends From> iterable) {
        if (iterable == null) {
            return Collections.emptySet();
        }

        boolean needsCopy = false;
        if (iterable instanceof Set) {
            for (From element : iterable) {
                if (!is_transformed.test(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            //noinspection unchecked
            return Collections.unmodifiableSet((Set<To>) iterable);
        }

        var iter = iterable.iterator();
        HashSet<To> set = iter instanceof Collection<?> col ?
                new HashSet<>(col.size()) : new HashSet<>();
        while (iter.hasNext()) {
            set.add(transform.apply(iter.next()));
        }
        return Collections.unmodifiableSet(set);
    }

    public static <To, From> NavigableSet<To> toNavigableSet(
            Function<From, To> transform,
            Predicate<From> is_transformed,
            Comparator<? super To> comparator,
            Iterable<? extends From> iterable) {
        if (iterable == null) {
            return Collections.emptyNavigableSet();
        }

        boolean needsCopy = false;
        if (iterable instanceof NavigableSet<?> ns
                && comparator.equals(ns.comparator())) {
            for (From element : iterable) {
                if (!is_transformed.test(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            //noinspection unchecked
            return Collections.unmodifiableNavigableSet((NavigableSet<To>) iterable);
        }

        var iter = iterable.iterator();
        TreeSet<To> set = new TreeSet<>(comparator);
        while (iter.hasNext()) {
            set.add(transform.apply(iter.next()));
        }
        return Collections.unmodifiableNavigableSet(set);
    }

    public static <To, From> NavigableSet<To> toNavigableSet(
            Function<From, To> transform,
            Predicate<From> is_transformed,
            Iterable<? extends From> iterable) {
        return toNavigableSet(transform, is_transformed,
                CollectionUtils.naturalOrder(), iterable);
    }
}
