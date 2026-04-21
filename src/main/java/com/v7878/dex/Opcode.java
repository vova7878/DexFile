package com.v7878.dex;

import static com.v7878.dex.DexVersion.DEX009;
import static com.v7878.dex.DexVersion.DEX013;
import static com.v7878.dex.DexVersion.DEX035;
import static com.v7878.dex.Format.ArrayPayload;
import static com.v7878.dex.Format.Format10t;
import static com.v7878.dex.Format.Format10x;
import static com.v7878.dex.Format.Format11n;
import static com.v7878.dex.Format.Format11p;
import static com.v7878.dex.Format.Format11x;
import static com.v7878.dex.Format.Format12x;
import static com.v7878.dex.Format.Format20t;
import static com.v7878.dex.Format.Format20t_24;
import static com.v7878.dex.Format.Format21c;
import static com.v7878.dex.Format.Format21ih;
import static com.v7878.dex.Format.Format21lh;
import static com.v7878.dex.Format.Format21s;
import static com.v7878.dex.Format.Format21t;
import static com.v7878.dex.Format.Format22b;
import static com.v7878.dex.Format.Format22c;
import static com.v7878.dex.Format.Format22s;
import static com.v7878.dex.Format.Format22t;
import static com.v7878.dex.Format.Format22x;
import static com.v7878.dex.Format.Format23x;
import static com.v7878.dex.Format.Format30t;
import static com.v7878.dex.Format.Format31c;
import static com.v7878.dex.Format.Format31i;
import static com.v7878.dex.Format.Format31t;
import static com.v7878.dex.Format.Format32x;
import static com.v7878.dex.Format.Format34c;
import static com.v7878.dex.Format.Format35c;
import static com.v7878.dex.Format.Format3rc;
import static com.v7878.dex.Format.Format41c;
import static com.v7878.dex.Format.Format45cc;
import static com.v7878.dex.Format.Format4rcc;
import static com.v7878.dex.Format.Format51l;
import static com.v7878.dex.Format.Format52c;
import static com.v7878.dex.Format.Format5rc;
import static com.v7878.dex.Format.FormatRaw;
import static com.v7878.dex.Format.MPackedSwitchPayload;
import static com.v7878.dex.Format.MSparseSwitchPayload;
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
    NOP(common(0x00), "nop", Format10x, 0),
    MOVE(common(0x01), "move", Format12x, 0),
    MOVE_FROM16(common(0x02), "move/from16", Format22x, 0),
    MOVE_16(common(0x03), "move/16", Format32x, 0),
    MOVE_WIDE(common(0x04), "move-wide", Format12x, 0),
    MOVE_WIDE_FROM16(common(0x05), "move-wide/from16", Format22x, 0),
    MOVE_WIDE_16(common(0x06), "move-wide/16", Format32x, 0),
    MOVE_OBJECT(common(0x07), "move-object", Format12x, 0),
    MOVE_OBJECT_FROM16(common(0x08), "move-object/from16", Format22x, 0),
    MOVE_OBJECT_16(common(0x09), "move-object/16", Format32x, 0),
    MOVE_RESULT(common(0x0a), "move-result", Format11x, 0),
    MOVE_RESULT_WIDE(common(0x0b), "move-result-wide", Format11x, 0),
    MOVE_RESULT_OBJECT(common(0x0c), "move-result-object", Format11x, 0),
    MOVE_EXCEPTION(common(0x0d), "move-exception", Format11x, 0),
    RETURN_VOID(common(0x0e), "return-void", Format10x, TYPE_RETURN | ENDS_FLOW),
    RETURN(common(0x0f), "return", Format11x, TYPE_RETURN | ENDS_FLOW),
    RETURN_WIDE(common(0x10), "return-wide", Format11x, TYPE_RETURN | ENDS_FLOW),
    RETURN_OBJECT(common(0x11), "return-object", Format11x, TYPE_RETURN | ENDS_FLOW),
    CONST_4(common(0x12), "const/4", Format11n, 0),
    CONST_16(common(0x13), "const/16", Format21s, 0),
    CONST(common(0x14), "const", Format31i, 0),
    CONST_HIGH16(modern(0x15), "const/high16", Format21ih, 0),
    CONST_WIDE_16(common(0x15, 0x15, 0x16), "const-wide/16", Format21s, 0),
    CONST_WIDE_32(common(0x16, 0x16, 0x17), "const-wide/32", Format31i, 0),
    CONST_WIDE(common(0x17, 0x17, 0x18), "const-wide", Format51l, 0),
    CONST_WIDE_HIGH16(modern(0x19), "const-wide/high16", Format21lh, 0),
    CONST_STRING(common(0x18, 0x18, 0x1a), "const-string", Format21c, STRING, CAN_THROW),
    CONST_STRING_JUMBO(modern(0x1b), "const-string/jumbo", Format31c, STRING, CAN_THROW),
    CONST_CLASS(common(0x19, 0x19, 0x1c), "const-class", Format21c, TYPE, CAN_THROW),
    MONITOR_ENTER(common(0x1c, 0x1c, 0x1d), "monitor-enter", Format11x, CAN_THROW),
    MONITOR_EXIT(common(0x1d, 0x1d, 0x1e), "monitor-exit", Format11x, CAN_THROW),
    CHECK_CAST(common(0x1e, 0x1e, 0x1f), "check-cast", Format21c, TYPE, CAN_THROW),
    INSTANCE_OF(common(0x1f, 0x1f, 0x20), "instance-of", Format22c, TYPE, CAN_THROW),
    ARRAY_LENGTH(common(0x20, 0x20, 0x21), "array-length", Format12x, CAN_THROW),
    NEW_INSTANCE(common(0x21, 0x21, 0x22), "new-instance", Format21c, TYPE, CAN_THROW),
    NEW_ARRAY(modern(0x23), "new-array", Format22c, TYPE, CAN_THROW),
    FILLED_NEW_ARRAY(modern(0x24), "filled-new-array", Format35c, TYPE, CAN_THROW),
    FILLED_NEW_ARRAY_RANGE(common(0x2c, 0x2c, 0x25), "filled-new-array/range", Format3rc, TYPE, CAN_THROW),
    FILL_ARRAY_DATA(modern(0x26), "fill-array-data", Format31t, CAN_THROW | HAS_PAYLOAD),
    THROW(common(0x32, 0x33, 0x27), "throw", Format11x, CAN_THROW | ENDS_FLOW),
    GOTO(common(0x33, 0x34, 0x28), "goto", Format10t, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    GOTO_16(modern(0x29), "goto/16", Format20t, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    GOTO_32(modern(0x2a), "goto/32", Format30t, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    PACKED_SWITCH(modern(0x2b), "packed-switch", Format31t, HAS_PAYLOAD | TYPE_SWITCH),
    SPARSE_SWITCH(modern(0x2c), "sparse-switch", Format31t, HAS_PAYLOAD | TYPE_SWITCH),

    CMPL_FLOAT(common(0x2d, 0x2e, 0x2d), "cmpl-float", Format23x, 0),
    CMPG_FLOAT(common(0x2e, 0x2f, 0x2e), "cmpg-float", Format23x, 0),
    CMPL_DOUBLE(common(0x2f, 0x30, 0x2f), "cmpl-double", Format23x, 0),
    CMPG_DOUBLE(common(0x30, 0x31, 0x30), "cmpg-double", Format23x, 0),
    CMP_LONG(common(0x31, 0x32, 0x31), "cmp-long", Format23x, 0),

    IF_EQ(common(0x37, 0x38, 0x32), "if-eq", Format22t, TYPE_BRANCH),
    IF_NE(common(0x38, 0x39, 0x33), "if-ne", Format22t, TYPE_BRANCH),
    IF_LT(common(0x39, 0x3a, 0x34), "if-lt", Format22t, TYPE_BRANCH),
    IF_GE(common(0x3a, 0x3b, 0x35), "if-ge", Format22t, TYPE_BRANCH),
    IF_GT(common(0x3b, 0x3c, 0x36), "if-gt", Format22t, TYPE_BRANCH),
    IF_LE(common(0x3c, 0x3d, 0x37), "if-le", Format22t, TYPE_BRANCH),

    IF_EQZ(common(0x3d, 0x3e, 0x38), "if-eqz", Format21t, TYPE_BRANCH),
    IF_NEZ(common(0x3e, 0x3f, 0x39), "if-nez", Format21t, TYPE_BRANCH),
    IF_LTZ(common(0x3f, 0x40, 0x3a), "if-ltz", Format21t, TYPE_BRANCH),
    IF_GEZ(common(0x40, 0x41, 0x3b), "if-gez", Format21t, TYPE_BRANCH),
    IF_GTZ(common(0x41, 0x42, 0x3c), "if-gtz", Format21t, TYPE_BRANCH),
    IF_LEZ(common(0x42, 0x43, 0x3d), "if-lez", Format21t, TYPE_BRANCH),

    // 3e - 43 unused

    AGET(common(0x43, 0x44, 0x44), "aget", Format23x, CAN_THROW),
    AGET_WIDE(common(0x44, 0x45, 0x45), "aget-wide", Format23x, CAN_THROW),
    AGET_OBJECT(common(0x45, 0x46, 0x46), "aget-object", Format23x, CAN_THROW),
    AGET_BOOLEAN(common(0x46, 0x47, 0x47), "aget-boolean", Format23x, CAN_THROW),
    AGET_BYTE(common(0x47, 0x48, 0x48), "aget-byte", Format23x, CAN_THROW),
    AGET_CHAR(common(0x48, 0x49, 0x49), "aget-char", Format23x, CAN_THROW),
    AGET_SHORT(common(0x49, 0x4a, 0x4a), "aget-short", Format23x, CAN_THROW),
    APUT(common(0x4a, 0x4b, 0x4b), "aput", Format23x, CAN_THROW),
    APUT_WIDE(common(0x4b, 0x4c, 0x4c), "aput-wide", Format23x, CAN_THROW),
    APUT_OBJECT(common(0x4c, 0x4d, 0x4d), "aput-object", Format23x, CAN_THROW),
    APUT_BOOLEAN(common(0x4d, 0x4e, 0x4e), "aput-boolean", Format23x, CAN_THROW),
    APUT_BYTE(common(0x4e, 0x4f, 0x4f), "aput-byte", Format23x, CAN_THROW),
    APUT_CHAR(common(0x4f, 0x50, 0x50), "aput-char", Format23x, CAN_THROW),
    APUT_SHORT(common(0x50, 0x51, 0x51), "aput-short", Format23x, CAN_THROW),

    IGET(common(0x51, 0x52, 0x52), "iget", Format22c, FIELD, CAN_THROW),
    IGET_WIDE(common(0x52, 0x53, 0x53), "iget-wide", Format22c, FIELD, CAN_THROW),
    IGET_OBJECT(common(0x53, 0x54, 0x54), "iget-object", Format22c, FIELD, CAN_THROW),
    IGET_BOOLEAN(common(0x54, 0x55, 0x55), "iget-boolean", Format22c, FIELD, CAN_THROW),
    IGET_BYTE(common(0x55, 0x56, 0x56), "iget-byte", Format22c, FIELD, CAN_THROW),
    IGET_CHAR(common(0x56, 0x57, 0x57), "iget-char", Format22c, FIELD, CAN_THROW),
    IGET_SHORT(common(0x57, 0x58, 0x58), "iget-short", Format22c, FIELD, CAN_THROW),
    IPUT(common(0x58, 0x59, 0x59), "iput", Format22c, FIELD, CAN_THROW),
    IPUT_WIDE(common(0x59, 0x5a, 0x5a), "iput-wide", Format22c, FIELD, CAN_THROW),
    IPUT_OBJECT(common(0x5a, 0x5b, 0x5b), "iput-object", Format22c, FIELD, CAN_THROW),
    IPUT_BOOLEAN(common(0x5b, 0x5c, 0x5c), "iput-boolean", Format22c, FIELD, CAN_THROW),
    IPUT_BYTE(common(0x5c, 0x5d, 0x5d), "iput-byte", Format22c, FIELD, CAN_THROW),
    IPUT_CHAR(common(0x5d, 0x5e, 0x5e), "iput-char", Format22c, FIELD, CAN_THROW),
    IPUT_SHORT(common(0x5e, 0x5f, 0x5f), "iput-short", Format22c, FIELD, CAN_THROW),

    SGET(common(0x5f, 0x60, 0x60), "sget", Format21c, FIELD, CAN_THROW),
    SGET_WIDE(common(0x60, 0x61, 0x61), "sget-wide", Format21c, FIELD, CAN_THROW),
    SGET_OBJECT(common(0x61, 0x62, 0x62), "sget-object", Format21c, FIELD, CAN_THROW),
    SGET_BOOLEAN(common(0x62, 0x63, 0x63), "sget-boolean", Format21c, FIELD, CAN_THROW),
    SGET_BYTE(common(0x63, 0x64, 0x64), "sget-byte", Format21c, FIELD, CAN_THROW),
    SGET_CHAR(common(0x64, 0x65, 0x65), "sget-char", Format21c, FIELD, CAN_THROW),
    SGET_SHORT(common(0x65, 0x66, 0x66), "sget-short", Format21c, FIELD, CAN_THROW),
    SPUT(common(0x66, 0x67, 0x67), "sput", Format21c, FIELD, CAN_THROW),
    SPUT_WIDE(common(0x67, 0x68, 0x68), "sput-wide", Format21c, FIELD, CAN_THROW),
    SPUT_OBJECT(common(0x68, 0x69, 0x69), "sput-object", Format21c, FIELD, CAN_THROW),
    SPUT_BOOLEAN(common(0x69, 0x6a, 0x6a), "sput-boolean", Format21c, FIELD, CAN_THROW),
    SPUT_BYTE(common(0x6a, 0x6b, 0x6b), "sput-byte", Format21c, FIELD, CAN_THROW),
    SPUT_CHAR(common(0x6b, 0x6c, 0x6c), "sput-char", Format21c, FIELD, CAN_THROW),
    SPUT_SHORT(common(0x6c, 0x6d, 0x6d), "sput-short", Format21c, FIELD, CAN_THROW),

    INVOKE_VIRTUAL(modern(0x6e), "invoke-virtual", Format35c, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER(modern(0x6f), "invoke-super", Format35c, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_DIRECT(modern(0x70), "invoke-direct", Format35c, METHOD, CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_STATIC(modern(0x71), "invoke-static", Format35c, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_INTERFACE(modern(0x72), "invoke-interface", Format35c, METHOD, CAN_THROW | TYPE_INVOKE),

    // 73 unused

    INVOKE_VIRTUAL_RANGE(common(0x72, 0x74, 0x74), "invoke-virtual/range", Format3rc, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER_RANGE(common(0x73, 0x75, 0x75), "invoke-super/range", Format3rc, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_DIRECT_RANGE(common(0x74, 0x76, 0x76), "invoke-direct/range", Format3rc, METHOD, CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_STATIC_RANGE(common(0x75, 0x77, 0x77), "invoke-static/range", Format3rc, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_INTERFACE_RANGE(common(0x76, 0x78, 0x78), "invoke-interface/range", Format3rc, METHOD, CAN_THROW | TYPE_INVOKE),

    // 79 - 7a unused

    NEG_INT(common(0x7b), "neg-int", Format12x, 0),
    NOT_INT(common(0x7c), "not-int", Format12x, 0),
    NEG_LONG(common(0x7d), "neg-long", Format12x, 0),
    NOT_LONG(common(0x7e), "not-long", Format12x, 0),
    NEG_FLOAT(common(0x7f), "neg-float", Format12x, 0),
    NEG_DOUBLE(common(0x80), "neg-double", Format12x, 0),
    INT_TO_LONG(common(0x81), "int-to-long", Format12x, 0),
    INT_TO_FLOAT(common(0x82), "int-to-float", Format12x, 0),
    INT_TO_DOUBLE(common(0x83), "int-to-double", Format12x, 0),
    LONG_TO_INT(common(0x84), "long-to-int", Format12x, 0),
    LONG_TO_FLOAT(common(0x85), "long-to-float", Format12x, 0),
    LONG_TO_DOUBLE(common(0x86), "long-to-double", Format12x, 0),
    FLOAT_TO_INT(common(0x87), "float-to-int", Format12x, 0),
    FLOAT_TO_LONG(common(0x88), "float-to-long", Format12x, 0),
    FLOAT_TO_DOUBLE(common(0x89), "float-to-double", Format12x, 0),
    DOUBLE_TO_INT(common(0x8a), "double-to-int", Format12x, 0),
    DOUBLE_TO_LONG(common(0x8b), "double-to-long", Format12x, 0),
    DOUBLE_TO_FLOAT(common(0x8c), "double-to-float", Format12x, 0),
    INT_TO_BYTE(common(0x8d), "int-to-byte", Format12x, 0),
    INT_TO_CHAR(common(0x8e), "int-to-char", Format12x, 0),
    INT_TO_SHORT(common(0x8f), "int-to-short", Format12x, 0),

    ADD_INT(common(0x90), "add-int", Format23x, 0),
    SUB_INT(common(0x91), "sub-int", Format23x, 0),
    MUL_INT(common(0x92), "mul-int", Format23x, 0),
    DIV_INT(common(0x93), "div-int", Format23x, CAN_THROW),
    REM_INT(common(0x94), "rem-int", Format23x, CAN_THROW),
    AND_INT(common(0x95), "and-int", Format23x, 0),
    OR_INT(common(0x96), "or-int", Format23x, 0),
    XOR_INT(common(0x97), "xor-int", Format23x, 0),
    SHL_INT(common(0x98), "shl-int", Format23x, 0),
    SHR_INT(common(0x99), "shr-int", Format23x, 0),
    USHR_INT(common(0x9a), "ushr-int", Format23x, 0),
    ADD_LONG(common(0x9b), "add-long", Format23x, 0),
    SUB_LONG(common(0x9c), "sub-long", Format23x, 0),
    MUL_LONG(common(0x9d), "mul-long", Format23x, 0),
    DIV_LONG(common(0x9e), "div-long", Format23x, CAN_THROW),
    REM_LONG(common(0x9f), "rem-long", Format23x, CAN_THROW),
    AND_LONG(common(0xa0), "and-long", Format23x, 0),
    OR_LONG(common(0xa1), "or-long", Format23x, 0),
    XOR_LONG(common(0xa2), "xor-long", Format23x, 0),
    SHL_LONG(common(0xa3), "shl-long", Format23x, 0),
    SHR_LONG(common(0xa4), "shr-long", Format23x, 0),
    USHR_LONG(common(0xa5), "ushr-long", Format23x, 0),
    ADD_FLOAT(common(0xa6), "add-float", Format23x, 0),
    SUB_FLOAT(common(0xa7), "sub-float", Format23x, 0),
    MUL_FLOAT(common(0xa8), "mul-float", Format23x, 0),
    DIV_FLOAT(common(0xa9), "div-float", Format23x, 0),
    REM_FLOAT(common(0xaa), "rem-float", Format23x, 0),
    ADD_DOUBLE(common(0xab), "add-double", Format23x, 0),
    SUB_DOUBLE(common(0xac), "sub-double", Format23x, 0),
    MUL_DOUBLE(common(0xad), "mul-double", Format23x, 0),
    DIV_DOUBLE(common(0xae), "div-double", Format23x, 0),
    REM_DOUBLE(common(0xaf), "rem-double", Format23x, 0),

    ADD_INT_2ADDR(common(0xb0), "add-int/2addr", Format12x, 0),
    SUB_INT_2ADDR(common(0xb1), "sub-int/2addr", Format12x, 0),
    MUL_INT_2ADDR(common(0xb2), "mul-int/2addr", Format12x, 0),
    DIV_INT_2ADDR(common(0xb3), "div-int/2addr", Format12x, CAN_THROW),
    REM_INT_2ADDR(common(0xb4), "rem-int/2addr", Format12x, CAN_THROW),
    AND_INT_2ADDR(common(0xb5), "and-int/2addr", Format12x, 0),
    OR_INT_2ADDR(common(0xb6), "or-int/2addr", Format12x, 0),
    XOR_INT_2ADDR(common(0xb7), "xor-int/2addr", Format12x, 0),
    SHL_INT_2ADDR(common(0xb8), "shl-int/2addr", Format12x, 0),
    SHR_INT_2ADDR(common(0xb9), "shr-int/2addr", Format12x, 0),
    USHR_INT_2ADDR(common(0xba), "ushr-int/2addr", Format12x, 0),
    ADD_LONG_2ADDR(common(0xbb), "add-long/2addr", Format12x, 0),
    SUB_LONG_2ADDR(common(0xbc), "sub-long/2addr", Format12x, 0),
    MUL_LONG_2ADDR(common(0xbd), "mul-long/2addr", Format12x, 0),
    DIV_LONG_2ADDR(common(0xbe), "div-long/2addr", Format12x, CAN_THROW),
    REM_LONG_2ADDR(common(0xbf), "rem-long/2addr", Format12x, CAN_THROW),
    AND_LONG_2ADDR(common(0xc0), "and-long/2addr", Format12x, 0),
    OR_LONG_2ADDR(common(0xc1), "or-long/2addr", Format12x, 0),
    XOR_LONG_2ADDR(common(0xc2), "xor-long/2addr", Format12x, 0),
    SHL_LONG_2ADDR(common(0xc3), "shl-long/2addr", Format12x, 0),
    SHR_LONG_2ADDR(common(0xc4), "shr-long/2addr", Format12x, 0),
    USHR_LONG_2ADDR(common(0xc5), "ushr-long/2addr", Format12x, 0),
    ADD_FLOAT_2ADDR(common(0xc6), "add-float/2addr", Format12x, 0),
    SUB_FLOAT_2ADDR(common(0xc7), "sub-float/2addr", Format12x, 0),
    MUL_FLOAT_2ADDR(common(0xc8), "mul-float/2addr", Format12x, 0),
    DIV_FLOAT_2ADDR(common(0xc9), "div-float/2addr", Format12x, 0),
    REM_FLOAT_2ADDR(common(0xca), "rem-float/2addr", Format12x, 0),
    ADD_DOUBLE_2ADDR(common(0xcb), "add-double/2addr", Format12x, 0),
    SUB_DOUBLE_2ADDR(common(0xcc), "sub-double/2addr", Format12x, 0),
    MUL_DOUBLE_2ADDR(common(0xcd), "mul-double/2addr", Format12x, 0),
    DIV_DOUBLE_2ADDR(common(0xce), "div-double/2addr", Format12x, 0),
    REM_DOUBLE_2ADDR(common(0xcf), "rem-double/2addr", Format12x, 0),

    ADD_INT_LIT16(common(0xd0), "add-int/lit16", Format22s, 0),
    RSUB_INT(common(0xd1), "rsub-int", Format22s, 0),
    MUL_INT_LIT16(common(0xd2), "mul-int/lit16", Format22s, 0),
    DIV_INT_LIT16(common(0xd3), "div-int/lit16", Format22s, CAN_THROW),
    REM_INT_LIT16(common(0xd4), "rem-int/lit16", Format22s, CAN_THROW),
    AND_INT_LIT16(common(0xd5), "and-int/lit16", Format22s, 0),
    OR_INT_LIT16(common(0xd6), "or-int/lit16", Format22s, 0),
    XOR_INT_LIT16(common(0xd7), "xor-int/lit16", Format22s, 0),

    ADD_INT_LIT8(common(0xd8), "add-int/lit8", Format22b, 0),
    RSUB_INT_LIT8(common(0xd9), "rsub-int/lit8", Format22b, 0),
    MUL_INT_LIT8(common(0xda), "mul-int/lit8", Format22b, 0),
    DIV_INT_LIT8(common(0xdb), "div-int/lit8", Format22b, CAN_THROW),
    REM_INT_LIT8(common(0xdc), "rem-int/lit8", Format22b, CAN_THROW),
    AND_INT_LIT8(common(0xdd), "and-int/lit8", Format22b, 0),
    OR_INT_LIT8(common(0xde), "or-int/lit8", Format22b, 0),
    XOR_INT_LIT8(common(0xdf), "xor-int/lit8", Format22b, 0),
    SHL_INT_LIT8(common(0xe0), "shl-int/lit8", Format22b, 0),
    SHR_INT_LIT8(common(0xe1), "shr-int/lit8", Format22b, 0),
    USHR_INT_LIT8(common(0xe2), "ushr-int/lit8", Format22b, 0),

    // e3 - f9 unused

    INVOKE_POLYMORPHIC(firstApi(0xfa, 26), "invoke-polymorphic", Format45cc, METHOD, PROTO, CAN_THROW | TYPE_INVOKE),
    INVOKE_POLYMORPHIC_RANGE(firstApi(0xfb, 26), "invoke-polymorphic/range", Format4rcc, METHOD, PROTO, CAN_THROW | TYPE_INVOKE),
    INVOKE_CUSTOM(firstApi(0xfc, 26), "invoke-custom", Format35c, CALLSITE, CAN_THROW | TYPE_INVOKE),
    INVOKE_CUSTOM_RANGE(firstApi(0xfd, 26), "invoke-custom/range", Format3rc, CALLSITE, CAN_THROW | TYPE_INVOKE),
    CONST_METHOD_HANDLE(firstApi(0xfe, 28), "const-method-handle", Format21c, METHOD_HANDLE, CAN_THROW),
    CONST_METHOD_TYPE(firstApi(0xff, 28), "const-method-type", Format21c, PROTO, CAN_THROW),

    PACKED_SWITCH_PAYLOAD(modern(0x100), "packed-switch-payload", PackedSwitchPayload, 0),
    SPARSE_SWITCH_PAYLOAD(modern(0x200), "sparse-switch-payload", SparseSwitchPayload, 0),
    ARRAY_PAYLOAD(common(0x300), "array-payload", ArrayPayload, 0),

    // legacy dex009 / dex013 opcodes

    M_CONST_SPECIAL(legacy(0x1a, 0x1a), "m-const/special", Format11p, 0),
    M_CONST_WIDE_SPECIAL(legacy(0x1b, 0x1b), "m-const-wide/special", Format11p, 0),

    // The semantics of the new-array instruction differ from newer versions of dex.
    // The instruction takes the type of array component, not the type of the array itself
    M_NEW_ARRAY(legacy(0x22, 0x22), "m-new-array", Format22c, TYPE, CAN_THROW),
    M_NEW_ARRAY_BOOLEAN(legacy(0x23, 0x23), "m-new-array-boolean", Format12x, CAN_THROW),
    M_NEW_ARRAY_BYTE(legacy(0x24, 0x24), "m-new-array-byte", Format12x, CAN_THROW),
    M_NEW_ARRAY_CHAR(legacy(0x25, 0x25), "m-new-array-char", Format12x, CAN_THROW),
    M_NEW_ARRAY_SHORT(legacy(0x26, 0x26), "m-new-array-short", Format12x, CAN_THROW),
    M_NEW_ARRAY_INT(legacy(0x27, 0x27), "m-new-array-int", Format12x, CAN_THROW),
    M_NEW_ARRAY_LONG(legacy(0x28, 0x28), "m-new-array-long", Format12x, CAN_THROW),
    M_NEW_ARRAY_FLOAT(legacy(0x29, 0x29), "m-new-array-float", Format12x, CAN_THROW),
    M_NEW_ARRAY_DOUBLE(legacy(0x2a, 0x2a), "m-new-array-double", Format12x, CAN_THROW),
    M_FILLED_NEW_ARRAY(legacy(0x2b, 0x2b), "m-filled-new-array", Format34c, TYPE, CAN_THROW),

    M_GOTO_24(legacy(52, 53), "m-goto/24", Format20t_24, TYPE_BRANCH | UNCONDITIONAL | ENDS_FLOW),
    M_PACKED_SWITCH(legacy(0x35, 0x36), "m-packed-switch", Format21t, HAS_PAYLOAD | TYPE_SWITCH),
    M_SPARSE_SWITCH(legacy(0x36, 0x37), "m-sparse-switch", Format21t, HAS_PAYLOAD | TYPE_SWITCH),

    M_INVOKE_VIRTUAL(legacy(0x6d, 0x6e), "m-invoke-virtual", Format34c, METHOD, CAN_THROW | TYPE_INVOKE),
    M_INVOKE_SUPER(legacy(0x6e, 0x6f), "m-invoke-super", Format34c, METHOD, CAN_THROW | TYPE_INVOKE),
    M_INVOKE_DIRECT(legacy(0x6f, 0x70), "m-invoke-direct", Format34c, METHOD, CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    M_INVOKE_STATIC(legacy(0x70, 0x71), "m-invoke-static", Format34c, METHOD, CAN_THROW | TYPE_INVOKE),
    M_INVOKE_INTERFACE(legacy(0x71, 0x72), "m-invoke-interface", Format34c, METHOD, CAN_THROW | TYPE_INVOKE),

    M_PACKED_SWITCH_PAYLOAD(legacy(0x100, 0x100), "m-packed-switch-payload", MPackedSwitchPayload, 0),
    M_SPARSE_SWITCH_PAYLOAD(legacy(0x200, 0x200), "m-sparse-switch-payload", MSparseSwitchPayload, 0),

    // legacy dex009 / dex013 odex opcodes

    M_EXECUTE_INLINE(legacy(0xee, 0xee), "m-execute-inline", Format34c, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    M_INVOKE_DIRECT_EMPTY(legacy(0xf0, 0xf0), "m-invoke-direct-empty", Format34c, METHOD, ODEX_ONLY | CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    M_INVOKE_VIRTUAL_QUICK(legacy(0xf8, 0xf8), "m-invoke-virtual-quick", Format34c, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    M_INVOKE_SUPER_QUICK(legacy(0xfa, 0xfa), "m-invoke-super-quick", Format34c, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),

    // odex opcodes

    EXECUTE_INLINE(dalvikOnly(modern(0xee)), "execute-inline", Format35c, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    EXECUTE_INLINE_RANGE(dalvikOnly(firstApi(0xef, 8)), "execute-inline/range", Format3rc, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),

    INVOKE_DIRECT_EMPTY(dalvikOnly(lastApi(modern(0xf0), 13)), "invoke-direct-empty", Format35c, METHOD, ODEX_ONLY | CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_OBJECT_INIT_RANGE(dalvikOnly(firstApi(0xf0, 14)), "invoke-object-init/range", Format3rc, METHOD, ODEX_ONLY | CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),

    IGET_QUICK(dalvikOrArt(common(0xf2), lastApi(0xe3, 30)), "iget-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_WIDE_QUICK(dalvikOrArt(common(0xf3), lastApi(0xe4, 30)), "iget-wide-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_OBJECT_QUICK(dalvikOrArt(common(0xf4), lastApi(0xe5, 30)), "iget-object-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),

    IPUT_QUICK(dalvikOrArt(common(0xf5), lastApi(0xe6, 30)), "iput-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_WIDE_QUICK(dalvikOrArt(common(0xf6), lastApi(0xe7, 30)), "iput-wide-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_OBJECT_QUICK(dalvikOrArt(common(0xf7), lastApi(0xe8, 30)), "iput-object-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),

    INVOKE_VIRTUAL_QUICK(dalvikOrArt(modern(0xf8), lastApi(0xe9, 30)), "invoke-virtual-quick", Format35c, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    INVOKE_VIRTUAL_QUICK_RANGE(dalvikOrArt(common(0xf9), lastApi(0xea, 30)), "invoke-virtual-quick/range", Format3rc, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),

    INVOKE_SUPER_QUICK(dalvikOnly(modern(0xfa)), "invoke-super-quick", Format35c, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER_QUICK_RANGE(dalvikOnly(common(0xfb)), "invoke-super-quick/range", Format3rc, RAW_INDEX, ODEX_ONLY | CAN_THROW | TYPE_INVOKE),

    // TODO THROW_VERIFICATION_ERROR(onlyDalvik(firstApi(0xed, 5)), "throw-verification-error", Format20bc, ODEX_ONLY | CAN_THROW | END_FLOW),

    // Note: breakpoint replaces the original opcode and restores it back when executed (it only exists at runtime)
    // BREAKPOINT(onlyDalvik(firstApi(0xec, 8)), "breakpoint", Format00x, RUNTIME_ONLY),

    IGET_VOLATILE(dalvikOnly(firstApi(0xe3, 9)), "iget-volatile", Format22c, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_VOLATILE(dalvikOnly(firstApi(0xe4, 9)), "iput-volatile", Format22c, FIELD, ODEX_ONLY | CAN_THROW),

    SGET_VOLATILE(dalvikOnly(firstApi(0xe5, 9)), "sget-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_VOLATILE(dalvikOnly(firstApi(0xe6, 9)), "sput-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),

    IGET_WIDE_VOLATILE(dalvikOnly(firstApi(0xe8, 9)), "iget-wide-volatile", Format22c, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_WIDE_VOLATILE(dalvikOnly(firstApi(0xe9, 9)), "iput-wide-volatile", Format22c, FIELD, ODEX_ONLY | CAN_THROW),

    SGET_WIDE_VOLATILE(dalvikOnly(firstApi(0xea, 9)), "sget-wide-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_WIDE_VOLATILE(dalvikOnly(firstApi(0xeb, 9)), "sput-wide-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),

    IGET_OBJECT_VOLATILE(dalvikOnly(firstApi(0xe7, 9)), "iget-object-volatile", Format22c, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_OBJECT_VOLATILE(dalvikOnly(firstApi(0xfc, 9)), "iput-object-volatile", Format22c, FIELD, ODEX_ONLY | CAN_THROW),

    SGET_OBJECT_VOLATILE(dalvikOnly(firstApi(0xfd, 9)), "sget-object-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_OBJECT_VOLATILE(dalvikOnly(firstApi(0xfe, 9)), "sput-object-volatile", Format21c, FIELD, ODEX_ONLY | CAN_THROW),

    RETURN_VOID_BARRIER(dalvikOrArt(firstApi(0xf1, 11), lastApi(0x73, 22)), "return-void-barrier", Format10x, ODEX_ONLY | TYPE_RETURN | ENDS_FLOW),
    RETURN_VOID_NO_BARRIER(betweenApi(0x73, 23, 30), "return-void-no-barrier", Format10x, ODEX_ONLY | TYPE_RETURN | ENDS_FLOW),

    IPUT_BOOLEAN_QUICK(betweenApi(0xeb, 23, 30), "iput-boolean-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_BYTE_QUICK(betweenApi(0xec, 23, 30), "iput-byte-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_CHAR_QUICK(betweenApi(0xed, 23, 30), "iput-char-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IPUT_SHORT_QUICK(betweenApi(0xee, 23, 30), "iput-short-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),

    IGET_BOOLEAN_QUICK(betweenApi(0xef, 23, 30), "iget-boolean-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_BYTE_QUICK(betweenApi(0xf0, 23, 30), "iget-byte-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_CHAR_QUICK(betweenApi(0xf1, 23, 30), "iget-char-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),
    IGET_SHORT_QUICK(betweenApi(0xf2, 23, 30), "iget-short-quick", Format22c, RAW_INDEX, ODEX_ONLY | CAN_THROW),

    // legacy android 7.x lambda opcodes

    // TODO INVOKE_LAMBDA(betweenApi(0xf3, 24, 25), "invoke-lambda", Format25x, EXPERIMENTAL_LAMBDA),
    // TODO CAPTURE_VARIABLE(betweenApi(0xf5, 24, 25), "capture-variable", Format21c(string), EXPERIMENTAL_LAMBDA),
    // TODO CREATE_LAMBDA(betweenApi(0xf6, 24, 25), "create-lambda", Format21c(method), EXPERIMENTAL_LAMBDA),
    // TODO LIBERATE_LAMBDA(betweenApi(0xf7, 24, 25), "liberate-lambda", Format22c(string), EXPERIMENTAL_LAMBDA),
    // TODO BOX_LAMBDA(betweenApi(0xf8, 24, 25), "box-lambda", Format22x, EXPERIMENTAL_LAMBDA),
    // TODO UNBOX_LAMBDA(betweenApi(0xf9, 24, 25), "unbox-lambda", Format22c(type), EXPERIMENTAL_LAMBDA),

    // legacy dex036 expanded opcodes

    CONST_CLASS_JUMBO(betweenApi(0x00ff, 14, 15), "const-class/jumbo", Format41c, TYPE, CAN_THROW),
    CHECK_CAST_JUMBO(betweenApi(0x01ff, 14, 15), "check-cast/jumbo", Format41c, TYPE, CAN_THROW),
    INSTANCE_OF_JUMBO(betweenApi(0x02ff, 14, 15), "instance-of/jumbo", Format52c, TYPE, CAN_THROW),
    NEW_INSTANCE_JUMBO(betweenApi(0x03ff, 14, 15), "new-instance/jumbo", Format41c, TYPE, CAN_THROW),
    NEW_ARRAY_JUMBO(betweenApi(0x04ff, 14, 15), "new-array/jumbo", Format52c, TYPE, CAN_THROW),
    FILLED_NEW_ARRAY_JUMBO(betweenApi(0x05ff, 14, 15), "filled-new-array/jumbo", Format5rc, TYPE, CAN_THROW),

    IGET_JUMBO(betweenApi(0x06ff, 14, 15), "iget/jumbo", Format52c, FIELD, CAN_THROW),
    IGET_WIDE_JUMBO(betweenApi(0x07ff, 14, 15), "iget-wide/jumbo", Format52c, FIELD, CAN_THROW),
    IGET_OBJECT_JUMBO(betweenApi(0x08ff, 14, 15), "iget-object/jumbo", Format52c, FIELD, CAN_THROW),
    IGET_BOOLEAN_JUMBO(betweenApi(0x09ff, 14, 15), "iget-boolean/jumbo", Format52c, FIELD, CAN_THROW),
    IGET_BYTE_JUMBO(betweenApi(0x0aff, 14, 15), "iget-byte/jumbo", Format52c, FIELD, CAN_THROW),
    IGET_CHAR_JUMBO(betweenApi(0x0bff, 14, 15), "iget-char/jumbo", Format52c, FIELD, CAN_THROW),
    IGET_SHORT_JUMBO(betweenApi(0x0cff, 14, 15), "iget-short/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_JUMBO(betweenApi(0x0dff, 14, 15), "iput/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_WIDE_JUMBO(betweenApi(0x0eff, 14, 15), "iput-wide/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_OBJECT_JUMBO(betweenApi(0x0fff, 14, 15), "iput-object/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_BOOLEAN_JUMBO(betweenApi(0x10ff, 14, 15), "iput-boolean/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_BYTE_JUMBO(betweenApi(0x11ff, 14, 15), "iput-byte/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_CHAR_JUMBO(betweenApi(0x12ff, 14, 15), "iput-char/jumbo", Format52c, FIELD, CAN_THROW),
    IPUT_SHORT_JUMBO(betweenApi(0x13ff, 14, 15), "iput-short/jumbo", Format52c, FIELD, CAN_THROW),

    SGET_JUMBO(betweenApi(0x14ff, 14, 15), "sget/jumbo", Format41c, FIELD, CAN_THROW),
    SGET_WIDE_JUMBO(betweenApi(0x15ff, 14, 15), "sget-wide/jumbo", Format41c, FIELD, CAN_THROW),
    SGET_OBJECT_JUMBO(betweenApi(0x16ff, 14, 15), "sget-object/jumbo", Format41c, FIELD, CAN_THROW),
    SGET_BOOLEAN_JUMBO(betweenApi(0x17ff, 14, 15), "sget-boolean/jumbo", Format41c, FIELD, CAN_THROW),
    SGET_BYTE_JUMBO(betweenApi(0x18ff, 14, 15), "sget-byte/jumbo", Format41c, FIELD, CAN_THROW),
    SGET_CHAR_JUMBO(betweenApi(0x19ff, 14, 15), "sget-char/jumbo", Format41c, FIELD, CAN_THROW),
    SGET_SHORT_JUMBO(betweenApi(0x1aff, 14, 15), "sget-short/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_JUMBO(betweenApi(0x1bff, 14, 15), "sput/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_WIDE_JUMBO(betweenApi(0x1cff, 14, 15), "sput-wide/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_OBJECT_JUMBO(betweenApi(0x1dff, 14, 15), "sput-object/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_BOOLEAN_JUMBO(betweenApi(0x1eff, 14, 15), "sput-boolean/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_BYTE_JUMBO(betweenApi(0x1fff, 14, 15), "sput-byte/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_CHAR_JUMBO(betweenApi(0x20ff, 14, 15), "sput-char/jumbo", Format41c, FIELD, CAN_THROW),
    SPUT_SHORT_JUMBO(betweenApi(0x21ff, 14, 15), "sput-short/jumbo", Format41c, FIELD, CAN_THROW),

    INVOKE_VIRTUAL_JUMBO(betweenApi(0x22ff, 14, 15), "invoke-virtual/jumbo", Format5rc, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_SUPER_JUMBO(betweenApi(0x23ff, 14, 15), "invoke-super/jumbo", Format5rc, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_DIRECT_JUMBO(betweenApi(0x24ff, 14, 15), "invoke-direct/jumbo", Format5rc, METHOD, CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),
    INVOKE_STATIC_JUMBO(betweenApi(0x25ff, 14, 15), "invoke-static/jumbo", Format5rc, METHOD, CAN_THROW | TYPE_INVOKE),
    INVOKE_INTERFACE_JUMBO(betweenApi(0x26ff, 14, 15), "invoke-interface/jumbo", Format5rc, METHOD, CAN_THROW | TYPE_INVOKE),

    // legacy dex036 odex opcodes

    INVOKE_OBJECT_INIT_JUMBO(betweenApi(0xf2ff, 14, 15), "invoke-object-init/jumbo", Format5rc, METHOD, ODEX_ONLY | CAN_THROW | TYPE_INVOKE | CAN_INITIALIZE_REFERENCE),

    IGET_VOLATILE_JUMBO(betweenApi(0xf3ff, 14, 15), "iget-volatile/jumbo", Format52c, FIELD, ODEX_ONLY | CAN_THROW),
    IGET_WIDE_VOLATILE_JUMBO(betweenApi(0xf4ff, 14, 15), "iget-wide-volatile/jumbo", Format52c, FIELD, ODEX_ONLY | CAN_THROW),
    IGET_OBJECT_VOLATILE_JUMBO(betweenApi(0xf5ff, 14, 15), "iget-object-volatile/jumbo", Format52c, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_VOLATILE_JUMBO(betweenApi(0xf6ff, 14, 15), "iput-volatile/jumbo", Format52c, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_WIDE_VOLATILE_JUMBO(betweenApi(0xf7ff, 14, 15), "iput-wide-volatile/jumbo", Format52c, FIELD, ODEX_ONLY | CAN_THROW),
    IPUT_OBJECT_VOLATILE_JUMBO(betweenApi(0xf8ff, 14, 15), "iput-object-volatile/jumbo", Format52c, FIELD, ODEX_ONLY | CAN_THROW),

    SGET_VOLATILE_JUMBO(betweenApi(0xf9ff, 14, 15), "sget-volatile/jumbo", Format41c, FIELD, ODEX_ONLY | CAN_THROW),
    SGET_WIDE_VOLATILE_JUMBO(betweenApi(0xfaff, 14, 15), "sget-wide-volatile/jumbo", Format41c, FIELD, ODEX_ONLY | CAN_THROW),
    SGET_OBJECT_VOLATILE_JUMBO(betweenApi(0xfbff, 14, 15), "sget-object-volatile/jumbo", Format41c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_VOLATILE_JUMBO(betweenApi(0xfcff, 14, 15), "sput-volatile/jumbo", Format41c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_WIDE_VOLATILE_JUMBO(betweenApi(0xfdff, 14, 15), "sput-wide-volatile/jumbo", Format41c, FIELD, ODEX_ONLY | CAN_THROW),
    SPUT_OBJECT_VOLATILE_JUMBO(betweenApi(0xfeff, 14, 15), "sput-object-volatile/jumbo", Format41c, FIELD, ODEX_ONLY | CAN_THROW),

    // TODO THROW_VERIFICATION_ERROR_JUMBO(betweenApi(0xffff, 14, 15), "throw-verification-error/jumbo", Format40cs, ODEX_ONLY | CAN_THROW | END_FLOW),

    // special single 16-bit opcode
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

        // TODO: flag for expanded opcodes
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

    Opcode(Constraint constraint, String name, Format format, int flags) {
        this(constraint, name, format, null, flags);
    }

    Opcode(Constraint constraint, String name, Format format, ReferenceType reference, int flags) {
        this(constraint, name, format, reference, null, flags);
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

    public ReferenceType getReferenceType(int index) {
        return switch (index) {
            case 0 -> reference1;
            case 1 -> reference2;
            default -> null;
        };
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
        return format == Format34c
                || format == Format35c
                || format == Format3rc
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

    private static Constraint legacy(int dex009, int dex013) {
        return combine(
                onlyDex(DEX013, value(dex013)),
                onlyDex(DEX009, value(dex009))
        );
    }

    private static Constraint modern(int dex035) {
        return firstDex(DEX035, value(dex035));
    }

    private static Constraint common(int common) {
        return value(common);
    }

    private static Constraint common(int dex009, int dex013, int dex035) {
        return combine(
                modern(dex035),
                legacy(dex009, dex013)
        );
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

    private static Constraint onlyDex(DexVersion version, Constraint constraint) {
        return new Constraint() {
            @Override
            public boolean contains(int opcode) {
                return constraint.contains(opcode);
            }

            @Override
            public Integer opcode(DexInfo info) {
                return version != info.dex() ? null : constraint.opcode(info);
            }

            @Override
            public String toString() {
                return "(dex == " + version + ") -> {" + constraint + "}";
            }
        };
    }

    private static boolean checkDexVersion(DexVersion min, DexVersion target) {
        return target.isCompact() || (min.ordinal() >= target.ordinal());
    }

    @SuppressWarnings("SameParameterValue")
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

    private static Constraint dalvikOrArt(Constraint dalvik, Constraint art) {
        return combine(dalvikOnly(dalvik), artOnly(art));
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
                return first + " | " + second;
            }
        };
    }
}
