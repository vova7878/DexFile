package com.v7878.dex.smali;

import com.v7878.dex.Opcode;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class SmaliParserBase extends Parser {
    private static final Pattern REGISTER = Pattern.compile(
            "[vp][0-9]+"
    );
    private static final String INTEGRAL = """
            -?(0\
            |([1-9][0-9]*)\
            |(0[0-7]+)\
            |(0[xX][0-9a-fA-F]+))\
            """;
    private static final Pattern INTEGER = Pattern.compile(INTEGRAL);
    private static final Pattern LONG = Pattern.compile(INTEGRAL + "[lL]");
    private static final Pattern SHORT = Pattern.compile(INTEGRAL + "[sS]");
    private static final Pattern BYTE = Pattern.compile(INTEGRAL + "[tT]");
    private static final String FP = """
            (-?[0-9]+[eE]-?[0-9]+)\
            |(-?0[xX][0-9a-fA-F]+[pP]-?[0-9]+)\
            |(-?[iI][nN][fF][iI][nN][iI][tT][yY])\
            |([nN][aA][nN])\
            """;
    private static final Pattern FLOAT = Pattern.compile(
            "(" + FP + ")[fF]|(-?[0-9]+[fF])"
    );
    private static final Pattern DOUBLE = Pattern.compile(
            "(" + FP + ")[dD]?|(-?[0-9]+[dD])"
    );
    private static final Map<String, Opcode> opcodesByName;

    static {
        var names = new HashMap<String, Opcode>();
        for (var opcode : Opcode.values()) {
            if (names.putIfAbsent(opcode.opname(), opcode) != null) {
                throw new IllegalStateException("Dublicate: " + opcode);
            }
        }
        opcodesByName = Collections.unmodifiableMap(names);
    }

    public SmaliParserBase(TokenStream input) {
        super(input);
    }

    public static Opcode opcode(String opname) {
        var opcode = opcodesByName.get(opname);
        if (opcode == null || opcode.isPayload()) {
            throw new IllegalStateException("Opcode not found: " + opname);
        }
        return opcode;
    }

    private boolean matchText(Predicate<String> predicate) {
        var token = getCurrentToken();
        if (token == null) return false;
        return predicate.test(token.getText());
    }

    private boolean matchText(Pattern pattern) {
        return matchText(token -> pattern.matcher(token).matches());
    }

    public boolean isRegister() {
        return matchText(REGISTER);
    }

    public boolean isNull() {
        return matchText("null"::equals);
    }

    public boolean isBool() {
        return matchText(token -> switch (token) {
            case "true", "false" -> true;
            default -> false;
        });
    }

    public boolean isAnnotationVisibility() {
        return matchText(token -> switch (token) {
            case "build", "runtime", "system" -> true;
            default -> false;
        });
    }

    public boolean isAccessFlag() {
        return matchText(token -> switch (token) {
            case "public", "private", "protected", "static", "final", "synchronized", "super",
                 "volatile", "bridge", "transient", "varargs", "native", "interface", "abstract",
                 "strictfp", "synthetic", "annotation", "enum", "mandated", "constructor",
                 "declared-synchronized", "verified", "optimized" -> true;
            default -> false;
        });
    }

    public boolean isRestrictionFlag() {
        return matchText(token -> switch (token) {
            case "whitelist", "greylist", "blacklist", "greylist-max-o",
                 "greylist-max-p", "greylist-max-q", "greylist-max-r",
                 "greylist-max-s", "core-platform-api", "test-api" -> true;
            default -> false;
        });
    }

    public boolean isMethodHandleTypeField() {
        return matchText(token -> switch (token) {
            case "static-put", "static-get", "instance-put", "instance-get" -> true;
            default -> false;
        });
    }

    public boolean isMethodHandleTypeMethod() {
        return matchText(token -> switch (token) {
            case "invoke-instance", "invoke-constructor", "invoke-direct",
                 "invoke-static", "invoke-interface" -> true;
            default -> false;
        });
    }

    public boolean isInteger() {
        return matchText(INTEGER);
    }

    public boolean isLong() {
        return matchText(LONG);
    }

    public boolean isShort() {
        return matchText(SHORT);
    }

    public boolean isByte() {
        return matchText(BYTE);
    }

    public boolean isFloat() {
        return matchText(FLOAT);
    }

    public boolean isDouble() {
        return matchText(DOUBLE);
    }
}
