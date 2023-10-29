package org.bigraphs.framework.converter.ecore;

import org.bigraphs.framework.converter.BigraphPrettyPrinter;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
