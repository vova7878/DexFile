package com.v7878.dex.smali.printer;

import java.util.Objects;

/**
 * Mechanical text layer of the smali printer: an {@link Appendable} with
 * four-space indentation, line-start handling, a comment mode that prefixes
 * every written line, and the literal, quoting and hex forms the smali
 * grammar accepts. Knows no model types beyond primitives and strings
 */
public final class SmaliEmitter {
    private final StringBuilder out;
    private int indent;
    private boolean lineStart = true;
    private boolean commenting;

    public SmaliEmitter(StringBuilder out) {
        this.out = Objects.requireNonNull(out);
    }

    /**
     * Adds one indentation level (four spaces).
     */
    public SmaliEmitter indent() {
        indent += 4;
        return this;
    }

    /**
     * Removes one indentation level.
     */
    public SmaliEmitter deindent() {
        indent -= 4;
        return this;
    }

    /**
     * Ends the current line.
     */
    public SmaliEmitter newline() {
        out.append('\n');
        lineStart = true;
        return this;
    }

    /**
     * Enters comment mode: every following line is prefixed with
     * {@code # } until {@link #endComment()}.
     */
    public SmaliEmitter startComment() {
        commenting = true;
        return this;
    }

    /**
     * Leaves comment mode.
     */
    public SmaliEmitter endComment() {
        commenting = false;
        return this;
    }

    /**
     * Appends raw text; the pending indent is written first.
     */
    public SmaliEmitter append(CharSequence text) {
        Objects.requireNonNull(text);
        writeIndent();
        out.append(text);
        return this;
    }

    /**
     * Appends a raw character; the pending indent is written first.
     */
    public SmaliEmitter append(char c) {
        writeIndent();
        out.append(c);
        return this;
    }

    /**
     * Appends a value as a signed decimal.
     */
    public SmaliEmitter append(long value) {
        return append(Long.toString(value));
    }

    /**
     * Appends a value as an instruction or payload literal: unsigned
     * lowercase hex with a {@code -} sign and an {@code L} suffix when the
     * value does not fit an {@code int}.
     */
    public SmaliEmitter literal(long value) {
        if (value < 0) {
            append("-0x").hex(-value);
            if (value < Integer.MIN_VALUE) {
                append('L');
            }
        } else {
            append("0x").hex(value);
            if (value > Integer.MAX_VALUE) {
                append('L');
            }
        }
        return this;
    }

    /**
     * Appends a value as a long literal: like {@link #literal(long)} but
     * with the {@code L} suffix also when the value fits an {@code int}.
     */
    public SmaliEmitter longLiteral(long value) {
        if (value < 0) {
            append("-0x").hex(-value).append('L');
        } else {
            append("0x").hex(value).append('L');
        }
        return this;
    }

    /**
     * Appends a value as unsigned lowercase hex, without the 0x prefix.
     */
    public SmaliEmitter hex(long value) {
        writeIndent();
        if (value == 0) {
            out.append('0');
            return this;
        }
        // written back to front into the smallest buffer that fits
        char[] buffer = new char[16];
        int index = buffer.length;
        while (value != 0) {
            int digit = (int) (value & 0xf);
            buffer[--index] = (char) (digit < 10
                    ? '0' + digit : 'a' + digit - 10);
            value >>>= 4;
        }
        out.append(new String(buffer, index, buffer.length - index));
        return this;
    }

    /**
     * Appends a signed offset in the {@code +0xN} / {@code -0xN} form.
     */
    public SmaliEmitter offset(long value) {
        if (value < 0) {
            return append("-0x").hex(-value);
        }
        return append("+0x").hex(value);
    }

    /**
     * Appends a string as a double-quoted literal with smali escapes.
     */
    public SmaliEmitter quoted(String value) {
        Objects.requireNonNull(value);
        append('"');
        for (int i = 0; i < value.length(); i++) {
            appendEscaped(value.charAt(i));
        }
        return append('"');
    }

    /**
     * Appends a character as a single-quoted literal with smali escapes.
     */
    public SmaliEmitter quoted(char value) {
        append('\'');
        appendEscaped(value);
        return append('\'');
    }

    /**
     * Appends a member, element or call-site name: {@code <init>} and
     * {@code <clinit>} verbatim, a backtick-quoted form when the name
     * contains characters a bare simple name cannot hold, verbatim
     * otherwise.
     */
    public SmaliEmitter simpleName(String name) {
        Objects.requireNonNull(name);
        if (name.equals("<init>") || name.equals("<clinit>")) {
            return append(name);
        }
        if (needsQuotes(name)) {
            return append('`').append(name).append('`');
        }
        return append(name);
    }

    private void appendEscaped(char c) {
        if (c == '\n') {
            append("\\n");
        } else if (c == '\r') {
            append("\\r");
        } else if (c == '\t') {
            append("\\t");
        } else if (c == '\b') {
            append("\\b");
        } else if (c == '\f') {
            append("\\f");
        } else if (c == '\'' || c == '"' || c == '\\') {
            append('\\').append(c);
        } else if (c < ' ' || c >= 0x7f) {
            append("\\u");
            for (int shift = 12; shift >= 0; shift -= 4) {
                int digit = (c >> shift) & 0xf;
                append((char) (digit < 10 ? '0' + digit : 'a' + digit - 10));
            }
        } else {
            append(c);
        }
    }

    private void writeIndent() {
        if (!lineStart) {
            return;
        }
        lineStart = false;
        out.append(" ".repeat(Math.max(0, indent)));
        if (commenting) {
            out.append("# ");
        }
    }

    private static boolean needsQuotes(String name) {
        return name.codePoints().anyMatch(cp -> !isSimpleCodePoint(cp));
    }

    // the SimpleChar set, minus the space that only a quoted
    // simple name may contain.
    // https://source.android.com/docs/core/runtime/dex-format#simplename
    private static boolean isSimpleCodePoint(int c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')) {
            return true;
        }
        if (c == '$' || c == '-' || c == '_') {
            return true;
        }
        if (c == 0x00a0) {
            return true;
        }
        if (c >= 0x00a1 && c <= 0x1fff) {
            return true;
        }
        if (c >= 0x2000 && c <= 0x200a) {
            return true;
        }
        if (c >= 0x2010 && c <= 0x2027) {
            return true;
        }
        if (c == 0x202f) {
            return true;
        }
        if (c >= 0x2030 && c <= 0xd7ff) {
            return true;
        }
        if (c >= 0xe000 && c <= 0xffef) {
            return true;
        }
        return c >= 0x10000 && c <= 0x10ffff;
    }
}
