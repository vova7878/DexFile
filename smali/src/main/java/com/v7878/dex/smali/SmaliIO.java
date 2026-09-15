package com.v7878.dex.smali;

import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.smali.parser.SmaliLexer;
import com.v7878.dex.smali.parser.SmaliParser;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class SmaliIO {
    private static SmaliParser parser(String smali) {
        var stream = CharStreams.fromString(smali);
        var lexer = new SmaliLexer(stream);
        var tokens = new CommonTokenStream(lexer);

        var parser = new SmaliParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());

        return parser;
    }

    public static Dex parse(String smali) {
        return parser(smali).full_dex_file().dex_file().value;
    }

    public static Annotation parseAnnotation(String smali) {
        return parser(smali).full_annotation().annotation().value;
    }

    public static ClassDef parseClass(String smali) {
        return parser(smali).full_class_def().class_def().value;
    }

    public static FieldDef parseField(String smali) {
        return parser(smali).full_field().field().value;
    }

    public static MethodDef parseMethod(String smali) {
        return parser(smali).full_method().method().value;
    }
}
