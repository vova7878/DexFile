package com.v7878.dex.smali.printer;

import static com.v7878.dex.DexConstants.ACC_FINAL;
import static com.v7878.dex.DexConstants.ACC_PRIVATE;
import static com.v7878.dex.DexConstants.ACC_STATIC;
import static com.v7878.dex.util.Checks.shouldNotReachHere;

import com.v7878.collections.IntMap;
import com.v7878.collections.IntSet;
import com.v7878.dex.AccessFlag;
import com.v7878.dex.Opcode;
import com.v7878.dex.ReferenceType;
import com.v7878.dex.analysis.AnalysisOptions;
import com.v7878.dex.analysis.AnalyzedMethod;
import com.v7878.dex.analysis.ClassPath;
import com.v7878.dex.analysis.ClassPath.CallTarget;
import com.v7878.dex.analysis.ClassPath.FieldAccess;
import com.v7878.dex.analysis.ClassPath.InvocationError;
import com.v7878.dex.analysis.ClassPath.InvokeType;
import com.v7878.dex.analysis.ClassPath.ResolutionError;
import com.v7878.dex.analysis.ClassPath.ResolvedField;
import com.v7878.dex.analysis.ClassPath.ResolvedMethod;
import com.v7878.dex.analysis.ClassPath.Result;
import com.v7878.dex.analysis.Position;
import com.v7878.dex.analysis.Register;
import com.v7878.dex.analysis.RegisterLine;
import com.v7878.dex.analysis.TypeResolver;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.CommonAnnotation;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MemberId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.MethodImplementation;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.ArrayPayload;
import com.v7878.dex.immutable.bytecode.Instruction;
import com.v7878.dex.immutable.bytecode.InstructionN0ci;
import com.v7878.dex.immutable.bytecode.InstructionN0t;
import com.v7878.dex.immutable.bytecode.InstructionN0x;
import com.v7878.dex.immutable.bytecode.InstructionN1c;
import com.v7878.dex.immutable.bytecode.InstructionN1i;
import com.v7878.dex.immutable.bytecode.InstructionN1l;
import com.v7878.dex.immutable.bytecode.InstructionN1p;
import com.v7878.dex.immutable.bytecode.InstructionN1t;
import com.v7878.dex.immutable.bytecode.InstructionN1x;
import com.v7878.dex.immutable.bytecode.InstructionN2c;
import com.v7878.dex.immutable.bytecode.InstructionN2i;
import com.v7878.dex.immutable.bytecode.InstructionN2t;
import com.v7878.dex.immutable.bytecode.InstructionN2x;
import com.v7878.dex.immutable.bytecode.InstructionN3x;
import com.v7878.dex.immutable.bytecode.InstructionNrc;
import com.v7878.dex.immutable.bytecode.InstructionNrcc;
import com.v7878.dex.immutable.bytecode.InstructionNv4c;
import com.v7878.dex.immutable.bytecode.InstructionNv5c;
import com.v7878.dex.immutable.bytecode.InstructionNv5cc;
import com.v7878.dex.immutable.bytecode.InstructionRaw0c;
import com.v7878.dex.immutable.bytecode.InstructionRaw0x;
import com.v7878.dex.immutable.bytecode.SwitchElement;
import com.v7878.dex.immutable.bytecode.SwitchPayload;
import com.v7878.dex.immutable.debug.AdvancePC;
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.immutable.debug.EndLocal;
import com.v7878.dex.immutable.debug.LineNumber;
import com.v7878.dex.immutable.debug.RestartLocal;
import com.v7878.dex.immutable.debug.SetEpilogueBegin;
import com.v7878.dex.immutable.debug.SetFile;
import com.v7878.dex.immutable.debug.SetPrologueEnd;
import com.v7878.dex.immutable.debug.StartLocal;
import com.v7878.dex.immutable.value.EncodedAnnotation;
import com.v7878.dex.immutable.value.EncodedArray;
import com.v7878.dex.immutable.value.EncodedBoolean;
import com.v7878.dex.immutable.value.EncodedByte;
import com.v7878.dex.immutable.value.EncodedChar;
import com.v7878.dex.immutable.value.EncodedDouble;
import com.v7878.dex.immutable.value.EncodedEnum;
import com.v7878.dex.immutable.value.EncodedField;
import com.v7878.dex.immutable.value.EncodedFloat;
import com.v7878.dex.immutable.value.EncodedInt;
import com.v7878.dex.immutable.value.EncodedLong;
import com.v7878.dex.immutable.value.EncodedMethod;
import com.v7878.dex.immutable.value.EncodedMethodHandle;
import com.v7878.dex.immutable.value.EncodedMethodType;
import com.v7878.dex.immutable.value.EncodedNull;
import com.v7878.dex.immutable.value.EncodedShort;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.smali.SmaliOptions;
import com.v7878.dex.util.Formatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableSet;
import java.util.StringJoiner;
import java.util.TreeMap;

/**
 * The model-to-text printer behind the {@code SmaliIO.print} methods. One
 * instance may print any number of dexes, classes and methods; it holds no
 * state between top-level {@code print} calls.
 * <p>
 * A method body is printed in two passes. The first computes instruction
 * addresses, drops the alignment {@code nop} in front of a payload at an odd
 * address, claims each payload for the first switch instruction pointing at
 * it (further references get a duplicated copy at a virtual address),
 * validates every label target against instruction boundaries and collects
 * labels, try directives and debug items per address. The second emits the
 * address groups in the baksmali order - offset comment, debug directives,
 * labels, instruction, try-end labels, {@code .catch} directives - with the
 * payload directives last, each behind its label.
 * <p>
 * Raw instructions print live as {@code raw 0xN} and
 * {@code raw-ref ...} and re-parse. The instructions the label
 * grammar cannot express - legacy {@code m-packed-switch} and
 * {@code m-sparse-switch}, off-boundary or out-of-range targets, orphan
 * payloads - are printed commented out
 * with a {@code nop} appended, so the output always re-parses. With
 * {@link SmaliOptions#hasRelativeOffsets()} everything prints live with raw
 * offsets instead, which is faithful for any code but not re-parseable.
 * <p>
 * The information options add comments in one namespace: the analysis
 * block of a body and the resolution answer of every reference print
 * {@code #@key value}, so the text still re-parses and the answer is
 * visible next to the code it describes. Fields and methods resolve
 * against the class path of the options, or against the printed model when
 * the options carry none.
 */
public final class SmaliPrinter {
    private static final TypeId UNKNOWN_CLASS = TypeId.of("Lunknown;");

    private final SmaliEmitter out;
    private final SmaliOptions options;
    private TypeId classContext;

    // the class path and the resolver of the current top-level print call:
    // the option source, or the printed model as the fallback
    private ClassPath classPath;
    private TypeResolver resolver;

    // per-method-body state, valid only inside printBody
    private final IntMap<TreeMap<String, Label>> labels = new IntMap<>();
    private final List<Label> numbering = new ArrayList<>();
    private AnalyzedMethod analysis;
    private Throwable analysisFailure;

    // the first resolvable operand of the instruction being printed; its
    // resolution comment trails the instruction line
    private ReferenceType commentType;
    private Object commentReference;
    private boolean commentSuppressed;

    public SmaliPrinter(SmaliEmitter out, SmaliOptions options) {
        this.out = out;
        this.options = options;
        options.verify();
    }

    /**
     * Prints every class of the dex in order, separated by a blank line.
     */
    public void print(Dex dex) {
        start(new ClassPath(dex::findClass));
        boolean first = true;
        for (ClassDef cls : dex.getClasses()) {
            if (!first) {
                out.newline();
            }
            first = false;
            printClassDef(cls);
        }
    }

    /**
     * Prints one class block.
     */
    public void print(ClassDef cls) {
        start(new ClassPath(type -> type.equals(cls.getType()) ? cls : null));
        printClassDef(cls);
    }

    /**
     * Prints one method block without an enclosing class, so member
     * references always use the long form.
     */
    public void print(MethodDef method) {
        classContext = null;
        start(null);
        printMethodDef(method);
    }

