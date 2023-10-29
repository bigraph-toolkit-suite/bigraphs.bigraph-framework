
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	DefaultDynamicSignature sig = createSignature();
	createOrGetBigraphMetaModel(sig);
	PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);
	PureBigraph s0 = builder.createRoot().addChild("A", "a").down().addChild("Snd").down().addChild("M", "a").linkToOuter("v_a").addChild("Ready").down().addChild("Fun").top().addChild("A", "b").down().addChild("Snd").down().addChild("M", "a").linkToOuter("v_b").top().addChild("Mail").createBigraph();
}
