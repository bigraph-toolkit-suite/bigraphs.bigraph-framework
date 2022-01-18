
public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	// ...
	ReactiveSystemPredicate<PureBigraph> phi = SubBigraphMatchPredicate.create(builder.spawnNewOne().createRoot().addChild("Mail").down().addChild("M", "a").linkToOuter("v").addSite().top().createBigraph()).withLabel("phi");
}
