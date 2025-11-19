/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.simulation.examples.bigrid.legacy;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.EcoreBigraph;
import org.bigraphs.framework.core.ElementaryBigraph;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.eclipse.collections.impl.factory.Maps;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Deprecated
@Disabled
public class BigraphGridCompositionUnitTest {

    private static String BASEPATH = "src/test/resources/dump/bigrid/";

    @Test
    void test_01() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraph topLeft = topLeft();
        PureBigraph topEdge = topEdge();
        ElementaryBigraph<DynamicSignature> tl_te = TL_TE_Wiring();

        BigraphFileModelManagement.Store.exportAsInstanceModel(topLeft, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(topEdge, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(tl_te, System.out);

        Bigraph<DynamicSignature> _tc = ops(topLeft).parallelProduct(tl_te).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) _tc, System.out);
        Bigraph<DynamicSignature> _tl_te = ops(_tc).parallelProduct(topEdge).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) _tl_te, System.out);
    }

    @Test
    void test_02() throws Exception {
        PureBigraph topLeft = topLeft();
        PureBigraph topEdge = topEdge();
        ElementaryBigraph<DynamicSignature> tl_te_w = TL_TE_Wiring();
        ElementaryBigraph<DynamicSignature> merge1 = merge1();
        ElementaryBigraph<DynamicSignature> merge2 = merge2();
        ElementaryBigraph<DynamicSignature> renaming_tc = renaming("east", "south");

        // With intermediate steps
        Bigraph<DynamicSignature> _tc = ops(topLeft).parallelProduct(tl_te_w).getOuterBigraph();
        Bigraph<DynamicSignature> m2_id = ops(merge2).juxtapose(renaming_tc).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) m2_id, System.out);
        Bigraph<DynamicSignature> _tc2 = ops(_tc).juxtapose(merge1).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) _tc2, System.out);
        Bigraph<DynamicSignature> ob = ops(m2_id).compose(_tc2).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) ob, System.out);

