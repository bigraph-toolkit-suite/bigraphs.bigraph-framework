package de.tudresden.inf.st.bigraphs.matching;

import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

//Agent bigraph muss edges in vertex set mitaufnehmen und als identischen vertex child adden an alle verlinkten
//welcher name das ist, ist egal, muss nur gleich sein.
//Redex muss outername als link vertex child adden, wenn das gematcht werden soll die verlinkung
//+ sites und roots müssen übereinstimmen und werden in vertex set und als vertex childs aufgenommen

// In dieser Variante werden die links erst einmal nicht mit aufgenommen
public class EcoreBigraphAgentAdapter extends AbstractMatchAdapter {

    public EcoreBigraphAgentAdapter(DynamicEcoreBigraph bigraph) {
        super(bigraph);
    }

}
