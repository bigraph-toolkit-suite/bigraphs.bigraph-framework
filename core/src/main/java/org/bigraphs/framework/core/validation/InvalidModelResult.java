/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.validation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InvalidModelResult implements BModelValidationResult {

    List<Exception> ex;

    public InvalidModelResult(Exception e) {
        this(Collections.singletonList(e));
    }

    public InvalidModelResult(List<Exception> ex) {
        this.ex = new LinkedList<>(ex);
    }

    public List<Exception> getExceptions() {
        return ex;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
