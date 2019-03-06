package de.tudresden.inf.st.bigraphs.core.model2;

import de.tudresden.inf.st.bigraphs.core.Attributed;
import de.tudresden.inf.st.bigraphs.core.PlaceType;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BPlace;
import org.eclipse.emf.ecore.EObject;

public interface Place extends BPlace, Attributed {
    PlaceType getPlaceType();

}
