package de.tudresden.inf.st.bigraphs.core.exceptions;

public class SignatureValidationFailedException extends RuntimeException {

    public SignatureValidationFailedException() {
        super("Signature validation failed because the model seems invalid.");
    }

    public SignatureValidationFailedException(String message) {
        super(message);
    }

    public SignatureValidationFailedException(Throwable throwable) {
        super(throwable);
    }
}
