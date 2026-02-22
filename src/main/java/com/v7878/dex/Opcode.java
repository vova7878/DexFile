package com.v7878.dex;

import static com.v7878.dex.DexVersion.DEX038;
import static com.v7878.dex.DexVersion.DEX039;
import static com.v7878.dex.Format.ArrayPayload;
import static com.v7878.dex.Format.Format10t;
import static com.v7878.dex.Format.Format10x;
import static com.v7878.dex.Format.Format11n;
import static com.v7878.dex.Format.Format11x;
import static com.v7878.dex.Format.Format12x;
import static com.v7878.dex.Format.Format20t;
import static com.v7878.dex.Format.Format21c;
import static com.v7878.dex.Format.Format21ih;
import static com.v7878.dex.Format.Format21lh;
import static com.v7878.dex.Format.Format21s;
import static com.v7878.dex.Format.Format21t;
import static com.v7878.dex.Format.Format22b;
import static com.v7878.dex.Format.Format22c22cs;
import static com.v7878.dex.Format.Format22s;
import static com.v7878.dex.Format.Format22t;
import static com.v7878.dex.Format.Format22x;
import static com.v7878.dex.Format.Format23x;
import static com.v7878.dex.Format.Format30t;
import static com.v7878.dex.Format.Format31c;
import static com.v7878.dex.Format.Format31i;
import static com.v7878.dex.Format.Format31t;
import static com.v7878.dex.Format.Format32x;
import static com.v7878.dex.Format.Format35c35mi35ms;
import static com.v7878.dex.Format.Format3rc3rmi3rms;
import static com.v7878.dex.Format.Format45cc;
import static com.v7878.dex.Format.Format4rcc;
import static com.v7878.dex.Format.Format51l;
import static com.v7878.dex.Format.FormatRaw;
import static com.v7878.dex.Format.PackedSwitchPayload;
import static com.v7878.dex.Format.SparseSwitchPayload;
import static com.v7878.dex.Opcode.Constants.CAN_INITIALIZE_REFERENCE;
import static com.v7878.dex.Opcode.Constants.CAN_THROW;
import static com.v7878.dex.Opcode.Constants.ENDS_FLOW;
import static com.v7878.dex.Opcode.Constants.HAS_PAYLOAD;
import static com.v7878.dex.Opcode.Constants.ODEX_ONLY;
import static com.v7878.dex.Opcode.Constants.TYPE_BRANCH;
import static com.v7878.dex.Opcode.Constants.TYPE_INVOKE;
import static com.v7878.dex.Opcode.Constants.TYPE_RETURN;
import static com.v7878.dex.Opcode.Constants.TYPE_SWITCH;
import static com.v7878.dex.Opcode.Constants.UNCONDITIONAL;
import static com.v7878.dex.ReferenceType.CALLSITE;
import static com.v7878.dex.ReferenceType.FIELD;
import static com.v7878.dex.ReferenceType.METHOD;
import static com.v7878.dex.ReferenceType.METHOD_HANDLE;
import static com.v7878.dex.ReferenceType.PROTO;
import static com.v7878.dex.ReferenceType.RAW_INDEX;
import static com.v7878.dex.ReferenceType.STRING;
import static com.v7878.dex.ReferenceType.TYPE;

