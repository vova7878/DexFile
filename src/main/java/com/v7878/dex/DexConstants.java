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

public final class DexConstants {
    private DexConstants() {
    }

    public static final int ENDIAN_CONSTANT = 0x12345678;
    public static final int NO_INDEX = -1;

    //access_flags definitions
    public static final int ACC_PUBLIC = 0x1;
    public static final int ACC_PRIVATE = 0x2;
    public static final int ACC_PROTECTED = 0x4;
    public static final int ACC_STATIC = 0x8;
    public static final int ACC_FINAL = 0x10;
    public static final int ACC_SYNCHRONIZED = 0x20;
    public static final int ACC_VOLATILE = 0x40;
    public static final int ACC_BRIDGE = 0x40;
    public static final int ACC_TRANSIENT = 0x80;
    public static final int ACC_VARARGS = 0x80;
    public static final int ACC_NATIVE = 0x100;
    public static final int ACC_INTERFACE = 0x200;
    public static final int ACC_ABSTRACT = 0x400;
    public static final int ACC_STRICT = 0x800;
    public static final int ACC_SYNTHETIC = 0x1000;
    public static final int ACC_ANNOTATION = 0x2000;
    public static final int ACC_ENUM = 0x4000;
    public static final int ACC_CONSTRUCTOR = 0x10000;
    public static final int ACC_DECLARED_SYNCHRONIZED = 0x20000;

    //Type Codes
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

    //Value formats
    public static final int VALUE_BYTE = 0x00;
    public static final int VALUE_SHORT = 0x02;
    public static final int VALUE_CHAR = 0x03;
    public static final int VALUE_INT = 0x04;
    public static final int VALUE_LONG = 0x06;
    public static final int VALUE_FLOAT = 0x10;
    public static final int VALUE_DOUBLE = 0x11;
    public static final int VALUE_METHOD_TYPE = 0x15;
    public static final int VALUE_METHOD_HANDLE = 0x16;
    public static final int VALUE_STRING = 0x17;
    public static final int VALUE_TYPE = 0x18;
    public static final int VALUE_FIELD = 0x19;
    public static final int VALUE_METHOD = 0x1a;
    public static final int VALUE_ENUM = 0x1b;
    public static final int VALUE_ARRAY = 0x1c;
    public static final int VALUE_ANNOTATION = 0x1d;
    public static final int VALUE_NULL = 0x1e;
    public static final int VALUE_BOOLEAN = 0x1f;

    //Method Handle Type Codes
    public static final int METHOD_HANDLE_TYPE_STATIC_PUT = 0x00;
    public static final int METHOD_HANDLE_TYPE_STATIC_GET = 0x01;
    public static final int METHOD_HANDLE_TYPE_INSTANCE_PUT = 0x02;
    public static final int METHOD_HANDLE_TYPE_INSTANCE_GET = 0x03;
    public static final int METHOD_HANDLE_TYPE_INVOKE_STATIC = 0x04;
    public static final int METHOD_HANDLE_TYPE_INVOKE_INSTANCE = 0x05;
    public static final int METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR = 0x06;
    public static final int METHOD_HANDLE_TYPE_INVOKE_DIRECT = 0x07;
    public static final int METHOD_HANDLE_TYPE_INVOKE_INTERFACE = 0x08;

    public enum DexVersion {
        V035(1, '0', '3', '5'),
        V037(24, '0', '3', '7'),
        V038(26, '0', '3', '8'),
        V039(28, '0', '3', '9'),
        V040(34, '0', '4', '0');

        private final int minApi;
        private final int value;

        private static int getValue(int first, int second, int third) {
            return first << 16 | second << 8 | third;
        }

        DexVersion(int minApi, int first, int second, int third) {
            this.minApi = minApi;
            this.value = getValue(first, second, third);
        }

        public int getMinApi() {
            return minApi;
        }

        public byte firstByte() {
            return (byte) ((value >> 16) & 0xff);
        }

        public byte secondByte() {
            return (byte) ((value >> 8) & 0xff);
        }

        public byte thirdByte() {
            return (byte) (value & 0xff);
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
            int value = getValue(first, second, third);

            for (DexVersion version : values()) {
                if (value == version.value) {
                    return version;
                }
            }

            throw new IllegalArgumentException("unknown dex version: \""
                    + (char) (first & 0xff) + (char) (second & 0xff) + (char) (third & 0xff) + "\"");
        }
    }
}
