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

import java.util.function.IntFunction;

// Temporary object. Needed to read dex
final class ReadContextImpl implements ReadContext {

    private IntFunction<String> strings;
    private IntFunction<TypeId> types;
    private IntFunction<ProtoId> protos;
    private IntFunction<FieldId> fields;
    private IntFunction<MethodId> methods;
    private IntFunction<MethodHandleItem> method_handles;
    private IntFunction<CallSiteId> call_sites;

    private final SparseArray<Opcode> opcodes;

    private final ReadOptions options;

    private final DexVersion dex_version;

    private final RandomInput data_base;

    private static RandomInput getDataBase(RandomInput in, ReadOptions options, DexVersion version, int data_off) {
        RandomInput base = options.getRedirectedDataBase();
        if (base == null) {
            base = version.isCompact() ? in.duplicate(data_off) : in;
        }
        if (base.position() != 0) {
            base = base.slice();
        }
        return base;
    }

    public ReadContextImpl(RandomInput in, ReadOptions options, FileMap map) {
        this.options = options;
        this.dex_version = map.version;
        this.opcodes = Opcode.forContext(this);
        this.data_base = getDataBase(in, options, dex_version, map.data_off);
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
        return strings.apply(index);
    }

    @Override
    public TypeId type(int index) {
        return types.apply(index);
    }

    @Override
    public ProtoId proto(int index) {
        return protos.apply(index);
    }

    @Override
    public FieldId field(int index) {
        return fields.apply(index);
    }

    @Override
    public MethodId method(int index) {
        return methods.apply(index);
    }

    @Override
    public MethodHandleItem method_handle(int index) {
        return method_handles.apply(index);
    }

    @Override
    public CallSiteId call_site(int index) {
        return call_sites.apply(index);
    }

    @Override
    public Opcode opcode(int opcodeValue) {
        Opcode out = opcodes.get(opcodeValue);
        if (out == null) {
            throw new IllegalStateException("unknown opcode: " + Integer.toHexString(opcodeValue));
        }
        return out;
    }

    public void initStrings(String[] strings) {
        this.strings = i -> strings[i];
    }

    public void initStrings(int count, IntFunction<String> reader) {
        var array = new String[count];
        this.strings = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }

    public void initTypes(TypeId[] types) {
        this.types = i -> types[i];
    }

    public void initTypes(int count, IntFunction<TypeId> reader) {
        var array = new TypeId[count];
        this.types = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }

    public void initProtos(ProtoId[] protos) {
        this.protos = i -> protos[i];
    }

    public void initProtos(int count, IntFunction<ProtoId> reader) {
        var array = new ProtoId[count];
        this.protos = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }

    public void initFields(FieldId[] fields) {
        this.fields = i -> fields[i];
    }

    public void initFields(int count, IntFunction<FieldId> reader) {
        var array = new FieldId[count];
        this.fields = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }

    public void initMethods(MethodId[] methods) {
        this.methods = i -> methods[i];
    }

    public void initMethods(int count, IntFunction<MethodId> reader) {
        var array = new MethodId[count];
        this.methods = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }

    public void initMethodHandles(MethodHandleItem[] method_handles) {
        this.method_handles = i -> method_handles[i];
    }

    public void initMethodHandles(int count, IntFunction<MethodHandleItem> reader) {
        var array = new MethodHandleItem[count];
        this.method_handles = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }

    public void initCallSites(CallSiteId[] call_sites) {
        this.call_sites = i -> call_sites[i];
    }

    public void initCallSites(int count, IntFunction<CallSiteId> reader) {
        var array = new CallSiteId[count];
        this.call_sites = i -> {
            var value = array[i];
            return value == null ? array[i] = reader.apply(i) : value;
        };
    }
}
