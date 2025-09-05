package org.bigraphs.framework.core;

import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.model.bigraphBaseModel.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class NodeAttributeUnitTest {


    @Test
    void attribute_read_write_test() throws IOException {
        DynamicSignature sig = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(sig);
        PureBigraph bigraph = b.root()
                .child("A")
                .child("B")
                .child("C")
                .create();
        BigraphEntity.NodeEntity<DynamicControl> v2 = bigraph.getNodes().stream()
                .filter(x -> x.getName().equals("v2")).findAny().get();
        Map<String, Object> attributes = v2.getAttributes();
        attributes.put("data", 1309);
        v2.setAttributes(attributes);
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
    }

    @Test
    void api_test_01() {
        BBigraph bBigraph = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        BRoot bRoot = BigraphBaseModelFactory.eINSTANCE.createBRoot();
        bBigraph.getBRoots().add(bRoot);

        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        BBigraph bBigraph2 = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        bBigraph2.getBRoots().add(bRoot);

        BEdge edge2 = BigraphBaseModelFactory.eINSTANCE.createBEdge();
        BPort port21 = BigraphBaseModelFactory.eINSTANCE.createBPort();
        BPort port22 = BigraphBaseModelFactory.eINSTANCE.createBPort();
        edge2.getBPoints().add(port21);
        edge2.getBPoints().add(port22);
        BNode node2 = BigraphBaseModelFactory.eINSTANCE.createBNode();
        node2.getAttributes().put("key", 123);
        node2.getAttributes().put("key2", "ABC");
        node2.getBPorts().add(port21);
        node2.getBPorts().add(port22);
        bRoot.getBChild().add(node2);
        node2.setName("A");
//        EcoreUtil.setConstraints();

        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        System.out.println(bBigraph2.getBRoots().size());
    }

    private static DynamicSignature createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .add("A", 0)
                .add("B", 0)
                .add("C", 0)

        ;
        return signatureBuilder.create();
    }
}
