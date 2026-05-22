package de.htwsaar.mathcoach.scc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.PushbackReader;
import java.io.StringReader;
import mysqlgrm.lexer.Lexer;
import mysqlgrm.parser.Parser;

public class AppTest {

    @Test
    void testMySQLParser() {

        assertDoesNotThrow(() -> {
            Parser p = new Parser(new Lexer(new PushbackReader(new StringReader("truncate table xxx;"))));
            p.parse();
        }, "Es wurde keine Exception erwartet beim Parsen des TRUNCATE-Statements.");
    }
}
