
public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	// ...
	ReactiveSystemPredicate<PureBigraph> phi = SubBigraphMatchPredicate.create(builder.spawn().root().child("Mail").down().child("M", "a").linkOuter("v").site().top().create()).withLabel("phi");
}
