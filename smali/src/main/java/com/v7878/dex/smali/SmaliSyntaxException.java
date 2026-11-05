package com.v7878.dex.smali;

/**
 * Thrown when smali text is not grammatical
 */
public class SmaliSyntaxException extends IllegalArgumentException {
    private final int line;
    private final int column;

    public SmaliSyntaxException(String detail) {
        this(0, 0, detail, null);
    }

    public SmaliSyntaxException(int line, int column, String detail) {
        this(line, column, detail, null);
    }

    public SmaliSyntaxException(int line, int column, String detail, Throwable cause) {
        super(message(line, column, detail), cause);
        this.line = line;
        this.column = column;
    }

    private static String message(int line, int column, String detail) {
        if (line <= 0 || column <= 0) {
            return detail;
        }
        return "line " + line + ":" + column + ": " + detail;
    }

    /**
     * Returns the 1-based line of the error, or {@code 0} when the position is unknown.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the 1-based column of the error, or {@code 0} when the position is unknown.
     */
    public int getColumn() {
        return column;
    }
}
