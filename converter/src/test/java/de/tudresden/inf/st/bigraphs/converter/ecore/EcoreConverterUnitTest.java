package de.tudresden.inf.st.bigraphs.converter.ecore;

import de.tudresden.inf.st.bigraphs.converter.PureReactiveSystemStub;
import de.tudresden.inf.st.bigraphs.converter.bigmc.BigMcTransformationUnitTest;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactionRule;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;

/**
 * @author Dominik Grzelak
 */
public class EcoreConverterUnitTest {
    private static PureBigraphFactory factory = pure();
    private static final String DUMP_TARGET = "src/test/resources/dump/";

    @Test
    void counting_bigraph() throws Exception {
        PureReactiveSystemStub reactiveSystem = new PureReactiveSystemStub();

        PureBigraph agent_a = BigMcTransformationUnitTest.createAgent_A(3, 4);
        ReactionRule<PureBigraph> rr_1 = BigMcTransformationUnitTest.createReactionRule_1();

        reactiveSystem.setAgent(agent_a);
        reactiveSystem.addReactionRule(rr_1);
        reactiveSystem.addReactionRule(BigMcTransformationUnitTest.createReactionRule_2());
        reactiveSystem.addReactionRule(BigMcTransformationUnitTest.createReactionRule_3());

        EcoreAgentConverter prettyPrinter = new EcoreAgentConverter();
        String s = prettyPrinter.toString(reactiveSystem.getAgent());
        System.out.println(s);

        FileOutputStream fout = new FileOutputStream(new File(DUMP_TARGET + "couting.xmi"));
        prettyPrinter.toOutputStream(reactiveSystem.getAgent(), fout);
        fout.close();

        EcoreConverter converter = new EcoreConverter();
        String s1 = converter.toString(reactiveSystem);
        System.out.println(s1);
        FileOutputStream fout2 = new FileOutputStream(new File(DUMP_TARGET + "couting_stream.xmi"));
        converter.toOutputStream(reactiveSystem, fout2);
        fout2.close();
    }
}
