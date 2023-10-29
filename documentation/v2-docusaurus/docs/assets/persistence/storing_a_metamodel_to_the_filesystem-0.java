
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

void storing_a_metamodel_to_the_filesystem() throws IOException { 
	DefaultDynamicSignature signature = pureSignatureBuilder().newControl("Building", 2).assign().newControl("Laptop", 1).assign().newControl("Printer", 2).assign().create();
	createOrGetBigraphMetaModel(signature);
	PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = pureBuilder(signature);
	PureBigraph bigraph = bigraphBuilder.createBigraph();
	BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream("meta-model.ecore"));
}
