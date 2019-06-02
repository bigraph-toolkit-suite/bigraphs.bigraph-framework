package de.tudresden.inf.st.bigraphs.matching.impl;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//Agent bigraph muss edges in vertex set mitaufnehmen und als identischen vertex child adden an alle verlinkten
//welcher name das ist, ist egal, muss nur gleich sein.
//Redex muss outername als link vertex child adden, wenn das gematcht werden soll die verlinkung
//+ sites und roots müssen übereinstimmen und werden in vertex set und als vertex childs aufgenommen

// In dieser Variante werden die links erst einmal nicht mit aufgenommen
public class EcoreBigraphAgentAdapter extends AbstractDynamicMatchAdapter {

    public EcoreBigraphAgentAdapter(PureBigraph bigraph) {
        super(bigraph);
        //TODO: assert: has only one root! only forest in tree matching
    }

    /**
     * includes also edges+outernames
     *
     * @param node
     * @return
     */
    public List<AbstractDynamicMatchAdapter.ControlLinkPair> getLinksOfNode(BigraphEntity node) {
        EObject instance = node.getInstance();
        List<AbstractDynamicMatchAdapter.ControlLinkPair> children = new ArrayList<>();

        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.nonNull(portRef)) {
            EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
            for (EObject eachPort : portList) {
                //bPoints: for links
                EStructuralFeature linkRef = eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
                if (Objects.nonNull(linkRef) && Objects.nonNull(eachPort.eGet(linkRef))) {
                    EObject obj = (EObject) eachPort.eGet(linkRef);
                    if (isOuterName(obj)) {
                        children.add(new ControlLinkPair(node.getControl(), BigraphEntity.create(obj, BigraphEntity.OuterName.class)));
                    } else if (isBEdge(obj)) {
                        children.add(new ControlLinkPair(node.getControl(), BigraphEntity.create(obj, BigraphEntity.Edge.class)));
                    }
                }
            }

        }
        return children;
    }
}
