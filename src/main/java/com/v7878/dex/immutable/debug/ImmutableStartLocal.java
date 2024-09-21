package com.v7878.dex.immutable.debug;

import com.v7878.dex.base.debug.BaseStartLocal;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.debug.StartLocal;
import com.v7878.dex.immutable.ImmutableTypeId;
import com.v7878.dex.util.Preconditions;

import java.util.Objects;

public class ImmutableStartLocal extends BaseStartLocal implements ImmutableDebugItem {
    private final int register;
    private final String name;
    private final ImmutableTypeId type;
    private final String signature;

    protected ImmutableStartLocal(int register, String name, TypeId type, String signature) {
        this.register = Preconditions.checkShortRegister(register);
        this.name = name; // may be null
        this.type = type == null ? null : ImmutableTypeId.of(type);
        this.signature = signature; // may be null
    }

    public static ImmutableStartLocal of(int register, String name, TypeId type, String signature) {
        return new ImmutableStartLocal(register, name, type, signature);
    }

    public static ImmutableStartLocal of(StartLocal other) {
        if (other instanceof ImmutableStartLocal immutable) return immutable;
        return new ImmutableStartLocal(other.getRegister(),
                other.getName(), other.getType(), other.getSignature());
    }

    @Override
    public int getRegister() {
        return register;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableTypeId getType() {
        return type;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegister(), getName(), getType(), getSignature());
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
