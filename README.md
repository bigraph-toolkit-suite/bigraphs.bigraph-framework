# Bigraphical Framework 

**Version:** ${revision}
Master: [![pipeline status](/../badges/master/pipeline.svg)](/../pipelines)
Develop: [![pipeline status](/../badges/develop/pipeline.svg)](/../pipelines)

-----

A framework for the creation and simulation of bigraphs to expedite the experimental evaluation of the bigraph theory in
real-world applications.

The goal of this framework is to facilitate the implementation of context-aware and agent-based systems.
It provides means for model-driven software development based on the bigraph theory.
The high level API eases the programming of bigraphical systems for real-world application.

**Features**

- Dynamic creation of bigraphs at runtime based on an EMOF-based meta model
- Read and write the meta and instance model from and to the file system
- Visualization (beta)
    - graphical export via GraphViz, DOT
    - PNG, JPG, ...
- Bigraph matching (beta) 
- Bigraphical Reactive System support: simulation of the evolution of 
bigraphs by reaction rules (synthesizing a labelled transition system) (alpha)
    - simulation
    - predicate checking
    - order of reaction rules
- Model transformation / Conversions (alpha) 
    - e.g., to GXL, BigMC and BigraphER



## Getting Started

Here is a quick teaser of creating a pure concrete bigraph using _Bigraph Framework_ in Java:

### Creating Bigraphs

#### Factory
A factory is necessary for accessing related builder instances for a specific kind of bigraph.
The factory depends on the kind of bigraph and its _signature_.
Depending on the bigraph type, the factory will return specialized methods.  

```java
// create factory for pure bigraphs (signatures and concrete bigraph instances)
PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
```

#### Signature and Bigraph Builder

The builder is responsible for instantiating and configuring concrete bigraph instances,
depending on the specific _signature_.
```java
// create signature
DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();
signatureBuilder   
    .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
    .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(0)).assign()
    .newControl().kind(ControlKind.PASSIVE).identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
;

DefaultDynamicSignature signature = signatureBuilder.create();
// create a bigraph builder and supply the signature
PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
BigraphEntity.OuterName a = builder.createOuterName("a");
BigraphEntity.InnerName b = builder.createInnerName("b");
builder.createRoot().addChild("User").linkToOuter(a)
.addChild("User", "a")
.addChild(signature.getControlByName("User")).linkToInner(b);

// create a concrete bigraph instance
PureBigraph bigraph = builder.createBigraph();
```

### Other APIs

#### Lean Bigraph API

Here is a brief summary of the new lean bigraph API which allows faster
bigraph creation and composition.

To following usage assumes the import statement `import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*`.

```java
// create the signature
DefaultDynamicSignature signature = pureSignatureBuilder()
    .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(0)).assign()
    .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(0)).assign()
    .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
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

#### **Bigraph Builder: Connecting nodes by links**

The bigraph builder provides more utility methods helping to build more
complex structures easily.

The following one shows, how to create nodes, and at the same time connecting them all with an edge:

```java
builder.createRoot().connectByEdge(signature.getControlByName("Job"),
                                signature.getControlByName("Job"),
                                signature.getControlByName("Job")); 
```

Now, we want to connect nodes located at different "places". Therefore, we
link them through an inner name, and
after, close the link to automatically transform it to an edge:
```java
// First, create an inner name
BigraphEntity.OuterName tmp_link = builder.createInnerName("link");

// Create two nodes within different hierarchies
builder.createRoot().addChild("Printer").linkToInner(tmp_link);
builder.createRoot().addChild("Computer").linkToInner(tmp_link);

// Finally, close the inner name. This will leave the edge intact.
builder.closeInnerName(tmp_link);
```

#### **Exporting the meta-model and instance model**

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
BigraphComposite<DefaultDynamicSignature> compositor = factory.asBigraphOperator(G);

BigraphComposite<DefaultDynamicSignature> result = compositor.compose(F);
compositor.juxtapose(F);
compositor.juxtapose(F).parallelProduct(H);
```

### Maven configuration

```xml
<!-- the core module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-core</artifactId>
  <version>${revision}</version>
  <type>pom</type>
</dependency>
<!-- the rewriting module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-simulation</artifactId>
  <version>${revision}</version>
  <type>pom</type>
</dependency>
<!-- the visualization module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-visualization</artifactId>
  <version>${revision}</version>
  <type>pom</type>
</dependency>
<!-- the converter module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-converter</artifactId>
  <version>${revision}</version>
  <type>pom</type>
</dependency>
```

