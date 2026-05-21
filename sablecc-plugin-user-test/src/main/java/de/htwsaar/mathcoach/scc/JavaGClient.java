package de.htwsaar.mathcoach.scc;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import xx.yy.zz.lexer.Lexer;
import xx.yy.zz.lexer.LexerException;
import xx.yy.zz.parser.Parser;
import xx.yy.zz.parser.ParserException;

/**
 *
 * @author hbui
 */
public final class JavaGClient {

    public JavaGClient() {
        PushbackReader r = new PushbackReader(new StringReader("Weird!!!"));
        Parser p = new Parser(new Lexer(r));
        try {
            p.parse();
        } catch (LexerException | ParserException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
