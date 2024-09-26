package com.v7878.dex.raw;

import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.bytecode.Instruction;

import java.util.List;

public record CodeItem(int registers, int ins, int outs,
                       List<Instruction> instructions, List<TryBlock> tries) {
}
