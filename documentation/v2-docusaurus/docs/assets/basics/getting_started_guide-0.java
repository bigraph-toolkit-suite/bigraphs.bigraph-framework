
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
	DefaultDynamicSignature signature = signatureBuilder.newControl().identifier("User").arity(1).status(ControlStatus.ATOMIC).assign().newControl(StringTypedName.of("Computer"), FiniteOrdinal.ofInteger(2)).assign().create();
}
