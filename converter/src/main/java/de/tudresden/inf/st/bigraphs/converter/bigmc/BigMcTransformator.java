package de.tudresden.inf.st.bigraphs.converter.bigmc;

import de.tudresden.inf.st.bigraphs.converter.ReactiveSystemPrettyPrinter;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementation of {@link ReactiveSystemPrettyPrinter} for the transformation of pure EMOF-based bigraphs to
 * BigMC's term language. The whole bigraphical reactive system is encoded.
 * <p>
 * Code is mostly adapted from the library jLibBig to our EMOF-based bigraph structure.
 *
 * @author Dominik Grzelak
 */
public class BigMcTransformator implements ReactiveSystemPrettyPrinter<PureBigraph, PureReactiveSystem> {

    public static final String LINE_SEP = System.getProperty("line.separator");

    public BigMcTransformator() {
    }

    @Override
    public String toString(PureReactiveSystem system) {
        StringBuilder s = new StringBuilder();

        s.append(toString(system.getSignature()));

        s.append(LINE_SEP);

        for (BigraphEntity.OuterName eachOuter : system.getAgent().getOuterNames())
            s.append("%name ").append(eachOuter.getName()).append(";").append(LINE_SEP);

        s.append(LINE_SEP);

        s.append(toString(system.getReactionRules()));

        s.append(LINE_SEP);

        s.append(toStringForAgent(system.getAgent())).append(" ;").append(LINE_SEP);

        s.append(LINE_SEP);
        s.append("%check");
        s.append(LINE_SEP);

        return s.toString();
    }

    private String toString(Collection<ReactionRule<PureBigraph>> reactionRules) {
        StringBuilder s = new StringBuilder();
        for (ReactionRule<PureBigraph> reaction : reactionRules) {
            s.append(toString(reaction)).append(";").append(LINE_SEP);
        }
        return s.toString();
    }

    private String toString(ReactionRule<PureBigraph> reaction) {
        return toString(reaction.getRedex()) + " -> " + toString(reaction.getReactum());
    }

    private String toString(PureBigraph bigraph) {
        StringBuilder s = new StringBuilder();
        Iterator<BigraphEntity.RootEntity> iterator = bigraph.getRoots().iterator();
        while (iterator.hasNext()) {
            BigraphEntity.RootEntity next = iterator.next();
            Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(next);
            if (!childrenOf.isEmpty()) {
                Iterator<BigraphEntity<?>> childIterator = childrenOf.iterator();
                while (childIterator.hasNext()) {
                    String s1 = toString(bigraph, childIterator.next(), bigraph.getOuterNames(), bigraph.getSites());
                    s.append(s1).append(childIterator.hasNext() ? " | " : "");
                }
            } else {
                s.append("nil");
            }
            s.append(iterator.hasNext() ? " || " : "");
        }
        return s.toString();
    }

    private String toStringForAgent(PureBigraph bigraph) {
        StringBuilder s = new StringBuilder("");
        Iterator<BigraphEntity.RootEntity> iterator = bigraph.getRoots().iterator();
        if (!iterator.hasNext()) return s.toString();
        BigraphEntity.RootEntity next = iterator.next();
        Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(next);
        if (!childrenOf.isEmpty()) {
            Iterator<BigraphEntity<?>> childIterator = childrenOf.iterator();
            while (childIterator.hasNext()) {
                String s1 = toString(bigraph, childIterator.next(), bigraph.getOuterNames(), bigraph.getSites());
                s.append(s1).append(childIterator.hasNext() ? " | " : "");
            }
        } else {
            s.append("nil");
        }

        return s.toString();
    }

    private String toString(PureBigraph bigraph, BigraphEntity d, Collection<BigraphEntity.OuterName> collection, Collection<BigraphEntity.SiteEntity> sitelist) {
        StringBuilder s = new StringBuilder();
        if (BigraphEntityType.isSite(d)) {
            s.append("$").append(((BigraphEntity.SiteEntity) d).getIndex());
        } else {
            s.append(d.getControl().getNamedType().stringValue());
            StringBuilder ns = new StringBuilder();
            int unlinked = 0;
            Iterator<BigraphEntity.Port> portIt = bigraph.getPorts(d).iterator();
            while (portIt.hasNext()) {
                BigraphEntity.Port next = portIt.next();
                BigraphEntity link = bigraph.getLinkOfPoint(next);
                if (BigraphEntityType.isOuterName(link) && collection.contains(link)) {
                    for (int i = 0; i < unlinked; ++i) {
                        ns.append(" - , ");
                    }
                    String name = ((BigraphEntity.OuterName) link).getName();
                    ns.append(" ").append(name).append(" , ");
                } else {
                    ++unlinked;
                }
            }

            if (ns.length() > 0) {
                s.append("[").append(ns.substring(0, ns.length() - 2)).append("]");
            }
            Collection<BigraphEntity<?>> children = bigraph.getChildrenOf(d);
            if (!children.isEmpty()) {
                s.append(".");
                if (children.size() > 1) s.append("( ");

                Iterator<BigraphEntity<?>> childIt = children.iterator();
                while (childIt.hasNext()) {
                    s.append(toString(bigraph, childIt.next(), collection, sitelist))
                            .append(childIt.hasNext() ? " | " : "");
                }

                if (children.size() > 1) s.append(" )");
            }


        }
        return s.toString();
    }

    @Override
    public void toOutputStream(PureReactiveSystem system, OutputStream outputStream) throws IOException {
        String s = toString(system);
        outputStream.write(s.getBytes(), 0, s.length());
    }

    public static String toString(Signature sig) {
        StringBuilder s = new StringBuilder();
        Set<Control<?, ?>> controls = sig.getControls();
        for (Control ctrl : controls) {
            s.append(ctrl.getControlKind().equals(ControlStatus.ACTIVE) ? "%active " : "%passive ")
                    .append(ctrl.getNamedType().stringValue()).append(" : ")
                    .append(ctrl.getArity().getValue()).append(";").append(LINE_SEP);
        }
        return s.toString();
    }
}