public enum Opcode {
    NOP(0x00, "nop", Format10x, 0),
    MOVE(0x01, "move", Format12x, 0),
    MOVE_FROM16(0x02, "move/from16", Format22x, 0),
    MOVE_16(0x03, "move/16", Format32x, 0),
    MOVE_WIDE(0x04, "move-wide", Format12x, 0),
    MOVE_WIDE_FROM16(0x05, "move-wide/from16", Format22x, 0),
    MOVE_WIDE_16(0x06, "move-wide/16", Format32x, 0),
    MOVE_OBJECT(0x07, "move-object", Format12x, 0),
    MOVE_OBJECT_FROM16(0x08, "move-object/from16", Format22x, 0),
    MOVE_OBJECT_16(0x09, "move-object/16", Format32x, 0),
    MOVE_RESULT(0x0a, "move-result", Format11x, 0),
    MOVE_RESULT_WIDE(0x0b, "move-result-wide", Format11x, 0),
    MOVE_RESULT_OBJECT(0x0c, "move-result-object", Format11x, 0),
    MOVE_EXCEPTION(0x0d, "move-exception", Format11x, 0),
    RETURN_VOID(0x0e, "return-void", Format10x, TYPE_RETURN | ENDS_FLOW),
    RETURN(0x0f, "return", Format11x, TYPE_RETURN | ENDS_FLOW),
    RETURN_WIDE(0x10, "return-wide", Format11x, TYPE_RETURN | ENDS_FLOW),
    RETURN_OBJECT(0x11, "return-object", Format11x, TYPE_RETURN | ENDS_FLOW),
    CONST_4(0x12, "const/4", Format11n, 0),
    CONST_16(0x13, "const/16", Format21s, 0),
    CONST(0x14, "const", Format31i, 0),
    CONST_HIGH16(0x15, "const/high16", Format21ih, 0),
    CONST_WIDE_16(0x16, "const-wide/16", Format21s, 0),
    CONST_WIDE_32(0x17, "const-wide/32", Format31i, 0),
    CONST_WIDE(0x18, "const-wide", Format51l, 0),
    CONST_WIDE_HIGH16(0x19, "const-wide/high16", Format21lh, 0),
    CONST_STRING(0x1a, "const-string", Format21c, STRING, CAN_THROW),
    CONST_STRING_JUMBO(0x1b, "const-string/jumbo", Format31c, STRING, CAN_THROW),
    CONST_CLASS(0x1c, "const-class", Format21c, TYPE, CAN_THROW),
    MONITOR_ENTER(0x1d, "monitor-enter", Format11x, CAN_THROW),
    MONITOR_EXIT(0x1e, "monitor-exit", Format11x, CAN_THROW),
    CHECK_CAST(0x1f, "check-cast", Format21c, TYPE, CAN_THROW),
    INSTANCE_OF(0x20, "instance-of", Format22c22cs, TYPE, CAN_THROW),
    ARRAY_LENGTH(0x21, "array-length", Format12x, CAN_THROW),
    NEW_INSTANCE(0x22, "new-instance", Format21c, TYPE, CAN_THROW),
    NEW_ARRAY(0x23, "new-array", Format22c22cs, TYPE, CAN_THROW),
    FILLED_NEW_ARRAY(0x24, "filled-new-array", Format35c35mi35ms, TYPE, CAN_THROW),
    FILLED_NEW_ARRAY_RANGE(0x25, "filled-new-array/range", Format3rc3rmi3rms, TYPE, CAN_THROW),
    FILL_ARRAY_DATA(0x26, "fill-array-data", Format31t, CAN_THROW | HAS_PAYLOAD),
    THROW(0x27, "throw", Format11x, CAN_THROW | ENDS_FLOW),
    GOTO(0x28, "goto", Format10t, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    GOTO_16(0x29, "goto/16", Format20t, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    GOTO_32(0x2a, "goto/32", Format30t, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    PACKED_SWITCH(0x2b, "packed-switch", Format31t, HAS_PAYLOAD | TYPE_SWITCH),
    SPARSE_SWITCH(0x2c, "sparse-switch", Format31t, HAS_PAYLOAD | TYPE_SWITCH),

    CMPL_FLOAT(0x2d, "cmpl-float", Format23x, 0),
    CMPG_FLOAT(0x2e, "cmpg-float", Format23x, 0),
    CMPL_DOUBLE(0x2f, "cmpl-double", Format23x, 0),
    CMPG_DOUBLE(0x30, "cmpg-double", Format23x, 0),
    CMP_LONG(0x31, "cmp-long", Format23x, 0),

    IF_EQ(0x32, "if-eq", Format22t, TYPE_BRANCH),
    IF_NE(0x33, "if-ne", Format22t, TYPE_BRANCH),
    IF_LT(0x34, "if-lt", Format22t, TYPE_BRANCH),
    IF_GE(0x35, "if-ge", Format22t, TYPE_BRANCH),
    IF_GT(0x36, "if-gt", Format22t, TYPE_BRANCH),
    IF_LE(0x37, "if-le", Format22t, TYPE_BRANCH),

    IF_EQZ(0x38, "if-eqz", Format21t, TYPE_BRANCH),
    IF_NEZ(0x39, "if-nez", Format21t, TYPE_BRANCH),
    IF_LTZ(0x3a, "if-ltz", Format21t, TYPE_BRANCH),
    IF_GEZ(0x3b, "if-gez", Format21t, TYPE_BRANCH),
    IF_GTZ(0x3c, "if-gtz", Format21t, TYPE_BRANCH),
    IF_LEZ(0x3d, "if-lez", Format21t, TYPE_BRANCH),

    // 3e - 43 unused

    AGET(0x44, "aget", Format23x, CAN_THROW),
    AGET_WIDE(0x45, "aget-wide", Format23x, CAN_THROW),
    AGET_OBJECT(0x46, "aget-object", Format23x, CAN_THROW),
    AGET_BOOLEAN(0x47, "aget-boolean", Format23x, CAN_THROW),
    AGET_BYTE(0x48, "aget-byte", Format23x, CAN_THROW),
    AGET_CHAR(0x49, "aget-char", Format23x, CAN_THROW),
    AGET_SHORT(0x4a, "aget-short", Format23x, CAN_THROW),
    APUT(0x4b, "aput", Format23x, CAN_THROW),
    APUT_WIDE(0x4c, "aput-wide", Format23x, CAN_THROW),
    APUT_OBJECT(0x4d, "aput-object", Format23x, CAN_THROW),
    APUT_BOOLEAN(0x4e, "aput-boolean", Format23x, CAN_THROW),
    APUT_BYTE(0x4f, "aput-byte", Format23x, CAN_THROW),
    APUT_CHAR(0x50, "aput-char", Format23x, CAN_THROW),
    APUT_SHORT(0x51, "aput-short", Format23x, CAN_THROW),

    IGET(0x52, "iget", Format22c22cs, FIELD, CAN_THROW),
    IGET_WIDE(0x53, "iget-wide", Format22c22cs, FIELD, CAN_THROW),
    IGET_OBJECT(0x54, "iget-object", Format22c22cs, FIELD, CAN_THROW),
    IGET_BOOLEAN(0x55, "iget-boolean", Format22c22cs, FIELD, CAN_THROW),
    IGET_BYTE(0x56, "iget-byte", Format22c22cs, FIELD, CAN_THROW),
    IGET_CHAR(0x57, "iget-char", Format22c22cs, FIELD, CAN_THROW),
    IGET_SHORT(0x58, "iget-short", Format22c22cs, FIELD, CAN_THROW),
    IPUT(0x59, "iput", Format22c22cs, FIELD, CAN_THROW),
    IPUT_WIDE(0x5a, "iput-wide", Format22c22cs, FIELD, CAN_THROW),
    IPUT_OBJECT(0x5b, "iput-object", Format22c22cs, FIELD, CAN_THROW),
    IPUT_BOOLEAN(0x5c, "iput-boolean", Format22c22cs, FIELD, CAN_THROW),
    IPUT_BYTE(0x5d, "iput-byte", Format22c22cs, FIELD, CAN_THROW),
    IPUT_CHAR(0x5e, "iput-char", Format22c22cs, FIELD, CAN_THROW),
    IPUT_SHORT(0x5f, "iput-short", Format22c22cs, FIELD, CAN_THROW),

    SGET(0x60, "sget", Format21c, FIELD, CAN_THROW),
    SGET_WIDE(0x61, "sget-wide", Format21c, FIELD, CAN_THROW),
    SGET_OBJECT(0x62, "sget-object", Format21c, FIELD, CAN_THROW),
    SGET_BOOLEAN(0x63, "sget-boolean", Format21c, FIELD, CAN_THROW),
    SGET_BYTE(0x64, "sget-byte", Format21c, FIELD, CAN_THROW),
    SGET_CHAR(0x65, "sget-char", Format21c, FIELD, CAN_THROW),
    SGET_SHORT(0x66, "sget-short", Format21c, FIELD, CAN_THROW),
    SPUT(0x67, "sput", Format21c, FIELD, CAN_THROW),
    SPUT_WIDE(0x68, "sput-wide", Format21c, FIELD, CAN_THROW),
    SPUT_OBJECT(0x69, "sput-object", Format21c, FIELD, CAN_THROW),
    SPUT_BOOLEAN(0x6a, "sput-boolean", Format21c, FIELD, CAN_THROW),
    SPUT_BYTE(0x6b, "sput-byte", Format21c, FIELD, CAN_THROW),
    SPUT_CHAR(0x6c, "sput-char", Format21c, FIELD, CAN_THROW),
    SPUT_SHORT(0x6d, "sput-short", Format21c, FIELD, CAN_THROW),

    INVOKE_VIRTUAL(0x6e, "invoke-virtual", Format35c35mi35ms, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER(0x6f, "invoke-super", Format35c35mi35ms, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_DIRECT(0x70, "invoke-direct", Format35c35mi35ms, METHOD, CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_STATIC(0x71, "invoke-static", Format35c35mi35ms, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_INTERFACE(0x72, "invoke-interface", Format35c35mi35ms, METHOD, CAN_THROW | TYPE_INVOKE),

    // 73 unused

    INVOKE_VIRTUAL_RANGE(0x74, "invoke-virtual/range", Format3rc3rmi3rms, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER_RANGE(0x75, "invoke-super/range", Format3rc3rmi3rms, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_DIRECT_RANGE(0x76, "invoke-direct/range", Format3rc3rmi3rms, METHOD, CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_STATIC_RANGE(0x77, "invoke-static/range", Format3rc3rmi3rms, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_INTERFACE_RANGE(0x78, "invoke-interface/range", Format3rc3rmi3rms, METHOD, CAN_THROW | TYPE_INVOKE),

    // 79 - 7a unused

    NEG_INT(0x7b, "neg-int", Format12x, 0),
    NOT_INT(0x7c, "not-int", Format12x, 0),
    NEG_LONG(0x7d, "neg-long", Format12x, 0),
    NOT_LONG(0x7e, "not-long", Format12x, 0),
    NEG_FLOAT(0x7f, "neg-float", Format12x, 0),
    NEG_DOUBLE(0x80, "neg-double", Format12x, 0),
    INT_TO_LONG(0x81, "int-to-long", Format12x, 0),
    INT_TO_FLOAT(0x82, "int-to-float", Format12x, 0),
    INT_TO_DOUBLE(0x83, "int-to-double", Format12x, 0),
    LONG_TO_INT(0x84, "long-to-int", Format12x, 0),
    LONG_TO_FLOAT(0x85, "long-to-float", Format12x, 0),
    LONG_TO_DOUBLE(0x86, "long-to-double", Format12x, 0),
    FLOAT_TO_INT(0x87, "float-to-int", Format12x, 0),
    FLOAT_TO_LONG(0x88, "float-to-long", Format12x, 0),
    FLOAT_TO_DOUBLE(0x89, "float-to-double", Format12x, 0),
    DOUBLE_TO_INT(0x8a, "double-to-int", Format12x, 0),
    DOUBLE_TO_LONG(0x8b, "double-to-long", Format12x, 0),
    DOUBLE_TO_FLOAT(0x8c, "double-to-float", Format12x, 0),
    INT_TO_BYTE(0x8d, "int-to-byte", Format12x, 0),
    INT_TO_CHAR(0x8e, "int-to-char", Format12x, 0),
    INT_TO_SHORT(0x8f, "int-to-short", Format12x, 0),

    ADD_INT(0x90, "add-int", Format23x, 0),
    SUB_INT(0x91, "sub-int", Format23x, 0),
    MUL_INT(0x92, "mul-int", Format23x, 0),
    DIV_INT(0x93, "div-int", Format23x, CAN_THROW),
    REM_INT(0x94, "rem-int", Format23x, CAN_THROW),
    AND_INT(0x95, "and-int", Format23x, 0),
    OR_INT(0x96, "or-int", Format23x, 0),
    XOR_INT(0x97, "xor-int", Format23x, 0),
    SHL_INT(0x98, "shl-int", Format23x, 0),
    SHR_INT(0x99, "shr-int", Format23x, 0),
    USHR_INT(0x9a, "ushr-int", Format23x, 0),
    ADD_LONG(0x9b, "add-long", Format23x, 0),
    SUB_LONG(0x9c, "sub-long", Format23x, 0),
    MUL_LONG(0x9d, "mul-long", Format23x, 0),
    DIV_LONG(0x9e, "div-long", Format23x, CAN_THROW),
    REM_LONG(0x9f, "rem-long", Format23x, CAN_THROW),
    AND_LONG(0xa0, "and-long", Format23x, 0),
    OR_LONG(0xa1, "or-long", Format23x, 0),
    XOR_LONG(0xa2, "xor-long", Format23x, 0),
    SHL_LONG(0xa3, "shl-long", Format23x, 0),
    SHR_LONG(0xa4, "shr-long", Format23x, 0),
    USHR_LONG(0xa5, "ushr-long", Format23x, 0),
    ADD_FLOAT(0xa6, "add-float", Format23x, 0),
    SUB_FLOAT(0xa7, "sub-float", Format23x, 0),
    MUL_FLOAT(0xa8, "mul-float", Format23x, 0),
    DIV_FLOAT(0xa9, "div-float", Format23x, 0),
    REM_FLOAT(0xaa, "rem-float", Format23x, 0),
    ADD_DOUBLE(0xab, "add-double", Format23x, 0),
    SUB_DOUBLE(0xac, "sub-double", Format23x, 0),
    MUL_DOUBLE(0xad, "mul-double", Format23x, 0),
    DIV_DOUBLE(0xae, "div-double", Format23x, 0),
    REM_DOUBLE(0xaf, "rem-double", Format23x, 0),

    ADD_INT_2ADDR(0xb0, "add-int/2addr", Format12x, 0),
    SUB_INT_2ADDR(0xb1, "sub-int/2addr", Format12x, 0),
    MUL_INT_2ADDR(0xb2, "mul-int/2addr", Format12x, 0),
    DIV_INT_2ADDR(0xb3, "div-int/2addr", Format12x, CAN_THROW),
    REM_INT_2ADDR(0xb4, "rem-int/2addr", Format12x, CAN_THROW),
    AND_INT_2ADDR(0xb5, "and-int/2addr", Format12x, 0),
    OR_INT_2ADDR(0xb6, "or-int/2addr", Format12x, 0),
    XOR_INT_2ADDR(0xb7, "xor-int/2addr", Format12x, 0),
    SHL_INT_2ADDR(0xb8, "shl-int/2addr", Format12x, 0),
    SHR_INT_2ADDR(0xb9, "shr-int/2addr", Format12x, 0),
    USHR_INT_2ADDR(0xba, "ushr-int/2addr", Format12x, 0),
    ADD_LONG_2ADDR(0xbb, "add-long/2addr", Format12x, 0),
    SUB_LONG_2ADDR(0xbc, "sub-long/2addr", Format12x, 0),
    MUL_LONG_2ADDR(0xbd, "mul-long/2addr", Format12x, 0),
    DIV_LONG_2ADDR(0xbe, "div-long/2addr", Format12x, CAN_THROW),
    REM_LONG_2ADDR(0xbf, "rem-long/2addr", Format12x, CAN_THROW),
    AND_LONG_2ADDR(0xc0, "and-long/2addr", Format12x, 0),
    OR_LONG_2ADDR(0xc1, "or-long/2addr", Format12x, 0),
    XOR_LONG_2ADDR(0xc2, "xor-long/2addr", Format12x, 0),
    SHL_LONG_2ADDR(0xc3, "shl-long/2addr", Format12x, 0),
    SHR_LONG_2ADDR(0xc4, "shr-long/2addr", Format12x, 0),
    USHR_LONG_2ADDR(0xc5, "ushr-long/2addr", Format12x, 0),
    ADD_FLOAT_2ADDR(0xc6, "add-float/2addr", Format12x, 0),
    SUB_FLOAT_2ADDR(0xc7, "sub-float/2addr", Format12x, 0),
    MUL_FLOAT_2ADDR(0xc8, "mul-float/2addr", Format12x, 0),
    DIV_FLOAT_2ADDR(0xc9, "div-float/2addr", Format12x, 0),
    REM_FLOAT_2ADDR(0xca, "rem-float/2addr", Format12x, 0),
    ADD_DOUBLE_2ADDR(0xcb, "add-double/2addr", Format12x, 0),
    SUB_DOUBLE_2ADDR(0xcc, "sub-double/2addr", Format12x, 0),
    MUL_DOUBLE_2ADDR(0xcd, "mul-double/2addr", Format12x, 0),
    DIV_DOUBLE_2ADDR(0xce, "div-double/2addr", Format12x, 0),
    REM_DOUBLE_2ADDR(0xcf, "rem-double/2addr", Format12x, 0),

    ADD_INT_LIT16(0xd0, "add-int/lit16", Format22s, 0),
    RSUB_INT(0xd1, "rsub-int", Format22s, 0),
    MUL_INT_LIT16(0xd2, "mul-int/lit16", Format22s, 0),
    DIV_INT_LIT16(0xd3, "div-int/lit16", Format22s, CAN_THROW),
    REM_INT_LIT16(0xd4, "rem-int/lit16", Format22s, CAN_THROW),
    AND_INT_LIT16(0xd5, "and-int/lit16", Format22s, 0),
    OR_INT_LIT16(0xd6, "or-int/lit16", Format22s, 0),
    XOR_INT_LIT16(0xd7, "xor-int/lit16", Format22s, 0),

    ADD_INT_LIT8(0xd8, "add-int/lit8", Format22b, 0),
    RSUB_INT_LIT8(0xd9, "rsub-int/lit8", Format22b, 0),
    MUL_INT_LIT8(0xda, "mul-int/lit8", Format22b, 0),
    DIV_INT_LIT8(0xdb, "div-int/lit8", Format22b, CAN_THROW),
    REM_INT_LIT8(0xdc, "rem-int/lit8", Format22b, CAN_THROW),
    AND_INT_LIT8(0xdd, "and-int/lit8", Format22b, 0),
    OR_INT_LIT8(0xde, "or-int/lit8", Format22b, 0),
    XOR_INT_LIT8(0xdf, "xor-int/lit8", Format22b, 0),
    SHL_INT_LIT8(0xe0, "shl-int/lit8", Format22b, 0),
    SHR_INT_LIT8(0xe1, "shr-int/lit8", Format22b, 0),
    USHR_INT_LIT8(0xe2, "ushr-int/lit8", Format22b, 0),

    // e3 - f9 unused

    INVOKE_POLYMORPHIC(firstDex(DEX038, firstApi(0xfa, 26)), "invoke-polymorphic", Format45cc, METHOD, PROTO, CAN_THROW | TYPE_INVOKE),
    INVOKE_POLYMORPHIC_RANGE(firstDex(DEX038, firstApi(0xfb, 26)), "invoke-polymorphic/range", Format4rcc, METHOD, PROTO, CAN_THROW | TYPE_INVOKE),
    INVOKE_CUSTOM(firstDex(DEX038, firstApi(0xfc, 26)), "invoke-custom", Format35c35mi35ms, CALLSITE, CAN_THROW | TYPE_INVOKE),
    INVOKE_CUSTOM_RANGE(firstDex(DEX038, firstApi(0xfd, 26)), "invoke-custom/range", Format3rc3rmi3rms, CALLSITE, CAN_THROW | TYPE_INVOKE),
    CONST_METHOD_HANDLE(firstDex(DEX039, firstApi(0xfe, 28)), "const-method-handle", Format21c, METHOD_HANDLE, CAN_THROW),
    CONST_METHOD_TYPE(firstDex(DEX039, firstApi(0xff, 28)), "const-method-type", Format21c, PROTO, CAN_THROW),

    PACKED_SWITCH_PAYLOAD(0x100, "packed-switch-payload", PackedSwitchPayload, 0),
    SPARSE_SWITCH_PAYLOAD(0x200, "sparse-switch-payload", SparseSwitchPayload, 0),
    ARRAY_PAYLOAD(0x300, "array-payload", ArrayPayload, 0),

    // odex opcodes

    EXECUTE_INLINE(dalvikOnly(0xee), "execute-inline", Format35c35mi35ms, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    EXECUTE_INLINE_RANGE(dalvikOnly(firstApi(0xef, 8)), "execute-inline/range", Format3rc3rmi3rms, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),

    INVOKE_DIRECT_EMPTY(dalvikOnly(lastApi(0xf0, 13)), "invoke-direct-empty", Format35c35mi35ms, METHOD, ODEX_ONLY | CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_OBJECT_INIT_RANGE(dalvikOnly(firstApi(0xf0, 14)), "invoke-object-init/range", Format3rc3rmi3rms, METHOD, ODEX_ONLY | CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),

    IGET_QUICK(value(0xf2), lastApi(0xe3, 30), "iget-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_WIDE_QUICK(value(0xf3), lastApi(0xe4, 30), "iget-wide-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_OBJECT_QUICK(value(0xf4), lastApi(0xe5, 30), "iget-object-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_QUICK(value(0xf5), lastApi(0xe6, 30), "iput-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_WIDE_QUICK(value(0xf6), lastApi(0xe7, 30), "iput-wide-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_OBJECT_QUICK(value(0xf7), lastApi(0xe8, 30), "iput-object-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),

    INVOKE_VIRTUAL_QUICK(value(0xf8), lastApi(0xe9, 30), "invoke-virtual-quick", Format35c35mi35ms, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    INVOKE_VIRTUAL_QUICK_RANGE(value(0xf9), lastApi(0xea, 30), "invoke-virtual-quick/range", Format3rc3rmi3rms, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER_QUICK(dalvikOnly(0xfa), "invoke-super-quick", Format35c35mi35ms, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER_QUICK_RANGE(dalvikOnly(0xfb), "invoke-super-quick/range", Format3rc3rmi3rms, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),

    //TODO THROW_VERIFICATION_ERROR(onlyDalvik(firstApi(0xed, 5)), "throw-verification-error", Format20bc, ODEX_ONLY | CAN_THROW | END_FLOW),

    // Note: breakpoint replaces the original opcode and restores it back when executed (it only exists at runtime)
    // BREAKPOINT(onlyDalvik(firstApi(0xec, 8)), "breakpoint", Format00x, RUNTIME_ONLY),

    IGET_VOLATILE(dalvikOnly(firstApi(0xe3, 9)), "iget-volatile", Format22c22cs, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_VOLATILE(dalvikOnly(firstApi(0xe4, 9)), "iput-volatile", Format22c22cs, FIELD, ODEX_ONLY | CAN_THROW),
    SGET_VOLATILE(dalvikOnly(firstApi(0xe5, 9)), "sget-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_VOLATILE(dalvikOnly(firstApi(0xe6, 9)), "sput-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),

    IGET_WIDE_VOLATILE(dalvikOnly(firstApi(0xe8, 9)), "iget-wide-volatile", Format22c22cs, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_WIDE_VOLATILE(dalvikOnly(firstApi(0xe9, 9)), "iput-wide-volatile", Format22c22cs, FIELD, ODEX_ONLY | CAN_THROW),
    SGET_WIDE_VOLATILE(dalvikOnly(firstApi(0xea, 9)), "sget-wide-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_WIDE_VOLATILE(dalvikOnly(firstApi(0xeb, 9)), "sput-wide-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),

    IGET_OBJECT_VOLATILE(dalvikOnly(firstApi(0xe7, 9)), "iget-object-volatile", Format22c22cs, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_OBJECT_VOLATILE(dalvikOnly(firstApi(0xfc, 9)), "iput-object-volatile", Format22c22cs, FIELD, ODEX_ONLY | CAN_THROW),
    SGET_OBJECT_VOLATILE(dalvikOnly(firstApi(0xfd, 9)), "sget-object-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_OBJECT_VOLATILE(dalvikOnly(firstApi(0xfe, 9)), "sput-object-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),

    RETURN_VOID_BARRIER(firstApi(0xf1, 11), lastApi(0x73, 22), "return-void-barrier", Format10x, ODEX_ONLY | TYPE_RETURN | ENDS_FLOW),
    RETURN_VOID_NO_BARRIER(artOnly(betweenApi(0x73, 23, 30)), "return-void-no-barrier", Format10x, ODEX_ONLY | TYPE_RETURN | ENDS_FLOW),

    IPUT_BOOLEAN_QUICK(artOnly(betweenApi(0xeb, 23, 30)), "iput-boolean-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_BYTE_QUICK(artOnly(betweenApi(0xec, 23, 30)), "iput-byte-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_CHAR_QUICK(artOnly(betweenApi(0xed, 23, 30)), "iput-char-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_SHORT_QUICK(artOnly(betweenApi(0xee, 23, 30)), "iput-short-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_BOOLEAN_QUICK(artOnly(betweenApi(0xef, 23, 30)), "iget-boolean-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_BYTE_QUICK(artOnly(betweenApi(0xf0, 23, 30)), "iget-byte-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_CHAR_QUICK(artOnly(betweenApi(0xf1, 23, 30)), "iget-char-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_SHORT_QUICK(artOnly(betweenApi(0xf2, 23, 30)), "iget-short-quick", Format22c22cs, RAW_INDEX, ODEX_ONLY | CAN_THROW),

    // experimental lambda opcodes

    //TODO INVOKE_LAMBDA(onlyArt(betweenApi(0xf3, 24, 25)), "invoke-lambda", Format25x, EXPERIMENTAL_LAMBDA),
    //TODO CAPTURE_VARIABLE(onlyArt(betweenApi(0xf5, 24, 25)), "capture-variable", Format21c(string), EXPERIMENTAL_LAMBDA),
    //TODO CREATE_LAMBDA(onlyArt(betweenApi(0xf6, 24, 25)), "create-lambda", Format21c(method), EXPERIMENTAL_LAMBDA),
    //TODO LIBERATE_LAMBDA(onlyArt(betweenApi(0xf7, 24, 25)), "liberate-lambda", Format22c(string), EXPERIMENTAL_LAMBDA),
    //TODO BOX_LAMBDA(onlyArt(betweenApi(0xf8, 24, 25)), "box-lambda", Format22x, EXPERIMENTAL_LAMBDA),
    //TODO UNBOX_LAMBDA(onlyArt(betweenApi(0xf9, 24, 25)), "unbox-lambda", Format22c(type), EXPERIMENTAL_LAMBDA)

    RAW(raw(), "raw", FormatRaw, 0);

    protected static class Constants {
        // a flavor of invoke
        static final int TYPE_INVOKE = 0x1;
        // returns, no additional statements
        static final int TYPE_RETURN = 0x2;
        // switch statement
        static final int TYPE_SWITCH = 0x4;
        // conditional or unconditional branch
        static final int TYPE_BRANCH = 0x8;
        // unconditional branch
        static final int UNCONDITIONAL = 0x10;
        // can initialize an uninitialized object reference
        static final int CAN_INITIALIZE_REFERENCE = 0x20;
        // with supplemental data
        static final int HAS_PAYLOAD = 0x40;
        // flow can`t continue to next statement
        static final int ENDS_FLOW = 0x80;
        // could cause an exception to be thrown
        static final int CAN_THROW = 0x100;
        // odex only instruction
        static final int ODEX_ONLY = 0x200;
    }

    record DexInfo(int api, boolean art, boolean odex, DexVersion dex) {
    }

    static abstract class Constraint {
        public abstract boolean contains(int opcode);

        public abstract Integer opcode(DexInfo info);

        @Override
        public abstract String toString();
    }

    private final Constraint constraint;
    private final String name;
    private final Format format;
    private final ReferenceType reference1;
    private final ReferenceType reference2;
    private final int flags;

    Opcode(int opcodeValue, String name, Format format, int flags) {
        this(value(opcodeValue), name, format, null, flags);
    }

    Opcode(int opcodeValue, String name, Format format, ReferenceType reference, int flags) {
        this(value(opcodeValue), name, format, reference, flags);
    }

    Opcode(Constraint constraint, String name, Format format, int flags) {
        this(constraint, name, format, null, flags);
    }

    Opcode(Constraint constraint, String name, Format format, ReferenceType reference, int flags) {
        this(constraint, name, format, reference, null, flags);
    }

    Opcode(Constraint dalvik, Constraint art, String name, Format format, int flags) {
        this(dalvik, art, name, format, null, flags);
    }

    Opcode(Constraint dalvik, Constraint art, String name, Format format, ReferenceType reference, int flags) {
        this(combine(dalvikOnly(dalvik), artOnly(art)), name, format, reference, null, flags);
    }

    Opcode(Constraint constraint, String name, Format format, ReferenceType reference1, ReferenceType reference2, int flags) {
        this.flags = flags;
        this.constraint = odexOnly() ? onlyOdex(constraint) : constraint;
        this.name = name;
        this.format = format;
        this.reference1 = reference1;
        this.reference2 = reference2;
    }

    public String opname() {
        return name;
    }

    public Format format() {
        return format;
    }

    public int getUnitCount() {
        return format.getUnitCount();
    }

    public ReferenceType getReferenceType1() {
        return reference1;
    }

    public ReferenceType getReferenceType2() {
        return reference2;
    }

    public final boolean canThrow() {
        return (flags & CAN_THROW) != 0;
    }

    public final boolean odexOnly() {
        return (flags & ODEX_ONLY) != 0;
    }

    public final boolean endsFlow() {
        return (flags & ENDS_FLOW) != 0;
    }

    public final boolean canContinue() {
        return !endsFlow();
    }

    public final boolean canInitializeReference() {
        return (flags & CAN_INITIALIZE_REFERENCE) != 0;
    }

    public final boolean isInvoke() {
        return (flags & TYPE_INVOKE) != 0;
    }

    public final boolean setsResult() {
        return isInvoke();
    }

    public final boolean isReturn() {
        return (flags & TYPE_RETURN) != 0;
    }

    public final boolean isSwitch() {
        return (flags & TYPE_SWITCH) != 0;
    }

    public final boolean isBranch() {
        return (flags & TYPE_BRANCH) != 0;
    }

    public final boolean isConditionalBranch() {
        int c = TYPE_BRANCH | UNCONDITIONAL;
        return (flags & c) == TYPE_BRANCH;
    }

    public final boolean isUnconditionalBranch() {
        int c = TYPE_BRANCH | UNCONDITIONAL;
        return (flags & c) == c;
    }

    public final boolean hasPayload() {
        return (flags & HAS_PAYLOAD) != 0;
    }

    public final boolean isPayload() {
        return format.isPayload();
    }

    public final boolean isVariableRegister() {
        return format == Format35c35mi35ms
                || format == Format3rc3rmi3rms
                || format == Format45cc
                || format == Format4rcc;
    }

    public final boolean isRaw() {
        return this == RAW;
    }

    Integer getValue(DexVersion dex, int api, boolean art, boolean odex) {
        return constraint.opcode(new DexInfo(api, art, odex, dex));
    }

    Constraint getConstraint() {
        return constraint;
    }

    private static Constraint value(int opcodeValue) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return opcode == opcodeValue;
            }

            @Override
            public Integer opcode(DexInfo info) {
                return opcodeValue;
            }

            @Override
            public String toString() {
                return Integer.toHexString(opcodeValue);
            }
        };
    }

    private static Constraint raw() {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return false;
            }

            @Override
            public Integer opcode(DexInfo info) {
                return null;
            }

            @Override
            public String toString() {
                return "raw -> false";
            }
        };
    }

    private static Constraint firstApi(Constraint constraint, int minApi) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return info.api() < minApi ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "api >= " + minApi + " -> {" + constraint + "}";
            }
        };
    }

    private static Constraint firstApi(int opcodeValue, int minApi) {
        return firstApi(value(opcodeValue), minApi);
    }

    private static Constraint lastApi(Constraint constraint, int maxApi) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return info.api() > maxApi ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "api <= " + maxApi + " -> {" + constraint + "}";
            }
        };
    }

    private static Constraint lastApi(int opcodeValue, int maxApi) {
        return lastApi(value(opcodeValue), maxApi);
    }

    private static Constraint betweenApi(Constraint constraint, int minApi, int maxApi) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                int api = info.api();
                return (api < minApi || api > maxApi) ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "(" + minApi + " <= api <= " + maxApi + ") -> {" + constraint + "}";
            }
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static Constraint betweenApi(int opcodeValue, int minApi, int maxApi) {
        return betweenApi(value(opcodeValue), minApi, maxApi);
    }

    private static boolean checkDexVersion(DexVersion min, DexVersion target) {
        return target.isCompact() || (min.ordinal() >= target.ordinal());
    }

    private static Constraint firstDex(DexVersion version, Constraint constraint) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return !checkDexVersion(version, info.dex()) ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "(cdex || dex >= " + version + ") -> {" + constraint + "}";
            }
        };
    }

    private static Constraint onlyOdex(Constraint constraint) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return !info.odex() ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "odex -> {" + constraint + "}";
            }
        };
    }

    private static Constraint artOnly(Constraint constraint) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return !info.art() ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "art -> {" + constraint + "}";
            }
        };
    }

    private static Constraint dalvikOnly(Constraint constraint) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return info.art() ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "dalvik -> {" + constraint + "}";
            }
        };
    }

    private static Constraint dalvikOnly(int opcodeValue) {
        return dalvikOnly(value(opcodeValue));
    }

    private static Constraint combine(Constraint first, Constraint second) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return first.contains(opcode) || second.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                Integer opcode = first.opcode(info);
                return opcode == null ? second.opcode(info) : opcode;
            }

            @Override
            public String toString() {
                return "<" + first + " | " + second + ">";
            }
        };
    }
}
