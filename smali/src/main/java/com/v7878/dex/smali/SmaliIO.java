package com.v7878.dex.smali;

import com.v7878.dex.immutable.Dex;
import com.v7878.dex.smali.parser.SmaliLexer;
import com.v7878.dex.smali.parser.SmaliParser;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class SmaliIO {
    public static Dex parse(String smali) {
        var stream = CharStreams.fromString(smali);
        var lexer = new SmaliLexer(stream);
        var tokens = new CommonTokenStream(lexer);

        var parser = new SmaliParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        return parser.smali().dex;
    }
}
