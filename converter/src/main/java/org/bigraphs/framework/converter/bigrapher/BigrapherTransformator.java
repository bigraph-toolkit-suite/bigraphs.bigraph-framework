package org.bigraphs.framework.converter.bigrapher;

import org.bigraphs.framework.converter.ReactiveSystemPrettyPrinter;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.HasLabel;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

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
public class BigrapherTransformator implements ReactiveSystemPrettyPrinter<PureBigraph, ReactiveSystem<PureBigraph>> {

    public static final String LINE_SEP = System.getProperty("line.separator");
    private static final String DEFAULT_REACTION_ARROW_SYMBOL = "-->";
    private static final char DEFAULT_WHITESPACE_CHAR = ' ';

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
    public String toString(ReactiveSystem<PureBigraph> system) {
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
            List<ReactiveSystemPredicate<PureBigraph>> predicates = new ArrayList<>(system.getPredicates());
            s.append(toString(predicates));
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

    @Override
    public void toOutputStream(ReactiveSystem system, OutputStream outputStream) throws IOException {
        reset();
        String s = toString(system);
        outputStream.write(s.getBytes(), 0, s.length());
    }

    private String toString(List<ReactiveSystemPredicate<PureBigraph>> predicates) {
        StringBuilder s = new StringBuilder();
        for (ReactiveSystemPredicate<PureBigraph> eachPredicate : predicates) {
            String predname = getPredicateVarName(eachPredicate);
            predicateLabels.add(predname);
            s.append(toStringForAgent(eachPredicate.getBigraph(), predname))
                    .append(";").append(LINE_SEP);
        }
        return s.toString();
    }

    private String toString(Collection<ReactionRule<PureBigraph>> reactionRules) {
        StringBuilder s = new StringBuilder();
        for (ReactionRule<PureBigraph> reaction : reactionRules) {
            s.append(toString(reaction)).append(instantiationMap(reaction)).append(";").append(LINE_SEP);
        }
        return s.toString();
    }

    private String instantiationMap(ReactionRule<PureBigraph> reaction) {
        if (!reaction.getInstantationMap().isIdentity()) {
            StringBuilder sb = new StringBuilder(" @ [");
            String indices = reaction.getInstantationMap().getMappings().keySet()
                    .stream().sorted()
                    .map(x ->
                            reaction.getInstantationMap().getMappings().get(x).getValue())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            sb.append(indices).append("]");
            return sb.toString();
        }
        return "";
    }

    private String toString(ReactionRule<PureBigraph> reaction) {
        StringBuilder sb = new StringBuilder("react ");
        String rrName;
        if (reaction instanceof HasLabel && ((HasLabel) reaction).isDefined()) {
            rrName = ((HasLabel) reaction).getLabel().toLowerCase();
            rrName = reactionLabels.contains(rrName) ? (rrName + reactionRuleCounter++) : rrName;
        } else {
            rrName = reaction.getClass().getSimpleName().toLowerCase() + (reactionRuleCounter++);
        }
        reactionLabels.add(rrName);
        sb.append(rrName).append(" = ");
        String redex = toString(reaction.getRedex(), false);
        redex = !randomLinkNames.isEmpty() ? closeFakeLinks(randomLinkNames) + " (" + redex + ")" : redex;
        randomLinkNames.clear();
        String reactum = toString(reaction.getReactum(), false);
        reactum = !randomLinkNames.isEmpty() ? closeFakeLinks(randomLinkNames) + " (" + reactum + ")" : reactum;
        randomLinkNames.clear();
        sb.append(redex).append(DEFAULT_WHITESPACE_CHAR).append(DEFAULT_REACTION_ARROW_SYMBOL).append(DEFAULT_WHITESPACE_CHAR).append(reactum);
        return sb.toString();
    }

    private String toString(PureBigraph bigraph, boolean addBarren) {
        StringBuilder s = new StringBuilder();
        Iterator<BigraphEntity.RootEntity> iterator = bigraph.getRoots().iterator();
        while (iterator.hasNext()) {
            s.append('(');
            BigraphEntity.RootEntity next = iterator.next();
            Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(next);
            if (!childrenOf.isEmpty()) {
                Iterator<BigraphEntity<?>> childIterator = childrenOf.iterator();
                while (childIterator.hasNext()) {
                    MutableList<BigraphEntity.Link> links = Lists.mutable.withAll(bigraph.getEdges());
//                    List<BigraphEntity.Link> links = new ArrayList<>(bigraph.getEdges());
                    links.addAll(bigraph.getOuterNames());
                    String s1 = toString(bigraph, childIterator.next(), links, bigraph.getSites(), addBarren);
                    s.append(s1).append(childIterator.hasNext() ? " | " : "");
                }
            }
            s.append(iterator.hasNext() ? ") || " : ")");
        }
        for (BigraphEntity.OuterName each : bigraph.getOuterNames()) {
            if (bigraph.getPointsFromLink(each).size() == 0) {
                s.append(DEFAULT_WHITESPACE_CHAR).append('|').append(DEFAULT_WHITESPACE_CHAR).append('{').append(each.getName()).append('}');
            }
        }
        return s.toString();
    }

    private String getAgentVarName(PureBigraph bigraph) {
        return bigraph.getMetaModel().getName().toLowerCase();
    }

    private String getPredicateVarName(ReactiveSystemPredicate<PureBigraph> eachPredicate) {
//        predicateLabels.add(predicateVarPrefix + (predicateVarCnt++));
//        return predicateLabels.get(predicateLabels.size() - 1);
        String predName;
        if (eachPredicate instanceof HasLabel && ((HasLabel) eachPredicate).isDefined()) {
            predName = ((HasLabel) eachPredicate).getLabel();
            predName = predicateLabels.contains(predName) ? (predName + predicateVarCnt++) : predName;
        } else {
            predName = eachPredicate.getClass().getSimpleName().toLowerCase() + (predicateVarCnt++);
        }
        return predName;
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
        Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(next);
        if (!childrenOf.isEmpty()) {
            Iterator<BigraphEntity<?>> childIterator = childrenOf.iterator();
            while (childIterator.hasNext()) {
                List<BigraphEntity.Link> links = new ArrayList<>(bigraph.getEdges());
                links.addAll(bigraph.getOuterNames());
                String s1 = toString(bigraph, childIterator.next(), links, bigraph.getSites(), true);
                s.append(s1).append(childIterator.hasNext() ? " | " : "");
            }
        } else {
            s.append("1");
        }

        StringBuilder emptyOuterCollector = new StringBuilder();
        for (BigraphEntity.OuterName each : bigraph.getOuterNames()) {
            if (bigraph.getPointsFromLink(each).size() == 0) {
                emptyOuterCollector.append(" || ").append('{').append(each.getName()).append('}');
            }
        }
        if (emptyOuterCollector.length() != 0) {
            s.insert(0, '(').append(')').append(emptyOuterCollector);
        }

        if (!randomLinkNames.isEmpty()) {
            StringBuilder s2 = new StringBuilder();
            String closeNames = closeFakeLinks(randomLinkNames);
            s2.append(varPrefix).append(closeNames).append(" (").append(s).append(")");
            randomLinkNames.clear();
            return s2.toString();
        } else {
            return s.insert(0, varPrefix).toString();
        }
    }

    private String toString(PureBigraph bigraph, BigraphEntity d, Collection<BigraphEntity.Link> collection, Collection<BigraphEntity.SiteEntity> sitelist, boolean addBarren) {
        StringBuilder s = new StringBuilder();
        if (BigraphEntityType.isSite(d)) {
            s.append("id(1)"); //.append(((BigraphEntity.SiteEntity) d).getIndex());
        } else {
            s.append(StringUtils.capitalize(d.getControl().getNamedType().stringValue()));
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
                    if (BigraphEntityType.isEdge(link) && !randomLinkNames.contains(name)) {
                        randomLinkNames.add(name);
                    }
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
            Collection<BigraphEntity<?>> children = bigraph.getChildrenOf(d);
            if (!children.isEmpty()) {
                s.append(".");
                if (children.size() > 1) s.append("( ");

                Iterator<BigraphEntity<?>> childIt = children.iterator();
                while (childIt.hasNext()) {
                    s.append(toString(bigraph, childIt.next(), collection, sitelist, addBarren))
                            .append(childIt.hasNext() ? " | " : "");
                }
            } else {
                if (d.getControl().getControlKind() != ControlStatus.ATOMIC && addBarren) // nesting for atomic controls not allowed
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
                    .append(ctrl.getControlKind().equals(ControlStatus.ATOMIC) ? "atomic " : "")
                    .append("ctrl ")
                    .append(StringUtils.capitalize(ctrl.getNamedType().stringValue())).append(" = ")
                    .append(ctrl.getArity().getValue()).append(";")
                    .append(LINE_SEP);
        }
        return s.toString();
    }
}
