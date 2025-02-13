package com.v7878.dex.immutable;

import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class CallSiteId implements Comparable<CallSiteId> {
    private final MethodHandleId method_handle;
    private final String method_name;
    private final ProtoId method_proto;
    private final List<EncodedValue> extra_arguments;

    private CallSiteId(MethodHandleId method_handle, String method_name,
                       ProtoId method_proto, Iterable<EncodedValue> extra_arguments) {
        this.method_handle = Objects.requireNonNull(method_handle);
        this.method_name = Objects.requireNonNull(method_name);
        this.method_proto = Objects.requireNonNull(method_proto);
        this.extra_arguments = ItemConverter.toList(extra_arguments);
    }

    public static CallSiteId of(MethodHandleId method_handle, String method_name,
                                ProtoId method_proto, Iterable<EncodedValue> extra_arguments) {
        return new CallSiteId(method_handle, method_name, method_proto, extra_arguments);
    }

    public static CallSiteId of(MethodHandleId method_handle, String method_name,
                                ProtoId method_proto, EncodedValue... extra_arguments) {
        return of(method_handle, method_name, method_proto, Arrays.asList(extra_arguments));
    }

    public MethodHandleId getMethodHandle() {
        return method_handle;
    }

    public String getMethodName() {
        return method_name;
    }

    public ProtoId getMethodProto() {
        return method_proto;
    }

    public List<? extends EncodedValue> getExtraArguments() {
        return extra_arguments;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethodName(), getMethodProto(), getMethodHandle(), getExtraArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof CallSiteId other
                && Objects.equals(getMethodName(), other.getMethodName())
                && Objects.equals(getMethodProto(), other.getMethodProto())
                && Objects.equals(getMethodHandle(), other.getMethodHandle())
                && Objects.equals(getExtraArguments(), other.getExtraArguments());
    }

    @Override
    public int compareTo(CallSiteId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getMethodHandle(), other.getMethodHandle());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodName(), other.getMethodName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodProto(), other.getMethodProto());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(getExtraArguments(), other.getExtraArguments());
    }
}
