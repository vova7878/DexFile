package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.ACC_OPTIMIZED;
import static com.v7878.dex.DexConstants.ACC_VERIFIED;
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
import static com.v7878.dex.DexConstants.ENDIAN_CONSTANT;
import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;
import static com.v7878.dex.DexConstants.REVERSE_ENDIAN_CONSTANT;
import static com.v7878.dex.DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM;
import static com.v7878.dex.DexConstants.TYPE_ANNOTATION_ITEM;
import static com.v7878.dex.DexConstants.TYPE_ANNOTATION_SET_ITEM;
import static com.v7878.dex.DexConstants.TYPE_ANNOTATION_SET_REF_LIST;
import static com.v7878.dex.DexConstants.TYPE_CALL_SITE_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_CLASS_DATA_ITEM;
import static com.v7878.dex.DexConstants.TYPE_CLASS_DEF_ITEM;
import static com.v7878.dex.DexConstants.TYPE_CODE_ITEM;
import static com.v7878.dex.DexConstants.TYPE_DEBUG_INFO_ITEM;
import static com.v7878.dex.DexConstants.TYPE_ENCODED_ARRAY_ITEM;
import static com.v7878.dex.DexConstants.TYPE_FIELD_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_HEADER_ITEM;
import static com.v7878.dex.DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM;
import static com.v7878.dex.DexConstants.TYPE_MAP_LIST;
import static com.v7878.dex.DexConstants.TYPE_METHOD_HANDLE_ITEM;
import static com.v7878.dex.DexConstants.TYPE_METHOD_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_PROTO_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_STRING_DATA_ITEM;
import static com.v7878.dex.DexConstants.TYPE_STRING_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_TYPE_ID_ITEM;
import static com.v7878.dex.DexConstants.TYPE_TYPE_LIST;
import static com.v7878.dex.DexIO.ClassHeader;
import static com.v7878.dex.DexIO.DexReaderCache;
import static com.v7878.dex.DexOffsets.BASE_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.CALL_SITE_ID_SIZE;
import static com.v7878.dex.DexOffsets.CLASS_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.CLASS_START_OFFSET;
import static com.v7878.dex.DexOffsets.CONTAINER_SIZE_OFFSET;
import static com.v7878.dex.DexOffsets.DATA_SIZE_OFFSET;
import static com.v7878.dex.DexOffsets.DATA_START_OFFSET;
import static com.v7878.dex.DexOffsets.DEBUG_INFO_BASE_OFFSET;
import static com.v7878.dex.DexOffsets.DEBUG_INFO_OFFSETS_POS_OFFSET;
import static com.v7878.dex.DexOffsets.DEBUG_INFO_OFFSETS_TABLE_OFFSET;
import static com.v7878.dex.DexOffsets.ENDIAN_TAG_OFFSET;
import static com.v7878.dex.DexOffsets.FIELD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.FIELD_START_OFFSET;
import static com.v7878.dex.DexOffsets.FILE_SIZE_OFFSET;
import static com.v7878.dex.DexOffsets.HEADER_OFF_OFFSET;
import static com.v7878.dex.DexOffsets.HEADER_SIZE_OFFSET;
import static com.v7878.dex.DexOffsets.LINK_SIZE_OFFSET;
import static com.v7878.dex.DexOffsets.LINK_START_OFFSET;
import static com.v7878.dex.DexOffsets.M3_CLASS_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M3_CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.M3_CLASS_START_OFFSET;
import static com.v7878.dex.DexOffsets.M3_FIELD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M3_FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.M3_FIELD_START_OFFSET;
import static com.v7878.dex.DexOffsets.M3_METHOD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M3_METHOD_ID_SIZE;
import static com.v7878.dex.DexOffsets.M3_METHOD_START_OFFSET;
import static com.v7878.dex.DexOffsets.M3_STRING_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M3_STRING_ID_SIZE;
import static com.v7878.dex.DexOffsets.M3_STRING_START_OFFSET;
import static com.v7878.dex.DexOffsets.M3_TYPE_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M3_TYPE_START_OFFSET;
import static com.v7878.dex.DexOffsets.M5_CLASS_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M5_CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.M5_CLASS_START_OFFSET;
import static com.v7878.dex.DexOffsets.M5_FIELD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M5_FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.M5_FIELD_START_OFFSET;
import static com.v7878.dex.DexOffsets.M5_METHOD_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M5_METHOD_ID_SIZE;
import static com.v7878.dex.DexOffsets.M5_METHOD_START_OFFSET;
import static com.v7878.dex.DexOffsets.M5_STRING_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M5_STRING_ID_SIZE;
import static com.v7878.dex.DexOffsets.M5_STRING_START_OFFSET;
import static com.v7878.dex.DexOffsets.M5_TYPE_COUNT_OFFSET;
import static com.v7878.dex.DexOffsets.M5_TYPE_START_OFFSET;
import static com.v7878.dex.DexOffsets.MAGIC_OFFSET;
import static com.v7878.dex.DexOffsets.MAP_ITEM_SIZE;
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
import static com.v7878.dex.DexOffsets.headerSize;
import static com.v7878.dex.DexVersion.DEX009;
import static com.v7878.dex.DexVersion.DEX013;
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
import static com.v7878.dex.util.Checks.checkIndex;

