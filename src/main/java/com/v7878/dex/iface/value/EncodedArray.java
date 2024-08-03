package com.v7878.dex.iface.value;

import java.util.List;

public non-sealed interface EncodedArray extends EncodedValue {
    List<? extends EncodedValue> getValue();
}
