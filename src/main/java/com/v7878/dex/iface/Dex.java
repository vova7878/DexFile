package com.v7878.dex.iface;

import java.util.List;

public interface Dex {
    List<? extends ClassDef> getClasses();
}
