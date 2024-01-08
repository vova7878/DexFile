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

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Comparator;
import java.util.Objects;

public final class AnnotationElement implements Mutable {

    public static final Comparator<AnnotationElement> COMPARATOR = (a, b) -> {
        int out = StringId.COMPARATOR.compare(a.name, b.name);
        if (out != 0) {
            return out;
        }

        // a.name == b.name
        //TODO?
        throw new IllegalStateException(
                "can`t compare annotation elements with same name: " + a + " " + b);
    };

    private String name;
    private EncodedValue value;

    public AnnotationElement(String name, EncodedValue value) {
        setName(name);
        setValue(value);
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name can`t be null");
    }

    public String getName() {
        return name;
    }

    public void setValue(EncodedValue value) {
        this.value = Objects.requireNonNull(value,
                "value can`t be null").mutate();
    }

    public EncodedValue getValue() {
        return value;
    }

    public static AnnotationElement read(RandomInput in, ReadContext context) {
        String name = context.string(in.readULeb128());
        EncodedValue value = EncodedValueReader.readValue(in, context);
        return new AnnotationElement(name, value);
    }

    public void collectData(DataCollector data) {
        data.add(name);
        data.fill(value);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(context.getStringIndex(name));
        value.write(context, out);
    }

    @Override
    public String toString() {
        return "AnnotationElement{" + name + " = " + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AnnotationElement) {
            AnnotationElement aeobj = (AnnotationElement) obj;
            return Objects.equals(name, aeobj.name)
                    && Objects.equals(value, aeobj.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public AnnotationElement mutate() {
        return new AnnotationElement(name, value);
    }
}
