
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

void storing_a_metamodel_to_the_filesystem() throws IOException { 
	DynamicSignature signature = pureSignatureBuilder().newControl("Building", 2).assign().newControl("Laptop", 1).assign().newControl("Printer", 2).assign().create();
	createOrGetBigraphMetaModel(signature);
	PureBigraphBuilder<DynamicSignature> bigraphBuilder = pureBuilder(signature);
	PureBigraph bigraph = bigraphBuilder.create();
	BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream("meta-model.ecore"));
}
