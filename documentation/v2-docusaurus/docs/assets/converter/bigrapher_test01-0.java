
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	DefaultDynamicSignature sig = createSignature();
	createOrGetBigraphMetaModel(sig);
	PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);
	PureBigraph s0 = builder.createRoot().// A{a}.Snd.(M{a, v_a} | Ready.Fun.1);
addChild("A", "a").down().addChild("Snd").down().addChild("M", "a").linkToOuter("v_a").addChild("Ready").down().addChild("Fun").top().// A{b}.Snd.(M{a, v_b});
addChild("A", "b").down().addChild("Snd").down().addChild("M", "a").linkToOuter("v_b").top().// Mail.1
addChild("Mail").createBigraph();
}
