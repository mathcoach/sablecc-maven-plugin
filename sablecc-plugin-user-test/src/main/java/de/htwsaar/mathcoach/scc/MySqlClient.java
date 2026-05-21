package de.htwsaar.mathcoach.scc;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import mysqlgrm.parser.Parser;
import mysqlgrm.lexer.Lexer;
import mysqlgrm.lexer.LexerException;
import mysqlgrm.parser.ParserException;

/**
 *
 * @author hbui
 */
public final class MySqlClient {
    public MySqlClient() {
        PushbackReader r = new PushbackReader(new StringReader("Weird!!!"));
        Parser p = new Parser(new Lexer(r));
        try {
            p.parse();
        } catch (LexerException | ParserException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
