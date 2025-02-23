package com.v7878.dex.builder;

import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.util.CollectionUtils.toUnmodifiableList;

import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;
import com.v7878.dex.util.Preconditions;
import com.v7878.dex.util.ShortyUtils;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class MethodBuilder {
    private String name;
    private TypeId return_type;
    private List<Parameter> parameters;
    private int access_flags;
    private int hiddenapi_flags;
    private MethodImplementation implementation;
    private NavigableSet<Annotation> annotations;

    private MethodBuilder() {
        this.annotations = new TreeSet<>();
    }

    private MethodDef finish() {
        return MethodDef.of(name, return_type, parameters, access_flags,
                hiddenapi_flags, implementation, annotations);
    }

    public static MethodDef build(Consumer<MethodBuilder> consumer) {
        var builder = new MethodBuilder();
        consumer.accept(builder);
        return builder.finish();
    }

    public MethodBuilder if_(boolean value, Consumer<MethodBuilder> true_branch,
                             Consumer<MethodBuilder> false_branch) {
        if (value) {
            true_branch.accept(this);
        } else {
            false_branch.accept(this);
        }
        return this;
    }

    public MethodBuilder if_(boolean value, Consumer<MethodBuilder> true_branch) {
        if (value) {
            true_branch.accept(this);
        }
        return this;
    }

    public MethodBuilder commit(Consumer<MethodBuilder> branch) {
        branch.accept(this);
        return this;
    }

    public MethodBuilder of(MethodDef def) {
        Objects.requireNonNull(def);
        return withName(def.getName())
                .withReturnType(def.getReturnType())
                .withParametersInternal(def.getParameters())
                .withFlags(def.getAccessFlags())
                .withHiddenApiFlags(def.getHiddenApiFlags())
                .withCode(def.getImplementation())
                .setAnnotations(def.getAnnotations());
    }

    public MethodBuilder of(MethodId id) {
        Objects.requireNonNull(id);
        return withName(id.getName())
                .withReturnType(id.getReturnType())
                .withParameterTypes(id.getParameterTypes());
    }

    public MethodBuilder withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public MethodBuilder withReturnType(TypeId type) {
        this.return_type = Objects.requireNonNull(type);
        return this;
    }

    public MethodBuilder withConstructorSignature() {
        return withName("<init>").withReturnType(TypeId.V);
    }

    public MethodBuilder withStaticConstructorSignature() {
        return withName("<clinit>").withReturnType(TypeId.V).withParameters();
    }

    // Note: parameters are set only as a whole, as single collection
    private MethodBuilder withParametersInternal(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public MethodBuilder withParameters(Iterable<Parameter> parameters) {
        return withParametersInternal(ItemConverter.toList(parameters));
    }

    public MethodBuilder withParameters(Parameter... parameters) {
        return withParameters(Arrays.asList(parameters));
    }

    public MethodBuilder withParameterTypes(Iterable<TypeId> parameters) {
        return withParametersInternal(Parameter.listOf(parameters));
    }

    public MethodBuilder withParameterTypes(TypeId... parameters) {
        return withParametersInternal(toUnmodifiableList(Arrays
                .stream(parameters).map(Parameter::of)));
    }

    public MethodBuilder withProto(ProtoId proto) {
        return withReturnType(proto.getReturnType())
                .withParameterTypes(proto.getParameterTypes());
    }

    public MethodBuilder withFlags(int flags) {
        this.access_flags = Preconditions.checkFieldAccessFlags(flags);
        return this;
    }
    //TODO: withFlags(AccessFlag... flags)

    public MethodBuilder withHiddenApiFlags(int flags) {
        this.hiddenapi_flags = Preconditions.checkHiddenApiFlags(flags);
        return this;
    }

    public MethodBuilder withCode(MethodImplementation implementation) {
        this.implementation = implementation;
        return this;
    }

    public MethodBuilder withCode(int locals, Consumer<CodeBuilder> consumer) {
        int ins_size = ShortyUtils.getDefInputRegisterCount(parameters);
        return withCode(CodeBuilder.build(ins_size + locals, ins_size,
                (access_flags & ACC_STATIC) == 0, consumer));
    }

    public MethodBuilder setAnnotations(Iterable<Annotation> annotations) {
        this.annotations = ItemConverter.toMutableNavigableSet(annotations);
        return this;
    }

    public MethodBuilder setAnnotations(Annotation... annotations) {
        return setAnnotations(Arrays.asList(annotations));
    }

    public MethodBuilder withAnnotations(Iterable<Annotation> annotations) {
        withoutAnnotations(annotations);
        this.annotations.addAll(ItemConverter.toList(annotations));
        return this;
    }

    public MethodBuilder withAnnotations(Annotation... annotations) {
        return withAnnotations(Arrays.asList(annotations));
    }

    public MethodBuilder withoutAnnotations(Iterable<Annotation> annotations) {
        CollectionUtils.removeAll(this.annotations, annotations);
        return this;
    }

    public MethodBuilder withoutAnnotations(Annotation... annotations) {
        return withoutAnnotations(Arrays.asList(annotations));
    }
}
