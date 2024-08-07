package com.v7878.dex.immutable;

import com.v7878.dex.iface.FieldId;
import com.v7878.dex.iface.MemberId;
import com.v7878.dex.iface.MethodId;

public interface ImmutableMemberId extends MemberId {
    static ImmutableMemberId of(MemberId member) {
        if (member instanceof MethodId method) {
            return ImmutableMethodId.of(method);
        }
        if (member instanceof FieldId field) {
            return ImmutableFieldId.of(field);
        }
        throw new IllegalArgumentException("Invalid member type");
    }

    static ImmutableMemberId of(boolean isMethod, MemberId member) {
        if (isMethod) {
            return ImmutableMethodId.of((MethodId) member);
        } else {
            return ImmutableFieldId.of((FieldId) member);
        }
    }
}
