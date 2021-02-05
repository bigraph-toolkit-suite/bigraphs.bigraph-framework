---
id: bigred-converter
title: BigRed
---

## Exporting to BigRed

### Exporting Signatures

To export a `DefaultDynamicSignature` as BigRed `*.bigraph-signature` file, you may use the `SignatureAdapter` class
in combination with BigRed's `org.bigraph.model.savers.SignatureXMLSaver`:

```java
DefaultDynamicSignature signature = ...;
SignatureAdapter signatureAdapter = new SignatureAdapter(signature);

PrintStream out = new PrintStream(System.out);
SignatureXMLSaver sx = new SignatureXMLSaver();
sx.setModel(signatureAdapter)
    .setOutputStream(out);
sx.exportObject();
```

This approach mainly used the underlying functionality of the BigRed library.

## Loading BigRed XML files (Signatures, Agents, Rules, Simulation Specification)

### Loading Signatures

To load a BigRed `*.bigraph-signature` file use the `de.tudresden.inf.st.bigraphs.converter.bigred.DefaultSignatureXMLLoader` class:

```java
DefaultSignatureXMLLoader sxl = new DefaultSignatureXMLLoader();
sxl.readXml("path/to/signatures/printing.bigraph-signature"));
DefaultDynamicSignature signature = sxl.importObject();
```

### Loading Agents

To load a BigRed `*.bigraph-agent` file use the `de.tudresden.inf.st.bigraphs.converter.bigred.DefaultBigraphXMLLoader` class:

```java
DefaultBigraphXMLLoader bxl = new DefaultBigraphXMLLoader();
bxl.readXml("path/to/agents/simple.bigraph-agent");
PureBigraph simpleBigraph = bxl.importObject();
```

BigRed agent specifications are imported as `PureBigraph` instances.

### Loading Reaction Rules

To load a BigRed `*.bigraph-rule` file use the `de.tudresden.inf.st.bigraphs.converter.bigred.DefaultReactionRuleXMLLoader` class:

```java
DefaultReactionRuleXMLLoader rxl = new DefaultReactionRuleXMLLoader();
rxl.readXml("path/to/rules/finish-job.bigraph-rule");
ParametricReactionRule<PureBigraph> finishJob = rxl.importObject();
```

BigRed reaction rule specifications are imported as `ParametricReactionRule<PureBigraph>` instances.


The loaders are separate and custom implementations that parse BigRed's XML files.
They do not use the underlying functionality of the BigRed library as the adapter class for writing bigraph entities.

### Loading a Simulation Specification

To load a BigRed `*.bigraph-simulation-spec` file, use the `de.tudresden.inf.st.bigraphs.converter.bigred.DefaultSimulationSpecXMLLoader` class:

```java
DefaultSimulationSpecXMLLoader ssxl = new DefaultSimulationSpecXMLLoader();
ssxl.readXml("path/to/simple.bigraph-simulation-spec");
PureReactiveSystem pureReactiveSystem = ssxl.importObject();
```

A BigRed simulation specification represents a bigraphical reactive system and is imported as a `PureReactiveSystem` instance.