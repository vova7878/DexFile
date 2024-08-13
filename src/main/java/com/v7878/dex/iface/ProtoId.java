package com.v7878.dex.iface;

import java.util.List;

public interface ProtoId extends Comparable<ProtoId> {
    List<? extends TypeId> getParameterTypes();

    TypeId getReturnType();

    default String getShorty() {
        List<? extends TypeId> parameters = getParameterTypes();
        StringBuilder out = new StringBuilder(parameters.size() + 1);
        out.append(getReturnType().getShorty());
        for (TypeId tmp : parameters) {
            out.append(tmp.getShorty());
        }
        return out.toString();
    }

    default int getInputRegisterCount() {
        int out = 0;
        for (TypeId tmp : getParameterTypes()) {
            out += tmp.getRegisterCount();
        }
        return out;
    }
}
