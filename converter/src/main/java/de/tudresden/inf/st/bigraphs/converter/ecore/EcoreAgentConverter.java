package de.tudresden.inf.st.bigraphs.converter.ecore;

import de.tudresden.inf.st.bigraphs.converter.BigraphPrettyPrinter;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dominik Grzelak
 */
public class EcoreAgentConverter implements BigraphPrettyPrinter<PureBigraph> {

    private BigraphArtifacts.Format exportFormat;

    public EcoreAgentConverter() {
        exportFormat = BigraphArtifacts.Format.XMI;
    }

    public EcoreAgentConverter(BigraphArtifacts.Format exportFormat) {
        this.exportFormat = exportFormat;
    }

    public EcoreAgentConverter withExportFormat(BigraphArtifacts.Format exportFormat) {
        this.exportFormat = exportFormat;
        return this;
    }

    public BigraphArtifacts.Format getExportFormat() {
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
                BigraphArtifacts.exportAsMetaModel(bigraph, outputStream);
                break;
            case XMI:
            default:
                BigraphArtifacts.exportAsInstanceModel(bigraph, outputStream);
                break;
        }
    }
}