//        // In one line:
//        Bigraph<DefaultDynamicSignature> result = ops(ops(merge2).juxtapose(renaming_tc).getOuterBigraph()).compose(ops(topLeft).parallelProduct(tl_te_w).juxtapose(merge1)).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result,alue Tree System.out);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) topEdge, System.out);
//
//        Bigraph<DefaultDynamicSignature> connected = ops(result).compose(topEdge).getOuterBigraph();
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) connected, System.out);
    }

    public Optional<Bigraph> createNorthSouthConnector(Bigraph<?> rowBigraph, Linkings<DynamicSignature> linkings) {
        AtomicInteger cnt = new AtomicInteger(0);
        return rowBigraph.getOuterNames().stream()
                .map(o -> {
                    int j = cnt.getAndIncrement();
                    if (o.getName().startsWith("south")) {
//                        return (Bigraph) linkings.substitution("ns" + (j + rowIx * (numOfCols - 1)), o.getName());
                        return (Bigraph) linkings.substitution("ns" + o.getName().replaceAll("[^0-9]", ""), o.getName());
                    } else if (o.getName().startsWith("north") /* && rowIx > 0 */) {
//                        return (Bigraph) linkings.substitution("ns" + (j + (rowIx - 1) * (numOfCols - 1)), o.getName());
                        return (Bigraph) linkings.substitution("ns" + o.getName().replaceAll("[^0-9]", ""), o.getName());
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .reduce((l, r) -> {
                    try {
                        return ops(l).juxtapose(r).getOuterBigraph();
                    } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                        e.printStackTrace();
                        throw new RuntimeException("");
                    }
                });
    }

    public Optional<Bigraph> createClosure(String name, int length, Bigraph<?> b, Linkings<DynamicSignature> linkings) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Set<NamedType<?>> names = IntStream.rangeClosed(0, length)
                .mapToObj(i -> (NamedType<?>) StringTypedName.of(name + i))
                .collect(Collectors.toSet());
        NamedType<?>[] rest = b.getOuterNames().stream()
                .filter(o -> !o.getName().startsWith(name))
                .map(o -> (NamedType<?>) StringTypedName.of(o.getName()))
                .toArray(StringTypedName[]::new);
        return Optional.of(ops(linkings.identity(rest)).juxtapose(linkings.closure(names)).getOuterBigraph());
    }


    public Optional<Bigraph> createRenaming(Map<String, String> renamings, PureBigraph previous, Linkings<DynamicSignature> linkings) {
        return previous.getOuterNames().stream()
                .map(x -> {
                    if (renamings.containsKey(x.getName())) {
                        return (Bigraph) linkings.substitution(renamings.get(x.getName()), x.getName());
                    } else {
                        return (Bigraph) linkings.substitution(x.getName(), x.getName());
                    }
                })
                .reduce((l, r) -> {
                    try {
                        return ops(l).juxtapose(r).getOuterBigraph();
                    } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                        throw new RuntimeException("");
                    }
                });
    }

    @Test
    @DisplayName("Create first row")
    void test_03() throws Exception {
        int n = 3;
        int i = 0;
        Linkings<DynamicSignature> linkings = pureLinkings(signature());
        Placings<DynamicSignature> placings = purePlacings(signature());
        PureBigraph previous = topLeft();

        Optional<Bigraph> reduce = createRenaming(
                Maps.immutable.of("east", "ew0", "south", "south0").castToMap(),
                previous, linkings
        );
        previous = (PureBigraph) ops(reduce.get()).compose(previous).getOuterBigraph();
        BigraphGraphvizExporter.toPNG(reduce.get(), true, new File(BASEPATH + "wiring_0.png"));
        BigraphGraphvizExporter.toPNG(previous, true, new File(BASEPATH + "d_" + i + "-" + 0 + ".png"));
        for (int j = 1; j < n; j++) {
            PureBigraph next = j != n - 1 ?
                    topEdge() :
                    topRight();
            BigraphGraphvizExporter.toPNG(next, true, new File(BASEPATH + "next" + i + "-" + j + ".png"));

            Optional<Bigraph> nextWiring = createRenaming(
                    Maps.immutable.of("east", "ew" + (j), "south", "south" + (j + i * (n - 1)), "west", "ew" + (j - 1)).castToMap(),
                    next,
                    linkings
            );

            BigraphGraphvizExporter.toPNG(nextWiring.get(), true, new File(BASEPATH + "wiring_" + j + ".png"));
            Bigraph<DynamicSignature> nextWithWiring = ops(nextWiring.get()).compose(next).getOuterBigraph();
            BigraphGraphvizExporter.toPNG(nextWithWiring, true, new File(BASEPATH + "nextWithWiring_" + i + "-" + j + ".png"));

            Bigraph<DynamicSignature> previousWithRegion = ops(previous).juxtapose(placings.identity1()).getOuterBigraph();
            Bigraph<DynamicSignature> outerBigraph = ops(previousWithRegion).nesting(nextWithWiring).getOuterBigraph();
            BigraphGraphvizExporter.toPNG(outerBigraph, true, new File(BASEPATH + "d_" + i + "-" + j + ".png"));
            System.out.println("\tFinished i,j = " + i + ", " + j);
            previous = (PureBigraph) outerBigraph;
        }

        Optional<Bigraph> ew = createClosure("ew", n - 2, previous, linkings);
        BigraphGraphvizExporter.toPNG(ew.get(), true, new File(BASEPATH + "cls_" + i + ".png"));
        Bigraph finalClosed = ops(ew.get()).compose(previous).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) finalClosed, System.out);
        BigraphGraphvizExporter.toPNG(finalClosed, true, new File(BASEPATH + "finalClosed_" + i + ".png"));
    }

    @Test
    @DisplayName("Create a middle row")
    void test_04() throws Exception {
        int n = 3;
        int i = 1;
        Linkings<DynamicSignature> linkings = pureLinkings(signature());
        Placings<DynamicSignature> placings = purePlacings(signature());
        PureBigraph previous = leftEdge();

        Optional<Bigraph> reduce = createRenaming(
                Maps.immutable.of(
                        "north", "north" + ((i - 1) * (n - 1)),
                        "east", "ew0",
                        "south", "south" + (i * (n - 1))).castToMap(),
                previous, linkings
        );
        previous = (PureBigraph) ops(reduce.get()).compose(previous).getOuterBigraph();
        BigraphGraphvizExporter.toPNG(reduce.get(), true, new File(BASEPATH + "wiring_0.png"));
        BigraphGraphvizExporter.toPNG(previous, true, new File(BASEPATH + "d_" + i + "-" + 0 + ".png"));

        for (int j = 1; j < n; j++) {

            PureBigraph next = j != n - 1 ?
                    center() :
                    rightEdge();
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) next, System.out);
            BigraphGraphvizExporter.toPNG(next, true, new File(BASEPATH + "next" + i + "-" + j + ".png"));

            Optional<Bigraph> nextWiring = createRenaming(
                    Maps.immutable.of(
                            "north", "north" + (j + (i - 1) * (n - 1)),
                            "east", "ew" + j,
                            "south", "south" + (j + i * (n - 1)),
                            "west", "ew" + (j - 1)).castToMap(),
                    next, linkings
            );

            BigraphGraphvizExporter.toPNG(nextWiring.get(), true, new File(BASEPATH + "nextWiring_" + i + "-" + j + ".png"));
            Bigraph<DynamicSignature> nextWithWiring = ops(nextWiring.get()).compose(next).getOuterBigraph();
            BigraphGraphvizExporter.toPNG(nextWithWiring, true, new File(BASEPATH + "nextWithWiring_" + i + "-" + j + ".png"));

            Bigraph<DynamicSignature> previousWithRegion = ops(previous).juxtapose(placings.identity1()).getOuterBigraph();
            Bigraph<DynamicSignature> d_ij = ops(previousWithRegion).nesting(nextWithWiring).getOuterBigraph();
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) d_ij, System.out);
            BigraphGraphvizExporter.toPNG(d_ij, true, new File(BASEPATH + "d_" + i + "-" + j + ".png"));
            System.out.println("\tFinished i,j = " + i + ", " + j);
            previous = (PureBigraph) d_ij;
        }

        Optional<Bigraph> ew = createClosure("ew", n - 2, previous, linkings);
        BigraphGraphvizExporter.toPNG(ew.get(), true, new File(BASEPATH + "cls_" + i + ".png"));
        Bigraph finalClosed = ops(ew.get()).compose(previous).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) finalClosed, System.out);
        BigraphGraphvizExporter.toPNG(finalClosed, true, new File(BASEPATH + "finalClosed_" + i + ".png"));
    }

    @Test
    @DisplayName("Create last row")
    void test_05() throws Exception {
        int n = 3;
        int i = 2;
        Linkings<DynamicSignature> linkings = pureLinkings(signature());
        Placings<DynamicSignature> placings = purePlacings(signature());
        PureBigraph previous = bottomLeft();

        Optional<Bigraph> reduce = createRenaming(
                Maps.immutable.of(
                        "north", "north" + ((i - 1) * (n - 1)),
                        "east", "ew0").castToMap(),
                previous, linkings
        );
        previous = (PureBigraph) ops(reduce.get()).compose(previous).getOuterBigraph();
        BigraphGraphvizExporter.toPNG(reduce.get(), true, new File(BASEPATH + "wiring_0.png"));
        BigraphGraphvizExporter.toPNG(previous, true, new File(BASEPATH + "d_" + i + "-" + 0 + ".png"));

        for (int j = 1; j < n; j++) {

            PureBigraph next = j != n - 1 ?
                    bottomEdge() :
                    bottomRight();

            BigraphGraphvizExporter.toPNG(next, true, new File(BASEPATH + "next" + i + "-" + j + ".png"));
            Optional<Bigraph> nextWiring = createRenaming(
                    Maps.immutable.of(
                            "north", "north" + (j + (i - 1) * (n - 1)),
                            "east", "ew" + j,
                            "west", "ew" + (j - 1)).castToMap(),
                    next, linkings
            );

            BigraphGraphvizExporter.toPNG(nextWiring.get(), true, new File(BASEPATH + "nextWiring_" + i + "-" + j + ".png"));
            Bigraph<DynamicSignature> nextWithWiring = ops(nextWiring.get()).compose(next).getOuterBigraph();
            BigraphGraphvizExporter.toPNG(nextWithWiring, true, new File(BASEPATH + "nextWithWiring_" + i + "-" + j + ".png"));

            Bigraph<DynamicSignature> previousWithRegion = ops(previous).juxtapose(placings.identity1()).getOuterBigraph();
            Bigraph<DynamicSignature> d_ij = ops(previousWithRegion).nesting(nextWithWiring).getOuterBigraph();
            BigraphGraphvizExporter.toPNG(d_ij, true, new File(BASEPATH + "d_" + i + "-" + j + ".png"));
            System.out.println("\tFinished i,j = " + i + ", " + j);
            previous = (PureBigraph) d_ij;
        }

        Optional<Bigraph> ew = createClosure("ew", n - 2, previous, linkings);
        BigraphGraphvizExporter.toPNG(ew.get(), true, new File(BASEPATH + "cls_" + i + ".png"));
        Bigraph finalClosed = ops(ew.get()).compose(previous).getOuterBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) finalClosed, System.out);
        BigraphGraphvizExporter.toPNG(finalClosed, true, new File(BASEPATH + "finalClosed_" + i + ".png"));
    }


    @Test
    public void test_BigridGenerator_01() throws IncompatibleInterfaceException, InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IOException {
        BigridGeneratorOld generator = new BigridGeneratorOld(null);
        PureBigraph generate = generator.generate(3, 3);

        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) generate, System.out);
        BigraphGraphvizExporter.toPNG(generate, true, new File(BASEPATH + "d_" + 0 + "-" + 2 + ".png"));
