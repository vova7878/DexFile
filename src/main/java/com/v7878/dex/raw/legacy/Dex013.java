package com.v7878.dex.raw.legacy;

import static com.v7878.dex.DexConstants.M5_DBG_ADVANCE_LINE;
import static com.v7878.dex.DexConstants.M5_DBG_ADVANCE_PC;
import static com.v7878.dex.DexConstants.M5_DBG_END_LOCAL;
import static com.v7878.dex.DexConstants.M5_DBG_END_SEQUENCE;
import static com.v7878.dex.DexConstants.M5_DBG_FIRST_SPECIAL;
import static com.v7878.dex.DexConstants.M5_DBG_LINE_BASE;
import static com.v7878.dex.DexConstants.M5_DBG_LINE_RANGE;
import static com.v7878.dex.DexConstants.M5_DBG_RESTART_LOCAL;
import static com.v7878.dex.DexConstants.M5_DBG_SET_EPILOGUE_BEGIN;
import static com.v7878.dex.DexConstants.M5_DBG_SET_FILE;
import static com.v7878.dex.DexConstants.M5_DBG_SET_PROLOGUE_END;
import static com.v7878.dex.DexConstants.M5_DBG_START_LOCAL;
import static com.v7878.dex.DexConstants.M5_DBG_START_LOCAL_EXTENDED;
import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.DexOffsets.M5_INSTANCE_FIELD_DEF_ALIGNMENT;
import static com.v7878.dex.DexOffsets.M5_STATIC_FIELD_DEF_ALIGNMENT;
import static com.v7878.dex.util.Ids.CLASS;
import static com.v7878.dex.util.Ids.STRING;
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

