/*
 * Copyright (c) 2023 Vladimir Kozelkov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
