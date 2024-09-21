package com.v7878.dex.immutable;

import com.v7878.dex.MethodHandleType;
import com.v7878.dex.base.BaseMethodHandleId;
import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MemberId;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.MethodId;
import com.v7878.dex.util.CollectionUtils;

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

    @Override
    public int hashCode() {
        return Objects.hash(getHandleType(), getMember());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof MethodHandleId other
                && Objects.equals(getHandleType(), other.getHandleType())
                && Objects.equals(getMember(), other.getMember());
    }

    @Override
    public int compareTo(MethodHandleId other) {
        if (other == this) return 0;
        int out = MethodHandleType.compare(getHandleType(), other.getHandleType());
        if (out != 0) return out;
        if (getHandleType().isMethodAccess()) {
            return CollectionUtils.compareNonNull((MethodId) getMember(), (MethodId) other.getMember());
        } else {
            return CollectionUtils.compareNonNull((FieldId) getMember(), (FieldId) other.getMember());
        }
    }
}
