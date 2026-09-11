package com.v7878.dex.smali.parser;

import com.v7878.dex.builder.CodeBuilder;

import java.util.List;

public record IContext(CodeBuilder ib, List<Runnable> actions) {
}
