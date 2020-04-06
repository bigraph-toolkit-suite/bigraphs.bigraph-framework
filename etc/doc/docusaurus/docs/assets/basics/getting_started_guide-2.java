```java
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	Placings<DefaultDynamicSignature> placings = pureFactory.createPlacings(signature);
	Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2);
	Linkings<DefaultDynamicSignature> linkings = pureFactory.createLinkings(signature);
	Linkings<DefaultDynamicSignature>.Identity login = linkings.identity(StringTypedName.of("login"));
}
```