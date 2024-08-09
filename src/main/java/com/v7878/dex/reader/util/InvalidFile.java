package com.v7878.dex.reader.util;

public class InvalidFile extends RuntimeException {
    public InvalidFile() {
    }

    public InvalidFile(String message) {
        super(message);
    }

    public InvalidFile(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFile(Throwable cause) {
        super(cause);
    }
}
