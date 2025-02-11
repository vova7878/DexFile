package com.v7878.dex.raw;

import com.v7878.dex.immutable.ExceptionHandler;

import java.util.List;
import java.util.Objects;

public record CatchHandler(List<ExceptionHandler> elements, Integer catch_all_addr) {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        //noinspection DeconstructionCanBeUsed
        return obj instanceof CatchHandler that
                && Objects.equals(catch_all_addr, that.catch_all_addr)
                && Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catch_all_addr, elements);
    }
}
