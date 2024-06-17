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

import static com.v7878.dex.DexConstants.ACC_CONSTRUCTOR;
import static com.v7878.dex.DexConstants.ACC_PRIVATE;
import static com.v7878.dex.DexConstants.ACC_STATIC;

import com.v7878.dex.bytecode.CodeBuilder;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.util.MutableList;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class EncodedMethod implements Mutable {

    public static final Comparator<EncodedMethod> COMPARATOR = (a, b) -> {
        int out = MethodId.COMPARATOR.compare(a.method, b.method);
        if (out != 0) {
            return out;
        }

        // a.method == b.method, but a != b
        throw new IllegalStateException(
                "can`t compare encoded methods with same method id" + a + " " + b);
    };

    private MethodId method;
    private int access_flags;
    private AnnotationSet annotations;
    private AnnotationSetList parameter_annotations;
    private CodeItem code;

    public EncodedMethod(MethodId method,
                         int access_flags, AnnotationSet annotations,
                         Collection<AnnotationSet> parameter_annotations, CodeItem code) {
        setMethod(method);
        setAccessFlags(access_flags);
        setAnnotations(annotations);
        setParameterAnnotations(parameter_annotations);
        setCode(code);
    }

    public EncodedMethod(MethodId method, int access_flags) {
        this(method, access_flags, null, null, null);
    }

    public void setMethod(MethodId method) {
        this.method = Objects.requireNonNull(method,
                "method can`t be null").mutate();
    }

    public MethodId getMethod() {
        return method;
    }

    public void setAccessFlags(int access_flags) {
        this.access_flags = access_flags;
    }

    public int getAccessFlags() {
        return access_flags;
    }

    public boolean isDirect() {
        return (access_flags & (ACC_PRIVATE | ACC_STATIC | ACC_CONSTRUCTOR)) != 0;
    }

    public void setAnnotations(AnnotationSet annotations) {
        this.annotations = annotations == null
                ? AnnotationSet.empty() : annotations.mutate();
    }

    public AnnotationSet getAnnotations() {
        return annotations;
    }

    public void setParameterAnnotations(
            Collection<AnnotationSet> parameter_annotations) {
        this.parameter_annotations = parameter_annotations == null
                ? AnnotationSetList.empty() : new AnnotationSetList(parameter_annotations);
    }

    public AnnotationSetList getParameterAnnotations() {
        return parameter_annotations;
    }

    public void setCode(CodeItem code) {
        this.code = code == null ? null : code.mutate();
    }

    public EncodedMethod withCode(int locals_size, Consumer<CodeBuilder> consumer) {
        EncodedMethod out = mutate();
        int ins_size = method.getProto().getInputRegistersCount();
        out.setCode(CodeBuilder.build(ins_size + locals_size, ins_size,
                (access_flags & Modifier.STATIC) == 0, consumer));
        return out;
    }

    public CodeItem getCode() {
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

    public static MutableList<EncodedMethod> readArray(RandomInput in,
                                                       ReadContext context, int size,
                                                       Map<MethodId, AnnotationSet> annotated_methods,
                                                       Map<MethodId, AnnotationSetList> annotated_parameters) {
        MutableList<EncodedMethod> out = MutableList.empty();
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

    private static void check(boolean is_direct_list, EncodedMethod encoded_method) {
        //TODO: improve messages
        if (is_direct_list) {
            if (!encoded_method.isDirect()) {
                throw new IllegalStateException("method must be direct");
            }
        } else {
            if (encoded_method.isDirect()) {
                throw new IllegalStateException("method must not be direct");
            }
        }
    }

    public static void writeArray(boolean is_direct_list, WriteContext context, RandomOutput out,
                                  EncodedMethod[] encoded_methods) {
        Arrays.sort(encoded_methods, COMPARATOR);
        int index = 0;
        for (EncodedMethod tmp : encoded_methods) {
            check(is_direct_list, tmp);
            int diff = context.getMethodIndex(tmp.method) - index;
            index += diff;
            out.writeULeb128(diff);
            tmp.write(context, out);
        }
    }

    public static void writeArray(boolean is_direct_list, WriteContext context, RandomOutput out,
                                  Collection<EncodedMethod> encoded_methods) {
        writeArray(is_direct_list, context, out, encoded_methods.toArray(new EncodedMethod[0]));
    }

    @Override
    public String toString() {
        String flags = DexModifier.printExecutableFlags(access_flags);
        if (!flags.isEmpty()) {
            flags += " ";
        }
        return flags + method;
    }

    @Override
    public EncodedMethod mutate() {
        return new EncodedMethod(method, access_flags, annotations,
                parameter_annotations, code);
    }
}
