package com.v7878.dex.util;

import com.v7878.dex.AccessFlags;
import com.v7878.dex.base.BaseFieldDef;
import com.v7878.dex.base.BaseMethodDef;
import com.v7878.dex.base.BaseTypeId;
import com.v7878.dex.iface.Annotation;
import com.v7878.dex.iface.FieldDef;
import com.v7878.dex.iface.MethodDef;
import com.v7878.dex.iface.MethodImplementation;
import com.v7878.dex.iface.Parameter;
import com.v7878.dex.iface.TypeId;
import com.v7878.dex.iface.value.EncodedValue;

import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;

public class CollectionUtils {
    public static <T extends Comparable<? super T>> int compareNonNull(T left, T right) {
        return left.compareTo(right);
    }

    public static <T extends Comparable<? super T>> int compareLexicographically(
            List<? extends T> left, List<? extends T> right) {
        return compareLexicographically(CollectionUtils::compareNonNull, left, right);
    }

    public static <T> int compareLexicographically(
            Comparator<? super T> comparator, List<? extends T> left, List<? extends T> right) {
        int lsize = left.size();
        int rsize = right.size();
        int size = Math.min(lsize, rsize);

        var elements1 = left.iterator();
        var elements2 = right.iterator();
        for (int i = 0; i < size; i++) {
            int out = comparator.compare(elements1.next(), elements2.next());
            if (out != 0) return out;
        }

        return Integer.compare(lsize, rsize);
    }

    private static final TypeId EMPTY_TYPE = new BaseTypeId() {
        @Override
        public String getDescriptor() {
            return "";
        }
    };

    private static final FieldDef LAST_STATIC_FIELD = new BaseFieldDef() {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public int getAccessFlags() {
            return AccessFlags.STATIC.value();
        }

        @Override
        public TypeId getType() {
            return EMPTY_TYPE;
        }

        @Override
        public EncodedValue getInitialValue() {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override
        public int getHiddenApiFlags() {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override
        public NavigableSet<? extends Annotation> getAnnotations() {
            throw new UnsupportedOperationException("Stub!");
        }
    };

    public static <T extends FieldDef> NavigableSet<T> getStaticFieldsSubset(NavigableSet<T> set) {
        //noinspection unchecked
        return set.headSet((T) LAST_STATIC_FIELD, true);
    }

    public static <T extends FieldDef> NavigableSet<T> getInstanceFieldsSubset(NavigableSet<T> set) {
        //noinspection unchecked
        return set.tailSet((T) LAST_STATIC_FIELD, false);
    }

    private static final MethodDef LAST_DIRECT_METHOD = new BaseMethodDef() {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public int getAccessFlags() {
            return AccessFlagUtils.DIRECT_MASK;
        }

        @Override
        public TypeId getReturnType() {
            return EMPTY_TYPE;
        }

        @Override
        public List<? extends Parameter> getParameters() {
            return List.of();
        }

        @Override
        public int getHiddenApiFlags() {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override
        public MethodImplementation getImplementation() {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override
        public NavigableSet<? extends Annotation> getAnnotations() {
            throw new UnsupportedOperationException("Stub!");
        }
    };

    public static <T extends MethodDef> NavigableSet<T> getDirectMethodsSubset(NavigableSet<T> set) {
        //noinspection unchecked
        return set.headSet((T) LAST_DIRECT_METHOD, true);
    }

    public static <T extends MethodDef> NavigableSet<T> getVirtualMethodsSubset(NavigableSet<T> set) {
        //noinspection unchecked
        return set.tailSet((T) LAST_DIRECT_METHOD, false);
    }
}
