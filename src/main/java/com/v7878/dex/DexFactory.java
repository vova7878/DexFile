package com.v7878.dex;

import com.v7878.dex.immutable.Dex;
import com.v7878.dex.raw.DexReader;

public final class DexFactory {
    private DexFactory() {
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

    //TODO: public static Dex loadDex(ReadOptions options, byte[] data, int[] ids) {}

    public static Dex loadDex(ReadOptions options, byte[] data) {
        DexReader reader = new DexReader(options, data);
        return Dex.of(reader.getClasses());
    }
}
