package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.ACC_VISIBILITY_MASK;
import static com.v7878.dex.DexConstants.DBG_ADVANCE_LINE;
import static com.v7878.dex.DexConstants.DBG_ADVANCE_PC;
import static com.v7878.dex.DexConstants.DBG_END_LOCAL;
import static com.v7878.dex.DexConstants.DBG_END_SEQUENCE;
import static com.v7878.dex.DexConstants.DBG_FIRST_SPECIAL;
import static com.v7878.dex.DexConstants.DBG_LINE_BASE;
import static com.v7878.dex.DexConstants.DBG_LINE_RANGE;
import static com.v7878.dex.DexConstants.DBG_RESTART_LOCAL;
import static com.v7878.dex.DexConstants.DBG_SET_EPILOGUE_BEGIN;
import static com.v7878.dex.DexConstants.DBG_SET_FILE;
import static com.v7878.dex.DexConstants.DBG_SET_PROLOGUE_END;
import static com.v7878.dex.DexConstants.DBG_START_LOCAL;
import static com.v7878.dex.DexConstants.DBG_START_LOCAL_EXTENDED;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_BLOCKED;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_O;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_SDK;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_UNSUPPORTED;
import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.DexConstants.TYPE_CALL_SITE_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM;
import static com.v7878.dex.DexConstants.TYPE_METHOD_HANDLE_ITEM;
import static com.v7878.dex.DexIO.InvalidDexFile;
import static com.v7878.dex.DexIO.NotADexFile;
import static com.v7878.dex.DexOffsets.BASE_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.CALL_SITE_ID_SIZE;
import static com.v7878.dex.DexOffsets.CLASS_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.CLASS_START_OFFSET;
import static com.v7878.dex.DexOffsets.DATA_START_OFFSET;
import static com.v7878.dex.DexOffsets.DEBUG_INFO_BASE_OFFSET;
import static com.v7878.dex.DexOffsets.DEBUG_INFO_OFFSETS_POS_OFFSET;
import static com.v7878.dex.DexOffsets.DEBUG_INFO_OFFSETS_TABLE_OFFSET;
import static com.v7878.dex.DexOffsets.FIELD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.FIELD_START_OFFSET;
import static com.v7878.dex.DexOffsets.FILE_SIZE_OFFSET;
import static com.v7878.dex.DexOffsets.HEADER_OFF_OFFSET;
import static com.v7878.dex.DexOffsets.MAGIC_OFFSET;
import static com.v7878.dex.DexOffsets.MAP_OFFSET;
import static com.v7878.dex.DexOffsets.METHOD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.METHOD_HANDLE_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_START_OFFSET;
import static com.v7878.dex.DexOffsets.PROTO_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.PROTO_ID_SIZE;
import static com.v7878.dex.DexOffsets.PROTO_START_OFFSET;
import static com.v7878.dex.DexOffsets.STRING_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.STRING_ID_SIZE;
import static com.v7878.dex.DexOffsets.STRING_START_OFFSET;
import static com.v7878.dex.DexOffsets.TRY_ITEM_ALIGNMENT;
import static com.v7878.dex.DexOffsets.TRY_ITEM_SIZE;
import static com.v7878.dex.DexOffsets.TYPE_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.TYPE_ID_SIZE;
import static com.v7878.dex.DexOffsets.TYPE_START_OFFSET;
import static com.v7878.dex.DexOffsets.getHeaderSize;
import static com.v7878.dex.raw.CompactDexConstants.kDebugElementsPerIndex;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderInsSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderInsnsSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderOutsSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderRegistersSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderTriesSize;
import static com.v7878.dex.raw.CompactDexConstants.kInsSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kInsnsSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kOutsSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kRegistersSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kTriesSizeSizeShift;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.getSecondFlag;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kBlacklist;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kDarkGreylist;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kLightGreylist;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kWhitelist;
import static com.v7878.dex.util.AlignmentUtils.isPowerOfTwo;
import static com.v7878.dex.util.Exceptions.shouldNotReachHere;

import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.DexIO;
import com.v7878.dex.DexVersion;
import com.v7878.dex.MethodHandleType;
import com.v7878.dex.Opcodes;
import com.v7878.dex.ReadOptions;
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

