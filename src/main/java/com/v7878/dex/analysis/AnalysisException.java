package com.v7878.dex.analysis;

/**
 * Signals that method analysis cannot proceed.
 */
public class AnalysisException extends RuntimeException {
    public AnalysisException() {
        super();
    }

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnalysisException(Throwable cause) {
        super(cause);
    }
}
