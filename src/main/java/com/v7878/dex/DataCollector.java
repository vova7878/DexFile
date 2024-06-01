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

public interface DataCollector {

    void add(String value);

    void add(TypeId value);

    void add(ProtoId value);

    void add(FieldId value);

    void add(MethodId value);

    void add(MethodHandleItem value);

    void add(CallSiteId value);

    void add(ClassDef value);

    void add(ClassData value);

    void add(TypeList value);

    void add(AnnotationItem value);

    void add(AnnotationSet value);

    void add(AnnotationSetList value);

    void add(ArrayValue value);

    void add(CodeItem value);

    void fill(CatchHandlerElement value);

    void fill(CatchHandler value);

    void fill(Instruction value);

    void fill(TryItem value);

    void fill(EncodedField value);

    void fill(EncodedMethod value);

    void fill(EncodedAnnotation value);

    void fill(AnnotationElement value);

    void fill(EncodedValue value);
}
