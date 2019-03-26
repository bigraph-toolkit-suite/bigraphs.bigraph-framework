package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.factory.SimpleBigraphFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

//TODO originalen bigraph type class zurückgeben falls user casten sicher will
public class DefaultBigraphCompositor<S extends Signature> extends BigraphDelegator<S> implements BigraphComposition<S> {

    private final BigraphBuilder<S> builder;

    public DefaultBigraphCompositor(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
        // is safe, S is inferred from the bigraph too and the same as the builder S (they will have the same type thus)
        this.builder = new SimpleBigraphFactory().createBigraphBuilder(getBigraphDelegate().getSignature());
    }

    @Override
    public Bigraph<S> compose(Bigraph<S> f) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph<S> g = getBigraphDelegate();
        assertSignaturesAreSame(g.getSignature(), f.getSignature());
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

        Set<BigraphEntity> nodes = new HashSet<>(builder.createBigraph().getNodes());
        for (BigraphEntity w : W_set) {
            //gleiche entity erstellen und zur liste hinzufügen und für dieses das parent setzen
            BigraphEntity w0 = createNewObjectFrom(w); //TODO komplett neuerstellen
            BigraphEntity p;// = createNewObjectFrom(w);
//            nodes.add(w0);
            if (w.getType().equals(BigraphEntityType.ROOT)) continue;
//            nodes.add(w);
            BigraphEntity case1 = f.getParent(w);
            boolean contains = false;
            if (case1 != null)
                contains = m.contains(case1);//TODO: check here
            if (case1 != null && kVF_Set.contains(w) && V_F.contains(case1)) {
                p = createNewObjectFrom(case1);
                nodes.add(p);
                setParentOfNode(w0, p);
            } else if (case1 != null && kVF_Set.contains(w) && contains) {
                BigraphEntity j = null;
                for (BigraphEntity eachJ : m) {
                    if (eachJ.equals(case1)) j = case1;
                }
                assert j != null;
                p = createNewObjectFrom(g.getParent(j));
                nodes.add(p);
                setParentOfNode(w0, p);
            } else if (V_G.contains(w)) {
                p = createNewObjectFrom(g.getParent(w));
                nodes.add(p);
                setParentOfNode(w0, p);
            }

            //setParent hier mit komplett neu erstellen EObjects
        }

        return null;
    }

    private BigraphEntity<?> createNewObjectFrom(BigraphEntity<?> entity) {
        //copy constructor call?
        if (entity.getType().equals(BigraphEntityType.NODE)) {
            return BigraphEntity.createNode(entity.getInstance(), entity.getControl());
        } else {
            return BigraphEntity.create(entity.getInstance(), entity.getClass());
        }
        // if isNode
        //BigraphEntity(@NonNull EObject instance, C control, BigraphEntityType type) casten und übergeben
        //else: BigraphEntity(@NonNull EObject instance, BigraphEntityType type) übergeben
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
