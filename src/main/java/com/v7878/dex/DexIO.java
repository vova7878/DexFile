package com.v7878.dex;

import com.v7878.dex.immutable.Dex;
import com.v7878.dex.raw.DexReader;

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

    //TODO: public static Dex read(ReadOptions options, byte[] data, int[] ids) {}

    public static Dex read(ReadOptions options, byte[] data) {
        DexReader reader = new DexReader(options, data);
        return Dex.of(reader.getClasses());
    }

    public static Dex read(byte[] data) {
        return read(ReadOptions.defaultOptions(), data);
    }

    //TODO: public static Dex[] readDexContainer(ReadOptions options, byte[] data) {}

    //TODO: public static byte[] write(WriteOptions options, Dex data) {}

    //TODO: public static byte[] write(Dex data) {}

    //TODO: public static byte[] writeDexContainer(WriteOptions options, Dex[] data) {}
}
