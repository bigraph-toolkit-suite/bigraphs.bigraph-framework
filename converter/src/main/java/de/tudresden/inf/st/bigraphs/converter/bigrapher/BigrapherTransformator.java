package de.tudresden.inf.st.bigraphs.converter.bigrapher;

import de.tudresden.inf.st.bigraphs.converter.ReactiveSystemPrettyPrinter;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.ReactiveSystemPredicates;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of {@link ReactiveSystemPrettyPrinter} for the transformation of pure EMOF-based bigraphs to
 * bigraphER's specification language. The whole bigraphical reactive system is encoded.
 * Only one agent is printed.
 * <p>
 * Code is mostly adapted from the library jLibBig to our EMOF-based bigraph structure.
 *
 * @author Dominik Grzelak
 */
public class BigrapherTransformator implements ReactiveSystemPrettyPrinter<PureBigraph, PureReactiveSystem> {

    public static final String LINE_SEP = System.getProperty("line.separator");

    private List<String> reactionLabels = new ArrayList<>();
    private List<String> predicateLabels = new ArrayList<>();
    private List<String> randomLinkNames = new ArrayList<>();
    private int reactionRuleCounter = 0;
    private final String predicateVarPrefix = "pred";
    private int predicateVarCnt = 0;

    public BigrapherTransformator() {
    }

    private void reset() {
        reactionRuleCounter = 0;
        predicateVarCnt = 0;
        reactionLabels.clear();
        predicateLabels.clear();
        randomLinkNames.clear();
    }

    @Override
    public String toString(PureReactiveSystem system) {
        reset();
        StringBuilder s = new StringBuilder();

        s.append(toString(system.getSignature()));

        s.append(LINE_SEP);

        s.append(toString(system.getReactionRules()));

        s.append(LINE_SEP);

        s.append(toStringForAgent(system.getAgent())).append(" ;").append(LINE_SEP);

        s.append(LINE_SEP);

        boolean hasPredicates = system.getPredicates().size() > 0;
        if (hasPredicates) {
            s.append(toString(system.getPredicates()));
            s.append(LINE_SEP);
        }

        s.append("begin brs").append(LINE_SEP);
        s.append("\tinit ").append(getAgentVarName(system.getAgent())).append(";").append(LINE_SEP);
        String rrSet = String.join(",", reactionLabels).toString();
        s.append("\trules = [{").append(rrSet).append("}];").append(LINE_SEP);
        if (hasPredicates) {
            String predLabels = String.join(",", predicateLabels).toString();
            s.append("\tpreds = {").append(predLabels).append("};").append(LINE_SEP);
        }
        s.append("end");
        s.append(LINE_SEP);

        return s.toString();
    }

    private String toString(List<ReactiveSystemPredicates<PureBigraph>> predicates) {
        //TODO
        StringBuilder s = new StringBuilder();
        for (ReactiveSystemPredicates<PureBigraph> predicate : predicates) {
            s.append(toStringForAgent(predicate.getBigraph(), getPredicateVarName(predicate.getBigraph())))
                    .append(";").append(LINE_SEP);
        }
        return s.toString();
    }

    @Override
    public void toOutputStream(PureReactiveSystem system, OutputStream outputStream) throws IOException {
        reset();
        String s = toString(system);
        outputStream.write(s.getBytes(), 0, s.length());
    }

    private String toString(Collection<ReactionRule<PureBigraph>> reactionRules) {
        StringBuilder s = new StringBuilder();
        for (ReactionRule<PureBigraph> reaction : reactionRules) {
            s.append(toString(reaction)).append(";").append(LINE_SEP);
        }
        return s.toString();
    }

    private String toString(ReactionRule<PureBigraph> reaction) {
        StringBuilder sb = new StringBuilder("react ");
        String rrName = reaction.getClass().getSimpleName().toLowerCase() + (reactionRuleCounter++);
        reactionLabels.add(rrName);
        sb.append(rrName).append(" = ");
        String redex = toString(reaction.getRedex());
        redex = !randomLinkNames.isEmpty() ? closeFakeLinks(randomLinkNames) + " (" + redex + ")" : redex;
        randomLinkNames.clear();
        String reactum = toString(reaction.getReactum());
        reactum = !randomLinkNames.isEmpty() ? closeFakeLinks(randomLinkNames) + " (" + reactum + ")" : reactum;
        randomLinkNames.clear();
        sb.append(redex).append(" -> ").append(reactum);
        return sb.toString();
    }

    private String toString(PureBigraph bigraph) {
        StringBuilder s = new StringBuilder();
        Iterator<BigraphEntity.RootEntity> iterator = bigraph.getRoots().iterator();
        while (iterator.hasNext()) {
            s.append('(');
            BigraphEntity.RootEntity next = iterator.next();
            Collection<BigraphEntity> childrenOf = bigraph.getChildrenOf(next);
            if (!childrenOf.isEmpty()) {
                Iterator<BigraphEntity> childIterator = childrenOf.iterator();
                while (childIterator.hasNext()) {
                    List<BigraphEntity.Link> links = new ArrayList<>(bigraph.getEdges());
                    links.addAll(bigraph.getOuterNames());
                    String s1 = toString(bigraph, childIterator.next(), links, bigraph.getSites());
                    s.append(s1).append(childIterator.hasNext() ? " | " : "");
                }
            } else {
//                s.append("nil");
            }
            s.append(iterator.hasNext() ? ") || " : ")");
        }
        return s.toString();
    }

