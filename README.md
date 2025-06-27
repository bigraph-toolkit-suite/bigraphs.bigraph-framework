<img src="etc/assets/bigraph-framework-logo.png" style="zoom:67%;" />

# Bigraph Framework

| Branch  | Latest Versions       | Status       |
|---------|-----------------------|--------------|
| Main    | 2.1.0 / 2.0.2 / 2.0.1 | Releases     |
| Develop | 3.0.0-SNAPSHOT        | Experimental |

- User Manual: https://bigraphs.org/products/bigraph-framework/docs/

- JavaDoc: https://bigraphs.org/products/bigraph-framework/apidocs/

----

**What is Bigraph Framework?**

Bigraph Framework is a framework
written in Java for the creation and simulation of bigraphs
to expedite the experimental evaluation of the bigraph theory in
real-world applications.

The goal of this framework is to facilitate the implementation of context-aware and agent-based systems, and reactive systems in general.
It provides means for model-driven software development based on the bigraph theory.
The high-level Java API eases the programming of bigraphical systems for real-world application.

**Features**

- Modelling and Storage
  - Dynamic creation of bigraphs at design time and runtime based on the [Bigraph Ecore Metamodel (BEM)](https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-ecore-metamodel)
  - Read and write instance models and metamodels of a bigraph from and to the file system
- Visualization
  - Graphical export via GraphViz/DOT
  - PNG, JPG, ...
  - Interactive visualization UI via GraphStream
  - Visualization of Compiler Graphs (VCG) format via yComp
- Bigraphical Reactive Systems (BRS): Simulate the evolution of bigraphs by reaction rules
  - Bigraph matching and rewriting via jLibBig
  - Generation of a labeled transition system (LTS)
  - Simulation and Model Checking (BFS, Random)
  - Predicate checking, logical connectors, LTL
  - Specify order of reaction rules via priorities
  - Conditional rules (not yet integrated in model checking procedure but available for custom usage)
  - Tracking rules (a rule can be assigned a tracking map)
- Model Transformations
  - Export a bigraph to common graph formats, e.g., DOT, GraphML, GXL, VCG
  - Export to formats of other bigraph tools: BigMC, BigraphER, BigRed, jLibBig, ...
  - Translate bigraphs to other graph classes: Ranked Graphs, multigraphs, ...
- Attributed Bigraphs
  - Add arbitrary attributes to nodes
  - Attributes are preserved when doing rewriting (this requires tracking maps)

**Requirements**
- Java >=17
  - (!) _not_ the headless version of the JDK (!)
- Graphviz for the visualization module
  - Ubuntu 20.04/22.04: `sudo apt install graphviz`

## Getting Started

Here is a quick teaser of creating a pure concrete bigraph using _Bigraph Framework_ in Java.

### Lean Bigraph API

The lean bigraph API allows fast bigraph creation and composition.

To following usage assumes the import statement `import static org.bigraphs.framework.core.factory.BigraphFactory.*`.

```java
// create the signature
DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl("A", 0).assign() // straightforward access
                // two other ways (more verbose):
                .newControl(StringTypedName.of("C"), FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).kind(ControlKind.ATOMIC).assign()
                .create();

// create two bigraphs
PureBigraph bigraph1 = pureBuilder(signature)
        .createRoot()
        .addChild("A").addChild("C")
        .createBigraph();

PureBigraph bigraph2 = pureBuilder(signature)
        // "User" is the control, "alice" is an outer name     
        .createRoot().addChild("User", "alice").addSite()
        .createBigraph();

// compose two bigraphs
BigraphComposite composite = ops(bigraph2).compose(bigraph1);
```

### Other APIs

#### **Bigraph Builder: Connecting nodes by links**

The bigraph builder provides more utility methods helping to build more
complex structures easily.

The following one shows, how to create nodes, and at the same time connecting them all with an edge:

```java
PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
builder.createRoot().connectByEdge(
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
builder.createRoot().addChild("Printer").linkToInner(tmp_link);
builder.createRoot().addChild("Computer").linkToInner(tmp_link);

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
BigraphComposite<DefaultDynamicSignature> composite = ops(G);

BigraphComposite<DefaultDynamicSignature> result = composite.compose(F);
composite.juxtapose(F);
composite.juxtapose(F).parallelProduct(H);
```

