package com.v7878.dex;

import com.v7878.dex.EncodedValue.AnnotationValue;
import com.v7878.dex.EncodedValue.ArrayValue;
import com.v7878.dex.EncodedValue.BooleanValue;
import com.v7878.dex.EncodedValue.ByteValue;
import com.v7878.dex.EncodedValue.CharValue;
import com.v7878.dex.EncodedValue.DoubleValue;
import com.v7878.dex.EncodedValue.EncodedValueType;
import com.v7878.dex.EncodedValue.EnumValue;
import com.v7878.dex.EncodedValue.FieldValue;
import com.v7878.dex.EncodedValue.FloatValue;
import com.v7878.dex.EncodedValue.IntValue;
import com.v7878.dex.EncodedValue.LongValue;
import com.v7878.dex.EncodedValue.MethodHandleValue;
import com.v7878.dex.EncodedValue.MethodTypeValue;
import com.v7878.dex.EncodedValue.MethodValue;
import com.v7878.dex.EncodedValue.NullValue;
import com.v7878.dex.EncodedValue.ShortValue;
import com.v7878.dex.EncodedValue.StringValue;
import com.v7878.dex.EncodedValue.TypeValue;
import com.v7878.dex.io.RandomInput;

//TODO: move to EncodedValue
public class EncodedValueReader {

    public static int peek(RandomInput in, EncodedValueType type) {
        if (type == null) {
            return in.readUnsignedByte();
        }
        return type.value;
    }

    public static BooleanValue readBoolean(int arg) {
        return new BooleanValue(arg != 0);
    }

    public static ByteValue readByte(RandomInput in, int arg) {
        ByteValue out = new ByteValue();
        out.value = (byte) ValueCoder.readSignedInt(in, arg);
        return out;
    }

    public static ShortValue readShort(RandomInput in, int arg) {
        ShortValue out = new ShortValue();
        out.value = (short) ValueCoder.readSignedInt(in, arg);
        return out;
    }

    public static CharValue readChar(RandomInput in, int arg) {
        CharValue out = new CharValue();
        out.value = (char) ValueCoder.readUnsignedInt(in, arg, false);
        return out;
    }

    public static IntValue readInt(RandomInput in, int arg) {
        IntValue out = new IntValue();
        out.value = ValueCoder.readSignedInt(in, arg);
        return out;
    }

    public static LongValue readLong(RandomInput in, int arg) {
        LongValue out = new LongValue();
        out.value = ValueCoder.readSignedLong(in, arg);
        return out;
    }

    public static FloatValue readFloat(RandomInput in, int arg) {
        FloatValue out = new FloatValue();
        out.value = Float.intBitsToFloat(
                ValueCoder.readUnsignedInt(in, arg, true));
        return out;
    }

    public static DoubleValue readDouble(RandomInput in, int arg) {
        DoubleValue out = new DoubleValue();
        out.value = Double.longBitsToDouble(
                ValueCoder.readUnsignedLong(in, arg, true));
        return out;
    }

    public static MethodTypeValue readMethodType(RandomInput in,
                                                 int arg, ReadContext context) {
        ProtoId value = context.proto(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new MethodTypeValue(value);
    }

    public static MethodHandleValue readMethodHandle(RandomInput in,
                                                     int arg, ReadContext context) {
        MethodHandleItem value = context.method_handle(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new MethodHandleValue(value);
    }

    public static StringValue readString(RandomInput in,
                                         int arg, ReadContext context) {
        String value = context.string(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new StringValue(value);
    }

    public static TypeValue readType(RandomInput in,
                                     int arg, ReadContext context) {
        TypeId value = context.type(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new TypeValue(value);
    }

    public static FieldValue readField(RandomInput in,
                                       int arg, ReadContext context) {
        FieldId value = context.field(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new FieldValue(value);
    }

    public static EnumValue readEnum(RandomInput in,
                                     int arg, ReadContext context) {
        FieldId value = context.field(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new EnumValue(value);
    }

    public static MethodValue readMethod(RandomInput in,
                                         int arg, ReadContext context) {
        MethodId value = context.method(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new MethodValue(value);
    }

    public static ArrayValue readArray(RandomInput in,
                                       ReadContext context) {
        int size = in.readULeb128();
        EncodedValue[] value = new EncodedValue[size];
        for (int i = 0; i < size; i++) {
            value[i] = readValue(in, context);
        }
        return new ArrayValue(value);
    }

    public static AnnotationValue readAnnotation(RandomInput in,
                                                 ReadContext context) {
        EncodedAnnotation value = EncodedAnnotation.read(in, context);
        return new AnnotationValue(value);
    }

    public static EncodedValue readValue(RandomInput in, ReadContext context) {
        return readValue(in, context, null);
    }

    public static EncodedValue readValue(
            RandomInput in, ReadContext context, EncodedValueType type) {
        int type_and_arg = peek(in, type);
        int arg = (type_and_arg & 0xe0) >> 5;
        int int_type = type_and_arg & 0x1f;
        switch (EncodedValueType.of(int_type)) {
            case BOOLEAN:
                return readBoolean(arg);
            case BYTE:
                return readByte(in, arg);
            case SHORT:
                return readShort(in, arg);
            case CHAR:
                return readChar(in, arg);
            case INT:
                return readInt(in, arg);
            case LONG:
                return readLong(in, arg);
            case FLOAT:
                return readFloat(in, arg);
            case DOUBLE:
                return readDouble(in, arg);
            case METHOD_TYPE:
                return readMethodType(in, arg, context);
            case METHOD_HANDLE:
                return readMethodHandle(in, arg, context);
            case STRING:
                return readString(in, arg, context);
            case TYPE:
                return readType(in, arg, context);
            case FIELD:
                return readField(in, arg, context);
            case ENUM:
                return readEnum(in, arg, context);
            case METHOD:
                return readMethod(in, arg, context);
            case ARRAY:
                return readArray(in, context);
            case ANNOTATION:
                return readAnnotation(in, context);
            case NULL:
                return new NullValue();
            default:
                throw new RuntimeException("Unexpected type: " + Integer.toHexString(int_type));
        }
    }
}
