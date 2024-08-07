package com.v7878.dex.immutable;

import com.v7878.dex.MethodHandleType;
import com.v7878.dex.base.BaseMethodHandleId;
import com.v7878.dex.iface.MemberId;
import com.v7878.dex.iface.MethodHandleId;

import java.util.Objects;

public class ImmutableMethodHandleId extends BaseMethodHandleId {
    private final MethodHandleType handle_type;
    private final ImmutableMemberId member;

    protected ImmutableMethodHandleId(MethodHandleType handle_type, MemberId member) {
        this.handle_type = Objects.requireNonNull(handle_type);
        this.member = ImmutableMemberId.of(handle_type.isMethodAccess(), member);
    }

    public static ImmutableMethodHandleId of(MethodHandleType handle_type, MemberId member) {
        return new ImmutableMethodHandleId(handle_type, member);
    }

    public static ImmutableMethodHandleId of(MethodHandleId other) {
        if (other instanceof ImmutableMethodHandleId immutable) return immutable;
        return new ImmutableMethodHandleId(other.getHandleType(), other.getMember());
    }

    @Override
    public MethodHandleType getHandleType() {
        return handle_type;
    }

    @Override
    public ImmutableMemberId getMember() {
        return member;
    }
}
