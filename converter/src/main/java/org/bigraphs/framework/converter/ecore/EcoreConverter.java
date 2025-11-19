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
package org.bigraphs.framework.converter.ecore;

import java.io.IOException;
import java.io.OutputStream;
import org.bigraphs.framework.converter.ReactiveSystemPrettyPrinter;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;

/**
 * @author Dominik Grzelak
 */
public class EcoreConverter extends EcoreAgentConverter implements ReactiveSystemPrettyPrinter<PureBigraph, ReactiveSystem<PureBigraph>> {
    public EcoreConverter() {
        super();
    }

    public EcoreConverter(BigraphFileModelManagement.Format exportFormat) {
        super(exportFormat);
    }

    @Override
    public String toString(ReactiveSystem<PureBigraph> system) {
        return super.toString(system.getAgent());
    }

    @Override
    public void toOutputStream(ReactiveSystem<PureBigraph> system, OutputStream outputStream) throws IOException {
        super.toOutputStream(system.getAgent(), outputStream);
        return;
    }
}
