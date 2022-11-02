package de.tudresden.inf.st.bigraphs.simulation.matching;

import com.google.common.base.Stopwatch;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.ops;

public class AbstractUnitTestSupport {
    void exportGraph(Bigraph<?> big, String path) {
        try {
            BigraphGraphvizExporter.toPNG((PureBigraph) big,
                    true,
                    new File(path)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void createGraphvizOutput(Bigraph<?> agent, BigraphMatch<?> next, String path) throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        PureBigraph context = (PureBigraph) next.getContext();
        PureBigraph redex = (PureBigraph) next.getRedex();
        Bigraph contextIdentity = next.getContextIdentity();
        Bigraph<DefaultDynamicSignature> identityForParams = next.getRedexIdentity();
        if (contextIdentity != null) {
            PureBigraph contextComposed = (PureBigraph) ops(context).parallelProduct(contextIdentity).getOuterBigraph();
//            BigraphModelFileStore.exportAsInstanceModel(contextComposed, "contextComposed",
//                    new FileOutputStream("src/test/resources/graphviz/contextComposed.xmi"));
            BigraphGraphvizExporter.toPNG(contextComposed,
                    true,
                    new File(path + "contextComposed.png")
            );
        }


        //This takes a lot if time!
        System.out.println("Create png's");
        Stopwatch timer = Stopwatch.createStarted();
        try {
            if (context != null)
                BigraphGraphvizExporter.toPNG(context,
                        true,
                        new File(path + "context.png")
                );
//            System.out.println(convert);
            if (agent != null)
                BigraphGraphvizExporter.toPNG(agent,
                        true,
                        new File(path + "agent.png")
                );
            if (redex != null)
                BigraphGraphvizExporter.toPNG(redex,
                        true,
                        new File(path + "redex.png")
                );
            if (contextIdentity != null)
                BigraphGraphvizExporter.toPNG(contextIdentity,
                        true,
                        new File(path + "identityForContext.png")
                );
            if (identityForParams != null)
                BigraphGraphvizExporter.toPNG(identityForParams,
                        true,
                        new File(path + "identityForParams.png")
                );

//            BigraphComposite bigraphComposite = factory
//                    .asBigraphOperator(identityForParams).parallelProduct(redex); //.compose();
//            GraphvizConverter.toPNG(bigraphComposite.getOuterBigraph(),
//                    true,
//                    new File(path + "redexImage.png")
//            );

            AtomicInteger cnt = new AtomicInteger(0);
//            next.getParameters().forEach(x -> {
////                try {
////                    BigraphGraphvizExporter.toPNG((PureBigraph) x,
////                            true,
////                            new File(path + "param_" + cnt.incrementAndGet() + ".png")
////                    );
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//
//            });
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Create png's took (millisecs) " + elapsed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
