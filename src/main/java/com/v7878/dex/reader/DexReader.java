package com.v7878.dex.reader;

import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.DexOffsets.BASE_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.CALL_SITE_ID_SIZE;
import static com.v7878.dex.DexOffsets.CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_HANDLE_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_ID_SIZE;
import static com.v7878.dex.DexOffsets.PROTO_ID_SIZE;
import static com.v7878.dex.DexOffsets.STRING_ID_SIZE;
import static com.v7878.dex.DexOffsets.TYPE_ID_SIZE;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.DexConstants;
import com.v7878.dex.DexFactory;
import com.v7878.dex.DexOffsets;
import com.v7878.dex.DexVersion;
import com.v7878.dex.MethodHandleType;
import com.v7878.dex.ReadOptions;
import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MemberId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
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
import com.v7878.dex.io.ByteArrayInput;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.raw.AnnotationDirectory;
import com.v7878.dex.raw.MapItem;
import com.v7878.dex.util.CachedFixedSizeList;
import com.v7878.dex.util.FixedSizeSet;
import com.v7878.dex.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public class DexReader {
    private final RandomInput main_buffer;
    private final RandomInput data_buffer;

    private final ReadOptions options;
    private final DexVersion version;

    private final SparseArray<List<TypeId>> typelist_cache;
    private final SparseArray<EncodedArray> encoded_array_cache;
    private final SparseArray<Annotation> annotation_cache;
    private final SparseArray<List<Annotation>> annotation_list_cache;
    private final SparseArray<List<Set<Annotation>>> annotation_set_list_cache;
    private final SparseArray<AnnotationDirectory> annotation_directory_cache;

    private final List<String> string_section;
    private final List<TypeId> type_section;
    private final List<FieldId> field_section;
    private final List<ProtoId> proto_section;
    private final List<MethodId> method_section;
    private final List<MethodHandleId> method_handle_section;
    private final List<CallSiteId> callsite_section;
    private final List<ClassDef> class_section;

    private final List<MapItem> map_items;

    //private final HiddenApiData hiddenapi_section;

    public DexReader(ReadOptions options, byte[] buf, int offset, int header_offset) {
        this.options = options;

        main_buffer = new ByteArrayInput(buf).slice(offset);

        if (main_buffer.size() < BASE_HEADER_SIZE) {
            throw new DexFactory.NotADexFile("File is too short");
        }
        version = DexVersion.forMagic(mainAt(0).readLong());
        //TODO: check version min api

        //TODO: check endian tag

        int container_off = 0;
        if (version.isDexContainer()) {
            container_off = mainAt(header_offset + DexOffsets.CONTAINER_OFF_OFFSET).readSmallUInt();
        }
        if (container_off != header_offset) {
            throw new DexFactory.InvalidDexFile("Unexpected container offset in header");
        }

        int data_off = 0;
        if (version.isCompact()) {
            data_off = mainAt(header_offset + DexOffsets.DATA_START_OFFSET).readSmallUInt();
        }
        data_buffer = main_buffer.slice(data_off);

        typelist_cache = new SparseArray<>();
        encoded_array_cache = new SparseArray<>();
        annotation_cache = new SparseArray<>();
        annotation_list_cache = new SparseArray<>();
        annotation_set_list_cache = new SparseArray<>();
        annotation_directory_cache = new SparseArray<>();

        string_section = makeSection(
                mainAt(header_offset + DexOffsets.STRING_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexOffsets.STRING_START_OFFSET).readSmallUInt(),
                STRING_ID_SIZE, this::readString
        );
        type_section = makeSection(
                mainAt(header_offset + DexOffsets.TYPE_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexOffsets.TYPE_START_OFFSET).readSmallUInt(),
                TYPE_ID_SIZE, this::readTypeId
        );
        field_section = makeSection(
                mainAt(header_offset + DexOffsets.FIELD_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexOffsets.FIELD_START_OFFSET).readSmallUInt(),
                FIELD_ID_SIZE, this::readFieldId
        );
        proto_section = makeSection(
                mainAt(header_offset + DexOffsets.PROTO_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexOffsets.PROTO_START_OFFSET).readSmallUInt(),
                PROTO_ID_SIZE, this::readProtoId
        );
        method_section = makeSection(
                mainAt(header_offset + DexOffsets.METHOD_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexOffsets.METHOD_START_OFFSET).readSmallUInt(),
                METHOD_ID_SIZE, this::readMethodId
        );

        map_items = readMapItemsList(mainAt(header_offset + DexOffsets.MAP_OFFSET).readSmallUInt());

        MapItem method_handles = getMapItemForSection(DexConstants.TYPE_METHOD_HANDLE_ITEM);
        method_handle_section = makeSection(
                method_handles != null ? method_handles.size() : 0,
                method_handles != null ? method_handles.offset() : NO_OFFSET,
                METHOD_HANDLE_ID_SIZE, this::readMethodHandleId
        );

        MapItem callsites = getMapItemForSection(DexConstants.TYPE_CALL_SITE_ID_ITEM);
        callsite_section = makeSection(
                callsites != null ? callsites.size() : 0,
                callsites != null ? callsites.offset() : NO_OFFSET,
                CALL_SITE_ID_SIZE, this::readCallSiteId
        );

        /*MapItem hiddenapi = getMapItemForSection(DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM);
        hiddenapi_section = new HiddenApiData(this,
                hiddenapi != null ? hiddenapi.getOffset() : NO_OFFSET);*/

        class_section = makeSection(
                mainAt(header_offset + DexOffsets.CLASS_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + DexOffsets.CLASS_START_OFFSET).readSmallUInt(),
                CLASS_DEF_SIZE, this::readClassDef
        );
    }

    public DexReader(ReadOptions options, byte[] buf) {
        this(options, buf, 0, 0);
    }

    public RandomInput mainAt(int offset) {
        return main_buffer.duplicateAt(offset);
    }

    public RandomInput dataAt(int offset) {
        return data_buffer.duplicateAt(offset);
    }

    public ReadOptions options() {
        return options;
    }

    public DexVersion version() {
        return version;
    }

    private MapItem readMapItem(RandomInput in) {
        int type = in.readUShort();
        in.addPosition(2); // padding
        int size = in.readInt();
        int offset = in.readInt();
        return new MapItem(type, size, offset);
    }

    private List<MapItem> readMapItemsList(int map_off) {
        if (map_off == NO_OFFSET) return List.of();
        var in = dataAt(map_off);
        int map_size = in.readSmallUInt();
        var out = new ArrayList<MapItem>(map_size);
        for (int i = 0; i < map_size; i++) {
            out.add(i, readMapItem(in));
        }
        return out;
    }

    public List<MapItem> getMapItems() {
        return map_items;
    }

    public MapItem getMapItemForSection(int type) {
        for (MapItem mapItem : getMapItems()) {
            if (mapItem.type() == type) {
                return mapItem;
            }
        }
        return null;
    }

    private List<TypeId> readTypeList(int offset) {
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<TypeId>(size);
        for (int i = 0; i < size; i++) {
            out.add(i, getTypeId(in.readUShort()));
        }
        return out;
    }

    public List<TypeId> getTypeList(int offset) {
        var section = typelist_cache;
        var out = section.get(offset);
        if (out != null) return out;
        out = readTypeList(offset);
        section.put(offset, out);
        return out;
    }

    private EncodedArray readEncodedArray(RandomInput in) {
        int size = in.readSmallULeb128();
        var value = new ArrayList<EncodedValue>(size);
        for (int i = 0; i < size; i++) {
            value.add(i, readEncodedValue(in));
        }
        return EncodedArray.of(value);
    }

    private AnnotationElement readAnnotationElement(RandomInput in) {
        var name = getString(in.readULeb128());
        var value = readEncodedValue(in);
        return AnnotationElement.of(name, value);
    }

    private EncodedAnnotation readEncodedAnnotation(RandomInput in) {
        TypeId type = getTypeId(in.readULeb128());
        int size = in.readULeb128();
        var elements = new HashSet<AnnotationElement>(size);
        for (int i = 0; i < size; i++) {
            elements.add(readAnnotationElement(in));
        }
        return EncodedAnnotation.of(type, elements);
    }

    private EncodedValue readEncodedValue(RandomInput in) {
        int type_and_arg = in.readUByte();
        int arg = (type_and_arg & 0xe0) >> 5;
        int int_type = type_and_arg & 0x1f;
        return switch (ValueType.of(int_type)) {
            case BOOLEAN -> EncodedBoolean.of(arg != 0);
            case BYTE -> EncodedByte.of((byte) ValueCoder.readSignedInt(in, arg));
            case SHORT -> EncodedShort.of((short) ValueCoder.readSignedInt(in, arg));
            case CHAR -> EncodedChar.of((char)
                    ValueCoder.readUnsignedInt(in, arg, false));
            case INT -> EncodedInt.of(ValueCoder.readSignedInt(in, arg));
            case FLOAT -> EncodedFloat.of(Float.intBitsToFloat(
                    ValueCoder.readUnsignedInt(in, arg, true)));
            case LONG -> EncodedLong.of(ValueCoder.readSignedLong(in, arg));
            case DOUBLE -> EncodedDouble.of(Double.longBitsToDouble(
                    ValueCoder.readUnsignedLong(in, arg, true)));
            case NULL -> EncodedNull.INSTANCE;
            case STRING -> EncodedString.of(getString(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case TYPE -> EncodedType.of(getTypeId(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case FIELD -> EncodedField.of(getFieldId(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case ENUM -> EncodedEnum.of(getFieldId(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case METHOD -> EncodedMethod.of(getMethodId(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case METHOD_TYPE -> EncodedMethodType.of(getProtoId(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case METHOD_HANDLE -> EncodedMethodHandle.of(getMethodHandleId(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case ARRAY -> readEncodedArray(in);
            case ANNOTATION -> readEncodedAnnotation(in);
        };
    }

    public EncodedArray getEncodedArray(int offset) {
        var section = encoded_array_cache;
        var out = section.get(offset);
        if (out != null) return out;
        out = readEncodedArray(dataAt(offset));
        section.put(offset, out);
        return out;
    }

    private Annotation readAnnotation(int offset) {
        var in = dataAt(offset);
        var visibility = AnnotationVisibility.of(in.readUByte());
        var encoded_annotation = readEncodedAnnotation(in);
        return Annotation.of(visibility, encoded_annotation);
    }

    public Annotation getAnnotation(int offset) {
        var section = annotation_cache;
        var out = section.get(offset);
        if (out != null) return out;
        out = readAnnotation(offset);
        section.put(offset, out);
        return out;
    }

    private List<Annotation> readAnnotationList(int offset) {
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<Annotation>(size);
        for (int i = 0; i < size; i++) {
            out.add(i, getAnnotation(in.readSmallUInt()));
        }
        return out;
    }

    public List<Annotation> getAnnotationList(int offset) {
        var section = annotation_list_cache;
        var out = section.get(offset);
        if (out != null) return out;
        out = readAnnotationList(offset);
        section.put(offset, out);
        return out;
    }

    public Set<Annotation> getAnnotationSet(int offset) {
        return FixedSizeSet.ofList(getAnnotationList(offset));
    }

    private List<Set<Annotation>> readAnnotationSetList(int offset) {
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<Set<Annotation>>(size);
        for (int i = 0; i < size; i++) {
            out.add(i, getAnnotationSet(in.readSmallUInt()));
        }
        return out;
    }

    public List<Set<Annotation>> getAnnotationSetList(int offset) {
        var section = annotation_set_list_cache;
        var out = section.get(offset);
        if (out != null) return out;
        out = readAnnotationSetList(offset);
        section.put(offset, out);
        return out;
    }

    /*public AnnotationDirectory getAnnotationDirectory(int offset) {
        var section = annotation_directory_cache;
        AnnotationDirectory out = section.get(offset);
        if (out != null) return out;
        out = new AnnotationDirectory(this, offset);
        section.put(offset, out);
        return out;
    }*/

    private void checkIndex(int index, int size, String name) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid %s index %d, not in [0, %d)", name, index, size));
        }
    }

    private <T> List<T> makeSection(int section_size, int offset, int element_size, IntFunction<T> readeer) {
        if (section_size == 0) return List.of();
        return new CachedFixedSizeList<>(section_size) {
            @Override
            protected T compute(int index) {
                return readeer.apply(offset + index * element_size);
            }
        };
    }

    private String readString(int offset) {
        int data_offset = mainAt(offset).readSmallUInt();
        return dataAt(data_offset).readMUTF8();
    }

    public String getString(int index) {
        var section = string_section;
        checkIndex(index, section.size(), "string");
        return section.get(index);
    }

    private TypeId readTypeId(int offset) {
        var descriptor = getString(mainAt(offset).readSmallUInt());
        return TypeId.of(descriptor);
    }

    public TypeId getTypeId(int index) {
        var section = type_section;
        checkIndex(index, section.size(), "type");
        return section.get(index);
    }

    private FieldId readFieldId(int offset) {
        var in = mainAt(offset);
        var declaring_class = getTypeId(in.readUShort());
        var type = getTypeId(in.readUShort());
        var name = getString(in.readSmallUInt());
        return FieldId.of(declaring_class, name, type);
    }

    public FieldId getFieldId(int index) {
        var section = field_section;
        checkIndex(index, section.size(), "field");
        return section.get(index);
    }

    private ProtoId readProtoId(int offset) {
        var in = mainAt(offset);
        in.readSmallUInt(); // shorty
        var return_type = getTypeId(in.readSmallUInt());
        int parameters_off = in.readSmallUInt();
        var parameters = parameters_off == 0 ? null : getTypeList(parameters_off);
        return ProtoId.of(return_type, parameters);
    }

    public ProtoId getProtoId(int index) {
        var section = proto_section;
        checkIndex(index, section.size(), "proto");
        return section.get(index);
    }

    private MethodId readMethodId(int offset) {
        var in = mainAt(offset);
        var declaring_class = getTypeId(in.readUShort());
        var proto = getProtoId(in.readUShort());
        var name = getString(in.readSmallUInt());
        return MethodId.of(declaring_class, name, proto.getReturnType(), proto.getParameterTypes());
    }


    public MethodId getMethodId(int index) {
        var section = method_section;
        checkIndex(index, section.size(), "method");
        return section.get(index);
    }

    private MethodHandleId readMethodHandleId(int offset) {
        var in = mainAt(offset);
        MethodHandleType type = MethodHandleType.of(in.readUShort());
        in.readUShort(); // padding
        int member_id = in.readUShort();
        in.readUShort(); // padding
        MemberId member = type.isMethodAccess() ? getMethodId(member_id) : getFieldId(member_id);
        return MethodHandleId.of(type, member);
    }

    public MethodHandleId getMethodHandleId(int index) {
        var section = method_handle_section;
        checkIndex(index, section.size(), "method handle");
        return section.get(index);
    }

    private CallSiteId readCallSiteId(int offset) {
        int index = 0; // FIXME
        var in = mainAt(offset);
        var array = getEncodedArray(in.readSmallUInt()).getValue();
        if (array.size() < 3) {
            throw new IllegalStateException("Invalid call site item: must contain at least 3 entries");
        }
        MethodHandleId handle;
        {
            var value = array.get(0);
            if (value.getValueType() != ValueType.METHOD_HANDLE) {
                throw new IllegalStateException(String.format(
                        "Invalid encoded value type (%s) for the first item in call site %d",
                        value.getValueType(), index));
            }
            handle = ((EncodedMethodHandle) value).getValue();
        }
        String name;
        {
            var value = array.get(1);
            if (value.getValueType() != ValueType.STRING) {
                throw new IllegalStateException(String.format(
                        "Invalid encoded value type (%s) for the second item in call site %d",
                        value.getValueType(), index));
            }
            name = ((EncodedString) value).getValue();
        }
        ProtoId proto;
        {
            var value = array.get(2);
            if (value.getValueType() != ValueType.METHOD_TYPE) {
                throw new IllegalStateException(String.format(
                        "Invalid encoded value type (%s) for the third item in call site %d",
                        value.getValueType(), index));
            }
            proto = ((EncodedMethodType) value).getValue();
        }
        List<EncodedValue> extra_args;
        {
            extra_args = array.size() <= 3 ? List.of() : array.subList(3, array.size());
        }
        return CallSiteId.of(handle, name, proto, extra_args);
    }

    public CallSiteId getCallSiteId(int index) {
        var section = callsite_section;
        checkIndex(index, section.size(), "callsite");
        return section.get(index);
    }

    private ClassDef readClassDef(int offset) {
        //TODO
        throw new UnsupportedOperationException("Not supported yet!");
    }

    public List<ClassDef> getClasses() {
        return class_section;
    }

    /*public IntSupplier getHiddenApiIterator(int class_idx) {
        checkIndex(class_idx, class_section.size(), "class def");
        return hiddenapi_section.iterator(class_idx);
    }*/
}
