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

import static com.v7878.dex.DexConstants.COMPACT_HEADER_SIZE;
import static com.v7878.dex.DexConstants.DATA_SECTION_ALIGNMENT;
import static com.v7878.dex.DexConstants.HEADER_SIZE;
import static com.v7878.dex.DexVersion.forMagic;
import static com.v7878.misc.Math.roundUp;

import com.v7878.dex.io.RandomIO;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.zip.Adler32;

// Temporary object. Needed to read or write
class FileMap {

    public record MapItem(int type, int offset, int size) {
        public static final Comparator<MapItem> COMPARATOR = Comparator.comparingInt(a -> a.offset);

        public static MapItem read(RandomInput in) {
            int type = in.readUnsignedShort();
            in.addPosition(2); //unused
            int size = in.readInt();
            int offset = in.readInt();
            return new MapItem(type, offset, size);
        }

        public void write(RandomOutput out) {
            out.writeShort(type);
            out.writeShort(0);
            out.writeInt(size);
            out.writeInt(offset);
        }
    }

    public static final int MAP_ALIGNMENT = 4;

    public static final int CHECKSUM_OFFSET = 8;
    public static final int SIGNATURE_OFFSET = CHECKSUM_OFFSET + 4;
    public static final int FILE_SIZE_OFFSET = SIGNATURE_OFFSET + 20;

    public DexVersion version;

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

    public int map_list_off;

    public int data_size;
    public int data_off;

    public int type_lists_size;
    public int type_lists_off;
    public int annotation_set_refs_size;
    public int annotation_set_refs_off;
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
    public int annotations_directories_size;
    public int annotations_directories_off;

    public int hiddenapi_class_data_items_off;

    public int compact_feature_flags;
    public int compact_debug_info_offsets_pos;
    public int compact_debug_info_offsets_table_offset;
    public int compact_debug_info_base;

    private static int getHeaderSize(DexVersion version) {
        return switch (version) {
            case CDEX001 -> COMPACT_HEADER_SIZE;
            // TODO: case DEX041 -> HEADER_V41_SIZE;
            default -> HEADER_SIZE;
        };
    }

    private static void check(boolean value) {
        if (!value) {
            throw new IllegalStateException();
        }
    }

    public static FileMap readHeader(RandomInput in, ReadOptions options) {
        FileMap out = new FileMap();
        byte[] magic = in.readByteArray(8);
        DexVersion version = forMagic(magic);
        out.version = version;
        options.requireMinApi(version.getMinApi());
        in.addPosition(4); //checksum
        in.addPosition(20); //signature
        in.addPosition(4); //file_size
        int header_size = in.readInt();
        if (header_size != getHeaderSize(version)) {
            throw new IllegalStateException("invalid header size: " + header_size);
        }
        int endian_tag = in.readInt();
        if (endian_tag != DexConstants.ENDIAN_CONSTANT) {
            throw new IllegalStateException("invalid endian_tag: " + Integer.toHexString(endian_tag));
        }
        in.addPosition(4); //link_size
        in.addPosition(4); //link_off
        out.map_list_off = in.readInt();
        out.string_ids_size = in.readInt();
        out.string_ids_off = in.readInt();
        out.type_ids_size = in.readInt();
        out.type_ids_off = in.readInt();
        out.proto_ids_size = in.readInt();
        out.proto_ids_off = in.readInt();
        out.field_ids_size = in.readInt();
        out.field_ids_off = in.readInt();
        out.method_ids_size = in.readInt();
        out.method_ids_off = in.readInt();
        out.class_defs_size = in.readInt();
        out.class_defs_off = in.readInt();
        out.data_size = in.readInt();
        out.data_off = in.readInt();

        if (version.isCompact()) {
            out.compact_feature_flags = in.readInt();
            out.compact_debug_info_offsets_pos = in.readInt();
            out.compact_debug_info_offsets_table_offset = in.readInt();
            out.compact_debug_info_base = in.readInt();
            in.addPosition(4); //owned_data_begin
            in.addPosition(4); //owned_data_end
        }
        return out;
    }

