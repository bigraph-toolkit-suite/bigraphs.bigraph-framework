package de.tudresden.inf.st.bigraphs.converter.ecore;

import de.tudresden.inf.st.bigraphs.converter.ReactiveSystemPrettyPrinter;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.impl.PureReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dominik Grzelak
 */
public class EcoreConverter extends EcoreAgentConverter implements ReactiveSystemPrettyPrinter<PureBigraph, PureReactiveSystem> {
    public EcoreConverter() {
        super();
    }

    public EcoreConverter(BigraphArtifacts.Format exportFormat) {
        super(exportFormat);
    }

    @Override
    public String toString(PureReactiveSystem system) {
        return super.toString(system.getAgent());
    }

    @Override
    public void toOutputStream(PureReactiveSystem system, OutputStream outputStream) throws IOException {
        super.toOutputStream(system.getAgent(), outputStream);
        return;
    }
}
