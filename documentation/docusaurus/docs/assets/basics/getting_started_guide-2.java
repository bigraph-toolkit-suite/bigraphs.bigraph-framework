```java
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	Placings<DefaultDynamicSignature> placings = pure().createPlacings(signature);
	Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2);
	Linkings<DefaultDynamicSignature> linkings = pure().createLinkings(signature);
	Linkings<DefaultDynamicSignature>.Identity login = linkings.identity(StringTypedName.of("login"));
}
```