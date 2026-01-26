package com.v7878.dex.immutable;

public abstract sealed class MemberId permits MethodId, FieldId {
    protected void checkDeclaringClass() {
        var type = getDeclaringClass();
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Invalid declaring class: " + type);
        }
    }

    public abstract TypeId getDeclaringClass();

    public abstract String getName();
}
