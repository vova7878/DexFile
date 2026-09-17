package com.v7878.dex.analysis;

import static com.v7878.dex.DexConstants.ACC_INTERFACE;
import static com.v7878.dex.immutable.TypeId.OBJECT;
import static com.v7878.dex.util.Ids.CLONEABLE;
import static com.v7878.dex.util.Ids.SERIALIZABLE;

import com.v7878.dex.DexIO.ClassHeader;
import com.v7878.dex.immutable.TypeId;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Answers type relation queries for {@link TypeInfo} values: assignability
 * and least common supertypes, with array and unresolved-type handling.
 *
 * <p>Implementations supply raw class relations through the protected
 * {@code resolve*} methods; {@code null} means unknown. Two implementations
 * are provided: {@link #DEFAULT} knows nothing, {@link #simple} derives
 * relations from class headers.
 */
public abstract sealed class TypeResolver {
    /**
     * Knows nothing: every class relation is unknown.
     */
    public static final TypeResolver DEFAULT = new UnknownResolver();

    /**
     * Derives relations from class headers: superclass chains, transitively
     * implemented interfaces and the interface flag. Classes the mapper
     * cannot resolve are unknown.
     */
    public static TypeResolver simple(Function<TypeId, ClassHeader> mapper) {
        return new SimpleResolver(mapper);
    }

    /**
     * Least common supertype of two classes; null means unknown.
     */
    protected abstract TypeId resolveJoin(TypeId a, TypeId b);

    /**
     * Whether a is a subtype of b; null means unknown.
     */
    protected abstract Boolean resolveInstanceOf(TypeId a, TypeId b);

    /**
     * Whether the class is an interface; null means unknown.
     */
    protected abstract Boolean resolveIsInterface(TypeId type);

    /**
     * Returns the least common supertype of a and b. Array depths are
     * combined the way the verifier merges registers: a depth mismatch
     * degrades to an Object array at the common depth, unless one side
     * is already unresolved, Serializable or Cloneable there.
     *
     * @throws IllegalArgumentException if either type is primitive
     */
    public TypeInfo join(TypeInfo a, TypeInfo b) {
        if (a.isPrimitive() || b.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        if (Objects.equals(a, b)) {
            return a;
        }
        int depth = Math.min(a.array_depth(), b.array_depth());
        if ((a.array_depth() == depth && a.isBasePrimitive())
                || (b.array_depth() == depth && b.isBasePrimitive())) {
            depth--;
        }
        assert depth >= 0;
        if (a.array_depth() != depth || b.array_depth() != depth) {
            // Note: All arrays implement Serializable and Cloneable
            if (a.array_depth() == depth && (a.base() == null ||
                    SERIALIZABLE.equals(a.base()) || CLONEABLE.equals(a.base()))) {
                return a;
            }
            if (b.array_depth() == depth && (b.base() == null ||
                    SERIALIZABLE.equals(b.base()) || CLONEABLE.equals(b.base()))) {
                return b;
            }
            return new TypeInfo(OBJECT, depth);
        }
        return new TypeInfo(joinFlat(a.base(), b.base()), depth);
    }

    /**
     * @see #join(TypeInfo, TypeInfo)
     */
    public TypeInfo join(TypeInfo a, TypeId b) {
        return join(a, TypeInfo.of(b));
    }

    /**
     * @see #join(TypeInfo, TypeInfo)
     */
    public TypeInfo join(TypeId a, TypeInfo b) {
        return join(TypeInfo.of(a), b);
    }

    /**
     * @see #join(TypeInfo, TypeInfo)
     */
    public TypeInfo join(TypeId a, TypeId b) {
        return join(TypeInfo.of(a), TypeInfo.of(b));
    }

    /**
     * Checks whether a value of type a is assignable to type b.
     *
     * <p>Deeper reference arrays are assignable to {@code Object[]},
     * {@code Serializable[]} and {@code Cloneable[]}.
     *
     * @param unknownMatches result used when the relation cannot be resolved
     * @param strict         {@code false} mirrors Android verification: any reference
     *                       (but no primitive) is assignable to an interface type, because
     *                       join loses interface information and the exact check happens at
     *                       runtime; {@code true} requires the target to be implemented
     * @throws IllegalArgumentException if either type is primitive
     */
    public boolean isAssignable(TypeInfo a, TypeInfo b, boolean unknownMatches, boolean strict) {
        if (a.isPrimitive() || b.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        if (Objects.equals(a, b)) {
            var unresolved = a.isUnresolved();
            assert unresolved == b.isUnresolved();
            return !unresolved || unknownMatches;
        }
        if (b.array_depth() > a.array_depth()) return false;
        if (b.array_depth() < a.array_depth()) {
            if (!strict && b.base() != null && isInterface(b.base(), unknownMatches)) {
                return true;
            }
            // T[][] instanceof ?[] or
            // T[][] instanceof any of Object[], Serializable[] or Cloneable[]
            return b.base() == null ? unknownMatches : (OBJECT.equals(b.base()) ||
                    SERIALIZABLE.equals(b.base()) || CLONEABLE.equals(b.base()));
        }
        if (a.isBasePrimitive() || b.isBasePrimitive()) {
            // Different primitive bases
            // int[]...[] instanceof float[]...[]
            // or ref[]...[] instanceof int[]...[]
            // or int[]...[] instanceof ref[]...[]
            return false;
        }
        return instanceOfFlat(a.base(), b.base(), unknownMatches, strict);
    }

    /**
     * @see #isAssignable(TypeInfo, TypeInfo, boolean, boolean)
     */
    public boolean isAssignable(TypeInfo a, TypeId b, boolean unknownMatches, boolean strict) {
        return isAssignable(a, TypeInfo.of(b), unknownMatches, strict);
    }

    /**
     * @see #isAssignable(TypeInfo, TypeInfo, boolean, boolean)
     */
    public boolean isAssignable(TypeId a, TypeInfo b, boolean unknownMatches, boolean strict) {
        return isAssignable(TypeInfo.of(a), b, unknownMatches, strict);
    }

    /**
     * @see #isAssignable(TypeInfo, TypeInfo, boolean, boolean)
     */
    public boolean isAssignable(TypeId a, TypeId b, boolean unknownMatches, boolean strict) {
        return isAssignable(TypeInfo.of(a), TypeInfo.of(b), unknownMatches, strict);
    }

    /**
     * Checks whether the type is an interface.
     *
     * @param defaultValue result used for unknown types
     * @throws IllegalArgumentException if the type is primitive
     */
    public boolean isInterface(TypeInfo type, boolean defaultValue) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException(
                    "The argument can only be a reference type");
        }
        var exact = type.exactType();
        if (exact == null) {
            return defaultValue;
        }
        if (exact.isArray()) {
            return false;
        }
        var out = resolveIsInterface(exact);
        return out == null ? defaultValue : out;
    }

    /**
     * @see #isInterface(TypeInfo, boolean)
     */
    public boolean isInterface(TypeId type, boolean defaultValue) {
        Objects.requireNonNull(type);
        return isInterface(TypeInfo.of(type), defaultValue);
    }

    private boolean instanceOfFlat(TypeId a, TypeId b, boolean defaultValue, boolean strict) {
        if (OBJECT.equals(b)) {
            return true;
        }
        if (strict && OBJECT.equals(a)) {
            // There is nothing above Object
            return false;
        }
        if (a == null || b == null) {
            return defaultValue;
        }
        if (Objects.equals(a, b)) {
            return true;
        }
        if (!strict) {
            if (isInterface(b, defaultValue)) {
                return true;
            }
            if (OBJECT.equals(a)) {
                // There is nothing above Object
                return false;
            }
        }
        var out = resolveInstanceOf(a, b);
        return out == null ? defaultValue : out;
    }

    private TypeId joinFlat(TypeId a, TypeId b) {
        if (Objects.equals(a, b)) {
            return a;
        }
        if (OBJECT.equals(a) || OBJECT.equals(b)) {
            return OBJECT;
        }
        if (a == null || b == null) {
            return null;
        }
        return resolveJoin(a, b);
    }

    private static final class UnknownResolver extends TypeResolver {
        @Override
        protected TypeId resolveJoin(TypeId a, TypeId b) {
            return null;
        }

        @Override
        protected Boolean resolveInstanceOf(TypeId a, TypeId b) {
            return null;
        }

        @Override
        protected Boolean resolveIsInterface(TypeId type) {
            return null;
        }
    }

    private static final class SimpleResolver extends TypeResolver {
        private final Function<TypeId, ClassHeader> mapper;

        SimpleResolver(Function<TypeId, ClassHeader> mapper) {
            this.mapper = Objects.requireNonNull(mapper);
        }

        private Set<TypeId> hierarchy(TypeId type, boolean[] exact) {
            var out = new LinkedHashSet<TypeId>();
            while (type != null) {
                if (!out.add(type)) {
                    // Cycle detected in the hierarchy, stop early
                    return out;
                }
                var info = mapper.apply(type);
                if (info == null) {
                    // The class is unknown: no further hierarchy information
                    exact[0] = false;
                    return out;
                }
                type = info.getSuperclass();
            }
            return out;
        }

        // join() only walks superclass chains: traversing interface
        // hierarchies is slow and may yield multiple equally valid answers
        @Override
        protected TypeId resolveJoin(TypeId a, TypeId b) {
            boolean[] exact = {true};

            var a_hierarchy = hierarchy(a, exact);
            var a_iter = a_hierarchy.iterator();
            var b_hierarchy = hierarchy(b, exact);
            var b_iter = b_hierarchy.iterator();

            boolean empty;
            do {
                empty = true;
                if (a_iter.hasNext()) {
                    var a_test = a_iter.next();
                    if (b_hierarchy.contains(a_test)) {
                        return a_test;
                    }
                    empty = false;
                }
                if (b_iter.hasNext()) {
                    var b_test = b_iter.next();
                    if (a_hierarchy.contains(b_test)) {
                        return b_test;
                    }
                    empty = false;
                }
            } while (!empty);

            return exact[0] ? OBJECT : null;
        }

        // Iterative breadth-first walk: no recursion, so arbitrarily deep
        // (but valid) hierarchies cannot overflow the stack. The same type
        // may be queued more than once (diamond inheritance, interfaces
        // sharing the Object superclass), so each type is processed at
        // most once; this also makes cyclic (invalid) hierarchies terminate
        @Override
        protected Boolean resolveInstanceOf(TypeId a, TypeId b) {
            var visited = new LinkedHashSet<TypeId>();
            var queue = new ArrayDeque<TypeId>();
            queue.add(a);
            boolean exact = true;
            while (!queue.isEmpty()) {
                var type = queue.poll();
                if (type.equals(b)) return true;
                if (!visited.add(type)) {
                    // Already processed: a diamond ancestor, a shared Object
                    // superclass, or a cycle — nothing new to explore
                    continue;
                }
                var info = mapper.apply(type);

                // The class is unknown to the mapper
                if (info == null) {
                    exact = false;
                    continue;
                }
                if ((info.getAccessFlags() & ACC_INTERFACE) != 0) {
                    // Interfaces always list Object as their superclass;
                    // queuing it would only add duplicates (a diamond
                    // is not a cycle), so the superclass is skipped
                    assert OBJECT.equals(info.getSuperclass())
                            : "interface superclass must be Object";
                    queue.addAll(info.getInterfaces());
                    continue;
                }
                // A null superclass (e.g. of Object itself) is simply not queued
                if (info.getSuperclass() != null) {
                    queue.add(info.getSuperclass());
                }
                queue.addAll(info.getInterfaces());
            }
            return exact ? false : null;
        }

        @Override
        protected Boolean resolveIsInterface(TypeId type) {
            var info = mapper.apply(type);
            if (info == null) return null;
            return (info.getAccessFlags() & ACC_INTERFACE) != 0;
        }
    }
}
