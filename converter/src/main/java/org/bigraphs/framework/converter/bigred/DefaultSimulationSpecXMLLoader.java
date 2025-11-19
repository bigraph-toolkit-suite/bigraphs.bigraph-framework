/*
 * Copyright (c) 2020-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.converter.bigred;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Iterator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.reactivesystem.AbstractSimpleReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;

/**
 * Class to load a BigRed XML file containing a signature.
 *
 * @author Dominik Grzelak
 */
public class DefaultSimulationSpecXMLLoader implements BigRedXmlLoader {

    //    protected Stack<BigraphEntity> parentStack = new Stack<>();
    private DefaultSignatureXMLLoader sxl = new DefaultSignatureXMLLoader();
    private DefaultReactionRuleXMLLoader rxl;
    private DefaultBigraphXMLLoader bxl;
    protected String xmlFile;
    protected DynamicSignature signature = null;
    private AbstractSimpleReactiveSystem<PureBigraph> reactiveSystem;

    public DefaultSimulationSpecXMLLoader(AbstractSimpleReactiveSystem<PureBigraph> reactiveSystem) {
        super();
        this.reactiveSystem = reactiveSystem;
    }

    public ReactiveSystem<PureBigraph> importObject() {
        return reactiveSystem;
    }

    public void readXml(String file) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(file);
            String basePath = new File(file).getParent();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    String localPart = startElement.getName().getLocalPart();
                    if (localPart.equals("signature")) {
//                        arityCnt = 0;
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("src")) {
                                sxl.readXml(Paths.get(basePath, attribute.getValue()).toString());
                                signature = sxl.importObject();
                                rxl = new DefaultReactionRuleXMLLoader(signature);
                                bxl = new DefaultBigraphXMLLoader(signature);
                                break;
                            }
                        }
                    }
                    if (localPart.equals("rule")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("src")) {
                                rxl.readXml(Paths.get(basePath, attribute.getValue()).toString());
                                reactiveSystem.addReactionRule(rxl.importObject());
                                break;
                            }
                        }
                    }
                    if (localPart.equals("bigraph")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("src")) {
                                bxl.readXml(Paths.get(basePath, attribute.getValue()).toString());
                                reactiveSystem.setAgent(bxl.importObject());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException | XMLStreamException | InvalidReactionRuleException e) {
            e.printStackTrace();
        }
    }
}
