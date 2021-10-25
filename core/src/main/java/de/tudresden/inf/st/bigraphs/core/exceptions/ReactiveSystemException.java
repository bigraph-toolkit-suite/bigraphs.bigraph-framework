package de.tudresden.inf.st.bigraphs.core.exceptions;

public abstract class ReactiveSystemException extends Exception {

    public ReactiveSystemException() {
    }

    public ReactiveSystemException(String message) {
        super(message);
    }

    public ReactiveSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReactiveSystemException(Throwable cause) {
        super(cause);
    }

    public ReactiveSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
