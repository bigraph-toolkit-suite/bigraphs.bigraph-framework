package org.bigraphs.framework.converter.bigred;

import org.bigraphs.framework.converter.PureReactiveSystemStub;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test methods to read and write the included "context-aware printing example" of BigRed.
 *
 * @author Dominik Grzelak
 */
public class PrintingBigraphExampleTest {
    private static String BASE_PATH = "src/test/resources/bigred/printing-example/";
    private static String DUMP_TARGET = "src/test/resources/dump/bigred/printing-example/";

    private static String getFile(String filename) throws IOException {
        File file = Paths.get(BASE_PATH, filename).toFile();
        return file.getCanonicalPath();
    }

    @Test
    void read_signatures_test() throws IOException {
        DefaultSignatureXMLLoader sxl = new DefaultSignatureXMLLoader();
        sxl.readXml(getFile("signatures/printing.bigraph-signature"));
        DefaultDynamicSignature signature = sxl.importObject();
        assertEquals(5, signature.getControls().size());
        List<String> requiredControlLabels = Arrays.asList("Job", "Spool", "Printer", "User", "Room");
        assertTrue(
                signature.getControls().stream().map(x -> x.getNamedType().stringValue()).collect(Collectors.toList())
                        .containsAll(requiredControlLabels)
        );
    }

    @Test
    void read_agents_test() throws IOException {
        DefaultBigraphXMLLoader bxl = new DefaultBigraphXMLLoader();
        bxl.readXml(getFile("agents/simple.bigraph-agent"));
        PureBigraph simpleBigraph = bxl.importObject();
        BigraphGraphvizExporter.toPNG(simpleBigraph, true, (new File(DUMP_TARGET + "simpleBigraph.png")));

        bxl.readXml(getFile("agents/complex.bigraph-agent"));
        PureBigraph complexBigraph = bxl.importObject();
        BigraphGraphvizExporter.toPNG(complexBigraph, true, (new File(DUMP_TARGET + "complexBigraph.png")));

        BigraphFileModelManagement.Store.exportAsInstanceModel(complexBigraph, new FileOutputStream(DUMP_TARGET + "complexBigraph.xmi"));
    }

    @Test
    void read_rule_test_FinishJob() throws InvalidReactionRuleException, IOException {
        DefaultReactionRuleXMLLoader rxl = new DefaultReactionRuleXMLLoader();
        rxl.readXml(getFile("rules/finish-job.bigraph-rule"));
        ParametricReactionRule<PureBigraph> finishJob = rxl.importObject();
        BigraphGraphvizExporter.toPNG(finishJob.getRedex(), true, (new File(DUMP_TARGET + "finish-job-redex.png")));
        BigraphGraphvizExporter.toPNG(finishJob.getReactum(), true, (new File(DUMP_TARGET + "finish-job-reactum.png")));
    }

    @Test
    void read_rule_test_JobToPrinter() throws InvalidReactionRuleException, IOException {
        DefaultReactionRuleXMLLoader rxl = new DefaultReactionRuleXMLLoader();
        rxl.readXml(getFile("rules/job-to-printer.bigraph-rule"));
        ParametricReactionRule<PureBigraph> finishJob = rxl.importObject();
        BigraphGraphvizExporter.toPNG(finishJob.getRedex(), true, (new File(DUMP_TARGET + "job-to-printer-redex.png")));
        BigraphGraphvizExporter.toPNG(finishJob.getReactum(), true, (new File(DUMP_TARGET + "job-to-printer-reactum.png")));
        BigraphFileModelManagement.Store.exportAsInstanceModel(finishJob.getRedex(), new FileOutputStream(DUMP_TARGET + "job-to-printer-redex.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel(finishJob.getReactum(), new FileOutputStream(DUMP_TARGET + "job-to-printer-reactum.xmi"));
    }

    @Test
    void read_rule_test_MoveRoom() throws InvalidReactionRuleException, IOException {
        DefaultReactionRuleXMLLoader rxl = new DefaultReactionRuleXMLLoader();
        rxl.readXml(getFile("rules/move-room.bigraph-rule"));
        ParametricReactionRule<PureBigraph> moveRoom = rxl.importObject();
        BigraphGraphvizExporter.toPNG(moveRoom.getRedex(), true, (new File(DUMP_TARGET + "move-room-redex.png")));
        BigraphGraphvizExporter.toPNG(moveRoom.getReactum(), true, (new File(DUMP_TARGET + "move-room-reactum.png")));
        BigraphFileModelManagement.Store.exportAsInstanceModel(moveRoom.getRedex(), new FileOutputStream(DUMP_TARGET + "move-room-redex.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel(moveRoom.getReactum(), new FileOutputStream(DUMP_TARGET + "move-room-reactum.xmi"));
    }

    @Test
    void read_rule_test_SubmitJob() throws InvalidReactionRuleException, IOException {
        DefaultReactionRuleXMLLoader rxl = new DefaultReactionRuleXMLLoader();
        rxl.readXml(getFile("rules/submit-job.bigraph-rule"));
        ParametricReactionRule<PureBigraph> submitJob = rxl.importObject();
        BigraphGraphvizExporter.toPNG(submitJob.getRedex(), true, (new File(DUMP_TARGET + "submit-job-redex.png")));
        BigraphGraphvizExporter.toPNG(submitJob.getReactum(), true, (new File(DUMP_TARGET + "submit-job-reactum.png")));
        BigraphFileModelManagement.Store.exportAsInstanceModel(submitJob.getRedex(), new FileOutputStream(DUMP_TARGET + "submit-job-redex.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel(submitJob.getReactum(), new FileOutputStream(DUMP_TARGET + "submit-job-reactum.xmi"));
    }

    @Test
    void read_simulationSpec_test() throws IOException {
        PureReactiveSystemStub pureReactiveSystem = new PureReactiveSystemStub();
        DefaultSimulationSpecXMLLoader ssxl = new DefaultSimulationSpecXMLLoader(pureReactiveSystem);
        ssxl.readXml(getFile("simple.bigraph-simulation-spec"));
        pureReactiveSystem = (PureReactiveSystemStub) ssxl.importObject();
        assertEquals(4, pureReactiveSystem.getReactionRules().size());
        assertNotNull(pureReactiveSystem.getAgent());
    }
}
