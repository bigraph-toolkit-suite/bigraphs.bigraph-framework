package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;

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
import java.util.Iterator;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;

/**
 * @author Dominik Grzelak
 */
public class SignatureXMLLoader {
    DynamicSignatureBuilder b = pure().createSignatureBuilder();

    public SignatureXMLLoader() {
        super();
    }

    public DefaultDynamicSignature importObject() {
        return b.create();
    }

    public void readConfig(String configFile) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(configFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            String cName = null;
            int arityCnt = 0;
            ControlKind kind = null;
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
                                kind = ControlKind.fromString(attribute.getValue());
                            }
                            if (attribute.getName().toString().equals("name")) {
                                cName = attribute.getValue();
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
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
