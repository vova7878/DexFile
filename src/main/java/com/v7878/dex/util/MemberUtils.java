package com.v7878.dex.util;

import static com.v7878.dex.DexConstants.ACC_DIRECT_MASK;
import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.SwitchElement;
import com.v7878.dex.immutable.value.EncodedNull;

import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class MemberUtils {
    private static FieldDef searchableField(String name, TypeId type, boolean is_static) {
        return FieldDef.raw(name, type, is_static ? ACC_STATIC : 0,
                0, null, Collections.emptyNavigableSet());
    }

    private static MethodDef searchableMethod(String name, TypeId ret, List<Parameter> parameters, boolean is_direct) {
        return MethodDef.unchecked(name, ret, parameters, is_direct ? ACC_DIRECT_MASK : 0,
                0, null, Collections.emptyNavigableSet());
    }

    private static Annotation searchableAnnotation(TypeId type) {
        return Annotation.raw(AnnotationVisibility.RUNTIME, type, Collections.emptyNavigableSet());
    }

    private static AnnotationElement searchableAnnotationElement(String name) {
        return AnnotationElement.of(name, EncodedNull.INSTANCE);
    }

    private static TryBlock searchableTryBlock(int address) {
        return TryBlock.raw(address, 0, 0, List.of());
    }

    private static final TypeId FIRST_TYPE = TypeId.raw("");
    private static final FieldDef FIRST_INSTANCE_FIELD =
            searchableField("", FIRST_TYPE, false);
    private static final MethodDef FIRST_VIRTUAL_METHOD =
            searchableMethod("", FIRST_TYPE, Collections.emptyList(), false);

    public static NavigableSet<FieldDef> getStaticFieldsSubset(NavigableSet<FieldDef> set) {
        return set.headSet(FIRST_INSTANCE_FIELD, false);
    }

    public static NavigableSet<FieldDef> getInstanceFieldsSubset(NavigableSet<FieldDef> set) {
        return set.tailSet(FIRST_INSTANCE_FIELD, true);
    }

    public static NavigableSet<MethodDef> getDirectMethodsSubset(NavigableSet<MethodDef> set) {
        return set.headSet(FIRST_VIRTUAL_METHOD, false);
    }

    public static NavigableSet<MethodDef> getVirtualMethodsSubset(NavigableSet<MethodDef> set) {
        return set.tailSet(FIRST_VIRTUAL_METHOD, true);
    }

    public static NavigableSet<FieldDef> mergeFields(List<FieldDef> static_, List<FieldDef> instance) {
        var out = new TreeSet<FieldDef>();
        if (static_ != null) for (var field : static_) {
            MemberUtils.addField(out, field);
        }
        if (instance != null) for (var field : instance) {
            MemberUtils.addField(out, field);
        }
        return Collections.unmodifiableNavigableSet(out);
    }

    public static NavigableSet<MethodDef> mergeMethods(List<MethodDef> direct, List<MethodDef> virtual) {
        var out = new TreeSet<MethodDef>();
        if (direct != null) for (var method : direct) {
            MemberUtils.addMethod(out, method);
        }
        if (virtual != null) for (var method : virtual) {
            MemberUtils.addMethod(out, method);
        }
        return Collections.unmodifiableNavigableSet(out);
    }

    public static FieldDef findField(NavigableSet<FieldDef> fields, boolean is_static, String name, TypeId type) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        return CollectionUtils.findValue(fields, searchableField(name, type, is_static));
    }

    public static MethodDef findMethod(NavigableSet<MethodDef> methods, boolean is_direct, String name, ProtoId proto) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(proto);
        return CollectionUtils.findValue(methods, searchableMethod(name, proto.getReturnType(),
                Parameter.listOf(proto.getParameterTypes()), is_direct));
    }

    public static Annotation findAnnotation(NavigableSet<Annotation> annotations, TypeId type) {
        Objects.requireNonNull(type);
        return CollectionUtils.findValue(annotations, searchableAnnotation(type));
    }

    public static AnnotationElement findElement(NavigableSet<AnnotationElement> annotations, String name) {
        Objects.requireNonNull(name);
        return CollectionUtils.findValue(annotations, searchableAnnotationElement(name));
    }

    public static TryBlock findTryBlock(NavigableSet<TryBlock> blocks, int address) {
        var block = searchableTryBlock(address);
        var floor = blocks.floor(block);
        if (floor == null) return null;
        if (floor.getStartAddress() <= address
                && address < floor.getEndAddress()) {
            return floor;
        }
        return null;
    }

    public static void addAnnotationElement(NavigableSet<AnnotationElement> elements, AnnotationElement value) {
        if (!elements.add(value)) {
            throw new IllegalArgumentException(String.format(
                    "Duplicate annotation element with name %s", value.getName()
            ));
        }
    }

    public static void addAnnotation(NavigableSet<Annotation> annotations, Annotation value) {
        if (!annotations.add(value)) {
            throw new IllegalArgumentException(String.format(
                    "Duplicate annotation with type %s", value.getType()
            ));
        }
    }

    public static void addMethod(NavigableSet<MethodDef> methods, MethodDef value) {
        if (!methods.add(value)) {
            throw new IllegalArgumentException(String.format(
                    "Duplicate method with signature %s", value.getName() + value.getProto()
            ));
        }
    }

    public static void addField(NavigableSet<FieldDef> fields, FieldDef value) {
        if (!fields.add(value)) {
            throw new IllegalArgumentException(String.format(
                    "Duplicate field with signature %s", value.getName() + ":" + value.getType()
            ));
        }
    }

    public static void addTryBlock(NavigableSet<TryBlock> blocks, TryBlock value) {
        if (!blocks.add(value)) {
            throw new IllegalArgumentException(String.format(
                    "Duplicate try block %s", value));
        }
    }

    public static void addSwitchElement(NavigableSet<SwitchElement> elements, SwitchElement value) {
        if (!elements.add(value)) {
            throw new IllegalArgumentException(
                    "Duplicate switch key: " + value.getKey());
        }
    }

    /**
     * Builds an unmodifiable set through the typed {@code add*} helper, so a
     * duplicate is rejected with a message that names the colliding member.
     * <p>
     * The source is always traversed element by element: even a
     * {@link NavigableSet} argument is not copied with {@code addAll}, because
     * it may use a custom comparator and then its contents are not necessarily
     * the same set as the one built with the natural ordering.
     */
    private static <T extends Comparable<? super T>> NavigableSet<T> toSet(
            Iterable<T> values, BiConsumer<NavigableSet<T>, T> adder) {
        if (values == null) {
            return Collections.emptyNavigableSet();
        }
        var out = new TreeSet<T>();
        for (var value : values) {
            adder.accept(out, value);
        }
        return Collections.unmodifiableNavigableSet(out);
    }

    public static NavigableSet<FieldDef> toFieldSet(Iterable<FieldDef> fields) {
        return toSet(fields, MemberUtils::addField);
    }

    public static NavigableSet<MethodDef> toMethodSet(Iterable<MethodDef> methods) {
        return toSet(methods, MemberUtils::addMethod);
    }

    public static NavigableSet<Annotation> toAnnotationSet(Iterable<Annotation> annotations) {
        return toSet(annotations, MemberUtils::addAnnotation);
    }

    public static NavigableSet<AnnotationElement> toAnnotationElementSet(
            Iterable<AnnotationElement> elements) {
        return toSet(elements, MemberUtils::addAnnotationElement);
    }

    public static NavigableSet<TryBlock> toTryBlockSet(Iterable<TryBlock> blocks) {
        return toSet(blocks, MemberUtils::addTryBlock);
    }

    public static NavigableSet<SwitchElement> toSwitchElementSet(
            Iterable<SwitchElement> elements) {
        return toSet(elements, MemberUtils::addSwitchElement);
    }
}
