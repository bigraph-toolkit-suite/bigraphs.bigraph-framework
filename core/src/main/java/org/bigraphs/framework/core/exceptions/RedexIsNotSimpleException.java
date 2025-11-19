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
 * Exception that indicates that a redex of a parametric reaction rule is not simple.
 *
 * @author Dominik Grzelak
 */
public class RedexIsNotSimpleException extends InvalidReactionRuleException {

    public RedexIsNotSimpleException() {
        super("The redex is not simple. It is either not open, guarding, or inner-injective.");
    }
}
