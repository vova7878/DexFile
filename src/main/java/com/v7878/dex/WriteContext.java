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

public interface WriteContext {
    WriteOptions getOptions();

    int getStringIndex(String value);

    int getTypeIndex(TypeId value);

    int getProtoIndex(ProtoId value);

    int getFieldIndex(FieldId value);

    int getMethodIndex(MethodId value);

    int getCallSiteIndex(CallSiteId value);

    int getMethodHandleIndex(MethodHandleItem value);

    int getTypeListOffset(TypeList value);

    int getAnnotationOffset(AnnotationItem value);

    int getAnnotationSetOffset(AnnotationSet value);

    int getAnnotationSetListOffset(AnnotationSetList value);

    int getClassDataOffset(ClassData value);

    int getAnnotationsDirectoryOffset(ClassDef value);

    int getArrayValueOffset(EncodedValue.ArrayValue value);

    int getCodeItemOffset(CodeItem value);
}
