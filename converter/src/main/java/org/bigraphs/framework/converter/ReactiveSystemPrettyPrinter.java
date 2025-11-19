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
package org.bigraphs.framework.converter;

import java.io.IOException;
import java.io.OutputStream;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;

/**
 * A generic pretty printer interface for bigraphical reactive systems.
 *
 * @param <R> type of the reactive system
 * @author Dominik Grzelak
 */
public interface ReactiveSystemPrettyPrinter<B extends Bigraph<? extends Signature<?>>, R extends ReactiveSystem> extends PrettyPrinter<B> {
    /**
     * Returns the result of a reactive system encoding as string.
     *
     * @param system the reactive system being encoded
     * @return
     */
    String toString(R system);

    /**
     * Redirects the result of an encoding to an output stream.
     *
     * @param system       the reactive system being encoded
     * @param outputStream the output stream where the result shall be written to
     * @throws IOException because of the stream
     */
    void toOutputStream(R system, OutputStream outputStream) throws IOException;
}
