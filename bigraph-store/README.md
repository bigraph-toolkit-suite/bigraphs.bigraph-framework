
Hint: net4j version of CDO Server must match with the maven dependency.
Otherwise an exception is thrown ala "org.eclipse.net4j.channel.ChannelException: Failed to register channel with peer: Protocol version 37 does not match expected version 34"

What works: emf.cdo.version 4.5.0 and CDO Server Eclipse Neon

## CDO Server
Deploy and Start a CDO Server: https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.emf.cdo.doc%2Fhtml%2Foperators%2FDoc00_OperatingServer.html


derby installation: https://medium.com/ctrl-alt-kaveet/tutorial-installing-apache-derby-4cbf03c4aaba


## CDO Client

Client examples:
- https://wiki.eclipse.org/CDO/Client
- http://git.eclipse.org/c/cdo/cdo.git/tree/plugins/org.eclipse.emf.cdo.examples/src/org/eclipse/emf/cdo/examples/server/Server.java
- http://git.eclipse.org/c/cdo/cdo.git/tree/plugins/org.eclipse.emf.cdo.examples/src/org/eclipse/emf/cdo/examples/StandaloneContainerExample.java


http://www.rcp-vision.com/cdo-connected-data-objects/
https://wiki.eclipse.org/CDO/Hibernate_Store/Tutorial
