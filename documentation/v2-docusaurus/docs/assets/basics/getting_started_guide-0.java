
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	DynamicSignatureBuilder sigBuilder = pureSignatureBuilder();
	DefaultDynamicSignature signature = sigBuilder.addControl("User", 1, ControlStatus.ATOMIC).addControl("Computer", 2).create();
}