import com.v7878.collections.IntMap;
import com.v7878.dex.AnnotationVisibility;
import com.v7878.dex.DexIO.InvalidDexFile;
import com.v7878.dex.DexIO.NotADexFile;
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
import com.v7878.dex.raw.legacy.Dex009;
import com.v7878.dex.raw.legacy.Dex013;
import com.v7878.dex.util.CachedFixedSizeList;
import com.v7878.dex.util.MemberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public final class DexReader implements DexReaderCache {
    private record AnnotationDirectory(NavigableSet<Annotation> class_annotations,
                                       IntMap<NavigableSet<Annotation>> field_annotations,
                                       IntMap<NavigableSet<Annotation>> method_annotations,
                                       IntMap<List<NavigableSet<Annotation>>> parameter_annotations) {
        public static final AnnotationDirectory EMPTY = new AnnotationDirectory(
                Collections.emptyNavigableSet(), IntMap.empty(), IntMap.empty(), IntMap.empty());
    }

    public record CodeItem(int registers, int ins, int outs, DebugInfo debug_info,
                           List<Instruction> instructions, NavigableSet<TryBlock> tries) {
        public static final CodeItem EMPTY = new CodeItem(255, 0, 0, null,
                Collections.emptyList(), Collections.emptyNavigableSet());
    }

    public record ClassDefHeader(
            TypeId type, int access_flags,
            TypeId superclass, List<TypeId> interfaces,
            String source_file, AnnotationDirectory annotations,
            int class_data_off, List<EncodedValue> static_values
    ) implements ClassHeader {
        @Override
        public TypeId getType() {
            return type;
        }

        @Override
        public int getAccessFlags() {
            return access_flags;
        }

        @Override
        public TypeId getSuperclass() {
            return superclass;
        }

        @Override
        public List<TypeId> getInterfaces() {
            return interfaces;
        }

        @Override
        public NavigableSet<Annotation> getAnnotations() {
            return annotations.class_annotations();
        }
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
    private final IntFunction<NavigableSet<Annotation>> annotation_set_cache;
    private final IntFunction<List<NavigableSet<Annotation>>> annotation_set_list_cache;
    private final IntFunction<AnnotationDirectory> annotation_directory_cache;
    private final IntFunction<CodeItem> code_cache;

    private final List<String> string_section;
    private final List<TypeId> type_section;
    private final List<FieldId> field_section;
    private final List<ProtoId> proto_section;
    private final List<MethodId> method_section;
    private final List<MethodHandleId> method_handle_section;
    private final List<CallSiteId> callsite_section;
    private final List<ClassDefHeader> class_header_section;
    private final List<ClassDef> class_section;

    private final List<MapItem> map_items;

    private final IntFunction<IntSupplier> hiddenapi_section;

    private final int file_size;

    // Not null only for compact dex files
    private final CompactData compact_debug_info;

    public DexReader(ReadOptions options, RandomInput input, int header_offset, int content_offset) {
        assert input.position() == 0;
        options.validate();
        if (header_offset < 0) {
            throw new IllegalArgumentException("Negative header offset");
        }
        this.options = options;
        main_buffer = input.duplicate();

        if (main_buffer.size() < Math.addExact(header_offset, BASE_HEADER_SIZE)) {
            throw new NotADexFile(String.format(
                    "File is too short: %d bytes, need at least %d for a dex header",
                    main_buffer.size(), header_offset + BASE_HEADER_SIZE));
        }
        long magic = mainAt(header_offset + MAGIC_OFFSET).readLong();
        try {
            version = DexVersion.forMagic(magic);
        } catch (IllegalArgumentException e) {
            throw new NotADexFile(e.getMessage(), e);
        }
        version.checkApi(options.getTargetApi());
        int header_size = headerSize(version);
        if (main_buffer.size() < Math.addExact(header_offset, header_size)) {
            throw new NotADexFile(String.format(
                    "File is too short: %d bytes, need at least %d for a %s header",
                    main_buffer.size(), header_offset + header_size, version));
        }

        if (version != DEX013 && version != DEX009) {
            int endian_tag = mainAt(header_offset + ENDIAN_TAG_OFFSET).readInt();
            switch (endian_tag) {
                case ENDIAN_CONSTANT -> { /* ok */ }
                case REVERSE_ENDIAN_CONSTANT -> throw new InvalidDexFile(
                        // The internet says that such dex`s never existed,
                        // but I think that odex on BE machines contained exactly this format
                        "Big endian dex files are not supported");
                default -> throw new InvalidDexFile(String.format(
                        "Invalid endian tag: 0x%x", endian_tag));
            }
            int header_size_field = mainAt(header_offset + HEADER_SIZE_OFFSET).readSmallUInt();
            if (header_size_field != header_size) {
                throw new InvalidDexFile(String.format(
                        "Invalid header size: %d, expected %d for %s",
                        header_size_field, header_size, version));
            }
        }

        file_size = mainAt(header_offset + FILE_SIZE_OFFSET).readSmallUInt();
        if (file_size < header_size) {
            throw new InvalidDexFile(String.format(
                    "Invalid file size: %d. Must be at least the %s header size (%d)",
                    file_size, version, header_size));
        }
        if (file_size > main_buffer.size() - header_offset) {
            throw new InvalidDexFile(String.format(
                    "Truncated dex file: file size %d, available %d",
                    file_size, main_buffer.size() - header_offset));
        }

        int container_off = 0;
        int container_size = file_size;
        if (version.isDexContainer()) {
            container_off = mainAt(header_offset + HEADER_OFF_OFFSET).readSmallUInt();
            container_size = mainAt(header_offset + CONTAINER_SIZE_OFFSET).readSmallUInt();
        }
        if (container_off != header_offset) {
            throw new InvalidDexFile("Unexpected header offset " + container_off);
        }
        if (main_buffer.size() < container_size) {
            throw new InvalidDexFile(String.format(
                    "Truncated dex container: size %d, available %d",
                    container_size, main_buffer.size()));
        }

        // The link section must be zero or in-bounds
        if (version != DEX013 && version != DEX009) {
            checkHeaderSection("link",
                    mainAt(header_offset + LINK_START_OFFSET).readSmallUInt(),
                    mainAt(header_offset + LINK_SIZE_OFFSET).readSmallUInt(),
                    header_offset, header_size);
            // TODO?: check link for dex009 / dex013
        }
        if (!version.isCompact() && !version.isDexContainer()
                && version != DEX013 && version != DEX009) {
            checkHeaderSection("data",
                    mainAt(header_offset + DATA_START_OFFSET).readSmallUInt(),
                    mainAt(header_offset + DATA_SIZE_OFFSET).readSmallUInt(),
                    header_offset, header_size);
        }

        opcodes = Opcodes.of(version, options);

        int data_off = content_offset;
        if (version.isCompact()) {
            data_off += mainAt(header_offset + DATA_START_OFFSET).readSmallUInt();
        }
        data_buffer = main_buffer.duplicateAt(data_off).markAsStart();

        typelist_cache = makeOffsetCache(this::readTypeList);
        encoded_array_cache = makeOffsetCache(this::readEncodedArray);
        if (options.hasDebugInfo()) {
            debug_info_cache = makeOffsetCache(this::readDebugInfo);
        } else {
            debug_info_cache = null;
        }
        annotation_cache = makeOffsetCache(this::readAnnotation);
        annotation_set_cache = makeOffsetCache(this::readAnnotationSet);
        annotation_set_list_cache = makeOffsetCache(this::readAnnotationSetList);
        annotation_directory_cache = makeOffsetCache(this::readAnnotationDirectory);

        switch (version) {
            case DEX009 -> {
                code_cache = null;
                string_section = makeSection("string_ids",
                        mainAt(header_offset + M3_STRING_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M3_STRING_START_OFFSET).readSmallUInt(),
                        M3_STRING_ID_SIZE, this::readString
                );
                type_section = makeSection("type_ids",
                        mainAt(header_offset + M3_TYPE_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M3_TYPE_START_OFFSET).readSmallUInt(),
                        TYPE_ID_SIZE, this::readTypeId
                );
                field_section = makeSection("field_ids",
                        mainAt(header_offset + M3_FIELD_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M3_FIELD_START_OFFSET).readSmallUInt(),
                        M3_FIELD_ID_SIZE, this::readFieldId
                );
                proto_section = Collections.emptyList();
                method_section = makeSection("method_ids",
                        mainAt(header_offset + M3_METHOD_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M3_METHOD_START_OFFSET).readSmallUInt(),
                        M3_METHOD_ID_SIZE, this::readMethodId
                );
                class_header_section = Collections.emptyList();
                class_section = makeSection("class_defs",
                        mainAt(header_offset + M3_CLASS_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M3_CLASS_START_OFFSET).readSmallUInt(),
                        M3_CLASS_DEF_SIZE, this::readClassDef
                );
            }
            case DEX013 -> {
                code_cache = null;
                string_section = makeSection("string_ids",
                        mainAt(header_offset + M5_STRING_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M5_STRING_START_OFFSET).readSmallUInt(),
                        M5_STRING_ID_SIZE, this::readString
                );
                type_section = makeSection("type_ids",
                        mainAt(header_offset + M5_TYPE_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M5_TYPE_START_OFFSET).readSmallUInt(),
                        TYPE_ID_SIZE, this::readTypeId
                );
                field_section = makeSection("field_ids",
                        mainAt(header_offset + M5_FIELD_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M5_FIELD_START_OFFSET).readSmallUInt(),
                        M5_FIELD_ID_SIZE, this::readFieldId
                );
                proto_section = Collections.emptyList();
                method_section = makeSection("method_ids",
                        mainAt(header_offset + M5_METHOD_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M5_METHOD_START_OFFSET).readSmallUInt(),
                        M5_METHOD_ID_SIZE, this::readMethodId
                );
                class_header_section = Collections.emptyList();
                class_section = makeSection("class_defs",
                        mainAt(header_offset + M5_CLASS_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + M5_CLASS_START_OFFSET).readSmallUInt(),
                        M5_CLASS_DEF_SIZE, this::readClassDef
                );
            }
            default -> {
                code_cache = makeOffsetCache(this::readCodeItem);
                string_section = makeSection("string_ids",
                        mainAt(header_offset + STRING_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + STRING_START_OFFSET).readSmallUInt(),
                        STRING_ID_SIZE, this::readString
                );
                type_section = makeSection("type_ids",
                        mainAt(header_offset + TYPE_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + TYPE_START_OFFSET).readSmallUInt(),
                        TYPE_ID_SIZE, this::readTypeId
                );
                field_section = makeSection("field_ids",
                        mainAt(header_offset + FIELD_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + FIELD_START_OFFSET).readSmallUInt(),
                        FIELD_ID_SIZE, this::readFieldId
                );
                proto_section = makeSection("proto_ids",
                        mainAt(header_offset + PROTO_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + PROTO_START_OFFSET).readSmallUInt(),
                        PROTO_ID_SIZE, this::readProtoId
                );
                method_section = makeSection("method_ids",
                        mainAt(header_offset + METHOD_COUNT_OFFSET).readSmallUInt(),
                        mainAt(header_offset + METHOD_START_OFFSET).readSmallUInt(),
                        METHOD_ID_SIZE, this::readMethodId
                );
                var class_count = mainAt(header_offset + CLASS_COUNT_OFFSET).readSmallUInt();
                var class_offset = mainAt(header_offset + CLASS_START_OFFSET).readSmallUInt();
                class_header_section = makeSection("class_defs",
                        class_count, class_offset,
                        CLASS_DEF_SIZE, this::readClassDefHeader
                );
                class_section = makeSection("class_defs",
                        class_count, class_offset,
                        CLASS_DEF_SIZE, this::readClassDef
                );
            }
        }

        if (version == DEX013 || version == DEX009) {
            map_items = Collections.emptyList();
            method_handle_section = Collections.emptyList();
            callsite_section = Collections.emptyList();
        } else {
            map_items = readMapItemsList(mainAt(header_offset + MAP_OFFSET).readSmallUInt());

            // Method handles and call sites were added to the format later,
            // so the header has no fields for them: they are part of the
            // main section but are located through map_items only
            MapItem method_handles = getMapItemForSection(TYPE_METHOD_HANDLE_ITEM);
            method_handle_section = makeSection("method_handles",
                    method_handles != null ? method_handles.size() : 0,
                    method_handles != null ? method_handles.offset() : NO_OFFSET,
                    METHOD_HANDLE_ID_SIZE, this::readMethodHandleId
            );

            MapItem callsites = getMapItemForSection(TYPE_CALL_SITE_ID_ITEM);
            callsite_section = makeSection("call_site_ids",
                    callsites != null ? callsites.size() : 0,
                    callsites != null ? callsites.offset() : NO_OFFSET,
                    CALL_SITE_ID_SIZE, this::readCallSiteId
            );
        }

        if (options.hasHiddenApiFlags()) {
            MapItem hiddenapi = getMapItemForSection(TYPE_HIDDENAPI_CLASS_DATA_ITEM);
            hiddenapi_section = readHiddenApiSection(
                    hiddenapi != null ? hiddenapi.offset() : NO_OFFSET);
        } else {
            hiddenapi_section = null;
        }

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

    /**
     * Checks that a header-announced section is zero or in-bounds: a zero
     * size requires a zero offset, a non-zero offset must lie after the
     * header, and the whole section must fit into the available data
     */
    private void checkHeaderSection(String name, int offset, int size,
                                    int header_offset, int header_size) {
        if (size == 0) {
            if (offset != 0) {
                throw new IllegalStateException(String.format(
                        "Unexpected %s section offset 0x%x for an empty section",
                        name, offset));
            }
            return;
        }
        if (offset < header_offset + header_size) {
            throw new IllegalStateException(String.format(
                    "%s section offset 0x%x is inside the header", name, offset));
        }
        if (offset + (long) size > main_buffer.size()) {
            throw new IllegalStateException(String.format(
                    "%s section is out of bounds: offset 0x%x, size %d, available %d",
                    name, offset, size, main_buffer.size() - offset));
        }
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

    @Override
    public DexVersion version() {
        return version;
    }

    public boolean isCompact() {
        return version.isCompact();
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
        if (map_off >= data_buffer.size()) {
            throw new IllegalStateException(String.format(
                    "map_list offset 0x%x is out of bounds, available %d",
                    map_off, data_buffer.size()));
        }
        var in = dataAt(map_off);
        int map_size = in.readSmallUInt();
        if (map_size * (long) MAP_ITEM_SIZE > in.available()) {
            throw new IllegalStateException(String.format(
                    "map_list is too large: %d entries at offset 0x%x",
                    map_size, map_off));
        }
        var out = new ArrayList<MapItem>(map_size);
        for (int i = 0; i < map_size; i++) {
            var item = readMapItem(in);
            checkMapItem(item, i);
            out.add(i, item);
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * Checks one map entry: the type code must be known and the section it
     * announces must fit into the data the reader can address. Id sections
     * and the header are addressed from the dex start, data sections from
     * the data section start.
     */
    private void checkMapItem(MapItem item, int index) {
        if (item.size() == 0) {
            // nothing is dereferenced through a zero-sized entry
            return;
        }
        long entry_bytes = mapEntryBytes(item.type(), item.size(), index);
        int bound = isMainRelativeMapSection(item.type())
                ? main_buffer.size() : data_buffer.size();
        if (item.offset() >= bound) {
            throw new IllegalStateException(String.format(
                    "Map item %d (type 0x%x) offset 0x%x is out of bounds, available %d",
                    index, item.type(), item.offset(), bound));
        }
        if (item.offset() + entry_bytes > bound) {
            throw new IllegalStateException(String.format(
                    "Map item %d (type 0x%x) section is out of bounds: "
                            + "offset 0x%x, size %d, available %d",
                    index, item.type(), item.offset(), item.size(),
                    bound - item.offset()));
        }
    }

    /**
     * Returns the number of bytes a map entry claims: fixed element sizes
     * for the id sections, the 4+n list forms, and 0 for sections whose
     * item sizes are implicit (only their offset is checked).
     */
    private static long mapEntryBytes(int type, int size, int index) {
        return switch (type) {
            case TYPE_STRING_ID_ITEM -> size * (long) STRING_ID_SIZE;
            case TYPE_TYPE_ID_ITEM -> size * (long) TYPE_ID_SIZE;
            case TYPE_PROTO_ID_ITEM -> size * (long) PROTO_ID_SIZE;
            case TYPE_FIELD_ID_ITEM -> size * (long) FIELD_ID_SIZE;
            case TYPE_METHOD_ID_ITEM -> size * (long) METHOD_ID_SIZE;
            case TYPE_CLASS_DEF_ITEM -> size * (long) CLASS_DEF_SIZE;
            case TYPE_CALL_SITE_ID_ITEM -> size * (long) CALL_SITE_ID_SIZE;
            case TYPE_METHOD_HANDLE_ITEM -> size * (long) METHOD_HANDLE_ID_SIZE;
            case TYPE_TYPE_LIST -> 4 + size * 2L;
            case TYPE_ANNOTATION_SET_REF_LIST, TYPE_ANNOTATION_SET_ITEM -> 4 + size * 4L;
            case TYPE_HEADER_ITEM, TYPE_MAP_LIST, TYPE_CLASS_DATA_ITEM,
                 TYPE_CODE_ITEM, TYPE_STRING_DATA_ITEM, TYPE_DEBUG_INFO_ITEM,
                 TYPE_ANNOTATION_ITEM, TYPE_ENCODED_ARRAY_ITEM,
                 TYPE_ANNOTATIONS_DIRECTORY_ITEM,
                 TYPE_HIDDENAPI_CLASS_DATA_ITEM -> 0;
            default -> throw new IllegalStateException(String.format(
                    "Unknown map section type 0x%x at index %d", type, index));
        };
    }

    private static boolean isMainRelativeMapSection(int type) {
        return switch (type) {
            case TYPE_HEADER_ITEM, TYPE_STRING_ID_ITEM, TYPE_TYPE_ID_ITEM,
                 TYPE_PROTO_ID_ITEM, TYPE_FIELD_ID_ITEM, TYPE_METHOD_ID_ITEM,
                 TYPE_CLASS_DEF_ITEM, TYPE_CALL_SITE_ID_ITEM,
                 TYPE_METHOD_HANDLE_ITEM -> true;
            default -> false;
        };
    }

    @Override
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
        var cache = new IntMap<T>();
        return offset -> {
            var out = cache.get(offset);
            if (out != null) return out;
            out = reader.apply(offset);
            cache.put(offset, out);
            return out;
        };
    }

    private List<TypeId> readTypeList(int offset) {
        if (version == DEX013) {
            return Dex013.readTypeList(this, offset);
        }
        if (version == DEX009) {
            return Dex009.readTypeList(this, offset);
        }
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<TypeId>(size);
        for (int i = 0; i < size; i++) {
            out.add(i, getType(in.readUShort()));
        }
        return Collections.unmodifiableList(out);
    }

    public List<TypeId> getTypeList(int offset) {
        return typelist_cache.apply(offset);
    }

    private EncodedArray readEncodedArray(RandomInput in) {
        int size = in.readSmallULeb128();
        List<EncodedValue> value = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            value.add(i, readEncodedValue(in));
        }
        value = Collections.unmodifiableList(value);
        return EncodedArray.raw(value);
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
        TypeId type = getType(in.readSmallULeb128());
        int size = in.readSmallULeb128();
        NavigableSet<AnnotationElement> elements = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            MemberUtils.addAnnotationElement(
                    elements, readAnnotationElement(in));
        }
        elements = Collections.unmodifiableNavigableSet(elements);
        return EncodedAnnotation.raw(type, elements);
    }

    private EncodedValue readEncodedValue(RandomInput in) {
        int type_and_arg = in.readUByte();
        int arg = (type_and_arg & 0xe0) >> 5;
        int int_type = type_and_arg & 0x1f;
        var type = ValueType.of(int_type);
        checkEncodedValueArg(type, arg, in.position() - 1);
        return switch (type) {
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
            case TYPE -> EncodedType.of(getType(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case FIELD -> EncodedField.of(getField(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case ENUM -> EncodedEnum.of(getField(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case METHOD -> EncodedMethod.of(getMethod(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case METHOD_TYPE -> EncodedMethodType.of(getProto(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case METHOD_HANDLE -> EncodedMethodHandle.of(getMethodHandle(
                    ValueCoder.readUnsignedInt(in, arg, false)));
            case ARRAY -> readEncodedArray(in);
            case ANNOTATION -> readEncodedAnnotation(in);
        };
    }

    /**
     * The 3-bit value_arg is the width of the payload minus one, so its
     * range depends on the value type (CheckEncodedValue in the verifier).
     * An out of range arg would make the decoder read extra bytes and
     * shift by a negative count, so it is a content error instead.
     */
    private static void checkEncodedValueArg(ValueType type, int arg, int position) {
        boolean valid = switch (type) {
            case BYTE, ARRAY, ANNOTATION, NULL -> arg == 0;
            case SHORT, CHAR, BOOLEAN -> arg <= 1;
            case INT, FLOAT, STRING, TYPE, FIELD, ENUM, METHOD, METHOD_TYPE,
                 METHOD_HANDLE -> arg <= 3;
            case LONG, DOUBLE -> true;
        };
        if (!valid) {
            throw new IllegalStateException(String.format(
                    "Bad encoded_value arg %d for %s at position %d",
                    arg, type, position));
        }
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

    private NavigableSet<Annotation> readAnnotationSet(int offset) {
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new TreeSet<Annotation>();
        for (int i = 0; i < size; i++) {
            MemberUtils.addAnnotation(
                    out, getAnnotation(in.readSmallUInt()));
        }
        return Collections.unmodifiableNavigableSet(out);
    }

    public NavigableSet<Annotation> getAnnotationSet(int offset) {
        return annotation_set_cache.apply(offset);
    }

    private List<NavigableSet<Annotation>> readAnnotationSetList(int offset) {
        var in = dataAt(offset);
        int size = in.readSmallUInt();
        var out = new ArrayList<NavigableSet<Annotation>>(size);
        for (int i = 0; i < size; i++) {
            int annotations_off = in.readSmallUInt();
            NavigableSet<Annotation> annotations = annotations_off == NO_OFFSET ?
                    Collections.emptyNavigableSet() : getAnnotationSet(annotations_off);
            out.add(i, annotations);
        }
        return Collections.unmodifiableList(out);
    }

    public List<NavigableSet<Annotation>> getAnnotationSetList(int offset) {
        return annotation_set_list_cache.apply(offset);
    }

    private IntMap<NavigableSet<Annotation>> readAnnotationSetMap(RandomInput in, int size) {
        var out = new IntMap<NavigableSet<Annotation>>(size);
        for (int i = 0; i < size; i++) {
            int id = in.readSmallUInt();
            var set = getAnnotationSet(in.readSmallUInt());
            out.put(id, set);
        }
        return out;
    }

    private IntMap<List<NavigableSet<Annotation>>> readAnnotationSetListMap(RandomInput in, int size) {
        var out = new IntMap<List<NavigableSet<Annotation>>>(size);
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
        NavigableSet<Annotation> class_annotations = class_annotations_off == NO_OFFSET ?
                Collections.emptyNavigableSet() : getAnnotationSet(class_annotations_off);
        IntMap<NavigableSet<Annotation>> field_annotations = fields_size == 0 ?
                IntMap.empty() : readAnnotationSetMap(in, fields_size);
        IntMap<NavigableSet<Annotation>> method_annotations = methods_size == 0 ?
                IntMap.empty() : readAnnotationSetMap(in, methods_size);
        IntMap<List<NavigableSet<Annotation>>> parameter_annotations = parameters_size == 0 ?
                IntMap.empty() : readAnnotationSetListMap(in, parameters_size);
        return new AnnotationDirectory(class_annotations, field_annotations,
                method_annotations, parameter_annotations);
    }

    public AnnotationDirectory getAnnotationDirectory(int offset) {
        return annotation_directory_cache.apply(offset);
    }

    private interface SectionReader<T> {
        T read(int index, int offset);
    }

    private <T> List<T> makeSection(String name, int section_size, int offset,
                                    int element_size, SectionReader<T> reader) {
        if (section_size == 0) return List.of();
        long section_bytes = section_size * (long) element_size;
        if (offset + section_bytes > main_buffer.size()) {
            throw new IllegalStateException(String.format(
                    "%s section is out of bounds: offset 0x%x, size %d, available %d",
                    name, offset, section_size, main_buffer.size() - offset));
        }
        return new CachedFixedSizeList<>(section_size) {
            @Override
            protected T compute(int index) {
                try {
                    return reader.read(index, offset + index * element_size);
                } catch (InvalidDexFile | NotADexFile e) {
                    throw e;
                } catch (RuntimeException e) {
                    throw new InvalidDexFile(String.format(
                            "Failed to read %s item %d", name, index), e);
                }
            }
        };
    }

    private String readString(int index, int offset) {
        if (version == DEX013) {
            return Dex013.readString(this, index, offset);
        }
        if (version == DEX009) {
            return Dex009.readString(this, index, offset);
        }
        var in = mainAt(offset);
        int data_offset = in.readSmallUInt();
        return dataAt(data_offset).readMUTF8();
    }

    @Override
    public List<String> getStrings() {
        return string_section;
    }

    private TypeId readTypeId(int index, int offset) {
        if (version == DEX009) {
            return Dex009.readTypeId(this, index, offset);
        }
        var descriptor = getString(mainAt(offset).readSmallUInt());
        return TypeId.of(descriptor);
    }

    @Override
    public List<TypeId> getTypes() {
        return type_section;
    }

    private FieldId readFieldId(int index, int offset) {
        if (version == DEX013) {
            return Dex013.readFieldId(this, index, offset);
        }
        if (version == DEX009) {
            return Dex009.readFieldId(this, index, offset);
        }
        var in = mainAt(offset);
        var declaring_class = getType(in.readUShort());
        var type = getType(in.readUShort());
        var name = getString(in.readSmallUInt());
        return FieldId.of(declaring_class, name, type);
    }

    @Override
    public List<FieldId> getFields() {
        return field_section;
    }

    private ProtoId readProtoId(int index, int offset) {
        var in = mainAt(offset);
        in.readSmallUInt(); // shorty
        var return_type = getType(in.readSmallUInt());
        int parameters_off = in.readSmallUInt();
        var parameters = parameters_off == NO_OFFSET ?
                List.<TypeId>of() : getTypeList(parameters_off);
        return ProtoId.raw(return_type, parameters);
    }

    @Override
    public List<ProtoId> getProtos() {
        return proto_section;
    }

    private MethodId readMethodId(int index, int offset) {
        if (version == DEX013) {
            return Dex013.readMethodId(this, index, offset);
        }
        if (version == DEX009) {
            return Dex009.readMethodId(this, index, offset);
        }
        var in = mainAt(offset);
        var declaring_class = getType(in.readUShort());
        var proto = getProto(in.readUShort());
        var name = getString(in.readSmallUInt());
        return MethodId.of(declaring_class, name, proto);
    }

    @Override
    public List<MethodId> getMethods() {
        return method_section;
    }

    private MethodHandleId readMethodHandleId(int index, int offset) {
        var in = mainAt(offset);
        MethodHandleType type = MethodHandleType.of(in.readUShort());
        in.readUShort(); // padding
        int member_id = in.readUShort();
        in.readUShort(); // padding
        MemberId member = type.isMethodAccess() ? getMethod(member_id) : getField(member_id);
        return MethodHandleId.of(type, member);
    }

    @Override
    public List<MethodHandleId> getMethodHandles() {
        return method_handle_section;
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
        return CallSiteId.raw(name, handle, method_name, proto, extra_args);
    }

    @Override
    public List<CallSiteId> getCallSites() {
        return callsite_section;
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
                        null : getType(type_idx);
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
        return Collections.unmodifiableList(out);
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
            parameter_names = Collections.unmodifiableList(parameter_names);
        }
        var items = readDebugItemArray(in, line_start);
        return new DebugInfo(parameter_names, items);
    }

    public DebugInfo getDebugInfo(int offset) {
        return debug_info_cache.apply(offset);
    }

    private List<FieldDef> readFieldDefList(
            RandomInput in, int count, IntSupplier hiddenapi,
            IntMap<NavigableSet<Annotation>> annotations_map,
            List<EncodedValue> static_values, boolean static_list) {
        List<FieldDef> out = new ArrayList<>(count);
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += in.readSmallULeb128();
            var id = getField(index);
            int access_flags = in.readULeb128();
            int hiddenapi_flags;
            if (options.getTargetApi() == 28) {
                long common = LegacyHiddenApiFlags.decrypt(access_flags);
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
            // Potential duplicates are handled when merging lists
            out.add(FieldDef.raw(id.getName(), id.getType(), access_flags, hiddenapi_flags,
                    initial_value, annotations_map.get(index, Collections.emptyNavigableSet())));
        }
        // Note: mutable
        return out;
    }

    private CatchHandler readCatchHandler(RandomInput in) {
        int pos = in.position();
        int size = in.readSLeb128();
        // Each handler element takes at least two bytes, so the size of
        // either sign cannot exceed half of the remaining data. This also
        // rejects Integer.MIN_VALUE, whose absolute value overflows
        int max_size = in.available() / 2;
        if (size > max_size || size < -max_size) {
            throw new IllegalStateException("Out of range catch handler size "
                    + size + " at position " + pos);
        }
        int handlersCount = size < 0 ? -size : size;
        List<ExceptionHandler> handlers = new ArrayList<>(handlersCount);
        for (int i = 0; i < handlersCount; i++) {
            var type = getType(in.readSmallULeb128());
            int address = in.readSmallULeb128();
            handlers.add(ExceptionHandler.of(type, address));
        }
        handlers = Collections.unmodifiableList(handlers);
        Integer catch_all_addr = null;
        if (size <= 0) {
            catch_all_addr = in.readSmallULeb128();
        }
        return new CatchHandler(handlers, catch_all_addr);
    }

    private TryBlock readTryBlock(RandomInput in, IntMap<CatchHandler> handlers) {
        int start_addr = in.readSmallUInt(); // code units
        int unit_count = in.readUShort(); // code units

        int handler_off = in.readUShort();
        CatchHandler handler = handlers.get(handler_off);
        if (handler == null) {
            throw new IllegalStateException("Unable to find catch handler with offset "
                    + handler_off + " at position " + (in.position() - TRY_ITEM_SIZE));
        }
        return TryBlock.raw(start_addr, unit_count, handler.catch_all_addr(), handler.elements());
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

            // registers_size already includes ins_size here.
            // ins_size and outs_size are redundant.
        } else {
            registers_size = in.readUShort();
            ins_size = in.readUShort();
            outs_size = in.readUShort();
            tries_size = in.readUShort();
            int debug_info_off = in.readSmallUInt();
            insns_count = in.readSmallUInt();
            // ins_size and outs_size are redundant.
            if (options.hasDebugInfo() && debug_info_off != NO_OFFSET) {
                if (debug_info_off >= data_buffer.size()) {
                    throw new IllegalStateException(String.format(
                            "Invalid debug_info_off 0x%x for code item at offset 0x%x",
                            debug_info_off, offset));
                }
                debug_info = getDebugInfo(debug_info_off);
            } else {
                debug_info = null;
            }
        }

        // The instructions must fit into the remaining data
        if (insns_count * 2L > in.available()) {
            throw new IllegalStateException(String.format(
                    "Out of range insns_size %d for code item at offset 0x%x",
                    insns_count, offset));
        }

        var instructions = InstructionReader.readArray(this, in, insns_count);
        NavigableSet<TryBlock> tries = new TreeSet<>();

        if (tries_size > 0) {
            in.alignPosition(TRY_ITEM_ALIGNMENT);

            int tries_pos = in.position();
            in.addPosition(tries_size * TRY_ITEM_SIZE);

            int handlers_start = in.position();
            int handlers_size = in.readSmallULeb128();
            // Each handler takes at least two bytes, so the list size
            // cannot exceed half of the remaining data
            if (handlers_size > in.available() / 2) {
                throw new IllegalStateException("Out of range handlers size "
                        + handlers_size + " at position " + handlers_start);
            }

            var handlers = new IntMap<CatchHandler>(handlers_size);
            for (int i = 0; i < handlers_size; i++) {
                int handler_offset = in.position() - handlers_start;
                handlers.put(handler_offset, readCatchHandler(in));
            }

            in.position(tries_pos);
            for (int i = 0; i < tries_size; i++) {
                tries.add(readTryBlock(in, handlers));
            }
        }

        tries = Collections.unmodifiableNavigableSet(tries);

        return new CodeItem(registers_size, ins_size,
                outs_size, debug_info, instructions, tries);
    }

    public CodeItem getCodeItem(int offset) {
        return code_cache.apply(offset);
    }

    public static MethodImplementation toImplementation(CodeItem code, DebugInfo debug_info) {
        if (code == null) return null;
        return MethodImplementation.raw(code.registers(), code.instructions(),
                code.tries(), debug_info == null ? List.of() : debug_info.items());
    }

    private List<Parameter> toParamaterList(List<String> names, List<TypeId> types,
                                            List<NavigableSet<Annotation>> annotations_map) {
        int types_size = types.size();
        int annotations_size = annotations_map.size();
        List<Parameter> out = new ArrayList<>(types.size());
        for (int i = 0; i < types_size; i++) {
            var type = types.get(i);
            String name = (names == null || i >= names.size()) ? null : names.get(i);
            var annotations = i < annotations_size ? annotations_map.get(i) :
                    Collections.<Annotation>emptyNavigableSet();
            out.add(Parameter.raw(type, name, annotations));
        }
        return Collections.unmodifiableList(out);
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
            IntMap<NavigableSet<Annotation>> method_annotations,
            IntMap<List<NavigableSet<Annotation>>> parameter_annotations) {
        List<MethodDef> out = new ArrayList<>(count);
        int index = 0;
        for (int i = 0; i < count; i++) {
            index += in.readSmallULeb128();
            var id = getMethod(index);
            int access_flags = in.readULeb128();
            int hiddenapi_flags;
            if (options.getTargetApi() == 28) {
                long common = LegacyHiddenApiFlags.decrypt(access_flags);
                access_flags = (int) common;
                hiddenapi_flags = options.hasHiddenApiFlags() ?
                        (int) (common >> 32) : 0;
            } else {
                hiddenapi_flags = options.hasHiddenApiFlags() ?
                        hiddenapi.getAsInt() : 0;
            }
            int code_off = in.readSmallULeb128();
            var code = code_off == NO_OFFSET ? null :
                    (!options.hasMethodBodies() ?
                            CodeItem.EMPTY : getCodeItem(code_off));
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
            // Potential duplicates are handled when merging lists
            out.add(MethodDef.raw(id.getName(), id.getReturnType(), parameters,
                    access_flags, hiddenapi_flags, implementation,
                    method_annotations.get(index, Collections.emptyNavigableSet())));
        }
        // Note: mutable
        return out;
    }

    /**
     * ACC_VERIFIED and ACC_OPTIMIZED are ODEX-only markers: ART rejects
     * class_def flags outside kAccJavaFlagsMask, so they are accepted only
     * when the target is Dalvik and odex instructions are expected.
     */
    private void checkOdexClassFlags(int index, TypeId type, int access_flags) {
        int odex_flags = access_flags & (ACC_VERIFIED | ACC_OPTIMIZED);
        if (odex_flags == 0 || (options.isTargetForDalvik() && options.hasOdexInstructions())) {
            return;
        }
        throw new IllegalStateException(String.format(
                "Class %d (%s) has odex access flags 0x%08X,"
                        + " expected a Dalvik target with odex instructions",
                index, type.getDescriptor(), odex_flags));
    }

    private ClassDefHeader readClassDefHeader(int index, int offset) {
        var in = mainAt(offset);

        TypeId clazz = getType(in.readSmallUInt());
        int access_flags = in.readInt();
        checkOdexClassFlags(index, clazz, access_flags);
        int superclass_idx = in.readSmallUIntWithM1();
        TypeId superclass = superclass_idx == NO_INDEX ?
                null : getType(superclass_idx);
        int interfaces_off = in.readSmallUInt();
        List<TypeId> interfaces = interfaces_off == NO_OFFSET ?
                List.of() : getTypeList(interfaces_off);
        int source_file_idx = in.readSmallUIntWithM1();
        String source_file = source_file_idx == NO_INDEX ?
                null : getString(source_file_idx);
        int annotations_off = in.readSmallUInt();
        var annotations = annotations_off == NO_OFFSET || !options.hasAnnotations() ?
                AnnotationDirectory.EMPTY : getAnnotationDirectory(annotations_off);
        int class_data_off = in.readSmallUInt();
        int static_values_off = in.readSmallUInt();
        var static_values = static_values_off == NO_OFFSET || !options.hasFieldValues() ?
                null : getEncodedArray(static_values_off).getValue();

        return new ClassDefHeader(clazz, access_flags, superclass, interfaces,
                source_file, annotations, class_data_off, static_values);
    }

    @Override
    public List<ClassDefHeader> getClassHeaders() {
        return class_header_section;
    }

    @Override
    public ClassDefHeader getClassHeader(int index) {
        return (ClassDefHeader) DexReaderCache.super.getClassHeader(index);
    }

    /**
     * Legacy readers parse access_flags themselves, so the gate is applied
     * to the class they return; the standard path is checked when its
     * header is read.
     */
    private ClassDef checkOdexClassFlags(int index, ClassDef value) {
        checkOdexClassFlags(index, value.getType(), value.getAccessFlags());
        return value;
    }

    private ClassDef readClassDef(int index, int offset) {
        if (version == DEX013) {
            return checkOdexClassFlags(index, Dex013.readClassDef(this, index, offset));
        }
        if (version == DEX009) {
            return checkOdexClassFlags(index, Dex009.readClassDef(this, index, offset));
        }

        var pre_def = getClassHeader(index);

        var clazz = pre_def.type();
        var access_flags = pre_def.access_flags();
        var superclass = pre_def.superclass();
        var interfaces = pre_def.interfaces();
        var source_file = pre_def.source_file();
        var annotations = pre_def.annotations();
        var class_data_off = pre_def.class_data_off();
        var static_values = pre_def.static_values();

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

        return ClassDef.raw(clazz, access_flags, superclass, interfaces, source_file,
                MemberUtils.mergeFields(static_fields, instance_fields),
                MemberUtils.mergeMethods(direct_methods, virtual_methods),
                annotations.class_annotations());
    }

    @Override
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
        checkIndex(class_idx, class_section.size(), "class");
        return options.hasHiddenApiFlags() ? hiddenapi_section.apply(class_idx) : null;
    }
}
