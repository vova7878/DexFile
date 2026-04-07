package com.v7878.dex.raw.legacy;

import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.DexOffsets.M3_INSTANCE_FIELD_DEF_ALIGNMENT;
import static com.v7878.dex.DexOffsets.M3_STATIC_FIELD_DEF_ALIGNMENT;
import static com.v7878.dex.util.Ids.CLASS;
import static com.v7878.dex.util.Ids.STRING;
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

import com.v7878.dex.immutable.Annotation;
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
import com.v7878.dex.immutable.debug.DebugItem;
import com.v7878.dex.immutable.debug.EndLocal;
import com.v7878.dex.immutable.debug.LineNumber;
import com.v7878.dex.immutable.debug.SetFile;
import com.v7878.dex.immutable.debug.StartLocal;
import com.v7878.dex.immutable.value.EncodedBoolean;
import com.v7878.dex.immutable.value.EncodedByte;
import com.v7878.dex.immutable.value.EncodedChar;
import com.v7878.dex.immutable.value.EncodedDouble;
import com.v7878.dex.immutable.value.EncodedFloat;
import com.v7878.dex.immutable.value.EncodedInt;
import com.v7878.dex.immutable.value.EncodedLong;
import com.v7878.dex.immutable.value.EncodedNull;
import com.v7878.dex.immutable.value.EncodedShort;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.raw.DebugInfo;
import com.v7878.dex.raw.DexReader;
import com.v7878.dex.raw.DexReader.CodeItem;
import com.v7878.dex.raw.InstructionReader;
import com.v7878.dex.util.DebugInfoMerger;
import com.v7878.dex.util.DebugInfoMerger.MergerDebugItem;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.TryBlocksMerger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;

public class Dex009 {
    public static String readString(DexReader reader, int index, int offset) {
        var in = reader.mainAt(offset);
        int data_offset = in.readSmallUInt();
        int length = in.readSmallUInt();
        return reader.dataAt(data_offset).readMUTF8(length);
    }

