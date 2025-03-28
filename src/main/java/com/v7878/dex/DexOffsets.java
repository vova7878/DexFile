package com.v7878.dex;

public final class DexOffsets {
    private DexOffsets() {
    }

    // header sizes
    public static final int BASE_HEADER_SIZE = 0x70;
    public static final int DEXCONTAINER_HEADER_SIZE = BASE_HEADER_SIZE + 4 * 2;
    public static final int COMPACT_HEADER_SIZE = BASE_HEADER_SIZE + 4 * 6;

    public static int getHeaderSize(DexVersion version) {
        return switch (version) {
            case CDEX001 -> COMPACT_HEADER_SIZE;
            case DEX041 -> DEXCONTAINER_HEADER_SIZE;
            default -> BASE_HEADER_SIZE;
        };
    }

    // dex element sizes
    public static final int STRING_ID_SIZE = 4;
    public static final int TYPE_ID_SIZE = 4;
    public static final int FIELD_ID_SIZE = 8;
    public static final int PROTO_ID_SIZE = 12;
    public static final int METHOD_ID_SIZE = 8;
    public static final int METHOD_HANDLE_ID_SIZE = 8;
    public static final int CALL_SITE_ID_SIZE = 4;
    public static final int CLASS_DEF_SIZE = 32;
    public static final int TRY_ITEM_SIZE = 8;

    // dex element alignments
    public static final int MAP_ALIGNMENT = 4;
    public static final int DATA_SECTION_ALIGNMENT = 8;
    public static final int TYPE_LIST_ALIGNMENT = 4;
    public static final int TRY_ITEM_ALIGNMENT = 4;
    public static final int CODE_ITEM_ALIGNMENT = 4;
    public static final int COMPACT_CODE_ITEM_ALIGNMENT = 2;
    public static final int PAYLOAD_INSTRUCTION_ALIGNMENT = 4;
    public static final int ANNOTATION_SET_ALIGNMENT = 4;
    public static final int ANNOTATION_SET_LIST_ALIGNMENT = 4;
    public static final int ANNOTATION_DIRECTORY_ALIGNMENT = 4;
    public static final int HIDDENAPI_ALIGNMENT = 4;
    public static final int COMPACT_OFFSET_TABLE_ALIGNMENT = 4;

    // header offsets
    public static final int MAGIC_OFFSET = 0;
    public static final int CHECKSUM_OFFSET = 8;
    public static final int CHECKSUM_DATA_START_OFFSET = 12;
    public static final int SIGNATURE_OFFSET = 12;
    public static final int SIGNATURE_SIZE = 20;
    public static final int SIGNATURE_DATA_START_OFFSET = 32;
    public static final int FILE_SIZE_OFFSET = 32;
    public static final int HEADER_SIZE_OFFSET = 36;
    public static final int ENDIAN_TAG_OFFSET = 40;
    public static final int MAP_OFFSET = 52;
    public static final int STRING_COUNT_OFFSET = 56;
    public static final int STRING_START_OFFSET = 60;
    public static final int TYPE_COUNT_OFFSET = 64;
    public static final int TYPE_START_OFFSET = 68;
    public static final int PROTO_COUNT_OFFSET = 72;
    public static final int PROTO_START_OFFSET = 76;
    public static final int FIELD_COUNT_OFFSET = 80;
    public static final int FIELD_START_OFFSET = 84;
    public static final int METHOD_COUNT_OFFSET = 88;
    public static final int METHOD_START_OFFSET = 92;
    public static final int CLASS_COUNT_OFFSET = 96;
    public static final int CLASS_START_OFFSET = 100;
    public static final int DATA_SIZE_OFFSET = 104;
    public static final int DATA_START_OFFSET = 108;

    public static final int CONTAINER_SIZE_OFFSET = 112;
    public static final int HEADER_OFF_OFFSET = 116;

    public static final int FEATURE_FLAGS_OFFSET = 112;
    public static final int DEBUG_INFO_OFFSETS_POS_OFFSET = 116;
    public static final int DEBUG_INFO_OFFSETS_TABLE_OFFSET = 120;
    public static final int DEBUG_INFO_BASE_OFFSET = 124;
}
