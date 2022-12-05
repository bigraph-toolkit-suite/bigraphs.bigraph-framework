
public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	// ...
	PureBigraph sndRedex = builder.spawnNewOne().createRoot().addChild("A", "a0").down().addChild("Snd").down().addChild("M", "a1").linkToOuter("v").addSite().top().addChild("Mail").createBigraph();
	PureBigraph sndReactum = builder.spawnNewOne().createRoot().addChild("A", "a0").addChild("Mail").down().addChild("M", "a1").linkToOuter("v").addSite().createBigraph();
	ParametricReactionRule<PureBigraph> snd = new ParametricReactionRule<>(sndRedex, sndReactum).withLabel("snd");
	PureBigraph readyRedex = builder.spawnNewOne().createRoot().addChild("A", "a").down().addChild("Ready").up().addChild("Mail").down().addChild("M", "a").linkToOuter("v").addSite().createBigraph();
	PureBigraphBuilder<DefaultDynamicSignature> readyBuilder = builder.spawnNewOne();
	readyBuilder.createOuterName("v");
	PureBigraph readyReactum = readyBuilder.createRoot().addChild("A", "a").addChild("Mail").createBigraph();
	ParametricReactionRule<PureBigraph> ready = new ParametricReactionRule<>(readyRedex, readyReactum).withLabel("ready");
	PureBigraph lambdaRedex = builder.spawnNewOne().createRoot().addChild("A", "a").down().addChild("Fun").createBigraph();
	PureBigraph lambdaReactum = builder.spawnNewOne().createRoot().addChild("A", "a").createBigraph();
	ParametricReactionRule<PureBigraph> lambda = new ParametricReactionRule<>(lambdaRedex, lambdaReactum).withLabel("lambda");
	PureBigraph newRedex = builder.spawnNewOne().createRoot().addChild("A", "a0").down().addChild("New").down().addChild("A'", "a1").down().addSite().up().addSite().up().addSite().createBigraph();
	PureBigraph newReactum = builder.spawnNewOne().createRoot().addChild("A", "a0").down().addSite().addSite().up().addChild("A", "a1").down().addSite().addSite().createBigraph();
	InstantiationMap instMap = InstantiationMap.create(newReactum.getSites().size()).map(0, 1).map(1, 2).map(2, 0).map(3, 2);
	ParametricReactionRule<PureBigraph> newRR = new ParametricReactionRule<>(newRedex, newReactum, instMap).withLabel("new");
}
