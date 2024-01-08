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

import com.v7878.dex.io.RandomInput;
import com.v7878.dex.io.RandomOutput;

import java.util.Objects;

public final class CatchHandlerElement implements Mutable {

    private TypeId type;
    private int address;

    public CatchHandlerElement(TypeId type, int address) {
        setType(type);
        setAddress(address);
    }

    public void setType(TypeId type) {
        this.type = Objects.requireNonNull(type,
                "catch handler type can`t be null").mutate();
    }

    public TypeId getType() {
        return type;
    }

    public void setAddress(int address) {
        if (address < 0) {
            throw new IllegalArgumentException("instruction address can`t be negative");
        }
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    public static CatchHandlerElement read(RandomInput in, ReadContext context) {
        return new CatchHandlerElement(context.type(in.readULeb128()), in.readULeb128());
    }

    public void collectData(DataCollector data) {
        data.add(type);
    }

    public void write(WriteContext context, RandomOutput out) {
        out.writeULeb128(context.getTypeIndex(type));
        out.writeULeb128(address);
    }

    @Override
    public String toString() {
        return type + " " + address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CatchHandlerElement) {
            CatchHandlerElement eobj = (CatchHandlerElement) obj;
            return address == eobj.address
                    && Objects.equals(type, eobj.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, type);
    }

    @Override
    public CatchHandlerElement mutate() {
        return new CatchHandlerElement(type, address);
    }
}
