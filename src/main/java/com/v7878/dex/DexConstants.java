package com.v7878.dex;

public final class DexConstants {
    private DexConstants() {
    }

    public static final int ENDIAN_CONSTANT = 0x12345678;
    public static final int NO_INDEX = -1;
    public static final int NO_OFFSET = 0;

    // access_flags definitions
    public static final int ACC_PUBLIC = 0x1;
    public static final int ACC_PRIVATE = 0x2;
    public static final int ACC_PROTECTED = 0x4;
    public static final int ACC_STATIC = 0x8;
    public static final int ACC_FINAL = 0x10;
    public static final int ACC_SYNCHRONIZED = 0x20;
    public static final int ACC_SUPER = 0x20;
    public static final int ACC_VOLATILE = 0x40;
    public static final int ACC_BRIDGE = 0x40;
    public static final int ACC_TRANSIENT = 0x80;
    public static final int ACC_VARARGS = 0x80;
    public static final int ACC_NATIVE = 0x100;
    public static final int ACC_INTERFACE = 0x200;
    public static final int ACC_ABSTRACT = 0x400;
    public static final int ACC_STRICTFP = 0x800;
    public static final int ACC_SYNTHETIC = 0x1000;
    public static final int ACC_ANNOTATION = 0x2000;
    public static final int ACC_ENUM = 0x4000;
    public static final int ACC_MANDATED = 0x8000;
    public static final int ACC_CONSTRUCTOR = 0x10000;
    public static final int ACC_DECLARED_SYNCHRONIZED = 0x20000;

    public static final int ACC_VISIBILITY_MASK = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED;
    public static final int ACC_DIRECT_MASK = ACC_PRIVATE | ACC_STATIC | ACC_CONSTRUCTOR;

    // Type Codes
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
    public static final int TYPE_DEBUG_INFO_ITEM = 0x2003;
    public static final int TYPE_ANNOTATION_ITEM = 0x2004;
    public static final int TYPE_ENCODED_ARRAY_ITEM = 0x2005;
    public static final int TYPE_ANNOTATIONS_DIRECTORY_ITEM = 0x2006;
    public static final int TYPE_HIDDENAPI_CLASS_DATA_ITEM = 0xF000;

    // Value formats
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

    // Method Handle Type Codes
    public static final int METHOD_HANDLE_TYPE_STATIC_PUT = 0x00;
    public static final int METHOD_HANDLE_TYPE_STATIC_GET = 0x01;
    public static final int METHOD_HANDLE_TYPE_INSTANCE_PUT = 0x02;
    public static final int METHOD_HANDLE_TYPE_INSTANCE_GET = 0x03;
    public static final int METHOD_HANDLE_TYPE_INVOKE_STATIC = 0x04;
    public static final int METHOD_HANDLE_TYPE_INVOKE_INSTANCE = 0x05;
    public static final int METHOD_HANDLE_TYPE_INVOKE_CONSTRUCTOR = 0x06;
    public static final int METHOD_HANDLE_TYPE_INVOKE_DIRECT = 0x07;
    public static final int METHOD_HANDLE_TYPE_INVOKE_INTERFACE = 0x08;

    // Visibility values
    public static final int VISIBILITY_BUILD = 0x00;
    public static final int VISIBILITY_RUNTIME = 0x01;
    public static final int VISIBILITY_SYSTEM = 0x02;

    // Restriction flags
    public static final int HIDDENAPI_FLAG_SDK = 0;
    public static final int HIDDENAPI_FLAG_UNSUPPORTED = 1;
    public static final int HIDDENAPI_FLAG_BLOCKED = 2;
    public static final int HIDDENAPI_FLAG_MAX_TARGET_O = 3;
    public static final int HIDDENAPI_FLAG_MAX_TARGET_P = 4;
    public static final int HIDDENAPI_FLAG_MAX_TARGET_Q = 5;
    public static final int HIDDENAPI_FLAG_MAX_TARGET_R = 6;
    public static final int HIDDENAPI_FLAG_MAX_TARGET_S = 7;

    // Debug byte code values
    public static final int DBG_END_SEQUENCE = 0x00;
    public static final int DBG_ADVANCE_PC = 0x01;
    public static final int DBG_ADVANCE_LINE = 0x02;
    public static final int DBG_START_LOCAL = 0x03;
    public static final int DBG_START_LOCAL_EXTENDED = 0x04;
    public static final int DBG_END_LOCAL = 0x05;
    public static final int DBG_RESTART_LOCAL = 0x06;
    public static final int DBG_SET_PROLOGUE_END = 0x07;
    public static final int DBG_SET_EPILOGUE_BEGIN = 0x08;
    public static final int DBG_SET_FILE = 0x09;
    public static final int DBG_FIRST_SPECIAL = 0x0a;
    public static final int DBG_LAST_SPECIAL = 0xff;
    public static final int DBG_LINE_BASE = -4;
    public static final int DBG_LINE_RANGE = 15;
    public static final int DBG_LINE_CEIL = DBG_LINE_BASE + DBG_LINE_RANGE - 1;
}
