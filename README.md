<img src="./etc/bigraph-framework-logo.png" style="zoom:67%;" />

# Bigraph Framework

Version | Master | Develop
---|---|---
0.9.0-SNAPSHOT | [![pipeline status](/../badges/master/pipeline.svg)](/../pipelines) | [![pipeline status](/../badges/develop/pipeline.svg)](/../pipelines)

-----

A framework for the creation and simulation of bigraphs to expedite the experimental evaluation of the bigraph theory in
real-world applications.

The goal of this framework is to facilitate the implementation of context-aware and agent-based systems, and reactive systems in general.
It provides means for model-driven software development based on the bigraph theory.
The high-level API eases the programming of bigraphical systems for real-world application.

**Features**

- Dynamic creation of bigraphs at runtime based on an EMOF-based meta model
- Read and write the meta and instance model from and to the file system
- Visualization (beta)
    - graphical export via GraphViz, DOT
    - PNG, JPG, ...
- Bigraph matching
- Bigraphical Reactive System support: simulation of the evolution of
bigraphs by reaction rules (synthesizing a labelled transition system) (beta)
    - Simulation
    - Predicate checking
    - Specify order of reaction rules
- Model transformation / Conversions (alpha)
    - into other graph formats, e.g., GraphML, GXL, and Ranked Graphs
    - for other bigraph tools: BigMC, BigraphER, and BigRed



## Getting Started

Here is a quick teaser of creating a pure concrete bigraph using _Bigraph Framework_ in Java.

### Lean Bigraph API

The lean bigraph API allows fast bigraph creation and composition.

To following usage assumes the import statement `import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*`.

```java
// create the signature
DefaultDynamicSignature signature = pureSignatureBuilder()
    .newControl("A", 0).assign()
    .newControl(StringTypedName.of("C"), FiniteOrdinal.ofInteger(1)).assign()
    .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).kind(ControlKind.ATOMIC).assign()
    .create();

// create two bigraphs
PureBigraph bigraph1 = pureBuilder(signature)
    .createRoot()
    .addChild("A").addChild("C")
    .createBigraph();

PureBigraph bigraph2 = pureBuilder(signature)
    .createRoot().addChild("User", "alice").addSite()
    .createBigraph();

// compose two bigraphs
BigraphComposite bigraphComposite = ops(bigraph2).compose(bigraph1);
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
BigraphArtifacts.exportAsMetaModel(bigraph, new FileOutputStream("./meta-model.ecore"));
BigraphArtifacts.exportAsInstanceModel(bigraph, new FileOutputStream("./instance-model.xmi"));
```

See the reference and documentation for a more comprehensive overview.

#### **Bigraph Composition**

To get the composition and tensor product of two bigraphs:
```java
PureBigraph G = ...;
PureBigraph F = ...;
PureBigraph H = ...;
BigraphComposite<DefaultDynamicSignature> compositor = ops(G);

BigraphComposite<DefaultDynamicSignature> result = compositor.compose(F);
compositor.juxtapose(F);
compositor.juxtapose(F).parallelProduct(H);
```

## Maven configuration

```xml
<!-- the core module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-core</artifactId>
  <version>${version}</version>
</dependency>
<!-- the rewriting module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-simulation</artifactId>
  <version>${version}</version>
</dependency>
<!-- the visualization module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-visualization</artifactId>
  <version>${version}</version>
</dependency>
<!-- the converter module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-converter</artifactId>
  <version>${version}</version>
</dependency>
```

The following Maven remote repository must be added as well. 

There are two options:

### Remote repositories

