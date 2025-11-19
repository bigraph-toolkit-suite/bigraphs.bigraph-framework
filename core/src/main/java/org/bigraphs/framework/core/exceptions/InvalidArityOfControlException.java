/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.exceptions;

/**
 * Exception that is thrown with operations on controls where the arity must be considered, e.g., connecting
 * links to a node with no free ports.
 *
 * @author Dominik Grzelak
 */
public class InvalidArityOfControlException extends InvalidConnectionException {

    public InvalidArityOfControlException() {
        super("Arity of control doesn't match the current node's arity.");
    }

    protected InvalidArityOfControlException(String message) {
        super(message);
    }
}
