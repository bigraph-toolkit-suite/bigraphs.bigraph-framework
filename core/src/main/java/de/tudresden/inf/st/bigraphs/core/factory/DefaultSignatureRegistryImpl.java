package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Signature;
import org.eclipse.emf.ecore.EPackage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dominik Grzelak
 */
public class DefaultSignatureRegistryImpl extends ConcurrentHashMap<Signature, EPackage> implements BigraphFactory.Registry {

    @Override
    public EPackage getEPackage(Signature signature) {
        if (signature != null) {
            EPackage ePackage = get(signature);
            return ePackage;
        }
        return null;
    }
}