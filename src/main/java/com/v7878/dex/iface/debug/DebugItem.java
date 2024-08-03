package com.v7878.dex.iface.debug;

public sealed interface DebugItem permits
        AdvancePC, EndLocal, LineNumber, RestartLocal,
        SetEpilogueBegin, SetFile, SetPrologueEnd, StartLocal {
}
