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

import com.v7878.dex.EncodedValue.ArrayValue;
import com.v7878.dex.bytecode.Instruction;

public abstract class DataFilter implements DataCollector {

    @Override
    public void add(String value) {
    }

    @Override
    public void add(TypeId value) {
        value.collectData(this);
    }

    @Override
    public void add(ProtoId value) {
        value.collectData(this);
    }

    @Override
    public void add(FieldId value) {
        value.collectData(this);
    }

    @Override
    public void add(MethodId value) {
        value.collectData(this);
    }

    @Override
    public void add(MethodHandleItem value) {
        value.collectData(this);
    }

    @Override
    public void add(CallSiteId value) {
        value.collectData(this);
    }

    @Override
    public void add(ClassDef value) {
        value.collectData(this);
    }

    @Override
    public void add(ClassData value) {
        value.collectData(this);
    }

    @Override
    public void add(TypeList value) {
        value.collectData(this);
    }

    @Override
    public void add(AnnotationItem value) {
        value.collectData(this);
    }

    @Override
    public void add(AnnotationSet value) {
        value.collectData(this);
    }

    @Override
    public void add(AnnotationSetList value) {
        value.collectData(this);
    }

    @Override
    public void add(ArrayValue value) {
        value.collectData(this);
    }

    @Override
    public void add(CodeItem value) {
        value.collectData(this);
    }

    @Override
    public void add(DebugInfo value) {
        value.collectData(this);
    }

    @Override
    public void fill(CatchHandlerElement value) {
        value.collectData(this);
    }

    @Override
    public void fill(CatchHandler value) {
        value.collectData(this);
    }

    @Override
    public void fill(Instruction value) {
        value.collectData(this);
    }

    @Override
    public void fill(TryItem value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedField value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedMethod value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedAnnotation value) {
        value.collectData(this);
    }

    @Override
    public void fill(AnnotationElement value) {
        value.collectData(this);
    }

    @Override
    public void fill(EncodedValue value) {
        value.collectData(this);
    }

    @Override
    public void fill(DebugItem value) {
        value.collectData(this);
    }
}
