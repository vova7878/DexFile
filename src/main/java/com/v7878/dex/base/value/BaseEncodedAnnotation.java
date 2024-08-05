package com.v7878.dex.base.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedAnnotation;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.util.CollectionUtils;

import java.util.Objects;

public abstract class BaseEncodedAnnotation implements EncodedAnnotation {
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getElements());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof EncodedAnnotation other
                && Objects.equals(getType(), other.getType())
                && Objects.equals(getElements(), other.getElements());
    }

    @Override
    public int compareTo(EncodedValue other) {
        if (other == this) return 0;
        int out = ValueType.compare(getValueType(), other.getValueType());
        if (out != 0) return out;
        var other_anno = ((EncodedAnnotation) other);
        out = CollectionUtils.compareNonNull(getType(), other_anno.getType());
        if (out != 0) return out;
        return CollectionUtils.compareLexicographically(
                getElements(), other_anno.getElements());
    }
}
