package com.v7878.dex;

import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.ByteArrayInput;
import com.v7878.dex.raw.DexReader;
import com.v7878.dex.raw.DexWriter;

import java.util.ArrayList;
import java.util.Objects;

public final class DexIO {
    private DexIO() {
    }

    public static class InvalidDexFile extends RuntimeException {
        public InvalidDexFile() {
        }

        public InvalidDexFile(String message) {
            super(message);
        }

        public InvalidDexFile(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidDexFile(Throwable cause) {
            super(cause);
        }
    }

    public static class NotADexFile extends RuntimeException {
        public NotADexFile() {
        }

        public NotADexFile(String message) {
            super(message);
        }

        public NotADexFile(String message, Throwable cause) {
            super(message, cause);
        }

        public NotADexFile(Throwable cause) {
            super(cause);
        }
    }

    public static Dex read(ReadOptions options, byte[] data, int data_offset, int[] ids) {
        Objects.requireNonNull(options);
        var input = new ByteArrayInput(data).sliceAt(data_offset);
        DexReader reader = new DexReader(options, input, 0);
        var classes = reader.getClasses();
        if (ids == null) {
            return Dex.of(classes);
        }
        var out = new ArrayList<ClassDef>(ids.length);
        for (int idx : ids) {
            out.add(classes.get(idx));
        }
        return Dex.of(out);
    }

    public static Dex read(ReadOptions options, byte[] data, int data_offset) {
        return read(options, data, data_offset, null);
    }

    public static Dex read(byte[] data) {
        return read(ReadOptions.defaultOptions(), data, 0);
    }

    //TODO: public static Dex[] readDexContainer(ReadOptions options, byte[] data, int[][] ids) {}

    public static Dex[] readDexContainer(ReadOptions options, byte[] data, int data_offset) {
        var input = new ByteArrayInput(data).sliceAt(data_offset);
        var readers = new ArrayList<DexReader>();
        int header_offset = 0;
        while (header_offset != input.size()) {
            var reader = new DexReader(options, input, header_offset);
            readers.add(reader);
            header_offset += reader.getFileSize();
        }
        var out = new Dex[readers.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = Dex.of(readers.get(i).getClasses());
        }
        return out;
    }

    public static byte[] write(WriteOptions options, Dex data) {
        Objects.requireNonNull(options);
        Objects.requireNonNull(data);
        var io = new ByteArrayIO();
        DexWriter writer = new DexWriter(options, io, data, 0);
        writer.finalizeHeader(writer.getFileSize());
        return io.toByteArray();
    }

    public static byte[] write(Dex data) {
        return write(WriteOptions.defaultOptions(), data);
    }

    public static byte[] writeDexContainer(WriteOptions options, Dex[] data) {
        Objects.requireNonNull(data);
        var version = options.getDexVersion();
        if (!version.isDexContainer()) {
            throw new IllegalArgumentException(
                    "Illegal dex container version " + version);
        }
        for (var dex : data) Objects.requireNonNull(dex);
        // TODO: shared string section?
        var io = new ByteArrayIO();
        var writers = new DexWriter[data.length];
        int header_offset = 0;
        for (int i = 0; i < data.length; i++) {
            writers[i] = new DexWriter(options, io, data[i], header_offset);
            header_offset += writers[i].getFileSize();
        }
        for (var writer : writers) {
            writer.finalizeHeader(header_offset);
        }
        return io.toByteArray();
    }
}
