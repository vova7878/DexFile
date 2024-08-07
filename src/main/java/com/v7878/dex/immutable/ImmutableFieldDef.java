package com.v7878.dex.immutable;

import com.v7878.dex.base.BaseFieldDef;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.value.ImmutableEncodedValue;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.NavigableSet;
import java.util.Objects;

public class ImmutableFieldDef extends BaseFieldDef {
    private final String name;
    private final ImmutableTypeId type;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final ImmutableEncodedValue initial_value;
    private final NavigableSet<? extends ImmutableAnnotation> annotations;

    protected ImmutableFieldDef(
            String name, TypeId type, int access_flags, int hiddenapi_flags,
            EncodedValue initial_value, Iterable<? extends Annotation> annotations) {
        this.name = Objects.requireNonNull(name);
        this.type = ImmutableTypeId.of(type);
        this.access_flags = Preconditions.checkMethodAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        // TODO: check that instance fields should not have initial_value
        this.initial_value = initial_value == null ? null :
                ImmutableEncodedValue.of(initial_value);
        this.annotations = ItemConverter.toNavigableSet(ImmutableAnnotation::of,
                value -> value instanceof ImmutableAnnotation, annotations);
    }

    public static ImmutableFieldDef of(
            String name, TypeId type, int access_flags, int hiddenapi_flags,
            EncodedValue initial_value, Iterable<? extends Annotation> annotations) {
        return new ImmutableFieldDef(name, type, access_flags,
                hiddenapi_flags, initial_value, annotations);
    }

    public static ImmutableFieldDef of(FieldDef other) {
        if (other instanceof ImmutableFieldDef immutable) return immutable;
        return new ImmutableFieldDef(other.getName(), other.getType(), other.getAccessFlags(),
                other.getHiddenApiFlags(), other.getInitialValue(), other.getAnnotations());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImmutableTypeId getType() {
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

    @Override
    public ImmutableEncodedValue getInitialValue() {
        return initial_value;
    }

    @Override
    public NavigableSet<? extends ImmutableAnnotation> getAnnotations() {
        return annotations;
    }
}