    public static TypeId readTypeId(DexReader reader, int index, int offset) {
        var descriptor = reader.getString(reader.mainAt(offset).readSmallUInt());
        return TypeId.ofBinaryName(descriptor);
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

    private static List<FieldDef> readFieldDefList(DexReader reader, int offset, boolean _static) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var list = new ArrayList<FieldDef>(count);
        in.alignPosition(_static ? M3_STATIC_FIELD_DEF_ALIGNMENT : M3_INSTANCE_FIELD_DEF_ALIGNMENT);
        for (int i = 0; i < count; i++) {
            var fid = reader.getField(in.readSmallUInt());
            int access_flags = in.readInt();

            EncodedValue value = null;
            if (_static) {
                var lvalue = in.readLong();
                var type = fid.getType();
                var shorty = type.getShorty();
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

            list.add(FieldDef.of(fid.getName(), fid.getType(), access_flags,
                    0, value, null));
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

    private record RawDebugItem(int index, int position, DebugItem item)
            implements MergerDebugItem<RawDebugItem> {
        @Override
        public int compareTo(RawDebugItem other) {
            if (other == this) return 0;
            int out = Integer.compare(position(), other.position());
            if (out != 0) return out;
            return Integer.compare(index, other.index);
        }
    }

    private static void readLocals(DexReader reader, int offset, List<RawDebugItem> items) {
        var in = reader.dataAt(offset);

        var size = in.readSmallUInt();
        for (int i = 0; i < size; i++) {
            int start = in.readSmallUInt();
            int end = in.readSmallUInt();

            int name_id = in.readSmallUIntWithM1();
            int descriptor_id = in.readSmallUIntWithM1();

            int register = in.readSmallUInt();

            String name = name_id == NO_INDEX ?
                    null : reader.getString(name_id);
            String descriptor = descriptor_id == NO_INDEX ?
                    null : reader.getString(descriptor_id);
            // Note: This version had an additional value
            // for the register type that meant untyped null
            var type = "<null>".equals(descriptor) ? null :
                    TypeId.of(descriptor);

            items.add(new RawDebugItem(items.size(), start,
                    StartLocal.of(register, name, type, null)));
            items.add(new RawDebugItem(items.size(),
                    end, EndLocal.of(register)));
        }
    }

    private static void readPositions(DexReader reader, int offset, List<RawDebugItem> items) {
        var in = reader.dataAt(offset);

        var size = in.readSmallUInt();
        for (int i = 0; i < size; i++) {
            int address = in.readSmallUInt();
            int line = in.readUShort();
            items.add(new RawDebugItem(items.size(),
                    address, LineNumber.of(line)));
        }
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
        var positions_off = in.readSmallUInt();
        var locals_off = in.readSmallUInt();

        var instructions = readInstructions(reader, insns_off);
        NavigableSet<TryBlock> tries = exceptions_off == NO_OFFSET ?
                Collections.emptyNavigableSet() : readTries(reader, exceptions_off);


        DebugInfo debug_info;
        if (reader.options().hasDebugInfo()) {
            List<RawDebugItem> debug_items = new ArrayList<>();

            if (source_file_idx != NO_INDEX) {
                var source_file = reader.getString(source_file_idx);
                debug_items.add(new RawDebugItem(
                        0, 0, SetFile.of(source_file)));
            }
            if (positions_off != NO_OFFSET) {
                readPositions(reader, positions_off, debug_items);
            }
            if (locals_off != NO_OFFSET) {
                readLocals(reader, locals_off, debug_items);
            }

            debug_info = new DebugInfo(null,
                    DebugInfoMerger.mergeDebugItems(debug_items));
        } else {
            debug_info = null;
        }

        return new CodeItem(registers_size, ins_size,
                outs_size, debug_info, instructions, tries);
    }

    private static List<MethodDef> readMethodDefList(
            DexReader reader, int offset, @SuppressWarnings("unused") boolean direct) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var list = new ArrayList<MethodDef>(count);
        for (int i = 0; i < count; i++) {
            var mid = reader.getMethod(in.readSmallUInt());
            int access_flags = in.readInt();
            int throws_list_off = in.readSmallUInt();
            int code_off = in.readSmallUInt();

            List<Annotation> annotations = null;
            if (throws_list_off != NO_OFFSET) {
                var throws_list = reader.getTypeList(throws_list_off);
                annotations = List.of(Annotation.Throws(throws_list));
            }

            var code = code_off == NO_OFFSET ? null : readCodeItem(reader, mid, code_off);
            DebugInfo debug_info = code == null ? null : code.debug_info();
            list.add(MethodDef.of(mid.getName(), mid.getReturnType(),
                    Parameter.listOf(mid.getParameterTypes()), access_flags, 0,
                    DexReader.toImplementation(code, debug_info), annotations));
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

        TypeId clazz = reader.getType(class_idx);
        TypeId superclass = superclass_idx == NO_INDEX ?
                null : reader.getType(superclass_idx);
        List<TypeId> interfaces = interfaces_off == NO_OFFSET ?
                List.of() : reader.getTypeList(interfaces_off);

        List<FieldDef> static_fields = static_fields_off == NO_OFFSET ?
                null : readFieldDefList(reader, static_fields_off, true);
        List<FieldDef> instance_fields = instance_fields_off == NO_OFFSET ?
                null : readFieldDefList(reader, instance_fields_off, false);
        List<MethodDef> direct_methods = direct_methods_off == NO_OFFSET ?
                null : readMethodDefList(reader, direct_methods_off, true);
        List<MethodDef> virtual_methods = virtual_methods_off == NO_OFFSET ?
                null : readMethodDefList(reader, virtual_methods_off, false);

        return ClassDef.raw(clazz, access_flags, superclass, interfaces, null,
                MemberUtils.mergeFields(static_fields, instance_fields),
                MemberUtils.mergeMethods(direct_methods, virtual_methods),
                Collections.emptyNavigableSet());
    }
}