Artifacts are deployed to the [ST-Group's Artifactory](https://stgroup.jfrog.io/).
To resolve the dependencies above, the following remote repository must be configured.

##### A) Within the `pom.xml` of a Maven project:
```xml
<-- Default -->
<repository>
    <snapshots>
        <enabled>true</enabled> <!-- set false to disable snapshot releases -->
    </snapshots>
    <id>STFactory</id>
    <name>st-tu-dresden-artifactory</name>
    <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/</url>
</repository>
```

##### B) Via the [`settings.xml`](https://maven.apache.org/ref/3.6.3/maven-settings/settings.html) of your Maven local repository `~/.m2/`:

```xml
<!-- ... -->
<profiles>
    <profile>
        <repositories>
            <repository>
                <snapshots>
                    <enabled>false</enabled>
                </snapshots>
                <id>STFactory-release</id>
                <name>st-tu-dresden-releases</name>
                <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-release</url>
            </repository>
            <repository>
                <snapshots/>
                <id>STFactory-snapshot</id>
                <name>st-tu-dresden-snapshots</name>
                <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-snapshot</url>
            </repository>
        </repositories>
        <id>artifactory</id>
    </profile>
</profiles>
<activeProfiles>
    <activeProfile>artifactory</activeProfile>
</activeProfiles>
<!-- ... -->
```

> See also [Building from Source](#Building-the-Framework-from Source) if you want to build the source by yourself and host them in your Maven local repository.

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

## Details

### Modules

A brief description of each module's purpose is given below.

#### bigraph-core

Provides builders, factories and interfaces to create concrete bigraphs and elementary bigraphs.

Concrete Bigraphs and their meta model (with the signature only) can be written/loaded to/from the file system.

#### bigraph-simulation

Simulate bigraphs by creating bigraphical reactive systems, reaction rules and agents.
Check a system according to some specification by defining various types of predicates.

#### bigraph-visualization

Provides simple means to export bigraphs and transition systems as graphic files.

Currently, DOT is used in combination with GraphViz. Bigraphs can be exported as `*.png` and `*.jpg`.

#### bigraph-converter

Provides several mechanism to convert bigraphs into other representations.
For example, bigraphs to GXL (Graph Exchange Language), BigraphER and BigMC.

<!-- ### Reaction Graph / Transition System -->

<!-- TODO: some details about the algorithm -->

<!-- ### Bigraph Matching -->

<!-- TODO: outline algorithm -->

### Bigraph Meta Model

**User-Friendly API**

- Internally, bigraphs are described by a meta model based on Ecore. The project can
be found in [this](https://git-st.inf.tu-dresden.de/bigraphs/ecore-bigraph-meta-model)
Gitlab repository. To create concrete bigraphs, a signature must be provided.
To do so, this meta model is extended when creating a new bigraphical signature
which is then called "meta model over a signature" of an abstract bigraph
(described by the Ecore model). We say that the signature is mapped to
the meta model over a signature. From that, multiple instance models can
be created where the instance bigraph relates to the signature _S_, thus,
corresponds to the meta model over the signature _S_.
- Extending the meta model with a signature by hand is time-consuming
especially when many models are created. The framework allows to create
bigraphs dynamically at runtime by letting the user providing a description
of the signature. The meta model over a signature is kept in memory and
instances can be created from it. As a result, the bigraph meta model must
not be touched manually. Both the meta model over a signature and the
instance model can be stored on the filesystem.
- That very meta model serves only as a data model for the *Bigraph Framework*
which provides additional functionality and a user-friendly API for the
creation and simulation of bigraphical reactive systems. Furthermore, we
achieve Separation of concerns: The meta model itself is implementation-agnostic.
The Bigraph Framework adds specific behavior superimposed upon this meta
model. Meaning, the implementation-specific details are kept out from the meta model.

## Build Configuration

It is not necessary to build from source to use *Bigraph Framework* but if you want to try out the latest version, the project can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper) or the regular `mvn` command. In this case, JDK 1.8 is needed.

> **Note:** The required version of Maven is 3.6.3

The recommendation here is to build it with the regular `mvn` command. You will need [Maven v3.6.3 or above](https://maven.apache.org/run-maven/index.html).

> **Tip:** A script called `install-maven.sh` can be found in the `./etc/ci/` folder to automatically install Maven 3.6.3.

### Building the Framework from Source

The following command must be executed from the root directory of this project:

```bash
# Default
$ mvn clean install -DskipTests

# To create a "fat jar" for each module, run:
$ mvn clean install -DskipTests -PfatJar
```

After the command successfully finished, you can now use _Bigraph Framework_ in other Java projects. 
Therefore, see [Maven configuration](#maven-configuration) on how to include the individual _Bigraph Framework_ dependencies.

### Building the Documentation

Building the documentation builds also the project without running tests.
You may need to execute the above steps before.

```bash
$ mvn clean package exec:java -f documentation/pom.xml && mvn install -f documentation/pom.xml -Pdistribute
```

#### Java Documentation

The generated apidoc is available from `etc/doc/docusaurus/website/static/apidocs`.

#### User Manual

The generated user manual is available from `etc/doc/docusaurus/website/` by calling `npm start`.

```bash
$ cd ./documentation/docusaurus/website
$ npm start
```

The manual is generated using [docusaurus](https://docusaurus.io/), which must be installed on the system (see [Development-and-Deployment.md](etc/Development-and-Deployment.md) for further instructions).

### Development and Deployment

See the document [etc/Development-and-Deployment.md](./etc/Development-and-Deployment.md) for more issues regarding the development and deployment of _Bigraph Framework_.

To deploy Bigraph Framework to the [ST-Group's Artifactory](https://stgroup.jfrog.io/):
```bash
mvn deploy -DskipTests -Pdeploy -Dusername=username -Dpassword=password
```

## License

**Bigraph Framework** is Open Source software released under the Apache 2.0 license.

```text
   Copyright 2020 Dominik Grzelak

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