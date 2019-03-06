package de.tudresden.inf.st.bigraphs.core.model2;

import de.tudresden.inf.st.bigraphs.core.Attributed;
import de.tudresden.inf.st.bigraphs.core.LinkType;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BLink;
import org.eclipse.emf.ecore.EObject;

public interface Link extends BLink, Attributed {
    LinkType getLinkType();
}
