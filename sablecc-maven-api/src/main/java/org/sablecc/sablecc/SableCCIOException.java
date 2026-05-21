package org.sablecc.sablecc;

import java.io.IOException;

/**
 * represents error while reading grammar stream or writing generated lexer and
 * parser.
 *
 * @author hbui
 */
public final class SableCCIOException extends SableCCApiException {

    public SableCCIOException(String message) {
        super(message);
    }

    public SableCCIOException(IOException cause) {
        super(cause);
    }
}