## Maven Configuration

```xml
<dependencies>
  <!-- the core module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-core</artifactId>
    <version>${version}</version>
  </dependency>
  <!-- the rewriting module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-simulation</artifactId>
    <version>${version}</version>
  </dependency>
  <!-- the visualization module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-visualization</artifactId>
    <version>${version}</version>
  </dependency>
  <!-- the converter module -->
  <dependency>
    <groupId>org.bigraphs.framework</groupId>
    <artifactId>bigraph-converter</artifactId>
    <version>${version}</version>
  </dependency>
</dependencies>
```

### Remote Repository for Snapshot Releases

SNAPSHOT releases are deployed [here](https://s01.oss.sonatype.org/content/repositories/snapshots).
To resolve them, the following remote repository must be configured in your `pom.xml`:
```xml
<repository>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
  <id>ossrh</id>
  <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```

> See also <a href="#Building-the-Framework-from-Source">Building from Source</a> if you want to build the source by yourself and host them in your Maven local repository.

#### Logging

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

## Module Details

A brief description of each module's purpose is given below.

#### bigraph-core

Provides builders, factories and interfaces to create concrete bigraphs and elementary bigraphs.

Concrete Bigraphs and their metamodel (with the signature only) can be written/loaded to/from the file system.

### bigraph-simulation

Simulate bigraphs by creating bigraphical reactive systems, reaction rules and agents.
Check a system according to some specification by defining various types of predicates.

### bigraph-visualization

Provides simple means to export bigraphs and transition systems as graphic files.

Currently, DOT is used in combination with GraphViz. Bigraphs can be exported as `*.png` and `*.jpg`.

**Requirements**

In order to use the functionality of the visualization module, the following tools must be installed on the machine:
- Graphviz: `apt install -y graphviz`

### bigraph-converter

Provides several ways to convert bigraphs into other representations.
For example, bigraphs to GraphML format, BigraphER's specification language or BigMC's term language.

### A User-Friendly API for the Bigraph Ecore Metamodel

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

## Development: Build Configuration

It is not necessary to build from source to use *Bigraph Framework* but if you want to try out the latest version, the project can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper) or the regular `mvn` command.
In this case, **JDK >=17** is needed.

> **Note:** The required version of Maven is 3.8.3 and Java JDK >=17.

The recommendation here is to build it with the regular `mvn` command.
You will need [Maven v3.8.3 or above](https://maven.apache.org/install.html).

### Building the Framework from Source

**Initialize**

The following command has to be run once:
```shell
mvn initialize
```
It installs some dependencies located in the `./etc/libs/` folder of this project in your local Maven repository, which is usually located at `~/.m2/`.
These are required for the development.

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
Therefore, see [Maven configuration](#maven-configuration) on how to include the individual _Bigraph Framework_ dependencies.

> **Note:** All parts of Bigraph Framework are also deployed to the [Central Repository](https://repo.maven.apache.org/maven2/).

### Building the Documentation: User Manual

See [etc/Development-and-Deployment.md](./etc/Development-and-Deployment.md) for more details.

Building the documentation builds also the project without running tests.
After running the commands as described below from the root of this project, the generated user manual will be available
at `documentation/v2-docusaurus/`:

```shell
mvn clean package -DskipTests
cd ./documentation/v2-docusaurus/
npm run start
```

Then, open the browser at `http://localhost:3000/products/bigraph-framework/`.

The manual is generated using [docusaurus](https://docusaurus.io/), which must be installed on the system
(see [Development-and-Deployment.md](etc/Development-and-Deployment.md)).

### Further Development and Deployment Instructions

See the document [etc/Development-and-Deployment.md](./etc/Development-and-Deployment.md) for more issues regarding the development and deployment of _Bigraph Framework_.

To deploy Bigraph Framework to the [Central Repository](https://repo.maven.apache.org/maven2/):
```bash
mvn clean deploy -DskipTests -P release,ossrh
```

## License

**Bigraph Framework** is Open Source software released under the Apache 2.0 license.

```text
   Copyright 2021-present Bigraph Toolkit Suite Developers.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```