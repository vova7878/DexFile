package com.v7878.dex.raw;

import com.v7878.dex.immutable.debug.DebugItem;

import java.util.List;

record DebugInfo(List<String> parameter_names,
                 List<DebugItem> items) {
}
