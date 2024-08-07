package com.v7878.dex.immutable;

import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MemberId;
import com.v7878.dex.iface.MethodId;

public interface ImmutableMemberId extends MemberId {
    static ImmutableMemberId of(MemberId other) {
        if (other instanceof ImmutableMemberId immutable) return immutable;
        if (other instanceof MethodId method) {
            return ImmutableMethodId.of(method);
        }
        if (other instanceof FieldId field) {
            return ImmutableFieldId.of(field);
        }
        throw new IllegalArgumentException("Invalid member type");
    }

    static ImmutableMemberId of(boolean isMethod, MemberId other) {
        if (other instanceof ImmutableMemberId immutable) return immutable;
        if (isMethod) {
            return ImmutableMethodId.of((MethodId) other);
        } else {
            return ImmutableFieldId.of((FieldId) other);
        }
    }
}
