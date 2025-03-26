package com.v7878.dex.raw;

import static com.v7878.dex.DexConstants.ACC_VISIBILITY_MASK;
import static com.v7878.dex.DexConstants.DBG_ADVANCE_LINE;
import static com.v7878.dex.DexConstants.DBG_ADVANCE_PC;
import static com.v7878.dex.DexConstants.DBG_END_LOCAL;
import static com.v7878.dex.DexConstants.DBG_END_SEQUENCE;
import static com.v7878.dex.DexConstants.DBG_FIRST_SPECIAL;
import static com.v7878.dex.DexConstants.DBG_LAST_SPECIAL;
import static com.v7878.dex.DexConstants.DBG_LINE_BASE;
import static com.v7878.dex.DexConstants.DBG_LINE_CEIL;
import static com.v7878.dex.DexConstants.DBG_LINE_RANGE;
import static com.v7878.dex.DexConstants.DBG_RESTART_LOCAL;
import static com.v7878.dex.DexConstants.DBG_SET_EPILOGUE_BEGIN;
import static com.v7878.dex.DexConstants.DBG_SET_PROLOGUE_END;
import static com.v7878.dex.DexConstants.DBG_START_LOCAL;
import static com.v7878.dex.DexConstants.DBG_START_LOCAL_EXTENDED;
import static com.v7878.dex.DexConstants.ENDIAN_CONSTANT;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_BLOCKED;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_MAX_TARGET_O;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_SDK;
import static com.v7878.dex.DexConstants.HIDDENAPI_FLAG_UNSUPPORTED;
import static com.v7878.dex.DexConstants.NO_INDEX;
import static com.v7878.dex.DexConstants.NO_OFFSET;
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
import static com.v7878.dex.DexOffsets.ANNOTATION_DIRECTORY_ALIGNMENT;
import static com.v7878.dex.DexOffsets.ANNOTATION_SET_ALIGNMENT;
import static com.v7878.dex.DexOffsets.ANNOTATION_SET_LIST_ALIGNMENT;
import static com.v7878.dex.DexOffsets.BASE_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.CALL_SITE_ID_SIZE;
import static com.v7878.dex.DexOffsets.CHECKSUM_DATA_START_OFFSET;
import static com.v7878.dex.DexOffsets.CHECKSUM_OFFSET;
import static com.v7878.dex.DexOffsets.CLASS_DEF_SIZE;
import static com.v7878.dex.DexOffsets.CODE_ITEM_ALIGNMENT;
import static com.v7878.dex.DexOffsets.COMPACT_CODE_ITEM_ALIGNMENT;
import static com.v7878.dex.DexOffsets.COMPACT_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.COMPACT_OFFSET_TABLE_ALIGNMENT;
import static com.v7878.dex.DexOffsets.DATA_SECTION_ALIGNMENT;
import static com.v7878.dex.DexOffsets.DEXCONTAINER_HEADER_SIZE;
import static com.v7878.dex.DexOffsets.FIELD_ID_SIZE;
import static com.v7878.dex.DexOffsets.HIDDENAPI_ALIGNMENT;
import static com.v7878.dex.DexOffsets.MAP_ALIGNMENT;
import static com.v7878.dex.DexOffsets.METHOD_HANDLE_ID_SIZE;
import static com.v7878.dex.DexOffsets.METHOD_ID_SIZE;
import static com.v7878.dex.DexOffsets.PROTO_ID_SIZE;
import static com.v7878.dex.DexOffsets.SIGNATURE_DATA_START_OFFSET;
import static com.v7878.dex.DexOffsets.SIGNATURE_OFFSET;
import static com.v7878.dex.DexOffsets.STRING_ID_SIZE;
import static com.v7878.dex.DexOffsets.TRY_ITEM_ALIGNMENT;
import static com.v7878.dex.DexOffsets.TRY_ITEM_SIZE;
import static com.v7878.dex.DexOffsets.TYPE_ID_SIZE;
import static com.v7878.dex.DexOffsets.TYPE_LIST_ALIGNMENT;
import static com.v7878.dex.raw.CompactDexConstants.kDebugElementsPerIndex;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderInsSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderInsnsSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderOutsSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderRegistersSize;
import static com.v7878.dex.raw.CompactDexConstants.kFlagPreHeaderTriesSize;
import static com.v7878.dex.raw.CompactDexConstants.kInsSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kInsnsSizeMask;
import static com.v7878.dex.raw.CompactDexConstants.kInsnsSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kOutsSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kRegistersSizeShift;
import static com.v7878.dex.raw.CompactDexConstants.kTriesSizeSizeShift;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.getSecondFlag;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kBlacklist;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kDarkGreylist;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kLightGreylist;
import static com.v7878.dex.raw.LegacyHiddenApiFlags.kWhitelist;
import static com.v7878.dex.util.AlignmentUtils.roundUp;

import com.v7878.dex.DexVersion;
import com.v7878.dex.Opcodes;
import com.v7878.dex.WriteOptions;
import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.AnnotationElement;
import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.CommonAnnotation;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
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
import com.v7878.dex.immutable.value.EncodedShort;
import com.v7878.dex.immutable.value.EncodedString;
import com.v7878.dex.immutable.value.EncodedType;
import com.v7878.dex.immutable.value.EncodedValue;
import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.RandomIO;
import com.v7878.dex.io.RandomOutput;
import com.v7878.dex.io.ValueCoder;
import com.v7878.dex.raw.DexCollector.AnnotationDirectory;
import com.v7878.dex.raw.DexCollector.CallSiteIdContainer;
import com.v7878.dex.raw.DexCollector.ClassDefContainer;
import com.v7878.dex.raw.DexCollector.CodeContainer;
import com.v7878.dex.raw.DexCollector.FieldDefContainer;
import com.v7878.dex.raw.DexCollector.MethodDefContainer;
import com.v7878.dex.raw.DexCollector.TryBlockContainer;
import com.v7878.dex.util.CollectionUtils;
import com.v7878.dex.util.EmptyArrays;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.stream.Stream;
import java.util.zip.Adler32;

public class DexWriter {
    private static class FileMap {
        public int header_size;

        public int file_size;

        public int map_list_off;

        public int string_ids_size;
        public int string_ids_off;
        public int type_ids_size;
        public int type_ids_off;
        public int proto_ids_size;
        public int proto_ids_off;
        public int field_ids_size;
        public int field_ids_off;
        public int method_ids_size;
        public int method_ids_off;
        public int class_defs_size;
        public int class_defs_off;
        public int call_site_ids_size;
        public int call_site_ids_off;
        public int method_handles_size;
        public int method_handles_off;

        public int data_size;
        public int data_off;

        public int type_lists_size;
        public int type_lists_off;
        public int annotation_set_lists_size;
        public int annotation_set_lists_off;
        public int annotation_sets_size;
        public int annotation_sets_off;
        public int class_data_items_size;
        public int class_data_items_off;
        public int code_items_size;
        public int code_items_off;
        public int string_data_items_size;
        public int string_data_items_off;
        public int debug_info_items_size;
        public int debug_info_items_off;
        public int annotations_size;
        public int annotations_off;
        public int encoded_arrays_size;
        public int encoded_arrays_off;
        public int annotation_directories_size;
        public int annotation_directories_off;

