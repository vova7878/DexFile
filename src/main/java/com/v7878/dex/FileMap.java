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

import com.v7878.dex.DexConstants.DexVersion;
import com.v7878.dex.io.RandomIO;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.Adler32;

// Temporary object. Needed to read or write
class FileMap {

    public static class MapItem {

        public static final Comparator<MapItem> COMPARATOR = Comparator.comparingInt(a -> a.offset);

        public final int type;
        public final int offset;
        public final int size;

        public MapItem(int type, int offset, int size) {
            this.type = type;
            this.offset = offset;
            this.size = size;
        }

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

    public static final int HEADER_SIZE = 0x70;
    public static final int MAP_ALIGNMENT = 4;

    public static final int CHECKSUM_OFFSET = 8;
    public static final int SIGNATURE_OFFSET = CHECKSUM_OFFSET + 4;
    public static final int FILE_SIZE_OFFSET = SIGNATURE_OFFSET + 20;

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

    //extra data for write
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
    public int hiddenapi_class_data_items_size;
    public int hiddenapi_class_data_items_off;

    private static void throwISE(boolean value, String msg) {
        if (!value) {
            throw new IllegalStateException(msg);
        }
    }

    private static void throwISE(boolean value) {
        if (!value) {
            throw new IllegalStateException();
        }
    }

    public static FileMap read(RandomInput in, ReadOptions options) {
        FileMap out = new FileMap();
        byte[] magic = in.readByteArray(8);
        if (magic[0] == 'd' && magic[1] == 'e' && magic[2] == 'x' && magic[3] == '\n' && magic[7] == '\0') {
            // ok, regular dex file
        } else if (magic[0] == 'c' && magic[1] == 'd' && magic[2] == 'e' && magic[3] == 'x' && magic[7] == '\0') {
            //TODO: cdex reading
            throw new UnsupportedOperationException("cdex reading unsupported yet");
        } else {
            throw new IllegalStateException("invalid magic: " + Arrays.toString(magic));
        }
        DexVersion version = DexVersion.fromBytes(magic[4], magic[5], magic[6]);
        options.requireMinApi(version.getMinApi());
        in.addPosition(4); //checksum
        in.addPosition(20); //signature
        in.addPosition(4); //file_size
        int header_size = in.readInt();
        if (header_size != HEADER_SIZE) {
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
        in.addPosition(4); //data_size
        in.addPosition(4); //data_off
        RandomInput in2 = in.duplicate(out.map_list_off);
        int map_size = in2.readInt();
        //TODO: messages
        for (int i = 0; i < map_size; i++) {
            MapItem item = MapItem.read(in2);
            switch (item.type) {
                case DexConstants.TYPE_HEADER_ITEM:
                    throwISE(item.size == 1);
                    throwISE(item.offset == 0);
                    break;
                case DexConstants.TYPE_STRING_ID_ITEM:
                    throwISE(item.size == out.string_ids_size);
                    throwISE(item.offset == out.string_ids_off);
                    break;
                case DexConstants.TYPE_TYPE_ID_ITEM:
                    throwISE(item.size == out.type_ids_size);
                    throwISE(item.offset == out.type_ids_off);
                    break;
                case DexConstants.TYPE_PROTO_ID_ITEM:
                    throwISE(item.size == out.proto_ids_size);
                    throwISE(item.offset == out.proto_ids_off);
                    break;
                case DexConstants.TYPE_FIELD_ID_ITEM:
                    throwISE(item.size == out.field_ids_size);
                    throwISE(item.offset == out.field_ids_off);
                    break;
                case DexConstants.TYPE_METHOD_ID_ITEM:
                    throwISE(item.size == out.method_ids_size);
                    throwISE(item.offset == out.method_ids_off);
                    break;
                case DexConstants.TYPE_CLASS_DEF_ITEM:
                    throwISE(item.size == out.class_defs_size);
                    throwISE(item.offset == out.class_defs_off);
                    break;
                case DexConstants.TYPE_CALL_SITE_ID_ITEM:
                    out.call_site_ids_size = item.size;
                    out.call_site_ids_off = item.offset;
                    break;
                case DexConstants.TYPE_METHOD_HANDLE_ITEM:
                    out.method_handles_size = item.size;
                    out.method_handles_off = item.offset;
                    break;
                case DexConstants.TYPE_MAP_LIST:
                    throwISE(item.size == 1);
                    break;
                case DexConstants.TYPE_TYPE_LIST:
                case DexConstants.TYPE_ANNOTATION_SET_REF_LIST:
                case DexConstants.TYPE_ANNOTATION_SET_ITEM:
                case DexConstants.TYPE_CLASS_DATA_ITEM:
                case DexConstants.TYPE_CODE_ITEM:
                case DexConstants.TYPE_STRING_DATA_ITEM:
                case DexConstants.TYPE_DEBUG_INFO_ITEM:
                case DexConstants.TYPE_ANNOTATION_ITEM:
                case DexConstants.TYPE_ENCODED_ARRAY_ITEM:
                case DexConstants.TYPE_ANNOTATIONS_DIRECTORY_ITEM:
                case DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM:
                    break; // ok
                default:
                    throw new IllegalStateException("unknown map_item type: " + item.type);
            }
        }
        return out;
    }

    public void writeMap(RandomOutput out) {
        map_list_off = (int) out.position();
        ArrayList<MapItem> list = new ArrayList<>();
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
        if (hiddenapi_class_data_items_size > 0) {
            list.add(new MapItem(DexConstants.TYPE_HIDDENAPI_CLASS_DATA_ITEM,
                    hiddenapi_class_data_items_off, hiddenapi_class_data_items_size));
        }

        list.add(new MapItem(DexConstants.TYPE_MAP_LIST, map_list_off, 1));

        list.sort(MapItem.COMPARATOR);

        out.writeInt(list.size());
        for (MapItem tmp : list) {
            tmp.write(out);
        }
    }

    public void writeHeader(RandomIO out, WriteOptions options, int file_size) {
        out.position(0);
        out.writeByteArray(new byte[]{'d', 'e', 'x', '\n'});
        DexVersion version = DexVersion.fromApi(options.getTargetApi());
        out.writeByteArray(new byte[]{version.firstByte(),
                version.secondByte(), version.thirdByte(), '\0'});
        out.addPosition(4); //checksum
        out.addPosition(20); //signature
        out.writeInt(file_size);
        out.writeInt(HEADER_SIZE);
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
