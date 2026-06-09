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

    private final String message;

    public SableCCGrammarException(ParserException cause) {
        super(cause);
        message = null;
    }

    public SableCCGrammarException(LexerException cause) {
        super(cause);
        message = null;
    }

    /**
     * only for use in conjunction with {@code GenLexer} or with
     * {@code GenParser}.
     */
    SableCCGrammarException(RuntimeException cause) {
        super(cause);
        String m = cause.getMessage();
        if (m != null ){
            message = "\n" + m;
        }else {
            message = null;
        }
    }
    
    @Override
    public String getMessage() {
        if(message != null) return message;
        else return super.getMessage();
    }
}
