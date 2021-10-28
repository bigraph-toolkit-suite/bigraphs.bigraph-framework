
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	BigraphComposite<DefaultDynamicSignature> composed = BigraphFactory.ops(merge).parallelProduct(login).compose(bigraph);
}
