package com.v7878.dex.immutable.debug;

public sealed class DebugItem permits AdvancePC, EndLocal, LineNumber,
        RestartLocal, SetEpilogueBegin, SetFile, SetPrologueEnd, StartLocal {
}