    /**
     * Selects the class path of the options, or the printed model as the
     * fallback; a bare type resolver leaves the resolution without class
     * definitions. Called once per top-level {@code print} call.
     */
    private void start(ClassPath fallback) {
        ClassPath cp = options.getClassPath();
        if (cp != null) {
            classPath = cp;
            resolver = cp.getTypeResolver();
            return;
        }
        TypeResolver tr = options.getTypeResolver();
        if (tr != null) {
            classPath = new ClassPath(type -> null);
            resolver = tr;
            return;
        }
        classPath = fallback != null ? fallback : new ClassPath(type -> null);
        resolver = classPath.getTypeResolver();
    }

    private void printClassDef(ClassDef cls) {
        classContext = cls.getType();

        out.append(".class ");
        printFlags(AccessFlag.getAccessFlagsForClass(cls.getAccessFlags()));
        printType(cls.getType());
        out.newline();

        if (cls.getSuperclass() != null) {
            out.append(".super ");
            printType(cls.getSuperclass());
            out.newline();
        }
        if (cls.getSourceFile() != null) {
            out.append(".source ");
            out.quoted(cls.getSourceFile());
            out.newline();
        }
        if (!cls.getInterfaces().isEmpty()) {
            out.newline();
            out.append("# interfaces").newline();
            for (TypeId iface : cls.getInterfaces()) {
                out.append(".implements ");
                printType(iface);
                out.newline();
            }
        }
        if (!cls.getAnnotations().isEmpty()) {
            out.newline();
            out.newline();
            out.append("# annotations").newline();
            printAnnotations(cls.getAnnotations());
        }
        printFieldSection("# static fields", cls.getStaticFields());
        printFieldSection("# instance fields", cls.getInstanceFields());
        printMethodSection("# direct methods", cls.getDirectMethods());
        printMethodSection("# virtual methods", cls.getVirtualMethods());
    }

    // ------------------------- class level -------------------------

    private void printFieldSection(String header, Iterable<FieldDef> fields) {
        boolean first = true;
        for (FieldDef field : fields) {
            out.newline();
            if (first) {
                out.newline();
                out.append(header).newline();
                first = false;
            }
            printField(field);
        }
    }

    private void printMethodSection(String header, Iterable<MethodDef> methods) {
        boolean first = true;
        for (MethodDef method : methods) {
            out.newline();
            if (first) {
                out.newline();
                out.append(header).newline();
                first = false;
            }
            printMethodDef(method);
        }
    }

    private void printField(FieldDef field) {
        out.append(".field ");
        printFlags(AccessFlag.getAccessFlagsForField(field.getAccessFlags()));
        out.simpleName(field.getName());
        out.append(':');
        printType(field.getType());
        if (field.getInitialValue() != null) {
            out.append(" = ");
            printValue(field.getInitialValue());
            printTrailingComment(constantComment(field.getInitialValue()));
        }
        out.newline();

        if (!field.getAnnotations().isEmpty()) {
            out.indent();
            printAnnotations(field.getAnnotations());
            out.deindent();
            out.append(".end field").newline();
        }
    }

    private void printFlags(AccessFlag[] flags) {
        for (AccessFlag flag : flags) {
            out.append(flag.toString()).append(' ');
        }
    }

    private void printAnnotations(NavigableSet<Annotation> annotations) {
        boolean first = true;
        for (Annotation annotation : annotations) {
            if (!first) {
                out.newline();
            }
            first = false;
            printAnnotation(annotation);
        }
    }

    private void printAnnotation(Annotation annotation) {
        out.append(".annotation ");
        out.append(annotation.getVisibility().toString()).append(' ');
        printType(annotation.getType());
        out.newline();
        out.indent();
        printElements(annotation.getElements());
        out.deindent();
        out.append(".end annotation").newline();
    }

    private void printSubAnnotation(CommonAnnotation annotation) {
        out.append(".subannotation ");
        printType(annotation.getType());
        out.newline();
        out.indent();
        printElements(annotation.getElements());
        out.deindent();
        out.append(".end subannotation");
    }

    private void printElements(NavigableSet<AnnotationElement> elements) {
        for (AnnotationElement element : elements) {
            out.simpleName(element.getName());
            out.append(" = ");
            printValue(element.getValue());
            printTrailingComment(constantComment(element.getValue()));
            out.newline();
        }
    }

    private void printValue(EncodedValue value) {
        if (value instanceof EncodedBoolean v) {
            out.append(Boolean.toString(v.getValue()));
        } else if (value instanceof EncodedByte v) {
            out.literal(v.getValue()).append('t');
        } else if (value instanceof EncodedShort v) {
            out.literal(v.getValue()).append('s');
        } else if (value instanceof EncodedInt v) {
            out.literal(v.getValue());
        } else if (value instanceof EncodedLong v) {
            out.longLiteral(v.getValue());
        } else if (value instanceof EncodedChar v) {
            out.quoted(v.getValue());
        } else if (value instanceof EncodedFloat v) {
            // TODO: This is not an exact value
            out.append(Float.toString(v.getValue())).append('f');
        } else if (value instanceof EncodedDouble v) {
            // TODO: This is not an exact value
            out.append(Double.toString(v.getValue()));
        } else if (value instanceof EncodedNull) {
            out.append("null");
        } else if (value instanceof EncodedString v) {
            out.quoted(v.getValue());
        } else if (value instanceof EncodedType v) {
            printType(v.getValue());
        } else if (value instanceof EncodedField v) {
            printFieldId(v.getValue());
        } else if (value instanceof EncodedEnum v) {
            out.append(".enum ");
            printFieldId(v.getValue());
        } else if (value instanceof EncodedMethod v) {
            printMethodId(v.getValue());
        } else if (value instanceof EncodedMethodType v) {
            printProto(v.getValue());
        } else if (value instanceof EncodedMethodHandle v) {
            printMethodHandle(v.getValue());
        } else if (value instanceof EncodedArray v) {
            printArray(v);
        } else if (value instanceof EncodedAnnotation v) {
            printSubAnnotation(v);
        } else {
            throw new IllegalArgumentException(
                    "Unknown encoded value: " + value.getClass());
        }
    }

    private void printArray(EncodedArray value) {
        var values = value.getValue();
        out.append('{');
        if (values.isEmpty()) {
            out.append('}');
            return;
        }
        out.newline();
        out.indent();
        for (int i = 0; i < values.size(); i++) {
            EncodedValue item = values.get(i);
            printValue(item);
            if (i + 1 < values.size()) {
                out.append(',');
            }
            printTrailingComment(constantComment(item));
            out.newline();
        }
        out.deindent();
        out.append('}');
    }

    private void printFieldId(FieldId id) {
        if (implicitRefs() && id.getDeclaringClass().equals(classContext)) {
            out.simpleName(id.getName());
            out.append(':');
            printType(id.getType());
            return;
        }
        printType(id.getDeclaringClass());
        out.append("->");
        out.simpleName(id.getName());
        out.append(':');
        printType(id.getType());
    }

    private void printMethodId(MethodId id) {
        if (implicitRefs() && id.getDeclaringClass().equals(classContext)) {
            out.simpleName(id.getName());
            printProto(id.getProto());
            return;
        }
        printType(id.getDeclaringClass());
        out.append("->");
        out.simpleName(id.getName());
        printProto(id.getProto());
    }

    private void printProto(ProtoId proto) {
        out.append('(');
        for (TypeId type : proto.getParameterTypes()) {
            printType(type);
        }
        out.append(')');
        printType(proto.getReturnType());
    }

    private void printMethodHandle(MethodHandleId id) {
        out.append(id.getHandleType().toString()).append('@');
        if (id.getMember() instanceof FieldId field) {
            printFieldId(field);
        } else {
            printMethodId((MethodId) id.getMember());
        }
    }

    private void printCallSite(CallSiteId id) {
        out.simpleName(id.getName());
        out.append('(');
        out.quoted(id.getMethodName());
        out.append(", ");
        printProto(id.getMethodProto());
        var extras = id.getExtraArguments();
        // with resolution info every handle gets its own line and comment;
        // the grammar allows whitespace and comments between the tokens
        boolean detailed = options.hasResolutionInfo() && !extras.isEmpty();
        if (detailed) {
            out.append(',');
            out.newline();
            out.indent();
            for (int i = 0; i < extras.size(); i++) {
                EncodedValue extra = extras.get(i);
                printValue(extra);
                if (i + 1 < extras.size()) {
                    out.append(',');
                }
                printTrailingComment(callSiteComment(extra));
                out.newline();
            }
            out.deindent();
        } else {
            for (EncodedValue extra : extras) {
                out.append(", ");
                printValue(extra);
            }
        }
        out.append(")@");
        printMethodId((MethodId) id.getMethodHandle().getMember());
        if (options.hasResolutionInfo()) {
            printTrailingComment(handleComment(id.getMethodHandle(), true));
        }
    }

