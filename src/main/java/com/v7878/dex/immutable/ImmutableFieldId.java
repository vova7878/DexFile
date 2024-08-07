package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseFieldId;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.TypeId;

import java.util.Objects;

public class ImmutableFieldId extends BaseFieldId implements ImmutableMemberId {
    private final ImmutableTypeId declaring_class;
    private final String name;
    private final ImmutableTypeId type;

    protected ImmutableFieldId(TypeId declaring_class, String name, TypeId type) {
        this.declaring_class = ImmutableTypeId.of(declaring_class);
        this.name = Objects.requireNonNull(name);
        this.type = ImmutableTypeId.of(type);
    }

    public static ImmutableFieldId of(TypeId declaring_class, String name, TypeId type) {
        return new ImmutableFieldId(declaring_class, name, type);
    }

    public static ImmutableFieldId of(FieldId other) {
        if (other instanceof ImmutableFieldId immutable) return immutable;
        return new ImmutableFieldId(other.getDeclaringClass(), other.getName(), other.getType());
    }

    @Override
    public ImmutableTypeId getDeclaringClass() {
        return declaring_class;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableTypeId getType() {
        return type;
    }
}
