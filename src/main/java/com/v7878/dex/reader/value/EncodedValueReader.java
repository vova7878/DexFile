package com.v7878.dex.reader.value;

import com.v7878.dex.ValueType;
import com.v7878.dex.iface.value.EncodedValue;
import com.v7878.dex.immutable.value.ImmutableEncodedBoolean;
import com.v7878.dex.immutable.value.ImmutableEncodedByte;
import com.v7878.dex.immutable.value.ImmutableEncodedChar;
import com.v7878.dex.immutable.value.ImmutableEncodedDouble;
import com.v7878.dex.immutable.value.ImmutableEncodedFloat;
import com.v7878.dex.immutable.value.ImmutableEncodedInt;
import com.v7878.dex.immutable.value.ImmutableEncodedLong;
import com.v7878.dex.immutable.value.ImmutableEncodedNull;
import com.v7878.dex.immutable.value.ImmutableEncodedShort;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.reader.ReaderDex;

public class EncodedValueReader {
    public static EncodedValue readValue(ReaderDex dexfile, RandomInput in) {
        int type_and_arg = in.readUByte();
        int arg = (type_and_arg & 0xe0) >> 5;
        int int_type = type_and_arg & 0x1f;
        return switch (ValueType.of(int_type)) {
            case BOOLEAN -> ImmutableEncodedBoolean.of(arg != 0);
            case BYTE -> ImmutableEncodedByte.of((byte) ValueCoder.readSignedInt(in, arg));
            case SHORT -> ImmutableEncodedShort.of((short) ValueCoder.readSignedInt(in, arg));
            case CHAR -> ImmutableEncodedChar.of((char)
                    ValueCoder.readUnsignedInt(in, arg, false));
            case INT -> ImmutableEncodedInt.of(ValueCoder.readSignedInt(in, arg));
            case FLOAT -> ImmutableEncodedFloat.of(Float.intBitsToFloat(
                    ValueCoder.readUnsignedInt(in, arg, true)));
            case LONG -> ImmutableEncodedLong.of(ValueCoder.readSignedLong(in, arg));
            case DOUBLE -> ImmutableEncodedDouble.of(Double.longBitsToDouble(
                    ValueCoder.readUnsignedLong(in, arg, true)));
            case NULL -> ImmutableEncodedNull.INSTANCE;
            case STRING -> ReaderEncodedString.readValue(dexfile, in, arg);
            case TYPE -> ReaderEncodedType.readValue(dexfile, in, arg);
            case FIELD -> ReaderEncodedField.readValue(dexfile, in, arg);
            case ENUM -> ReaderEncodedEnum.readValue(dexfile, in, arg);
            case METHOD -> ReaderEncodedMethod.readValue(dexfile, in, arg);
            case METHOD_TYPE -> ReaderEncodedMethodType.readValue(dexfile, in, arg);
            case METHOD_HANDLE -> ReaderEncodedMethodHandle.readValue(dexfile, in, arg);
            case ARRAY -> ReaderEncodedArray.readValue(dexfile, in);
            case ANNOTATION -> ReaderEncodedAnnotation.readValue(dexfile, in);
        };
    }
}
