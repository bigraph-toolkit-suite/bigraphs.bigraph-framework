package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphDecoder;
import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphEncoder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.AbstractSimpleReactiveSystem;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.std.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An implementation of an {@link AbstractSimpleReactiveSystem} providing a simple BRS data structure for pure bigraphs
 * (see {@link PureBigraph}) and possibly later also binding bigraphs, bigraphs with sharing etc.
 *
 * Uses some functionality from jLibBig.
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureReactiveSystem extends AbstractSimpleReactiveSystem<PureBigraph> {
    private final Logger logger = LoggerFactory.getLogger(PureReactiveSystem.class);

    private it.uniud.mads.jlibbig.core.std.InstantiationMap constructEta(ReactionRule<PureBigraph> reactionRule) {
        de.tudresden.inf.st.bigraphs.core.reactivesystem.InstantiationMap instantationMap = reactionRule.getInstantationMap();
        int[] imArray = new int[instantationMap.getMappings().size()];
        for (int i = 0; i < imArray.length; i++) {
            imArray[i] = instantationMap.get(i).getValue();
        }
        it.uniud.mads.jlibbig.core.std.InstantiationMap eta = new InstantiationMap(
                reactionRule.getRedex().getSites().size(),
                imArray
        );
        return eta;
    }

    @Override
    public PureBigraph buildGroundReaction(final PureBigraph agent, final BigraphMatch<PureBigraph> match, final ReactionRule<PureBigraph> rule) {
        try {

            PureBigraphParametricMatch matchResult = (PureBigraphParametricMatch) match;

            AgentMatch jLibMatchResult = (AgentMatch) matchResult.getJLibMatchResult();

            InstantiationMap eta = constructEta(rule);
//            BigraphFileModelManagement.exportAsInstanceModel(matchResult.getContext(), System.out);
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) matchResult.getRedexIdentity(), System.out);
//            BigraphFileModelManagement.exportAsInstanceModel(matchResult.getRedexImage(), System.out);
//            BigraphFileModelManagement.exportAsInstanceModel(matchResult.getRedex(), System.out);
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) matchResult.getContextIdentity(), System.out);

            boolean[] cloneParam = new boolean[eta.getPlaceDomain()];
            int prms[] = new int[eta.getPlaceDomain()];
            for (int i = 0; i < eta.getPlaceDomain(); i++) {
                int j = eta.getPlaceInstance(i);
                prms[i] = j;
            }
            for (int i = 0; i < prms.length; i++) {
                if (cloneParam[i])
                    continue;
                for (int j = i + 1; j < prms.length; j++) {
                    cloneParam[j] = cloneParam[j] || (prms[i] == prms[j]);
                }
            }

            BigraphBuilder bb = new BigraphBuilder(jLibMatchResult.getRedex().getSignature());
            for (int i = eta.getPlaceDomain() - 1; 0 <= i; i--) {
                bb.leftJuxtapose(jLibMatchResult.getParams().get(eta.getPlaceInstance(i)),
                        !cloneParam[i]);
            }
            Bigraph lambda = jLibMatchResult.getParamWiring();
            for (EditableInnerName n : lambda.inners.values()) {
                if (!bb.containsOuterName(n.getName())) {
                    lambda.inners.remove(n.getName());
                    n.setHandle(null);
                }
            }
            for (int i = eta.getPlaceCodomain() - eta.getPlaceDomain(); i > 0; i--) {
                lambda.roots.remove(0);
                lambda.sites.remove(0);
            }
            for (int i = eta.getPlaceDomain() - eta.getPlaceCodomain(); i > 0; i--) {
                EditableRoot r = new EditableRoot();
                r.setOwner(lambda);
                EditableSite s = new EditableSite(r);
                lambda.roots.add(r);
                lambda.sites.add(s);
            }
            bb.outerCompose(lambda, true);
            Bigraph inreact = instantiateReactum(jLibMatchResult, rule);
            inreact = Bigraph.juxtapose(inreact, jLibMatchResult.getRedexId(), true);
            bb.outerCompose(inreact, true);
            bb.outerCompose(jLibMatchResult.getContext(), true);
            Bigraph result = bb.makeBigraph(true);

            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////////
//            InstantiationMap eta = constructEta(rule);
//            BigraphBuilder bb = new BigraphBuilder(
//                    instantiateReactum(jLibMatchResult, rule), true);
//            bb.leftJuxtapose(jLibMatchResult.getRedexId(), true);
//            bb.outerCompose(jLibMatchResult.getContext(), true);
//            Bigraph big = bb.makeBigraph(true);
//            Iterator<Bigraph> args = eta.instantiate(jLibMatchResult.getParam()).iterator();
//            // }
//
//            while (args.hasNext()) {
//                Bigraph result;
//                Bigraph params = args.next();
//                if (args.hasNext())
//                    result = Bigraph.compose(big.clone(), params, true);
//                else
//                    result = Bigraph.compose(big, params, true);
////                if (DEBUG_PRINT_RESULT)
////                    System.out.println(result);
////                if (DEBUG_CONSISTENCY_CHECK && !result.isConsistent()) {
////                    throw new RuntimeException("Inconsistent bigraph");
////                }
////                return result;
//                JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
//                PureBigraph decodedResult = decoder.decode(result, agent.getSignature());
//                return decodedResult;
//            }
//
//            return null;
            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////////
            JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();
            PureBigraph decodedResult = decoder.decode(result, agent.getSignature());
