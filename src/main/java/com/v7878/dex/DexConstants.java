package com.v7878.dex;

public class DexConstants {

    public static final int ENDIAN_CONSTANT = 0x12345678;
    public static final int NO_INDEX = -1;

    //map_list
    public static final int TYPE_HEADER_ITEM = 0x0000;
    public static final int TYPE_STRING_ID_ITEM = 0x0001;
    public static final int TYPE_TYPE_ID_ITEM = 0x0002;
    public static final int TYPE_PROTO_ID_ITEM = 0x0003;
    public static final int TYPE_FIELD_ID_ITEM = 0x0004;
    public static final int TYPE_METHOD_ID_ITEM = 0x0005;
    public static final int TYPE_CLASS_DEF_ITEM = 0x0006;
    public static final int TYPE_CALL_SITE_ID_ITEM = 0x0007;
    public static final int TYPE_METHOD_HANDLE_ITEM = 0x0008;
    public static final int TYPE_MAP_LIST = 0x1000;
    public static final int TYPE_TYPE_LIST = 0x1001;
    public static final int TYPE_ANNOTATION_SET_REF_LIST = 0x1002;
    public static final int TYPE_ANNOTATION_SET_ITEM = 0x1003;
    public static final int TYPE_CLASS_DATA_ITEM = 0x2000;
    public static final int TYPE_CODE_ITEM = 0x2001;
    public static final int TYPE_STRING_DATA_ITEM = 0x2002;
    //TODO
    public static final int TYPE_DEBUG_INFO_ITEM = 0x2003;
    public static final int TYPE_ANNOTATION_ITEM = 0x2004;
    public static final int TYPE_ENCODED_ARRAY_ITEM = 0x2005;
    public static final int TYPE_ANNOTATIONS_DIRECTORY_ITEM = 0x2006;
    //TODO?
    public static final int TYPE_HIDDENAPI_CLASS_DATA_ITEM = 0xF000;

    public enum DexVersion {
        V035(1, (byte) '0', (byte) '3', (byte) '5'),
        V037(24, (byte) '0', (byte) '3', (byte) '7'),
        V038(26, (byte) '0', (byte) '3', (byte) '8'),
        V039(28, (byte) '0', (byte) '3', (byte) '9'),
        V040(34, (byte) '0', (byte) '4', (byte) '0');
        private final int minApi;

        private final byte first;
        private final byte second;
        private final byte third;

        DexVersion(int minApi, byte first, byte second, byte third) {
            this.minApi = minApi;
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public int getMinApi() {
            return minApi;
        }

        public byte firstByte() {
            return first;
        }

        public byte secondByte() {
            return second;
        }

        public byte thirdByte() {
            return third;
        }

        public static DexVersion fromApi(int api) {
            if (api < V037.minApi) {
                return V035;
            }
            if (api < V038.minApi) {
                return V037;
            }
            if (api < V039.minApi) {
                return V038;
            }
            if (api < V040.minApi) {
                return V039;
            }
            return V040;
        }

        public static DexVersion fromBytes(byte first, byte second, byte third) {
            if (first == V035.first && second == V035.second && third == V035.third) {
                return V035;
            }
            if (first == V037.first && second == V037.second && third == V037.third) {
                return V037;
            }
            if (first == V038.first && second == V038.second && third == V038.third) {
                return V038;
            }
            if (first == V039.first && second == V039.second && third == V039.third) {
                return V039;
            }
            if (first == V040.first && second == V040.second && third == V040.third) {
                return V040;
            }
            throw new IllegalArgumentException("unknown dex version: \""
                    + (char) (first & 0xff) + (char) (second & 0xff) + (char) (third & 0xff) + "\"");
        }
    }
}
