package com.v7878.dex.reader.util;

public class NotADexFile extends RuntimeException {
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
