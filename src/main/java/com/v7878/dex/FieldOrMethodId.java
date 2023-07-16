package com.v7878.dex;

import java.util.Objects;

public abstract class FieldOrMethodId implements PublicCloneable {

    private TypeId declaring_class;
    private String name;

    public FieldOrMethodId(TypeId declaring_class, String name) {
        setDeclaringClass(declaring_class);
        setName(name);
    }

    public final void setDeclaringClass(TypeId declaring_class) {
        this.declaring_class = Objects.requireNonNull(declaring_class,
                "declaring_class can`t be null").clone();
    }

    public final TypeId getDeclaringClass() {
        return declaring_class;
    }

    public final void setName(String name) {
        this.name = Objects.requireNonNull(name, "name can`t be null");
    }

    public final String getName() {
        return name;
    }

    public void collectData(DataCollector data) {
        data.add(declaring_class);
        data.add(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldOrMethodId) {
            FieldOrMethodId fmobj = (FieldOrMethodId) obj;
            return Objects.equals(declaring_class, fmobj.declaring_class)
                    && Objects.equals(name, fmobj.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaring_class, name);
    }

    @Override
    public abstract FieldOrMethodId clone();
}