    public void readMap(ReadContext context) {
        RandomInput in = context.data(map_list_off);
        int map_size = in.readInt();
        //TODO: messages
        for (int i = 0; i < map_size; i++) {
            MapItem item = MapItem.read(in);
            switch (item.type) {
                case DexConstants.TYPE_HEADER_ITEM -> {
                    check(item.size == 1);
                    check(item.offset == 0);
                }
                case DexConstants.TYPE_STRING_ID_ITEM -> {
                    check(item.size == string_ids_size);
                    check(item.offset == string_ids_off);
                }
                case DexConstants.TYPE_TYPE_ID_ITEM -> {
                    check(item.size == type_ids_size);
                    check(item.offset == type_ids_off);
                }
                case DexConstants.TYPE_PROTO_ID_ITEM -> {
                    check(item.size == proto_ids_size);
                    check(item.offset == proto_ids_off);
                }
                case DexConstants.TYPE_FIELD_ID_ITEM -> {
                    check(item.size == field_ids_size);
                    check(item.offset == field_ids_off);
                }
                case DexConstants.TYPE_METHOD_ID_ITEM -> {
                    check(item.size == method_ids_size);
                    check(item.offset == method_ids_off);
                }
                case DexConstants.TYPE_CLASS_DEF_ITEM -> {
                    check(item.size == class_defs_size);
                    check(item.offset == class_defs_off);
                }
                case DexConstants.TYPE_CALL_SITE_ID_ITEM -> {
                    call_site_ids_size = item.size;
                    call_site_ids_off = item.offset;
                }
                case DexConstants.TYPE_METHOD_HANDLE_ITEM -> {
                    method_handles_size = item.size;
                    method_handles_off = item.offset;
                }
                case DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM -> {
                    check(item.size == 1);
                    hiddenapi_class_data_items_off = item.offset;
                }
                case DexConstants.TYPE_MAP_LIST -> {
                    check(item.size == 1);
                    check(item.offset == map_list_off);
                }
                case DexConstants.TYPE_TYPE_LIST, DexConstants.TYPE_ANNOTATION_SET_REF_LIST,
                     DexConstants.TYPE_ANNOTATION_SET_ITEM, DexConstants.TYPE_CLASS_DATA_ITEM,
                     DexConstants.TYPE_CODE_ITEM, DexConstants.TYPE_STRING_DATA_ITEM,
                     DexConstants.TYPE_DEBUG_INFO_ITEM, DexConstants.TYPE_ANNOTATION_ITEM,
                     DexConstants.TYPE_ENCODED_ARRAY_ITEM,
                     DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM -> { /* ok */ }
                default -> throw new IllegalStateException("unknown map_item type: " + item.type);
            }
        }
    }

    public void computeHeaderInfo(WriteContextImpl context) {
        version = context.getDexVersion();

        int offset = getHeaderSize(version);

        string_ids_off = offset;
        string_ids_size = context.strings().length;
        offset += string_ids_size * StringId.SIZE;

        type_ids_off = offset;
        type_ids_size = context.types().length;
        offset += type_ids_size * TypeId.SIZE;

        proto_ids_off = offset;
        proto_ids_size = context.protos().length;
        offset += proto_ids_size * ProtoId.SIZE;

        field_ids_off = offset;
        field_ids_size = context.fields().length;
        offset += field_ids_size * FieldId.SIZE;

        method_ids_off = offset;
        method_ids_size = context.methods().length;
        offset += method_ids_size * MethodId.SIZE;

        class_defs_off = offset;
        class_defs_size = context.classDefs().length;
        offset += class_defs_size * ClassDef.SIZE;

        call_site_ids_off = offset;
        call_site_ids_size = context.callSites().length;
        offset += call_site_ids_size * CallSiteId.SIZE;

        method_handles_off = offset;
        method_handles_size = context.methodHandles().length;
        offset += method_handles_size * MethodHandleItem.SIZE;

        data_off = roundUp(offset, DATA_SECTION_ALIGNMENT);
    }