public class DexReader implements DexIO.DexReaderCache {
    private record AnnotationDirectory(Set<Annotation> class_annotations,
                                       SparseArray<Set<Annotation>> field_annotations,
                                       SparseArray<Set<Annotation>> method_annotations,
                                       SparseArray<List<Set<Annotation>>> parameter_annotations) {
        public static AnnotationDirectory empty() {
            return new AnnotationDirectory(Set.of(), SparseArray.empty(),
                    SparseArray.empty(), SparseArray.empty());
        }
    }

    private record CodeItem(int registers, int ins, int outs, DebugInfo debug_info,
                            List<Instruction> instructions, List<TryBlock> tries) {
    }

    private record CompactData(int offsets_pos,
                               int offsets_table_offset,
                               int base) {
    }

    private final RandomInput main_buffer;
    private final RandomInput data_buffer;

    private final ReadOptions options;
    private final DexVersion version;
    private final Opcodes opcodes;

    private final IntFunction<List<TypeId>> typelist_cache;
    private final IntFunction<EncodedArray> encoded_array_cache;
    private final IntFunction<DebugInfo> debug_info_cache;
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

    private final int file_size;

    // Not null only for compact dex files
    private final CompactData compact_debug_info;

    public DexReader(ReadOptions options, RandomInput input, int header_offset) {
        assert input.position() == 0;
        options.validate();
        if (header_offset < 0) {
            throw new IllegalArgumentException("Negative header offset");
        }
        this.options = options;
        main_buffer = input.duplicate();

        if (main_buffer.size() < Math.addExact(header_offset, BASE_HEADER_SIZE)) {
            throw new NotADexFile("File is too short");
        }
        // TODO: add option? version = DexVersion.forMagic(mainAt(MAGIC_OFFSET).readLong());
        version = DexVersion.forMagic(mainAt(header_offset + MAGIC_OFFSET).readLong());
        // TODO: check dex version min api
        if (main_buffer.size() < Math.addExact(header_offset, getHeaderSize(version))) {
            throw new NotADexFile("File is too short");
        }

        // TODO: check endian tag

        file_size = mainAt(header_offset + FILE_SIZE_OFFSET).readSmallUInt();
        if (main_buffer.size() < Math.addExact(header_offset, file_size)) {
            throw new InvalidDexFile("Truncated dex file");
        }

        int container_off = 0;
        if (version.isDexContainer()) {
            container_off = mainAt(header_offset + HEADER_OFF_OFFSET).readSmallUInt();
        }
        if (container_off != header_offset) {
            throw new InvalidDexFile("Unexpected header offset " + container_off);
        }

        opcodes = Opcodes.of(version, options.getTargetApi(),
                options.isTargetForArt(), options.hasOdexInstructions());

        int data_off = 0;
        if (version.isCompact()) {
            data_off = mainAt(header_offset + DATA_START_OFFSET).readSmallUInt();
        }
        data_buffer = main_buffer.sliceAt(data_off);

        typelist_cache = makeOffsetCache(this::readTypeList);
        encoded_array_cache = makeOffsetCache(this::readEncodedArray);
        if (options.hasDebugInfo()) {
            debug_info_cache = makeOffsetCache(this::readDebugInfo);
        } else {
            debug_info_cache = null;
        }
        annotation_cache = makeOffsetCache(this::readAnnotation);
        annotation_list_cache = makeOffsetCache(this::readAnnotationList);
        annotation_set_list_cache = makeOffsetCache(this::readAnnotationSetList);
        annotation_directory_cache = makeOffsetCache(this::readAnnotationDirectory);
        code_cache = makeOffsetCache(this::readCodeItem);

        string_section = makeSection(
                mainAt(header_offset + STRING_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + STRING_START_OFFSET).readSmallUInt(),
                STRING_ID_SIZE, this::readString
        );
        type_section = makeSection(
                mainAt(header_offset + TYPE_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + TYPE_START_OFFSET).readSmallUInt(),
                TYPE_ID_SIZE, this::readTypeId
        );
        field_section = makeSection(
                mainAt(header_offset + FIELD_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + FIELD_START_OFFSET).readSmallUInt(),
                FIELD_ID_SIZE, this::readFieldId
        );
        proto_section = makeSection(
                mainAt(header_offset + PROTO_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + PROTO_START_OFFSET).readSmallUInt(),
                PROTO_ID_SIZE, this::readProtoId
        );
        method_section = makeSection(
                mainAt(header_offset + METHOD_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + METHOD_START_OFFSET).readSmallUInt(),
                METHOD_ID_SIZE, this::readMethodId
        );

        map_items = readMapItemsList(mainAt(header_offset + MAP_OFFSET).readSmallUInt());

        MapItem method_handles = getMapItemForSection(TYPE_METHOD_HANDLE_ITEM);
        method_handle_section = makeSection(
                method_handles != null ? method_handles.size() : 0,
                method_handles != null ? method_handles.offset() : NO_OFFSET,
                METHOD_HANDLE_ID_SIZE, this::readMethodHandleId
        );

        MapItem callsites = getMapItemForSection(TYPE_CALL_SITE_ID_ITEM);
        callsite_section = makeSection(
                callsites != null ? callsites.size() : 0,
                callsites != null ? callsites.offset() : NO_OFFSET,
                CALL_SITE_ID_SIZE, this::readCallSiteId
        );

        if (options.hasHiddenApiFlags()) {
            MapItem hiddenapi = getMapItemForSection(TYPE_HIDDENAPI_CLASS_DATA_ITEM);
            hiddenapi_section = readHiddenApiSection(
                    hiddenapi != null ? hiddenapi.offset() : NO_OFFSET);
        } else {
            hiddenapi_section = null;
        }

        class_section = makeSection(
                mainAt(header_offset + CLASS_COUNT_OFFSET).readSmallUInt(),
                mainAt(header_offset + CLASS_START_OFFSET).readSmallUInt(),
                CLASS_DEF_SIZE, this::readClassDef
        );

        if (isCompact() && options.hasDebugInfo()) {
            compact_debug_info = new CompactData(
                    mainAt(header_offset + DEBUG_INFO_OFFSETS_POS_OFFSET).readSmallUInt(),
                    mainAt(header_offset + DEBUG_INFO_OFFSETS_TABLE_OFFSET).readSmallUInt(),
                    mainAt(header_offset + DEBUG_INFO_BASE_OFFSET).readInt()
            );
        } else {
            compact_debug_info = null;
        }
    }

