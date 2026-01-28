[![HiRSE Code Promo Badge](https://img.shields.io/badge/Promo-8db427?style=plastic&label=HiRSE&labelColor=005aa0&link=https%3A%2F%2Fgo.fzj.de%2FCodePromo)](https://go.fzj.de/CodePromo)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.bigraphs.framework/framework-parent)](https://central.sonatype.com/search?q=org.bigraphs.framework)

<img src="etc/assets/bigraph-framework-logo.png" style="zoom:67%;" />

- User Manual: https://bigraphs.org/software/bigraph-framework/docs/

- JavaDoc: https://bigraphs.org/software/bigraph-framework/apidocs/

---

# Bigraph Framework

Bigraph Framework is a Java framework designed for developers and researchers for building, simulating, and analyzing reactive systems (e.g., cyber-physical, context-aware, agent-based, or distributed systems, IoT environments, ...).

It lets you model dynamic systems with both structure (who is inside what) and connectivity (who is connected and interacting to whom), and then simulate, visualize, and verify how those systems evolve over time.

Under the hood, the framework is based on Milner’s theory of Bigraphical Reactive Systems (BRS).

### Features

**Bigraph Modeling and Persistence**
- Create and manipulate bigraphs dynamically at **design time and runtime** using the **Bigraph Ecore Metamodel (BEM)**  
  ([bigraphs.bigraph-ecore-metamodel](https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-ecore-metamodel))
- Load and store both **bigraph metamodels** and **instance models** in standard file formats (Ecore/XMI)

**Visualization and Inspection**
- Export bigraphs and transition systems to standard graph formats:
  **GraphViz (DOT), PNG, VCG (yComp)**
- Interactive, programmatic visualization via **GraphStream** for exploring structures

**Bigraphical Reactive Systems (BRS)**

Model and analyze system dynamics using **reaction rules** and **graph rewriting**:

- Pattern matching and rewriting powered by **jLibBig**, with full **node and link tracking**
- Specialized **link-graph (hypergraph) matching**
- Multiple **simulation and model-checking strategies**:
  - Breadth-first search (BFS)
  - Depth-first search (DFS)
  - Random exploration
  - Match-all / Match-first
  - Custom (Simulated Annealing, ...)
- Automatic construction of **Labeled Transition Systems (LTS)**
- **State predicates and logical connectors** for property checking
- **Rule priorities** to control nondeterminism and execution order
- **Tracking rules** to preserve identity of entities across reactions
- **Conditional rules** for guarded rewriting

**Import, Export, and Tool Interoperability**
- Export bigraphs and LTSs to standard graph formats:
  **DOT, GraphML, GXL, VCG**
- Interoperate with other bigraph tools:
  **BigMC, BigraphER, BigRed, jLibBig**, and others

**Attributed Bigraphs**
- Attach **arbitrary attributes** to:
  - Nodes
  - Links (edges and outer names)
- Attributes are **preserved during rewriting**, enabling data-rich CPS and agent-based models  
  (via tracking maps)


## Getting Started

Here is a quick teaser of creating a pure concrete bigraph using _Bigraph Framework_ in Java.

### Lean Bigraph API

The lean bigraph API allows fast bigraph creation and composition.

To following usage assumes the import statement `import static org.bigraphs.framework.core.factory.BigraphFactory.*`.

```java
// Create the signature
DynamicSignature signature = pureSignatureBuilder()
                // Straightforward:
                .add("A", 0)
                .add("C", 1)
                .add("User", 1, ControlStatus.ATOMIC)
                .create();

// Create two bigraphs
PureBigraph bigraph1 = pureBuilder(signature)
        .root()
        .child("A").child("C")
        .create();

PureBigraph bigraph2 = pureBuilder(signature)
        // "User" is the control, "alice" is an outer name     
        .root().child("User", "alice").site()
        .create();

// compose two bigraphs
BigraphComposite composite = ops(bigraph2).compose(bigraph1);
```

### Other APIs

#### **Bigraph Builder: Connecting nodes by links**

The bigraph builder provides more utility methods helping to build more
complex structures easily.

The following one shows, how to create nodes, and at the same time connecting them all with an edge:

```java
PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
builder.root().connectByEdge(
    "Job",
            "Job",
    signature.getControlByName("Job")
);
```

Now, we want to connect nodes located at different "places". Therefore, we
link them through an inner name, and
after, close the link to automatically transform it to an edge:
```java
// First, create an inner name
BigraphEntity.InnerName tmp_link = builder.createInnerName("link");

// Create two nodes within different hierarchies
builder.root().child("Printer").linkInner(tmp_link);
builder.root().child("Computer").linkInner(tmp_link);

// Finally, close the inner name. This will leave the edge intact.
builder.closeInnerName(tmp_link);
```

#### **Exporting the Ecore metamodel and instance model**

```java
PureBigraph bigraph = ...;
// Writes a bigraph to the filesystem
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream("./meta-model.ecore"));
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, new FileOutputStream("./instance-model.xmi"));
// prints bigraph on the console
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
```

See the reference and documentation for a more comprehensive overview.

#### **Bigraph Composition**

To get the composition and tensor product of two bigraphs:
```java
PureBigraph G = ...;
PureBigraph F = ...;
PureBigraph H = ...;
BigraphComposite<DynamicSignature> composite = ops(G);

BigraphComposite<DynamicSignature> result = composite.compose(F);
composite.juxtapose(F);
composite.juxtapose(F).parallelProduct(H);
```

### A User-Friendly API for the Bigraph Ecore Metamodel (BEM)

- Internally, bigraphs are described by a metamodel based on Ecore.
  The project can be found in this [GitHub repository](https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-ecore-metamodel).
- To create concrete bigraphs, a signature must be provided.
  To do so, this metamodel is extended when creating a new bigraphical signature which is then called "metamodel over a signature" of an abstract bigraph (described by the Ecore model).
  We say that the signature is mapped to the metamodel over a signature.
  From that, multiple instance models can be created where the instance bigraph relates to the signature _S_, thus, corresponds to the metamodel over the signature _S_.
- Extending the metamodel with a signature by hand is time-consuming
  especially when many models are created. The framework allows to create
  bigraphs dynamically at runtime by letting the user providing a description
  of the signature. The metamodel over a signature is kept in memory and
  instances can be created from it. As a result, the bigraph metamodel must
  not be touched manually. Both the metamodel over a signature and the
  instance model can be stored on the filesystem.
- That very metamodel serves only as a data model for the *Bigraph Framework*
  which provides additional functionality and a user-friendly API for the
  creation and simulation of bigraphical reactive systems. Furthermore, we
  achieve Separation of concerns: The metamodel itself is implementation-agnostic.
  The Bigraph Framework adds specific behavior superimposed upon this meta
  model. Meaning, the implementation-specific details are kept out from the metamodel.

## Installation & Setup of the Java Framework

> All parts of Bigraph Framework are available from the [Central Repository](https://central.sonatype.com/).

> See also [Building from Source](etc/Development-and-Deployment.md#building-the-framework-from-source) if you want to build the source by yourself and host them in your Maven local repository.

### Requirements

- Java >=21 (JDK)
- Maven / Gradle
- Graphviz for the `bigraph-visualization` module
  - Ubuntu: `sudo apt install graphviz`

### Maven

```xml
<dependencies>
  
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-core</artifactId>
    <version>2.3.5</version>
  </dependency>
  
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-simulation</artifactId>
    <version>2.3.5</version>
  </dependency>

  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-visualization</artifactId>
    <version>2.3.5</version>
  </dependency>

  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-converter</artifactId>
    <version>2.3.5</version>
  </dependency>

</dependencies>
```

### Gradle

```groovy
compile "org.bigraphs.framework:bigraph-core:2.3.5"
compile "org.bigraphs.framework:bigraph-simulation:2.3.5"
compile "org.bigraphs.framework:bigraph-visualization:2.3.5"
compile "org.bigraphs.framework:bigraph-converter:2.3.5"
```

### Logging

Bigraph Framework employs SLF4J as a facade for the many logging frameworks.

Depending on your project setup, you may need to include the following libraries in your `pom.xml`/`build.gradle`.

For a bare Maven/Gradle project:
```xml
<!-- For example, use log4j-->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.30</version>
</dependency>

<!-- Or, use a no-operation (NOP) logger implementation -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-nop</artifactId>
    <version>2.0.7</version>
</dependency>

<!-- or, for example, the reload4j implementation (fork of log4j) -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-reload4j</artifactId>
    <version>2.0.9</version>
</dependency>
```

For Spring-based Projects:
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-logging</artifactId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```

The example above shows how to use log4j2 in your project as the underlying logging framework.


## Development and Deployment

See the document [etc/Development-and-Deployment.md](./etc/Development-and-Deployment.md) for more details concerning the
development and deployment of _Bigraph Framework_.

## License

**Bigraph Framework** is Open Source software released under the [Apache 2.0 license](LICENSE).

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.

Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers (Main Developer: Dominik Grzelak)
 
### Third Party Licenses

This report lists all third-party dependencies of the
project: [documentation/v2-docusaurus/static/license/aggregate-third-party-report.html](documentation/v2-docusaurus/static/license/aggregate-third-party-report.html)

The simulation module of **Bigraph Framework** includes and shades [jLibBig](https://github.com/bigraphs/jlibbig),
a Java library for bigraphical reactive systems, which is licensed under the **GNU Lesser General Public License,
version 2.1 only (LGPL-2.1-only)**.

In full compliance with LGPL-2.1:
- The jLibBig code is not obfuscated or renamed.
- You may modify jLibBig or replace it using the standard Maven build process.
- Modifications are documented in: [`NOTICE-jlibbig.txt`](./etc/libs/jlibbig-0.0.4/NOTICE-jlibbig.txt).