        public int hiddenapi_class_data_items_off;

        public int compact_feature_flags;
        public int compact_debug_info_offsets_pos;
        public int compact_debug_info_offsets_table_offset;
        public int compact_debug_info_base;
        public int compact_owned_data_begin;
        public int compact_owned_data_end;

        public int header_off;
        public int container_size;
    }

    private record CompactData(int[] offsets) {
    }

    private final RandomIO main_buffer;
    private final RandomIO data_buffer;

    private final WriteOptions options;
    private final Opcodes opcodes;

    private final FileMap map;

    private final String[] strings;
    private final TypeId[] types;
    private final ProtoId[] protos;
    private final FieldId[] fields;
    private final MethodId[] methods;
    private final CallSiteIdContainer[] call_sites;
    private final MethodHandleId[] method_handles;

    private final Map<List<TypeId>, Integer> type_lists;
    private final Map<EncodedArray, Integer> encoded_arrays;
    private final Map<DebugInfo, Integer> debug_infos;
    private final Map<CodeContainer, Integer> code_items;
    private final Map<Annotation, Integer> annotations;
    private final Map<NavigableSet<Annotation>, Integer> annotation_sets;
    private final Map<List<NavigableSet<Annotation>>, Integer> annotation_set_lists;
    private final Map<AnnotationDirectory, Integer> annotation_directories;

    private final ClassDefContainer[] class_defs;

    private final int[][] hiddenapi_flags;

    // Not null only for compact dex files
    private final CompactData compact_debug_info;

    public DexWriter(WriteOptions options, RandomIO io, Dex dexfile, int header_offset) {
        assert io.position() == 0;
        options.validate();
        this.options = options;
        main_buffer = io.duplicate();

        opcodes = Opcodes.of(options.getDexVersion(), options.getTargetApi(),
                options.isTargetForArt(), options.hasOdexInstructions());

        map = new FileMap();

        if (!isDexContainer() && header_offset != 0) {
            throw new IllegalArgumentException("Unexpected container offset");
        }
        map.header_off = header_offset;

        if (isCompact()) {
            // TODO
            map.compact_feature_flags = /* kDefaultMethods */ 0x1;
        }

        var collector = new DexCollector(isCompact());
        collector.fillDex(dexfile);

        strings = collector.strings.toArray(EmptyArrays.STRING);
        // TODO: at most 65535
        types = collector.types.toArray(EmptyArrays.TYPE_ID);
        // TODO: at most 65535
        protos = collector.protos.toArray(EmptyArrays.PROTO_ID);
        fields = collector.fields.toArray(EmptyArrays.FIELD_ID);
        methods = collector.methods.toArray(EmptyArrays.METHOD_ID);
        call_sites = collector.call_sites.toArray(EmptyArrays.CALLSITE_ID_CONTAINER);
        method_handles = collector.method_handles.toArray(EmptyArrays.METHOD_HANDLE_ID);

        // TODO: "The classes must be ordered such that a given class's superclass
        //  and implemented interfaces appear in the list earlier than the referring class"
        class_defs = collector.class_defs.toArray(EmptyArrays.CLASS_DEF_CONTAINER);

        type_lists = collector.type_lists;
        encoded_arrays = collector.encoded_arrays;
        debug_infos = collector.debug_infos;
        code_items = collector.code_items;
        annotations = collector.annotations;
        annotation_sets = collector.annotation_sets;
        annotation_set_lists = collector.annotation_set_lists;
        annotation_directories = collector.annotation_directories;

        if (options.hasHiddenApiFlags() && options.getTargetApi() != 28) {
            assert options.getTargetApi() >= 29;
            hiddenapi_flags = getHiddenApiFlags();
        } else {
            hiddenapi_flags = null;
        }

        compact_debug_info = isCompact() ? new CompactData(
                new int[methods.length]
        ) : null;

        initMap();

        // Note: for compact dex, offsets are calculated from the data section, not the header
        data_buffer = isCompact() ? io.sliceAt(map.data_off) : io.duplicateAt(map.data_off);

        // We want offset 0 to be reserved
        if (isCompact()) {
            data_buffer.addPosition(DATA_SECTION_ALIGNMENT);
        }

        // Write code item first to minimize the space required for encoded methods
        if (isCompact()) {
            // For cdex, the code items don't depend on the debug info
            writeCodeItemSection();
            writeDebugInfoSection();
        } else {
            writeDebugInfoSection();
            writeCodeItemSection();
        }
        writeEncodedArraySection();
        writeAnnotationSection();
        writeAnnotationSetSection();
        writeAnnotationSetListSection();
        writeAnnotationDirectorySection();
        writeTypeListSection();

        writeTypeSection();
        writeFieldSection();
        writeProtoSection();
        writeMethodSection();
        writeCallSiteSection();
        writeMethodHandleSection();

        // class def + class data
        writeClassDefSections();
        // string id + string data
        writeStringSections();

        writeHiddenApiSection();

        writeMap();

        if (isCompact()) {
            writeDebugInfoOffsetTable();
        }

        data_buffer.alignPosition(DATA_SECTION_ALIGNMENT);
        // Note: for compact dex, data section is placed
        // after the entire file and isn`t included in its size
        if (isCompact()) {
            map.file_size = map.data_off;
            map.data_size = data_buffer.position();
            map.compact_owned_data_begin = 0;
            map.compact_owned_data_end = map.data_size;
        } else {
            map.file_size = data_buffer.position();
            map.data_size = map.file_size - map.data_off;
        }
        map.file_size -= map.header_off;
    }

    private static int getHeaderSize(DexVersion version) {
        return switch (version) {
            case CDEX001 -> COMPACT_HEADER_SIZE;
            case DEX041 -> DEXCONTAINER_HEADER_SIZE;
            default -> BASE_HEADER_SIZE;
        };
    }

    private void initMap() {
        int offset = map.header_off;

        map.header_size = getHeaderSize(version());
        offset += map.header_size;

        map.string_ids_off = offset;
        map.string_ids_size = strings.length;
        offset += map.string_ids_size * STRING_ID_SIZE;

        map.type_ids_off = offset;
        map.type_ids_size = types.length;
        offset += map.type_ids_size * TYPE_ID_SIZE;

        map.proto_ids_off = offset;
        map.proto_ids_size = protos.length;
        offset += map.proto_ids_size * PROTO_ID_SIZE;

        map.field_ids_off = offset;
        map.field_ids_size = fields.length;
        offset += map.field_ids_size * FIELD_ID_SIZE;

        map.method_ids_off = offset;
        map.method_ids_size = methods.length;
        offset += map.method_ids_size * METHOD_ID_SIZE;

        map.class_defs_off = offset;
        map.class_defs_size = class_defs.length;
        offset += map.class_defs_size * CLASS_DEF_SIZE;

        map.call_site_ids_off = offset;
        map.call_site_ids_size = call_sites.length;
        offset += map.call_site_ids_size * CALL_SITE_ID_SIZE;

        map.method_handles_off = offset;
        map.method_handles_size = method_handles.length;
        offset += map.method_handles_size * METHOD_HANDLE_ID_SIZE;

        map.data_off = roundUp(offset, DATA_SECTION_ALIGNMENT);
    }

