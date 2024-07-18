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

package com.v7878.dex.bytecode;

import static com.v7878.dex.DexVersion.DEX038;
import static com.v7878.dex.DexVersion.DEX039;

import com.v7878.dex.DexContext;
import com.v7878.dex.DexVersion;
import com.v7878.dex.util.SparseArray;

import java.util.function.Function;

public enum Opcode {

    NOP(0x00, "nop", Format.Format10x::new),
    MOVE(0x01, "move", Format.Format12x::new),
    MOVE_FROM16(0x02, "move/from16", Format.Format22x::new),
    MOVE_16(0x03, "move/16", Format.Format32x::new),
    MOVE_WIDE(0x04, "move-wide", Format.Format12x::new),
    MOVE_WIDE_FROM16(0x05, "move-wide/from16", Format.Format22x::new),
    MOVE_WIDE_16(0x06, "move-wide/16", Format.Format32x::new),
    MOVE_OBJECT(0x07, "move-object", Format.Format12x::new),
    MOVE_OBJECT_FROM16(0x08, "move-object/from16", Format.Format22x::new),
    MOVE_OBJECT_16(0x09, "move-object/16", Format.Format32x::new),
    MOVE_RESULT(0x0a, "move-result", Format.Format11x::new),
    MOVE_RESULT_WIDE(0x0b, "move-result-wide", Format.Format11x::new),
    MOVE_RESULT_OBJECT(0x0c, "move-result-object", Format.Format11x::new),
    MOVE_EXCEPTION(0x0d, "move-exception", Format.Format11x::new),
    RETURN_VOID(0x0e, "return-void", Format.Format10x::new),
    RETURN(0x0f, "return", Format.Format11x::new),
    RETURN_WIDE(0x10, "return-wide", Format.Format11x::new),
    RETURN_OBJECT(0x11, "return-object", Format.Format11x::new),
    CONST_4(0x12, "const/4", Format.Format11n::new),
    CONST_16(0x13, "const/16", Format.Format21t21s::new),
    CONST(0x14, "const", Format.Format31i31t::new),
    CONST_HIGH16(0x15, "const/high16", Format.Format21ih::new),
    CONST_WIDE_16(0x16, "const-wide/16", Format.Format21t21s::new),
    CONST_WIDE_32(0x17, "const-wide/32", Format.Format31i31t::new),
    CONST_WIDE(0x18, "const-wide", Format.Format51l::new),
    CONST_WIDE_HIGH16(0x19, "const-wide/high16", Format.Format21lh::new),
    CONST_STRING(0x1a, "const-string", opcode -> new Format.Format21c(opcode, ReferenceType.STRING)),
    CONST_STRING_JUMBO(0x1b, "const-string/jumbo", opcode -> new Format.Format31c(opcode, ReferenceType.STRING)),
    CONST_CLASS(0x1c, "const-class", opcode -> new Format.Format21c(opcode, ReferenceType.TYPE)),
    MONITOR_ENTER(0x1d, "monitor-enter", Format.Format11x::new),
    MONITOR_EXIT(0x1e, "monitor-exit", Format.Format11x::new),
    CHECK_CAST(0x1f, "check-cast", opcode -> new Format.Format21c(opcode, ReferenceType.TYPE)),
    INSTANCE_OF(0x20, "instance-of", opcode -> new Format.Format22c(opcode, ReferenceType.TYPE)),
    ARRAY_LENGTH(0x21, "array-length", Format.Format12x::new),
    NEW_INSTANCE(0x22, "new-instance", opcode -> new Format.Format21c(opcode, ReferenceType.TYPE)),
    NEW_ARRAY(0x23, "new-array", opcode -> new Format.Format22c(opcode, ReferenceType.TYPE)),
    FILLED_NEW_ARRAY(0x24, "filled-new-array", opcode -> new Format.Format35c(opcode, ReferenceType.TYPE)),
    FILLED_NEW_ARRAY_RANGE(0x25, "filled-new-array/range", opcode -> new Format.Format3rc(opcode, ReferenceType.TYPE)),
    FILL_ARRAY_DATA(0x26, "fill-array-data", Format.Format31i31t::new),
    THROW(0x27, "throw", Format.Format11x::new),
    GOTO(0x28, "goto", Format.Format10t::new),
    GOTO_16(0x29, "goto/16", Format.Format20t::new),
    GOTO_32(0x2a, "goto/32", Format.Format30t::new),
    PACKED_SWITCH(0x2b, "packed-switch", Format.Format31i31t::new),
    SPARSE_SWITCH(0x2c, "sparse-switch", Format.Format31i31t::new),

