package de.tudresden.inf.st.bigraphs.converter.rankedgraph;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.converter.gxl.GraphMLDomBuilder;
import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.visualization.BigraphRankedGraphExporter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Place graph hierarchy is encoded as attribute "parent"
// hierarchy is als reflected by the id of the nodes (to maintain the parent relationship)

//DPO rule: attribute count muss übereinstimmen von agent und RR, dürfen nicht leer sein sonst segfault in GMTE
//das gilt nur für nodes, bei edges ist es scheinbar egal

//(siehe: http://homepages.laas.fr/khalil/GMTE/index.php?n=GMTE.Tutorials)
//der redex in inv zone

/**
 * @author Dominik Grzelak
 */
public class RankedGraphEncodingTests {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/rankedgraphs/";
    private PureBigraphFactory factory = pure();

    @Test
    void name2() throws TypeNotExistsException, InvalidConnectionException, IOException {
        PureBigraph bigraph = createBigraphA();
//        BigraphGraphvizExporter.toPNG(bigraph, true, new File("bigraph-example.png"));
        PureBigraphRankedGraphEncoding graphEncoding = new PureBigraphRankedGraphEncoding(bigraph);
//        graphEncoding.encode();
//        Graph<AbstractRankedGraph.LabeledNode, AbstractRankedGraph.LabeledEdge> graph = graphEncoding.getGraph();
//        drawGraph(graphEncoding.getGraph());
        new BigraphRankedGraphExporter().toPNG(graphEncoding, new File(TARGET_TEST_PATH + "graph2.png"));
    }

    private void drawGraph(Graph graph) throws IOException {
        JGraphXAdapter graphAdapter = new JGraphXAdapter(graph);

//        mxHierarchicalLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.WEST);
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.SOUTH);
        ((mxHierarchicalLayout) layout).setFineTuning(true);
        ((mxHierarchicalLayout) layout).setResizeParent(true);
        graphAdapter.setResetEdgesOnMove(true);
        graphAdapter.setResetEdgesOnResize(true);
        graphAdapter.setResetEdgesOnConnect(true);

        graphAdapter.getModel().beginUpdate();
        layout.execute(graphAdapter.getDefaultParent());
        Object[] ver = graphAdapter.getChildCells(graphAdapter.getDefaultParent());
        DoubleSummaryStatistics averagePosition = Arrays.stream(ver).mapToDouble(x -> graphAdapter.getBoundingBox(x).getX()).summaryStatistics();
        DoubleSummaryStatistics averageHeight = Arrays.stream(ver).mapToDouble(x -> graphAdapter.getBoundingBox(x).getHeight()).summaryStatistics();

        Object[] filteredVariables = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isVariableNode())
                .toArray(Object[]::new);
        Object[] filteredRoots = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isRootNode())
                .toArray(Object[]::new);
        Object[] filteredLinkNodes = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isLinkNode())
                .toArray(Object[]::new);
        Object[] filteredPlaceNodes = Arrays.stream(ver).map(x -> (mxCell) x)
                .filter(x -> x.isVertex() && ((AbstractRankedGraph.LabeledNode) x.getValue()).isPlaceNode())
                .toArray(Object[]::new);
        Object[] filteredEdgesForInterface = Arrays.stream(ver).map(x -> (mxCell) x).filter(mxCell::isEdge)
                .filter(x -> {
                    AbstractRankedGraph.LabeledNode source = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getSource();
                    return source.isInterfaceNode();
                })
                .toArray(Object[]::new);
        Object[] filteredEdgesForNodes = Arrays.stream(ver).map(x -> (mxCell) x).filter(mxCell::isEdge)
                .filter(x -> {
                    AbstractRankedGraph.LabeledNode source = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getSource();
                    AbstractRankedGraph.LabeledNode target = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getTarget();
                    return !source.isInterfaceNode() && target.isLinkNode();
                })
                .toArray(Object[]::new);
        Object[] filteredEdgesForNodes2Node = Arrays.stream(ver).map(x -> (mxCell) x).filter(mxCell::isEdge)
                .filter(x -> {
                    AbstractRankedGraph.LabeledNode source = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getSource();
                    AbstractRankedGraph.LabeledNode target = (AbstractRankedGraph.LabeledNode) ((AbstractRankedGraph.LabeledEdge) x.getValue()).getTarget();
                    return source.isPlaceNode() && target.isPlaceNode();
                })
                .toArray(Object[]::new);

        graphAdapter.setCellStyle("shape=ellipse;fontColor=green;strokeColor=green;fillColor=white", filteredLinkNodes);
        graphAdapter.setCellStyle("shape=ellipse;fillColor=none;strokeColor=black;fontColor=black", filteredPlaceNodes);
        graphAdapter.setCellStyle("strokeWidth=0;fillColor=none;strokeColor=none", filteredRoots);
        graphAdapter.setCellStyle("strokeWidth=0;fillColor=none;strokeColor=none", filteredVariables);


        graphAdapter.setCellStyle("dashed=1;strokeColor=black", filteredEdgesForInterface);
        graphAdapter.setCellStyle("strokeColor=green", filteredEdgesForNodes);
        graphAdapter.setCellStyle("strokeColor=black", filteredEdgesForNodes2Node);

