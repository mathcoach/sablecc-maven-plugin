package de.htwsaar.mathcoach.scc;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.maven.project.MavenProjectHelper;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProject;
import org.sablecc.sablecc.SableCCApi;
import org.sablecc.sablecc.SableCCApiException;

/**
 * Call SableCC to generate Java file from ObjectMacro file.
 *
 * @author Hong Phuc Bui
 * @version 2.0-SNAPSHOT
 *
 * @phase generate-resources
 */
@Mojo(name = "sablecc", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = false)
public final class SableccCaller extends AbstractMojo {

    /**
     * where to write the generated parser. Default:
     * {@code ${basedir}/target/generated-sources/sablecc}
     */
    @Parameter(defaultValue = "${basedir}/target/generated-sources/sablecc")
    private String destination;
    @Parameter(defaultValue = "false")
    private boolean noInline;
    @Parameter(defaultValue = "20")
    private int inlineMaxAlts;
    @Parameter(required = false, defaultValue = "")
    private String outputPackage;
    @Parameter(required = true)
    private String grammar;
    @Parameter(defaultValue = "${component.org.apache.maven.project.MavenProjectHelper}")
    private MavenProjectHelper projectHelper;
    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${basedir}/src/main/sablecc")
    private String sableccDirPath;


    @Override
    public void execute() throws MojoFailureException {
        if (projectHelper == null) {
            projectHelper = new DefaultMavenProjectHelper();
        }
        if (project == null) {
            getLog().warn("project is null");
        }
        if (noInline) {// this warning will be removed when I can set this option
            getLog().warn("--no-inline is set by default to TRUE !!!!!!!!!!!");
        }

        try {
            // TODO: because the method SableCC.main(String[] argv)
            // does not throw any exception to tell/signal the Client
            // but just calls System.exit(1) for any error, I can not
            // use these method to compile the grammar file. Therefore
            // these options don't take any effect:
            // --no-inline
            // --inline-max-alts

            // check and normalize path to grammar file
            ArgumentVerifier arg = new ArgumentVerifier();
            final File grammarFile = guessSableCCFile(grammar);
            final Path validedGrammarPath = arg.verifyGrammarPath(grammarFile.getAbsolutePath());
            // check and normalize path to destiantion
            File destinateDir = new File(destination);
            if (!destinateDir.isAbsolute()) {
                destinateDir = new File(project.getBasedir(), destination);
            }
            final Path validedDirPath = arg.verifyDestinationPath(destinateDir.getAbsolutePath());
            compileGrammar(validedGrammarPath, validedDirPath);

            updateProjectFiles(project, validedDirPath);
        } catch (SableCCApiException ex) {
            getLog().error("Cannot compile the file " + grammar);
            getLog().error(ex.getMessage());
            throw new MojoFailureException("Cannot compile the file " + grammar, ex);
        }

    }

    private void compileGrammar(Path grammar, Path destination) {
        if (neeedCompile(grammar, destination)) {
            getLog().debug("Need to compile grammar " + this.grammar);
            SableCCApi sablecc = new SableCCApi(true, false);
            sablecc.processGrammar(grammar.toFile(), destination.toFile());
        } else {
            getLog().info("Not need to compile " + this.grammar);
            getLog().info("Clean output directory to force re-compile the grammar file " + this.grammar);
        }
    }

    private void updateProjectFiles(MavenProject project, Path destinatioin) {
        final List resourcePattern = Collections.singletonList("**/**.dat");
        Path projectBase = project.getBasedir().toPath();
        String parserDirectoryName = projectBase.relativize(destinatioin).toString();

        getLog().info("Add resource with pattern `" + resourcePattern + "` from " + parserDirectoryName);
        projectHelper.addResource(project, parserDirectoryName,
            resourcePattern, Collections.EMPTY_LIST);
        getLog().info("Add generated source root " + parserDirectoryName);
        project.addCompileSourceRoot(parserDirectoryName);
    }

    private File guessSableCCFile(String grammarConfigParam) {
        if (grammarConfigParam.contains(File.separator)) {
            File grammarFile = new File(grammarConfigParam);
            if (!grammarFile.isAbsolute()) {
                grammarFile = new File(project.getBasedir(), grammarConfigParam);
            }
            return grammarFile;
        } else {
            File sableccDir = new File(sableccDirPath);
            File grammarFile = new File(sableccDir, grammarConfigParam);
            return grammarFile;
        }
    }

    private boolean neeedCompile(Path grammar, Path destination) {
        if (outputPackage == null || outputPackage.trim().length() == 0) {
            getLog().info("No output package given or the given outputPackage is an empty string");
            getLog().info(" Cannot calculate timestamp");
            getLog().info(" Grammar will be recompiled");
            return true;
        } else {
            Path generatedParserPath = destination.resolve(outputPackage.replace(".", File.pathSeparator))
                .resolve("parser").resolve("Parser.java");
            getLog().debug("Check time stamp for the file:" + generatedParserPath);
            File parserFile = generatedParserPath.toFile();
            if (parserFile.isFile()) {// if the parser file exists
                long parserLastModi = parserFile.lastModified();
                getLog().debug("*********************** Last modi time of parser:" + parserLastModi);
                File grammarFile = grammar.toFile();
                long grammarLastModi = grammarFile.lastModified();
                getLog().debug("*********************** Last modi time of grammar:" + grammarLastModi);
                // the grammar file older than the parser file
                return grammarLastModi > parserLastModi;
            } else {
                return true;
            }

        }
    }
}
