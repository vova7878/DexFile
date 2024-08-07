package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseCallSiteId;
import com.v7878.dex.iface.CallSiteId;
import com.v7878.dex.iface.MethodHandleId;
import com.v7878.dex.iface.ProtoId;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.value.ImmutableEncodedValue;
import com.v7878.dex.util.ItemConverter;

import java.util.List;
import java.util.Objects;

public class ImmutableCallSiteId extends BaseCallSiteId {
    private final ImmutableMethodHandleId method_handle;
    private final String method_name;
    private final ImmutableProtoId method_proto;
    private final List<? extends ImmutableEncodedValue> extra_arguments;

    protected ImmutableCallSiteId(
            MethodHandleId method_handle, String method_name, ProtoId method_proto,
            Iterable<? extends EncodedValue> extra_arguments) {
        this.method_handle = ImmutableMethodHandleId.of(method_handle);
        this.method_name = Objects.requireNonNull(method_name);
        this.method_proto = ImmutableProtoId.of(method_proto);
        this.extra_arguments = ItemConverter.toList(ImmutableEncodedValue::of,
                value -> value instanceof ImmutableEncodedValue, extra_arguments);
    }

    public static ImmutableCallSiteId of(
            MethodHandleId method_handle, String method_name, ProtoId method_proto,
            Iterable<? extends EncodedValue> extra_arguments) {
        return new ImmutableCallSiteId(method_handle, method_name, method_proto, extra_arguments);
    }

    public static ImmutableCallSiteId of(CallSiteId other) {
        if (other instanceof ImmutableCallSiteId immutable) return immutable;
        return new ImmutableCallSiteId(other.getMethodHandle(), other.getMethodName(),
                other.getMethodProto(), other.getExtraArguments());
    }

    @Override
    public ImmutableMethodHandleId getMethodHandle() {
        return method_handle;
    }

    @Override
    public String getMethodName() {
        return method_name;
    }

    @Override
    public ImmutableProtoId getMethodProto() {
        return method_proto;
    }

    @Override
    public List<? extends ImmutableEncodedValue> getExtraArguments() {
        return extra_arguments;
    }
}
