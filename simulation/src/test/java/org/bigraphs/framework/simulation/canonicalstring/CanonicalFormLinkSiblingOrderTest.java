/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.canonicalstring;

import static org.bigraphs.framework.core.factory.BigraphFactory.pureBuilder;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.junit.jupiter.api.Test;

/**
 * Two empty {@code Door} siblings are place-identical. Their order must follow
 * the place-structure of the rooms they connect, not the instance edge names
 * assigned by a rewrite.
 */
class CanonicalFormLinkSiblingOrderTest {

    @Test
    void isomorphicDoorLayoutsShareACanonicalForm() throws Exception {
        PureBigraph first = building(false);
        PureBigraph second = building(true);

        BigraphCanonicalForm form = BigraphCanonicalForm.createInstance();
        String a = form.bfcs(first);
        String b = form.bfcs(second);
        assertEquals(a, b, () -> "expected isomorphic buildings to share a BFCS\nfirst=" + a + "\nsecond=" + b);
    }

    @Test
    void allocatingTheOtherDoorProducesADifferentCanonicalForm() throws Exception {
        BigraphCanonicalForm form = BigraphCanonicalForm.createInstance();
        assertNotEquals(form.bfcs(building(false)), form.bfcs(buildingAllocatedTowardA()));
    }

    /**
     * {@code A --door-- Robot --allocated-door-- B --door-- empty}.
     * {@code swapEmptyDoors} emits the two empty doors in opposite child order
     * and uses different inner-name labels.
     */
    private PureBigraph building(boolean swapEmptyDoors)
            throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        String p = swapEmptyDoors ? "x" : "e";
        BigraphEntity.InnerName ar0 = b.createInner(p + "AR0");
        BigraphEntity.InnerName ar1 = b.createInner(p + "AR1");
        BigraphEntity.InnerName rb0 = b.createInner(p + "RB0");
        BigraphEntity.InnerName rb1 = b.createInner(p + "RB1");
        BigraphEntity.InnerName be0 = b.createInner(p + "BE0");
        BigraphEntity.InnerName be1 = b.createInner(p + "BE1");
        BigraphEntity.InnerName claim = b.createInner(p + "Claim");

        PureBigraphBuilder<DynamicSignature>.Hierarchy root = b.root()
                .child("Room").down().child("A").child("Right").linkInner(ar1).up()
                .child("Room").down()
                    .child("Robot").down().child("Allocated").linkInner(claim).up()
                    .child("Left").linkInner(ar0)
                    .child("Right").linkInner(rb1)
                .up()
                .child("Room").down()
                    .child("B")
                    .child("Left").linkInner(rb0)
                    .child("Right").linkInner(be1)
                .up()
                .child("Room").down().child("Left").linkInner(be0).up();

        if (swapEmptyDoors) {
            root.child("Door").linkInner(be0).linkInner(be1)
                    .child("Door").linkInner(ar0).linkInner(ar1)
                    .child("Door").down().child("Allocated").linkInner(claim).up()
                            .linkInner(rb0).linkInner(rb1);
        } else {
            root.child("Door").linkInner(ar0).linkInner(ar1)
                    .child("Door").down().child("Allocated").linkInner(claim).up()
                            .linkInner(rb0).linkInner(rb1)
                    .child("Door").linkInner(be0).linkInner(be1);
        }

        b.closeInner(ar0, ar1, rb0, rb1, be0, be1, claim);
        return b.create();
    }

    private PureBigraph buildingAllocatedTowardA()
            throws InvalidConnectionException, TypeNotExistsException {
        PureBigraphBuilder<DynamicSignature> b = pureBuilder(signature());
        BigraphEntity.InnerName ar0 = b.createInner("eAR0");
        BigraphEntity.InnerName ar1 = b.createInner("eAR1");
        BigraphEntity.InnerName rb0 = b.createInner("eRB0");
        BigraphEntity.InnerName rb1 = b.createInner("eRB1");
        BigraphEntity.InnerName be0 = b.createInner("eBE0");
        BigraphEntity.InnerName be1 = b.createInner("eBE1");
        BigraphEntity.InnerName claim = b.createInner("eClaim");

        b.root()
                .child("Room").down().child("A").child("Right").linkInner(ar1).up()
                .child("Room").down()
                    .child("Robot").down().child("Allocated").linkInner(claim).up()
                    .child("Left").linkInner(ar0)
                    .child("Right").linkInner(rb1)
                .up()
                .child("Room").down()
                    .child("B")
                    .child("Left").linkInner(rb0)
                    .child("Right").linkInner(be1)
                .up()
                .child("Room").down().child("Left").linkInner(be0).up()
                .child("Door").down().child("Allocated").linkInner(claim).up()
                        .linkInner(ar0).linkInner(ar1)
                .child("Door").linkInner(rb0).linkInner(rb1)
                .child("Door").linkInner(be0).linkInner(be1);

        b.closeInner(ar0, ar1, rb0, rb1, be0, be1, claim);
        return b.create();
    }

    private DynamicSignature signature() {
        return (DynamicSignature) pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Door")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Left")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Right")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Robot")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("Allocated")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .create();
    }
}
