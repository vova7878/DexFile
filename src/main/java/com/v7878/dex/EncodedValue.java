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

import static com.v7878.dex.DexConstants.VALUE_ANNOTATION;
import static com.v7878.dex.DexConstants.VALUE_ARRAY;
import static com.v7878.dex.DexConstants.VALUE_BOOLEAN;
import static com.v7878.dex.DexConstants.VALUE_BYTE;
import static com.v7878.dex.DexConstants.VALUE_CHAR;
import static com.v7878.dex.DexConstants.VALUE_DOUBLE;
import static com.v7878.dex.DexConstants.VALUE_ENUM;
import static com.v7878.dex.DexConstants.VALUE_FIELD;
import static com.v7878.dex.DexConstants.VALUE_FLOAT;
import static com.v7878.dex.DexConstants.VALUE_INT;
import static com.v7878.dex.DexConstants.VALUE_LONG;
import static com.v7878.dex.DexConstants.VALUE_METHOD;
import static com.v7878.dex.DexConstants.VALUE_METHOD_HANDLE;
import static com.v7878.dex.DexConstants.VALUE_METHOD_TYPE;
import static com.v7878.dex.DexConstants.VALUE_NULL;
import static com.v7878.dex.DexConstants.VALUE_SHORT;
import static com.v7878.dex.DexConstants.VALUE_STRING;
import static com.v7878.dex.DexConstants.VALUE_TYPE;

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.util.MutableList;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;

//TODO: cleanup code
public interface EncodedValue extends Mutable {

    Comparator<EncodedValue> COMPARATOR = (a, b) -> {
        EncodedValueType type;
        if ((type = a.type()) != b.type()) {
            return a.type().value - b.type().value;
        }

        //noinspection unchecked
        return ((Comparator<EncodedValue>) type.comparator).compare(a, b);
    };

    enum EncodedValueType {
        BYTE(VALUE_BYTE, ByteValue.COMPARATOR),
        SHORT(VALUE_SHORT, ShortValue.COMPARATOR),
        CHAR(VALUE_CHAR, CharValue.COMPARATOR),
        INT(VALUE_INT, IntValue.COMPARATOR),
        LONG(VALUE_LONG, LongValue.COMPARATOR),
        FLOAT(VALUE_FLOAT, FloatValue.COMPARATOR),
        DOUBLE(VALUE_DOUBLE, DoubleValue.COMPARATOR),
        METHOD_TYPE(VALUE_METHOD_TYPE, MethodTypeValue.COMPARATOR),
        METHOD_HANDLE(VALUE_METHOD_HANDLE, MethodHandleValue.COMPARATOR),
        STRING(VALUE_STRING, StringValue.COMPARATOR),
        TYPE(VALUE_TYPE, TypeValue.COMPARATOR),
        FIELD(VALUE_FIELD, FieldValue.COMPARATOR),
        METHOD(VALUE_METHOD, MethodValue.COMPARATOR),
        ENUM(VALUE_ENUM, EnumValue.COMPARATOR),
        ARRAY(VALUE_ARRAY, ArrayValue.COMPARATOR),
        ANNOTATION(VALUE_ANNOTATION, AnnotationValue.COMPARATOR),
        NULL(VALUE_NULL, NullValue.COMPARATOR),
        BOOLEAN(VALUE_BOOLEAN, BooleanValue.COMPARATOR);

        public final int value;
        public final Comparator<?> comparator;

        EncodedValueType(int value, Comparator<?> comparator) {
            this.value = value;
            this.comparator = comparator;
        }

        public static EncodedValueType of(int int_type) {
            for (EncodedValueType type : values()) {
                if (int_type == type.value) {
                    return type;
                }
            }
            throw new IllegalStateException("Unexpected type: " + Integer.toHexString(int_type));
        }
    }

    default void collectData(DataCollector data) {
    }

    void write(WriteContext context, RandomOutput out);

    boolean isDefault();

    EncodedValueType type();

    Object value();

    @Override
    EncodedValue mutate();

