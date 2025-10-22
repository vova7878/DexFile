package com.v7878.dex.immutable;

import com.v7878.dex.MethodHandleType;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class MethodHandleId implements Comparable<MethodHandleId> {
    private final MethodHandleType handle_type;
    private final MemberId member;

    private MethodHandleId(MethodHandleType handle_type, MemberId member) {
        this.handle_type = Objects.requireNonNull(handle_type);
        if (handle_type.isMethodAccess()) {
            this.member = Objects.requireNonNull((MethodId) member);
        } else {
            this.member = Objects.requireNonNull((FieldId) member);
        }
    }

    public static MethodHandleId of(MethodHandleType handle_type, MemberId member) {
        return new MethodHandleId(handle_type, member);
    }

    public MethodHandleType getHandleType() {
        return handle_type;
    }

    public MemberId getMember() {
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

    @Override
    public String toString() {
        return getHandleType() + "->" + getMember();
    }
}
