/*
 * Copyright (c) 2019-2026 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a {@link BigraphModelChecker} for model checking of BRS with pure bigraphs
 * (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureBigraphModelChecker extends BigraphModelChecker<PureBigraph> {

    private Logger logger = LoggerFactory.getLogger(BigraphModelChecker.class);

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, ModelCheckingOptions options) {
        super(reactiveSystem, options);
    }

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, ModelCheckingStrategy<PureBigraph> modelCheckingStrategy, ModelCheckingOptions options) {
        super(reactiveSystem, modelCheckingStrategy, options);
    }

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationStrategy.Type simulationStrategyType, ModelCheckingOptions options, ReactiveSystemListener<PureBigraph> listener) {
        super(reactiveSystem, simulationStrategyType, options, listener);
    }

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationStrategy.Type simulationStrategyType, ModelCheckingOptions options) {
        super(reactiveSystem, simulationStrategyType, options);
    }
}
