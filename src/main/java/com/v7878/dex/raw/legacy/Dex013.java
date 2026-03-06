package com.v7878.dex.raw.legacy;

import static com.v7878.dex.DexConstants.ACC_ABSTRACT;
import static com.v7878.dex.DexConstants.ACC_NATIVE;
import static com.v7878.dex.DexConstants.ACC_SYNTHETIC;
import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.util.Ids.CLASS;
import static com.v7878.dex.util.Ids.STRING;
import static com.v7878.dex.util.ShortyUtils.invalidShorty;

import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.Parameter;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
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
import com.v7878.dex.raw.DexReader;
import com.v7878.dex.util.MemberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dex013 {
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

    private static List<FieldDef> readFieldDefList(DexReader reader, int offset, boolean _static) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var list = new ArrayList<FieldDef>(count);
        // TODO
        in.alignPosition(_static ? 8 : 4);
        for (int i = 0; i < count; i++) {
            var id = reader.getField(in.readSmallUInt());
            int access_flags = in.readInt();
            EncodedValue value = null;
            if (_static) {
                var lvalue = in.readLong();
                var type = id.getType();
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
                        // TODO: msg
                        throw new IllegalArgumentException();
                    }
                    default -> throw invalidShorty(shorty);
                };
            }
            list.add(FieldDef.of(id.getName(), id.getType(), access_flags,
                    0, value, null));
        }
        return list;
    }

    private static List<MethodDef> readMethodDefList(DexReader reader, int offset, boolean direct) {
        var in = reader.dataAt(offset);
        int count = in.readSmallUInt();
        var list = new ArrayList<MethodDef>(count);
        for (int i = 0; i < count; i++) {
            var id = reader.getMethod(in.readSmallUInt());
            int access_flags = in.readInt();
            int throws_list_off = in.readInt();
            int code_off = in.readInt();
            // TODO: read code
            list.add(MethodDef.of(id.getName(), id.getReturnType(),
                    Parameter.listOf(id.getParameterTypes()),
                    (access_flags & ACC_ABSTRACT) != 0 ? access_flags :
                            ((access_flags & ACC_NATIVE) != 0 ? access_flags :
                                    (access_flags | ACC_NATIVE | ACC_SYNTHETIC)),
                    0, null, null));
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

        // TODO? The actual dex013 format always has this field set to 0.
        //  This is either a typo or a disabled, unstable option. Because the annotations
        //  are actually written to the dex file, they're simply not linked to their class.
        //  We can fix the dx utility from the SDK and try to restore the structure,
        //  but it's never been correct. Is this necessary?
        int annotations_off = in.readSmallUInt();

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
