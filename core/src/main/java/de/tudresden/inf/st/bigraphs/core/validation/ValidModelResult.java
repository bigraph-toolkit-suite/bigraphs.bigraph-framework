package de.tudresden.inf.st.bigraphs.core.validation;

import de.tudresden.inf.st.bigraphs.core.EcoreSignature;

public class ValidModelResult implements BModelValidationResult{
    Class<? extends EcoreSignature> clazz;

    public ValidModelResult(Class<? extends EcoreSignature> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends EcoreSignature> getModelClass() {
        return clazz;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
