package org.sablecc.sablecc;

/**
 * Represents all kinds of errors occurring during process a grammar file.
 *
 *
 * @author hbui
 */
public class SableCCApiException extends RuntimeException {

    public SableCCApiException(String message) {
        super(message);
    }

    SableCCApiException(Exception cause) {
        super(cause);
    }

}
