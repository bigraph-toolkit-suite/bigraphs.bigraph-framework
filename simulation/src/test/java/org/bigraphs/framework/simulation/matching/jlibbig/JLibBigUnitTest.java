package org.bigraphs.framework.simulation.matching.jlibbig;

import it.uniud.mads.jlibbig.core.std.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Dominik Grzelak
 */
public class JLibBigUnitTest {

    @Test
    void test_01() {
        Signature signature = new Signature(createControls());
        BigraphBuilder builder = new BigraphBuilder(signature);

//        OuterName door = builder.addOuterName("a");
//        InnerName doorLink = builder.addInnerName("a", door);
//        builder.closeInnerName(doorLink);
        
        Root root = builder.addRoot();
        Node building = builder.addNode("B", root);
        Node roomLeft = builder.addNode("R", building);
        Node roomLeft2 = builder.addNode("R", roomLeft);
        builder.addNode("U", roomLeft2);
        Node roomRight = builder.addNode("R", building);
        builder.addNode("U", roomRight);

        Bigraph agent = builder.makeBigraph();
        System.out.printf("Agent: %s\n", agent.toString());

        Bigraph redex = createRedex(signature);
        Bigraph reactum = createReactum(signature);
        RewritingRule transmitRule = new RewritingRule(redex, reactum, 0, 0, 0);

        Matcher m = new Matcher();
        Iterable<? extends Match> match = m.match(agent, transmitRule.getRedex());
        Iterator<? extends Match> iterator = match.iterator();
        while (iterator.hasNext()) {
            Match next = iterator.next();
// * Given two bigraphs F and G (over the same signature), a match of F in G is a
// * triple <C,R,P> such that their composition C;R;P yields G as for
// * {@link it.uniud.mads.jlibbig.core.Match}.
// Furthermore, the redex R is the
// * juxtaposition of F and a suitable identity; these are called the redex image
// * and the redex id respectively.
            Bigraph context = next.getContext();
            Bigraph redexId = next.getRedexId();
            Bigraph redexImage = next.getRedexImage();
            Bigraph juxtapose = Bigraph.juxtapose(redexImage, redexId);
            System.out.println(redexId.toString());
            System.out.println(redexImage.toString());
            System.out.println(juxtapose.toString());
            System.out.println(redex.toString());
            System.out.println(context.toString());
        }

        // rewrite ergebnis
        int cnt = 0;
        for (Bigraph big : transmitRule.apply(m, agent)) {
//            RuleApplication ruleApplication = new RuleApplication(big, transmitRule);
//            System.out.println(ruleApplication.getBig().toString());
            cnt++;
        }
        System.out.println("Rewriting Cnt: " + cnt);

    }

    //REDEX
    public Bigraph createRedex(Signature signature) {
        BigraphBuilder builder = new BigraphBuilder(signature);
        Root root = builder.addRoot();
        builder.addSite(builder.addNode("R", root));
        builder.addSite(builder.addNode("R", root));
        Bigraph redex = builder.makeBigraph();
        System.out.printf("Redex: %s\n", redex.toString());
        return redex;
    }

    public Bigraph createReactum(Signature signature) {
        BigraphBuilder builder = new BigraphBuilder(signature);
        Root root = builder.addRoot();
        Node roomLeft = builder.addNode("R", root);
        builder.addSite(roomLeft);
        builder.addNode("U", roomLeft);
        builder.addSite(builder.addNode("R", root));
        builder.addSite(builder.addNode("R", root));
        Bigraph redex = builder.makeBigraph();
        System.out.printf("Redex: %s\n", redex.toString());
        return redex;
    }

    public static Collection<Control> createControls() {
        return Collections.unmodifiableList(Arrays.asList(
                createControl("B", 1),
                createControl("R", 1),
                createControl("U", 0),
                createControl("C", 0),
                createControl("S", 0),
                createControl("D", 0)
        ));
    }

    private static Control createControl(String name, int arity) {
        return new Control(name, true, arity);
    }
}
