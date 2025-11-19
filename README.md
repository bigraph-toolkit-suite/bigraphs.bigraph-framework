<img src="etc/assets/bigraph-framework-logo.png" style="zoom:67%;" />

> Latest Version: **2.3.2**

[![HiRSE Code Promo Badge](https://img.shields.io/badge/Promo-8db427?style=plastic&label=HiRSE&labelColor=005aa0&link=https%3A%2F%2Fgo.fzj.de%2FCodePromo)](https://go.fzj.de/CodePromo)

- User Manual: https://bigraphs.org/software/bigraph-framework/docs/

- JavaDoc: https://bigraphs.org/software/bigraph-framework/apidocs/

---

# Bigraph Framework

**What is Bigraph Framework?**

Bigraph Framework is a framework
written in Java for the creation and simulation of bigraphs
to foster the experimental evaluation of the bigraph theory in
real-world applications.

The goal of this framework is to facilitate the implementation of context-aware, agent-based systems, and reactive systems in general.
It provides means for model-driven software development based on the bigraph theory.
The high-level Java API eases the programming of bigraphical systems for real-world application.

**Features**

- Modelling and Storage
    - Dynamic creation of bigraphs at design time and runtime based on the [Bigraph Ecore Metamodel (BEM)](https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-ecore-metamodel)
    - Read and write instance models and metamodels of a bigraph from and to the file system
- Visualization
    - Graphical export via GraphViz/DOT, PNG, Visualization of Compiler Graphs (VCG) format via yComp
    - Interactive visualization via GraphStream
- Bigraphical Reactive Systems (BRS): Simulate the evolution of bigraphs by reaction rules
    - Bigraph matching and rewriting via [jLibBig](https://bigraphs.github.io/jlibbig/)
    - Dedicated subhypergraph matching on link graphs (query-data matching)
    - Generation of a labeled transition system (LTS)
    - Simulation and Model Checking (BFS, Random)
    - Predicate checking
    - Specify order of reaction rules via priorities
    - Tracking rules (trace node identities across reactions)
    - Conditional rules (not yet integrated in model checking procedure but available for custom usage)
- Model Importer/Exporter
    - Export a bigraph to common graph formats, e.g., DOT, GraphML, GXL, VCG
    - Export to formats of other bigraph tools: BigMC, BigraphER, BigRed, jLibBig, ...
- Attributed Bigraphs
    - Add arbitrary attributes to nodes
    - Attributes are preserved when doing rewriting (this requires tracking maps)

**Requirements**

- Java >=17 (JDK)
- Maven / Gradle
- Graphviz for the `bigraph-visualization` module
    - Ubuntu: `sudo apt install graphviz`

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

## Project Configuration

> All parts of Bigraph Framework are available from the [Central Repository](https://central.sonatype.com/).

> See also <a href="#Building-the-Framework-from-Source">Building from Source</a> if you want to build the source by yourself and host them in your Maven local repository.

### Maven

```xml
<dependencies>
  <!-- the core module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-core</artifactId>
    <version>2.3.2</version>
  </dependency>
  <!-- the rewriting module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-simulation</artifactId>
    <version>2.3.2</version>
  </dependency>
  <!-- the visualization module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-visualization</artifactId>
    <version>2.3.2</version>
  </dependency>
  <!-- the converter module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-converter</artifactId>
    <version>2.3.2</version>
  </dependency>
</dependencies>
```

### Gradle

```groovy
compile "org.bigraphs.framework:bigraph-core:2.3.2"
compile "org.bigraphs.framework:bigraph-simulation:2.3.2"
compile "org.bigraphs.framework:bigraph-visualization:2.3.2"
compile "org.bigraphs.framework:bigraph-converter:2.3.2"
```

### Logging

This framework employs SLF4J as a facade for the log4j logging framework.

Depending on your project setup, you may need to include the following libraries in your `pom.xml` :

```xml
<!-- For Spring -->
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

        <!-- For a bare Maven project -->
<dependency>
<groupId>org.slf4j</groupId>
<artifactId>slf4j-log4j12</artifactId>
<version>1.7.30</version>
</dependency>
```

The example above shows how to use log4j2 in your project as the underlying logging framework.

## Development

### Requirements

It is not necessary to build from source to use *Bigraph Framework* but if you want to try out the latest version, the project can be easily built with the [maven wrapper](https://maven.apache.org/tools/wrapper/) or the regular `mvn` command.

> **Note:** The required version of Maven is >= 3.8.3 and Java JDK >=17.

The recommendation here is to build it with the regular `mvn` command.

On Debian systems you can install it by issuing the following command:

```shell
$ sudo apt install maven
```

See [Installation](https://maven.apache.org/install.html) for other options.

### Building the Framework from Source

**Initialize**

The following command has to be run once:

```shell
$ mvn initialize
```
It installs some dependencies located in the `./etc/libs/` folder of this project in your local Maven repository, which is usually located at `~/.m2/`.
These are required for the development.

> When using IntelliJ IDEA, make sure to "Sync All Maven Projects" again to resolve any project errors that may appear due to missing dependencies on first startup.

**Build/Install**

One of the following commands must be executed from the root directory of this project:

```bash
# Default
$ mvn clean install -DskipTests

# To create a "fat jar" for each module, run:
$ mvn clean install -DskipTests -PfatJar
```

After the command successfully finishes, you can now use _Bigraph Framework_ in other Java projects.
All modules of _Bigraph Framework_ have been installed in the local Maven repository.
Therefore, see [Maven configuration](#maven) on how to include the individual _Bigraph Framework_ dependencies.

### Building the Documentation: User Manual

See [etc/Development-and-Deployment.md](./etc/Development-and-Deployment.md) for more details.

Building the documentation builds also the project without running tests.
After running the commands as described below from the root of this project, the generated user manual will be available
at `documentation/v2-docusaurus/`:

```shell
$ mvn clean package -DskipTests
$ mvn license:aggregate-third-party-report
$ cd ./documentation/v2-docusaurus/
$ nvm use 20.18.1
$ npm run start
```

Then, open the browser at `http://localhost:3000/software/bigraph-framework/`.

The manual is generated using [docusaurus](https://docusaurus.io/), which must be installed on the system
(see [Development-and-Deployment.md](etc/Development-and-Deployment.md)).

### Deployment

To deploy Bigraph Framework to the [Central Repository](https://central.sonatype.com/):

```bash
$ mvn clean deploy -DskipTests -P release,central
```

See the document [etc/Development-and-Deployment.md](./etc/Development-and-Deployment.md) for more details concerning the
development and deployment of _Bigraph Framework_.

## License

**Bigraph Framework** is Open Source software released under the [Apache 2.0 license](LICENSE).

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.

Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers (Main Developer: Dominik Grzelak)
 
### Third Party Licenses

This report lists all third-party dependencies of the
project: [documentation/documentation/v2-docusaurus/static/third-party-licenses-report/third-party-report.html](documentation/documentation/v2-docusaurus/static/third-party-licenses-report/third-party-report.html)

The simulation module of **Bigraph Framework** includes and shades [jLibBig](https://github.com/bigraphs/jlibbig),
a Java library for bigraphical reactive systems, which is licensed under the **GNU Lesser General Public License,
version 2.1 only (LGPL-2.1-only)**.

In full compliance with LGPL-2.1:
- The jLibBig code is not obfuscated or renamed.
- You may modify jLibBig or replace it using the standard Maven build process.
- Modifications are documented in: [`NOTICE-jlibbig.txt`](./etc/libs/jlibbig-0.0.4/NOTICE-jlibbig.txt).

