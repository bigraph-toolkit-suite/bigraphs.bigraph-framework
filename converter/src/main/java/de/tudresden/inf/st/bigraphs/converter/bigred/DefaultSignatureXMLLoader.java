package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

/**
 * Class to load a BigRed XML file containing a signature.
 *
 * @author Dominik Grzelak
 */
public class DefaultSignatureXMLLoader implements BigRedXmlLoader {

    DynamicSignatureBuilder b = pureSignatureBuilder();

    /**
     * A map that maps for each control name the port name to an index (normally, ports don't have labels but are numbered)
     */
    protected final Map<String, List<Pair<String, Integer>>> controlPortNamePortIndexMapping = new LinkedHashMap<>();

    public DefaultSignatureXMLLoader() {
        super();
    }

    public DefaultDynamicSignature importObject() {
        return b.create();
    }

    public void readXml(String file) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(file);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            String cName = null;
            int arityCnt = 0;
            ControlStatus kind = null;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().getLocalPart().equals("control")) {
                        arityCnt = 0;
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("kind")) {
                                kind = ControlStatus.fromString(attribute.getValue());
                            }
                            if (attribute.getName().toString().equals("name")) {
                                cName = attribute.getValue();
                            }
                        }
                    }
                    if (Objects.nonNull(cName) && startElement.getName().getLocalPart().equals("port")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name")) {
                                String portName = attribute.getValue();
                                controlPortNamePortIndexMapping.putIfAbsent(cName, new ArrayList<>());
                                controlPortNamePortIndexMapping.get(cName).add(new MutablePair<>(portName, arityCnt));
                            }
                        }
                    }
                }

                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals("port")) {
                        arityCnt++;
                    }
                    if (endElement.getName().getLocalPart().equals("control")) {
                        b.newControl().kind(kind).identifier(StringTypedName.of(cName))
                                .arity(arityCnt).assign();
                        cName = null;
                    }
                }
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<Pair<String, Integer>>> getControlPortNamePortIndexMapping() {
        return controlPortNamePortIndexMapping;
    }
}
