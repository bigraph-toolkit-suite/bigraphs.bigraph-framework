
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	BigraphComposite<DefaultDynamicSignature> composed = ops(merge).parallelProduct(login).compose(bigraph);
}
