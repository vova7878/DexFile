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

import com.v7878.dex.bytecode.CodeBuilder;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.PCList;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class EncodedMethod implements PublicCloneable {

    public static final Comparator<EncodedMethod> COMPARATOR = (a, b) -> {
        int out = MethodId.COMPARATOR.compare(a.method, b.method);
        if (out != 0) {
            return out;
        }

        // a.method == b.method
        //TODO?
        throw new IllegalStateException(
                "can`t compare encoded methods with safe method id" + a + " " + b);
    };

    private MethodId method;
    private int access_flags;
    private AnnotationSet annotations;
    private AnnotationSetList parameter_annotations;
    private CodeItem code;

    public EncodedMethod(MethodId method,
                         int access_flags, AnnotationSet annotations,
                         AnnotationSetList parameter_annotations, CodeItem code) {
        setMethod(method);
        setAccessFlags(access_flags);
        setAnnotations(annotations);
        setParameterAnnotations(parameter_annotations);
        setCode(code);
    }

    public EncodedMethod(MethodId method, int access_flags) {
        this(method, access_flags, null, null, null);
    }

    public final void setMethod(MethodId method) {
        this.method = Objects.requireNonNull(method,
                "method can`t be null").clone();
    }

    public final MethodId getMethod() {
        return method;
    }

    public final void setAccessFlags(int access_flags) {
        this.access_flags = access_flags;
    }

    public final int getAccessFlags() {
        return access_flags;
    }

    public final void setAnnotations(AnnotationSet annotations) {
        this.annotations = annotations == null
                ? AnnotationSet.empty() : annotations.clone();
    }

    public final AnnotationSet getAnnotations() {
        return annotations;
    }

    public final void setParameterAnnotations(
            AnnotationSetList parameter_annotations) {
        this.parameter_annotations = parameter_annotations == null
                ? AnnotationSetList.empty() : parameter_annotations.clone();
    }

    public final AnnotationSetList getParameterAnnotations() {
        return parameter_annotations;
    }

    public final void setCode(CodeItem code) {
        this.code = code == null ? null : code.clone();
    }

    public EncodedMethod withCode(int locals_size, Consumer<CodeBuilder> consumer) {
        int ins_size = method.getProto().getInputRegistersCount();
        setCode(CodeBuilder.build(ins_size + locals_size, ins_size,
                (access_flags & Modifier.STATIC) == 0, consumer));
        return this;
    }

    public final CodeItem getCode() {
        return code;
    }

    public static EncodedMethod read(RandomInput in, ReadContext context,
                                     MethodId method, Map<MethodId, AnnotationSet> annotated_methods,
                                     Map<MethodId, AnnotationSetList> annotated_parameters) {
        int access_flags = in.readULeb128();
        AnnotationSet annotations = annotated_methods.get(method);
        AnnotationSetList parameter_annotations
                = annotated_parameters.get(method);
        int code_off = in.readULeb128();
        CodeItem code = null;
        if (code_off != 0) {
            code = CodeItem.read(in.duplicate(code_off), context);
        }
        return new EncodedMethod(method, access_flags, annotations,
                parameter_annotations, code);
    }

    public static PCList<EncodedMethod> readArray(RandomInput in,
                                                  ReadContext context, int size,
                                                  Map<MethodId, AnnotationSet> annotated_methods,
                                                  Map<MethodId, AnnotationSetList> annotated_parameters) {
        PCList<EncodedMethod> out = PCList.empty();
        int methodIndex = 0;
        for (int i = 0; i < size; i++) {
            methodIndex += in.readULeb128();
            out.add(read(in, context, context.method(methodIndex),
                    annotated_methods, annotated_parameters));
        }
        return out;
    }

    public void collectData(DataCollector data) {
        data.add(method);
        if (!annotations.isEmpty()) {
            data.add(annotations);
        }
        if (!parameter_annotations.isEmpty()) {
            data.add(parameter_annotations);
        }
        if (code != null) {
            data.add(code);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(access_flags);
        out.writeULeb128(code == null ? 0 : context.getCodeItemOffset(code));
    }

    public static void writeArray(WriteContext context, RandomOutput out,
                                  EncodedMethod[] encoded_methods) {
        Arrays.sort(encoded_methods, COMPARATOR);
        int index = 0;
        for (EncodedMethod tmp : encoded_methods) {
            int diff = context.getMethodIndex(tmp.method) - index;
            index += diff;
            out.writeULeb128(diff);
            tmp.write(context, out);
        }
    }

    public static void writeArray(WriteContext context, RandomOutput out,
                                  PCList<EncodedMethod> encoded_methods) {
        writeArray(context, out, encoded_methods.toArray(new EncodedMethod[0]));
    }

    @Override
    public String toString() {
        String flags = Modifier.toString(access_flags);
        if (flags.length() != 0) {
            flags += " ";
        }
        return flags + method;
    }

    @Override
    public PublicCloneable clone() {
        return new EncodedMethod(method, access_flags, annotations,
                parameter_annotations, code);
    }
}
