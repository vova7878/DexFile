package com.v7878.dex.immutable;

import com.v7878.dex.MethodHandleType;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public final class MethodHandleId implements Comparable<MethodHandleId> {
    private final MethodHandleType handle_type;
    private final MemberId member;

    private static RuntimeException wrongMemberType(
            MethodHandleType handle_type, String expected, MemberId member) {
        throw new IllegalArgumentException("Method handle " + handle_type
                + " requires " + expected + ", got " + member);
    }

    private MethodHandleId(MethodHandleType handle_type, MemberId member) {
        this.handle_type = Objects.requireNonNull(handle_type);
        Objects.requireNonNull(member);
        if (handle_type.isMethodAccess()) {
            if (!(member instanceof MethodId method)) {
                throw wrongMemberType(handle_type, "method id", member);
            }
            this.member = method;
        } else {
            if (!(member instanceof FieldId field)) {
                throw wrongMemberType(handle_type, "field id", member);
            }
            this.member = field;
        }
    }

    public static MethodHandleId of(MethodHandleType handle_type, MemberId member) {
        return new MethodHandleId(handle_type, member);
    }

    public static MethodHandleId of(String descriptor) {
        Objects.requireNonNull(descriptor);
        var parts = descriptor.split("@", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Not a method handle descriptor: " + descriptor);
        }
        var type = MethodHandleType.of(parts[0]);
        return MethodHandleId.of(type, type.isMethodAccess() ?
                MethodId.of(parts[1]) : FieldId.of(parts[1]));
    }

    public String getDescriptor() {
        return getHandleType() + "@" + getMember();
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
        return getDescriptor();
    }
}