    static EncodedValue defaultValue(TypeId type) {
        // V - void
        return switch (type.getShorty()) {
            case 'Z' -> new BooleanValue();
            case 'B' -> new ByteValue();
            case 'S' -> new ShortValue();
            case 'C' -> new CharValue();
            case 'I' -> new IntValue();
            case 'J' -> new LongValue();
            case 'F' -> new FloatValue();
            case 'D' -> new DoubleValue();
            case 'L' -> new NullValue();
            default -> throw new IllegalArgumentException();
        };
    }

    static EncodedValue of(Object obj) {
        if (obj == null) {
            return new NullValue();
        }

        if (obj instanceof EncodedValue) {
            return (EncodedValue) obj;
        }

        if (obj instanceof Boolean) {
            return new BooleanValue((Boolean) obj);
        }

        if (obj instanceof Byte) {
            return new ByteValue((Byte) obj);
        }

        if (obj instanceof Short) {
            return new ShortValue((Short) obj);
        }

        if (obj instanceof Character) {
            return new CharValue((Character) obj);
        }

        if (obj instanceof Integer) {
            return new IntValue((Integer) obj);
        }

        if (obj instanceof Float) {
            return new FloatValue((Float) obj);
        }

        if (obj instanceof Long) {
            return new LongValue((Long) obj);
        }

        if (obj instanceof Double) {
            return new DoubleValue((Double) obj);
        }

        if (obj instanceof String) {
            return new StringValue((String) obj);
        }

        if (obj instanceof TypeId) {
            return new TypeValue((TypeId) obj);
        }
        if (obj instanceof Class) {
            return new TypeValue(TypeId.of((Class<?>) obj));
        }

        if (obj instanceof ProtoId) {
            return new MethodTypeValue((ProtoId) obj);
        }
        if (obj instanceof MethodType) {
            return new MethodTypeValue(ProtoId.of((MethodType) obj));
        }

        if (obj instanceof MethodId) {
            return new MethodValue((MethodId) obj);
        }
        if (obj instanceof Executable) {
            return new MethodValue(MethodId.of((Executable) obj));
        }

        if (obj instanceof FieldId) {
            return new FieldValue((FieldId) obj);
        }
        if (obj instanceof Field) {
            return new FieldValue(FieldId.of((Field) obj));
        }

        if (obj instanceof Enum) {
            return new EnumValue(FieldId.of((Enum<?>) obj));
        }

        if (obj instanceof MethodHandleItem) {
            return new MethodHandleValue((MethodHandleItem) obj);
        }
        if (obj instanceof MethodHandle) {
            //TODO: implement by unsafe
            throw new UnsupportedOperationException("not implemented yet");
        }

        if (obj instanceof EncodedAnnotation) {
            return new AnnotationValue((EncodedAnnotation) obj);
        }
        if (obj.getClass().isAnnotation()) {
            //TODO: implement by reflection
            throw new UnsupportedOperationException("not implemented yet");
        }

        if (obj instanceof boolean[]) {
            ArrayValue out = new ArrayValue();
            for (boolean tmp : (boolean[]) obj) {
                out.add(new BooleanValue(tmp));
            }
            return out;
        }

        if (obj instanceof byte[]) {
            ArrayValue out = new ArrayValue();
            for (byte tmp : (byte[]) obj) {
                out.add(new ByteValue(tmp));
            }
            return out;
        }

        if (obj instanceof short[]) {
            ArrayValue out = new ArrayValue();
            for (short tmp : (short[]) obj) {
                out.add(new ShortValue(tmp));
            }
            return out;
        }

        if (obj instanceof char[]) {
            ArrayValue out = new ArrayValue();
            for (char tmp : (char[]) obj) {
                out.add(new CharValue(tmp));
            }
            return out;
        }

        if (obj instanceof int[]) {
            ArrayValue out = new ArrayValue();
            for (int tmp : (int[]) obj) {
                out.add(new IntValue(tmp));
            }
            return out;
        }

        if (obj instanceof float[]) {
            ArrayValue out = new ArrayValue();
            for (float tmp : (float[]) obj) {
                out.add(new FloatValue(tmp));
            }
            return out;
        }

        if (obj instanceof long[]) {
            ArrayValue out = new ArrayValue();
            for (long tmp : (long[]) obj) {
                out.add(new LongValue(tmp));
            }
            return out;
        }

        if (obj instanceof double[]) {
            ArrayValue out = new ArrayValue();
            for (double tmp : (double[]) obj) {
                out.add(new DoubleValue(tmp));
            }
            return out;
        }

        if (obj instanceof Object[]) {
            ArrayValue out = new ArrayValue();
            for (Object tmp : (Object[]) obj) {
                out.add(of(tmp));
            }
            return out;
        }

        throw new IllegalArgumentException("unable to convert " + obj + " to EncodedValue");
    }

