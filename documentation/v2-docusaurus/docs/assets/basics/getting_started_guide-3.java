
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	BigraphComposite<DynamicSignature> composed = ops(merge).parallelProduct(login).compose(bigraph);
}
