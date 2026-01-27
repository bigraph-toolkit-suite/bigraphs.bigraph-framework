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
package org.bigraphs.framework.simulation.matching.pure;

import it.uniud.mads.jlibbig.core.std.Match;
import java.util.Collection;
import java.util.Collections;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.AbstractSimpleReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;

/**
 * This class represents the result of a valid match.
 * It contains all required elements to perform the rewriting step later.
 *
 * @author Dominik Grzelak
 * @see AbstractSimpleReactiveSystem
 */
public class PureBigraphMatch implements BigraphMatch<PureBigraph> {

    private PureBigraph context;
    private PureBigraph redex;
    private PureBigraph redexImage;
    private final Collection<PureBigraph> parameters;
    private Bigraph<DynamicSignature> redexIdentity;
    private Bigraph<DynamicSignature> contextIdentity;
    private PureBigraph paramWiring;
    private PureBigraph params;
    private final it.uniud.mads.jlibbig.core.std.Match jLibMatchResult;

    public PureBigraphMatch(it.uniud.mads.jlibbig.core.std.Match jLibMatchResult,
                            PureBigraph context,
                            PureBigraph redex,
                            PureBigraph redexImage,
                            Bigraph<DynamicSignature> redexIdentity,
                            PureBigraph paramWiring,
                            Collection<PureBigraph> parameters) {
        // Everything is null exceptjLibMatchResult and redex because we have jLibBig bigraph objects
        // Until the bigraph is rewritten with the match result, they will be null.
        this.jLibMatchResult = jLibMatchResult;
        this.contextIdentity = null;
        this.redexIdentity = redexIdentity;
        this.redex = redex;
        this.context = context;
        this.redexImage = redexImage;
        this.paramWiring = paramWiring;
        this.parameters = parameters;
        this.params = null;
    }

    @Deprecated
    public PureBigraphMatch(PureBigraph context,
                            PureBigraph redex,
                            PureBigraph redexImage,
                            Collection<PureBigraph> parameters,
                            Bigraph<DynamicSignature> redexIdentity,
                            Bigraph<DynamicSignature> contextIdentity) {
        this.jLibMatchResult = null;
        this.parameters = parameters;
        this.context = context;
        this.redexImage = redexImage;
        this.redexIdentity = redexIdentity;
        this.redex = redex;
        this.contextIdentity = contextIdentity;
    }

    public Match getJLibMatchResult() {
        return jLibMatchResult;
    }

    @Override
    public Collection getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    @Override
    public PureBigraph getParam() {
        return params;
    }


    public void setParam(PureBigraph param) {
        params = param;
    }

    @Override
    public boolean wasRewritten() {
        return context != null && params != null && redexImage != null;
    }

    @Override
    public PureBigraph getContext() {
        return context;
    }

    public void setContext(PureBigraph context) {
        this.context = context;
    }

    /**
     * <b>Note:</b> The return type is of class {@link Bigraph} with a {@link DynamicSignature}. We cannot cast
     * it to a pure bigraph because it may also be an elementary bigraph (in the form of a pure bigraph type)
     *
     * @return the identity link graph
     */
    @Override
    public Bigraph<DynamicSignature> getContextIdentity() {
        return contextIdentity;
    }

    @Override
    public PureBigraph getRedex() {
        return redex;
    }

    @Override
    public PureBigraph getRedexImage() {
        return redexImage;
    }

    public void setRedexImage(PureBigraph redexImage) {
        this.redexImage = redexImage;
    }

    @Override
    public Bigraph<DynamicSignature> getRedexIdentity() {
        return redexIdentity;
    }
}
