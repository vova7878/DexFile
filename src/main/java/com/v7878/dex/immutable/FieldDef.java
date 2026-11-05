package com.v7878.dex.immutable;

import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.AccessFlag;
import com.v7878.dex.Internal;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.Preconditions;

import java.util.Locale;
import java.util.NavigableSet;
import java.util.Objects;

public final class FieldDef extends MemberDef implements Comparable<FieldDef> {
    private final String name;
    private final TypeId type;
    private final int access_flags;
    private final int hiddenapi_flags;
    private final EncodedValue initial_value;
    private final NavigableSet<Annotation> annotations;

    private FieldDef(String name, TypeId type, int access_flags, int hiddenapi_flags,
                     EncodedValue initial_value, NavigableSet<Annotation> annotations) {
        // Checked before the semantic rules, so a null argument is reported first.
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        Objects.requireNonNull(annotations);
        if ((access_flags & ACC_STATIC) == 0 && initial_value != null) {
            throw new IllegalArgumentException("Instance fields can`t have initial value");
        } else if (initial_value != null) {
            checkInitialValueKind(name, type, initial_value);
        }
        this.name = name;
        this.type = type;
        this.access_flags = Preconditions.checkFieldAccessFlags(access_flags);
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(hiddenapi_flags);
        this.initial_value = initial_value; // may be null
        this.annotations = annotations;
    }

    /**
     * The kind of a static field's initial value must match the field type.
     * Decided by the shorty alone, without walking the type
     */
    private static void checkInitialValueKind(String name, TypeId type, EncodedValue value) {
        char field_shorty = type.getShorty();
        char value_shorty = value.getShorty();
        if (field_shorty != value_shorty) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected initial value type for field %s: %s vs %s",
                    name, value.getClass().getSimpleName(), type));
        }
    }

    @Internal
    public static FieldDef raw(String name, TypeId type, int access_flags, int hiddenapi_flags,
                               EncodedValue initial_value, NavigableSet<Annotation> annotations) {
        return new FieldDef(name, type, access_flags,
                hiddenapi_flags, initial_value, annotations);
    }

    /**
     * {@code null} is treated as an empty collection.
     */
    public static FieldDef of(String name, TypeId type, int access_flags, int hiddenapi_flags,
                              EncodedValue initial_value, Iterable<Annotation> annotations) {
        return new FieldDef(name, type, access_flags, hiddenapi_flags,
                initial_value, MemberUtils.toAnnotationSet(annotations));
    }

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

    public boolean isStatic() {
        return (getAccessFlags() & ACC_STATIC) != 0;
    }

    public boolean isInstance() {
        return !isStatic();
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
        return Objects.hash(getName(), getAccessFlags(), getHiddenApiFlags(),
                getType(), getInitialValue(),
                CollectionUtils.setHashCode(getAnnotations()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof FieldDef other
                && getAccessFlags() == other.getAccessFlags()
                && getHiddenApiFlags() == other.getHiddenApiFlags()
                && Objects.equals(getName(), other.getName())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getInitialValue(), other.getInitialValue())
                && CollectionUtils.setEquals(getAnnotations(), other.getAnnotations());
    }

    /**
     * Debug view, not part of the API contract.
     */
    @Override
    public String toString() {
        // TODO: replace with the smali printer
        return "FieldDef{flags=[" + AccessFlag.formatAccessFlagsForField(getAccessFlags())
                + "] name=" + getName()
                + " type=" + getType()
                + " hiddenapi=" + String.format(Locale.ROOT, "0x%08X", getHiddenApiFlags())
                + " annotations=" + getAnnotations().size()
                + "}";
    }

    @Override
    public int compareTo(FieldDef other) {
        if (other == this) return 0;
        // First static fields, then instance fields
        int out = -Boolean.compare(isStatic(), other.isStatic());
        if (out != 0) return out;
        out = CollectionUtils.compareNonNull(getName(), other.getName());
        if (out != 0) return out;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }
}
