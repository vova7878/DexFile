package com.v7878.dex.builder;

import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;

import java.util.Arrays;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class FieldBuilder {
    private String name;
    private TypeId type;
    private int access_flags;
    private int hiddenapi_flags;
    private EncodedValue initial_value;
    private NavigableSet<Annotation> annotations;

    private FieldBuilder() {
        this.annotations = new TreeSet<>();
    }

    private FieldDef finish() {
        return FieldDef.of(name, type, access_flags,
                hiddenapi_flags, initial_value, annotations);
    }

    public static FieldDef build(Consumer<FieldBuilder> consumer) {
        var builder = new FieldBuilder();
        consumer.accept(builder);
        return builder.finish();
    }

    public FieldBuilder if_(boolean value, Consumer<FieldBuilder> true_branch,
                            Consumer<FieldBuilder> false_branch) {
        if (value) {
            true_branch.accept(this);
        } else {
            false_branch.accept(this);
        }
        return this;
    }

    public FieldBuilder if_(boolean value, Consumer<FieldBuilder> true_branch) {
        if (value) {
            true_branch.accept(this);
        }
        return this;
    }

    public FieldBuilder commit(Consumer<FieldBuilder> branch) {
        branch.accept(this);
        return this;
    }

    public FieldBuilder of(FieldDef def) {
        Objects.requireNonNull(def);
        return withName(def.getName())
                .withType(def.getType())
                .withFlags(def.getAccessFlags())
                .withHiddenApiFlags(def.getHiddenApiFlags())
                .withInitialValue(def.getInitialValue())
                .setAnnotations(def.getAnnotations());
    }

    public FieldBuilder of(FieldId id) {
        Objects.requireNonNull(id);
        return withName(id.getName())
                .withType(id.getType());
    }

    public FieldBuilder withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public FieldBuilder withType(TypeId type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

    public FieldBuilder withFlags(int flags) {
        this.access_flags = Preconditions.checkFieldAccessFlags(flags);
        return this;
    }
    //TODO: withFlags(AccessFlag... flags)

    public FieldBuilder withHiddenApiFlags(int flags) {
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(flags);
        return this;
    }

    public FieldBuilder withInitialValue(EncodedValue value) {
        this.initial_value = value;
        return this;
    }

    public FieldBuilder setAnnotations(Iterable<Annotation> annotations) {
        this.annotations = ItemConverter.toMutableNavigableSet(annotations);
        return this;
    }

    public FieldBuilder setAnnotations(Annotation... annotations) {
        return setAnnotations(Arrays.asList(annotations));
    }

    public FieldBuilder withAnnotations(Iterable<Annotation> annotations) {
        this.annotations.addAll(ItemConverter.toList(annotations));
        return this;
    }

    public FieldBuilder withAnnotations(Annotation... annotations) {
        return withAnnotations(Arrays.asList(annotations));
    }
}
