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
package org.bigraphs.framework.core.exceptions.builder;

import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidArityOfControlException;

/**
 * Exception is thrown inside a builder when the node has no free ports but a connection to a link (i.e., edge or outer name)
 * is tried to make.
 * <p>
 * The exception is not thrown if a control is atomic (see {@link ControlIsAtomicException}).
 *
 * @author Dominik Grzelak
 * @see ControlIsAtomicException
 */
public class ToManyConnections extends InvalidArityOfControlException {

    public ToManyConnections() {
        super("To many connections on this node");
    }
}