    public void writeMap(RandomOutput out) {
        out.alignPosition(MAP_ALIGNMENT);
        map_list_off = (int) out.position();
        ArrayList<MapItem> list = new ArrayList<>();

        // main section
        list.add(new MapItem(DexConstants.TYPE_HEADER_ITEM, 0, 1));
        if (string_ids_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_STRING_ID_ITEM,
                    string_ids_off, string_ids_size));
        }
        if (type_ids_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_TYPE_ID_ITEM,
                    type_ids_off, type_ids_size));
        }
        if (proto_ids_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_PROTO_ID_ITEM,
                    proto_ids_off, proto_ids_size));
        }
        if (field_ids_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_FIELD_ID_ITEM,
                    field_ids_off, field_ids_size));
        }
        if (method_ids_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_METHOD_ID_ITEM,
                    method_ids_off, method_ids_size));
        }
        if (class_defs_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_CLASS_DEF_ITEM,
                    class_defs_off, class_defs_size));
        }
        if (call_site_ids_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_CALL_SITE_ID_ITEM,
                    call_site_ids_off, call_site_ids_size));
        }
        if (method_handles_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_METHOD_HANDLE_ITEM,
                    method_handles_off, method_handles_size));
        }

        // data section
        if (type_lists_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_TYPE_LIST,
                    type_lists_off, type_lists_size));
        }
        if (annotation_set_refs_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_ANNOTATION_SET_REF_LIST,
                    annotation_set_refs_off, annotation_set_refs_size));
        }
        if (annotation_sets_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_ANNOTATION_SET_ITEM,
                    annotation_sets_off, annotation_sets_size));
        }
        if (class_data_items_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_CLASS_DATA_ITEM,
                    class_data_items_off, class_data_items_size));
        }
        if (code_items_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_CODE_ITEM,
                    code_items_off, code_items_size));
        }
        if (string_data_items_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_STRING_DATA_ITEM,
                    string_data_items_off, string_data_items_size));
        }
        if (debug_info_items_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_DEBUG_INFO_ITEM,
                    debug_info_items_off, debug_info_items_size));
        }
        if (annotations_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_ANNOTATION_ITEM,
                    annotations_off, annotations_size));
        }
        if (encoded_arrays_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_ENCODED_ARRAY_ITEM,
                    encoded_arrays_off, encoded_arrays_size));
        }
        if (annotations_directories_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM,
                    annotations_directories_off, annotations_directories_size));
        }
        if (hiddenapi_class_data_items_off > 0) {
            list.add(new MapItem(DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM,
                    hiddenapi_class_data_items_off, 1));
        }

        list.add(new MapItem(DexConstants.TYPE_MAP_LIST, map_list_off, 1));

        list.sort(MapItem.COMPARATOR);

        out.writeInt(list.size());
        for (MapItem tmp : list) {
            tmp.write(out);
        }
    }

    public void writeHeader(RandomIO out, int file_size) {
        out.writeByteArray(version.getMagic());
        out.addPosition(4); //checksum
        out.addPosition(20); //signature
        out.writeInt(file_size);
        out.writeInt(getHeaderSize(version));
        out.writeInt(DexConstants.ENDIAN_CONSTANT);
        out.writeInt(0); //link_size
        out.writeInt(0); //link_off

        out.writeInt(map_list_off);
        out.writeInt(string_ids_size);
        out.writeInt(string_ids_size > 0 ? string_ids_off : 0);
        out.writeInt(type_ids_size);
        out.writeInt(type_ids_size > 0 ? type_ids_off : 0);
        out.writeInt(proto_ids_size);
        out.writeInt(proto_ids_size > 0 ? proto_ids_off : 0);
        out.writeInt(field_ids_size);
        out.writeInt(field_ids_size > 0 ? field_ids_off : 0);
        out.writeInt(method_ids_size);
        out.writeInt(method_ids_size > 0 ? method_ids_off : 0);
        out.writeInt(class_defs_size);
        out.writeInt(class_defs_size > 0 ? class_defs_off : 0);
        out.writeInt(data_size);
        out.writeInt(data_size > 0 ? data_off : 0);

        if (version.isCompact()) {
            out.writeInt(0x1 /* kDefaultMethods */); //compact_feature_flags TODO: add option
            out.writeInt(0); //compact_debug_info_offsets_pos
            out.writeInt(0); //compact_debug_info_offsets_table_offset
            out.writeInt(0); //compact_debug_info_base
            out.writeInt(0); //owned_data_begin
            out.writeInt(data_size); //owned_data_end
        }

        if (version.isCompact()) {
            // TODO: How are the checksum and signature fields calculated for compact dex?
        } else {
            out.position(SIGNATURE_OFFSET);
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("unable to find SHA-1 MessageDigest", e);
            }
            byte[] signature = md.digest(out.duplicate(FILE_SIZE_OFFSET)
                    .readByteArray(file_size - FILE_SIZE_OFFSET));
            out.writeByteArray(signature);

            out.position(CHECKSUM_OFFSET);
            Adler32 adler = new Adler32();
            int adler_length = file_size - SIGNATURE_OFFSET;
            adler.update(out.duplicate(SIGNATURE_OFFSET)
                    .readByteArray(adler_length), 0, adler_length);
            out.writeInt((int) adler.getValue());
        }
    }
}
