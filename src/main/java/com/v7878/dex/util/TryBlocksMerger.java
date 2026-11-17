package com.v7878.dex.util;

import com.v7878.collections.IntMap;
import com.v7878.collections.IntSet;
import com.v7878.dex.immutable.ExceptionHandler;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class TryBlocksMerger {
    // try_item.insn_count is a ushort, so a single try block
    // may cover at most this many code units
    private static final int MAX_TRY_UNIT_COUNT = 0xFFFF;

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
            final Map<TypeId, Integer> handlers = new LinkedHashMap<>();
            Integer catch_all_address = null;
        }

        // The caller's list is never mutated: empty items are dropped
        // from a local copy only
        var items = new ArrayList<TryItem>(try_items);

        int[] borders;
        {
            int count = items.size();
            var borders_set = new IntSet(count);
            for (int i = 0; i < count; ) {
                var block = items.get(i);
                int start = block.start(), end = block.end();
                // Empty block
                if (end <= start) {
                    assert start == end;
                    items.remove(i);
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

        for (var item : items) {
            var exception = item.exception();
            var handler_address = item.handler();
            var is_exclusion = handler_address < 0;

            int start_index = Arrays.binarySearch(borders, item.start());
            int end_index = Arrays.binarySearch(borders, item.end());
            assert start_index >= 0 && end_index > start_index;

            for (int i = start_index; i < end_index; i++) {
                var position = borders[i];
                var container = elements.get(position);
                assert container != null;
                if (exception == null) {
                    if (is_exclusion) {
                        container.handlers.clear();
                        container.catch_all_address = null;
                    } else {
                        if (container.catch_all_address != null) {
                            throw new IllegalArgumentException(String.format(
                                    "More than one catch-all handler for code position %d", position));
                        }
                        container.catch_all_address = handler_address;
                    }
                } else {
                    if (is_exclusion) {
                        container.handlers.remove(exception);
                    } else {
                        if (container.handlers.putIfAbsent(exception, handler_address) != null) {
                            throw new IllegalArgumentException(String.format(
                                    "More than one catch handler of type %s for code position %d",
                                    exception.getDescriptor(), position));
                        }
                    }
                }
            }
        }

        var out = new TreeSet<TryBlock>();
        for (int i = 0; i < elements_size; ) {
            var container = elements.valueAt(i);
            if (container.catch_all_address == null && container.handlers.isEmpty()) {
                // A stretch without any handler breaks the run
                i++;
                continue;
            }
            // Adjacent elements with a fully equal composition coalesce
            // into one block spanning the whole run
            int end_index = i + 1;
            while (end_index < elements_size) {
                var next = elements.valueAt(end_index);
                if (!sameComposition(container.handlers, container.catch_all_address,
                        next.handlers, next.catch_all_address)) {
                    break;
                }
                end_index++;
            }
            var handlers = new ArrayList<ExceptionHandler>(container.handlers.size());
            for (var e : container.handlers.entrySet()) {
                handlers.add(ExceptionHandler.of(e.getKey(), e.getValue()));
            }
            var fixed_handlers = Collections.unmodifiableList(handlers);
            int start = borders[i];
            int count = borders[end_index] - start;
            // A range longer than the try_item.insn_count ushort limit
            // is split into consecutive parts with the same handlers
            while (count > 0) {
                int part = Math.min(count, MAX_TRY_UNIT_COUNT);
                out.add(TryBlock.raw(start, part,
                        container.catch_all_address, fixed_handlers));
                start += part;
                count -= part;
            }
            i = end_index;
        }
        return Collections.unmodifiableNavigableSet(out);
    }

    // Equal compositions: the same handler types in the same insertion
    // order with the same addresses, and the same catch-all address
    // (both absent or equal). Map.equals is not enough here: it ignores
    // the order, while the order defines which handler wins at runtime.
    private static boolean sameComposition(
            Map<TypeId, Integer> handlers, Integer catch_all_address,
            Map<TypeId, Integer> other_handlers, Integer other_catch_all_address) {
        if (!Objects.equals(catch_all_address, other_catch_all_address)
                || handlers.size() != other_handlers.size()) {
            return false;
        }
        var other_iterator = other_handlers.entrySet().iterator();
        for (var entry : handlers.entrySet()) {
            var other_entry = other_iterator.next();
            if (!entry.getKey().equals(other_entry.getKey())
                    || !entry.getValue().equals(other_entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