    static int peek(RandomInput in, EncodedValueType type) {
        if (type == null) {
            return in.readUnsignedByte();
        }
        return type.value;
    }

    static BooleanValue readBoolean(int arg) {
        return new BooleanValue(arg != 0);
    }

    static ByteValue readByte(RandomInput in, int arg) {
        ByteValue out = new ByteValue();
        out.value = (byte) ValueCoder.readSignedInt(in, arg);
        return out;
    }

    static ShortValue readShort(RandomInput in, int arg) {
        ShortValue out = new ShortValue();
        out.value = (short) ValueCoder.readSignedInt(in, arg);
        return out;
    }

    static CharValue readChar(RandomInput in, int arg) {
        CharValue out = new CharValue();
        out.value = (char) ValueCoder.readUnsignedInt(in, arg, false);
        return out;
    }

    static IntValue readInt(RandomInput in, int arg) {
        IntValue out = new IntValue();
        out.value = ValueCoder.readSignedInt(in, arg);
        return out;
    }

    static LongValue readLong(RandomInput in, int arg) {
        LongValue out = new LongValue();
        out.value = ValueCoder.readSignedLong(in, arg);
        return out;
    }

    static FloatValue readFloat(RandomInput in, int arg) {
        FloatValue out = new FloatValue();
        out.value = Float.intBitsToFloat(
                ValueCoder.readUnsignedInt(in, arg, true));
        return out;
    }

    static DoubleValue readDouble(RandomInput in, int arg) {
        DoubleValue out = new DoubleValue();
        out.value = Double.longBitsToDouble(
                ValueCoder.readUnsignedLong(in, arg, true));
        return out;
    }

