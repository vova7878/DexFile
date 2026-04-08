package com.v7878.dex.rewriter;

import com.v7878.dex.ReferenceType;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.ExceptionHandler;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.Instruction21c;
import com.v7878.dex.immutable.bytecode.Instruction22c;
import com.v7878.dex.immutable.bytecode.Instruction31c;
import com.v7878.dex.immutable.bytecode.Instruction34c;
import com.v7878.dex.immutable.bytecode.Instruction35c;
import com.v7878.dex.immutable.bytecode.Instruction3rc;
import com.v7878.dex.immutable.bytecode.Instruction41c;
import com.v7878.dex.immutable.bytecode.Instruction45cc;
import com.v7878.dex.immutable.bytecode.Instruction4rcc;
import com.v7878.dex.immutable.bytecode.Instruction52c;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.immutable.debug.StartLocal;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.immutable.value.EncodedArray;
import com.v7878.dex.immutable.value.EncodedEnum;
import com.v7878.dex.immutable.value.EncodedField;
import com.v7878.dex.immutable.value.EncodedMethod;
import com.v7878.dex.immutable.value.EncodedMethodHandle;
import com.v7878.dex.immutable.value.EncodedMethodType;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.util.Converter;

import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;

public class DexRewriter {
    private static <T> List<T> rewriteList(List<? extends T> list, Function<T, T> rewriter) {
        return Converter.transform(list, value ->
                Objects.requireNonNull(rewriter.apply(value)));
    }

    private static <T extends Comparable<? super T>> NavigableSet<T> rewriteNavigableSet(
            NavigableSet<? extends T> set, Function<T, T> rewriter) {
        var out = new TreeSet<T>();
        for (var value : set) {
            out.add(Objects.requireNonNull(rewriter.apply(value)));
        }
        return Collections.unmodifiableNavigableSet(out);
    }

    private static <T> T rewriteNullable(T value, Function<T, T> rewriter) {
        return value == null ? null : rewriter.apply(value);
    }

    public Dex rewriteDex(Dex value) {
        return Dex.raw(rewriteList(value.getClasses(), this::rewriteClassDef));
    }

    public ClassDef rewriteClassDef(ClassDef value) {
        return ClassDef.raw(
                rewriteTypeId(value.getType()),
                value.getAccessFlags(),
                rewriteNullable(value.getSuperclass(), this::rewriteTypeId),
                rewriteList(value.getInterfaces(), this::rewriteTypeId),
                value.getSourceFile(),
                rewriteNavigableSet(value.getFields(), this::rewriteFieldDef),
                rewriteNavigableSet(value.getMethods(), this::rewriteMethodDef),
                rewriteNavigableSet(value.getAnnotations(), this::rewriteAnnotation)
        );
    }

    public TypeId rewriteTypeId(TypeId value) {
        return value; // nop
    }

    public FieldId rewriteFieldId(FieldId value) {
        return FieldId.of(
                rewriteTypeId(value.getDeclaringClass()),
                value.getName(),
                rewriteTypeId(value.getType())
        );
    }

    public ProtoId rewriteProtoId(ProtoId value) {
        return ProtoId.raw(
                rewriteTypeId(value.getReturnType()),
                rewriteList(value.getParameterTypes(), this::rewriteTypeId)
        );
    }

    public MethodId rewriteMethodId(MethodId value) {
        return MethodId.of(
                rewriteTypeId(value.getDeclaringClass()),
                value.getName(),
                rewriteProtoId(value.getProto())
        );
    }

    public MethodHandleId rewriteMethodHandleId(MethodHandleId value) {
        return MethodHandleId.of(
                value.getHandleType(),
                value.getHandleType().isMethodAccess() ?
                        rewriteMethodId((MethodId) value.getMember()) :
                        rewriteFieldId((FieldId) value.getMember())
        );
    }

    public CallSiteId rewriteCallSiteId(CallSiteId value) {
        return CallSiteId.raw(
                // This String does not refer to the dex format, but is simply for identification purposes
                value.getName(),
                rewriteMethodHandleId(value.getMethodHandle()),
                value.getMethodName(),
                rewriteProtoId(value.getMethodProto()),
                rewriteList(value.getExtraArguments(), this::rewriteEncodedValue)
        );
    }

    public Annotation rewriteAnnotation(Annotation value) {
        return Annotation.raw(
                value.getVisibility(),
                rewriteTypeId(value.getType()),
                rewriteNavigableSet(value.getElements(), this::rewriteAnnotationElement)
        );
    }

    public AnnotationElement rewriteAnnotationElement(AnnotationElement value) {
        return AnnotationElement.of(
                value.getName(),
                rewriteEncodedValue(value.getValue())
        );
    }

    public EncodedValue rewriteEncodedValue(EncodedValue value) {
        return switch (value.getValueType()) {
            case TYPE -> EncodedType.of(rewriteTypeId(((EncodedType) value).getValue()));
            case FIELD -> EncodedField.of(rewriteFieldId(((EncodedField) value).getValue()));
            case ENUM -> EncodedEnum.of(rewriteFieldId(((EncodedEnum) value).getValue()));
            case METHOD -> EncodedMethod.of(rewriteMethodId(((EncodedMethod) value).getValue()));
            case METHOD_TYPE ->
                    EncodedMethodType.of(rewriteProtoId(((EncodedMethodType) value).getValue()));
            case METHOD_HANDLE ->
                    EncodedMethodHandle.of(rewriteMethodHandleId(((EncodedMethodHandle) value).getValue()));
            case ARRAY ->
                    EncodedArray.raw(rewriteList(((EncodedArray) value).getValue(), this::rewriteEncodedValue));
            case ANNOTATION -> {
                var annotation = (EncodedAnnotation) value;
                yield EncodedAnnotation.raw(
                        rewriteTypeId(annotation.getType()),
                        rewriteNavigableSet(annotation.getElements(), this::rewriteAnnotationElement)
                );
            }
            default -> value;
        };
    }

