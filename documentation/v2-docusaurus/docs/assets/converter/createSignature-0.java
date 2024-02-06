
import static org.bigraphs.framework.core.factory.BigraphFactory.*;

private static DefaultDynamicSignature createSignature() { 
	DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
	defaultBuilder.newControl("A", 1).assign().newControl("A'", 1).assign().newControl("Mail", 0).assign().newControl("M", 2).status(ControlStatus.ATOMIC).assign().newControl("Snd", 0).assign().newControl("Ready", 0).assign().newControl("New", 0).assign().newControl("Fun", 0).assign();
}
