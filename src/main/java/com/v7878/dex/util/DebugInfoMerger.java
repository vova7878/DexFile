package com.v7878.dex.util;

import com.v7878.dex.immutable.debug.AdvancePC;
import com.v7878.dex.immutable.debug.DebugItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugInfoMerger {
    public interface MergerDebugItem<T extends MergerDebugItem<T>> extends Comparable<T> {
        int position();

        DebugItem item();

        @Override
        int compareTo(T v);
    }

    public static <T extends MergerDebugItem<T>> List<DebugItem> mergeDebugItems(List<T> debug_items) {
        // The caller's list is never reordered: sorting happens on a copy
        var items = new ArrayList<>(debug_items);
        Collections.sort(items);

        var out = new ArrayList<DebugItem>(items.size());
        int pc = 0;
        for (var item : items) {
            int position = item.position();
            if (pc != position) {
                out.add(AdvancePC.of(position - pc));
                pc = position;
            }
            out.add(item.item());
        }
        return Collections.unmodifiableList(out);
    }
}
