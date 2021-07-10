package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Primitives;
import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphDecoder;
import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphEncoder;
import de.tudresden.inf.st.bigraphs.core.exceptions.ContextIsNotActive;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatchingEngine;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatchingSupport;
//import it.uniud.mads.jlibbig.core.ReactionRule;
import it.uniud.mads.jlibbig.core.std.*;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.ops;

/**
 * Matching algorithm for pure bigraphs (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 */
public class PureBigraphMatchingEngine extends BigraphMatchingSupport implements BigraphMatchingEngine<PureBigraph> {

    private final Logger logger = LoggerFactory.getLogger(PureBigraphMatchingEngine.class);

    //    private final PureBigraphRedexAdapter redexAdapter;
//    private final PureBigraphAgentAdapter agentAdapter;
    //    private AtomicInteger totalHits;
    private final MutableList<PureBigraphParametricMatch> matches = Lists.mutable.empty();
    private final JLibBigBigraphEncoder encoder = new JLibBigBigraphEncoder();
    private final JLibBigBigraphDecoder decoder = new JLibBigBigraphDecoder();

    it.uniud.mads.jlibbig.core.std.Bigraph jLibAgent;
    it.uniud.mads.jlibbig.core.std.Bigraph jLibRedex;
    Matcher matcher = new Matcher();
    AgentMatcher agentMatcher = new AgentMatcher();
    Iterable<? extends AgentMatch> jLibMatchIterator;
    boolean hasMatched = false;
    ReactionRule<PureBigraph> reactionRule;

    private Stopwatch matchingTimer;

    PureBigraphMatchingEngine(PureBigraph agent, ReactionRule<PureBigraph> reactionRule) {
        this.reactionRule = reactionRule;
        //signature, ground agent
        Stopwatch timer = logger.isDebugEnabled() ? Stopwatch.createStarted() : null;
//        this.redexAdapter = new PureBigraphRedexAdapter((PureBigraph) reactionRule.getRedex());
//        this.agentAdapter = new PureBigraphAgentAdapter(agent);

        this.jLibAgent = encoder.encode(agent);
        this.jLibRedex = encoder.encode(reactionRule.getRedex(), jLibAgent.getSignature());

        if (logger.isDebugEnabled() && Objects.nonNull(timer))
            logger.debug("Initialization time: {} (ms)", (timer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
    }

    @Override
    public List<PureBigraphParametricMatch> getMatches() {
//        redexAdapter.clearCache();
//        agentAdapter.clearCache();
        return matches;
    }

    /**
     * Computes all matches
     * <p>
     * First, structural matching, afterwards link matching
     */
    public void beginMatch() {
        if (logger.isDebugEnabled()) {
            matchingTimer = Stopwatch.createStarted();
        }

        de.tudresden.inf.st.bigraphs.core.reactivesystem.InstantiationMap instantationMap = reactionRule.getInstantationMap();
        int[] imArray = new int[instantationMap.getMappings().size()];
        for (int i = 0; i < imArray.length; i++) {
            imArray[i] = instantationMap.get(i).getValue();
        }
        //TODO use agent matcher (ground bigraphs) or matcher
        it.uniud.mads.jlibbig.core.std.InstantiationMap eta = new InstantiationMap(
                reactionRule.getRedex().getSites().size(),
                imArray
        );
        int prms[] = new int[eta.getPlaceDomain()];
        boolean[] neededParam = new boolean[reactionRule.getRedex().getSites().size()];
        for (int i = 0; i < eta.getPlaceDomain(); i++) {
            int j = eta.getPlaceInstance(i);
            neededParam[j] = true;
            prms[i] = j;
        }
//        jLibMatchIterator = agentMatcher.match(jLibAgent, jLibRedex, neededParam);
        jLibMatchIterator = agentMatcher.match(jLibAgent, jLibRedex);

        hasMatched = true;

//        logger.debug("Number of hits (matches): {}", totalHits.get());
        logger.debug("Were all matches found?: {}", hasMatched());
    }

    /**
     * This methods builds the actual bigraphs determined by the matching algorithm (see {@link #beginMatch()}).
     */
    public void createMatchResult() {
        if (logger.isDebugEnabled()) {
            logger.debug("Matching took: {} (ms)", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
            matchingTimer.reset().start();
        }
        try {
            for (Iterator<? extends AgentMatch> it = jLibMatchIterator.iterator(); it.hasNext(); ) {
                AgentMatch each = it.next();
                PureBigraph context = null; //decoder.decode(each.getContext());
                PureBigraph redex = reactionRule.getRedex(); //decoder.decode(each.getRedex());
                PureBigraph redexImage = null; //decoder.decode(each.getRedexImage());
                PureBigraph redexIdentity = null; //decoder.decode(each.getRedexId());
                Collection<PureBigraph> params = new LinkedList<>();
//                for (Bigraph eachP : each.getParams()) {
//                    PureBigraph paramDecoded = decoder.decode(eachP);
//                    params.add(paramDecoded);
//                }
                PureBigraph paramWiring = null; //decoder.decode(each.getParamWiring());
                PureBigraphParametricMatch matchResult = new PureBigraphParametricMatch(
                        each,
                        context,
                        redex,
                        redexImage,
                        redexIdentity,
                        paramWiring,
                        params
                );
                this.matches.add(matchResult);
            }
        } catch (AssertionError error) {
            error.printStackTrace();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Time to build the match result: {} ms", (matchingTimer.stop().elapsed(TimeUnit.NANOSECONDS) / 1e+6f));
//            logger.debug("Number of valid match combinations: {}", validCounter);
        }
    }


    /**
     * Checks if any match could be found and also if <emph>_all_</emph> redex roots could be matched.
     *
     * @return {@code true}, if a correct match could be found, otherwise {@code false}
     */
    public boolean hasMatched() {
        return hasMatched; //totalHits.get() > 0 && totalHits.get() >= redexAdapter.getRoots().size() && (totalHits.get() % redexAdapter.getRoots().size() == 0);
    }
}
