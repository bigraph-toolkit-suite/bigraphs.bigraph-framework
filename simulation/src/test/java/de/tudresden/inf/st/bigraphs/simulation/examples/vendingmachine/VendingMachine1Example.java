package de.tudresden.inf.st.bigraphs.simulation.examples.vendingmachine;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.examples.BaseExampleTestSupport;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.BigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.PureBigraphModelChecker;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static de.tudresden.inf.st.bigraphs.simulation.modelchecking.ModelCheckingOptions.transitionOpts;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VendingMachine1Example extends BaseExampleTestSupport {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/vendingmachine/";

    public VendingMachine1Example() {
        super(TARGET_DUMP_PATH, true);
    }

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));
        new File(TARGET_DUMP_PATH + "states/").mkdir();
    }

    //TODO: ein pfad predicate könnte auch sein: wenn regel 1 2x ausgeführt wurde weiß man dass zweimal kaffee gegeben wurde vice versa für tea
    @Test
    void simulate() throws Exception {

        PureBigraph agent = agent(2, 2, 2);
        printMetaModel(agent);
        eb(agent, "agent", false);
        ReactionRule<PureBigraph> insertCoinRR = insertCoin();
        eb(insertCoinRR.getRedex(), "insertCoinL");
        eb(insertCoinRR.getReactum(), "insertCoinR");
//        print(insertCoinRR.getRedex());
//        print(insertCoinRR.getReactum());

        ReactionRule<PureBigraph> pushBtn1 = pushButton1();
        eb(pushBtn1.getRedex(), "pushBtn1L");
        eb(pushBtn1.getReactum(), "pushBtn1R");
//        print(pushBtn1.getRedex());
//        print(pushBtn1.getReactum());
        ReactionRule<PureBigraph> pushBtn2 = pushButton2();
        eb(pushBtn2.getRedex(), "pushBtn2L");
        eb(pushBtn2.getReactum(), "pushBtn2R");
//        print(pushBtn2.getRedex());
//        print(pushBtn2.getReactum());

        ReactionRule<PureBigraph> giveCoffee = giveCoffee();
        eb(giveCoffee.getRedex(), "giveCoffeeL");
        eb(giveCoffee.getReactum(), "giveCoffeeR");
//        print(giveCoffee.getRedex());
//        print(giveCoffee.getReactum());

        ReactionRule<PureBigraph> giveTea = giveTea();
        eb(giveTea.getRedex(), "giveTeaL");
        eb(giveTea.getReactum(), "giveTeaR");
//        print(giveTea.getRedex());
//        print(giveTea.getReactum());

        SubBigraphMatchPredicate<PureBigraph> teaEmpty = teaContainerIsEmpty();
        eb(teaEmpty.getBigraph(), "teaEmpty");
        print(teaEmpty.getBigraph());
        SubBigraphMatchPredicate<PureBigraph> coffeeEmpty = coffeeContainerIsEmpty();
        eb(coffeeEmpty.getBigraph(), "coffeeEmpty");
        print(coffeeEmpty.getBigraph());

        PureReactiveSystem reactiveSystem = new PureReactiveSystem();
        reactiveSystem.setAgent(agent);
        reactiveSystem.addPredicate(teaEmpty);
        reactiveSystem.addPredicate(coffeeEmpty);
        reactiveSystem.addReactionRule(insertCoinRR);
        reactiveSystem.addReactionRule(pushBtn1);
        reactiveSystem.addReactionRule(pushBtn2);
        reactiveSystem.addReactionRule(giveCoffee);
        reactiveSystem.addReactionRule(giveTea);
//        reactiveSystem.addPredicate(predicate);


        PureBigraphModelChecker modelChecker = new PureBigraphModelChecker(
                reactiveSystem,
                BigraphModelChecker.SimulationStrategy.Type.BFS,
                opts());
//        modelChecker.setReactiveSystemListener(this);
        modelChecker.execute();
//        assertTrue(Files.exists(completePath));
//        assertTrue(carArrivedAtTarget);
    }

    private ModelCheckingOptions opts() {
        Path completePath = Paths.get(TARGET_DUMP_PATH, "transition_graph.png");
        ModelCheckingOptions opts = ModelCheckingOptions.create();
        opts
                .and(transitionOpts()
                        .setMaximumTransitions(50)
                        .setMaximumTime(60)
                        .allowReducibleClasses(false)
                        .create()
                )
                .doMeasureTime(true)
                .and(ModelCheckingOptions.exportOpts()
                        .setReactionGraphFile(new File(completePath.toUri()))
                        .setPrintCanonicalStateLabel(false)
                        .setOutputStatesFolder(new File(TARGET_DUMP_PATH + "states/"))
                        .create()
                )
        ;
        return opts;
    }

    private SubBigraphMatchPredicate<PureBigraph> teaContainerIsEmpty() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());
        builder.createRoot()
                .addChild("VM").down()
                .addSite()
                .addChild("Container")
                .addChild("Container").down()
                .addChild("Coffee").addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    private SubBigraphMatchPredicate<PureBigraph> coffeeContainerIsEmpty() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig());
        builder.createRoot()
                .addChild("VM").down()
                .addSite()
                .addChild("Container")
                .addChild("Container").down()
                .addChild("Tea").addSite()
        ;
        PureBigraph bigraph = builder.createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    private PureBigraph agent(int numOfCoffee, int numOfTea, int numOfCoinsPhd) throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> vmB = pureBuilder(sig());
        PureBigraphBuilder<DefaultDynamicSignature> phdB = pureBuilder(sig());

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy containerCoffee = vmB.hierarchy("Container");
        for (int i = 0; i < numOfCoffee; i++) {
            containerCoffee = containerCoffee.addChild("Coffee");
        }
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy containerTea = vmB.hierarchy("Container");
        for (int i = 0; i < numOfTea; i++) {
            containerTea = containerTea.addChild("Tea");
        }
        vmB.createRoot()
                .addChild("VM")
                .down()
                .addChild(containerCoffee.top())
                .addChild(containerTea.top())
                .addChild("Button1")
                .addChild("Button2")
                .addChild("Tresor")
        ;

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy wallet = vmB.hierarchy("Wallet");
        for (int i = 0; i < numOfCoinsPhd; i++) {
            wallet = wallet.addChild("Coin");
        }
        phdB.createRoot().addChild("PHD")
                .down()
                .addChild(wallet.top());


        Placings<DefaultDynamicSignature> placings = purePlacings(sig());
        Placings<DefaultDynamicSignature>.Merge merge2 = placings.merge(2);
        PureBigraph vm = vmB.createBigraph();
        PureBigraph phd = phdB.createBigraph();
        Bigraph<DefaultDynamicSignature> both = ops(vm).parallelProduct(phd).getOuterBigraph();
        Bigraph<DefaultDynamicSignature> result = ops(merge2).compose(both).getOuterBigraph();
        return (PureBigraph) result;
    }

    /**
     * Insert is only possible if no button was pressed
     */
    public ReactionRule<PureBigraph> insertCoin() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Coin").addSite()
                .top()
                .addChild("VM").down().addSite().addChild("Button1").addChild("Button2");
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down().addSite().addChild("Button1").addChild("Button2").addChild("Coin");
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    /**
     * TODO: idea show how we can easily change the context (button can only be pressed if PHD has an ID card, or if it is in the safety zone etc.
     * phd must be present; a VM cannot press a button itself
     * For coffee.
     *
     * @return
     * @throws Exception
     */
    public ReactionRule<PureBigraph> pushButton1() throws Exception {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button2")
                .addChild("Button1")
        ;
        builder2.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button2")
                .addChild("Button1").down().addChild("Pressed");
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    /**
     * Several things happen:
     * Check that button was pressed;
     * check that enough money was inserted <- customization opportunity for user
     * check if coffee is available
     * <p>
     * give the rest of the money back
     * put the rest in the tresor
     * release button
     */
    public ReactionRule<PureBigraph> giveCoffee() throws Exception {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down()
                .addChild("Coin").addSite()
                .addChild("Container").down().addChild("Coffee").addSite().up()
                .addChild("Button1").down().addChild("Pressed").up()
                .addChild("Tresor").down().addSite();
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Coffee").addSite()
                .top()
                .addChild("VM").down()
                .addSite()
                .addChild("Container").down().addSite().up()
                .addChild("Button1")
                .addChild("Tresor").down().addChild("Coin").addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    public ReactionRule<PureBigraph> giveTea() throws Exception {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addSite()
                .top()
                .addChild("VM").down()
                .addChild("Coin").addSite()
                .addChild("Container").down().addChild("Tea").addSite().up()
                .addChild("Button2").down().addChild("Pressed").up()
                .addChild("Tresor").down().addSite();
        ;
        builder2.createRoot()
                .addChild("PHD").down().addChild("Wallet").down().addChild("Tea").addSite()
                .top()
                .addChild("VM").down()
                .addSite()
                .addChild("Container").down().addSite().up()
                .addChild("Button2")
                .addChild("Tresor").down().addChild("Coin").addSite()
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }


    /**
     * phd must be present; a VM cannot press a button itself.
     * for tea.
     */
    public ReactionRule<PureBigraph> pushButton2() throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        DefaultDynamicSignature signature = sig();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(signature);

        builder.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button1")
                .addChild("Button2");
        ;
        builder2.createRoot()
                .addChild("PHD").down().addSite()
                .top()
                .addChild("VM").down().addChild("Coin").addSite()
                .addChild("Button1")
                .addChild("Button2").down().addChild("Pressed")
        ;
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }

    private DefaultDynamicSignature sig() {
        DynamicSignatureBuilder sb = pureSignatureBuilder();
        DefaultDynamicSignature sig = sb
                .addControl("Coin", 0)
                .addControl("VM", 0)
                .addControl("Button1", 0)
                .addControl("Button2", 0)
                .addControl("Pressed", 0)
                .addControl("Coffee", 0)
                .addControl("Container", 0)
                .addControl("Tea", 0)
                .addControl("PHD", 0)
                .addControl("Wallet", 0)
                .addControl("Tresor", 0)
                .create();
        return sig;
    }
}
