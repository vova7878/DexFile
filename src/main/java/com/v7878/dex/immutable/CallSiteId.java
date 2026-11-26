package com.v7878.dex.immutable;

import com.v7878.dex.Internal;
import com.v7878.dex.immutable.value.EncodedArray;
import com.v7878.dex.immutable.value.EncodedMethodHandle;
import com.v7878.dex.immutable.value.EncodedMethodType;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.HashUtils;
import com.v7878.dex.util.ImmutableAbstractList;

import java.util.List;
import java.util.Objects;

public final class CallSiteId implements Comparable<CallSiteId> {
    // The name is unique per index and keeps entries with the same content
    // distinct
    private final String name;
    private final MethodHandleId method_handle;
    private final String method_name;
    private final ProtoId method_proto;
    private final List<EncodedValue> extra_arguments;

    private EncodedArray array;

    private int hash = HashUtils.NO_HASH; // not part of equals/hashCode/compareTo

    private static EncodedArray buildArray(CallSiteId site) {
        return EncodedArray.raw(
                new ImmutableAbstractList<>(site.extra_arguments.size() + 3) {
                    @Override
                    public EncodedValue get(int i) {
                        return switch (i) {
                            case 0 -> EncodedMethodHandle.of(site.method_handle);
                            case 1 -> EncodedString.of(site.method_name);
                            case 2 -> EncodedMethodType.of(site.method_proto);
                            default -> {
                                Objects.checkIndex(i, size());
                                yield site.extra_arguments.get(i - 3);
                            }
                        };
                    }
                }
        );
    }

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

    /**
     * {@code null} is treated as an empty collection.
     */
    public static CallSiteId of(String name, MethodHandleId method_handle, String method_name,
                                ProtoId method_proto, Iterable<EncodedValue> extra_arguments) {
        return new CallSiteId(name, method_handle, method_name, method_proto, Converter.toList(extra_arguments));
    }

    /**
     * {@code null} is treated as an empty collection.
     */
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

    public List<EncodedValue> getExtraArguments() {
        return extra_arguments;
    }

    /**
     * The call site payload: the method handle, the method name, the method type and then the
     * extra arguments.
     * <p>
     * The instance is not guaranteed to be the same between calls.
     */
    public EncodedArray getEncodedArray() {
        EncodedArray out = array;
        if (out == null) {
            array = out = buildArray(this);
        }
        return out;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == HashUtils.NO_HASH) {
            hash = h = HashUtils.replaceZero(computeHashCode());
        }
        return h;
    }

    private int computeHashCode() {
        int h = 1;
        h = 31 * h + Objects.hashCode(getName());
        h = 31 * h + Objects.hashCode(getMethodName());
        h = 31 * h + Objects.hashCode(getMethodProto());
        h = 31 * h + Objects.hashCode(getMethodHandle());
        h = 31 * h + Objects.hashCode(getExtraArguments());
        return h;
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

    /**
     * Debug view, not part of the API contract.
     */
    @Override
    public String toString() {
        // TODO: replace with the smali printer
        return "CallSiteId{name=" + getName()
                + " handle=" + getMethodHandle()
                + " method=" + getMethodName()
                + " proto=" + getMethodProto()
                + " extras=" + getExtraArguments().size()
                + "}";
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
