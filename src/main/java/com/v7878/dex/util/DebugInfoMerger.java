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
        Collections.sort(debug_items);

        var out = new ArrayList<DebugItem>(debug_items.size());
        int pc = 0;
        for (var item : debug_items) {
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
