
package de.htwsaar.mathcoach.scc;

import java.io.File;
import java.nio.file.Path;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Strategy Pattern could be uesed here to make the check of valid grammar
 * file and destination directory more scalable and finer, but it is maybe
 * overkill here.
 *
 * @author Hong-Phuc Bui
 * @version Feb 23, 2013
 */
public final class ArgumentVerifier {

    public Path verifyGrammarPath(String grammar) throws MojoFailureException {
		File grammarFile = new File(grammar);
		if (!grammarFile.exists()){
            throw new MojoFailureException("The grammar file " + grammar + " does not exist");
		}

		if (grammarFile.isDirectory()){
            throw new MojoFailureException("The path " + grammar + " is a directory");
		}

        return grammarFile.toPath().toAbsolutePath().normalize();
	}

    public Path verifyDestinationPath(String dir) throws MojoFailureException {
		File destinationDir = new File(dir);
		if (!destinationDir.exists()){
			if (!destinationDir.mkdirs()){
                throw new RuntimeException("Cannot make destination directory " + dir);
			}
		}else if (destinationDir.isFile()){
            throw new MojoFailureException("The destination path " + dir + " is a file");
		}
        String destination = destinationDir.getAbsolutePath();
		if (destination==null){
            throw new MojoFailureException("The destination directory name is null");
		}
        return destinationDir.toPath().toAbsolutePath().normalize();
	}
}
