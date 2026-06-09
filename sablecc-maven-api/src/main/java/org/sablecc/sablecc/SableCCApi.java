package org.sablecc.sablecc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;
import org.sablecc.sablecc.lexer.Lexer;
import org.sablecc.sablecc.lexer.LexerException;
import org.sablecc.sablecc.node.AGrammar;
import org.sablecc.sablecc.node.Start;
import org.sablecc.sablecc.parser.Parser;
import org.sablecc.sablecc.parser.ParserException;

/**
 * this class must be placed in <code>org.sablecc.sablecc</code> to access the
 * field * <code>SableCC.inliningMaxAlts</code>. Do not move it elsewhere!
 *
 * @author hbui
 */
public final class SableCCApi {

    private final boolean processInlining;
    private final boolean prettyPrinting;
    private final int inliningMaxAlts;

    /**
     * Setup SableCC CLI arguments.
     *
     * Either
     * <code>sablecc -d {destination} --no-inline [--pretty-print] {filename}</code>
     * or
     * <code>sablecc -d {destination} --inline-max-alts {number} [--pretty-print] {filename}</code>
     *
     * @param processInlining
     * @param prettyPrinting
     */
    public SableCCApi(boolean processInlining, boolean prettyPrinting) {        
        this.processInlining = processInlining;
        this.prettyPrinting = prettyPrinting;
        this.inliningMaxAlts = SableCC.inliningMaxAlts;
    }

    /**
     * Setup SableCC CLI arguments.
     *
     * <code>sablecc -d {destination} --inline-max-alts {number} [--pretty-print] {filename}</code>
     *
     * @param inliningMaxAlts setups CLI-Option <code>--inline-max-alts</code>,
     * also sets explicitly <code>--no--inline</code> to <code>false</code>.
     * {@code inliningMaxAlts} must be a positive integer.
     *
     * @param prettyPrinting setups CLI-Option <code>--pretty-print</code>
     *
     *
     */
    public SableCCApi(int inliningMaxAlts, boolean prettyPrinting) {
        if (inliningMaxAlts <= 0) {
            throw new SableCCApiException("ERROR: parameter inliningMaxAlts must be a positive integer.");
        }
        this.processInlining = true;
        this.prettyPrinting = prettyPrinting;
        this.inliningMaxAlts = inliningMaxAlts;
    }

    /**
     * processes a grammar file as if
     * {@link org.sablecc.sablecc.SableCC#processGrammar(java.io.File, java.io.File)}
     * are called. Generated lexer and parser are written into
     * {@code  destination}. The origin {@link System.out} is replaced by
     * {@code out}.
     *
     * @param grammarFile the grammar file, must exist. This method does not
     * check if the grammar file exists.
     *
     * @param destination the destination directory, must exists. This method
     * does not check if the destination directory exits.
     *
     * @param out
     */
    public void processGrammar(File grammarFile, File destination, PrintStream out) {
        // temporary rewrite System.out to out
        PrintStream originOut = System.out;
        PrintStream originErr = System.err;
        final int originInlineMaxAlts = SableCC.inliningMaxAlts;
        try {
            System.setOut(out);
            System.setErr(out);

            SableCC.inliningMaxAlts = inliningMaxAlts;
            genrateGrammar(grammarFile, destination);
        } catch (IOException ex) {
            throw new SableCCIOException(ex);
        } catch (LexerException ex) {
            throw new SableCCGrammarException(ex);
        } catch (ParserException ex) {
            throw new SableCCGrammarException(ex);
        } catch(RuntimeException ex) {            
            throw new SableCCGrammarException(ex);
        } finally {
            SableCC.inliningMaxAlts = originInlineMaxAlts;
            System.setOut(originOut);
            System.setErr(originErr);
        }
    }

