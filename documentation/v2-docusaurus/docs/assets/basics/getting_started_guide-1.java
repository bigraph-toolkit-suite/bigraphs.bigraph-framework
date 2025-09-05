
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
	builder.root().child("User", "login").child("Computer", "login");
	PureBigraph bigraph = builder.root().child("User", "login").child("Computer", "login").create();
}
