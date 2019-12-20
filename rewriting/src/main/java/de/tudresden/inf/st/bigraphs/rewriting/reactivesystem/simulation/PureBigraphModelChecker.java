package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of an {@link BigraphModelChecker} providing a simple model checking for BRS for pure bigraphs (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureBigraphModelChecker extends BigraphModelChecker<PureBigraph> {
    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private static int cnt = 0;

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationType simulationType, ReactiveSystemOptions options) {
        super(reactiveSystem, simulationType, options);
    }
    
    @Override
    protected synchronized PureBigraph buildGroundReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        try {
            //OK
//            try {
//                BigraphGraphvizExporter.toPNG(match.getContext(),
//                        true,
//                        new File(String.format("context_%s.png", cnt))
//                );
//                BigraphGraphvizExporter.toPNG(match.getContextIdentity(),
//                        true,
//                        new File(String.format("contextIdentity_%s.png", cnt))
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Bigraph outerBigraph = factory
                    .asBigraphOperator(match.getContext())
                    .parallelProduct((Bigraph<DefaultDynamicSignature>) match.getContextIdentity())
                    .getOuterBigraph();
//            System.out.println(outerBigraph);
//            try {
//                BigraphGraphvizExporter.toPNG(outerBigraph,
//                        true,
//                        new File("contextimage.png")
//                );
//                BigraphGraphvizExporter.toPNG(rule.getReactum(),
//                        true,
//                        new File("reactum.png")
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Bigraph originAgent = factory.asBigraphOperator(outerBigraph).compose(match.getRedex()).getOuterBigraph();
//            GraphvizConverter.toPNG(originAgent,
//                    true,
//                    new File("agentOrigin.png")
//            );
//            GraphvizConverter.toPNG(rule.getRedex(),
//                    true,
//                    new File("redex.png")
//            );
            Bigraph agentReacted = factory.asBigraphOperator(outerBigraph)
                    .compose(rule.getReactum())
                    .getOuterBigraph();
//            GraphvizConverter.toPNG(agentReacted,
//                    true,
//                    new File("agentReacted.png")
//            );
            exportState((PureBigraph) agentReacted, String.valueOf(cnt));
            cnt++;
//            try {
//                BigraphGraphvizExporter.toPNG(agentReacted,
//                        true,
//                        new File("agent-reacted-" + cnt + ".png")
//                );
////                BigraphArtifacts.exportAsInstanceModel(agentReacted, new FileOutputStream(String.format("instance-model_%s.xmi", cnt)));
////                BigraphArtifacts.exportAsMetaModel(agentReacted, new FileOutputStream(String.format("meta-model_%s.ecore", cnt)));
//                cnt++;
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return (PureBigraph) agentReacted;
        } catch (IncompatibleSignatureException e) {
            e.printStackTrace();
        } catch (IncompatibleInterfaceException e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    //TODO: beachte instantiation map
    @Override
    protected synchronized PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        //first build parallel product of the parameters using the instantiation map
        try {

//            //OK
//            try {
//                BigraphGraphvizExporter.toPNG(match.getContext(),
//                        true,
//                        new File(String.format("context_%s.png", cnt))
//                );
//                BigraphGraphvizExporter.toPNG(match.getContextIdentity(),
//                        true,
//                        new File(String.format("contextIdentity_%s.png", cnt))
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            //NOTE: juxtapose changed to parallelProduct (check if this is right)
            Bigraph outerBigraph = factory
                    .asBigraphOperator(match.getContext())
                    .parallelProduct((Bigraph<DefaultDynamicSignature>) match.getContextIdentity())
                    .getOuterBigraph();
//            try {
//                BigraphGraphvizExporter.toPNG(outerBigraph,
//                        true,
//                        new File(String.format("outerBigraph_%s.png", cnt))
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Bigraph d_Params = null;
            List<PureBigraph> parameters = new ArrayList<>(match.getParameters());
            if (parameters.size() >= 2) {
                FiniteOrdinal<Integer> mu_ix = rule.getInstantationMap().get(0);
                BigraphComposite d1 = factory.asBigraphOperator(parameters.get(mu_ix.getValue()));
                for (int i = 1, n = parameters.size(); i < n; i++) {
                    mu_ix = rule.getInstantationMap().get(i);
                    d1 = d1.parallelProduct(parameters.get(mu_ix.getValue()));
                }
                d_Params = d1.getOuterBigraph();
            } else {
                d_Params = parameters.get(0);
            }

//            try {
//                BigraphGraphvizExporter.toPNG(d_Params,
//                        true,
//                        new File(String.format("parameters_%s.png", cnt))
//                );
//                BigraphGraphvizExporter.toPNG(match.getRedexIdentity(),
//                        true,
//                        new File(String.format("redex-identity_%s.png", cnt))
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            //id_X || R) * d_Params <=> R . d_Params
            BigraphComposite reactumImage = factory.asBigraphOperator(match.getRedexIdentity()).parallelProduct(rule.getReactum());//.compose(d_Params).getOuterBigraph();
            BigraphComposite compose = reactumImage.compose(d_Params);

//            try {
//                BigraphGraphvizExporter.toPNG(reactumImage.getOuterBigraph(),
//                        true,
//                        new File(String.format("reactumImage_%s.png", cnt))
//                );
//                BigraphGraphvizExporter.toPNG(compose.getOuterBigraph(),
//                        true,
//                        new File(String.format("reactumImage-composed_%s.png", cnt))
//                );
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            Bigraph agentReacted = factory.asBigraphOperator(outerBigraph)
                    .compose(compose)
                    .getOuterBigraph();

            exportState((PureBigraph) agentReacted, String.valueOf(cnt));
            //
//            if (Objects.nonNull(options.get(ReactiveSystemOptions.Options.EXPORT))) {
//                ReactiveSystemOptions.ExportOptions opts = options.get(ReactiveSystemOptions.Options.EXPORT);
//                if (opts.hasOutputStatesFolder()) {
//                    try {
////                Bigraph test = factory.asBigraphOperator(outerBigraph).compose(rule.getReactum()).getOuterBigraph();
////                BigraphGraphvizExporter.toPNG(test,
////                        true,
////                        new File("test" + (cnt) + ".png")
////                );
//                        BigraphGraphvizExporter.toPNG(agentReacted,
//                                true,
//                                new File(String.format("agentReacted_%s.png", cnt))
//                        );
////                BigraphArtifacts.exportAsInstanceModel(agentReacted, new FileOutputStream(String.format("instance-model_%s.xmi", cnt)));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
            cnt++;

            return (PureBigraph) agentReacted;
        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
            e.printStackTrace();
            return null;
        }
//        catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
    }


}
