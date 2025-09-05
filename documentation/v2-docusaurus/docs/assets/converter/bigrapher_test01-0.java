
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	DynamicSignature sig = createSignature();
	createOrGetBigraphMetaModel(sig);
	PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);
	PureBigraph s0 = builder.root().child("A", "a").down().child("Snd").down().child("M", "a").linkOuter("v_a").child("Ready").down().child("Fun").top().child("A", "b").down().child("Snd").down().child("M", "a").linkOuter("v_b").top().child("Mail").create();
}
