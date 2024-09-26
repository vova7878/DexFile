package com.v7878.dex.raw;

import com.v7878.dex.immutable.ExceptionHandler;

import java.util.List;

public record CatchHandler(List<ExceptionHandler> elements, Integer catch_all_addr) {
}
