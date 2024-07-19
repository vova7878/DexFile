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

import com.v7878.dex.bytecode.Opcode;
import com.v7878.dex.io.RandomInput;
import com.v7878.dex.util.SparseArray;

// Temporary object. Needed to read dex
final class ReadContextImpl implements ReadContext {

    private String[] strings;
    private TypeId[] types;
    private ProtoId[] protos;
    private FieldId[] fields;
    private MethodId[] methods;
    private MethodHandleItem[] method_handles;
    private CallSiteId[] call_sites;

    private final SparseArray<Opcode> opcodes;

    private final ReadOptions options;

    private final DexVersion dex_version;

    private final RandomInput data_base;

    private static RandomInput getDataBase(RandomInput in, DexVersion version, int data_off) {
        if (version.isCompact()) {
            // Note: start position of base should be 0
            return in.slice(data_off);
        }
        return in;
    }

    public ReadContextImpl(RandomInput in, ReadOptions options, FileMap map) {
        this.options = options;
        this.dex_version = map.version;
        this.opcodes = Opcode.forContext(this);
        // TODO: add a way to redirect data to another source (maybe from options...)
        this.data_base = getDataBase(in, dex_version, map.data_off);
    }

    @Override
    public RandomInput data(int offset) {
        return data_base.duplicate(offset);
    }

    @Override
    public ReadOptions getOptions() {
        return options;
    }

    @Override
    public DexVersion getDexVersion() {
        return dex_version;
    }

    @Override
    public String string(int index) {
        return strings[index];
    }

    @Override
    public TypeId type(int index) {
        return types[index];
    }

    @Override
    public ProtoId proto(int index) {
        return protos[index];
    }

    @Override
    public FieldId field(int index) {
        return fields[index];
    }

    @Override
    public MethodId method(int index) {
        return methods[index];
    }

    @Override
    public MethodHandleItem method_handle(int index) {
        return method_handles[index];
    }

    @Override
    public CallSiteId call_site(int index) {
        return call_sites[index];
    }

    @Override
    public Opcode opcode(int opcodeValue) {
        Opcode out = opcodes.get(opcodeValue);
        if (out == null) {
            throw new IllegalStateException("unknown opcode: " + Integer.toHexString(opcodeValue));
        }
        return out;
    }

    public void setStrings(String[] strings) {
        this.strings = strings;
    }

    public void setTypes(TypeId[] types) {
        this.types = types;
    }

    public void setProtos(ProtoId[] protos) {
        this.protos = protos;
    }

    public void setFields(FieldId[] fields) {
        this.fields = fields;
    }

    public void setMethods(MethodId[] methods) {
        this.methods = methods;
    }

    public void setMethodHandles(MethodHandleItem[] method_handles) {
        this.method_handles = method_handles;
    }

    public void setCallSites(CallSiteId[] call_sites) {
        this.call_sites = call_sites;
    }
}
