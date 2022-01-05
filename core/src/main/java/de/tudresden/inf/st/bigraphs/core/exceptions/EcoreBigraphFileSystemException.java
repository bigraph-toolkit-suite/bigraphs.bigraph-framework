package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * Indicates any error that happens during a bigraph model file operation.
 *
 * @author Dominik Grzelak
 */
public class EcoreBigraphFileSystemException extends Exception {

    public EcoreBigraphFileSystemException() {
    }

    public EcoreBigraphFileSystemException(String message) {
        super(message);
    }

    public EcoreBigraphFileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public EcoreBigraphFileSystemException(Throwable cause) {
        super(cause);
    }

    public EcoreBigraphFileSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
