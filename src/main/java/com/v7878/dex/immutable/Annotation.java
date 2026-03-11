package com.v7878.dex.immutable;

import static com.v7878.dex.util.Ids.ANNOTATION_DEFAULT;
import static com.v7878.dex.util.Ids.CRITICAL_NATIVE;
import static com.v7878.dex.util.Ids.ENCLOSING_CLASS;
import static com.v7878.dex.util.Ids.ENCLOSING_METHOD;
import static com.v7878.dex.util.Ids.FAST_NATIVE;
import static com.v7878.dex.util.Ids.INNER_CLASS;
import static com.v7878.dex.util.Ids.MEMBER_CLASSES;
import static com.v7878.dex.util.Ids.METHOD_PARAMETERS;
import static com.v7878.dex.util.Ids.SIGNATURE;
import static com.v7878.dex.util.Ids.THROWS;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.Internal;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.immutable.value.EncodedInt;
import com.v7878.dex.immutable.value.EncodedMethod;
import com.v7878.dex.immutable.value.EncodedNull;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.Converter;
import com.v7878.dex.util.Preconditions;

import java.util.Arrays;
import java.util.NavigableSet;
import java.util.Objects;

public final class Annotation implements CommonAnnotation, Comparable<Annotation> {
    private final AnnotationVisibility visibility;
    private final TypeId type;
    private final NavigableSet<AnnotationElement> elements;

    private Annotation(AnnotationVisibility visibility, TypeId type,
                       NavigableSet<AnnotationElement> elements) {
        this.visibility = Objects.requireNonNull(visibility);
        this.type = Objects.requireNonNull(type);
        this.elements = Objects.requireNonNull(elements);
    }

    @Internal
    public static Annotation raw(AnnotationVisibility visibility, TypeId type,
                                 NavigableSet<AnnotationElement> elements) {
        return new Annotation(visibility, type, elements);
    }

    public static Annotation of(AnnotationVisibility visibility, TypeId type,
                                Iterable<AnnotationElement> elements) {
        return new Annotation(visibility, type, Converter.toNavigableSet(elements));
    }

    public static Annotation of(AnnotationVisibility visibility, TypeId type,
                                AnnotationElement... elements) {
        return of(visibility, type, Arrays.asList(elements));
    }

    public static Annotation of(AnnotationVisibility visibility, CommonAnnotation annotation) {
        return new Annotation(visibility, annotation.getType(), annotation.getElements());
    }

    public static Annotation FastNative() {
        return Annotation.of(AnnotationVisibility.BUILD, FAST_NATIVE);
    }

    public static Annotation CriticalNative() {
        return Annotation.of(AnnotationVisibility.BUILD, CRITICAL_NATIVE);
    }

    public static Annotation AnnotationDefault(CommonAnnotation annotation) {
        return Annotation.of(AnnotationVisibility.SYSTEM, ANNOTATION_DEFAULT,
                AnnotationElement.of("value", EncodedAnnotation.of(annotation)));
    }

    public static Annotation EnclosingClass(TypeId clazz) {
        return Annotation.of(AnnotationVisibility.SYSTEM, ENCLOSING_CLASS,
                AnnotationElement.of("value", EncodedType.of(clazz)));
    }

    public static Annotation EnclosingMethod(MethodId method) {
        return Annotation.of(AnnotationVisibility.SYSTEM, ENCLOSING_METHOD,
                AnnotationElement.of("value", EncodedMethod.of(method)));
    }

    public static Annotation InnerClass(String name, int access_flags) {
        return Annotation.of(AnnotationVisibility.SYSTEM, INNER_CLASS,
                AnnotationElement.of("name", name == null ?
                        EncodedNull.INSTANCE : EncodedString.of(name)),
                AnnotationElement.of("accessFlags", EncodedInt.of(
                        Preconditions.checkInnerClassAccessFlags(access_flags))));
    }

    public static Annotation MemberClasses(TypeId... classes) {
        return Annotation.of(AnnotationVisibility.SYSTEM, MEMBER_CLASSES,
                AnnotationElement.of("value", EncodedValue.ofValue(classes)));
    }

    public static Annotation MemberClasses(Iterable<TypeId> classes) {
        return Annotation.of(AnnotationVisibility.SYSTEM, MEMBER_CLASSES,
                AnnotationElement.of("value", EncodedValue.ofValue(classes)));
    }

    public static Annotation MethodParameters(String[] names, int[] access_flags) {
        if (names.length != access_flags.length) {
            throw new IllegalArgumentException(
                    String.format("Number of names (%s) and access flags (%s) are not equal",
                            names.length, access_flags.length)
            );
        }
        access_flags = access_flags.clone();
        for (var flags : access_flags) {
            Preconditions.checkParamaterAccessFlags(flags);
        }
        return Annotation.of(AnnotationVisibility.SYSTEM, METHOD_PARAMETERS,
                AnnotationElement.of("names", EncodedValue.ofValue(names)),
                AnnotationElement.of("accessFlags", EncodedValue.ofValue(access_flags)));
    }

    public static Annotation Signature(String... value) {
        return Annotation.of(AnnotationVisibility.SYSTEM, SIGNATURE,
                AnnotationElement.of("value", EncodedValue.ofValue(value)));
    }

    public static Annotation Signature(Iterable<String> value) {
        return Annotation.of(AnnotationVisibility.SYSTEM, SIGNATURE,
                AnnotationElement.of("value", EncodedValue.ofValue(value)));
    }

    public static Annotation Throws(TypeId... exceptions) {
        return Annotation.of(AnnotationVisibility.SYSTEM, THROWS,
                AnnotationElement.of("value", EncodedValue.ofValue(exceptions)));
    }

    public static Annotation Throws(Iterable<TypeId> exceptions) {
        return Annotation.of(AnnotationVisibility.SYSTEM, THROWS,
                AnnotationElement.of("value", EncodedValue.ofValue(exceptions)));
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
