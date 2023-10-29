package org.bigraphs.framework.converter.ecore;

import org.bigraphs.framework.converter.ReactiveSystemPrettyPrinter;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.io.IOException;
import java.io.OutputStream;

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