    static MethodTypeValue readMethodType(RandomInput in,
                                          int arg, ReadContext context) {
        ProtoId value = context.proto(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new MethodTypeValue(value);
    }

    static MethodHandleValue readMethodHandle(RandomInput in,
                                              int arg, ReadContext context) {
        MethodHandleItem value = context.method_handle(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new MethodHandleValue(value);
    }

    static StringValue readString(RandomInput in,
                                  int arg, ReadContext context) {
        String value = context.string(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new StringValue(value);
    }

    static TypeValue readType(RandomInput in,
                              int arg, ReadContext context) {
        TypeId value = context.type(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new TypeValue(value);
    }

    static FieldValue readField(RandomInput in,
                                int arg, ReadContext context) {
        FieldId value = context.field(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new FieldValue(value);
    }

    static EnumValue readEnum(RandomInput in,
                              int arg, ReadContext context) {
        FieldId value = context.field(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new EnumValue(value);
    }

    static MethodValue readMethod(RandomInput in,
                                  int arg, ReadContext context) {
        MethodId value = context.method(
                ValueCoder.readUnsignedInt(in, arg, false));
        return new MethodValue(value);
    }

    static ArrayValue readArray(RandomInput in,
                                ReadContext context) {
        int size = in.readULeb128();
        EncodedValue[] value = new EncodedValue[size];
        for (int i = 0; i < size; i++) {
            value[i] = readValue(in, context);
        }
        return new ArrayValue(value);
    }

    static AnnotationValue readAnnotation(RandomInput in,
                                          ReadContext context) {
        EncodedAnnotation value = EncodedAnnotation.read(in, context);
        return new AnnotationValue(value);
    }

    static EncodedValue readValue(RandomInput in, ReadContext context) {
        return readValue(in, context, null);
    }

    static EncodedValue readValue(
            RandomInput in, ReadContext context, EncodedValueType type) {
        int type_and_arg = peek(in, type);
        int arg = (type_and_arg & 0xe0) >> 5;
        int int_type = type_and_arg & 0x1f;
        return switch (EncodedValueType.of(int_type)) {
            case BOOLEAN -> readBoolean(arg);
            case BYTE -> readByte(in, arg);
            case SHORT -> readShort(in, arg);
            case CHAR -> readChar(in, arg);
            case INT -> readInt(in, arg);
            case LONG -> readLong(in, arg);
            case FLOAT -> readFloat(in, arg);
            case DOUBLE -> readDouble(in, arg);
            case METHOD_TYPE -> readMethodType(in, arg, context);
            case METHOD_HANDLE -> readMethodHandle(in, arg, context);
            case STRING -> readString(in, arg, context);
            case TYPE -> readType(in, arg, context);
            case FIELD -> readField(in, arg, context);
            case ENUM -> readEnum(in, arg, context);
            case METHOD -> readMethod(in, arg, context);
            case ARRAY -> readArray(in, context);
            case ANNOTATION -> readAnnotation(in, context);
            case NULL -> new NullValue();
            //noinspection UnnecessaryDefault
            default ->
                    throw new RuntimeException("Unexpected type: " + Integer.toHexString(int_type));
        };
    }

    abstract class SimpleValue implements EncodedValue {

        private final EncodedValueType type;

        public SimpleValue(EncodedValueType type) {
            if (type == null) {
                throw new AssertionError();
            }
            this.type = type;
        }

        @Override
        public EncodedValueType type() {
            return type;
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public abstract SimpleValue mutate();

        @Override
        public int hashCode() {
            return Objects.hashCode(value());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return obj instanceof SimpleValue evobj
                    && type() == evobj.type()
                    && Objects.equals(value(), evobj.value());
        }

        @Override
        public String toString() {
            return value().toString();
        }
    }

    class BooleanValue extends SimpleValue {

        public static final Comparator<BooleanValue> COMPARATOR =
                (a, b) -> Boolean.compare(a.value, b.value);

        public boolean value;

        public BooleanValue() {
            super(EncodedValueType.BOOLEAN);
        }

        public BooleanValue(boolean value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            out.writeByte(type().value | ((value ? 1 : 0) << 5));
        }

        @Override
        public boolean isDefault() {
            return !value;
        }

        @Override
        public Boolean value() {
            return value;
        }

        @Override
        public BooleanValue mutate() {
            return new BooleanValue(value);
        }
    }

    class ByteValue extends SimpleValue {

        public static final Comparator<ByteValue> COMPARATOR =
                Comparator.comparingInt(a -> a.value);

        public byte value;

        public ByteValue() {
            super(EncodedValueType.BYTE);
        }

        public ByteValue(byte value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeSignedIntegralValue(out, type(), value);
        }

        @Override
        public boolean isDefault() {
            return value == 0;
        }

        @Override
        public Byte value() {
            return value;
        }

        @Override
        public ByteValue mutate() {
            return new ByteValue(value);
        }
    }

    class ShortValue extends SimpleValue {

        public static final Comparator<ShortValue> COMPARATOR =
                Comparator.comparingInt(a -> a.value);

        public short value;

        public ShortValue() {
            super(EncodedValueType.SHORT);
        }

        public ShortValue(short value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeSignedIntegralValue(out, type(), value);
        }

        @Override
        public boolean isDefault() {
            return value == 0;
        }

        @Override
        public Short value() {
            return value;
        }

        @Override
        public ShortValue mutate() {
            return new ShortValue(value);
        }
    }

    class CharValue extends SimpleValue {

        public static final Comparator<CharValue> COMPARATOR =
                Comparator.comparingInt(a -> a.value);

        public char value;

        public CharValue() {
            super(EncodedValueType.CHAR);
        }

        public CharValue(char value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(), value);
        }

        @Override
        public boolean isDefault() {
            return value == 0;
        }

        @Override
        public Character value() {
            return value;
        }

        @Override
        public CharValue mutate() {
            return new CharValue(value);
        }
    }

    class IntValue extends SimpleValue {

        public static final Comparator<IntValue> COMPARATOR =
                Comparator.comparingInt(a -> a.value);

        public int value;

        public IntValue() {
            super(EncodedValueType.INT);
        }

        public IntValue(int value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeSignedIntegralValue(out, type(), value);
        }

        @Override
        public boolean isDefault() {
            return value == 0;
        }

        @Override
        public Integer value() {
            return value;
        }

        @Override
        public IntValue mutate() {
            return new IntValue(value);
        }
    }

    class LongValue extends SimpleValue {

        public static final Comparator<LongValue> COMPARATOR =
                Comparator.comparingLong(a -> a.value);

        public long value;

        public LongValue() {
            super(EncodedValueType.LONG);
        }

        public LongValue(long value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeSignedIntegralValue(out, type(), value);
        }

        @Override
        public boolean isDefault() {
            return value == 0;
        }

        @Override
        public Long value() {
            return value;
        }

        @Override
        public LongValue mutate() {
            return new LongValue(value);
        }
    }

    class FloatValue extends SimpleValue {

        public static final Comparator<FloatValue> COMPARATOR =
                (a, b) -> Float.compare(a.value, b.value);

        public float value;

        public FloatValue() {
            super(EncodedValueType.FLOAT);
        }

        public FloatValue(float value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeRightZeroExtendedValue(out, type(),
                    ((long) Float.floatToRawIntBits(value)) << 32);
        }

        @Override
        public boolean isDefault() {
            return value == 0f;
        }

        @Override
        public Float value() {
            return value;
        }

        @Override
        public FloatValue mutate() {
            return new FloatValue(value);
        }
    }

    class DoubleValue extends SimpleValue {

        public static final Comparator<DoubleValue> COMPARATOR =
                Comparator.comparingDouble(a -> a.value);

        public double value;

        public DoubleValue() {
            super(EncodedValueType.DOUBLE);
        }

        public DoubleValue(double value) {
            this();
            this.value = value;
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeRightZeroExtendedValue(out, type(),
                    Double.doubleToRawLongBits(value)
            );
        }

        @Override
        public boolean isDefault() {
            return value == 0d;
        }

        @Override
        public Double value() {
            return value;
        }

        @Override
        public DoubleValue mutate() {
            return new DoubleValue(value);
        }
    }

    class NullValue extends SimpleValue {

        public static final Comparator<NullValue> COMPARATOR = (a, b) -> 0;

        public NullValue() {
            super(EncodedValueType.NULL);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            out.writeByte(type().value);
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public String toString() {
            return "null";
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public NullValue mutate() {
            return new NullValue();
        }
    }

    class MethodTypeValue extends SimpleValue {

        public static final Comparator<MethodTypeValue> COMPARATOR =
                (a, b) -> ProtoId.COMPARATOR.compare(a.value, b.value);

        private ProtoId value;

        public MethodTypeValue(ProtoId value) {
            super(EncodedValueType.METHOD_TYPE);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getProtoIndex(value)
            );
        }

        public final void setValue(ProtoId value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final ProtoId value() {
            return value;
        }

        @Override
        public MethodTypeValue mutate() {
            return new MethodTypeValue(value);
        }
    }

    class MethodHandleValue extends SimpleValue {

        public static final Comparator<MethodHandleValue> COMPARATOR =
                (a, b) -> MethodHandleItem.COMPARATOR.compare(a.value, b.value);

        private MethodHandleItem value;

        public MethodHandleValue(MethodHandleItem value) {
            super(EncodedValueType.METHOD_HANDLE);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getMethodHandleIndex(value));
        }

        public final void setValue(MethodHandleItem value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final MethodHandleItem value() {
            return value;
        }

        @Override
        public MethodHandleValue mutate() {
            return new MethodHandleValue(value);
        }
    }

    class StringValue extends SimpleValue {

        public static final Comparator<StringValue> COMPARATOR =
                (a, b) -> StringId.COMPARATOR.compare(a.value, b.value);

        private String value;

        public StringValue(String value) {
            super(EncodedValueType.STRING);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getStringIndex(value));
        }

        public final void setValue(String value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null");
        }

        @Override
        public final String value() {
            return value;
        }

        @Override
        public StringValue mutate() {
            return new StringValue(value);
        }
    }

    class TypeValue extends SimpleValue {

        public static final Comparator<TypeValue> COMPARATOR =
                (a, b) -> TypeId.COMPARATOR.compare(a.value, b.value);

        private TypeId value;

        public TypeValue(TypeId value) {
            super(EncodedValueType.TYPE);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getTypeIndex(value));
        }

        public final void setValue(TypeId value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final TypeId value() {
            return value;
        }

        @Override
        public TypeValue mutate() {
            return new TypeValue(value);
        }
    }

    class FieldValue extends SimpleValue {

        public static final Comparator<FieldValue> COMPARATOR =
                (a, b) -> FieldId.COMPARATOR.compare(a.value, b.value);

        private FieldId value;

        public FieldValue(FieldId value) {
            super(EncodedValueType.FIELD);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getFieldIndex(value));
        }

        public final void setValue(FieldId value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final FieldId value() {
            return value;
        }

        @Override
        public FieldValue mutate() {
            return new FieldValue(value);
        }
    }

    class MethodValue extends SimpleValue {

        public static final Comparator<MethodValue> COMPARATOR =
                (a, b) -> MethodId.COMPARATOR.compare(a.value, b.value);

        private MethodId value;

        public MethodValue(MethodId value) {
            super(EncodedValueType.METHOD);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getMethodIndex(value));
        }

        public final void setValue(MethodId value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final MethodId value() {
            return value;
        }

        @Override
        public MethodValue mutate() {
            return new MethodValue(value);
        }
    }

    class EnumValue extends SimpleValue {

        public static final Comparator<EnumValue> COMPARATOR =
                (a, b) -> FieldId.COMPARATOR.compare(a.value, b.value);

        private FieldId value;

        public EnumValue(FieldId value) {
            super(EncodedValueType.ENUM);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.add(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            ValueCoder.writeUnsignedIntegralValue(out, type(),
                    context.getFieldIndex(value));
        }

        public final void setValue(FieldId value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final FieldId value() {
            return value;
        }

        @Override
        public EnumValue mutate() {
            return new EnumValue(value);
        }
    }

    class ArrayValue extends MutableList<EncodedValue> implements EncodedValue {

        public static final Comparator<ArrayValue> COMPARATOR
                = getComparator(EncodedValue.COMPARATOR);

        public ArrayValue(EncodedValue... value) {
            super(value);
        }

        @Override
        public EncodedValueType type() {
            return EncodedValueType.ARRAY;
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public void collectData(DataCollector data) {
            for (EncodedValue tmp : this) {
                data.fill(tmp);
            }
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            out.writeByte(type().value);
            writeData(context, out);
        }

        public void writeData(WriteContext context, RandomOutput out) {
            out.writeULeb128(size());
            for (EncodedValue tmp : this) {
                tmp.write(context, out);
            }
        }

        static void writeSection(WriteContextImpl context, FileMap map,
                                 RandomOutput out, ArrayValue[] array_values) {
            if (array_values.length != 0) {
                map.encoded_arrays_off = (int) out.position();
                map.encoded_arrays_size = array_values.length;
            }
            for (ArrayValue tmp : array_values) {
                int start = (int) out.position();
                tmp.writeData(context, out);
                context.addArrayValue(tmp, start);
            }
        }

        @Override
        public final ArrayValue value() {
            return this;
        }

        public boolean containsOnlyDefaults() {
            for (EncodedValue tmp : this) {
                if (!tmp.isDefault()) {
                    return false;
                }
            }
            return true;
        }

        // hashcode from super

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            return obj instanceof ArrayValue && super.equals(obj);
        }

        @Override
        public ArrayValue mutate() {
            ArrayValue out = new ArrayValue();
            out.addAll(this);
            return out;
        }
    }

    class AnnotationValue extends SimpleValue {

        public static final Comparator<AnnotationValue> COMPARATOR =
                (a, b) -> EncodedAnnotation.COMPARATOR.compare(a.value, b.value);

        private EncodedAnnotation value;

        public AnnotationValue(EncodedAnnotation value) {
            super(EncodedValueType.ANNOTATION);
            setValue(value);
        }

        @Override
        public void collectData(DataCollector data) {
            data.fill(value);
        }

        @Override
        public void write(WriteContext context, RandomOutput out) {
            out.writeByte(type().value);
            value.write(context, out);
        }

        public final void setValue(EncodedAnnotation value) {
            this.value = Objects.requireNonNull(value,
                    "value can`t be null").mutate();
        }

        @Override
        public final EncodedAnnotation value() {
            return value;
        }

        @Override
        public AnnotationValue mutate() {
            return new AnnotationValue(value);
        }
    }
}