//            BigraphFileModelManagement.exportAsInstanceModel(decodedResult, System.out);
            return decodedResult;
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    //TODO: hier müsste nun eine liste zurückkommen wegen eta instantiation.
    // bzw zweite methode definieren: eine die eta nicht beachtet und eine die es macht -> dann verwenden wir immer nur die zwei in BreadthFirstStrategy
    @Override
    public PureBigraph buildParametricReaction(final PureBigraph agent, final BigraphMatch<PureBigraph> match, final ReactionRule<PureBigraph> rule) {
        try {
            return this.buildGroundReaction(agent, match, rule);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Instantiates rule's reactum with respect to the given match.
     *
     * @param match the match with respect to the reactum has to be instantiated.
     * @return the reactum instance.
     */
    protected final Bigraph instantiateReactum(Match match, ReactionRule<PureBigraph> rule) {
        JLibBigBigraphEncoder decoder = new JLibBigBigraphEncoder();
//        Bigraph reactum = getReactum();
        Bigraph reactum = decoder.encode(rule.getReactum(), match.getRedex().getSignature());
        Bigraph big = new Bigraph(reactum.getSignature());
        Owner owner = big;
        Map<Handle, EditableHandle> hnd_dic = new HashMap<>();
        // replicate outer names
        for (EditableOuterName o1 : reactum.outers.values()) {
            EditableOuterName o2 = o1.replicate();
            big.outers.put(o2.getName(), o2);
            o2.setOwner(owner);
            hnd_dic.put(o1, o2);
        }
        // replicate inner names
        for (EditableInnerName i1 : reactum.inners.values()) {
            EditableInnerName i2 = i1.replicate();
            EditableHandle h1 = i1.getHandle();
            EditableHandle h2 = hnd_dic.get(h1);
            if (h2 == null) {
                // the bigraph is inconsistent if g is null
                h2 = h1.replicate();
                h2.setOwner(owner);
                hnd_dic.put(h1, h2);
            }
            i2.setHandle(h2);
            big.inners.put(i2.getName(), i2);
        }
        // replicate place structure
        // the queue is used for a breadth first visit
        class Pair {
            final EditableChild c;
            final EditableParent p;

            Pair(EditableParent p, EditableChild c) {
                this.c = c;
                this.p = p;
            }
        }
        Deque<Pair> q = new ArrayDeque<>();
        for (EditableRoot r1 : reactum.roots) {
            EditableRoot r2 = r1.replicate();
            big.roots.add(r2);
            r2.setOwner(owner);
            for (EditableChild c : r1.getEditableChildren()) {
                q.add(new Pair(r2, c));
            }
        }
        EditableSite[] sites = new EditableSite[reactum.sites.size()];
        while (!q.isEmpty()) {
            Pair t = q.poll();
            if (t.c instanceof EditableNode) {
                EditableNode n1 = (EditableNode) t.c;
                EditableNode n2 = n1.replicate();
                n2.setName(n1.getName());
                instantiateReactumNode(n1, n2, match);
                // set m's parent (which added adds m as its child)
                n2.setParent(t.p);
                for (int i = n1.getControl().getArity() - 1; 0 <= i; i--) {
                    EditableNode.EditablePort p1 = n1.getPort(i);
                    EditableHandle h1 = p1.getHandle();
                    // looks for an existing replica
                    EditableHandle h2 = hnd_dic.get(h1);
                    if (h2 == null) {
                        // the bigraph is inconsistent if g is null
                        h2 = h1.replicate();
                        h2.setOwner(owner);
                        hnd_dic.put(h1, h2);
                    }
                    n2.getPort(i).setHandle(h2);
                }

//                Collection<EditableNode> nodes = (Collection<EditableNode>) big.getNodes();
//                nodes.add(n2);
                // enqueue children for visit
                for (EditableChild c : n1.getEditableChildren()) {
                    q.add(new Pair(n2, c));
                }
            } else {
                // c instanceof EditableSite
                EditableSite s1 = (EditableSite) t.c;
                EditableSite s2 = s1.replicate();
                s2.setParent(t.p);
                sites[reactum.sites.indexOf(s1)] = s2;
            }
        }
        big.sites.addAll(Arrays.asList(sites));
        return big;
    }

    /**
     * This method is called during the instantiation of rule's reactum. Inherit
     * this method to customise instantiation of Nodes e.g. attaching properties
     * taken from nodes in the redex image determined by the given match.
     *
     * @param original The original node from the reactum.
     * @param instance The replica to be used.
     * @param match    The match referred by the instantiation.
     */
    protected void instantiateReactumNode(Node original, Node instance,
                                          Match match) {
        System.out.println("");
    }
}
