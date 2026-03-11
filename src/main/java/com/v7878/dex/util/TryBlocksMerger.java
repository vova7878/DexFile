package com.v7878.dex.util;

import com.v7878.collections.IntMap;
import com.v7878.collections.IntSet;
import com.v7878.dex.immutable.ExceptionHandler;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public class TryBlocksMerger {
    public interface TryItem {
        int start();

        int end();

        int handler();

        TypeId exception();
    }

    // <------------------>
    //           <--->
    //           <------------->
    // <------------->
    //           <-------->
    //             |
    //             | (Find all borders)
    //             v
    // /--------|/---|/---|/---|
    //             |
    //             | (Cut at begin of each range)
    //             v
    // <--------//--->
    //           <--->
    //           <---//---//--->
    // <--------//---//--->
    //           <---//--->
    //             |
    //             | (merge elements with same start)
    //             v
    // <========><***><+++><--->
    public static NavigableSet<TryBlock> mergeTryItems(List<? extends TryItem> try_items) {
        class TryContainer {
            final Set<TypeId> exceptions = new HashSet<>();
            final List<ExceptionHandler> handlers = new ArrayList<>();
            Integer catch_all_address = null;
        }

        int[] borders;
        {
            int count = try_items.size();
            var borders_set = new IntSet(count);
            for (int i = 0; i < count; ) {
                var block = try_items.get(i);
                int start = block.start(), end = block.end();
                if (end <= start) {
                    assert start == end;
                    try_items.remove(i);
                    count--;
                    continue;
                }
                borders_set.add(start);
                borders_set.add(end);
                i++;
            }
            borders = borders_set.toArray();
        }
        if (borders.length == 0) return Collections.emptyNavigableSet();
        int elements_size = borders.length - 1;

        var elements = new IntMap<TryContainer>(elements_size);
        for (int i = 0; i < elements_size; i++) {
            elements.put(borders[i], new TryContainer());
        }

        for (var item : try_items) {
            var exception = item.exception();
            var handler_address = item.handler();
            var handler = exception == null ? null :
                    ExceptionHandler.of(exception, handler_address);

            int start_index = Arrays.binarySearch(borders, item.start());
            int end_index = Arrays.binarySearch(borders, item.end());
            assert start_index >= 0 && end_index > start_index;

            for (int i = start_index; i < end_index; i++) {
                var position = borders[i];
                var container = elements.get(position);
                assert container != null;
                if (exception == null) {
                    if (container.catch_all_address != null) {
                        throw new IllegalArgumentException(String.format(
                                "More than one catch-all handler for code position %d", position));
                    }
                    container.catch_all_address = handler_address;
                } else {
                    if (container.exceptions.contains(exception)) {
                        throw new IllegalArgumentException(String.format(
                                "More than one catch handler of type %s for code position %d",
                                exception.getDescriptor(), position));
                    }
                    container.exceptions.add(exception);
                    container.handlers.add(handler);
                }
            }
        }

        var out = new TreeSet<TryBlock>();
        for (int i = 0; i < elements_size; i++) {
            var container = elements.valueAt(i);
            if (container.catch_all_address != null || !container.handlers.isEmpty()) {
                out.add(TryBlock.of(borders[i], borders[i + 1] - borders[i],
                        container.catch_all_address, container.handlers));
            }
        }
        return Collections.unmodifiableNavigableSet(out);
    }
}