    private boolean implicitRefs() {
        return options.hasImplicitReferences() && classContext != null;
    }

    private void printType(TypeId type) {
        String descriptor = type.getDescriptor();
        int index = 0;
        while (index < descriptor.length() && descriptor.charAt(index) == '[') {
            out.append('[');
            index++;
        }
        if (index >= descriptor.length() || descriptor.charAt(index) != 'L') {
            out.append(descriptor.substring(index));
            return;
        }
        // quote every simple-name segment that a bare name cannot hold
        out.append('L');
        int start = index + 1;
        for (int i = start; i < descriptor.length(); i++) {
            char c = descriptor.charAt(i);
            if (c == '/' || c == ';') {
                out.simpleName(descriptor.substring(start, i));
                out.append(c);
                start = i + 1;
                if (c == ';') {
                    return;
                }
            }
        }
        out.append(descriptor.substring(start));
        out.append(';');
    }

    private void printReference(ReferenceType type, Object reference, Opcode op) {
        if (!commentSuppressed && commentType == null
                && (type == ReferenceType.FIELD || type == ReferenceType.METHOD
                || type == ReferenceType.METHOD_HANDLE)) {
            commentType = type;
            commentReference = reference;
        }
        switch (type) {
            case STRING -> out.quoted((String) reference);
            case TYPE -> printType((TypeId) reference);
            case FIELD -> printFieldId((FieldId) reference);
            case METHOD -> printMethodId((MethodId) reference);
            case PROTO -> printProto((ProtoId) reference);
            case CALLSITE -> printCallSite((CallSiteId) reference);
            case METHOD_HANDLE -> printMethodHandle((MethodHandleId) reference);
            case CLASS_DEF -> {
                out.append('@');
                printType((TypeId) reference);
            }
            case RAW_INDEX -> {
                // any of the three spellings parses for every raw-index
                // opcode; pick the one matching the opcode family
                String name = op.opname();
                if (name.contains("execute-inline")) {
                    out.append("inline@0x").hex((Integer) reference);
                } else if (name.contains("quick")
                        && (name.startsWith("invoke") || name.startsWith("m-invoke"))) {
                    out.append("vtable@0x").hex((Integer) reference);
                } else {
                    out.append("field@0x").hex((Integer) reference);
                }
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported reference type: " + type);
        }
    }

    // ------------------------- method level -------------------------

    private void printMethodDef(MethodDef method) {
        analysis = null;
        analysisFailure = null;
        out.append(".method ");
        printFlags(AccessFlag.getAccessFlagsForMethod(method.getAccessFlags()));
        out.simpleName(method.getName());
        out.append('(');
        for (Parameter parameter : method.getParameters()) {
            printType(parameter.getType());
        }
        out.append(')');
        printType(method.getReturnType());
        out.newline();

        var impl = method.getImplementation();
        out.indent();
        if (impl == null) {
            printParameters(method, true);
            printAnnotations(method.getAnnotations());
        } else {
            printBody(method, impl);
        }
        out.deindent();
        out.append(".end method").newline();
    }

    /**
     * Prints the {@code .param} directives of the method. A bodiless method
     * has no register file, so its parameters are printed commented out -
     * the grammar rejects a live {@code .param} without a register
     * directive.
     */
    private void printParameters(MethodDef method, boolean bodiless) {
        int p = (method.getAccessFlags() & ACC_STATIC) == 0 ? 1 : 0;
        for (Parameter parameter : method.getParameters()) {
            boolean withName = options.hasDebugInfo()
                    && parameter.getName() != null;
            if (!withName && parameter.getAnnotations().isEmpty()) {
                p += parameter.getType().getRegisterCount();
                continue;
            }
            if (bodiless) {
                out.startComment();
            }
            out.append(".param p").append(p);
            if (withName) {
                out.append(", ");
                out.quoted(parameter.getName());
            }
            out.append("    # ");
            printType(parameter.getType());
            out.newline();
            if (!parameter.getAnnotations().isEmpty()) {
                out.indent();
                printAnnotations(parameter.getAnnotations());
                out.deindent();
                out.append(".end param").newline();
            }
            if (bodiless) {
                out.endComment();
            }
            p += parameter.getType().getRegisterCount();
        }
    }

    private static final class Label {
        final int address;
        final String prefix;
        int sequence = -1;

        Label(int address, String prefix) {
            this.address = address;
            this.prefix = prefix;
        }
    }

    private record CatchLine(int start, int end, int handlerAddress,
                             TypeId type, boolean commented, Label tryStart,
                             Label tryEnd, Label handler) {
    }

    private record PayloadCopy(int payloadIndex, int switchIndex,
                               int labelAddress, Label label) {
    }

    private Label internLabel(int address, String prefix) {
        var byPrefix = labels.computeIfAbsent(address, x -> new TreeMap<>());
        var label = byPrefix.get(prefix);
        if (label == null) {
            label = new Label(address, prefix);
            byPrefix.put(prefix, label);
            numbering.add(label);
        }
        return label;
    }

    // a label rendered outside the address groups (payload and try-end
    // labels); it still participates in the sequential numbering
    private Label detachedLabel(int address, String prefix) {
        var label = new Label(address, prefix);
        numbering.add(label);
        return label;
    }

    private void printLabelName(Label label) {
        out.append(label.prefix);
        out.hex(label.sequence >= 0 ? label.sequence : label.address);
    }

    private void printBody(MethodDef method, MethodImplementation impl) {
        analyze(method);
        boolean instance = (method.getAccessFlags() & ACC_STATIC) == 0;
        var insns = impl.getInstructions();
        int count = insns.size();

        int[] address = new int[count + 1];
        boolean[] isPayload = new boolean[count];
        boolean[] dropped = new boolean[count];
        boolean[] fallback = new boolean[count];
        int codeSize = 0;
        for (int i = 0; i < count; i++) {
            address[i] = codeSize;
            codeSize += insns.get(i).getUnitCount();
            isPayload[i] = insns.get(i).getOpcode().isPayload();
        }
        address[count] = codeSize;

        // the alignment nop in front of a payload is regenerated on re-parse
        for (int i = 1; i < count; i++) {
            if (isPayload[i] && !isPayload[i - 1] && (address[i - 1] & 1) != 0
                    && isNop(insns.get(i - 1))) {
                dropped[i - 1] = true;
            }
        }

        // the anchors a label can bind to: retained instruction starts and
        // the end of the retained stream
        var anchors = new IntSet();
        int retainedEnd = 0;
        for (int i = 0; i < count; i++) {
            if (!isPayload[i] && !dropped[i]) {
                anchors.add(address[i]);
                retainedEnd = address[i] + insns.get(i).getUnitCount();
            }
        }
        anchors.add(retainedEnd);

        boolean anyMode = options.hasRelativeOffsets();

        // payload ownership: the first switch instruction pointing at a
        // payload claims it, every further one gets a duplicated copy
        int[] payloadOwner = new int[count];
        Arrays.fill(payloadOwner, -1);
        Label[] payloadLabel = new Label[count];
        Label[] payloadRef = new Label[count];
        boolean[] payloadFallback = new boolean[count];
        var duplicates = new ArrayList<PayloadCopy>();
        int virtualEnd = codeSize;

        // try directives keyed by the anchor of the last covered
        // instruction; an empty try starting at 0 renders before everything
        var catchesAt = new IntMap<List<CatchLine>>();
        var tryEndsAt = new IntMap<List<Label>>();

        // debug items keyed by the anchor they are printed at
        var debugAt = new IntMap<List<DebugItem>>();

        labels.clear();
        numbering.clear();

        for (int i = 0; i < count; i++) {
            if (isPayload[i] || dropped[i]) {
                continue;
            }
            Instruction insn = insns.get(i);
            Opcode op = insn.getOpcode();

            if (op.hasPayload()) {
                // TODO
                // the legacy m-packed/m-sparse switch has no parser support
                if (!anyMode && (op == Opcode.M_PACKED_SWITCH
                        || op == Opcode.M_SPARSE_SWITCH)) {
                    fallback[i] = true;
                    continue;
                }
                int target = address[i]
                        + ((InstructionN1t) insn).getBranchOffset();
                int p = findPayload(insns, address, isPayload, target);
                if (p < 0) {
                    if (!anyMode) {
                        fallback[i] = true;
                    }
                    continue;
                }
                if (payloadOwner[p] < 0) {
                    payloadOwner[p] = i;
                    if (payloadLabel[p] == null) {
                        payloadLabel[p] = detachedLabel(address[p],
                                payloadPrefix(op));
                    }
                    payloadRef[i] = payloadLabel[p];
                } else {
                    var label = detachedLabel(virtualEnd, payloadPrefix(op));
                    duplicates.add(new PayloadCopy(p, i, virtualEnd, label));
                    payloadRef[i] = label;
                    virtualEnd += insns.get(p).getUnitCount();
                }
                continue;
            }
            if (op.isBranch()) {
                if (anyMode) {
                    continue;
                }
                int target = address[i] + branchOffset(insn);
                if (!anchors.contains(target)) {
                    fallback[i] = true;
                    continue;
                }
                internLabel(target,
                        op.isUnconditionalBranch() ? "goto_" : "cond_");
            }
        }

        // switch element targets, relative to the owning switch instruction
        for (int p = 0; p < count; p++) {
            if (!isPayload[p]
                    || !(insns.get(p) instanceof SwitchPayload payload)) {
                continue;
            }
            int owner = payloadOwner[p];
            if (owner < 0) {
                if (!anyMode) {
                    payloadFallback[p] = true;
                }
                continue;
            }
            if (!anyMode && (!reassemblable(payload)
                    || !collectElementLabels(payload, address[owner],
                    anchors))) {
                payloadFallback[p] = true;
                fallback[owner] = true;
            }
        }
        for (PayloadCopy copy : duplicates) {
            var payload = (SwitchPayload) insns.get(copy.payloadIndex());
            if (!anyMode && (!reassemblable(payload)
                    || !collectElementLabels(payload, address[copy.switchIndex()],
                    anchors))) {
                fallback[copy.switchIndex()] = true;
            }
        }

        for (TryBlock tryBlock : impl.getTryBlocks()) {
            int start = tryBlock.getStartAddress();
            int end = tryBlock.getEndAddress();
            // the try-end label and .catch render after the last covered
            // instruction, not at the range end
            int lastCovered = end == 0 ? -1 : floorAnchor(anchors, end - 1);
            boolean valid = !anyMode && anchors.contains(start)
                    && anchors.contains(end) && end <= retainedEnd
                    && (tryBlock.getCatchAllAddress() == null
                    || anchors.contains(tryBlock.getCatchAllAddress()));
            // TODO: add early exit
            for (var handler : tryBlock.getHandlers()) {
                valid &= anchors.contains(handler.getAddress());
            }
            if (valid) {
                var tryStart = internLabel(start, "try_start_");
                if (tryBlock.getCatchAllAddress() != null) {
                    internLabel(tryBlock.getCatchAllAddress(), "catchall_");
                }
                for (var handler : tryBlock.getHandlers()) {
                    internLabel(handler.getAddress(), "catch_");
                }
                var tryEnds = tryEndsAt.computeIfAbsent(lastCovered,
                        x -> new ArrayList<>());
                var existing = tryEnds.stream()
                        .filter(l -> l.address == end).findFirst();
                Label tryEnd = existing.orElseGet(() -> {
                    var label = detachedLabel(end, "try_end_");
                    tryEnds.add(label);
                    return label;
                });
                addCatch(catchesAt, lastCovered, tryStart, tryEnd, tryBlock,
                        false);
            } else {
                // relative mode prints the range live with offsets, a range
                // the label grammar cannot express stays a comment
                addCatch(catchesAt, lastCovered, null, null, tryBlock,
                        !anyMode);
            }
        }

        if (options.hasDebugInfo()) {
            int cursor = 0;
            for (DebugItem item : impl.getDebugItems()) {
                if (item instanceof AdvancePC pc) {
                    cursor += pc.getAddrDiff();
                    continue;
                }
                // snap a mid-instruction address forward to the next anchor
                int anchor = Math.min(cursor, retainedEnd);
                Integer ceil = ceiling(anchors, anchor);
                Integer floor = floor(anchors, anchor);
                int group = ceil != null ? ceil : (floor != null ? floor : 0);
                debugAt.computeIfAbsent(group, x -> new ArrayList<>()).add(item);
            }
        }

        if (options.hasSequentialLabels()) {
            numbering.sort((a, b) -> a.address != b.address
                    ? Integer.compare(a.address, b.address)
                    : a.prefix.compareTo(b.prefix));
            var counters = new HashMap<String, Integer>();
            for (Label label : numbering) {
                label.sequence = counters.merge(label.prefix, 1, Integer::sum) - 1;
            }
        }

        // -------- emission --------

        int totalRegs = impl.getRegisterCount();
        int fullParams = method.countFullInputRegisters();
        if (options.hasUseLocals()) {
            out.append(".locals ").append(totalRegs - fullParams).newline();
        } else {
            out.append(".registers ").append(totalRegs).newline();
        }

        printParameters(method, false);
        printAnnotations(method.getAnnotations());
        if (options.hasAnalysisInfo()) {
            if (analysis != null) {
                printStateLine("entry", analysis.firstLine(), instance);
            } else {
                printAnalysisFailure(analysisFailure);
            }
        }
        out.newline();

        boolean payloadSection = !duplicates.isEmpty();
        for (int i = 0; i < count; i++) {
            payloadSection |= isPayload[i];
        }

        // an empty try at address 0 renders before the first group
        if (tryEndsAt.contains(-1)) {
            for (Label label : tryEndsAt.get(-1)) {
                printLabelLine(label);
            }
        }
        if (catchesAt.contains(-1)) {
            printCatchLines(catchesAt.get(-1));
        }

        boolean[] hasContent = new boolean[anchors.size()];
        for (int g = 0; g < anchors.size(); g++) {
            int a = anchors.at(g);
            hasContent[g] = !debugAt.get(a, List.of()).isEmpty()
                    || labels.contains(a)
                    || tryEndsAt.contains(a)
                    || catchesAt.contains(a);
            for (int i = 0; i < count && !hasContent[g]; i++) {
                hasContent[g] = !isPayload[i] && !dropped[i] && address[i] == a;
            }
        }

        for (int g = 0; g < anchors.size(); g++) {
            if (!hasContent[g]) {
                continue;
            }
            int a = anchors.at(g);
            int insnIndex = -1;
            for (int i = 0; i < count; i++) {
                if (!isPayload[i] && !dropped[i] && address[i] == a) {
                    insnIndex = i;
                    break;
                }
            }
            if (debugAt.contains(a)) {
                printDebugItems(debugAt.get(a), totalRegs, fullParams);
            }
            if (labels.contains(a)) {
                for (Label label : labels.get(a).values()) {
                    printLabelLine(label);
                }
            }
            Position position = null;
            if (insnIndex >= 0) {
                if (options.hasAnalysisInfo() || options.hasCodeOffsets()) {
                    out.append("#@0x").hex(a).newline();
                }
                if (analysis != null) {
                    position = analysis.getPositions().get(a);
                }
                if (options.hasAnalysisInfo() && position != null) {
                    printAnalysisBlock(position, instance);
                }
            }
            if (insnIndex >= 0) {
                printInstructionEntry(insns, address, fallback, payloadRef,
                        insnIndex, totalRegs, fullParams, position);
            }
            if (tryEndsAt.contains(a)) {
                for (Label label : tryEndsAt.get(a)) {
                    printLabelLine(label);
                }
            }
            if (catchesAt.contains(a)) {
                printCatchLines(catchesAt.get(a));
            }
            if (insnIndex >= 0) {
                boolean more = payloadSection;
                for (int h = g + 1; h < anchors.size(); h++) {
                    more |= hasContent[h];
                }
                if (more) {
                    out.newline();
                }
            }
        }

        // -------- payload directives, in instruction list order --------

        int totalPayloads = duplicates.size();
        for (int i = 0; i < count; i++) {
            totalPayloads += isPayload[i] ? 1 : 0;
        }
        int printed = 0;
        for (int i = 0; i < count; i++) {
            if (!isPayload[i]) {
                continue;
            }
            printed++;
            boolean live = anyMode
                    || (payloadOwner[i] >= 0 && !payloadFallback[i]);
            int base = live && payloadOwner[i] >= 0
                    ? address[payloadOwner[i]] : 0;
            printPayloadDirective(insns.get(i), live, payloadLabel[i], base,
                    printed == totalPayloads);
        }
        for (PayloadCopy copy : duplicates) {
            printed++;
            boolean live = !fallback[copy.switchIndex()];
            printPayloadDirective(insns.get(copy.payloadIndex()), live,
                    copy.label(), address[copy.switchIndex()],
                    printed == totalPayloads);
        }
    }

    private void addCatch(IntMap<List<CatchLine>> catchesAt,
                          int lastCovered, Label tryStart, Label tryEnd,
                          TryBlock tryBlock, boolean commented) {
        var lines = catchesAt.computeIfAbsent(lastCovered,
                x -> new ArrayList<>());
        // handler labels exist only in the label mode of a valid range
        boolean labelMode = tryStart != null;
        if (tryBlock.getCatchAllAddress() != null) {
            int address = tryBlock.getCatchAllAddress();
            Label handler = labelMode
                    ? internLabel(address, "catchall_") : null;
            lines.add(new CatchLine(tryBlock.getStartAddress(),
                    tryBlock.getEndAddress(), address, null, commented,
                    tryStart, tryEnd, handler));
        }
        for (var h : tryBlock.getHandlers()) {
            Label handler = labelMode
                    ? internLabel(h.getAddress(), "catch_") : null;
            lines.add(new CatchLine(tryBlock.getStartAddress(),
                    tryBlock.getEndAddress(), h.getAddress(),
                    h.getExceptionType(), commented,
                    tryStart, tryEnd, handler));
        }
    }

    private void printLabelLine(Label label) {
        out.append(':');
        printLabelName(label);
        out.newline();
    }

    private void printCatchLines(List<CatchLine> lines) {
        boolean anyMode = options.hasRelativeOffsets();
        for (CatchLine line : lines) {
            if (line.commented()) {
                out.startComment();
            }
            if (line.type() == null) {
                out.append(".catchall");
            } else {
                out.append(".catch ");
                printType(line.type());
            }
            out.append(" {");
            if (line.tryStart() != null) {
                out.append(':');
                printLabelName(line.tryStart());
                out.append(" .. :");
                printLabelName(line.tryEnd());
                out.append("} :");
                printLabelName(line.handler());
            } else if (anyMode) {
                out.offset(line.start());
                out.append(" .. ");
                out.offset(line.end());
                out.append("} ");
                out.offset(line.handlerAddress());
            } else {
                out.append("0x").hex(line.start());
                out.append(" .. 0x").hex(line.end());
                out.append("} 0x").hex(line.handlerAddress());
            }
            out.newline();
            if (line.commented()) {
                out.endComment();
            }
        }
    }

    private void printDebugItems(List<DebugItem> items, int totalRegs,
                                 int fullParams) {
        // list order: the parser re-derives debug items in text order, so
        // any reordering here would break the model round trip
        for (DebugItem item : items) {
            if (item instanceof SetPrologueEnd) {
                out.append(".prologue").newline();
            } else if (item instanceof SetEpilogueBegin) {
                out.append(".epilogue").newline();
            } else if (item instanceof SetFile file) {
                out.append(".source");
                if (file.getName() != null) {
                    out.append(' ');
                    out.quoted(file.getName());
                }
                out.newline();
            } else if (item instanceof LineNumber line) {
                out.append(".line ")
                        .append(Integer.toUnsignedLong(line.getLine()))
                        .newline();
            } else if (item instanceof StartLocal local) {
                out.append(".local ");
                printRegister(local.getRegister(), totalRegs, fullParams);
                if (local.getName() != null || local.getType() != null
                        || local.getSignature() != null) {
                    out.append(", ");
                    if (local.getName() != null) {
                        out.quoted(local.getName());
                    } else {
                        out.append("null");
                    }
                    out.append(':');
                    if (local.getType() != null) {
                        printType(local.getType());
                    } else {
                        out.append('V');
                    }
                    if (local.getSignature() != null) {
                        out.append(", ");
                        out.quoted(local.getSignature());
                    }
                }
                out.newline();
            } else if (item instanceof EndLocal local) {
                out.append(".end local ");
                printRegister(local.getRegister(), totalRegs, fullParams);
                out.newline();
            } else if (item instanceof RestartLocal local) {
                out.append(".restart local ");
                printRegister(local.getRegister(), totalRegs, fullParams);
                out.newline();
            } else {
                throw new IllegalArgumentException(
                        "Unknown debug item: " + item.getClass());
            }
        }
    }

    private void printPayloadDirective(Instruction payload, boolean live,
                                       Label label, int base, boolean last) {
        if (live && !options.hasRelativeOffsets() && label != null) {
            printLabelLine(label);
        }
        if (!live) {
            out.startComment();
        }
        if (payload instanceof ArrayPayload array) {
            out.append(".array-data ").append(array.getElementWidth()).newline();
            out.indent();
            String suffix = switch (array.getElementWidth()) {
                case 1 -> "t";
                case 2 -> "s";
                default -> "";
            };
            for (Number element : array.getArrayElements()) {
                out.literal(element.longValue()).append(suffix).newline();
            }
            out.deindent();
            out.append(".end array-data");
        } else {
            var sw = (SwitchPayload) payload;
            boolean sparse = sw.getOpcode() == Opcode.SPARSE_SWITCH_PAYLOAD
                    || sw.getOpcode() == Opcode.M_SPARSE_SWITCH_PAYLOAD;
            var elements = sw.getSwitchElements();
            if (sparse) {
                out.append(".sparse-switch").newline();
                out.indent();
                for (SwitchElement element : elements) {
                    out.literal(element.getKey()).append(" -> ");
                    printElementTarget(element.getOffset(), base, live);
                    out.newline();
                }
                out.deindent();
                out.append(".end sparse-switch");
            } else {
                out.append(".packed-switch ");
                out.literal(elements.isEmpty() ? 0 : elements.first().getKey());
                out.newline();
                out.indent();
                for (SwitchElement element : elements) {
                    printElementTarget(element.getOffset(), base, live);
                    out.newline();
                }
                out.deindent();
                out.append(".end packed-switch");
            }
        }
        if (!live) {
            out.endComment();
        }
        out.newline();
        if (!last) {
            out.newline();
        }
    }

    private void printElementTarget(int offset, int base, boolean live) {
        if (options.hasRelativeOffsets() || !live) {
            out.offset(offset);
            return;
        }
        int target = base + offset;
        var byPrefix = labels.get(target);
        if (byPrefix == null) {
            throw new IllegalStateException(
                    "No labels at address " + target);
        }
        var label = byPrefix.get(byPrefix.containsKey("sswitch_")
                ? "sswitch_" : "pswitch_");
        out.append(':');
        printLabelName(label);
    }

    private void printInstructionEntry(List<Instruction> insns, int[] address,
                                       boolean[] fallback, Label[] payloadRef,
                                       int index, int totalRegs,
                                       int fullParams, Position position) {
        Instruction insn = insns.get(index);
        boolean relative = options.hasRelativeOffsets();
        if (fallback[index]) {
            out.startComment();
            printInstruction(insn, index, address[index], true, payloadRef,
                    totalRegs, fullParams, position);
            out.endComment().newline();
            out.append("nop");
        } else {
            printInstruction(insn, index, address[index], relative,
                    payloadRef, totalRegs, fullParams, position);
        }
        out.newline();
    }

    private void printInstruction(Instruction insn, int index, int addr,
                                  boolean relative, Label[] payloadRef,
                                  int totalRegs, int fullParams,
                                  Position position) {
        Opcode op = insn.getOpcode();
        commentType = null;
        commentReference = null;
        // no answer exists for these operands: a polymorphic call site has
        // no ClassPath kind, a verification error is not a reference use
        commentSuppressed = op == Opcode.INVOKE_POLYMORPHIC
                || op == Opcode.INVOKE_POLYMORPHIC_RANGE
                || op == Opcode.THROW_VERIFICATION_ERROR
                || op == Opcode.THROW_VERIFICATION_ERROR_JUMBO;
        out.append(op.opname());
        switch (op.format()) {
            case Format10t, Format20t, Format20t_24, Format30t -> {
                out.append(' ');
                printBranch(addr, ((InstructionN0t) insn).getBranchOffset(),
                        op.isUnconditionalBranch(), relative);
            }
            case Format10x -> { /* nothing */ }
            case Format11n, Format21ih, Format21s, Format31i -> {
                var i = (InstructionN1i) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                out.literal(i.getLiteral());
            }
            case Format11p -> {
                var i = (InstructionN1p) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(" {");
                out.literal(i.getIndex());
                out.append('}');
            }
            case Format11x -> {
                var i = (InstructionN1x) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
            }
            case Format12x, Format22x, Format32x -> {
                var i = (InstructionN2x) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                printRegister(i.getRegister2(), totalRegs, fullParams);
            }
            case Format20bc, Format40sc -> {
                var i = (InstructionN0ci) insn;
                out.append(' ');
                out.append(i.getError().toString());
                out.append(", ");
                printReference(i.getReferenceType1(), i.getReference1(), op);
            }
            case Format21c, Format31c, Format41c -> {
                var i = (InstructionN1c) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
            }
            case Format21lh, Format51l -> {
                var i = (InstructionN1l) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                out.literal(i.getWideLiteral());
            }
            case Format21t, Format31t -> {
                var i = (InstructionN1t) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                if (op.hasPayload()) {
                    if (relative) {
                        out.offset(i.getBranchOffset());
                    } else {
                        out.append(':');
                        printLabelName(payloadRef[index]);
                    }
                } else {
                    printBranch(addr, i.getBranchOffset(),
                            op.isUnconditionalBranch(), relative);
                }
            }
            case Format22b, Format22s -> {
                var i = (InstructionN2i) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                printRegister(i.getRegister2(), totalRegs, fullParams);
                out.append(", ");
                out.literal(i.getLiteral());
            }
            case Format22c, Format52c -> {
                var i = (InstructionN2c) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                printRegister(i.getRegister2(), totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
            }
            case Format22t -> {
                var i = (InstructionN2t) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                printRegister(i.getRegister2(), totalRegs, fullParams);
                out.append(", ");
                printBranch(addr, i.getBranchOffset(),
                        op.isUnconditionalBranch(), relative);
            }
            case Format23x -> {
                var i = (InstructionN3x) insn;
                out.append(' ');
                printRegister(i.getRegister1(), totalRegs, fullParams);
                out.append(", ");
                printRegister(i.getRegister2(), totalRegs, fullParams);
                out.append(", ");
                printRegister(i.getRegister3(), totalRegs, fullParams);
            }
            case Format34c -> {
                var i = (InstructionNv4c) insn;
                out.append(' ');
                int[] regs = {i.getRegister1(), i.getRegister2(),
                        i.getRegister3(), i.getRegister4()};
                printRegisters(regs, i.getRegisterCount(), totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
            }
            case Format35c -> {
                var i = (InstructionNv5c) insn;
                out.append(' ');
                int[] regs = {i.getRegister1(), i.getRegister2(),
                        i.getRegister3(), i.getRegister4(), i.getRegister5()};
                printRegisters(regs, i.getRegisterCount(), totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
            }
            case Format3rc, Format5rc -> {
                var i = (InstructionNrc) insn;
                out.append(' ');
                printRegisterRange(i.getStartRegister(), i.getRegisterCount(),
                        totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
            }
            case Format45cc -> {
                var i = (InstructionNv5cc) insn;
                out.append(' ');
                int[] regs = {i.getRegister1(), i.getRegister2(),
                        i.getRegister3(), i.getRegister4(), i.getRegister5()};
                printRegisters(regs, i.getRegisterCount(), totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
                out.append(", ");
                printReference(op.getReferenceType2(), i.getReference2(), op);
            }
            case Format4rcc -> {
                var i = (InstructionNrcc) insn;
                out.append(' ');
                printRegisterRange(i.getStartRegister(), i.getRegisterCount(),
                        totalRegs, fullParams);
                out.append(", ");
                printReference(op.getReferenceType1(), i.getReference1(), op);
                out.append(", ");
                printReference(op.getReferenceType2(), i.getReference2(), op);
            }
            case FormatRaw10x, FormatWrapper20x -> {
                var i = (InstructionRaw0x) insn;
                out.append(" 0x");
                out.hex(i.getValue() & 0xffff);
            }
            case FormatRaw10c, FormatRaw20c, FormatWrapper40ci -> {
                var i = (InstructionRaw0c) insn;
                out.append(' ');
                printReference(i.getReferenceType1(), i.getReference1(), op);
            }
            // payloads
            default -> throw shouldNotReachHere();
        }
        if (options.hasResolutionInfo() && !commentSuppressed) {
            printTrailingComment(instructionComment(position, op));
        }
    }

    private void printBranch(int addr, int offset, boolean unconditional,
                             boolean relative) {
        if (relative) {
            out.offset(offset);
            return;
        }
        var byPrefix = labels.get(addr + offset);
        if (byPrefix == null) {
            throw new IllegalStateException(
                    "No label at offset " + offset + " from address " + addr);
        }
        var label = byPrefix.get(unconditional ? "goto_" : "cond_");
        out.append(':');
        printLabelName(label);
    }

    private void printRegister(int reg, int totalRegs, int fullParams) {
        int firstParam = totalRegs - fullParams;
        if (options.hasParameterRegisters() && reg >= firstParam) {
            out.append('p').append(reg - firstParam);
        } else {
            out.append('v').append(reg);
        }
    }

    private void printRegisters(int[] registers, int count, int totalRegs,
                                int fullParams) {
        out.append('{');
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                out.append(", ");
            }
            printRegister(registers[i], totalRegs, fullParams);
        }
        out.append('}');
    }

    private void printRegisterRange(int start, int count, int totalRegs,
                                    int fullParams) {
        if (count == 0) {
            out.append("{}");
            return;
        }
        int firstParam = totalRegs - fullParams;
        if (options.hasParameterRegisters() && start >= firstParam) {
            out.append("{p").append(start - firstParam);
            out.append(" .. p").append(start + count - 1 - firstParam);
            out.append('}');
        } else {
            out.append("{v").append(start);
            out.append(" .. v").append(start + count - 1);
            out.append('}');
        }
    }

    /**
     * A packed payload the parser cannot reassemble is printed as a
     * comment: one whose keys are not the int sequence
     * {@code first, first+1, ...} (possible only in a hand-built model),
     * or one whose key range {@code first + count} overflows an int,
     * which the parser rejects with an assertion.
     */
    private static boolean reassemblable(SwitchPayload payload) {
        if (payload.getOpcode() == Opcode.SPARSE_SWITCH_PAYLOAD
                || payload.getOpcode() == Opcode.M_SPARSE_SWITCH_PAYLOAD) {
            return true;
        }
        var elements = payload.getSwitchElements();
        if (elements.isEmpty()) {
            return true;
        }
        long first = elements.first().getKey();
        int index = 0;
        for (SwitchElement element : elements) {
            if (element.getKey() != (int) (first + index)) {
                return false;
            }
            index++;
        }
        return first + elements.size() <= Integer.MAX_VALUE;
    }

    private boolean collectElementLabels(SwitchPayload payload, int base, IntSet anchors) {
        boolean sparse = payload.getOpcode() == Opcode.SPARSE_SWITCH_PAYLOAD
                || payload.getOpcode() == Opcode.M_SPARSE_SWITCH_PAYLOAD;
        for (SwitchElement element : payload.getSwitchElements()) {
            int target = base + element.getOffset();
            if (!anchors.contains(target)) {
                return false;
            }
            internLabel(target, sparse ? "sswitch_" : "pswitch_");
        }
        return true;
    }

    private static int findPayload(List<Instruction> insns, int[] address,
                                   boolean[] isPayload, int target) {
        int index = Arrays.binarySearch(address, 0, insns.size(), target);
        if (index < 0) {
            return -1;
        }
        if (isPayload[index]) {
            return index;
        }
        // a payload reference may aim at the alignment nop in front of the
        // payload; re-aim it at the payload itself
        if (isNop(insns.get(index)) && index + 1 < insns.size()
                && isPayload[index + 1]) {
            return index + 1;
        }
        return -1;
    }

    private static boolean isNop(Instruction insn) {
        return insn instanceof InstructionN0x n
                && n.getOpcode() == Opcode.NOP;
    }

    private static int branchOffset(Instruction insn) {
        if (insn instanceof InstructionN0t n) {
            return n.getBranchOffset();
        }
        if (insn instanceof InstructionN1t n) {
            return n.getBranchOffset();
        }
        return ((InstructionN2t) insn).getBranchOffset();
    }

    private static String payloadPrefix(Opcode op) {
        if (op == Opcode.FILL_ARRAY_DATA) {
            return "array_";
        }
        if (op == Opcode.SPARSE_SWITCH || op == Opcode.M_SPARSE_SWITCH) {
            return "sswitch_data_";
        }
        return "pswitch_data_";
    }

    private static int floorAnchor(IntSet anchors, int address) {
        var floor = floor(anchors, address);
        return floor == null ? 0 : floor;
    }

    // TODO?: move to IntSet
    private static Integer floor(IntSet set, int value) {
        var index = set.indexOf(value);
        if (index >= 0) {
            return set.at(index);
        }
        index = ~index - 1;
        if (index < 0) {
            return null;
        }
        return set.at(index);
    }

    private static Integer ceiling(IntSet set, int value) {
        var index = set.indexOf(value);
        if (index >= 0) {
            return set.at(index);
        }
        index = ~index;
        if (index >= set.size()) {
            return null;
        }
        return set.at(index);
    }

    // ------------------------- information options -------------------------

    /**
     * Runs the body analysis when an information option needs it; a failure
     * is kept for the {@code analysisInfo} comment, so the code still prints
     * and the resolution comments fall back to the static answers.
     */
    private void analyze(MethodDef method) {
        analysis = null;
        analysisFailure = null;
        if (!options.hasAnalysisInfo() && !options.hasResolutionInfo()) {
            return;
        }
        TypeId declaringClass = classContext != null ? classContext : UNKNOWN_CLASS;
        var analysisOptions = AnalysisOptions.defaultOptions()
                .withVerification(options.hasAnalysisVerification());
        try {
            analysis = AnalyzedMethod.analyze(resolver, declaringClass,
                    method, analysisOptions);
        } catch (RuntimeException e) {
            analysisFailure = e;
        }
    }

    private void printAnalysisFailure(Throwable failure) {
        if (failure == null) {
            return;
        }
        var buffer = new StringWriter();
        failure.printStackTrace(new PrintWriter(buffer));
        boolean first = true;
        for (String line : buffer.toString().split("\n")) {
            String text = line.strip();
            if (text.isEmpty()) {
                continue;
            }
            if (first) {
                out.append("#@analysis-failed ").append(text).newline();
                first = false;
            } else {
                out.append("#@ ").append(text).newline();
            }
        }
    }

    private void printStateLine(String key, RegisterLine line, boolean instance) {
        out.append("#@").append(key).append(' ');
        out.append(line.describe());
        if (instance && !line.isThisInitialized()) {
            out.append(" this-uninitialized");
        }
        out.newline();
    }

    /**
     * The {@code #@locks} summary of a register line: {@code v0:1 v2:0~1}
     * for every register holding or possibly holding a monitor; omitted
     * when there is none.
     */
    private void printLocks(RegisterLine line) {
        var sj = new StringJoiner(" ");
        for (int i = 0; i < line.registerCount(); i++) {
            var lock = line.at(i).lock();
            if (lock.isPossiblyHeld()) {
                sj.add("v" + i + ":" + lock);
            }
        }
        var text = sj.toString();
        if (!text.isEmpty()) {
            out.append("#@locks ").append(text).newline();
        }
    }

    /**
     * The block of one instruction: the register states, the consumed result
     * or exception, the touched registers and the flags; a line that carries
     * nothing is omitted, {@code #@flags} is unconditional.
     */
    private void printAnalysisBlock(Position position, boolean instance) {
        if (position.isBranchTarget()) {
            printStateLine("before", position.before(), instance);
        }
        Opcode op = position.opcode();
        boolean completesNormally = op != Opcode.THROW && !op.isReturn();
        if (completesNormally) {
            printStateLine("after", position.after(), instance);
        }
        printLocks(completesNormally ? position.after() : position.before());
        if (position.resultType() != null) {
            out.append("#@result ").append(position.resultType().getDescriptor()).newline();
        }
        if (position.exceptionType() != null) {
            out.append("#@exception ").append(position.exceptionType().toString()).newline();
        }
        if (!position.inputs().isEmpty()) {
            out.append("#@reads ").append(Formatter.registers(position.inputs())).newline();
        }
        if (!position.outputs().isEmpty()) {
            out.append("#@writes ").append(Formatter.registers(position.outputs())).newline();
        }
        if (!position.predecessors().isEmpty()) {
            out.append("#@predecessors ").append(position.predecessors().toString()).newline();
        }
        if (!position.successors().isEmpty()) {
            out.append("#@successors ").append(position.successors().toString()).newline();
        }
        out.append("#@flags ").append(runtimeFlags(position)).newline();
    }

    private static String runtimeFlags(Position position) {
        String state;
        if (position.isRuntimeReachable()) {
            state = "runtime-reachable";
        } else if (position.isStructurallyReachable()) {
            state = "structurally-reachable";
        } else {
            state = "unreachable";
        }
        if (position.isAlwaysThrowing()) {
            state += ", always-throws";
        }
        if (position.isStrictNop()) {
            return state + ", strict-nop";
        } else if (position.isLossyNop()) {
            return state + ", lossy-nop";
        }
        return state;
    }

    private void printTrailingComment(String comment) {
        if (comment == null) {
            return;
        }
        out.append("    #@").append(comment);
    }

    private String instructionComment(Position position, Opcode op) {
        if (commentType == null) {
            return null;
        }
        return switch (commentType) {
            case FIELD -> fieldComment((FieldId) commentReference, fieldAccess(op));
            case METHOD -> methodComment((MethodId) commentReference, op, position);
            case METHOD_HANDLE -> handleComment((MethodHandleId) commentReference, true);
            default -> null;
        };
    }

    /**
     * The resolution comment of a constant reference value (an annotation or
     * field value): a constant has no referrer, so the no-check overloads
     * answer.
     */
    private String constantComment(EncodedValue value) {
        if (!options.hasResolutionInfo()) {
            return null;
        }
        if (value instanceof EncodedField field) {
            return fieldResult(classPath.resolveField(field.getValue()));
        }
        if (value instanceof EncodedEnum constant) {
            return fieldResult(classPath.resolveField(constant.getValue()));
        }
        if (value instanceof EncodedMethod method) {
            return methodResult(classPath.resolveMethod(method.getValue()));
        }
        if (value instanceof EncodedMethodHandle handle) {
            return handleComment(handle.getValue(), false);
        }
        return null;
    }

    /**
     * The resolution comment of a call-site extra argument: only a method
     * handle carries a reference, and the containing class is the referrer.
     */
    private String callSiteComment(EncodedValue value) {
        if (!options.hasResolutionInfo() || !(value instanceof EncodedMethodHandle handle)) {
            return null;
        }
        return handleComment(handle.getValue(), true);
    }

    private String fieldComment(FieldId id, FieldAccess access) {
        return fieldResult(classContext == null
                ? classPath.resolveField(id)
                : classPath.resolveField(id, classContext, access));
    }

    private String methodComment(MethodId id, Opcode op, Position position) {
        InvokeType type = classContext == null ? null : invokeType(op);
        if (type == null) {
            return methodResult(classPath.resolveMethod(id));
        }
        return switch (type) {
            case STATIC -> dispatchResult(classPath.findNonVirtualMethodToCall(
                    id, classContext, InvokeType.STATIC));
            case DIRECT -> dispatchResult(classPath.findNonVirtualMethodToCall(
                    id, classContext, InvokeType.DIRECT));
            case SUPER -> dispatchResult(classPath.findSuperMethodToCall(
                    id, classContext, classContext));
            case VIRTUAL, INTERFACE -> virtualComment(id, type, position);
        };
    }

    private String virtualComment(MethodId id, InvokeType type, Position position) {
        Result<ResolvedMethod> resolution = classPath.resolveMethod(id, classContext, type);
        if (resolution.value() == null) {
            return failureComment(resolution);
        }
        TypeId receiver = dispatchReceiver(position, id, resolution.value());
        if (receiver == null) {
            return methodResult(resolution);
        }
        return dispatchResult(classPath.findMethodToCall(id, classContext, type, receiver));
    }

    /**
     * The runtime class of a virtual call receiver: the exact analysis type
     * of the receiver register, or the declarer of a final method, or an
     * array reference class. Null means only the static answer is known.
     */
    private static TypeId dispatchReceiver(Position position, MethodId id,
                                           ResolvedMethod resolved) {
        if (position != null) {
            int reg = receiverRegister(position.instruction());
            if (reg >= 0 && reg < position.getRegisterCount()) {
                Register value = position.before().at(reg);
                if (value.isExactRef() && value.getTypeInfo() != null) {
                    return value.getTypeInfo().exactType();
                }
            }
        }
        if ((resolved.declaringClass().getAccessFlags() & ACC_FINAL) != 0) {
            return resolved.declaringClass().getType();
        }
        if (id.getDeclaringClass().isArray()) {
            return id.getDeclaringClass();
        }
        return null;
    }

    private static int receiverRegister(Instruction insn) {
        if (insn instanceof InstructionNv5c five) {
            return five.getRegister1();
        }
        if (insn instanceof InstructionNv4c four) {
            return four.getRegister1();
        }
        if (insn instanceof InstructionNrc range) {
            return range.getStartRegister();
        }
        if (insn instanceof InstructionNrcc range) {
            return range.getStartRegister();
        }
        return -1;
    }

    /**
     * The resolution answer of a method handle. A handle is resolved, never
     * dispatched, with the one exception ART makes: the super branch of a
     * non-private invoke-direct handle asks the super path.
     */
    private String handleComment(MethodHandleId handle, boolean withChecks) {
        TypeId referrer = withChecks ? classContext : null;
        MemberId member = handle.getMember();
        return switch (handle.getHandleType()) {
            case STATIC_GET -> fieldHandleComment((FieldId) member, referrer, FieldAccess.SGET);
            case STATIC_PUT -> fieldHandleComment((FieldId) member, referrer, FieldAccess.SPUT);
            case INSTANCE_GET -> fieldHandleComment((FieldId) member, referrer, FieldAccess.IGET);
            case INSTANCE_PUT -> fieldHandleComment((FieldId) member, referrer, FieldAccess.IPUT);
            case INVOKE_STATIC ->
                    methodHandleComment((MethodId) member, referrer, InvokeType.STATIC);
            case INVOKE_CONSTRUCTOR ->
                    methodHandleComment((MethodId) member, referrer, InvokeType.DIRECT);
            case INVOKE_INSTANCE ->
                    methodHandleComment((MethodId) member, referrer, InvokeType.VIRTUAL);
            case INVOKE_INTERFACE ->
                    methodHandleComment((MethodId) member, referrer, InvokeType.INTERFACE);
            case INVOKE_DIRECT -> directHandleComment((MethodId) member, referrer);
        };
    }

    private String fieldHandleComment(FieldId id, TypeId referrer, FieldAccess access) {
        return fieldResult(referrer == null
                ? classPath.resolveField(id)
                : classPath.resolveField(id, referrer, access));
    }

    private String methodHandleComment(MethodId id, TypeId referrer, InvokeType type) {
        return methodResult(referrer == null
                ? classPath.resolveMethod(id)
                : classPath.resolveMethod(id, referrer, type));
    }

    /**
     * The two-step invoke-direct handle: ART resolves the method once
     * ignoring the invocation type to detect private, then resolves a
     * private method with the direct checks or dispatches a non-private
     * superclass method through the super path.
     */
    private String directHandleComment(MethodId id, TypeId referrer) {
        Result<ResolvedMethod> first = classPath.resolveMethod(id);
        if (first.value() == null) {
            return failureComment(first);
        }
        boolean isPrivate = (first.value().method().getAccessFlags() & ACC_PRIVATE) != 0;
        if (referrer == null) {
            return "resolve " + first.value()
                    + (isPrivate ? " direct" : " super") + inexact(first);
        }
        if (isPrivate) {
            Result<ResolvedMethod> second = classPath.resolveMethod(
                    id, referrer, InvokeType.DIRECT);
            if (second.value() == null) {
                return failureComment(second);
            }
            return "resolve " + second.value() + " direct" + inexact(second);
        }
        return dispatchResult(classPath.findSuperMethodToCall(
                id, referrer, referrer), " super");
    }

    private static String fieldResult(Result<ResolvedField> result) {
        if (result.value() == null) {
            return failureComment(result);
        }
        return "resolve " + result.value() + inexact(result);
    }

    private static String methodResult(Result<ResolvedMethod> result) {
        if (result.value() == null) {
            return failureComment(result);
        }
        return "resolve " + result.value() + inexact(result);
    }

    private static String dispatchResult(Result<CallTarget> result) {
        return dispatchResult(result, "");
    }

    private static String dispatchResult(Result<CallTarget> result, String word) {
        if (result.value() == null) {
            return failureComment(result);
        }
        CallTarget target = result.value();
        if (target instanceof CallTarget.DefaultConflict conflict) {
            var text = new StringJoiner(", ", "dispatch default-conflict ",
                    " throws IncompatibleClassChangeError");
            for (ResolvedMethod candidate : conflict.candidates()) {
                text.add(candidate.toString());
            }
            return text.toString();
        }
        var method = (CallTarget.Method) target;
        var text = new StringBuilder("dispatch ");
        text.append(method.method()).append(word);
        if (method.invocationError() != InvocationError.NONE) {
            text.append(" throws ").append(exceptionName(method.invocationError()));
        }
        if (!result.exact()) {
            text.append(" inexact");
        }
        return text.toString();
    }

    private static String failureComment(Result<?> result) {
        if (result.error() == ResolutionError.NONE) {
            return "unknown";
        }
        String text = "error " + exceptionName(result.error());
        String message = result.message();
        return message == null ? text : text + ": " + message;
    }

    private static String inexact(Result<?> result) {
        return result.exact() ? "" : " inexact";
    }

    private static String exceptionName(ResolutionError error) {
        return switch (error) {
            case NONE -> throw new IllegalArgumentException("No resolution error");
            case NO_SUCH_METHOD -> "NoSuchMethodError";
            case NO_SUCH_FIELD -> "NoSuchFieldError";
            case NO_CLASS_DEF -> "NoClassDefFoundError";
            case ILLEGAL_ACCESS -> "IllegalAccessError";
            case INCOMPATIBLE_CLASS_CHANGE -> "IncompatibleClassChangeError";
        };
    }

    private static String exceptionName(InvocationError error) {
        return switch (error) {
            case NONE -> throw new IllegalArgumentException("No invocation error");
            case ABSTRACT_METHOD -> "AbstractMethodError";
            case ILLEGAL_ACCESS -> "IllegalAccessError";
            case INCOMPATIBLE_CLASS_CHANGE -> "IncompatibleClassChangeError";
        };
    }

    private static FieldAccess fieldAccess(Opcode op) {
        String name = op.opname();
        boolean isStatic = name.startsWith("sget") || name.startsWith("sput");
        boolean isPut = name.startsWith("iput") || name.startsWith("sput");
        if (isStatic) {
            return isPut ? FieldAccess.SPUT : FieldAccess.SGET;
        }
        return isPut ? FieldAccess.IPUT : FieldAccess.IGET;
    }

    /**
     * The invoke kind of an instruction, or null for a non-invoke opcode and
     * for the kinds the class path does not model.
     */
    private static InvokeType invokeType(Opcode op) {
        String name = op.opname();
        if (name.startsWith("m-")) {
            name = name.substring(2);
        }
        if (!name.startsWith("invoke-")) {
            return null;
        }
        if (name.startsWith("invoke-static")) {
            return InvokeType.STATIC;
        }
        if (name.startsWith("invoke-direct")) {
            return InvokeType.DIRECT;
        }
        if (name.startsWith("invoke-virtual")) {
            return InvokeType.VIRTUAL;
        }
        if (name.startsWith("invoke-interface")) {
            return InvokeType.INTERFACE;
        }
        if (name.startsWith("invoke-super")) {
            return InvokeType.SUPER;
        }
        return null;
    }
}
