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
import com.v7878.dex.util.MutableList;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CatchHandler implements Mutable {

    private MutableList<CatchHandlerElement> handlers;
    private Integer catch_all_addr;

    public CatchHandler(Collection<CatchHandlerElement> handlers,
                        Integer catch_all_addr) {
        setHandlers(handlers);
        setCatchAllAddress(catch_all_addr);
    }

    public CatchHandler() {
        this(null, null);
    }

    public void setHandlers(Collection<CatchHandlerElement> handlers) {
        this.handlers = handlers == null
                ? MutableList.empty() : new MutableList<>(handlers);
    }

    public MutableList<CatchHandlerElement> getHandlers() {
        return handlers;
    }

    public void setCatchAllAddress(Integer catch_all_addr) {
        if (catch_all_addr != null && catch_all_addr < 0) {
            throw new IllegalArgumentException("instruction address can`t be negative");
        }
        this.catch_all_addr = catch_all_addr;
    }

    public Integer getCatchAllAddress() {
        return catch_all_addr;
    }

    public static CatchHandler read(RandomInput in, ReadContext context) {
        int size = in.readSLeb128();
        int handlersCount = Math.abs(size);
        MutableList<CatchHandlerElement> handlers = MutableList.empty();
        for (int i = 0; i < handlersCount; i++) {
            handlers.add(CatchHandlerElement.read(in, context));
        }
        Integer catch_all_addr = null;
        if (size <= 0) {
            catch_all_addr = in.readULeb128();
        }
        return new CatchHandler(handlers, catch_all_addr);
    }

    public void collectData(DataCollector data) {
        for (CatchHandlerElement tmp : handlers) {
            data.fill(tmp);
        }
    }

    public void write(WriteContext context, RandomOutput out) {
        if (handlers.isEmpty() && catch_all_addr == null) {
            throw new IllegalArgumentException("unable to write empty catch handler");
        }
        out.writeSLeb128(catch_all_addr == null ? handlers.size() : -handlers.size());
        for (CatchHandlerElement tmp : handlers) {
            tmp.write(context, out);
        }
        if (catch_all_addr != null) {
            out.writeULeb128(catch_all_addr);
        }
    }

    @Override
    public String toString() {
        return "catch handler " + catch_all_addr + " " + handlers.stream()
                .map((handler) -> handler.getType() + " " + handler.getAddress())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof CatchHandler chobj
                && Objects.equals(catch_all_addr, chobj.catch_all_addr)
                && Objects.equals(handlers, chobj.handlers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handlers, catch_all_addr);
    }

    @Override
    public CatchHandler mutate() {
        return new CatchHandler(handlers, catch_all_addr);
    }
}
