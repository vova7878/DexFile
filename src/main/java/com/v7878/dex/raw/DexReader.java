package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.DexOffsets.BASE_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.CALL_SITE_ID_SIZE;
import static com.v7878.dex.DexOffsets.CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_HANDLE_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_ID_SIZE;
import static com.v7878.dex.DexOffsets.PROTO_ID_SIZE;
import static com.v7878.dex.DexOffsets.STRING_ID_SIZE;
import static com.v7878.dex.DexOffsets.TRY_ITEM_ALIGNMENT;
import static com.v7878.dex.DexOffsets.TRY_ITEM_SIZE;
import static com.v7878.dex.DexOffsets.TYPE_ID_SIZE;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.DexConstants;
import com.v7878.dex.DexFactory;
import com.v7878.dex.DexOffsets;
import com.v7878.dex.DexVersion;
import com.v7878.dex.MethodHandleType;
import com.v7878.dex.Opcodes;
import com.v7878.dex.ReadOptions;
import com.v7878.dex.ReferenceType.ReferenceIndexer;
import com.v7878.dex.ValueType;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.ExceptionHandler;
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
import com.v7878.dex.util.CachedFixedSizeList;
import com.v7878.dex.util.FixedSizeSet;
import com.v7878.dex.util.MemberUtils;
import com.v7878.dex.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public class DexReader implements ReferenceIndexer {
    private final RandomInput main_buffer;
    private final RandomInput data_buffer;

    private final ReadOptions options;
    private final DexVersion version;
    private final Opcodes opcodes;

    private final IntFunction<List<TypeId>> typelist_cache;
    private final IntFunction<EncodedArray> encoded_array_cache;
    private final IntFunction<Annotation> annotation_cache;
    private final IntFunction<List<Annotation>> annotation_list_cache;
    private final IntFunction<List<Set<Annotation>>> annotation_set_list_cache;
    private final IntFunction<AnnotationDirectory> annotation_directory_cache;
    private final IntFunction<CodeItem> code_cache;

    private final List<String> string_section;
    private final List<TypeId> type_section;
    private final List<FieldId> field_section;
    private final List<ProtoId> proto_section;
    private final List<MethodId> method_section;
    private final List<MethodHandleId> method_handle_section;
    private final List<CallSiteId> callsite_section;
    private final List<ClassDef> class_section;

    private final List<MapItem> map_items;

    private final IntFunction<IntSupplier> hiddenapi_section;

    public DexReader(ReadOptions options, byte[] buf, int offset, int header_offset) {
        this.options = options;

        main_buffer = new ByteArrayInput(buf).slice(offset);

        if (main_buffer.size() < BASE_HEADER_SIZE) {
            throw new DexFactory.NotADexFile("File is too short");
        }
        version = DexVersion.forMagic(mainAt(0).readLong());
        //TODO: check version min api

        opcodes = Opcodes.of(version, options.getTargetApi(),
                options.isTargetForArt(), options.hasOdexInstructions());

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

        typelist_cache = makeOffsetCache(this::readTypeList);
        encoded_array_cache = makeOffsetCache(this::readEncodedArray);
        annotation_cache = makeOffsetCache(this::readAnnotation);
        annotation_list_cache = makeOffsetCache(this::readAnnotationList);
        annotation_set_list_cache = makeOffsetCache(this::readAnnotationSetList);
        annotation_directory_cache = makeOffsetCache(this::readAnnotationDirectory);
        code_cache = makeOffsetCache(this::readCodeItem);

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

        MapItem hiddenapi = getMapItemForSection(DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM);
        hiddenapi_section = readHiddenApiSection(
                hiddenapi != null ? hiddenapi.offset() : NO_OFFSET);

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

    public Opcodes opcodes() {
        return opcodes;
    }

    public ReadOptions options() {
        return options;
    }

    public DexVersion version() {
        return version;
    }

    public boolean isCompact() {
        return version().isCompact();
    }

    private MapItem readMapItem(RandomInput in) {
        int type = in.readUShort();
        in.addPosition(2); // padding
        int size = in.readSmallUInt();
        int offset = in.readSmallUInt();
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

    private static <T> IntFunction<T> makeOffsetCache(IntFunction<T> reader) {
        var cache = new SparseArray<T>();
        return offset -> {
            var out = cache.get(offset);
            if (out != null) return out;
            out = reader.apply(offset);
            cache.put(offset, out);
            return out;
        };
    }

    public List<TypeId> getTypeList(int offset) {
        return typelist_cache.apply(offset);
    }

    private EncodedArray readEncodedArray(RandomInput in) {
        int size = in.readSmallULeb128();
        var value = new ArrayList<EncodedValue>(size);
        for (int i = 0; i < size; i++) {
            value.add(i, readEncodedValue(in));
        }
        return EncodedArray.of(value);
    }

    private EncodedArray readEncodedArray(int offset) {
        return readEncodedArray(dataAt(offset));
    }

    private AnnotationElement readAnnotationElement(RandomInput in) {
        var name = getString(in.readSmallULeb128());
        var value = readEncodedValue(in);
        return AnnotationElement.of(name, value);
    }

    private EncodedAnnotation readEncodedAnnotation(RandomInput in) {
        TypeId type = getTypeId(in.readSmallULeb128());
        int size = in.readSmallULeb128();
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
        return encoded_array_cache.apply(offset);
    }

    private Annotation readAnnotation(int offset) {
        var in = dataAt(offset);
        var visibility = AnnotationVisibility.of(in.readUByte());
        var encoded_annotation = readEncodedAnnotation(in);
        return Annotation.of(visibility, encoded_annotation);
    }

    public Annotation getAnnotation(int offset) {
        return annotation_cache.apply(offset);
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
        return annotation_list_cache.apply(offset);
    }

    public Set<Annotation> getAnnotationSet(int offset) {
        return FixedSizeSet.ofList(getAnnotationList(offset));
    }

    private List<Set<Annotation>> readAnnotationSetList(int offset) {
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<Set<Annotation>>(size);
        for (int i = 0; i < size; i++) {
            int annotations_off = in.readSmallUInt();
            var annotations = annotations_off == NO_OFFSET ?
                    Set.<Annotation>of() : getAnnotationSet(in.readSmallUInt());
            out.add(i, annotations);
        }
        return out;
    }

    public List<Set<Annotation>> getAnnotationSetList(int offset) {
        return annotation_set_list_cache.apply(offset);
    }

    private SparseArray<Set<Annotation>> readAnnotationSetMap(RandomInput in, int size) {
        var out = new SparseArray<Set<Annotation>>(size);
        for (int i = 0; i < size; i++) {
            int id = in.readSmallUInt();
            var set = getAnnotationSet(in.readSmallUInt());
            out.put(id, set);
        }
        return out;
    }

    private SparseArray<List<Set<Annotation>>> readAnnotationSetListMap(RandomInput in, int size) {
        var out = new SparseArray<List<Set<Annotation>>>(size);
        for (int i = 0; i < size; i++) {
            int id = in.readSmallUInt();
            var list = getAnnotationSetList(in.readSmallUInt());
            out.put(id, list);
        }
        return out;
    }

    private AnnotationDirectory readAnnotationDirectory(int offset) {
        var in = dataAt(offset);
        int class_annotations_off = in.readSmallUInt();
        var class_annotations = class_annotations_off == NO_OFFSET ?
                null : getAnnotationSet(class_annotations_off);
        int annotated_fields_size = in.readSmallUInt();
        int annotated_methods_size = in.readSmallUInt();
        int annotated_parameters_size = in.readSmallUInt();
        var field_annotations = annotated_fields_size == 0 ?
                null : readAnnotationSetMap(in, annotated_fields_size);
        var method_annotations = annotated_methods_size == 0 ?
                null : readAnnotationSetMap(in, annotated_methods_size);
        var parameter_annotations = annotated_parameters_size == 0 ?
                null : readAnnotationSetListMap(in, annotated_parameters_size);
        return AnnotationDirectory.of(class_annotations, field_annotations,
                method_annotations, parameter_annotations);
    }

    public AnnotationDirectory getAnnotationDirectory(int offset) {
        return annotation_directory_cache.apply(offset);
    }

    private void checkIndex(int index, int size, String name) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid %s index %d, not in [0, %d)", name, index, size));
        }
    }

    private interface SectionReader<T> {
        T read(int index, int offset);
    }

    private <T> List<T> makeSection(int section_size, int offset, int element_size, SectionReader<T> reader) {
        if (section_size == 0) return List.of();
        return new CachedFixedSizeList<>(section_size) {
            @Override
            protected T compute(int index) {
                return reader.read(index, offset + index * element_size);
            }
        };
    }

    private String readString(int index, int offset) {
        int data_offset = mainAt(offset).readSmallUInt();
        return dataAt(data_offset).readMUTF8();
    }

    public String getString(int index) {
        var section = string_section;
        checkIndex(index, section.size(), "string");
        return section.get(index);
    }

    private TypeId readTypeId(int index, int offset) {
        var descriptor = getString(mainAt(offset).readSmallUInt());
        return TypeId.of(descriptor);
    }

    public TypeId getTypeId(int index) {
        var section = type_section;
        checkIndex(index, section.size(), "type");
        return section.get(index);
    }

    private FieldId readFieldId(int index, int offset) {
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

    private ProtoId readProtoId(int index, int offset) {
        var in = mainAt(offset);
        in.readSmallUInt(); // shorty
        var return_type = getTypeId(in.readSmallUInt());
        int parameters_off = in.readSmallUInt();
        var parameters = parameters_off == NO_OFFSET ?
                null : getTypeList(parameters_off);
        return ProtoId.of(return_type, parameters);
    }

    public ProtoId getProtoId(int index) {
        var section = proto_section;
        checkIndex(index, section.size(), "proto");
        return section.get(index);
    }

    private MethodId readMethodId(int index, int offset) {
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

    private MethodHandleId readMethodHandleId(int index, int offset) {
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

    private CallSiteId readCallSiteId(int index, int offset) {
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

    private List<FieldDef> readFieldDefList(
            RandomInput in, int count, IntSupplier hiddenapi,
            SparseArray<Set<Annotation>> annotations_map,
            List<EncodedValue> static_values, boolean static_list) {
        List<FieldDef> out = new ArrayList<>(count);
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += in.readSmallULeb128();
            var id = getFieldId(index);
            int access_flags = in.readULeb128();
            EncodedValue initial_value = null;
            if (static_list) {
                initial_value = (static_values != null && i < static_values.size()) ?
                        static_values.get(i) : EncodedValue.defaultValue(id.getType());
            }
            out.add(FieldDef.of(id.getName(), id.getType(), access_flags,
                    hiddenapi.getAsInt(), initial_value, annotations_map.get(index)));
        }
        return out;
    }

    private CatchHandler readCatchHandler(RandomInput in) {
        int size = in.readSLeb128();
        int handlersCount = Math.abs(size);
        var handlers = new ArrayList<ExceptionHandler>(handlersCount);
        for (int i = 0; i < handlersCount; i++) {
            var type = getTypeId(in.readSmallULeb128());
            int address = in.readSmallULeb128();
            handlers.add(ExceptionHandler.of(type, address));
        }
        Integer catch_all_addr = null;
        if (size <= 0) {
            catch_all_addr = in.readSmallULeb128();
        }
        return new CatchHandler(handlers, catch_all_addr);
    }

    private TryBlock readTryBlock(RandomInput in, SparseArray<CatchHandler> handlers) {
        int start_addr = in.readSmallUInt(); // code units
        int unit_count = in.readUShort(); // code units

        int handler_off = in.readUShort();
        CatchHandler handler = handlers.get(handler_off);
        if (handler == null) {
            throw new IllegalStateException(
                    "Unable to find catch handler with offset " + handler_off);
        }
        return TryBlock.of(start_addr, unit_count, handler.elements(), handler.catch_all_addr());
    }

    private static final int kRegistersSizeShift = 12;
    private static final int kInsSizeShift = 8;
    private static final int kOutsSizeShift = 4;
    private static final int kTriesSizeSizeShift = 0;
    private static final int kInsnsSizeShift = 5;

    private static final int kFlagPreHeaderRegistersSize = 0b00001;
    private static final int kFlagPreHeaderInsSize = 0b00010;
    private static final int kFlagPreHeaderOutsSize = 0b00100;
    private static final int kFlagPreHeaderTriesSize = 0b01000;
    private static final int kFlagPreHeaderInsnsSize = 0b10000;

    private static int readUShortBackward(RandomInput in) {
        in.addPosition(-2);
        int out = in.readUShort();
        in.addPosition(-2);
        return out;
    }

    private CodeItem readCodeItem(int offset) {
        var in = dataAt(offset);

        int registers_size;
        int ins_size;
        int outs_size;
        int tries_size; // TODO
        int insns_count; // 2-byte code units

        if (isCompact()) {
            RandomInput preheader = in.duplicate();

            int fields = in.readUShort();
            int insns_count_and_flags = in.readUShort();

            insns_count = insns_count_and_flags >> kInsnsSizeShift;
            registers_size = (fields >> kRegistersSizeShift) & 0xF;
            ins_size = (fields >> kInsSizeShift) & 0xF;
            outs_size = (fields >> kOutsSizeShift) & 0xF;
            tries_size = (fields >> kTriesSizeSizeShift) & 0xF;

            if ((insns_count_and_flags & kFlagPreHeaderInsnsSize) != 0) {
                insns_count += readUShortBackward(preheader) +
                        (readUShortBackward(preheader) << 16);
            }
            if ((insns_count_and_flags & kFlagPreHeaderRegistersSize) != 0) {
                registers_size += readUShortBackward(preheader);
            }
            if ((insns_count_and_flags & kFlagPreHeaderInsSize) != 0) {
                ins_size += readUShortBackward(preheader);
            }
            if ((insns_count_and_flags & kFlagPreHeaderOutsSize) != 0) {
                outs_size += readUShortBackward(preheader);
            }
            if ((insns_count_and_flags & kFlagPreHeaderTriesSize) != 0) {
                tries_size += readUShortBackward(preheader);
            }

            registers_size += ins_size;
        } else {
            registers_size = in.readUShort();
            ins_size = in.readUShort();
            outs_size = in.readUShort();
            tries_size = in.readUShort();
            int debug_info_off = in.readSmallUInt(); // TODO
            insns_count = in.readSmallUInt();
        }

        var instructions = InstructionReader.readArray(this, in, insns_count);
        var tries = new ArrayList<TryBlock>(tries_size);

        if (tries_size > 0) {
            in.alignPosition(TRY_ITEM_ALIGNMENT);

            int tries_pos = in.position();
            in.addPosition(tries_size * TRY_ITEM_SIZE);

            int handlers_start = in.position();
            int handlers_size = in.readSmallULeb128();

            var handlers = new SparseArray<CatchHandler>(handlers_size);
            for (int i = 0; i < handlers_size; i++) {
                int handler_offset = in.position() - handlers_start;
                handlers.put(handler_offset, readCatchHandler(in));
            }

            in.position(tries_pos);
            for (int i = 0; i < tries_size; i++) {
                tries.add(readTryBlock(in, handlers));
            }
        }

        return new CodeItem(registers_size, ins_size,
                outs_size, instructions, tries);
    }

    public CodeItem getCodeItem(int offset) {
        return code_cache.apply(offset);
    }

    private MethodImplementation toImplementation(CodeItem code) {
        if (code == null) return null;
        return MethodImplementation.of(code.registers(),
                code.instructions(), code.tries(), null);
    }

    private List<Parameter> toParamaterList(
            List<TypeId> types, List<Set<Annotation>> annotations_map) {
        int types_size = types.size();
        int annotations_size = annotations_map.size();
        List<Parameter> out = new ArrayList<>(types.size());
        for (int i = 0; i < types_size; i++) {
            var type = types.get(i);
            String name = null; // TODO
            var annotations = i < annotations_size ? annotations_map.get(i) : null;
            out.add(Parameter.of(type, name, annotations));
        }
        return out;
    }

    private List<MethodDef> readMethodDefList(
            RandomInput in, int count, IntSupplier hiddenapi,
            SparseArray<Set<Annotation>> method_annotations,
            SparseArray<List<Set<Annotation>>> parameter_annotations) {
        List<MethodDef> out = new ArrayList<>(count);
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += in.readSmallULeb128();
            var id = getMethodId(index);
            int access_flags = in.readULeb128();
            int code_off = in.readSmallULeb128();
            List<Parameter> parameters = toParamaterList(id.getParameterTypes(),
                    parameter_annotations.get(index, List.of()));
            var code = code_off == NO_OFFSET ? null : getCodeItem(code_off);
            MethodImplementation implementation = toImplementation(code);
            out.add(MethodDef.of(id.getName(), id.getReturnType(),
                    parameters, access_flags, hiddenapi.getAsInt(),
                    implementation, method_annotations.get(index)));
        }
        return out;
    }

    private ClassDef readClassDef(int index, int offset) {
        var in = mainAt(offset);

        TypeId clazz = getTypeId(in.readSmallUInt());
        int access_flags = in.readInt();
        int superclass_idx = in.readSmallUInt();
        TypeId superclass = null;
        if (superclass_idx != DexConstants.NO_INDEX) {
            superclass = getTypeId(superclass_idx);
        }
        int interfaces_off = in.readSmallUInt();
        List<TypeId> interfaces = null;
        if (interfaces_off != NO_OFFSET) {
            interfaces = getTypeList(interfaces_off);
        }
        int source_file_idx = in.readSmallUInt();
        String source_file = null;
        if (source_file_idx != DexConstants.NO_INDEX) {
            source_file = getString(source_file_idx);
        }
        int annotations_off = in.readSmallUInt();
        AnnotationDirectory annotations = annotations_off == NO_OFFSET ?
                AnnotationDirectory.EMPTY : getAnnotationDirectory(annotations_off);
        int class_data_off = in.readSmallUInt();
        int static_values_off = in.readSmallUInt();
        List<EncodedValue> static_values = null;
        if (static_values_off != NO_OFFSET) {
            static_values = getEncodedArray(static_values_off).getValue();
        }

        List<FieldDef> static_fields = null;
        List<FieldDef> instance_fields = null;
        List<MethodDef> direct_methods = null;
        List<MethodDef> virtual_methods = null;

        if (class_data_off != NO_OFFSET) {
            RandomInput class_data = dataAt(class_data_off);
            int static_fields_size = class_data.readSmallULeb128();
            int instance_fields_size = class_data.readSmallULeb128();
            int direct_methods_size = class_data.readSmallULeb128();
            int virtual_methods_size = class_data.readSmallULeb128();
            var hiddenapi = getHiddenApiIterator(index);
            static_fields = readFieldDefList(class_data, static_fields_size,
                    hiddenapi, annotations.field_annotations(), static_values, true);
            instance_fields = readFieldDefList(class_data, instance_fields_size,
                    hiddenapi, annotations.field_annotations(), null, false);
            direct_methods = readMethodDefList(class_data, direct_methods_size, hiddenapi,
                    annotations.method_annotations(), annotations.parameter_annotations());
            virtual_methods = readMethodDefList(class_data, virtual_methods_size, hiddenapi,
                    annotations.method_annotations(), annotations.parameter_annotations());
        }

        return ClassDef.of(clazz, access_flags, superclass, interfaces, source_file,
                MemberUtils.mergeFields(static_fields, instance_fields),
                MemberUtils.mergeMethods(direct_methods, virtual_methods),
                annotations.class_annotations());
    }

    public List<ClassDef> getClasses() {
        return class_section;
    }

    public IntFunction<IntSupplier> readHiddenApiSection(int offset) {
        final IntSupplier zero = () -> 0;
        return offset == NO_OFFSET ? ignored -> zero : class_idx -> {
            var in = dataAt(offset);
            in.addPosition(Integer.BYTES); // section size
            in.addPosition(class_idx * Integer.BYTES);
            int flags_offset = in.readSmallUInt();
            if (flags_offset == NO_OFFSET) {
                return zero;
            }
            in = dataAt(offset + flags_offset);
            return in::readULeb128;
        };
    }

    public IntSupplier getHiddenApiIterator(int class_idx) {
        checkIndex(class_idx, class_section.size(), "class def");
        return hiddenapi_section.apply(class_idx);
    }
}