    public RandomInput mainAt(int offset) {
        return main_buffer.duplicateAt(offset);
    }

    public RandomInput dataAt(int offset) {
        return data_buffer.duplicateAt(offset);
    }

    public int getFileSize() {
        return file_size;
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

    public EncodedArray getEncodedArray(int offset) {
        return encoded_array_cache.apply(offset);
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
            Set<Annotation> annotations = annotations_off == NO_OFFSET ?
                    Set.of() : getAnnotationSet(annotations_off);
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
        int fields_size = in.readSmallUInt();
        int methods_size = in.readSmallUInt();
        int parameters_size = in.readSmallUInt();
        Set<Annotation> class_annotations = class_annotations_off == NO_OFFSET ?
                Set.of() : getAnnotationSet(class_annotations_off);
        SparseArray<Set<Annotation>> field_annotations = fields_size == 0 ?
                SparseArray.empty() : readAnnotationSetMap(in, fields_size);
        SparseArray<Set<Annotation>> method_annotations = methods_size == 0 ?
                SparseArray.empty() : readAnnotationSetMap(in, methods_size);
        SparseArray<List<Set<Annotation>>> parameter_annotations = parameters_size == 0 ?
                SparseArray.empty() : readAnnotationSetListMap(in, parameters_size);
        return new AnnotationDirectory(class_annotations, field_annotations,
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

    @Override
    public String getString(int index) {
        var section = string_section;
        checkIndex(index, section.size(), "string");
        return section.get(index);
    }

    private TypeId readTypeId(int index, int offset) {
        var descriptor = getString(mainAt(offset).readSmallUInt());
        return TypeId.of(descriptor);
    }

    @Override
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

    @Override
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

    @Override
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
        return MethodId.of(declaring_class, name, proto);
    }

    @Override
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

    @Override
    public MethodHandleId getMethodHandleId(int index) {
        var section = method_handle_section;
        checkIndex(index, section.size(), "method handle");
        return section.get(index);
    }

    private CallSiteId readCallSiteId(int index, int offset) {
        var in = mainAt(offset);
        var array = getEncodedArray(in.readSmallUInt()).getValue();
        if (array.size() < 3) {
            throw new InvalidDexFile("Invalid call site item: must contain at least 3 entries");
        }
        MethodHandleId handle;
        {
            var value = array.get(0);
            if (value.getValueType() != ValueType.METHOD_HANDLE) {
                throw new InvalidDexFile(String.format(
                        "Invalid encoded value type (%s) for the first item in call site %d",
                        value.getValueType(), index));
            }
            handle = ((EncodedMethodHandle) value).getValue();
        }
        String method_name;
        {
            var value = array.get(1);
            if (value.getValueType() != ValueType.STRING) {
                throw new InvalidDexFile(String.format(
                        "Invalid encoded value type (%s) for the second item in call site %d",
                        value.getValueType(), index));
            }
            method_name = ((EncodedString) value).getValue();
        }
        ProtoId proto;
        {
            var value = array.get(2);
            if (value.getValueType() != ValueType.METHOD_TYPE) {
                throw new InvalidDexFile(String.format(
                        "Invalid encoded value type (%s) for the third item in call site %d",
                        value.getValueType(), index));
            }
            proto = ((EncodedMethodType) value).getValue();
        }
        List<EncodedValue> extra_args;
        {
            extra_args = array.size() <= 3 ? List.of() : array.subList(3, array.size());
        }
        String name = String.format("callsite_%s", index);
        return CallSiteId.of(name, handle, method_name, proto, extra_args);
    }

    @Override
    public CallSiteId getCallSiteId(int index) {
        var section = callsite_section;
        checkIndex(index, section.size(), "callsite");
        return section.get(index);
    }

    public List<DebugItem> readDebugItemArray(
            RandomInput in, int line_start) {
        var out = new ArrayList<DebugItem>();
        int[] address = {0, 0};
        Runnable emit_address = () -> {
            if (address[0] != address[1]) {
                out.add(AdvancePC.of(address[0] - address[1]));
                address[1] = address[0];
            }
        };
        int[] line = {line_start, 0};
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
            case DBG_END_SEQUENCE -> false;
            case DBG_ADVANCE_PC -> {
                address[0] += in.readULeb128();
                yield true;
            }
            case DBG_ADVANCE_LINE -> {
                line[0] += in.readSLeb128();
                yield true;
            }
            case DBG_START_LOCAL, DBG_START_LOCAL_EXTENDED -> {
                int reg = in.readULeb128();
                int name_idx = in.readULeb128() - 1;
                String name = name_idx == NO_INDEX ?
                        null : getString(name_idx);
                int type_idx = in.readULeb128() - 1;
                TypeId type = type_idx == NO_INDEX ?
                        null : getTypeId(type_idx);
                int signature_idx = opcode == DBG_START_LOCAL ?
                        NO_INDEX : in.readULeb128() - 1;
                String signature = signature_idx == NO_INDEX ?
                        null : getString(signature_idx);
                emit_address.run();
                out.add(StartLocal.of(reg, name, type, signature));
                yield true;
            }
            case DBG_END_LOCAL -> {
                int reg = in.readULeb128();
                emit_address.run();
                out.add(EndLocal.of(reg));
                yield true;
            }
            case DBG_RESTART_LOCAL -> {
                int reg = in.readULeb128();
                emit_address.run();
                out.add(RestartLocal.of(reg));
                yield true;
            }
            case DBG_SET_PROLOGUE_END -> {
                emit_address.run();
                out.add(SetPrologueEnd.INSTANCE);
                yield true;
            }
            case DBG_SET_EPILOGUE_BEGIN -> {
                emit_address.run();
                out.add(SetEpilogueBegin.INSTANCE);
                yield true;
            }
            case DBG_SET_FILE -> {
                int name_idx = in.readULeb128() - 1;
                String name = name_idx == NO_INDEX ?
                        null : getString(name_idx);
                emit_address.run();
                out.add(SetFile.of(name));
                yield true;
            }
            default -> {
                int adjopcode = opcode - DBG_FIRST_SPECIAL;
                address[0] += adjopcode / DBG_LINE_RANGE;
                line[0] += DBG_LINE_BASE + (adjopcode % DBG_LINE_RANGE);
                emit_address.run();
                emit_line.run();
                yield true;
            }
        });
        return out;
    }

