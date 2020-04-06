```java
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	PureBigraphBuilder<DefaultDynamicSignature> builder = pureFactory.createBigraphBuilder(signature);
	builder.createRoot().addChild("User", "login").addChild("Computer", "login");
	PureBigraph bigraph = builder.createRoot().addChild("User", "login2").addChild("Computer", "login").createBigraph();
}
```