import com.v7878.collections.IntMap;
import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TryBlock;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.immutable.bytecode.Instruction;
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
import com.v7878.dex.immutable.value.EncodedFloat;
import com.v7878.dex.immutable.value.EncodedInt;
import com.v7878.dex.immutable.value.EncodedLong;
import com.v7878.dex.immutable.value.EncodedNull;
import com.v7878.dex.immutable.value.EncodedShort;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.raw.DebugInfo;
import com.v7878.dex.raw.DexReader;
import com.v7878.dex.raw.DexReader.AnnotationDirectory;
import com.v7878.dex.raw.DexReader.CodeItem;
import com.v7878.dex.raw.InstructionReader;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.TryBlocksMerger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class Dex013 {
    public static List<TypeId> readTypeList(DexReader reader, int offset) {
        var in = reader.dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<TypeId>(size);
        for (int i = 0; i < size; i++) {
            out.add(i, reader.getType(in.readSmallUInt()));
        }
        return Collections.unmodifiableList(out);
    }

    public static String readString(DexReader reader, int index, int offset) {
        var in = reader.mainAt(offset);
        int data_offset = in.readSmallUInt();
        int length = in.readSmallUInt();
        return reader.dataAt(data_offset).readMUTF8(length);
    }

    public static FieldId readFieldId(DexReader reader, int index, int offset) {
        var in = reader.mainAt(offset);
        var declaring_class = reader.getType(in.readSmallUInt());
        var name = reader.getString(in.readSmallUInt());
        var descriptor = reader.getString(in.readSmallUInt());
        return FieldId.of(declaring_class, name, TypeId.of(descriptor));
    }

    public static MethodId readMethodId(DexReader reader, int index, int offset) {
        var in = reader.mainAt(offset);
        var declaring_class = reader.getType(in.readSmallUInt());
        var name = reader.getString(in.readSmallUInt());
        var descriptor = reader.getString(in.readSmallUInt());
        return MethodId.of(declaring_class, name, ProtoId.of(descriptor));
    }

    private static List<FieldDef> readFieldDefList(DexReader reader, int offset, boolean _static,
                                                   IntMap<NavigableSet<Annotation>> annotations) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var list = new ArrayList<FieldDef>(count);
        in.alignPosition(_static ? M5_STATIC_FIELD_DEF_ALIGNMENT : M5_INSTANCE_FIELD_DEF_ALIGNMENT);
        for (int i = 0; i < count; i++) {
            int field_idx = in.readSmallUInt();
            var fid = reader.getField(field_idx);
            int access_flags = in.readInt();

            EncodedValue value = null;
            if (_static) {
                var lvalue = in.readLong();
                var type = fid.getType();
                var shorty = type.getShorty();
                if (reader.options().hasFieldValues()) {
                    value = switch (shorty) {
                        case 'Z' -> EncodedBoolean.of(lvalue != 0);
                        case 'B' -> EncodedByte.of((byte) lvalue);
                        case 'S' -> EncodedShort.of((short) lvalue);
                        case 'C' -> EncodedChar.of((char) lvalue);
                        case 'I' -> EncodedInt.of((int) lvalue);
                        case 'F' -> EncodedFloat.of(Float.intBitsToFloat((int) lvalue));
                        case 'J' -> EncodedLong.of(lvalue);
                        case 'D' -> EncodedDouble.of(Double.longBitsToDouble(lvalue));
                        case 'L' -> {
                            if (lvalue == -1) {
                                yield EncodedNull.INSTANCE;
                            }
                            if (STRING.equals(type)) {
                                yield EncodedString.of(reader.getString((int) lvalue));
                            }
                            if (CLASS.equals(type)) {
                                yield EncodedType.of(reader.getType((int) lvalue));
                            }
                            throw new IllegalStateException(String.format(
                                    "Unsupported field type %s with non-null value 0x%016X", type, lvalue));
                        }
                        default -> throw invalidShorty(shorty);
                    };
                }
            }

            list.add(FieldDef.of(fid.getName(), fid.getType(), access_flags, 0, value,
                    annotations.get(field_idx, Collections.emptyNavigableSet())));
        }
        return list;
    }

    private record TryItem(int start, int end, int handler, TypeId exception)
            implements TryBlocksMerger.TryItem {
    }

    private static NavigableSet<TryBlock> readTries(DexReader reader, int offset) {
        var in = reader.dataAt(offset);

        var size = in.readSmallUInt();
        var items = new ArrayList<TryItem>(size);
        for (int i = 0; i < size; i++) {
            int start = in.readSmallUInt();
            int end = in.readSmallUInt();
            int handler = in.readSmallUInt();
            int exception_idx = in.readSmallUIntWithM1();

            var exception = exception_idx == NO_INDEX ?
                    null : reader.getType(exception_idx);
            items.add(i, new TryItem(start, end, handler, exception));
        }

        return TryBlocksMerger.mergeTryItems(items);
    }

    private static List<Instruction> readInstructions(DexReader reader, int offset) {
        var in = reader.dataAt(offset);
        int insns_count = in.readSmallUInt();
        return InstructionReader.readArray(reader, in, insns_count);
    }

    public static List<DebugItem> readDebugItemArray(
            DexReader reader, RandomInput in, String source_file, int line_start) {
        var out = new ArrayList<DebugItem>();
        if (source_file != null) {
            out.add(SetFile.of(source_file));
        }
        int[] address = {0, 0};
        Runnable emit_address = () -> {
            if (address[0] != address[1]) {
                out.add(AdvancePC.of(address[0] - address[1]));
                address[1] = address[0];
            }
        };
        int[] line = {line_start, -1};
        Runnable emit_line = () -> {
            if (line[0] != line[1]) {
                out.add(LineNumber.of(line[0]));
                line[1] = line[0];
            }
        };

        int opcode;
        do {
            opcode = in.readUByte();
        } while (switch (opcode) {
            case M5_DBG_END_SEQUENCE -> false;
            case M5_DBG_ADVANCE_PC -> {
                address[0] += in.readULeb128();
                yield true;
            }
            case M5_DBG_ADVANCE_LINE -> {
                line[0] += in.readSLeb128();
                yield true;
            }
            case M5_DBG_START_LOCAL, M5_DBG_START_LOCAL_EXTENDED -> {
                int reg = in.readULeb128();
                int name_idx = in.readULeb128() - 1;
                String name = name_idx == NO_INDEX ?
                        null : reader.getString(name_idx);
                int descriptor_idx = in.readULeb128() - 1;
                String descriptor = descriptor_idx == NO_INDEX ?
                        null : reader.getString(descriptor_idx);
                // Note: this version had an additional register type value
                // that meant untyped null
                var type = descriptor == null || "<null>".equals(descriptor) ?
                        null : TypeId.of(descriptor);
                int signature_idx = opcode == M5_DBG_START_LOCAL ?
                        NO_INDEX : in.readULeb128() - 1;
                String signature = signature_idx == NO_INDEX ?
                        null : reader.getString(signature_idx);
                emit_address.run();
                out.add(StartLocal.of(reg, name, type, signature));
                yield true;
            }
            case M5_DBG_END_LOCAL -> {
                int reg = in.readULeb128();
                emit_address.run();
                out.add(EndLocal.of(reg));
                yield true;
            }
            case M5_DBG_RESTART_LOCAL -> {
                int reg = in.readULeb128();
                emit_address.run();
                out.add(RestartLocal.of(reg));
                yield true;
            }
            case M5_DBG_SET_PROLOGUE_END -> {
                emit_address.run();
                out.add(SetPrologueEnd.INSTANCE);
                yield true;
            }
            case M5_DBG_SET_EPILOGUE_BEGIN -> {
                emit_address.run();
                out.add(SetEpilogueBegin.INSTANCE);
                yield true;
            }
            case M5_DBG_SET_FILE -> {
                int name_idx = in.readULeb128() - 1;
                String name = name_idx == NO_INDEX ?
                        null : reader.getString(name_idx);
                emit_address.run();
                out.add(SetFile.of(name));
                yield true;
            }
            default -> {
                int adjopcode = opcode - M5_DBG_FIRST_SPECIAL;
                address[0] += adjopcode / M5_DBG_LINE_RANGE;
                line[0] += M5_DBG_LINE_BASE + (adjopcode % M5_DBG_LINE_RANGE);
                emit_address.run();
                emit_line.run();
                yield true;
            }
        });
        return Collections.unmodifiableList(out);
    }

    public static DebugInfo readDebugInfo(DexReader reader, MethodId mid,
                                          String source_file, int offset) {
        var in = reader.dataAt(offset);
        int line_start = in.readULeb128();
        List<String> parameter_names;
        {
            int parameters_size = mid.getParameterTypes().size();
            parameter_names = new ArrayList<>(parameters_size);
            for (int i = 0; i < parameters_size; i++) {
                int name_idx = in.readULeb128() - 1;
                parameter_names.add(name_idx == NO_INDEX ?
                        null : reader.getString(name_idx));
            }
            parameter_names = Collections.unmodifiableList(parameter_names);
        }
        var items = readDebugItemArray(reader, in, source_file, line_start);
        return new DebugInfo(parameter_names, items);
    }

    public static CodeItem readCodeItem(DexReader reader, MethodId mid, int offset) {
        var in = reader.dataAt(offset);

        var registers_size = in.readUShort();
        var ins_size = in.readUShort();
        var outs_size = in.readUShort();
        in.readUShort(); // unused

        var source_file_idx = in.readSmallUIntWithM1();
        var insns_off = in.readSmallUInt();
        var exceptions_off = in.readSmallUInt();
        var debug_info_off = in.readSmallUInt();

        var instructions = readInstructions(reader, insns_off);
        NavigableSet<TryBlock> tries = exceptions_off == NO_OFFSET ?
                Collections.emptyNavigableSet() : readTries(reader, exceptions_off);

        DebugInfo debug_info;
        if (reader.options().hasDebugInfo() && debug_info_off != NO_OFFSET) {
            var source_file = source_file_idx == NO_INDEX ?
                    null : reader.getString(source_file_idx);
            debug_info = readDebugInfo(reader, mid, source_file, debug_info_off);
        } else {
            debug_info = null;
        }

        return new CodeItem(registers_size, ins_size,
                outs_size, debug_info, instructions, tries);
    }

    private static List<MethodDef> readMethodDefList(
            DexReader reader, int offset, @SuppressWarnings("unused") boolean direct,
            IntMap<NavigableSet<Annotation>> method_annotations,
            IntMap<List<NavigableSet<Annotation>>> parameter_annotations) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var list = new ArrayList<MethodDef>(count);
        for (int i = 0; i < count; i++) {
            int method_idx = in.readSmallUInt();
            var mid = reader.getMethod(method_idx);
            int access_flags = in.readInt();
            int throws_list_off = in.readSmallUInt();
            int code_off = in.readSmallUInt();

            var annotations = new ArrayList<>(method_annotations.get(
                    method_idx, Collections.emptyNavigableSet()));
            if (reader.options().hasAnnotations() && throws_list_off != NO_OFFSET) {
                annotations.add(Annotation.Throws(reader.getTypeList(throws_list_off)));
            }

            var parameter_types = mid.getParameterTypes();
            var parameter_annotation_list = parameter_annotations.get(method_idx, List.of());
            var parameters = new ArrayList<Parameter>(parameter_types.size());
            for (int j = 0; j < parameter_types.size(); j++) {
                var parameter_annotation_set = j < parameter_annotation_list.size() ?
                        parameter_annotation_list.get(j) : Collections.<Annotation>emptyNavigableSet();
                parameters.add(Parameter.of(parameter_types.get(j), null, parameter_annotation_set));
            }

            var code = code_off == NO_OFFSET ? null :
                    (!reader.options().hasMethodBodies() ?
                            CodeItem.EMPTY : readCodeItem(reader, mid, code_off));
            var debug_info = code == null ? null : code.debug_info();
            list.add(MethodDef.of(mid.getName(), mid.getReturnType(), parameters,
                    access_flags, 0, DexReader.toImplementation(code, debug_info), annotations));
        }
        return list;
    }

    public static ClassDef readClassDef(DexReader reader, int index, int offset) {
        var in = reader.mainAt(offset);

        int class_idx = in.readSmallUInt();
        int access_flags = in.readInt();
        int superclass_idx = in.readSmallUIntWithM1();
        int interfaces_off = in.readSmallUInt();

        int static_fields_off = in.readSmallUInt();
        int instance_fields_off = in.readSmallUInt();
        int direct_methods_off = in.readSmallUInt();
        int virtual_methods_off = in.readSmallUInt();

        int annotations_off = in.readSmallUInt();

        var annotations = annotations_off == NO_OFFSET || !reader.options().hasAnnotations() ?
                AnnotationDirectory.EMPTY : reader.getAnnotationDirectory(annotations_off);

        TypeId clazz = reader.getType(class_idx);
        TypeId superclass = superclass_idx == NO_INDEX ?
                null : reader.getType(superclass_idx);
        List<TypeId> interfaces = interfaces_off == NO_OFFSET ?
                List.of() : reader.getTypeList(interfaces_off);

        List<FieldDef> static_fields = static_fields_off == NO_OFFSET ?
                null : readFieldDefList(reader, static_fields_off, true,
                annotations.field_annotations());
        List<FieldDef> instance_fields = instance_fields_off == NO_OFFSET ?
                null : readFieldDefList(reader, instance_fields_off, false,
                annotations.field_annotations());
        List<MethodDef> direct_methods = direct_methods_off == NO_OFFSET ?
                null : readMethodDefList(reader, direct_methods_off, true,
                annotations.method_annotations(), annotations.parameter_annotations());
        List<MethodDef> virtual_methods = virtual_methods_off == NO_OFFSET ?
                null : readMethodDefList(reader, virtual_methods_off, false,
                annotations.method_annotations(), annotations.parameter_annotations());

        return ClassDef.raw(clazz, access_flags, superclass, interfaces, null,
                MemberUtils.mergeFields(static_fields, instance_fields),
                MemberUtils.mergeMethods(direct_methods, virtual_methods),
                annotations.class_annotations());
    }

    /**
     * The legacy directory holds four offsets - the class set, the field
     * list, the method list and the parameter list - rather than sizes with
     * inline entries.
     */
    public static AnnotationDirectory readAnnotationDirectory(DexReader reader, int offset) {
        var in = reader.dataAt(offset);
        int class_annotations_off = in.readSmallUInt();
        int field_annotations_off = in.readSmallUInt();
        int method_annotations_off = in.readSmallUInt();
        int parameter_annotations_off = in.readSmallUInt();

        var class_annotations = class_annotations_off == NO_OFFSET ?
                Collections.<Annotation>emptyNavigableSet() :
                reader.getAnnotationSet(class_annotations_off);
        var field_annotations = field_annotations_off == NO_OFFSET ?
                IntMap.<NavigableSet<Annotation>>empty() :
                readAnnotationSetMap(reader, field_annotations_off);
        var method_annotations = method_annotations_off == NO_OFFSET ?
                IntMap.<NavigableSet<Annotation>>empty() :
                readAnnotationSetMap(reader, method_annotations_off);
        var parameter_annotations = parameter_annotations_off == NO_OFFSET ?
                IntMap.<List<NavigableSet<Annotation>>>empty() :
                readParameterAnnotationMap(reader, parameter_annotations_off);
        return new AnnotationDirectory(class_annotations, field_annotations,
                method_annotations, parameter_annotations);
    }

    private static IntMap<NavigableSet<Annotation>> readAnnotationSetMap(
            DexReader reader, int offset) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var out = new IntMap<NavigableSet<Annotation>>(count);
        for (int i = 0; i < count; i++) {
            int member_idx = in.readSmallUInt();
            out.put(member_idx, reader.getAnnotationSet(in.readSmallUInt()));
        }
        return out;
    }

    private static IntMap<List<NavigableSet<Annotation>>> readParameterAnnotationMap(
            DexReader reader, int offset) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var out = new IntMap<List<NavigableSet<Annotation>>>(count);
        for (int i = 0; i < count; i++) {
            int method_idx = in.readSmallUInt();
            int set_list_off = in.readSmallUInt();
            out.put(method_idx, set_list_off == NO_OFFSET ?
                    List.of() : reader.getAnnotationSetList(set_list_off));
        }
        return out;
    }

    public static Annotation readAnnotation(DexReader reader, int offset) {
        var in = reader.dataAt(offset);
        var visibility = AnnotationVisibility.of(in.readUByte());
        return Annotation.of(visibility, readEncodedAnnotation(reader, in));
    }

    private static EncodedAnnotation readEncodedAnnotation(DexReader reader, RandomInput in) {
        TypeId type = reader.getType(in.readSmallULeb128());
        int size = in.readSmallULeb128();
        var elements = new TreeSet<AnnotationElement>();
        for (int i = 0; i < size; i++) {
            String name = reader.getString(in.readSmallULeb128());
            MemberUtils.addAnnotationElement(elements,
                    AnnotationElement.of(name, readValue(reader, in)));
        }
        return EncodedAnnotation.raw(type, Collections.unmodifiableNavigableSet(elements));
    }

    /**
     * The legacy value encoding carries no type tags: a literal is reported
     * as the integer of its encoded width, so float and double elements
     * surface as their IEEE bit patterns and char and boolean elements as
     * plain integers.
     */
    private static EncodedValue makeLiteralValue(int size, long value) {
        return switch (size) {
            case 1 -> EncodedByte.of((byte) value);
            case 2 -> EncodedShort.of((short) value);
            case 3, 4 -> EncodedInt.of((int) value);
            default -> EncodedLong.of(value);
        };
    }

    private static EncodedValue readValue(DexReader reader, RandomInput in) {
        int tag = in.readUByte();
        if ((tag & 0x80) != 0) {
            int value = ((tag & 0x7f) << 25) >> 25;
            return makeLiteralValue(1, value);
        }
        if (tag > 0x30 && tag <= 0x38) {
            int size = tag - 0x30;
            long value = 0;
            for (int i = 0; i < size; i++) {
                value |= (long) in.readUByte() << (i * 8);
            }
            int shift = 64 - size * 8;
            return makeLiteralValue(size, value << shift >> shift);
        }
        return switch (tag) {
            case 's' -> EncodedString.of(reader.getString(in.readSmallULeb128()));
            case 't' -> EncodedType.of(reader.getType(in.readSmallULeb128()));
            case 'e' -> EncodedEnum.of(reader.getField(in.readSmallULeb128()));
            case '[' -> {
                int size = in.readSmallULeb128();
                var values = new ArrayList<EncodedValue>(size);
                for (int i = 0; i < size; i++) {
                    values.add(readValue(reader, in));
                }
                yield EncodedArray.raw(Collections.unmodifiableList(values));
            }
            case '@' -> readEncodedAnnotation(reader, in);
            default -> throw new IllegalStateException(String.format(
                    "Unknown legacy annotation value tag 0x%02X at position %d",
                    tag, in.position() - 1));
        };
    }
}
