package de.tudresden.inf.st.bigraphs.core.validation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InvalidModelResult implements BModelValidationResult {

    List<Exception> ex;

    public InvalidModelResult(Exception e) {
        this(Collections.singletonList(e));
    }

    public InvalidModelResult(List<Exception> ex) {
        this.ex = new LinkedList<>(ex);
    }

    public List<Exception> getExceptions() {
        return ex;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
