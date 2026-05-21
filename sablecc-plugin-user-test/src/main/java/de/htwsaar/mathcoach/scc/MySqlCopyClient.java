package de.htwsaar.mathcoach.scc;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import mysqlgrm.copy.lexer.Lexer;
import mysqlgrm.copy.lexer.LexerException;
import mysqlgrm.copy.parser.Parser;
import mysqlgrm.copy.parser.ParserException;

/**
 *
 * @author hbui
 */
public class MySqlCopyClient {

    public MySqlCopyClient() {
        PushbackReader r = new PushbackReader(new StringReader("Weird!!!"));
        Parser p = new Parser(new Lexer(r));
        try {
            p.parse();
        } catch (LexerException | ParserException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
