package com.v7878.dex.raw;

import com.v7878.dex.immutable.ExceptionHandler;

import java.util.List;
import java.util.Objects;

public record CatchHandler(List<ExceptionHandler> elements, Integer catch_all_addr) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof CatchHandler other
                && Objects.equals(catch_all_addr, other.catch_all_addr)
                && Objects.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = 31 * h + Objects.hashCode(elements);
        h = 31 * h + Objects.hashCode(catch_all_addr);
        return h;
    }
}