//        BigraphGraphvizExporter.toPNG(generate[0], true, new File(BASEPATH + "d_" + 0 + "-" + 2 + ".png"));
//        BigraphGraphvizExporter.toPNG(generate[1], true, new File(BASEPATH + "d_" + 1 + "-" + 2 + ".png"));
//        BigraphGraphvizExporter.toPNG(generate[2], true, new File(BASEPATH + "d_" + 2 + "-" + 2 + ".png"));


//        Linkings<DefaultDynamicSignature> linkings = pureLinkings(generator.getSignature());
//        Optional<Bigraph> wiringNS0 = createNorthSouthConnector(generate[0], linkings);
//        BigraphGraphvizExporter.toPNG(wiringNS0.get(), true, new File(BASEPATH + "ns_" + 0 + ".png"));
//        Optional<Bigraph> wiringNS1 = createNorthSouthConnector(generate[1], linkings);
//        BigraphGraphvizExporter.toPNG(wiringNS1.get(), true, new File(BASEPATH + "ns_" + 1 + ".png"));
//        Optional<Bigraph> wiringNS2 = createNorthSouthConnector(generate[2], linkings);
//        BigraphGraphvizExporter.toPNG(wiringNS2.get(), true, new File(BASEPATH + "ns_" + 2 + ".png"));

    }


    public ElementaryBigraph<DynamicSignature> merge1() {
        return purePlacings(signature())
                .merge(1);
    }

    public ElementaryBigraph<DynamicSignature> merge2() {
        return purePlacings(signature())
                .merge(2);
    }

    public ElementaryBigraph<DynamicSignature> TL_TE_Wiring() {

        return pureLinkings(signature())
                .substitution("east", "west");
    }

    public ElementaryBigraph<DynamicSignature> renaming(String... names) {
        return pureLinkings(signature())
                .identity(Arrays.stream(names).map(StringTypedName::of).toArray(StringTypedName[]::new));
    }

    public PureBigraph bottomLeft() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.OuterName north = b.createOuter("north");
        BigraphEntity.OuterName east = b.createOuter("east");
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        BigraphEntity.InnerName tmp2 = b.createInner("tmp2");
        b.root()
                .child("BottomLeftCorner")
                .linkOuter(north)
                .linkOuter(east)
                .linkInner(tmp)
                .linkInner(tmp2);
        b.closeInner(tmp); // leaves the edge intact
        b.closeInner(tmp2); // leaves the edge intact
        return b.create();
    }

    public PureBigraph bottomEdge() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.OuterName north = b.createOuter("north");
        BigraphEntity.OuterName east = b.createOuter("east");
        BigraphEntity.OuterName west = b.createOuter("west");
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        b.root()
                .child("BottomEdge")
                .linkOuter(north)
                .linkOuter(east)
                .linkInner(tmp)
                .linkOuter(west);
        b.closeInner(tmp); // leaves the edge intact
        return b.create();
    }

    public PureBigraph bottomRight() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.OuterName north = b.createOuter("north");
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        BigraphEntity.InnerName tmp2 = b.createInner("tmp2");
        BigraphEntity.OuterName west = b.createOuter("west");
        b.root()
                .child("BottomRightCorner")
                .linkOuter(north)
                .linkInner(tmp)
                .linkInner(tmp2)
                .linkOuter(west);
        b.closeInner(tmp); // leaves the edge intact
        b.closeInner(tmp2); // leaves the edge intact
        return b.create();
    }

    public PureBigraph topLeft() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        BigraphEntity.InnerName tmp2 = b.createInner("tmp2");
        BigraphEntity.OuterName east = b.createOuter("east");
        BigraphEntity.OuterName south = b.createOuter("south");
        b.root()
                .child("TopLeftCorner")
                .linkInner(tmp)
                .linkOuter(east)
                .linkOuter(south)
                .linkInner(tmp2);
        b.closeInner(tmp); // leaves the edge intact
        b.closeInner(tmp2); // leaves the edge intact
        return b.create();
    }

    public PureBigraph topRight() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        BigraphEntity.InnerName tmp2 = b.createInner("tmp2");
        BigraphEntity.OuterName west = b.createOuter("west");
        BigraphEntity.OuterName south = b.createOuter("south");
        b.root()
                .child("TopRightCorner")
                .linkInner(tmp)
                .linkInner(tmp2)
                .linkOuter(south)
                .linkOuter(west)
        ;
        b.closeInner(tmp); // leaves the edge intact
        b.closeInner(tmp2); // leaves the edge intact
        return b.create();
    }

    public PureBigraph leftEdge() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.OuterName north = b.createOuter("north");
        BigraphEntity.OuterName east = b.createOuter("east");
        BigraphEntity.OuterName south = b.createOuter("south");
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        b.root()
                .child("LeftEdge")
                .linkOuter(north)
                .linkOuter(east)
                .linkOuter(south)
                .linkInner(tmp);
        b.closeInner(tmp); // leaves the edge intact
        return b.create();
    }

    public PureBigraph rightEdge() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.OuterName north = b.createOuter("north");
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        BigraphEntity.OuterName south = b.createOuter("south");
        BigraphEntity.OuterName west = b.createOuter("west");
        b.root()
                .child("RightEdge")
                .linkOuter(north)
                .linkInner(tmp)
                .linkOuter(south)
                .linkOuter(west);
        b.closeInner(tmp); // leaves the edge intact
        return b.create();
    }

    public PureBigraph center() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.OuterName north = b.createOuter("north");
        BigraphEntity.OuterName east = b.createOuter("east");
        BigraphEntity.OuterName south = b.createOuter("south");
        BigraphEntity.OuterName west = b.createOuter("west");
        b.root()
                .child("Center")
                .linkOuter(north)
                .linkOuter(east)
                .linkOuter(south)
                .linkOuter(west);
        return b.create();
    }

    public PureBigraph topEdge() throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.InnerName tmp = b.createInner("tmp");
        BigraphEntity.OuterName east = b.createOuter("east");
        BigraphEntity.OuterName south = b.createOuter("south");
        BigraphEntity.OuterName west = b.createOuter("west");
        b.root()
                .child("TopEdge")
                .linkInner(tmp)
                .linkOuter(east)
                .linkOuter(south)
                .linkOuter(west);
        b.closeInner(tmp); // leaves the edge intact
        return b.create();
    }

    public DynamicSignature signature() {
        return pureSignatureBuilder()
                .newControl("TopLeftCorner", 4).assign()
                .newControl("TopEdge", 4).assign()
                .newControl("TopRightCorner", 4).assign()
                .newControl("LeftEdge", 4).assign()
                .newControl("Center", 4).assign()
                .newControl("RightEdge", 4).assign()
                .newControl("BottomLeftCorner", 4).assign()
                .newControl("BottomEdge", 4).assign()
                .newControl("BottomRightCorner", 4).assign()
                .create();
    }
}
