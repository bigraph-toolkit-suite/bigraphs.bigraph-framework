package de.tudresden.inf.st.bigraphs.core.generators;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generates random bigraphs.
 *
 * @author Dominik Grzelak
 */
public abstract class RandomBigraphGeneratorSupport {
    SecureRandom rnd;
    LinkStrategy linkStrategy;

    public enum LinkStrategy {
        MAXIMAL_DEGREE_ASSORTATIVE, MAXIMAL_DEGREE_DISASSORTATIVE, MIN_LINKING, NONE;
    }

    public RandomBigraphGeneratorSupport() {
        this(LinkStrategy.MIN_LINKING);
    }

    public RandomBigraphGeneratorSupport(LinkStrategy linkStrategy) {
        this.linkStrategy = linkStrategy;
        this.rnd = new SecureRandom();
    }

    double[] stats;

    public LinkStrategy getLinkStrategy() {
        return linkStrategy;
    }

    public RandomBigraphGeneratorSupport setLinkStrategy(LinkStrategy linkStrategy) {
        this.linkStrategy = linkStrategy;
        return this;
    }


    // function to split a list into two sublists in Java
    //TODO try with eclipse collections
    protected static List<BigraphEntity>[] split(List<BigraphEntity> list) {
        int size = list.size();
        List<BigraphEntity> first = new ArrayList<>(list.subList(0, (size) / 2));
        List<BigraphEntity> second = new ArrayList<>(list.subList((size) / 2, size));
        return new List[]{first, second};
    }

    public int degreeOf(BigraphEntity nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            cnt += childs.size();
        }
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            cnt++;
        }
        return cnt;
    }

    public double[] getStats() {
        return stats;
    }

    protected void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance());
    }

    protected Supplier<Control> provideControlSupplier(Signature<? extends Control> signature) {
        return new Supplier<Control>() {
            private List<Control> controls = new ArrayList<>(signature.getControls());
//            private final Random controlRnd = new Random();

            @Override
            public Control get() {
                return controls.get(rnd.nextInt(controls.size()));
            }
        };
    }

    protected Supplier<String> vertexLabelSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "v" + id++;
            }
        };
    }

    protected Supplier<String> edgeLabelSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "e" + id++;
            }
        };
    }
}
