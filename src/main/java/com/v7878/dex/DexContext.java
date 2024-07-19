package com.v7878.dex;

public interface DexContext {

    DexOptions<?> getOptions();

    DexVersion getDexVersion();

    default boolean supportsDefaultMethods() {
        //TODO: true if dex >= 037 or if cdex (feature_flags & kDefaultMethods) != 0
        return true;
    }
}
