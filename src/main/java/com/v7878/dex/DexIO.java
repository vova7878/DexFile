package com.v7878.dex;

import static com.v7878.dex.util.Checks.checkIndex;

import com.v7878.dex.immutable.CallSiteId;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldId;
import com.v7878.dex.immutable.MethodHandleId;
import com.v7878.dex.immutable.MethodId;
import com.v7878.dex.immutable.ProtoId;
import com.v7878.dex.immutable.TypeId;
import com.v7878.dex.io.ByteArrayIO;
import com.v7878.dex.io.ByteArrayInput;
import com.v7878.dex.raw.DexReader;
import com.v7878.dex.raw.DexWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DexIO {
    private DexIO() {
    }

    public interface DexMapEntry {
        int type();

        int size();

        int offset();
    }

    public interface DexReaderCache {
        private static <T> T getValue(List<T> section, int index, String name) {
            checkIndex(index, section.size(), name);
            return section.get(index);
        }

        List<String> getStrings();

        List<TypeId> getTypes();

        List<FieldId> getFields();

        List<ProtoId> getProtos();

        List<MethodId> getMethods();

        List<ClassDef> getClasses();

        List<MethodHandleId> getMethodHandles();

        List<CallSiteId> getCallSites();

        List<? extends DexMapEntry> getMapItems();

        default int getStringCount() {
            return getStrings().size();
        }

        default int getTypeCount() {
            return getTypes().size();
        }

        default int getFieldCount() {
            return getFields().size();
        }

        default int getProtoCount() {
            return getProtos().size();
        }

        default int getMethodCount() {
            return getMethods().size();
        }

        default int getClassCount() {
            return getClasses().size();
        }

        default int getMethodHandleCount() {
            return getMethodHandles().size();
        }

        default int getCallSiteCount() {
            return getCallSites().size();
        }

        default String getString(int index) {
            return getValue(getStrings(), index, "string");
        }

        default TypeId getType(int index) {
            return getValue(getTypes(), index, "type");
        }

        default FieldId getField(int index) {
            return getValue(getFields(), index, "field");
        }

        default ProtoId getProto(int index) {
            return getValue(getProtos(), index, "proto");
        }

        default MethodId getMethod(int index) {
            return getValue(getMethods(), index, "method");
        }

        default ClassDef getClass(int index) {
            return getValue(getClasses(), index, "class");
        }

        default MethodHandleId getMethodHandle(int index) {
            return getValue(getMethodHandles(), index, "method handle");
        }

        default CallSiteId getCallSite(int index) {
            return getValue(getCallSites(), index, "callsite");
        }

        // TODO: Other sections?
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

    public static Dex read(ReadOptions options, byte[] data,
                           int data_offset, int header_offset, int[] ids) {
        Objects.requireNonNull(options);
        var input = new ByteArrayInput(data).sliceAt(data_offset);
        var reader = new DexReader(options, input, header_offset);
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

    public static Dex read(ReadOptions options, byte[] data,
                           int data_offset, int header_offset) {
        return read(options, data, data_offset, header_offset, null);
    }

    public static Dex read(byte[] data) {
        return read(ReadOptions.defaultOptions(), data, 0, 0);
    }

    public static DexReaderCache readCache(ReadOptions options, byte[] data,
                                           int data_offset, int header_offset) {
        Objects.requireNonNull(options);
        var input = new ByteArrayInput(data).sliceAt(data_offset);
        return new DexReader(options, input, header_offset);
    }

    public static DexReaderCache readCache(byte[] data) {
        return readCache(ReadOptions.defaultOptions(), data, 0, 0);
    }

    //TODO: public static Dex[] readDexContainer(ReadOptions options, byte[] data, int[][] ids) {}

    public static Dex[] readDexContainer(ReadOptions options, byte[] data, int data_offset) {
        Objects.requireNonNull(options);
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
