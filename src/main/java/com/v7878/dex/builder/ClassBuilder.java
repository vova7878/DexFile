package com.v7878.dex.builder;

import com.v7878.dex.AccessFlag;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class ClassBuilder {
    private TypeId type;
    private int access_flags;
    private TypeId superclass;
    private final List<TypeId> interfaces;
    private String source_file;
    private final NavigableSet<FieldDef> fields;
    private final NavigableSet<MethodDef> methods;
    private final NavigableSet<Annotation> annotations;

    public TypeId getType() {
        return type;
    }

    public int getAccessFlags() {
        return access_flags;
    }

    public TypeId getSuperClass() {
        return superclass;
    }

    public List<TypeId> getInterfaces() {
        return interfaces;
    }

    public String getSourceFile() {
        return source_file;
    }

    public NavigableSet<FieldDef> getFields() {
        return fields;
    }

    public NavigableSet<MethodDef> getMethods() {
        return methods;
    }

    private ClassBuilder() {
        this.interfaces = new ArrayList<>();
        this.fields = new TreeSet<>();
        this.methods = new TreeSet<>();
        this.annotations = new TreeSet<>();
    }

    public static ClassBuilder newInstance() {
        return new ClassBuilder();
    }

    public ClassDef finish() {
        return ClassDef.of(type, access_flags, superclass, interfaces,
                source_file, fields, methods, annotations);
    }

    public static ClassDef build(Consumer<ClassBuilder> consumer) {
        var builder = new ClassBuilder();
        consumer.accept(builder);
        return builder.finish();
    }

    public static ClassDef build(TypeId type, Consumer<ClassBuilder> consumer) {
        return build(b -> consumer.accept(b.withType(type)));
    }

    public ClassBuilder if_(boolean value, Consumer<ClassBuilder> true_branch,
                            Consumer<ClassBuilder> false_branch) {
        if (value) {
            true_branch.accept(this);
        } else {
            false_branch.accept(this);
        }
        return this;
    }

    public ClassBuilder if_(boolean value, Consumer<ClassBuilder> true_branch) {
        if (value) {
            true_branch.accept(this);
        }
        return this;
    }

    public ClassBuilder commit(Consumer<ClassBuilder> branch) {
        branch.accept(this);
        return this;
    }

    public ClassBuilder of(ClassDef def) {
        Objects.requireNonNull(def);
        return withType(def.getType())
                .withFlags(def.getAccessFlags())
                .withSuperClass(def.getSuperclass())
                .setInterfaces(def.getInterfaces())
                .withSourceFile(def.getSourceFile())
                .setFields(def.getFields())
                .setMethods(def.getMethods())
                .setAnnotations(def.getAnnotations());
    }

    public ClassBuilder withType(TypeId type) {
        this.type = Objects.requireNonNull(type);
        return this;
    }

    public ClassBuilder withFlags(int flags) {
        this.access_flags = Preconditions.checkClassAccessFlags(flags);
        return this;
    }

    public ClassBuilder withFlags(AccessFlag... flags) {
        return withFlags(AccessFlag.combine(flags));
    }

    public ClassBuilder withSourceFile(String source_file) {
        this.source_file = source_file;
        return this;
    }

    public ClassBuilder withSuperClass(TypeId superclass) {
        this.superclass = superclass;
        return this;
    }

    public ClassBuilder setInterfaces(Iterable<TypeId> interfaces) {
        this.interfaces.clear();
        CollectionUtils.addAll(this.interfaces, interfaces);
        return this;
    }

    public ClassBuilder setInterfaces(TypeId... interfaces) {
        return setInterfaces(Arrays.asList(interfaces));
    }

    public ClassBuilder withInterfaces(Iterable<TypeId> interfaces) {
        CollectionUtils.addAll(this.interfaces, interfaces);
        return this;
    }

    public ClassBuilder withInterfaces(TypeId... interfaces) {
        return withInterfaces(Arrays.asList(interfaces));
    }

    public ClassBuilder withoutInterfaces(Iterable<TypeId> interfaces) {
        CollectionUtils.removeAll(this.interfaces, interfaces);
        return this;
    }

    public ClassBuilder withoutInterfaces(TypeId... interfaces) {
        return withoutInterfaces(Arrays.asList(interfaces));
    }

    public ClassBuilder setAnnotations(Iterable<Annotation> annotations) {
        this.annotations.clear();
        CollectionUtils.addAll(this.annotations, annotations);
        return this;
    }

    public ClassBuilder setAnnotations(Annotation... annotations) {
        return setAnnotations(Arrays.asList(annotations));
    }

    public ClassBuilder withAnnotations(Iterable<Annotation> annotations) {
        CollectionUtils.replaceAll(this.annotations, annotations);
        return this;
    }

    public ClassBuilder withAnnotations(Annotation... annotations) {
        return withAnnotations(Arrays.asList(annotations));
    }

    public ClassBuilder withoutAnnotations(Iterable<Annotation> annotations) {
        CollectionUtils.removeAll(this.annotations, annotations);
        return this;
    }

    public ClassBuilder withoutAnnotations(Annotation... annotations) {
        return withoutAnnotations(Arrays.asList(annotations));
    }

    public ClassBuilder setFields(Iterable<FieldDef> fields) {
        this.fields.clear();
        CollectionUtils.addAll(this.fields, fields);
        return this;
    }

    public ClassBuilder setFields(FieldDef... fields) {
        return setFields(Arrays.asList(fields));
    }

    public ClassBuilder withFields(Iterable<FieldDef> fields) {
        CollectionUtils.replaceAll(this.fields, fields);
        return this;
    }

    public ClassBuilder withFields(FieldDef... fields) {
        return withFields(Arrays.asList(fields));
    }

    public ClassBuilder withField(Consumer<FieldBuilder> consumer) {
        return withFields(FieldBuilder.build(consumer));
    }

    public ClassBuilder withField(String name, TypeId type, int flags) {
        return withField(fb -> fb
                .withName(name)
                .withType(type)
                .withFlags(flags)
        );
    }

    public ClassBuilder withField(String descriptor, int flags) {
        return withField(fb -> fb
                .of(descriptor)
                .withFlags(flags)
        );
    }

    public ClassBuilder withoutFields(Iterable<FieldDef> fields) {
        CollectionUtils.removeAll(this.fields, fields);
        return this;
    }

    public ClassBuilder withoutFields(FieldDef... fields) {
        return withoutFields(Arrays.asList(fields));
    }

    public ClassBuilder setMethods(Iterable<MethodDef> methods) {
        this.methods.clear();
        CollectionUtils.addAll(this.methods, methods);
        return this;
    }

    public ClassBuilder setMethods(MethodDef... methods) {
        return setMethods(Arrays.asList(methods));
    }

    public ClassBuilder withMethods(Iterable<MethodDef> methods) {
        CollectionUtils.replaceAll(this.methods, methods);
        return this;
    }

    public ClassBuilder withMethods(MethodDef... methods) {
        return withMethods(Arrays.asList(methods));
    }

    public ClassBuilder withMethod(Consumer<MethodBuilder> consumer) {
        return withMethods(MethodBuilder.build(consumer));
    }

    public ClassBuilder withMethodBody(String name, ProtoId proto,
                                       int flags, MethodImplementation code) {
        return withMethod(b -> b
                .withName(name)
                .withProto(proto)
                .withFlags(flags)
                .withCode(code)
        );
    }

    public ClassBuilder withMethodBody(String descriptor, int flags,
                                       MethodImplementation code) {
        return withMethod(b -> b
                .of(descriptor)
                .withFlags(flags)
                .withCode(code)
        );
    }

    public ClassBuilder withMethodBody(String name, ProtoId proto, int flags,
                                       int locals, Consumer<CodeBuilder> consumer) {
        return withMethod(b -> b
                .withName(name)
                .withProto(proto)
                .withFlags(flags)
                .withCode(locals, consumer)
        );
    }

    public ClassBuilder withMethodBody(String descriptor, int flags,
                                       int locals, Consumer<CodeBuilder> consumer) {
        return withMethod(b -> b
                .of(descriptor)
                .withFlags(flags)
                .withCode(locals, consumer)
        );
    }

    public ClassBuilder withoutMethods(Iterable<MethodDef> methods) {
        CollectionUtils.removeAll(this.methods, methods);
        return this;
    }

    public ClassBuilder withoutMethods(MethodDef... methods) {
        return withoutMethods(Arrays.asList(methods));
    }
}