    public int getStringIndex(String value) {
        int out = CollectionUtils.binarySearch(strings, value);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find string \"" + value + "\"");
        }
        return out;
    }

    public int getTypeIndex(TypeId value) {
        int out = CollectionUtils.binarySearch(types, value);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find type \"" + value + "\"");
        }
        return out;
    }

    public int getProtoIndex(ProtoId value) {
        int out = CollectionUtils.binarySearch(protos, value);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find proto \"" + value + "\"");
        }
        return out;
    }

    public int getFieldIndex(FieldId value) {
        int out = CollectionUtils.binarySearch(fields, value);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find field \"" + value + "\"");
        }
        return out;
    }

    public int getMethodIndex(MethodId value) {
        int out = CollectionUtils.binarySearch(methods, value);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find method \"" + value + "\"");
        }
        return out;
    }

    public int getCallSiteIndex(CallSiteId value) {
        int out = CollectionUtils.binarySearch(call_sites, value,
                (c, v) -> c.value().compareTo(v));
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find call site \"" + value + "\"");
        }
        return out;
    }

    public int getMethodHandleIndex(MethodHandleId value) {
        int out = CollectionUtils.binarySearch(method_handles, value);
        if (out < 0) {
            throw new IllegalArgumentException(
                    "Unable to find method handle \"" + value + "\"");
        }
        return out;
    }

    public int getTypeListOffset(List<TypeId> value) {
        Integer out = type_lists.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find type list \"" + value + "\"");
        }
        return out;
    }

    public int getEncodedArrayOffset(EncodedArray value) {
        Integer out = encoded_arrays.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find encoded array \"" + value + "\"");
        }
        return out;
    }

    public int getDebugInfoOffset(DebugInfo value) {
        Integer out = debug_infos.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find debug info \"" + value + "\"");
        }
        return out;
    }

    public int getCodeItemOffset(CodeContainer value) {
        Integer out = code_items.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find code item \"" + value + "\"");
        }
        return out;
    }

    public int getAnnotationOffset(Annotation value) {
        Integer out = annotations.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find annotation \"" + value + "\"");
        }
        return out;
    }

    public int getAnnotationSetOffset(NavigableSet<Annotation> value) {
        Integer out = annotation_sets.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find annotation set \"" + value + "\"");
        }
        return out;
    }

    public int getAnnotationSetListOffset(List<NavigableSet<Annotation>> value) {
        Integer out = annotation_set_lists.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find annotation set list \"" + value + "\"");
        }
        return out;
    }

    public int getAnnotationDirectoryOffset(AnnotationDirectory value) {
        Integer out = annotation_directories.get(value);
        if (out == null) {
            throw new IllegalArgumentException(
                    "Unable to find annotations directory \"" + value + "\"");
        }
        return out;
    }

    public int getFileSize() {
        return map.file_size;
    }

    public Opcodes opcodes() {
        return opcodes;
    }

    public WriteOptions options() {
        return options;
    }

    public DexVersion version() {
        return options().getDexVersion();
    }

    public boolean isCompact() {
        return version().isCompact();
    }

    public boolean isDexContainer() {
        return version().isDexContainer();
    }

    public void writeHeader() {
        main_buffer.position(map.header_off);
        main_buffer.writeLong(version().getMagic()); // TODO: only little-endian
        main_buffer.addPosition(4); // checksum
        main_buffer.addPosition(20); // signature
        main_buffer.writeInt(map.file_size);
        main_buffer.writeInt(map.header_size);
        main_buffer.writeInt(ENDIAN_CONSTANT);
        main_buffer.writeInt(0); // link_size
        main_buffer.writeInt(0); // link_off

        main_buffer.writeInt(map.map_list_off);
        main_buffer.writeInt(map.string_ids_size);
        main_buffer.writeInt(map.string_ids_size > 0 ? map.string_ids_off : 0);
        main_buffer.writeInt(map.type_ids_size);
        main_buffer.writeInt(map.type_ids_size > 0 ? map.type_ids_off : 0);
        main_buffer.writeInt(map.proto_ids_size);
        main_buffer.writeInt(map.proto_ids_size > 0 ? map.proto_ids_off : 0);
        main_buffer.writeInt(map.field_ids_size);
        main_buffer.writeInt(map.field_ids_size > 0 ? map.field_ids_off : 0);
        main_buffer.writeInt(map.method_ids_size);
        main_buffer.writeInt(map.method_ids_size > 0 ? map.method_ids_off : 0);
        main_buffer.writeInt(map.class_defs_size);
        main_buffer.writeInt(map.class_defs_size > 0 ? map.class_defs_off : 0);
        main_buffer.writeInt(map.data_size);
        main_buffer.writeInt(map.data_size > 0 ? map.data_off : 0);

        if (isCompact()) {
            main_buffer.writeInt(map.compact_feature_flags);
            main_buffer.writeInt(map.compact_debug_info_offsets_pos);
            main_buffer.writeInt(map.compact_debug_info_offsets_table_offset);
            main_buffer.writeInt(map.compact_debug_info_base);
            main_buffer.writeInt(map.compact_owned_data_begin);
            main_buffer.writeInt(map.compact_owned_data_end);
        } else if (isDexContainer()) {
            main_buffer.writeInt(map.container_size);
            main_buffer.writeInt(map.header_off);
        }
    }

    public void finalizeHeader(int container_size) {
        if (isDexContainer()) {
            assert container_size >= map.file_size;
            map.container_size = container_size;
        } else {
            assert container_size == map.file_size;
        }
        writeHeader();
        if (isCompact()) {
            // TODO: How are the checksum and signature fields calculated for compact dex?
        } else {
            main_buffer.position(map.header_off + SIGNATURE_OFFSET);
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Unable to find SHA-1 MessageDigest", e);
            }
            byte[] signature = md.digest(main_buffer
                    .duplicateAt(map.header_off + SIGNATURE_DATA_START_OFFSET)
                    .readByteArray(map.file_size - SIGNATURE_DATA_START_OFFSET));
            main_buffer.writeByteArray(signature);

            main_buffer.position(map.header_off + CHECKSUM_OFFSET);
            Adler32 adler = new Adler32();
            int adler_length = map.file_size - CHECKSUM_DATA_START_OFFSET;
            adler.update(main_buffer
                    .duplicateAt(map.header_off + CHECKSUM_DATA_START_OFFSET)
                    .readByteArray(adler_length), 0, adler_length);
            main_buffer.writeInt((int) adler.getValue());
        }
    }

    public void writeMapItem(MapItem value) {
        data_buffer.writeShort(value.type());
        data_buffer.writeShort(0);
        data_buffer.writeInt(value.size());
        data_buffer.writeInt(value.offset());
    }

    public void writeMap() {
        data_buffer.alignPosition(MAP_ALIGNMENT);
        map.map_list_off = data_buffer.position();
        var list = new ArrayList<MapItem>();

        // main section
        list.add(new MapItem(TYPE_HEADER_ITEM, 1, map.header_off));
        if (map.string_ids_size > 0) {
            list.add(new MapItem(TYPE_STRING_ID_ITEM,
                    map.string_ids_size, map.string_ids_off));
        }
        if (map.type_ids_size > 0) {
            list.add(new MapItem(TYPE_TYPE_ID_ITEM,
                    map.type_ids_size, map.type_ids_off));
        }
        if (map.proto_ids_size > 0) {
            list.add(new MapItem(TYPE_PROTO_ID_ITEM,
                    map.proto_ids_size, map.proto_ids_off));
        }
        if (map.field_ids_size > 0) {
            list.add(new MapItem(TYPE_FIELD_ID_ITEM,
                    map.field_ids_size, map.field_ids_off));
        }
        if (map.method_ids_size > 0) {
            list.add(new MapItem(TYPE_METHOD_ID_ITEM,
                    map.method_ids_size, map.method_ids_off));
        }
        if (map.class_defs_size > 0) {
            list.add(new MapItem(TYPE_CLASS_DEF_ITEM,
                    map.class_defs_size, map.class_defs_off));
        }
        if (map.call_site_ids_size > 0) {
            list.add(new MapItem(TYPE_CALL_SITE_ID_ITEM,
                    map.call_site_ids_size, map.call_site_ids_off));
        }
        if (map.method_handles_size > 0) {
            list.add(new MapItem(TYPE_METHOD_HANDLE_ITEM,
                    map.method_handles_size, map.method_handles_off));
        }

        // data section
        if (map.type_lists_size > 0) {
            list.add(new MapItem(TYPE_TYPE_LIST,
                    map.type_lists_size, map.type_lists_off));
        }
        if (map.annotation_set_lists_size > 0) {
            list.add(new MapItem(TYPE_ANNOTATION_SET_REF_LIST,
                    map.annotation_set_lists_size, map.annotation_set_lists_off));
        }
        if (map.annotation_sets_size > 0) {
            list.add(new MapItem(TYPE_ANNOTATION_SET_ITEM,
                    map.annotation_sets_size, map.annotation_sets_off));
        }
        if (map.class_data_items_size > 0) {
            list.add(new MapItem(TYPE_CLASS_DATA_ITEM,
                    map.class_data_items_size, map.class_data_items_off));
        }
        if (map.code_items_size > 0) {
            list.add(new MapItem(TYPE_CODE_ITEM,
                    map.code_items_size, map.code_items_off));
        }
        if (map.string_data_items_size > 0) {
            list.add(new MapItem(TYPE_STRING_DATA_ITEM,
                    map.string_data_items_size, map.string_data_items_off));
        }
        if (map.debug_info_items_size > 0) {
            list.add(new MapItem(TYPE_DEBUG_INFO_ITEM,
                    map.debug_info_items_size, map.debug_info_items_off));
        }
        if (map.annotations_size > 0) {
            list.add(new MapItem(TYPE_ANNOTATION_ITEM,
                    map.annotations_size, map.annotations_off));
        }
        if (map.encoded_arrays_size > 0) {
            list.add(new MapItem(TYPE_ENCODED_ARRAY_ITEM,
                    map.encoded_arrays_size, map.encoded_arrays_off));
        }
        if (map.annotation_directories_size > 0) {
            list.add(new MapItem(TYPE_ANNOTATIONS_DIRECTORY_ITEM,
                    map.annotation_directories_size, map.annotation_directories_off));
        }
        if (map.hiddenapi_class_data_items_off > 0) {
            list.add(new MapItem(TYPE_HIDDENAPI_CLASS_DATA_ITEM,
                    1, map.hiddenapi_class_data_items_off));
        }

        list.add(new MapItem(TYPE_MAP_LIST, 1, map.map_list_off));

        list.sort(Comparator.comparingInt(MapItem::offset));

        data_buffer.writeInt(list.size());
        for (MapItem tmp : list) {
            writeMapItem(tmp);
        }
    }

    public void writeDebugInfoOffsetTable() {
        var info = compact_debug_info;
        assert isCompact() && info != null;
        var offsets = info.offsets();

        data_buffer.alignPosition(COMPACT_OFFSET_TABLE_ALIGNMENT);
        int start = data_buffer.position();

        int base = Integer.MAX_VALUE;
        for (var offset : offsets) {
            if (offset != 0) {
                base = Math.min(base, offset);
            }
        }

        var offsets_buffer = data_buffer.duplicate();
        int offsets_count = (offsets.length + kDebugElementsPerIndex - 1) / kDebugElementsPerIndex;
        data_buffer.addPosition(offsets_count * 4);

        int block_start = 0;
        while (block_start < offsets.length) {
            offsets_buffer.writeInt(data_buffer.position() - start);
            int block_size = Math.min(offsets.length - block_start, kDebugElementsPerIndex);

            short bit_mask = 0;
            for (int i = 0; i < block_size; i++) {
                if (offsets[block_start + i] != 0) {
                    bit_mask |= (short) (1 << i);
                }
            }

            data_buffer.writeByte(bit_mask >> 8);
            data_buffer.writeByte(bit_mask);

            int prev_offset = base;
            for (int i = 0; i < block_size; ++i) {
                int offset = offsets[block_start + i];
                if (offset != 0) {
                    int delta = offset - prev_offset;
                    data_buffer.writeULeb128(delta);
                    prev_offset = offset;
                }
            }

            block_start += block_size;
        }

        map.compact_debug_info_offsets_pos = start;
        // Note: Why does this field exist?
        // If you write the table first, it will be equal to zero
        map.compact_debug_info_offsets_table_offset = 0;
        map.compact_debug_info_base = base;
    }

    public void writeString(String value) {
        main_buffer.writeInt(data_buffer.position());
        data_buffer.writeMUtf8(value);
    }

    public void writeStringSections() {
        if (strings.length != 0) {
            map.string_data_items_off = data_buffer.position();
            map.string_data_items_size = map.string_ids_size;
        }
        main_buffer.position(map.string_ids_off);
        for (var value : strings) {
            writeString(value);
        }
    }

    public void writeType(TypeId value) {
        main_buffer.writeInt(getStringIndex(value.getDescriptor()));
    }

    public void writeTypeSection() {
        main_buffer.position(map.type_ids_off);
        for (var value : types) {
            writeType(value);
        }
    }

    public void writeField(FieldId value) {
        main_buffer.writeShort(getTypeIndex(value.getDeclaringClass()));
        main_buffer.writeShort(getTypeIndex(value.getType()));
        main_buffer.writeInt(getStringIndex(value.getName()));
    }

    public void writeFieldSection() {
        main_buffer.position(map.field_ids_off);
        for (var value : fields) {
            writeField(value);
        }
    }

    public void writeProto(ProtoId value) {
        main_buffer.writeInt(getStringIndex(value.computeShorty()));
        main_buffer.writeInt(getTypeIndex(value.getReturnType()));
        var parameters = value.getParameterTypes();
        main_buffer.writeInt(parameters.isEmpty() ? 0
                : getTypeListOffset(parameters));
    }

    public void writeProtoSection() {
        main_buffer.position(map.proto_ids_off);
        for (var value : protos) {
            writeProto(value);
        }
    }

    public void writeMethod(MethodId value) {
        main_buffer.writeShort(getTypeIndex(value.getDeclaringClass()));
        main_buffer.writeShort(getProtoIndex(value.getProto()));
        main_buffer.writeInt(getStringIndex(value.getName()));
    }

    public void writeMethodSection() {
        main_buffer.position(map.method_ids_off);
        for (var value : methods) {
            writeMethod(value);
        }
    }

    public void writeCallSite(CallSiteIdContainer value) {
        main_buffer.writeInt(getEncodedArrayOffset(value.array()));
    }

    public void writeCallSiteSection() {
        main_buffer.position(map.call_site_ids_off);
        for (var value : call_sites) {
            writeCallSite(value);
        }
    }

    public void writeMethodHandle(MethodHandleId value) {
        main_buffer.writeShort(value.getHandleType().value());
        main_buffer.writeShort(0);
        main_buffer.writeShort(value.getHandleType().isMethodAccess()
                ? getMethodIndex((MethodId) value.getMember())
                : getFieldIndex((FieldId) value.getMember()));
        main_buffer.writeShort(0);
    }

    public void writeMethodHandleSection() {
        main_buffer.position(map.method_handles_off);
        for (var value : method_handles) {
            writeMethodHandle(value);
        }
    }

    private int fixLegacyHiddenApiFlags(int access_flags, int hiddenapi_flags) {
        hiddenapi_flags = switch (hiddenapi_flags) {
            case HIDDENAPI_FLAG_SDK -> kWhitelist;
            case HIDDENAPI_FLAG_UNSUPPORTED -> kLightGreylist;
            case HIDDENAPI_FLAG_MAX_TARGET_O -> kDarkGreylist;
            case HIDDENAPI_FLAG_BLOCKED -> kBlacklist;
            default -> throw new IllegalArgumentException(String.format(
                    "Invalid hidden api flag: %d", hiddenapi_flags));
        };
        if ((hiddenapi_flags & 0x1) != 0) {
            access_flags ^= ACC_VISIBILITY_MASK;
        }
        if ((hiddenapi_flags & 0x2) != 0) {
            int second_flag = getSecondFlag(access_flags);
            access_flags |= second_flag;
        }
        return access_flags;
    }

    public void writeFieldDef(FieldDefContainer value) {
        int access_flags = value.value().getAccessFlags();
        if (options.hasHiddenApiFlags() && options.getTargetApi() == 28) {
            int hiddenapi_flags = value.value().getHiddenApiFlags();
            access_flags = fixLegacyHiddenApiFlags(access_flags, hiddenapi_flags);
        }
        data_buffer.writeULeb128(access_flags);
    }

    public void writeFieldDefArray(FieldDefContainer[] array) {
        int index = 0;
        for (var tmp : array) {
            int diff = getFieldIndex(tmp.id()) - index;
            index += diff;
            data_buffer.writeULeb128(diff);
            writeFieldDef(tmp);
        }
    }

    public void writeMethodDef(MethodDefContainer value, int index) {
        int access_flags = value.value().getAccessFlags();
        if (options.hasHiddenApiFlags() && options.getTargetApi() == 28) {
            int hiddenapi_flags = value.value().getHiddenApiFlags();
            access_flags = fixLegacyHiddenApiFlags(access_flags, hiddenapi_flags);
        }
        data_buffer.writeULeb128(access_flags);
        var code = value.code();
        data_buffer.writeULeb128(code == null ? NO_OFFSET : getCodeItemOffset(code));
        if (isCompact()) {
            var debug_info = value.debug_info();
            if (debug_info != null) {
                compact_debug_info.offsets[index] = getDebugInfoOffset(debug_info);
            }
        }
    }

    public void writeMethodDefArray(MethodDefContainer[] array) {
        int index = 0;
        for (var tmp : array) {
            int diff = getMethodIndex(tmp.id()) - index;
            index += diff;
            data_buffer.writeULeb128(diff);
            writeMethodDef(tmp, index);
        }
    }

    public int writeClassData(ClassDefContainer value) {
        int start = data_buffer.position();
        data_buffer.writeULeb128(value.static_fields().length);
        data_buffer.writeULeb128(value.instance_fields().length);
        data_buffer.writeULeb128(value.direct_methods().length);
        data_buffer.writeULeb128(value.virtual_methods().length);
        writeFieldDefArray(value.static_fields());
        writeFieldDefArray(value.instance_fields());
        writeMethodDefArray(value.direct_methods());
        writeMethodDefArray(value.virtual_methods());
        return start;
    }

    public void writeClassDef(ClassDefContainer value) {
        main_buffer.writeInt(getTypeIndex(value.value().getType()));
        main_buffer.writeInt(value.value().getAccessFlags());
        var superclass = value.value().getSuperclass();
        main_buffer.writeInt(superclass == null ?
                NO_INDEX : getTypeIndex(superclass));
        var interfaces = value.interfaces();
        main_buffer.writeInt(interfaces == null ?
                NO_OFFSET : getTypeListOffset(interfaces));
        var source_file = value.value().getSourceFile();
        main_buffer.writeInt(source_file == null ?
                NO_INDEX : getStringIndex(source_file));
        var annotations = value.annotations();
        main_buffer.writeInt(annotations == null ?
                NO_OFFSET : getAnnotationDirectoryOffset(annotations));
        main_buffer.writeInt(value.isEmptyClassData() ?
                NO_OFFSET : writeClassData(value));
        var static_values = value.static_values();
        main_buffer.writeInt(static_values == null ?
                NO_OFFSET : getEncodedArrayOffset(static_values));
    }

    public void writeClassDefSections() {
        map.class_data_items_size = Stream.of(class_defs)
                .mapToInt(def -> def.isEmptyClassData() ? 0 : 1).sum();
        if (map.class_data_items_size != 0) {
            map.class_data_items_off = data_buffer.position();
        }
        main_buffer.position(map.class_defs_off);
        for (var value : class_defs) {
            writeClassDef(value);
        }
    }

    public void writeTypeList(List<TypeId> value) {
        data_buffer.alignPosition(TYPE_LIST_ALIGNMENT);
        int start = data_buffer.position();
        data_buffer.writeInt(value.size());
        for (var tmp : value) {
            data_buffer.writeShort(getTypeIndex(tmp));
        }
        type_lists.replace(value, start);
    }

    public void writeTypeListSection() {
        var size = type_lists.size();
        if (size != 0) {
            data_buffer.alignPosition(TYPE_LIST_ALIGNMENT);
            map.type_lists_off = data_buffer.position();
            map.type_lists_size = size;
        }
        for (var tmp : type_lists.keySet()) {
            writeTypeList(tmp);
        }
    }

    public void writeDebugItemArray(RandomOutput out, List<DebugItem> items, int[] first_line) {
        int[] address = {0, 0};
        Runnable emit_address = () -> {
            if (address[0] != address[1]) {
                out.writeByte(DBG_ADVANCE_PC);
                out.writeULeb128(address[0] - address[1]);
                address[1] = address[0];
            }
        };
        int[] line = {0, 0};
        Runnable emit_position = () -> {
            if (line[0] != line[1]) {
                if (line[1] == 0) first_line[0] = line[1] = line[0];

                int addr_diff = address[0] - address[1];
                int line_diff = line[0] - line[1];

                int adjusted_opcode = Math.max(Math.min(
                        line_diff, DBG_LINE_CEIL), DBG_LINE_BASE);
                line_diff -= adjusted_opcode;
                adjusted_opcode -= DBG_LINE_BASE;

                int max_addr_diff = DBG_LAST_SPECIAL - DBG_FIRST_SPECIAL;
                max_addr_diff -= adjusted_opcode;
                max_addr_diff /= DBG_LINE_RANGE;
                max_addr_diff = Math.min(max_addr_diff, addr_diff);
                addr_diff -= max_addr_diff;

                adjusted_opcode += max_addr_diff * DBG_LINE_RANGE + DBG_FIRST_SPECIAL;

                if (addr_diff != 0) {
                    out.writeByte(DBG_ADVANCE_PC);
                    out.writeULeb128(addr_diff);
                }
                if (line_diff != 0) {
                    out.writeByte(DBG_ADVANCE_LINE);
                    out.writeSLeb128(line_diff);
                }
                out.writeByte(adjusted_opcode);

                address[1] = address[0];
                line[1] = line[0];
            }
        };
        for (var item : items) {
            if (item instanceof AdvancePC op) {
                address[0] += op.getAddrDiff();
            } else if (item instanceof StartLocal op) {
                var register = op.getRegister();
                var name = op.getName();
                var type = op.getType();
                var signature = op.getSignature();
                emit_address.run();
                out.writeByte(signature == null ? DBG_START_LOCAL : DBG_START_LOCAL_EXTENDED);
                out.writeULeb128(register);
                out.writeULeb128((name == null ? NO_INDEX : getStringIndex(name)) + 1);
                out.writeULeb128((type == null ? NO_INDEX : getTypeIndex(type)) + 1);
                if (signature != null) out.writeULeb128(getStringIndex(signature) + 1);
            } else if (item instanceof EndLocal op) {
                var register = op.getRegister();
                emit_address.run();
                out.writeByte(DBG_END_LOCAL);
                out.writeULeb128(register);
            } else if (item instanceof RestartLocal op) {
                var register = op.getRegister();
                emit_address.run();
                out.writeByte(DBG_RESTART_LOCAL);
                out.writeULeb128(register);
            } else if (item instanceof SetPrologueEnd) {
                emit_address.run();
                out.writeByte(DBG_SET_PROLOGUE_END);
            } else if (item instanceof SetEpilogueBegin) {
                emit_address.run();
                out.writeByte(DBG_SET_EPILOGUE_BEGIN);
            } else if (item instanceof SetFile op) {
                var name = op.getName();
                emit_address.run();
                out.writeULeb128((name == null ? NO_INDEX : getStringIndex(name)) + 1);
            } else if (item instanceof LineNumber op) {
                line[0] = op.getLine();
                emit_position.run();
            }
        }
        out.writeByte(DBG_END_SEQUENCE);
    }

    public void writeDebugInfo(DebugInfo value) {
        int start = data_buffer.position();

        int[] line_start = {1};
        ByteArrayIO dbg_sequence = new ByteArrayIO();
        writeDebugItemArray(dbg_sequence, value.items(), line_start);
        dbg_sequence.position(0);

        var names = value.parameter_names();
        var names_size = names.size();

        data_buffer.writeULeb128(line_start[0]);
        data_buffer.writeULeb128(names_size);
        for (int i = 0; i < names_size; i++) {
            var name = names.get(i);
            var index = name == null ? NO_INDEX : getStringIndex(name);
            data_buffer.writeULeb128(index + 1);
        }
        dbg_sequence.writeTo(data_buffer);

        debug_infos.replace(value, start);
    }

    public void writeDebugInfoSection() {
        var size = debug_infos.size();
        if (size != 0) {
            map.debug_info_items_off = data_buffer.position();
            map.debug_info_items_size = size;
        }
        for (var tmp : debug_infos.keySet()) {
            writeDebugInfo(tmp);
        }
    }

    private int countCodeUnits(List<Instruction> insns) {
        boolean has_payloads = false;
        int out = 0;
        for (var tmp : insns) {
            out += tmp.getUnitCount();
            has_payloads = has_payloads || tmp.getOpcode().isPayload();
        }
        return has_payloads ? ~out : out;
    }

    private int writePreHeader(int registers_size, int ins_size,
                               int outs_size, int tries_size,
                               int insns_count, boolean has_payloads) {
        registers_size -= ins_size;
        int fields = (registers_size & 0xF) << kRegistersSizeShift
                | (ins_size & 0xF) << kInsSizeShift
                | (outs_size & 0xF) << kOutsSizeShift
                | (tries_size & 0xF) << kTriesSizeSizeShift;
        registers_size &= ~0xF;
        ins_size &= ~0xF;
        outs_size &= ~0xF;
        tries_size &= ~0xF;

        int insns_count_and_flags = (insns_count & kInsnsSizeMask) << kInsnsSizeShift;
        insns_count &= ~kInsnsSizeMask;

        if (has_payloads) {
            boolean odd_shorts = (tries_size != 0) ^ (outs_size != 0) ^ (ins_size != 0)
                    ^ (registers_size != 0) ^ ((data_buffer.position() & 0b10) != 0);
            if (odd_shorts) {
                data_buffer.addPosition(2);
            }
        }

        if (tries_size != 0) {
            data_buffer.writeShort(tries_size);
            insns_count_and_flags |= kFlagPreHeaderTriesSize;
        }
        if (outs_size != 0) {
            data_buffer.writeShort(outs_size);
            insns_count_and_flags |= kFlagPreHeaderOutsSize;
        }
        if (ins_size != 0) {
            data_buffer.writeShort(ins_size);
            insns_count_and_flags |= kFlagPreHeaderInsSize;
        }
        if (registers_size != 0) {
            data_buffer.writeShort(registers_size);
            insns_count_and_flags |= kFlagPreHeaderRegistersSize;
        }
        if (insns_count != 0) {
            insns_count_and_flags |= kFlagPreHeaderInsnsSize;
            data_buffer.writeShort(insns_count >> 16);
            data_buffer.writeShort(insns_count);
        }

        int code_item_start = data_buffer.position();

        data_buffer.writeShort(fields);
        data_buffer.writeShort(insns_count_and_flags);

        return code_item_start;
    }

    public void writeCatchHandler(CatchHandler value) {
        var catch_all_addr = value.catch_all_addr();
        var elements = value.elements();
        if (elements.isEmpty() && catch_all_addr == null) {
            throw new IllegalStateException("Unable to write empty catch handler");
        }
        data_buffer.writeSLeb128(catch_all_addr == null ? elements.size() : -elements.size());
        for (var tmp : elements) {
            data_buffer.writeULeb128(getTypeIndex(tmp.getExceptionType()));
            data_buffer.writeULeb128(tmp.getAddress());
        }
        if (catch_all_addr != null) {
            data_buffer.writeULeb128(catch_all_addr);
        }
    }

    public void writeTryBlock(TryBlockContainer value, RandomOutput out,
                              HashMap<CatchHandler, Integer> handlers) {
        out.writeInt(value.value().getStartAddress());
        out.writeShort(value.value().getUnitCount());
        Integer offset = handlers.get(value.handler());
        if (offset == null) {
            throw new IllegalStateException(
                    "Unable to find offset for catch handler");
        }
        out.writeShort(offset);
    }

    public void writeCodeItem(CodeContainer value) {
        data_buffer.alignPosition(isCompact() ? COMPACT_CODE_ITEM_ALIGNMENT : CODE_ITEM_ALIGNMENT);

        var insns = value.value().getInstructions();
        var tries = value.tries();

        int registers_size = value.value().getRegisterCount();
        int ins_size = value.ins();
        int outs_size = value.outs();
        int tries_size = value.value().getTryBlocks().size();
        int insns_count = countCodeUnits(insns);
        boolean has_payloads = insns_count < 0;
        insns_count = has_payloads ? ~insns_count : insns_count;

        int code_item_start;

        if (isCompact()) {
            code_item_start = writePreHeader(registers_size, ins_size,
                    outs_size, tries_size, insns_count, has_payloads);
        } else {
            code_item_start = data_buffer.position();

            data_buffer.writeShort(registers_size);
            data_buffer.writeShort(ins_size);
            data_buffer.writeShort(outs_size);
            data_buffer.writeShort(tries_size);
            var debug_info = value.debug_info();
            data_buffer.writeInt(debug_info == null ?
                    NO_OFFSET : getDebugInfoOffset(debug_info));
            data_buffer.writeInt(insns_count);
        }

        int insns_start = data_buffer.position();
        for (var tmp : insns) {
            InstructionWriter.writeInstruction(tmp, this, data_buffer);
        }
        int insns_size = data_buffer.position() - insns_start;

        if (insns_size != insns_count * 2) {
            throw new IllegalStateException(String.format(
                    "Calculated instructions size(%s) != written bytes(%s)",
                    insns_count * 2, insns_size));
        }

        if (tries_size != 0) {
            data_buffer.fillZerosToAlignment(TRY_ITEM_ALIGNMENT);

            var tries_buffer = data_buffer.duplicate();
            data_buffer.addPosition(TRY_ITEM_SIZE * tries_size);

            HashMap<CatchHandler, Integer> handlers = new HashMap<>(tries_size);
            for (var tmp : tries) {
                handlers.put(tmp.handler(), null);
            }

            int handlers_start = data_buffer.position();
            data_buffer.writeULeb128(handlers.size());

            for (CatchHandler tmp : handlers.keySet()) {
                int handler_offset = data_buffer.position() - handlers_start;
                writeCatchHandler(tmp);
                handlers.replace(tmp, handler_offset);
            }

            for (var tmp : tries) {
                writeTryBlock(tmp, tries_buffer, handlers);
            }
        }

        code_items.replace(value, code_item_start);
    }

    public void writeCodeItemSection() {
        var size = code_items.size();
        if (size != 0) {
            data_buffer.alignPosition(isCompact() ? COMPACT_CODE_ITEM_ALIGNMENT : CODE_ITEM_ALIGNMENT);
            map.code_items_off = data_buffer.position();
            map.code_items_size = size;
        }
        for (var tmp : code_items.keySet()) {
            writeCodeItem(tmp);
        }
    }

    public void writeEncodedArrayData(EncodedArray value) {
        List<EncodedValue> array = value.getValue();
        data_buffer.writeULeb128(array.size());
        for (EncodedValue tmp : array) {
            writeEncodedValue(tmp);
        }
    }

    public void writeEncodedArray(EncodedArray value) {
        int start = data_buffer.position();
        writeEncodedArrayData(value);
        encoded_arrays.replace(value, start);
    }

    public void writeEncodedArraySection() {
        var size = encoded_arrays.size();
        if (size != 0) {
            map.encoded_arrays_off = data_buffer.position();
            map.encoded_arrays_size = size;
        }
        for (var tmp : encoded_arrays.keySet()) {
            writeEncodedArray(tmp);
        }
    }

    public void writeAnnotationElement(AnnotationElement value) {
        data_buffer.writeULeb128(getStringIndex(value.getName()));
        writeEncodedValue(value.getValue());
    }

    public void writeAnnotationData(CommonAnnotation value) {
        var elements = value.getElements();
        data_buffer.writeULeb128(getTypeIndex(value.getType()));
        data_buffer.writeULeb128(elements.size());
        for (var tmp : elements) {
            writeAnnotationElement(tmp);
        }
    }

    public void writeAnnotation(Annotation value) {
        int start = data_buffer.position();
        data_buffer.writeByte(value.getVisibility().value());
        writeAnnotationData(value);
        annotations.replace(value, start);
    }

    public void writeAnnotationSection() {
        var size = annotations.size();
        if (size != 0) {
            map.annotations_off = data_buffer.position();
            map.annotations_size = size;
        }
        for (var tmp : annotations.keySet()) {
            writeAnnotation(tmp);
        }
    }

    public void writeAnnotationSet(NavigableSet<Annotation> value) {
        data_buffer.alignPosition(ANNOTATION_SET_ALIGNMENT);
        int start = data_buffer.position();
        data_buffer.writeInt(value.size());
        for (var tmp : value) {
            data_buffer.writeInt(getAnnotationOffset(tmp));
        }
        annotation_sets.replace(value, start);
    }

    public void writeAnnotationSetSection() {
        var size = annotation_sets.size();
        if (size != 0) {
            data_buffer.alignPosition(ANNOTATION_SET_ALIGNMENT);
            map.annotation_sets_off = data_buffer.position();
            map.annotation_sets_size = size;
        }
        for (var tmp : annotation_sets.keySet()) {
            writeAnnotationSet(tmp);
        }
    }

    public void writeAnnotationSetList(List<NavigableSet<Annotation>> value) {
        data_buffer.alignPosition(ANNOTATION_SET_LIST_ALIGNMENT);
        int start = data_buffer.position();
        data_buffer.writeInt(value.size());
        for (var tmp : value) {
            data_buffer.writeInt(tmp == null ?
                    NO_OFFSET : getAnnotationSetOffset(tmp));
        }
        annotation_set_lists.replace(value, start);
    }

    public void writeAnnotationSetListSection() {
        var size = annotation_set_lists.size();
        if (size != 0) {
            data_buffer.alignPosition(ANNOTATION_SET_LIST_ALIGNMENT);
            map.annotation_set_lists_off = data_buffer.position();
            map.annotation_set_lists_size = size;
        }
        for (var tmp : annotation_set_lists.keySet()) {
            writeAnnotationSetList(tmp);
        }
    }

    public void writeAnnotationDirectory(AnnotationDirectory value) {
        data_buffer.alignPosition(ANNOTATION_DIRECTORY_ALIGNMENT);
        int start = data_buffer.position();

        var class_annotations = value.class_annotations();
        data_buffer.writeInt(class_annotations == null ? NO_OFFSET
                : getAnnotationSetOffset(class_annotations));

        data_buffer.writeInt(value.field_annotations().size());
        data_buffer.writeInt(value.method_annotations().size());
        data_buffer.writeInt(value.parameter_annotations().size());

        for (var tmp : value.field_annotations().entrySet()) {
            data_buffer.writeInt(getFieldIndex(tmp.getKey()));
            data_buffer.writeInt(getAnnotationSetOffset(tmp.getValue()));
        }

        for (var tmp : value.method_annotations().entrySet()) {
            data_buffer.writeInt(getMethodIndex(tmp.getKey()));
            data_buffer.writeInt(getAnnotationSetOffset(tmp.getValue()));
        }

        for (var tmp : value.parameter_annotations().entrySet()) {
            data_buffer.writeInt(getMethodIndex(tmp.getKey()));
            data_buffer.writeInt(getAnnotationSetListOffset(tmp.getValue()));
        }

        annotation_directories.replace(value, start);
    }

    public void writeAnnotationDirectorySection() {
        var size = annotation_directories.size();
        if (size != 0) {
            data_buffer.alignPosition(ANNOTATION_DIRECTORY_ALIGNMENT);
            map.annotation_directories_off = data_buffer.position();
            map.annotation_directories_size = size;
        }
        for (var tmp : annotation_directories.keySet()) {
            writeAnnotationDirectory(tmp);
        }
    }

    public void writeEncodedValue(EncodedValue value) {
        var type = value.getValueType();
        var type_int = type.value();
        switch (type) {
            case BOOLEAN -> data_buffer.writeByte(
                    type_int | (((EncodedBoolean) value).getValue() ? 1 : 0) << 5);
            case BYTE -> ValueCoder.writeSignedIntegralValue(
                    data_buffer, type, ((EncodedByte) value).getValue());
            case SHORT -> ValueCoder.writeSignedIntegralValue(
                    data_buffer, type, ((EncodedShort) value).getValue());
            case CHAR -> ValueCoder.writeUnsignedIntegralValue(
                    data_buffer, type, ((EncodedChar) value).getValue());
            case INT -> ValueCoder.writeSignedIntegralValue(
                    data_buffer, type, ((EncodedInt) value).getValue());
            case FLOAT -> ValueCoder.writeRightZeroExtendedValue(data_buffer, type,
                    ((long) Float.floatToRawIntBits(((EncodedFloat) value).getValue())) << 32);
            case LONG -> ValueCoder.writeSignedIntegralValue(
                    data_buffer, type, ((EncodedLong) value).getValue());
            case DOUBLE -> ValueCoder.writeRightZeroExtendedValue(data_buffer, type,
                    Double.doubleToRawLongBits(((EncodedDouble) value).getValue()));
            case NULL -> data_buffer.writeByte(type_int);
            case STRING -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getStringIndex(((EncodedString) value).getValue()));
            case TYPE -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getTypeIndex(((EncodedType) value).getValue()));
            case FIELD -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getFieldIndex(((EncodedField) value).getValue()));
            case ENUM -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getFieldIndex(((EncodedEnum) value).getValue()));
            case METHOD -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getMethodIndex(((EncodedMethod) value).getValue()));
            case METHOD_TYPE -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getProtoIndex(((EncodedMethodType) value).getValue()));
            case METHOD_HANDLE -> ValueCoder.writeUnsignedIntegralValue(data_buffer, type,
                    getMethodHandleIndex(((EncodedMethodHandle) value).getValue()));
            case ARRAY -> {
                data_buffer.writeByte(type_int);
                writeEncodedArrayData((EncodedArray) value);
            }
            case ANNOTATION -> {
                data_buffer.writeByte(type_int);
                writeAnnotationData((EncodedAnnotation) value);
            }
        }
    }

    private int[][] getHiddenApiFlags() {
        boolean section_empty = true;
        int[][] out = new int[class_defs.length][];
        for (int i = 0; i < out.length; i++) {
            boolean def_empty = true;
            var def = class_defs[i];
            int[] arr = new int[def.static_fields().length + def.instance_fields().length +
                    def.direct_methods().length + def.virtual_methods().length];
            int index = 0;
            for (var tmp : def.static_fields()) {
                int flags = tmp.value().getHiddenApiFlags();
                def_empty &= flags == 0;
                arr[index++] = flags;
            }
            for (var tmp : def.instance_fields()) {
                int flags = tmp.value().getHiddenApiFlags();
                def_empty &= flags == 0;
                arr[index++] = flags;
            }
            for (var tmp : def.direct_methods()) {
                int flags = tmp.value().getHiddenApiFlags();
                def_empty &= flags == 0;
                arr[index++] = flags;
            }
            for (var tmp : def.virtual_methods()) {
                int flags = tmp.value().getHiddenApiFlags();
                def_empty &= flags == 0;
                arr[index++] = flags;
            }
            out[i] = def_empty ? null : arr;
            section_empty &= def_empty;
        }
        return section_empty ? null : out;
    }

    public void writeHiddenApiSection() {
        if (hiddenapi_flags == null) return;
        data_buffer.alignPosition(HIDDENAPI_ALIGNMENT);
        int start = data_buffer.position();
        data_buffer.addPosition(4); // size
        RandomOutput offsets = data_buffer.duplicate();
        int offsets_size = hiddenapi_flags.length * 4;
        data_buffer.addPosition(offsets_size);
        for (int[] tmp : hiddenapi_flags) {
            if (tmp == null) {
                offsets.writeInt(0);
                continue;
            }

            offsets.writeInt(data_buffer.position() - start);
            for (int flag : tmp) {
                data_buffer.writeULeb128(flag);
            }
        }
        offsets.position(start);
        offsets.writeInt(data_buffer.position() - start);
        map.hiddenapi_class_data_items_off = start;
    }
}