    CMPL_FLOAT(0x2d, "cmpl-float", Format.Format23x::new),
    CMPG_FLOAT(0x2e, "cmpg-float", Format.Format23x::new),
    CMPL_DOUBLE(0x2f, "cmpl-double", Format.Format23x::new),
    CMPG_DOUBLE(0x30, "cmpg-double", Format.Format23x::new),
    CMP_LONG(0x31, "cmp-long", Format.Format23x::new),

    IF_EQ(0x32, "if-eq", Format.Format22t22s::new),
    IF_NE(0x33, "if-ne", Format.Format22t22s::new),
    IF_LT(0x34, "if-lt", Format.Format22t22s::new),
    IF_GE(0x35, "if-ge", Format.Format22t22s::new),
    IF_GT(0x36, "if-gt", Format.Format22t22s::new),
    IF_LE(0x37, "if-le", Format.Format22t22s::new),

    IF_EQZ(0x38, "if-eqz", Format.Format21t21s::new),
    IF_NEZ(0x39, "if-nez", Format.Format21t21s::new),
    IF_LTZ(0x3a, "if-ltz", Format.Format21t21s::new),
    IF_GEZ(0x3b, "if-gez", Format.Format21t21s::new),
    IF_GTZ(0x3c, "if-gtz", Format.Format21t21s::new),
    IF_LEZ(0x3d, "if-lez", Format.Format21t21s::new),

    // 3e - 43 unused

    AGET(0x44, "aget", Format.Format23x::new),
    AGET_WIDE(0x45, "aget-wide", Format.Format23x::new),
    AGET_OBJECT(0x46, "aget-object", Format.Format23x::new),
    AGET_BOOLEAN(0x47, "aget-boolean", Format.Format23x::new),
    AGET_BYTE(0x48, "aget-byte", Format.Format23x::new),
    AGET_CHAR(0x49, "aget-char", Format.Format23x::new),
    AGET_SHORT(0x4a, "aget-short", Format.Format23x::new),
    APUT(0x4b, "aput", Format.Format23x::new),
    APUT_WIDE(0x4c, "aput-wide", Format.Format23x::new),
    APUT_OBJECT(0x4d, "aput-object", Format.Format23x::new),
    APUT_BOOLEAN(0x4e, "aput-boolean", Format.Format23x::new),
    APUT_BYTE(0x4f, "aput-byte", Format.Format23x::new),
    APUT_CHAR(0x50, "aput-char", Format.Format23x::new),
    APUT_SHORT(0x51, "aput-short", Format.Format23x::new),

