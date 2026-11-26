package com.v7878.dex.immutable.debug;

import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public final class StartLocal extends DebugItem {
    private final int register;
    private final String name;
    private final TypeId type;
    private final String signature;

    private StartLocal(int register, String name, TypeId type, String signature) {
        this.register = Preconditions.checkShortRegister(register);
        this.name = name; // may be null
        this.type = type; // may be null
        this.signature = signature; // may be null
    }

    public static StartLocal of(int register, String name, TypeId type, String signature) {
        return new StartLocal(register, name, type, signature);
    }

    public int getRegister() {
        return register;
    }

    public String getName() {
        return name;
    }

    public TypeId getType() {
        return type;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = 31 * h + Integer.hashCode(getRegister());
        h = 31 * h + Objects.hashCode(getName());
        h = 31 * h + Objects.hashCode(getType());
        h = 31 * h + Objects.hashCode(getSignature());
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof StartLocal other
                && getRegister() == other.getRegister()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getSignature(), other.getSignature());
    }
}
