package com.v7878.dex.smali;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Converts an ANTLR syntax error into a {@link SmaliSyntaxException}.
 *
 * <p>Stateless: one instance is shared by every parse.
 */
final class SmaliErrorListener extends BaseErrorListener {
    static final SmaliErrorListener INSTANCE = new SmaliErrorListener();

    private SmaliErrorListener() {
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException e) {
        throw new SmaliSyntaxException(line, charPositionInLine + 1, msg, e);
    }
}
