
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	DynamicSignatureBuilder sigBuilder = pureSignatureBuilder();
	DynamicSignature signature = sigBuilder.add("User", 1, ControlStatus.ATOMIC).add("Computer", 2).create();
}
