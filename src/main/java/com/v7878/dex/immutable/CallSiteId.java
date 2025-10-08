package com.v7878.dex.immutable;

import com.v7878.dex.Internal;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;

import java.util.List;
import java.util.Objects;

public final class CallSiteId implements Comparable<CallSiteId> {
    // Each CallSite is assigned a unique name based on its id,
    //  this is necessary to distinguish elements with the same content
    private final String name;
    private final MethodHandleId method_handle;
    private final String method_name;
    private final ProtoId method_proto;
    private final List<EncodedValue> extra_arguments;

    private CallSiteId(String name, MethodHandleId method_handle, String method_name,
                       ProtoId method_proto, List<EncodedValue> extra_arguments) {
        this.name = Objects.requireNonNull(name);
        this.method_handle = Objects.requireNonNull(method_handle);
        this.method_name = Objects.requireNonNull(method_name);
        this.method_proto = Objects.requireNonNull(method_proto);
        this.extra_arguments = Objects.requireNonNull(extra_arguments);
    }

    @Internal
    public static CallSiteId raw(String name, MethodHandleId method_handle, String method_name,
                                 ProtoId method_proto, List<EncodedValue> extra_arguments) {
        return new CallSiteId(name, method_handle, method_name, method_proto, extra_arguments);
    }

    public static CallSiteId of(String name, MethodHandleId method_handle, String method_name,
                                ProtoId method_proto, Iterable<EncodedValue> extra_arguments) {
        return new CallSiteId(name, method_handle, method_name, method_proto, Converter.toList(extra_arguments));
    }

    public static CallSiteId of(String name, MethodHandleId method_handle, String method_name,
                                ProtoId method_proto, EncodedValue... extra_arguments) {
        return new CallSiteId(name, method_handle, method_name, method_proto, Converter.toList(extra_arguments));
    }

    public String getName() {
        return name;
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
        return Objects.hash(getName(), getMethodName(), getMethodProto(),
                getMethodHandle(), getExtraArguments());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof CallSiteId other
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getMethodName(), other.getMethodName())
                && Objects.equals(getMethodProto(), other.getMethodProto())
                && Objects.equals(getMethodHandle(), other.getMethodHandle())
                && Objects.equals(getExtraArguments(), other.getExtraArguments());
    }

    @Override
    public int compareTo(CallSiteId other) {
        if (other == this) return 0;
        int out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodHandle(), other.getMethodHandle());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodName(), other.getMethodName());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getMethodProto(), other.getMethodProto());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(getExtraArguments(), other.getExtraArguments());
    }
}