    public DebugInfo readDebugInfo(int offset) {
        var in = dataAt(offset);
        int line_start = in.readULeb128();
        List<String> parameter_names;
        {
            int parameters_size = in.readSmallULeb128();
            parameter_names = new ArrayList<>(parameters_size);
            for (int i = 0; i < parameters_size; i++) {
                int name_idx = in.readULeb128() - 1;
                parameter_names.add(name_idx == NO_INDEX ?
                        null : getString(name_idx));
            }
        }
        var items = readDebugItemArray(in, line_start);
        return new DebugInfo(parameter_names, items);
    }

    public DebugInfo getDebugInfo(int offset) {
        return debug_info_cache.apply(offset);
    }

    private long fixLegacyHiddenApiFlags(int access_flags) {
        int hiddenapi_flags = 0;
        // First bit
        {
            if (!isPowerOfTwo(access_flags & ACC_VISIBILITY_MASK)) {
                access_flags ^= ACC_VISIBILITY_MASK;
                hiddenapi_flags |= 0x1;
            }
        }
        // Second bit
        {
            int second_flag = getSecondFlag(access_flags);
            if ((access_flags & second_flag) != 0) {
                access_flags &= ~second_flag;
                hiddenapi_flags |= 0x2;
            }
        }
        hiddenapi_flags = switch (hiddenapi_flags) {
            case kWhitelist -> HIDDENAPI_FLAG_SDK;
            case kLightGreylist -> HIDDENAPI_FLAG_UNSUPPORTED;
            case kDarkGreylist -> HIDDENAPI_FLAG_MAX_TARGET_O;
            case kBlacklist -> HIDDENAPI_FLAG_BLOCKED;
            default -> throw shouldNotReachHere();
        };
        return access_flags & 0xffffffffL |
                (hiddenapi_flags & 0xffffffffL) << 32;
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
            int hiddenapi_flags;
            if (options.getTargetApi() == 28) {
                long common = fixLegacyHiddenApiFlags(access_flags);
                access_flags = (int) common;
                hiddenapi_flags = options.hasHiddenApiFlags() ?
                        (int) (common >> 32) : 0;
            } else {
                hiddenapi_flags = options.hasHiddenApiFlags() ?
                        hiddenapi.getAsInt() : 0;
            }
            EncodedValue initial_value = null;
            if (static_list) {
                initial_value = (static_values != null && i < static_values.size()) ?
                        static_values.get(i) : EncodedValue.defaultValue(id.getType());
            }
            out.add(FieldDef.of(id.getName(), id.getType(), access_flags,
                    hiddenapi_flags, initial_value, annotations_map.get(index)));
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
            throw new InvalidDexFile(
                    "Unable to find catch handler with offset " + handler_off);
        }
        return TryBlock.of(start_addr, unit_count, handler.catch_all_addr(), handler.elements());
    }

    private static int readUShortBackward(RandomInput in) {
        in.addPosition(-2);
        int out = in.readUShort();
        in.addPosition(-2);
        return out;
    }

    private CodeItem readCodeItem(int offset) {
        var in = dataAt(offset);

        DebugInfo debug_info;

        int registers_size;
        int ins_size;
        int outs_size;
        int tries_size;
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

            // In compact dex files, debug information is located in a separate table
            debug_info = null;
        } else {
            registers_size = in.readUShort();
            ins_size = in.readUShort();
            outs_size = in.readUShort();
            tries_size = in.readUShort();
            int debug_info_off = in.readSmallUInt();
            if (options.hasDebugInfo() && debug_info_off != NO_OFFSET) {
                debug_info = getDebugInfo(debug_info_off);
            } else {
                debug_info = null;
            }
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
                outs_size, debug_info, instructions, tries);
    }

    public CodeItem getCodeItem(int offset) {
        return code_cache.apply(offset);
    }

    private MethodImplementation toImplementation(CodeItem code, DebugInfo debug_info) {
        if (code == null) return null;
        return MethodImplementation.of(code.registers(), code.instructions(),
                code.tries(), debug_info == null ? null : debug_info.items());
    }

    private List<Parameter> toParamaterList(List<String> names, List<TypeId> types,
                                            List<Set<Annotation>> annotations_map) {
        int types_size = types.size();
        int annotations_size = annotations_map.size();
        List<Parameter> out = new ArrayList<>(types.size());
        for (int i = 0; i < types_size; i++) {
            var type = types.get(i);
            String name = (names == null || i >= names.size()) ? null : names.get(i);
            var annotations = i < annotations_size ? annotations_map.get(i) : null;
            out.add(Parameter.of(type, name, annotations));
        }
        return out;
    }

    // TODO: cache blocks?
    private int getDebugInfoOffset(int method_idx) {
        var info = compact_debug_info;
        assert isCompact() && info != null;

        int table_index = method_idx / kDebugElementsPerIndex;
        int bit_index = method_idx % kDebugElementsPerIndex;

        int table_offset = info.offsets_pos + info.offsets_table_offset;

        var in = dataAt(table_offset + table_index * 4);
        int block_offset = in.readSmallUInt();

        in = dataAt(info.offsets_pos + block_offset);
        int bit_mask = in.readUByte() << 8;
        bit_mask |= in.readUByte();

        if ((bit_mask & (1 << bit_index)) == 0) {
            return NO_OFFSET;
        }

        int offset_mask = 0xFFFF >> (16 - bit_index);
        int offset_count = Integer.bitCount(bit_mask & offset_mask);
        int debug_info_offset = info.base;
        for (int i = 0; i <= offset_count; i++) {
            debug_info_offset += in.readULeb128();
        }
        return debug_info_offset;
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
            int hiddenapi_flags;
            if (options.getTargetApi() == 28) {
                long common = fixLegacyHiddenApiFlags(access_flags);
                access_flags = (int) common;
                hiddenapi_flags = options.hasHiddenApiFlags() ?
                        (int) (common >> 32) : 0;
            } else {
                hiddenapi_flags = options.hasHiddenApiFlags() ?
                        hiddenapi.getAsInt() : 0;
            }
            int code_off = in.readSmallULeb128();
            var code = code_off == NO_OFFSET ? null : getCodeItem(code_off);
            DebugInfo debug_info;
            if (code == null || !options.hasDebugInfo()) {
                debug_info = null;
            } else if (isCompact()) {
                int debug_info_off = getDebugInfoOffset(index);
                debug_info = debug_info_off == NO_OFFSET ?
                        null : getDebugInfo(debug_info_off);
            } else {
                debug_info = code.debug_info();
            }
            List<Parameter> parameters = toParamaterList(
                    debug_info == null ? null : debug_info.parameter_names(),
                    id.getParameterTypes(), parameter_annotations.get(index, List.of()));
            MethodImplementation implementation = toImplementation(code, debug_info);
            out.add(MethodDef.of(id.getName(), id.getReturnType(),
                    parameters, access_flags, hiddenapi_flags,
                    implementation, method_annotations.get(index)));
        }
        return out;
    }

    private ClassDef readClassDef(int index, int offset) {
        var in = mainAt(offset);

        TypeId clazz = getTypeId(in.readSmallUInt());
        int access_flags = in.readInt();
        // TODO: readSmallUInt but with -1
        int superclass_idx = in.readInt();
        TypeId superclass = superclass_idx == NO_INDEX ?
                null : getTypeId(superclass_idx);
        int interfaces_off = in.readSmallUInt();
        List<TypeId> interfaces = interfaces_off == NO_OFFSET ?
                null : getTypeList(interfaces_off);
        // TODO: readSmallUInt but with -1
        int source_file_idx = in.readInt();
        String source_file = source_file_idx == NO_INDEX ?
                null : getString(source_file_idx);
        int annotations_off = in.readSmallUInt();
        AnnotationDirectory annotations = annotations_off == NO_OFFSET ?
                AnnotationDirectory.empty() : getAnnotationDirectory(annotations_off);
        int class_data_off = in.readSmallUInt();
        int static_values_off = in.readSmallUInt();
        List<EncodedValue> static_values = static_values_off == NO_OFFSET ?
                null : getEncodedArray(static_values_off).getValue();

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

    @Override
    public ClassDef getClassDef(int index) {
        var section = class_section;
        checkIndex(index, section.size(), "class");
        return section.get(index);
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
        return options.hasHiddenApiFlags() ? hiddenapi_section.apply(class_idx) : null;
    }
}