The following remote Maven repository must be added as well. There are two options:
- A) Within the `pom.xml` of a Maven project:
```xml
    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>bintray-st-tu-dresden-maven-repository</id>
            <name>bintray</name>
            <url>https://dl.bintray.com/st-tu-dresden/maven-repository</url>
        </repository>
    </repositories>
```

- B) Via the `settings.xml` of your Maven local repository `~/.m2/`:

```xml
    <profiles>
        <!-- possibly other profiles -->
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-st-tu-dresden-maven-repository</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/st-tu-dresden/maven-repository</url>
                </repository>
            </repositories>
            <id>bintray-st-tu-dresden</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray-st-tu-dresden</activeProfile>
    </activeProfiles>
```

The [Bintray](https://bintray.com/) service is used for hosting Maven artifacts.
See also [Building from Source](#Building-from-Source) if you want to build the source
by yourself and host them in the local Maven repository. Then, both steps _A)_ and _B)_ are not needed.

## Details

### Bigraph Meta Model 

**User-Friendly API**

- Internally, bigraphs are described by a meta model based on Ecore. The project can be found in [this](https://git-st.inf.tu-dresden.de/bigraphs/ecore-bigraph-meta-model) Gitlab repository. To create concrete bigraphs, a signature must be provided. To do so, this meta model is extended when creating a new bigraphical signature which is then called "meta model over a signature" of an abstract bigraph (described by the Ecore model). We say that the signature is mapped to the meta model over a signature. From that, multiple instance models can be created where the instance bigraph
relates to the signature _S_, thus, corresponds to the meta model over the signature _S_.
- Extending the meta model with a signature by hand is time-consuming especially when many models are created.
The framework allows to create bigraphs dynamically at runtime by letting the user providing a description of the 
signature. The meta model over a signature is kept in memory and instances can be created from it.
As a result, the bigraph meta model must not be touched manually.
Both the meta model over a signature and the instance model can be stored on the filesystem.
- That very meta model serves only as a data model for the *Bigraph Framework* which provides additional functionalities 
and a user-friendly API for the creation and simulation of bigraphical reactive systems. 
Furthermore, we achieve Separation of concerns: The meta model itself is implementation-agnostic. The Bigraph Framework adds specific behavior superimposed upon this meta model. Meaning, the implementation-specific details are kept out from the meta model.


### Modules

TODO

#### core

Provides builder, factories and interfaces to create concrete bigraphs and elementary bigraphs.

Concrete Bigraphs and their meta model (with the signature only) can be written to the file system for inspection.

#### rewriting

Create reaction rules and bigraphical reactive systems. Simulate bigraphs. Define Predicates.

#### visualization

Provides simple means to export bigraphs and transition systems as graphic files.

Currently, DOT is used in combination with GraphViz. Bigraphs can be exported as `*.png` and `*.jpg`. 

#### Converters

Provides several mechanism to convert bigraphs into other representations.
For example, Bigraphs to GXL (Graph Exchange Language).

### Reaction Graph / Transition System

TODO: some details about the algorithm

### Bigraph Matching

TODO: outline algorithm

## Building from Source

It is not necessary to build from source to use the bigraphical meta model but if you want to try out the latest version, the project can be easily built with the [maven wrapper](https://github.com/takari/maven-wrapper). In this case, JDK 1.8 is needed.

```bash
$ ./mvnw clean install
```

If you want to build with the regular `mvn` command, you will need [Maven v3.5.0 or above](https://maven.apache.org/run-maven/index.html).

### Building reference documentation

TODO 

Building the documentation builds also the project without running tests.

```bash
$ ./mvnw clean install -Pdistribute
```

The generated documentation is available from `target/site/reference/html/index.html`.

The documentation is built using mkdocs and must be installed on the system (see [DEPLOYMENT](etc/ci/DEPLOYMENT.md)
for further instructions).

### Development and Deployment

See the document [DEPLOYMENT](etc/ci/DEPLOYMENT.md) for more issues regarding the development
and deployment of _Bigraph Framework_. 

## License

Bigraph Framework is Open Source software released under the Apache 2.0 license.

```text
   Copyright [2019] Dominik Grzelak

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