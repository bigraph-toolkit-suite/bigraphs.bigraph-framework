package de.tudresden.inf.st.bigraphs.store;

import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.server.IStore;
import org.eclipse.emf.cdo.server.db.CDODBUtil;
import org.eclipse.emf.cdo.server.db.mapping.IMappingStrategy;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.db.IDBAdapter;
import org.eclipse.net4j.db.IDBConnectionProvider;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.lifecycle.ILifecycle;
import org.eclipse.net4j.util.lifecycle.LifecycleEventAdapter;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.om.OMPlatform;
import org.eclipse.net4j.util.om.log.PrintLogHandler;
import org.eclipse.net4j.util.om.trace.PrintTraceHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphCDOTest {

    private static CDONet4jSession cdoSession;

    @BeforeAll
    public static void init() {
        OMPlatform.INSTANCE.setDebugging(true);
        OMPlatform.INSTANCE.addTraceHandler(PrintTraceHandler.CONSOLE);
        OMPlatform.INSTANCE.addLogHandler(PrintLogHandler.CONSOLE);

        //The following lines are not needed if the extension
        //registry (OSGi/Equinox) is running
        Net4jUtil.prepareContainer(IPluginContainer.INSTANCE); // Prepare the Net4j kernel
        TCPUtil.prepareContainer(IPluginContainer.INSTANCE); // Prepare the TCP support
        CDONet4jServerUtil.prepareContainer(IPluginContainer.INSTANCE); // Prepare the CDO server

        cdoSession = openSession("repo1");
        System.out.println(cdoSession.toString());
    }

    @Test
    void example() {
//        IRepository repository = CDOServerUtil.createRepository(name, store, properties);
//        LifecycleUtil.deactivate(repository);
        LifecycleUtil.deactivate(IPluginContainer.INSTANCE);
    }

    public static CDONet4jSession openSession(String repoName) {
        final IConnector connector = (IConnector) IPluginContainer.INSTANCE
                .getElement( //
                        "org.eclipse.net4j.connectors", // Product group
                        "tcp", // Type
                        "localhost"); // Description

        CDONet4jSessionConfiguration config = CDONet4jUtil
                .createNet4jSessionConfiguration();
        config.setConnector(connector);
        config.setRepositoryName(repoName);

        CDONet4jSession session = config.openNet4jSession();

        session.addListener(new LifecycleEventAdapter() {
            @Override
            protected void onDeactivated(ILifecycle lifecycle) {
                connector.close();
            }
        });

        return session;
    }

//    private static IStore createStore(String name) {
//        JdbcDataSource dataSource = new JdbcDataSource();
//        dataSource.setURL("jdbc:h2:database/" + name);
//
//        IMappingStrategy mappingStrategy = CDODBUtil.createHorizontalMappingStrategy(true, true);
//        IDBAdapter dbAdapter = new H2Adapter();
//        IDBConnectionProvider dbConnectionProvider = dbAdapter.createConnectionProvider(dataSource);
//        return CDODBUtil.createStore(mappingStrategy, dbAdapter, dbConnectionProvider);
//    }
}