    private String getAgentVarName(PureBigraph bigraph) {
        return bigraph.getModelPackage().getName().toLowerCase();
    }

    private String getPredicateVarName(PureBigraph bigraph) {
        predicateLabels.add(predicateVarPrefix + (predicateVarCnt++));
        return predicateLabels.get(predicateLabels.size() - 1);
    }

    private String toStringForAgent(PureBigraph bigraph) {
        return toStringForAgent(bigraph, getAgentVarName(bigraph));
    }

    private String toStringForAgent(PureBigraph bigraph, String varName) {
        String varPrefix = "big " + varName + " = ";
        StringBuilder s = new StringBuilder("");
        Iterator<BigraphEntity.RootEntity> iterator = bigraph.getRoots().iterator();
        if (!iterator.hasNext()) return s.append(";").toString();
        BigraphEntity.RootEntity next = iterator.next();
        Collection<BigraphEntity> childrenOf = bigraph.getChildrenOf(next);
        if (!childrenOf.isEmpty()) {
            Iterator<BigraphEntity> childIterator = childrenOf.iterator();
            while (childIterator.hasNext()) {
                List<BigraphEntity.Link> links = new ArrayList<>(bigraph.getEdges());
                links.addAll(bigraph.getOuterNames());
                String s1 = toString(bigraph, childIterator.next(), links, bigraph.getSites());
                s.append(s1).append(childIterator.hasNext() ? " | " : "");
            }
        } else {
            s.append("1");
        }

        if (!randomLinkNames.isEmpty()) {
            StringBuilder s2 = new StringBuilder();
            String closeNames = closeFakeLinks(randomLinkNames);
            s2.append(varPrefix).append(closeNames).append(" (").append(s.toString()).append(")");
            randomLinkNames.clear();
            return s2.toString();
        } else {
            return s.insert(0, varPrefix).toString();
        }
    }

    private String toString(PureBigraph bigraph, BigraphEntity d, Collection<BigraphEntity.Link> collection, Collection<BigraphEntity.SiteEntity> sitelist) {
        StringBuilder s = new StringBuilder();
        if (BigraphEntityType.isSite(d)) {
            s.append("id(1)"); //.append(((BigraphEntity.SiteEntity) d).getIndex());
        } else {
            s.append(d.getControl().getNamedType().stringValue());
            StringBuilder ns = new StringBuilder();
            int unlinked = 0;
            int arity = d.getControl().getArity().getValue().intValue();
            for (int a = 0; a < arity; a++) { //portIt.hasNext()) {
                List<BigraphEntity.Port> portIt = new ArrayList<>(bigraph.getPorts(d));
                BigraphEntity link = null;
                boolean hasPort = (portIt.size() > 0 && a < portIt.size() && (portIt.get(a) != null) && (link = bigraph.getLinkOfPoint(portIt.get(a))) != null);
                if (hasPort && (BigraphEntityType.isOuterName(link) || BigraphEntityType.isEdge(link)) && collection.contains(link)) {
                    for (int i = 0; i < unlinked; ++i) {
                        ns.append("-, ");
                    }
                    String name = ((BigraphEntity.Link) link).getName();
                    ns.append(name).append(", ");
                } else {
                    ++unlinked;
                }
            }

            if (ns.length() > 0) {
                s.append("{").append(ns.substring(0, ns.length() - 2)).append("}");
            }
            // if no ports are connected but arity > 0: add fake name and close it later
            if (ns.length() == 0 && unlinked != 0) {
//                closeFakeNames = true;
                String s1 = IntStream.range(0, arity)
                        .mapToObj(x -> {
                            String r = generateFakeLinkName();
                            randomLinkNames.add(r);
                            return r;
                        })
                        .collect(Collectors.joining(","));
                s.append("{").append(s1).append("}");
            }
            Collection<BigraphEntity> children = bigraph.getChildrenOf(d);
            if (!children.isEmpty()) {
                s.append(".");
                if (children.size() > 1) s.append("( ");

                Iterator<BigraphEntity> childIt = children.iterator();
                while (childIt.hasNext()) {
                    s.append(toString(bigraph, childIt.next(), collection, sitelist))
                            .append(childIt.hasNext() ? " | " : "");
                }

            } else {
                if (d.getControl().getControlKind() != ControlKind.ATOMIC) // nesting for atomic controls not allowed
                    s.append(".1");
            }
            if (children.size() > 1) s.append(" )");
        }
        return s.toString();
    }

    private String generateFakeLinkName() {
        return RandomStringUtils.randomAlphabetic(4).toLowerCase();
    }

    private String closeFakeLinks(List<String> linkNames) {
        return linkNames.stream().collect(Collectors.joining(" /", "/", ""));
    }

    public static String toString(Signature sig) {
        StringBuilder s = new StringBuilder();
        Set<Control<?, ?>> controls = sig.getControls();
        for (Control ctrl : controls) {
            s
                    .append(ctrl.getControlKind().equals(ControlKind.ATOMIC) ? "atomic " : "")
                    .append("ctrl ")
                    .append(ctrl.getNamedType().stringValue()).append(" = ")
                    .append(ctrl.getArity().getValue()).append(";")
                    .append(LINE_SEP);
        }
        return s.toString();
    }
}