    IGET(0x52, "iget", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IGET_WIDE(0x53, "iget-wide", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IGET_OBJECT(0x54, "iget-object", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IGET_BOOLEAN(0x55, "iget-boolean", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IGET_BYTE(0x56, "iget-byte", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IGET_CHAR(0x57, "iget-char", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IGET_SHORT(0x58, "iget-short", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT(0x59, "iput", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT_WIDE(0x5a, "iput-wide", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT_OBJECT(0x5b, "iput-object", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT_BOOLEAN(0x5c, "iput-boolean", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT_BYTE(0x5d, "iput-byte", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT_CHAR(0x5e, "iput-char", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),
    IPUT_SHORT(0x5f, "iput-short", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD)),

    SGET(0x60, "sget", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SGET_WIDE(0x61, "sget-wide", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SGET_OBJECT(0x62, "sget-object", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SGET_BOOLEAN(0x63, "sget-boolean", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SGET_BYTE(0x64, "sget-byte", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SGET_CHAR(0x65, "sget-char", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SGET_SHORT(0x66, "sget-short", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT(0x67, "sput", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT_WIDE(0x68, "sput-wide", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT_OBJECT(0x69, "sput-object", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT_BOOLEAN(0x6a, "sput-boolean", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT_BYTE(0x6b, "sput-byte", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT_CHAR(0x6c, "sput-char", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),
    SPUT_SHORT(0x6d, "sput-short", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD)),

    INVOKE_VIRTUAL(0x6e, "invoke-virtual", opcode -> new Format.Format35c(opcode, ReferenceType.METHOD)),
    INVOKE_SUPER(0x6f, "invoke-super", opcode -> new Format.Format35c(opcode, ReferenceType.METHOD)),
    INVOKE_DIRECT(0x70, "invoke-direct", opcode -> new Format.Format35c(opcode, ReferenceType.METHOD)),
    INVOKE_STATIC(0x71, "invoke-static", opcode -> new Format.Format35c(opcode, ReferenceType.METHOD)),
    INVOKE_INTERFACE(0x72, "invoke-interface", opcode -> new Format.Format35c(opcode, ReferenceType.METHOD)),

    // 73 unused

    INVOKE_VIRTUAL_RANGE(0x74, "invoke-virtual/range", opcode -> new Format.Format3rc(opcode, ReferenceType.METHOD)),
    INVOKE_SUPER_RANGE(0x75, "invoke-super/range", opcode -> new Format.Format3rc(opcode, ReferenceType.METHOD)),
    INVOKE_DIRECT_RANGE(0x76, "invoke-direct/range", opcode -> new Format.Format3rc(opcode, ReferenceType.METHOD)),
    INVOKE_STATIC_RANGE(0x77, "invoke-static/range", opcode -> new Format.Format3rc(opcode, ReferenceType.METHOD)),
    INVOKE_INTERFACE_RANGE(0x78, "invoke-interface/range", opcode -> new Format.Format3rc(opcode, ReferenceType.METHOD)),

    // 79 - 7a unused

    NEG_INT(0x7b, "neg-int", Format.Format12x::new),
    NOT_INT(0x7c, "not-int", Format.Format12x::new),
    NEG_LONG(0x7d, "neg-long", Format.Format12x::new),
    NOT_LONG(0x7e, "not-long", Format.Format12x::new),
    NEG_FLOAT(0x7f, "neg-float", Format.Format12x::new),
    NEG_DOUBLE(0x80, "neg-double", Format.Format12x::new),
    INT_TO_LONG(0x81, "int-to-long", Format.Format12x::new),
    INT_TO_FLOAT(0x82, "int-to-float", Format.Format12x::new),
    INT_TO_DOUBLE(0x83, "int-to-double", Format.Format12x::new),
    LONG_TO_INT(0x84, "long-to-int", Format.Format12x::new),
    LONG_TO_FLOAT(0x85, "long-to-float", Format.Format12x::new),
    LONG_TO_DOUBLE(0x86, "long-to-double", Format.Format12x::new),
    FLOAT_TO_INT(0x87, "float-to-int", Format.Format12x::new),
    FLOAT_TO_LONG(0x88, "float-to-long", Format.Format12x::new),
    FLOAT_TO_DOUBLE(0x89, "float-to-double", Format.Format12x::new),
    DOUBLE_TO_INT(0x8a, "double-to-int", Format.Format12x::new),
    DOUBLE_TO_LONG(0x8b, "double-to-long", Format.Format12x::new),
    DOUBLE_TO_FLOAT(0x8c, "double-to-float", Format.Format12x::new),
    INT_TO_BYTE(0x8d, "int-to-byte", Format.Format12x::new),
    INT_TO_CHAR(0x8e, "int-to-char", Format.Format12x::new),
    INT_TO_SHORT(0x8f, "int-to-short", Format.Format12x::new),

    ADD_INT(0x90, "add-int", Format.Format23x::new),
    SUB_INT(0x91, "sub-int", Format.Format23x::new),
    MUL_INT(0x92, "mul-int", Format.Format23x::new),
    DIV_INT(0x93, "div-int", Format.Format23x::new),
    REM_INT(0x94, "rem-int", Format.Format23x::new),
    AND_INT(0x95, "and-int", Format.Format23x::new),
    OR_INT(0x96, "or-int", Format.Format23x::new),
    XOR_INT(0x97, "xor-int", Format.Format23x::new),
    SHL_INT(0x98, "shl-int", Format.Format23x::new),
    SHR_INT(0x99, "shr-int", Format.Format23x::new),
    USHR_INT(0x9a, "ushr-int", Format.Format23x::new),
    ADD_LONG(0x9b, "add-long", Format.Format23x::new),
    SUB_LONG(0x9c, "sub-long", Format.Format23x::new),
    MUL_LONG(0x9d, "mul-long", Format.Format23x::new),
    DIV_LONG(0x9e, "div-long", Format.Format23x::new),
    REM_LONG(0x9f, "rem-long", Format.Format23x::new),
    AND_LONG(0xa0, "and-long", Format.Format23x::new),
    OR_LONG(0xa1, "or-long", Format.Format23x::new),
    XOR_LONG(0xa2, "xor-long", Format.Format23x::new),
    SHL_LONG(0xa3, "shl-long", Format.Format23x::new),
    SHR_LONG(0xa4, "shr-long", Format.Format23x::new),
    USHR_LONG(0xa5, "ushr-long", Format.Format23x::new),
    ADD_FLOAT(0xa6, "add-float", Format.Format23x::new),
    SUB_FLOAT(0xa7, "sub-float", Format.Format23x::new),
    MUL_FLOAT(0xa8, "mul-float", Format.Format23x::new),
    DIV_FLOAT(0xa9, "div-float", Format.Format23x::new),
    REM_FLOAT(0xaa, "rem-float", Format.Format23x::new),
    ADD_DOUBLE(0xab, "add-double", Format.Format23x::new),
    SUB_DOUBLE(0xac, "sub-double", Format.Format23x::new),
    MUL_DOUBLE(0xad, "mul-double", Format.Format23x::new),
    DIV_DOUBLE(0xae, "div-double", Format.Format23x::new),
    REM_DOUBLE(0xaf, "rem-double", Format.Format23x::new),

    ADD_INT_2ADDR(0xb0, "add-int/2addr", Format.Format12x::new),
    SUB_INT_2ADDR(0xb1, "sub-int/2addr", Format.Format12x::new),
    MUL_INT_2ADDR(0xb2, "mul-int/2addr", Format.Format12x::new),
    DIV_INT_2ADDR(0xb3, "div-int/2addr", Format.Format12x::new),
    REM_INT_2ADDR(0xb4, "rem-int/2addr", Format.Format12x::new),
    AND_INT_2ADDR(0xb5, "and-int/2addr", Format.Format12x::new),
    OR_INT_2ADDR(0xb6, "or-int/2addr", Format.Format12x::new),
    XOR_INT_2ADDR(0xb7, "xor-int/2addr", Format.Format12x::new),
    SHL_INT_2ADDR(0xb8, "shl-int/2addr", Format.Format12x::new),
    SHR_INT_2ADDR(0xb9, "shr-int/2addr", Format.Format12x::new),
    USHR_INT_2ADDR(0xba, "ushr-int/2addr", Format.Format12x::new),
    ADD_LONG_2ADDR(0xbb, "add-long/2addr", Format.Format12x::new),
    SUB_LONG_2ADDR(0xbc, "sub-long/2addr", Format.Format12x::new),
    MUL_LONG_2ADDR(0xbd, "mul-long/2addr", Format.Format12x::new),
    DIV_LONG_2ADDR(0xbe, "div-long/2addr", Format.Format12x::new),
    REM_LONG_2ADDR(0xbf, "rem-long/2addr", Format.Format12x::new),
    AND_LONG_2ADDR(0xc0, "and-long/2addr", Format.Format12x::new),
    OR_LONG_2ADDR(0xc1, "or-long/2addr", Format.Format12x::new),
    XOR_LONG_2ADDR(0xc2, "xor-long/2addr", Format.Format12x::new),
    SHL_LONG_2ADDR(0xc3, "shl-long/2addr", Format.Format12x::new),
    SHR_LONG_2ADDR(0xc4, "shr-long/2addr", Format.Format12x::new),
    USHR_LONG_2ADDR(0xc5, "ushr-long/2addr", Format.Format12x::new),
    ADD_FLOAT_2ADDR(0xc6, "add-float/2addr", Format.Format12x::new),
    SUB_FLOAT_2ADDR(0xc7, "sub-float/2addr", Format.Format12x::new),
    MUL_FLOAT_2ADDR(0xc8, "mul-float/2addr", Format.Format12x::new),
    DIV_FLOAT_2ADDR(0xc9, "div-float/2addr", Format.Format12x::new),
    REM_FLOAT_2ADDR(0xca, "rem-float/2addr", Format.Format12x::new),
    ADD_DOUBLE_2ADDR(0xcb, "add-double/2addr", Format.Format12x::new),
    SUB_DOUBLE_2ADDR(0xcc, "sub-double/2addr", Format.Format12x::new),
    MUL_DOUBLE_2ADDR(0xcd, "mul-double/2addr", Format.Format12x::new),
    DIV_DOUBLE_2ADDR(0xce, "div-double/2addr", Format.Format12x::new),
    REM_DOUBLE_2ADDR(0xcf, "rem-double/2addr", Format.Format12x::new),

    ADD_INT_LIT16(0xd0, "add-int/lit16", Format.Format22t22s::new),
    RSUB_INT(0xd1, "rsub-int", Format.Format22t22s::new),
    MUL_INT_LIT16(0xd2, "mul-int/lit16", Format.Format22t22s::new),
    DIV_INT_LIT16(0xd3, "div-int/lit16", Format.Format22t22s::new),
    REM_INT_LIT16(0xd4, "rem-int/lit16", Format.Format22t22s::new),
    AND_INT_LIT16(0xd5, "and-int/lit16", Format.Format22t22s::new),
    OR_INT_LIT16(0xd6, "or-int/lit16", Format.Format22t22s::new),
    XOR_INT_LIT16(0xd7, "xor-int/lit16", Format.Format22t22s::new),

    ADD_INT_LIT8(0xd8, "add-int/lit8", Format.Format22b::new),
    RSUB_INT_LIT8(0xd9, "rsub-int/lit8", Format.Format22b::new),
    MUL_INT_LIT8(0xda, "mul-int/lit8", Format.Format22b::new),
    DIV_INT_LIT8(0xdb, "div-int/lit8", Format.Format22b::new),
    REM_INT_LIT8(0xdc, "rem-int/lit8", Format.Format22b::new),
    AND_INT_LIT8(0xdd, "and-int/lit8", Format.Format22b::new),
    OR_INT_LIT8(0xde, "or-int/lit8", Format.Format22b::new),
    XOR_INT_LIT8(0xdf, "xor-int/lit8", Format.Format22b::new),
    SHL_INT_LIT8(0xe0, "shl-int/lit8", Format.Format22b::new),
    SHR_INT_LIT8(0xe1, "shr-int/lit8", Format.Format22b::new),
    USHR_INT_LIT8(0xe2, "ushr-int/lit8", Format.Format22b::new),

    // e3 - f9 unused

    INVOKE_POLYMORPHIC(firstDexVersionOrCDex(DEX038, firstApi(0xfa, 26)), "invoke-polymorphic", opcode -> new Format.Format45cc(opcode, ReferenceType.METHOD, ReferenceType.PROTO)),
    INVOKE_POLYMORPHIC_RANGE(firstDexVersionOrCDex(DEX038, firstApi(0xfb, 26)), "invoke-polymorphic/range", opcode -> new Format.Format4rcc(opcode, ReferenceType.METHOD, ReferenceType.PROTO)),
    INVOKE_CUSTOM(firstDexVersionOrCDex(DEX038, firstApi(0xfc, 26)), "invoke-custom", opcode -> new Format.Format35c(opcode, ReferenceType.CALLSITE)),
    INVOKE_CUSTOM_RANGE(firstDexVersionOrCDex(DEX038, firstApi(0xfd, 26)), "invoke-custom/range", opcode -> new Format.Format3rc(opcode, ReferenceType.CALLSITE)),
    CONST_METHOD_HANDLE(firstDexVersionOrCDex(DEX039, firstApi(0xfe, 28)), "const-method-handle", opcode -> new Format.Format21c(opcode, ReferenceType.METHOD_HANDLE)),
    CONST_METHOD_TYPE(firstDexVersionOrCDex(DEX039, firstApi(0xff, 28)), "const-method-type", opcode -> new Format.Format21c(opcode, ReferenceType.PROTO)),

    PACKED_SWITCH_PAYLOAD(0x100, "packed-switch-payload", Format.PackedSwitchPayload::new),
    SPARSE_SWITCH_PAYLOAD(0x200, "sparse-switch-payload", Format.SparseSwitchPayload::new),
    ARRAY_PAYLOAD(0x300, "array-payload", Format.ArrayPayload::new),

    //TODO
    EXECUTE_INLINE(onlyDalvik(allApis(0xee)), "execute-inline", opcode -> new Format.Format35c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    EXECUTE_INLINE_RANGE(onlyDalvik(firstApi(0xef, 8)), "execute-inline/range", opcode -> new Format.Format3rc(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),

    /*INVOKE_DIRECT_EMPTY(onlyDalvik(lastApi(0xf0, 13)), "invoke-direct-empty", Format.TODO::new, Opcode.ODEX_ONLY),*/
    //INVOKE_OBJECT_INIT_RANGE(onlyDalvik(firstApi(0xf0, 14)), "invoke-object-init/range", Format.TODO::new, Opcode.ODEX_ONLY),

    IGET_QUICK(allApis(0xf2), lastApi(0xe3, 30), "iget-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IGET_WIDE_QUICK(allApis(0xf3), lastApi(0xe4, 30), "iget-wide-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IGET_OBJECT_QUICK(allApis(0xf4), lastApi(0xe5, 30), "iget-object-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IPUT_QUICK(allApis(0xf5), lastApi(0xe6, 30), "iput-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IPUT_WIDE_QUICK(allApis(0xf6), lastApi(0xe7, 30), "iput-wide-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IPUT_OBJECT_QUICK(allApis(0xf7), lastApi(0xe8, 30), "iput-object-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),

    INVOKE_VIRTUAL_QUICK(allApis(0xf8), lastApi(0xe9, 30), "invoke-virtual-quick", opcode -> new Format.Format35c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    INVOKE_VIRTUAL_QUICK_RANGE(allApis(0xf9), lastApi(0xea, 30), "invoke-virtual-quick/range", opcode -> new Format.Format3rc(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    INVOKE_SUPER_QUICK(onlyDalvik(allApis(0xfa)), "invoke-super-quick", opcode -> new Format.Format35c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    INVOKE_SUPER_QUICK_RANGE(onlyDalvik(allApis(0xfb)), "invoke-super-quick/range", opcode -> new Format.Format3rc(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),

    //THROW_VERIFICATION_ERROR(onlyDalvik(firstApi(0xed, 5)), "throw-verification-error", TODO:Format.Format20bc::new, Opcode.ODEX_ONLY),

    //BREAKPOINT(onlyDalvik(firstApi(0xec, 8)), "breakpoint", Format.TODO::new, Opcode.ODEX_ONLY),

    IGET_VOLATILE(onlyDalvik(firstApi(0xe3, 9)), "iget-volatile", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    IPUT_VOLATILE(onlyDalvik(firstApi(0xe4, 9)), "iput-volatile", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    SGET_VOLATILE(onlyDalvik(firstApi(0xe5, 9)), "sget-volatile", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    SPUT_VOLATILE(onlyDalvik(firstApi(0xe6, 9)), "sput-volatile", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),

    IGET_WIDE_VOLATILE(onlyDalvik(firstApi(0xe8, 9)), "iget-wide-volatile", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    IPUT_WIDE_VOLATILE(onlyDalvik(firstApi(0xe9, 9)), "iput-wide-volatile", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    SGET_WIDE_VOLATILE(onlyDalvik(firstApi(0xea, 9)), "sget-wide-volatile", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    SPUT_WIDE_VOLATILE(onlyDalvik(firstApi(0xeb, 9)), "sput-wide-volatile", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),

    IGET_OBJECT_VOLATILE(onlyDalvik(firstApi(0xe7, 9)), "iget-object-volatile", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    IPUT_OBJECT_VOLATILE(onlyDalvik(firstApi(0xfc, 9)), "iput-object-volatile", opcode -> new Format.Format22c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    SGET_OBJECT_VOLATILE(onlyDalvik(firstApi(0xfd, 9)), "sget-object-volatile", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),
    SPUT_OBJECT_VOLATILE(onlyDalvik(firstApi(0xfe, 9)), "sput-object-volatile", opcode -> new Format.Format21c(opcode, ReferenceType.FIELD), Opcode.ODEX_ONLY),

    RETURN_VOID_BARRIER(firstApi(0xf1, 11), lastApi(0x73, 22), "return-void-barrier", Format.Format10x::new, Opcode.ODEX_ONLY),
    RETURN_VOID_NO_BARRIER(onlyArt(betweenApi(0x73, 23, 30)), "return-void-no-barrier", Format.Format10x::new, Opcode.ODEX_ONLY),

    IPUT_BOOLEAN_QUICK(onlyArt(betweenApi(0xeb, 23, 30)), "iput-boolean-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IPUT_BYTE_QUICK(onlyArt(betweenApi(0xec, 23, 30)), "iput-byte-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IPUT_CHAR_QUICK(onlyArt(betweenApi(0xed, 23, 30)), "iput-char-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IPUT_SHORT_QUICK(onlyArt(betweenApi(0xee, 23, 30)), "iput-short-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IGET_BOOLEAN_QUICK(onlyArt(betweenApi(0xef, 23, 30)), "iget-boolean-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IGET_BYTE_QUICK(onlyArt(betweenApi(0xf0, 23, 30)), "iget-byte-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IGET_CHAR_QUICK(onlyArt(betweenApi(0xf1, 23, 30)), "iget-char-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),
    IGET_SHORT_QUICK(onlyArt(betweenApi(0xf2, 23, 30)), "iget-short-quick", opcode -> new Format.Format22c(opcode, ReferenceType.RAW), Opcode.ODEX_ONLY),

    /*INVOKE_LAMBDA(onlyArt(betweenApi(0xf3, 24, 25)), "invoke-lambda", TODO:Format.Format25x::new, Opcode.EXPERIMENTAL_NOUGAT),
    CAPTURE_VARIABLE(onlyArt(betweenApi(0xf5, 24, 25)), "capture-variable", TODO:Format.Format21c(string)::new, Opcode.EXPERIMENTAL_NOUGAT),
    CREATE_LAMBDA(onlyArt(betweenApi(0xf6, 24, 25)), "create-lambda", TODO:Format.Format21c(method)::new, Opcode.EXPERIMENTAL_NOUGAT),
    LIBERATE_LAMBDA(onlyArt(betweenApi(0xf7, 24, 25)), "liberate-lambda", TODO:Format.Format22c(string)::new, Opcode.EXPERIMENTAL_NOUGAT),
    BOX_LAMBDA(onlyArt(betweenApi(0xf8, 24, 25)), "box-lambda", TODO:Format.Format22x::new, Opcode.EXPERIMENTAL_NOUGAT),
    UNBOX_LAMBDA(onlyArt(betweenApi(0xf9, 24, 25)), "unbox-lambda", TODO:Format.Format22c(type)::new, Opcode.EXPERIMENTAL_NOUGAT)*/;

    private static final int ODEX_ONLY = 1;

    @FunctionalInterface
    private interface DexConstraints {
        Integer opcodeValue(DexContext context);
    }

    private final DexConstraints constraints;
    private final String name;
    private final int flags;
    private final Format format;

    public String opname() {
        return name;
    }

    private Integer rawOpcodeValue(DexContext context) {
        return constraints.opcodeValue(context);
    }

    public int opcodeValue(DexContext context) {
        Integer opcode = rawOpcodeValue(context);
        if (opcode == null) {
            //TODO: more useful message
            throw new IllegalArgumentException("Can`t find opcode value for " + this);
        }
        return opcode;
    }

    public <T extends Format> T format() {
        //noinspection unchecked
        return (T) format;
    }

    public boolean isOdexOnly() {
        return (flags & ODEX_ONLY) != 0;
    }

    Opcode(int opcodeValue, String name, Function<Opcode, Format> format) {
        this(allApis(opcodeValue), name, format, 0);
    }

    Opcode(DexConstraints constraints, String name, Function<Opcode, Format> format) {
        this(constraints, name, format, 0);
    }

    Opcode(DexConstraints constraints, String name, Function<Opcode, Format> format, int flags) {
        this.flags = flags;
        this.constraints = isOdexOnly() ? onlyOdex(constraints) : constraints;
        this.name = name;
        this.format = format.apply(this);
    }

    Opcode(DexConstraints forDalvik, DexConstraints forArt, String name, Function<Opcode, Format> format, int flags) {
        this(combine(onlyDalvik(forDalvik), onlyArt(forArt)), name, format, flags);
    }

    public static SparseArray<Opcode> forContext(DexContext context) {
        SparseArray<Opcode> out = new SparseArray<>(values().length);
        for (Opcode op : values()) {
            Integer value = op.rawOpcodeValue(context);
            if (value != null) {
                out.put(value, op);
            }
        }
        return out;
    }

    private static DexConstraints allApis(int opcodeValue) {
        return context -> opcodeValue;
    }

    private static DexConstraints firstApi(int opcodeValue, int api) {
        return context -> {
            int target = context.getOptions().getTargetApi();
            return target < api ? null : opcodeValue;
        };
    }

    private static DexConstraints lastApi(int opcodeValue, int api) {
        return context -> {
            int target = context.getOptions().getTargetApi();
            return target > api ? null : opcodeValue;
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static DexConstraints betweenApi(int opcodeValue, int minApi, int maxApi) {
        return context -> {
            int target = context.getOptions().getTargetApi();
            return (target < minApi || target > maxApi) ? null : opcodeValue;
        };
    }

    private static boolean checkDexVersion(DexVersion min, DexVersion target) {
        return target.isCompact() || (min.ordinal() >= target.ordinal());
    }

    private static DexConstraints firstDexVersionOrCDex(DexVersion version, DexConstraints constraints) {
        return context -> checkDexVersion(version, context.getDexVersion()) ?
                constraints.opcodeValue(context) : null;
    }

    private static DexConstraints onlyOdex(DexConstraints constraints) {
        return context -> context.getOptions().hasOdexInstructions() ?
                constraints.opcodeValue(context) : null;
    }

    private static DexConstraints onlyArt(DexConstraints constraints) {
        return context -> context.getOptions().isTargetForArt() ?
                constraints.opcodeValue(context) : null;
    }

    private static DexConstraints onlyDalvik(DexConstraints constraints) {
        return context -> context.getOptions().isTargetForDalvik() ?
                constraints.opcodeValue(context) : null;
    }

    private static DexConstraints combine(DexConstraints first, DexConstraints second) {
        return context -> {
            Integer opcode = first.opcodeValue(context);
            return opcode == null ? second.opcodeValue(context) : opcode;
        };
    }
}
