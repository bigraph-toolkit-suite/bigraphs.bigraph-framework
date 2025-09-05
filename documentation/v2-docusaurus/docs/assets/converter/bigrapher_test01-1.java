
public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	// ...
	PureBigraph sndRedex = builder.spawn().root().child("A", "a0").down().child("Snd").down().child("M", "a1").linkOuter("v").site().top().child("Mail").create();
	PureBigraph sndReactum = builder.spawn().root().child("A", "a0").child("Mail").down().child("M", "a1").linkOuter("v").site().create();
	ParametricReactionRule<PureBigraph> snd = new ParametricReactionRule<>(sndRedex, sndReactum).withLabel("snd");
	PureBigraph readyRedex = builder.spawn().root().child("A", "a").down().child("Ready").up().child("Mail").down().child("M", "a").linkOuter("v").site().create();
	PureBigraphBuilder<DynamicSignature> readyBuilder = builder.spawn();
	readyBuilder.createOuter("v");
	PureBigraph readyReactum = readyBuilder.root().child("A", "a").child("Mail").create();
	ParametricReactionRule<PureBigraph> ready = new ParametricReactionRule<>(readyRedex, readyReactum).withLabel("ready");
	PureBigraph lambdaRedex = builder.spawn().root().child("A", "a").down().child("Fun").create();
	PureBigraph lambdaReactum = builder.spawn().root().child("A", "a").create();
	ParametricReactionRule<PureBigraph> lambda = new ParametricReactionRule<>(lambdaRedex, lambdaReactum).withLabel("lambda");
	PureBigraph newRedex = builder.spawn().root().child("A", "a0").down().child("New").down().child("A'", "a1").down().site().up().site().up().site().create();
	PureBigraph newReactum = builder.spawn().root().child("A", "a0").down().site().site().up().child("A", "a1").down().site().site().create();
	InstantiationMap instMap = InstantiationMap.create(newReactum.getSites().size()).map(0, 1).map(1, 2).map(2, 0).map(3, 2);
	ParametricReactionRule<PureBigraph> newRR = new ParametricReactionRule<>(newRedex, newReactum, instMap).withLabel("new");
}
