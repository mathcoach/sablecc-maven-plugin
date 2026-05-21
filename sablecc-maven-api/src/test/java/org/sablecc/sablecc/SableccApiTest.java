package org.sablecc.sablecc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author hbui
 */
class SableccApiTest {

    Path createGrammar(String grammarResource) throws IOException {
        InputStream grammar = getClass().getClassLoader().getResourceAsStream(grammarResource);
        Path dir = Files.createTempDirectory("sablecc-grammar");
        Path grammarFile = dir.resolve(grammarResource);
        Files.copy(grammar, grammarFile, StandardCopyOption.REPLACE_EXISTING);
        return dir;
    }

    @Test
    void testGenerateGrammarWithStringPrinter() throws IOException {
        boolean processInlining = true;
        boolean prettyPrinting = false;
        final String grammarResource = "complex.scc";
        SableCCApi sapi = new SableCCApi(processInlining, prettyPrinting);
        Path grammarDir = createGrammar(grammarResource);
        Path grammar = grammarDir.resolve(grammarResource);

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(s);
        sapi.processGrammar(grammar.toFile(), grammarDir.toFile(), out);
        String stdOut = s.toString();
        assertThat(stdOut).contains(
            " -- Generating parser for ",
            "Generating the parser."
        );
        Path parserClass = grammarDir.resolve("xx/yy/zz/parser/Parser.java");
        assertThat(parserClass).isRegularFile();
    }

    @Test
    void testGenerateGrammar() throws IOException {
        boolean processInlining = true;
        boolean prettyPrinting = true;
        final String grammarResource = "MySQLGrammar.scc";
        SableCCApi sapi = new SableCCApi(processInlining, prettyPrinting);
        Path grammarDir = createGrammar(grammarResource);
        Path grammar = grammarDir.resolve(grammarResource);

        sapi.processGrammar(grammar.toFile(), grammarDir.toFile());

        Path parserClass = grammarDir.resolve("mysqlgrm/parser/");
        assertThat(parserClass).isEmptyDirectory();
    }

}