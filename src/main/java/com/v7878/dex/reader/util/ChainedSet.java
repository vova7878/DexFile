package com.v7878.dex.reader.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class ChainedSet<T> extends AbstractSet<T> {
    private final Set<? extends T> set1;
    private final Set<? extends T> set2;

    public ChainedSet(Set<? extends T> set1, Set<? extends T> set2) {
        this.set1 = set1;
        this.set2 = set2;
    }

    @Override
    public int size() {
        return set1.size() + set2.size();
    }

    @Override
    public Iterator<T> iterator() {
        var i1 = set1.iterator();
        var i2 = set2.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i1.hasNext() || i2.hasNext();
            }

            @Override
            public T next() {
                if (i1.hasNext()) {
                    return i1.next();
                }
                return i2.next();
            }
        };
    }
}
