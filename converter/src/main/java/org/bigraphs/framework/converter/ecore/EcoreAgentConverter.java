/*
 * Copyright (c) 2020-2024 Bigraph Toolkit Suite Developers
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bigraphs.framework.converter.BigraphPrettyPrinter;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.impl.pure.PureBigraph;

/**
 * @author Dominik Grzelak
 */
public class EcoreAgentConverter implements BigraphPrettyPrinter<PureBigraph> {

    private BigraphFileModelManagement.Format exportFormat;

    public EcoreAgentConverter() {
        exportFormat = BigraphFileModelManagement.Format.XMI;
    }

    public EcoreAgentConverter(BigraphFileModelManagement.Format exportFormat) {
        this.exportFormat = exportFormat;
    }

    public EcoreAgentConverter withExportFormat(BigraphFileModelManagement.Format exportFormat) {
        this.exportFormat = exportFormat;
        return this;
    }

    public BigraphFileModelManagement.Format getExportFormat() {
        return exportFormat;
    }

    /**
     * Throws a runtime exception if the encoding fails.
     *
     * @param bigraph the bigraph to encode
     * @return the bigraph in a textual form
     */
    @Override
    public String toString(PureBigraph bigraph) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            toOutputStream(bigraph, outputStream);
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toOutputStream(PureBigraph bigraph, OutputStream outputStream) throws IOException {
        switch (exportFormat) {
            case ECORE:
                BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, outputStream);
                break;
            case XMI:
            default:
                BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, outputStream);
                break;
        }
    }
}
