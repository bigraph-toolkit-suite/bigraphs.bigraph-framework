
void getting_started_guide() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException { 
	// ...
	Placings<DynamicSignature> placings = purePlacings(signature);
	Placings<DynamicSignature>.Merge merge = placings.merge(2);
	Linkings<DynamicSignature> linkings = pureLinkings(signature);
	Linkings<DynamicSignature>.Identity login = linkings.identity("login");
}
