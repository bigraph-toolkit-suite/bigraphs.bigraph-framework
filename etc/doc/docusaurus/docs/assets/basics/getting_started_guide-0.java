```java
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	PureBigraphFactory pureFactory = BigraphFactory.pure();
	DefaultDynamicSignature signature = pureFactory.createSignatureBuilder().newControl().identifier("User").arity(1).kind(ControlKind.ATOMIC).assign().newControl(StringTypedName.of("Computer"), FiniteOrdinal.ofInteger(2)).assign().create();
}
```