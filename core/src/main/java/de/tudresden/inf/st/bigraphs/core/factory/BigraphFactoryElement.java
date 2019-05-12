package de.tudresden.inf.st.bigraphs.core.factory;

import java.lang.reflect.Type;

//for chain of responsibility later used with a "factorybuilder"
public interface BigraphFactoryElement {

    Type getSuccessorImpl();
}
