package org.sablecc.sablecc;

import org.sablecc.sablecc.lexer.LexerException;
import org.sablecc.sablecc.parser.ParserException;

/**
 * Represents errors which happen during the process of gerenrating parser.
 *
 *
 * @author hbui
 */
public final class SableCCGrammarException extends SableCCApiException {

    public SableCCGrammarException(ParserException cause) {
        super(cause);
    }

    public SableCCGrammarException(LexerException cause) {
        super(cause);
    }

    /**
     * only for use in conjunction with {@code GenLexer} or with
     * {@code GenParser}.
     */
    SableCCGrammarException(RuntimeException cause) {
        super(cause);
    }

}
