package com.v7878.dex.smali;

import com.v7878.dex.immutable.Annotation;
import com.v7878.dex.immutable.ClassDef;
import com.v7878.dex.immutable.Dex;
import com.v7878.dex.immutable.FieldDef;
import com.v7878.dex.immutable.MethodDef;
import com.v7878.dex.smali.parser.SmaliLexer;
import com.v7878.dex.smali.parser.SmaliParser;
import com.v7878.dex.smali.printer.SmaliEmitter;
import com.v7878.dex.smali.printer.SmaliPrinter;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.function.Consumer;
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

    // ------------------------- printing -------------------------

    private static String print(Consumer<SmaliPrinter> action,
                                SmaliOptions options) {
        var sb = new StringBuilder();
        action.accept(new SmaliPrinter(new SmaliEmitter(sb), options));
        return sb.toString();
    }

    /**
     * Prints every class of the dex with the default options; the result
     * re-parses with {@link #parse(String)} to an equal dex for models the
     * library itself produces.
     */
    public static String print(Dex dex) {
        return print(dex, SmaliOptions.defaultOptions());
    }

    /**
     * Prints every class of the dex with the given options; classes are
     * separated by a blank line. See the {@code print} overloads for the
     * round-trip contract.
     */
    public static String print(Dex dex, SmaliOptions options) {
        return print(printer -> printer.print(dex), options);
    }

    /**
     * Prints one class block with the default options; the result re-parses
     * with {@link #parseClass(String)} to an equal class definition for
     * models the library itself produces.
     */
    public static String printClass(ClassDef cls) {
        return printClass(cls, SmaliOptions.defaultOptions());
    }

    /**
     * Prints one class block with the given options. A round trip through
     * {@link #parseClass(String)} preserves the model when the method bodies
     * are in the canonical layout this library emits (payloads at the end
     * behind an alignment {@code nop}, debug items on instruction
     * boundaries, try ranges on instruction boundaries) and the text holds
     * no unprintable content; code the label grammar cannot express is
     * printed commented out with a {@code nop} and does not round-trip.
     */
    public static String printClass(ClassDef cls, SmaliOptions options) {
        return print(printer -> printer.print(cls), options);
    }

    /**
     * Prints one method block with the default options; the result
     * re-parses with {@link #parseMethod(String)} to an equal method
     * definition for models the library itself produces. Member references
     * always use the long form: a lone method has no enclosing class to
     * resolve a short reference against.
     */
    public static String printMethod(MethodDef method) {
        return printMethod(method, SmaliOptions.defaultOptions());
    }

    /**
     * Prints one method block with the given options; see
     * {@link #printMethod(MethodDef)} for the round-trip contract. The
     * {@link SmaliOptions#hasImplicitReferences()} option has no effect
     * here.
     */
    public static String printMethod(MethodDef method, SmaliOptions options) {
        return print(printer -> printer.print(method), options);
    }
}
