---
id: graphml-converter
title: GraphML
---

## Converting a Bigraph to GraphML

The _GraphML_ file format allows to represent arbitrary graphs in a XML-based structure.
More about _GraphML_ can be found on the official website [http://graphml.graphdrawing.org/](http://graphml.graphdrawing.org/).
An easy start into the file format provides the [GraphML Primer](http://graphml.graphdrawing.org/primer/graphml-primer.html).

The next listing shows how to convert a pure bigraph that is depicted below into _GraphML_.

![imgs](assets/converter/robotzone-bigraph-example.png)
The node identifiers are omitted in the figure.

### As String

```java
PureBigraph pureBigraph = ...;
PureBigraph2GraphMLPrettyPrinter graphMLPrettyPrinter = new PureBigraph2GraphMLPrettyPrinter();
String s = graphMLPrettyPrinter.toString(pureBigraph);
System.out.println(s);
```

The encoded bigraph in _GraphML_ is shown below.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:y="http://www.yworks.com/xml/graphml">
  <graph xmlns="" id="bigraph" edgedefault="undirected">
    <node id="0">
      <data key="d0">root</data>
    </node>
    <node id="v0">
      <data key="d0">node</data>
      <data key="d1">Zone1</data>
    </node>
    <edge id="v0:0" source="v0" target="0" directed="true" />
    <node id="v1">
      <data key="d0">node</data>
      <data key="d1">Zone2</data>
    </node>
    <edge id="v1:0" source="v1" target="0" directed="true" />
    <node id="v2">
      <data key="d0">node</data>
      <data key="d1">Robot</data>
      <port name="0" />
    </node>
    <edge id="v2:v0" source="v2" target="v0" directed="true" />
    <node id="v4">
      <data key="d0">node</data>
      <data key="d1">Zone3</data>
    </node>
    <edge id="v4:v1" source="v4" target="v1" directed="true" />
    <node id="v5">
      <data key="d0">node</data>
      <data key="d1">Object</data>
      <port name="0" />
    </node>
    <edge id="v5:v1" source="v5" target="v1" directed="true" />
    <node id="v3">
      <data key="d0">node</data>
      <data key="d1">Gripper</data>
      <port name="0" />
    </node>
    <edge id="v3:v2" source="v3" target="v2" directed="true" />
    <node id="v6">
      <data key="d0">node</data>
      <data key="d1">Ownership</data>
      <port name="0" />
    </node>
    <edge id="v6:v5" source="v6" target="v5" directed="true" />
    <node id="isFree">
      <data key="d0">outer</data>
    </node>
    <node id="rId">
      <data key="d0">outer</data>
    </node>
    <node id="canGrip">
      <data key="d0">outer</data>
    </node>
    <node id="belongsTo">
      <data key="d0">outer</data>
    </node>
    <hyperedge>
      <data key="d2">isFree</data>
      <endpoint node="v5" port="0" />
    </hyperedge>
    <hyperedge>
      <data key="d2">rId</data>
      <endpoint node="v2" port="0" />
    </hyperedge>
    <hyperedge>
      <data key="d2">canGrip</data>
      <endpoint node="v3" port="0" />
    </hyperedge>
    <hyperedge>
      <data key="d2">belongsTo</data>
      <endpoint node="v6" port="0" />
    </hyperedge>
  </graph>
  <key xmlns="" id="d0" for="node" attr.name="type" attr.type="string" />
  <key xmlns="" id="d1" for="node" attr.name="control" attr.type="string" />
  <key xmlns="" id="d2" for="node" attr.name="refName" attr.type="string" />
</graphml>
```

### To OutputStream

```java
PureBigraph pureBigraph = ...;
PureBigraph2GraphMLPrettyPrinter graphMLPrettyPrinter = new PureBigraph2GraphMLPrettyPrinter();
graphMLPrettyPrinter.toOutputStream(pureBigraph, System.out);
```

## Validation

The syntax of GraphML is defined by the _GraphML Schema_.
The XML schema of _GraphML_ is provided with _Bigraph Framework_ and supports syntax validation of the encoded bigraph.

An example is shown in the listing below.

```java
import javax.xml.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;

// some code omitted

ByteArrayOutputStream os = new ByteArrayOutputStream();
graphMLPrettyPrinter.toOutputStream(pureBigraph, os);

// Schema is loaded from classpath resource within the Bigraph Framework jar dependency
URL resource = BigraphPrettyPrinter.class.getClassLoader().getResource("graphml.xsd");
SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
Schema schema = sf.newSchema(resource);
Validator validator = schema.newValidator();
Source xmlFile = new StreamSource(new ByteArrayInputStream(os.toByteArray()));
validator.validate(xmlFile);
```

If no exception is thrown, the XML file is said to be valid.