package com.v7878.dex.iface.debug;

import com.v7878.dex.iface.TypeId;

public non-sealed interface StartLocal extends DebugItem {
    int getRegister();

    String getName();

    TypeId getType();

    String getSignature();
}