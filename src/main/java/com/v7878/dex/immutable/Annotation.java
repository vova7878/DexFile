package com.v7878.dex.immutable;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.immutable.value.EncodedInt;
import com.v7878.dex.immutable.value.EncodedMethod;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.ItemConverter;

import java.util.Arrays;
import java.util.NavigableSet;
import java.util.Objects;

public final class Annotation implements CommonAnnotation, Comparable<Annotation> {
    private final AnnotationVisibility visibility;
    private final TypeId type;
    private final NavigableSet<AnnotationElement> elements;

    private Annotation(AnnotationVisibility visibility, TypeId type,
                       NavigableSet<AnnotationElement> elements) {
        this.visibility = visibility;
        this.type = type;
        this.elements = elements;
    }

    public static Annotation of(AnnotationVisibility visibility, TypeId type,
                                Iterable<AnnotationElement> elements) {
        return new Annotation(Objects.requireNonNull(visibility),
                Objects.requireNonNull(type), ItemConverter.toNavigableSet(elements));
    }

    public static Annotation of(AnnotationVisibility visibility, TypeId type,
                                AnnotationElement... elements) {
        return of(visibility, type, Arrays.asList(elements));
    }

    public static Annotation of(AnnotationVisibility visibility, EncodedAnnotation annotation) {
        return new Annotation(Objects.requireNonNull(visibility),
                annotation.getType(), annotation.getElements());
    }

    public static Annotation FastNative() {
        return Annotation.of(AnnotationVisibility.BUILD, TypeId.ofName(
                "dalvik.annotation.optimization.FastNative"));
    }

    public static Annotation CriticalNative() {
        return Annotation.of(AnnotationVisibility.BUILD, TypeId.ofName(
                "dalvik.annotation.optimization.CriticalNative"));
    }

    public static Annotation AnnotationDefault(CommonAnnotation annotation) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                        "dalvik.annotation.AnnotationDefault"),
                AnnotationElement.of("value", EncodedAnnotation.of(annotation)));
    }

    public static Annotation EnclosingClass(TypeId clazz) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                        "dalvik.annotation.EnclosingClass"),
                AnnotationElement.of("value", EncodedType.of(clazz)));
    }

    public static Annotation EnclosingMethod(MethodId method) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                        "dalvik.annotation.EnclosingMethod"),
                AnnotationElement.of("value", EncodedMethod.of(method)));
    }

    public static Annotation InnerClass(String name, int access_flags) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                        "dalvik.annotation.InnerClass"),
                AnnotationElement.of("name", EncodedString.of(name)),
                // TODO: check access flags
                AnnotationElement.of("accessFlags", EncodedInt.of(access_flags)));
    }

    public static Annotation MemberClasses(TypeId... classes) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                "dalvik.annotation.MemberClasses"), AnnotationElement.of(
                "value", EncodedValue.ofValue(classes)));
    }

    public static Annotation MethodParameters(String[] names, int[] access_flags) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                        "dalvik.annotation.MethodParameters"),
                // TODO: check names
                AnnotationElement.of("names", EncodedValue.ofValue(names)),
                // TODO: check access flags
                AnnotationElement.of("accessFlags", EncodedValue.ofValue(access_flags)));
    }

    public static Annotation Signature(String... value) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                "dalvik.annotation.Signature"), AnnotationElement.of(
                "value", EncodedValue.ofValue(value)));
    }

    public static Annotation Throws(TypeId... exceptions) {
        return Annotation.of(AnnotationVisibility.SYSTEM, TypeId.ofName(
                "dalvik.annotation.Throws"), AnnotationElement.of(
                "value", EncodedValue.ofValue(exceptions)));
    }

    public AnnotationVisibility getVisibility() {
        return visibility;
    }

    @Override
    public TypeId getType() {
        return type;
    }

    @Override
    public NavigableSet<AnnotationElement> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getType(), getElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Annotation other
                && Objects.equals(getVisibility(), other.getVisibility())
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getElements(), other.getElements());
    }

    @Override
    public int compareTo(Annotation other) {
        if (other == this) return 0;
        return CollectionUtils.compareNonNull(getType(), other.getType());
    }
}
