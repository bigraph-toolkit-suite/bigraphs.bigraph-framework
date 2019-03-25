package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

//TODO builder instanzieren
public class BigraphCompositor<S extends Signature> implements BigraphOperations<S> {

    private BigraphBuilder<?> builder;

    public BigraphCompositor() {
    }

    public void setBuilder(BigraphBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Bigraph<S> compose(Bigraph<S> g, Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        assertSignaturesAreSame(g.getSignature(), f.getSignature());
        this.builder.useSignature(g.getSignature());
//        assertInterfaceCompatibleForCompose(left, right);

        Collection<BigraphEntity> V_G = new ArrayList<>(g.getNodes());
        V_G.addAll(g.getRoots());
        V_G.addAll(g.getSites());

        Collection<BigraphEntity> V_F = new ArrayList<>(f.getNodes());
        V_F.addAll(f.getRoots());
        V_F.addAll(f.getSites());

        Collection<BigraphEntity.SiteEntity> k = f.getSites();
        Collection<BigraphEntity.SiteEntity> m = g.getSites();
        //check disjoint nodes
        Set<BigraphEntity> W_set = new LinkedHashSet<>();
        W_set.addAll(V_G);
        W_set.addAll(V_F);
        W_set.addAll(k);
        W_set.removeAll(f.getRoots());
        W_set.removeAll(g.getRoots());

        Set<BigraphEntity> kVF_Set = new LinkedHashSet<>();
        kVF_Set.addAll(V_F);
        kVF_Set.addAll(k);

        for (BigraphEntity w : W_set) {
            if (w.getType().equals(BigraphEntityType.ROOT)) continue;
            BigraphEntity case1 = f.getParent(w);
            boolean contains = false;
            if (case1 != null)
                contains = m.contains(case1);//TODO: check here
            if (case1 != null && kVF_Set.contains(w) && V_F.contains(case1)) {
                setParentOfNode(w, case1);
            } else if (case1 != null && kVF_Set.contains(w) && contains) {
                BigraphEntity j = null;
                for (BigraphEntity eachJ : m) {
                    if (eachJ.equals(case1)) j = case1;
                }
                assert j != null;
                setParentOfNode(w, g.getParent(j));
            } else if (V_G.contains(w)) {
                setParentOfNode(w, g.getParent(w));
            }
        }

        return null;
    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature childRef = parent.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        EStructuralFeature prntRef = parent.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);

        node.getInstance().eSet(prntRef, parent.getInstance());
        ((EList) parent.getInstance().eGet(childRef)).add(node.getInstance());
    }

    protected void assertSignaturesAreSame(S signature1, S signature2) throws IncompatibleSignatureException {
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
        }
    }

    protected void assertInterfaceCompatibleForCompose(Bigraph<S> left, Bigraph<S> right) throws IncompatibleInterfaceException {
        //xs: left: innerNames
        //ys: right: outerNames

        //check place graph
        int mLeft = left.getSites().size();
        int mRight = right.getRoots().size();
        int nLeft = left.getInnerNames().size();
        int nRight = right.getOuterNames().size();
        boolean disjoint = Collections.disjoint(left.getInnerNames(), right.getOuterNames());
        if (disjoint || mLeft != mRight) {
            throw new IncompatibleInterfaceException();
        }
    }
}
