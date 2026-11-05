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
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.function.Function;

public class SmaliIO {
    private static <T> T parseRule(String smali, Function<SmaliParser, T> rule) {
        var lexer = new SmaliLexer(CharStreams.fromString(smali));
        lexer.removeErrorListeners();
        lexer.addErrorListener(SmaliErrorListener.INSTANCE);

        var tokens = new CommonTokenStream(lexer);
        // Lex eagerly: a lexer error must abort before the parser can act on
        // the characters the lexer would otherwise skip during recovery.
        tokens.fill();

        var parser = new SmaliParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SmaliErrorListener.INSTANCE);
        parser.setErrorHandler(new BailErrorStrategy());

        try {
            return rule.apply(parser);
        } catch (ParseCancellationException e) {
            throw syntaxException(e);
        }
    }

    // BailErrorStrategy.recoverInline bypasses the error listeners, so the
    // exception it throws still has to be converted here.
    private static SmaliSyntaxException syntaxException(ParseCancellationException e) {
        for (var cause = e.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof SmaliSyntaxException syntax) {
                return syntax;
            }
            if (cause instanceof RecognitionException recognition) {
                var token = recognition.getOffendingToken();
                if (token == null) {
                    break;
                }
                int line = token.getLine();
                int column = token.getCharPositionInLine() + 1;
                if (token.getType() == Token.EOF) {
                    return new SmaliSyntaxException(line, column, "unexpected end of input", e);
                }
                return new SmaliSyntaxException(line, column,
                        "unexpected token: " + token.getText(), e);
            }
        }
        return new SmaliSyntaxException(0, 0, "invalid smali input", e);
    }

    public static Dex parse(String smali) {
        return parseRule(smali, parser -> parser.full_dex_file().dex_file().value);
    }

    public static Annotation parseAnnotation(String smali) {
        return parseRule(smali, parser -> parser.full_annotation().annotation().value);
    }

    public static ClassDef parseClass(String smali) {
        return parseRule(smali, parser -> parser.full_class_def().class_def().value);
    }

    public static FieldDef parseField(String smali) {
        return parseRule(smali, parser -> parser.full_field().field().value);
    }

    public static MethodDef parseMethod(String smali) {
        return parseRule(smali, parser -> parser.full_method().method().value);
    }
}
