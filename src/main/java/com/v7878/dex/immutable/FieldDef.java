package com.v7878.dex.immutable;

import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;

public final class FieldDef extends MemberDef implements Comparable<FieldDef> {
    private final String name;
    private final TypeId type;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final EncodedValue initial_value;
    private final NavigableSet<Annotation> annotations;

    private FieldDef(
            String name, TypeId type, int access_flags, int hiddenapi_flags,
            EncodedValue initial_value, Iterable<Annotation> annotations) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.access_flags = Preconditions.checkFieldAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        if ((access_flags & ACC_STATIC) == 0 && initial_value != null) {
            throw new IllegalArgumentException("Instance fields can`t have initial value");
        }
        this.initial_value = initial_value; // may be null
        this.annotations = ItemConverter.toNavigableSet(annotations);
    }

    public static FieldDef of(
            String name, TypeId type, int access_flags, int hiddenapi_flags,
            EncodedValue initial_value, Iterable<Annotation> annotations) {
        return new FieldDef(name, type, access_flags,
                hiddenapi_flags, initial_value, annotations);
    }

    // TODO?: Add simpler constructor?

    @Override
    public String getName() {
        return name;
    }

    public TypeId getType() {
        return type;
    }

    @Override
    public int getAccessFlags() {
        return access_flags;
    }

    @Override
    public int getHiddenApiFlags() {
        return hiddenapi_flags;
    }

    public EncodedValue getInitialValue() {
        return initial_value;
    }

    @Override
    public NavigableSet<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAccessFlags(),
                getHiddenApiFlags(), getType(), getInitialValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof FieldDef other
                && getAccessFlags() == other.getAccessFlags()
                && getHiddenApiFlags() == other.getHiddenApiFlags()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getInitialValue(), other.getInitialValue());
    }

    @Override
    public int compareTo(FieldDef other) {
        if (other == this) return 0;
        // First static fields, then instance fields
        int out = -Boolean.compare((getAccessFlags() & ACC_STATIC) != 0,
                (other.getAccessFlags() & ACC_STATIC) != 0);
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }
}
