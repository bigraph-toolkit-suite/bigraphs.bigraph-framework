/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import java.io.IOException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.eclipse.emf.compare.match.*;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.utils.UseIdentifiers;

/**
 * @author Dominik Grzelak
 */
public class CompareTest {

    public static void main(String[] args) throws IOException {
        DynamicSignature signature = pureSignatureBuilder().newControl("A", 1).assign()
                .newControl("B", 2).assign()
                .create();
        PureBigraphBuilder<DynamicSignature>.Hierarchy hierarchy = pureBuilder(signature)
                .root().child("A").child("B");
        PureBigraph bigraph = hierarchy.create();

        PureBigraphBuilder<DynamicSignature>.Hierarchy hierarchy2 = pureBuilder(signature)
                .root().child("A").child("A");
        PureBigraph bigraph2 = hierarchy2.create();


//        EObject copy = EcoreUtil.copy(bigraph.getModel());
        EcoreBigraph.Stub stub = new EcoreBigraph.Stub(bigraph);
        EcoreBigraph.Stub stub2 = new EcoreBigraph.Stub(bigraph2);

//        ResourceSet resourceSet1 = BigraphFileModelManagement.getResourceSetBigraphInstanceModel(
//                stub.getModelPackage(),
//                stub.getInputStreamOfInstanceModel());
//        ResourceSet resourceSet2 = BigraphFileModelManagement.getResourceSetBigraphInstanceModel(
//                stub2.getModelPackage(),
//                stub2.getInputStreamOfInstanceModel());
//        System.out.println(resourceSet1);
//        System.out.println(resourceSet2);


        IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);
        IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());

        IMatchEngine.Factory.Registry standaloneInstance = MatchEngineFactoryRegistryImpl.createStandaloneInstance();
        IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcher, comparisonFactory);
        matchEngineFactory.setRanking(10);
        IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
        matchEngineRegistry.add(matchEngineFactory);

//        EMFCompare comparator = EMFCompare.builder()
//                .setMatchEngineFactoryRegistry(matchEngineRegistry)
//                .build();


//        IComparisonScope scope = new DefaultComparisonScope(resourceSet1, resourceSet2, (Notifier)null); //EMFCompare.createDefaultScope(resourceSet1, resourceSet2);
//        Comparison comparison = comparator.compare(scope);
//        System.out.println(comparison);
//        List<Diff> differences = comparison.getDifferences();

//        Predicate<? super Diff> predicate = and(fromSide(DifferenceSource.LEFT), not(hasConflict(ConflictKind.REAL, ConflictKind.PSEUDO)));
// Filter out the differences that do not satisfy the predicate
//        Iterable<Diff> nonConflictingDifferencesFromLeft = filter(comparison.getDifferences(), predicate);
//        System.out.println(nonConflictingDiff/erencesFromLeft);
//        Iterator<Diff> iterator = nonConflictingDifferencesFromLeft.iterator();
//        while (iterator.hasNext()) {
//            Diff next = iterator.next();
//            System.out.println(next);
//        }

    }
}
