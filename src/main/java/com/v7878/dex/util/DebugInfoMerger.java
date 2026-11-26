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

        int pc = 0;
        int count = 0;
        for (int i = 0, n = items.size(); i < n; i++) {
            int position = items.get(i).position();
            if (pc != position) {
                count++;
                pc = position;
            }
            count++;
        }

        var out = new DebugItem[count];
        int index = 0;
        pc = 0;
        for (int i = 0, n = items.size(); i < n; i++) {
            var item = items.get(i);
            int position = item.position();
            if (pc != position) {
                out[index++] = AdvancePC.of(position - pc);
                pc = position;
            }
            out[index++] = item.item();
        }
        return Converter.asList(out);
    }
}