    public FieldDef rewriteFieldDef(FieldDef value) {
        return FieldDef.raw(
                value.getName(),
                rewriteTypeId(value.getType()),
                value.getAccessFlags(),
                value.getHiddenApiFlags(),
                rewriteNullable(value.getInitialValue(), this::rewriteEncodedValue),
                rewriteNavigableSet(value.getAnnotations(), this::rewriteAnnotation)
        );
    }

    public MethodDef rewriteMethodDef(MethodDef value) {
        return MethodDef.raw(
                value.getName(),
                rewriteTypeId(value.getReturnType()),
                rewriteList(value.getParameters(), this::rewriteParameter),
                value.getAccessFlags(),
                value.getHiddenApiFlags(),
                rewriteNullable(value.getImplementation(), this::rewriteMethodImplementation),
                rewriteNavigableSet(value.getAnnotations(), this::rewriteAnnotation)
        );
    }

    public MethodImplementation rewriteMethodImplementation(MethodImplementation value) {
        return MethodImplementation.raw(
                value.getRegisterCount(),
                rewriteList(value.getInstructions(), this::rewriteInstruction),
                rewriteNavigableSet(value.getTryBlocks(), this::rewriteTryBlock),
                rewriteList(value.getDebugItems(), this::rewriteDebugItem)
        );
    }

    public Object rewriteReference(ReferenceType type, Object value) {
        Objects.requireNonNull(value);
        return switch (type) {
            case TYPE -> rewriteTypeId((TypeId) value);
            case FIELD -> rewriteFieldId((FieldId) value);
            case METHOD -> rewriteMethodId((MethodId) value);
            case PROTO -> rewriteProtoId((ProtoId) value);
            case CALLSITE -> rewriteCallSiteId((CallSiteId) value);
            case METHOD_HANDLE -> rewriteMethodHandleId((MethodHandleId) value);
            case STRING, RAW_INDEX -> value;
        };
    }

    public Instruction rewriteInstruction(Instruction value) {
        if (value instanceof Instruction21c i) {
            return Instruction21c.of(
                    i.getOpcode(),
                    i.getRegister1(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction22c i) {
            return Instruction22c.of(
                    i.getOpcode(),
                    i.getRegister1(),
                    i.getRegister2(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction31c i) {
            return Instruction31c.of(
                    i.getOpcode(),
                    i.getRegister1(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction34c i) {
            return Instruction34c.of(
                    i.getOpcode(),
                    i.getRegisterCount(),
                    i.getRegister1(),
                    i.getRegister2(),
                    i.getRegister3(),
                    i.getRegister4(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction35c i) {
            return Instruction35c.of(
                    i.getOpcode(),
                    i.getRegisterCount(),
                    i.getRegister1(),
                    i.getRegister2(),
                    i.getRegister3(),
                    i.getRegister4(),
                    i.getRegister5(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction3rc i) {
            return Instruction3rc.of(
                    i.getOpcode(),
                    i.getRegisterCount(),
                    i.getStartRegister(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction41c i) {
            return Instruction41c.of(
                    i.getOpcode(),
                    i.getRegister1(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        if (value instanceof Instruction45cc i) {
            return Instruction45cc.of(
                    i.getOpcode(),
                    i.getRegisterCount(),
                    i.getRegister1(),
                    i.getRegister2(),
                    i.getRegister3(),
                    i.getRegister4(),
                    i.getRegister5(),
                    rewriteReference(i.getReferenceType1(), i.getReference1()),
                    rewriteReference(i.getReferenceType2(), i.getReference2())
            );
        }
        if (value instanceof Instruction4rcc i) {
            return Instruction4rcc.of(
                    i.getOpcode(),
                    i.getRegisterCount(),
                    i.getStartRegister(),
                    rewriteReference(i.getReferenceType1(), i.getReference1()),
                    rewriteReference(i.getReferenceType2(), i.getReference2())
            );
        }
        if (value instanceof Instruction52c i) {
            return Instruction52c.of(
                    i.getOpcode(),
                    i.getRegister1(),
                    i.getRegister2(),
                    rewriteReference(i.getReferenceType1(), i.getReference1())
            );
        }
        return value;
    }

    public TryBlock rewriteTryBlock(TryBlock value) {
        return TryBlock.raw(
                value.getStartAddress(),
                value.getUnitCount(),
                value.getCatchAllAddress(),
                rewriteList(value.getHandlers(), this::rewriteExceptionHandler)
        );
    }

    public ExceptionHandler rewriteExceptionHandler(ExceptionHandler value) {
        return ExceptionHandler.of(
                rewriteTypeId(value.getExceptionType()),
                value.getAddress()
        );
    }

    public DebugItem rewriteDebugItem(DebugItem value) {
        if (value instanceof StartLocal local) {
            return StartLocal.of(local.getRegister(), local.getName(),
                    rewriteTypeId(local.getType()), local.getSignature());
        }
        return value;
    }

    public Parameter rewriteParameter(Parameter value) {
        return Parameter.raw(
                rewriteTypeId(value.getType()),
                value.getName(),
                rewriteNavigableSet(value.getAnnotations(), this::rewriteAnnotation)
        );
    }
}