    /**
     * processes a grammar file as if
     * {@link org.sablecc.sablecc.SableCC#processGrammar(java.io.File, java.io.File)}
     * are called.
     *
     * @param grammarFile the grammar file, must exist. This method does not
     * check if the grammar file exists.
     *
     * @param destination the destination directory, must exists. This method
     * does not check if the destination directory exits.
     *
     */    
    public void processGrammar(File grammarFile, File destination) {
        processGrammar(grammarFile, destination, System.out);
        /*
        final int originInlineMaxAlts = SableCC.inliningMaxAlts;
        try {
            SableCC.inliningMaxAlts = inliningMaxAlts;
            genrateGrammar(grammarFile, destination);
        } catch (IOException ex) {
            throw new SableCCIOException(ex);
        } catch (LexerException ex) {
            throw new SableCCGrammarException(ex);
        } catch (ParserException ex) {
            throw new SableCCGrammarException(ex);
        } catch(RuntimeException ex) {            
            throw new SableCCGrammarException(ex);
        } finally {
            SableCC.inliningMaxAlts = originInlineMaxAlts;
        }
        */
    }

    private void genrateGrammar(File in, File dir) throws IOException, ParserException, LexerException {
        // re-initialize all static structures in the engine
        LR0Collection.reinit();
        Symbol.reinit();
        Production.reinit();
        Grammar.reinit();

        System.out.println("\n -- Generating parser for " + in.getName() + " in " + dir.getPath());

        // Build the AST
        Start tree;
        try ( FileReader temp = new FileReader(in)) {
            tree = new Parser(new Lexer(new PushbackReader(temp, 1000))).parse();
        }


        boolean hasTransformations = false;

        if (((AGrammar) tree.getPGrammar()).getAst() == null) {
            System.out.println("Adding productions and alternative of section AST.");
            //AddAstProductions astProductions = new AddAstProductions();
            tree.apply(new AddAstProductions());
        } else {
            hasTransformations = true;
        }

        System.out.println("Verifying identifiers.");
        ResolveIds ids = new ResolveIds(dir);
        tree.apply(ids);

        System.out.println("Verifying ast identifiers.");
        ResolveAstIds ast_ids = new ResolveAstIds(ids);
        tree.apply(ast_ids);

        System.out.println("Adding empty productions and empty alternative transformation if necessary.");
        tree.apply(new AddEventualEmptyTransformationToProductions(ids, ast_ids));

        System.out.println("Adding productions and alternative transformation if necessary.");
        AddProdTransformAndAltTransform adds = new AddProdTransformAndAltTransform();
        tree.apply(adds);
        /*
    System.out.println("Replacing AST + operator by * and removing ? operator if necessary");
    tree.apply( new AstTransformations() );
         */
        System.out.println("computing alternative symbol table identifiers.");
        ResolveAltIds alt_ids = new ResolveAltIds(ids);
        tree.apply(alt_ids);

        System.out.println("Verifying production transform identifiers.");
        ResolveProdTransformIds ptransform_ids = new ResolveProdTransformIds(ast_ids);
        tree.apply(ptransform_ids);

        System.out.println("Verifying ast alternatives transform identifiers.");
        ResolveTransformIds transform_ids = new ResolveTransformIds(ast_ids, alt_ids, ptransform_ids);
        tree.apply(transform_ids);

        System.out.println("Generating token classes.");
        tree.apply(new GenTokens(ids));

        System.out.println("Generating production classes.");
        tree.apply(new GenProds(ast_ids));

        System.out.println("Generating alternative classes.");
        tree.apply(new GenAlts(ast_ids));

        System.out.println("Generating analysis classes.");
        tree.apply(new GenAnalyses(ast_ids));

        System.out.println("Generating utility classes.");
        tree.apply(new GenUtils(ast_ids));

        try {
            System.out.println("Generating the lexer.");
            tree.apply(new GenLexer(ids));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw new SableCCGrammarException(e);
        }

        try {
            System.out.println("Generating the parser.");
            tree.apply(new GenParser(ids, alt_ids, transform_ids, ast_ids.getFirstAstProduction(), processInlining, prettyPrinting, hasTransformations));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw new SableCCGrammarException(e);
        }
    }

}