//        graphAdapter.moveCells(filteredVariables, 0, doubleSummaryStatistics2.getMax(), false);
        graphAdapter.alignCells("bottom", filteredVariables);
        graphAdapter.alignCells("top", filteredRoots);
//        graphAdapter.
        DoubleSummaryStatistics rootInterfacePositionEstimate = Arrays.stream(filteredRoots)
                .mapToDouble(x -> graphAdapter.getBoundingBox(x).getX()).summaryStatistics();
        double newPosRoot = rootInterfacePositionEstimate.getAverage() - averagePosition.getAverage() * averageHeight.getAverage();
        graphAdapter.moveCells(filteredRoots, 0, newPosRoot * 2, false);
//        graphAdapter.getM
        graphAdapter.getModel().endUpdate();


        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        File imgFile = new File(TARGET_TEST_PATH + "graph.png");
//        imgFile.createNewFile();
        ImageIO.write(image, "PNG", imgFile);

        assertTrue(imgFile.exists());
    }

    @Test
    void name() throws InvalidConnectionException, TypeNotExistsException, IOException {
        PureBigraph bigraph = createBigraphA();

        Map<BigraphEntity, String> idMap = new HashMap<>();
        Traverser<BigraphEntity<?>> childrenTraverser = Traverser.forTree(x -> {
            if (BigraphEntityType.isNode(x)) {
                String n = ((BigraphEntity.NodeEntity) x).getName() + ":" + idMap.get(bigraph.getParent(x));
                idMap.put(x, n);
            }
            if (BigraphEntityType.isRoot(x)) {
                idMap.put(x, String.valueOf(((BigraphEntity.RootEntity) x).getIndex()));
            }
            Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(x);
            return childrenOf;
        });


        GraphMLDomBuilder.buildtop();

        BigraphEntity.RootEntity firstRoot = bigraph.getRoots().iterator().next();
        //TODO: addNode with attributes root,index,...
        // kann gleiche node-"id" für site und root verwendet werden? - ja: GMTE erzeugt sowieso neue ids aber beachtet diese nodes

        //build id concat with map?
        ArrayList<BigraphEntity<?>> bigraphEntities = Lists.newArrayList(childrenTraverser.breadthFirst(firstRoot));
        for (BigraphEntity<?> each : bigraphEntities) {
            System.out.println(each);
            switch (each.getType()) {
                case NODE:
                    String id = idMap.get(each);
                    String value = each.getControl().getNamedType().stringValue();
                    Element nodeElem = GraphMLDomBuilder.addNode(id, each.getType().name(), value);
                    if (bigraph.getParent(each).getType() == BigraphEntityType.ROOT) {
                        String targetId0 = "" + ((BigraphEntity.RootEntity) bigraph.getParent(each)).getIndex();
                        GraphMLDomBuilder.addAttributeToNode(nodeElem, "parent", targetId0);
//                        GraphMLDomBuilder.addEdge("outerface", id, targetId0);
                    } else if (bigraph.getParent(each).getType() == BigraphEntityType.NODE) {
                        GraphMLDomBuilder.addAttributeToNode(nodeElem, "parent", idMap.get(bigraph.getParent(each)));
                    }
                    break;
                case ROOT:
                    String id2 = String.valueOf(((BigraphEntity.RootEntity) each).getIndex());
                    GraphMLDomBuilder.addNode(id2, each.getType().name(), null);
                    break;
                case SITE:
                    String id3 = String.valueOf(((BigraphEntity.SiteEntity) each).getIndex());
                    Element nodeElem2 = GraphMLDomBuilder.addNode(id3, each.getType().name(), null);
                    String sourceId = "" + ((BigraphEntity.SiteEntity) each).getIndex();
                    if (bigraph.getParent(each).getType() == BigraphEntityType.ROOT) {
//                        GraphMLDomBuilder.addEdge("outerface", sourceId, "" + ((BigraphEntity.RootEntity) bigraph.getParent(each)).getIndex());
                        GraphMLDomBuilder.addAttributeToNode(nodeElem2, "parent", "" + ((BigraphEntity.RootEntity) bigraph.getParent(each)).getIndex());
                    } else if (bigraph.getParent(each).getType() == BigraphEntityType.NODE) {
//                        GraphMLDomBuilder.addEdge("innerface", sourceId, idMap.get(bigraph.getParent(each)));
                        GraphMLDomBuilder.addAttributeToNode(nodeElem2, "parent", idMap.get(bigraph.getParent(each)));
                    }
                    break;
            }

            //TODO if site addNode, addEdge to Parent
            //TODO if node: addNode,
            //TODO: if parent is root: addEdge
        }
        int i_interfaceCounter = bigraph.getSites().size();
        for (BigraphEntity.InnerName eachInner : bigraph.getInnerNames()) {
            Element nodeElem = GraphMLDomBuilder.addNode("" + (++i_interfaceCounter), eachInner.getType().name(), null);
        }

        printAll(GraphMLDomBuilder.document);

    }

    public static void printAll(Document doc) {
        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, System.out);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write the xml document into file
     *
     * @param file the file name
     * @param doc  xml document
     */
    public static void save(String file, Document doc) {
        System.out.println("### document saved in : " + file);
        try {
            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
            sortie.output(doc, new java.io.FileOutputStream(file));
        } catch (java.io.IOException e) {
        }
    }


    private PureBigraph createBigraphA() throws InvalidConnectionException, TypeNotExistsException, IOException {
        PureBigraphBuilder<DefaultDynamicSignature> builder;
        Signature<DefaultDynamicControl> signature;
        signature = createExampleSignature();
        builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName x0 = builder.createInnerName("x0");
        BigraphEntity.InnerName e0 = builder.createInnerName("e0");
        BigraphEntity.InnerName e1 = builder.createInnerName("e1");

        builder.createRoot()
                .addChild("K").linkToInner(e0).down().addChild("K").linkToInner(e0).down().addSite().up().up()
                .addChild("M").linkToInner(e0).linkToInner(e1);
        builder.createRoot().addChild("L").linkToInner(e1).linkToInner(x0).down().addSite();
        builder.closeInnerName(e0);
        builder.closeInnerName(e1);

        PureBigraph bigraph = builder.createBigraph();

        BigraphArtifacts.exportAsInstanceModel(bigraph, new FileOutputStream(TARGET_TEST_PATH + "test_instance-model.xmi"));

        return bigraph;
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("M")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("K")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("L")).arity(FiniteOrdinal.ofInteger(2)).assign()
        ;

        return (S) signatureBuilder.create();
    }
}
