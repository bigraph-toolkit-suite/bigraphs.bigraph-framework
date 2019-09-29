# Bigraphical Framework 

**Version:** ${revision}

-----

A framework for the creation and simulation of bigraphs to expedite the experimental evaluation of the bigraph theory in
real-world applications.

The goal of this framework is to facilitate the implementation of context-aware or agent-based systems.
It provides means for model-driven software development based on the bigraph theory.
The high level API eases the programming of bigraphical systems for real-world application.

**Features**

- Dynamic creation of bigraphs at runtime based on an Ecore meta model
- Bigraph matching (beta) 
- Visualization (beta)
- Reaction system (= transition system, but no minimal transition system) (alpha)
- Read and write meta and instance model to file system (WIP)



## Getting Started

Here is a quick teaser of creating a pure concrete bigraph using _Bigraph Framework_ in Java:

```java
// create factory

// create signature

// create instance of a pure bigraph
```

### Maven configuration

```xml
<dependency>
  <groupId></groupId>
  <artifactId></artifactId>
  <version>${revision}</version>
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

Graphviz is used as format. Can be exported as png and jpg. 

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

The generated documentation is available from target/.../index.html.

The documentation is built using mkdocs.

### Development and Deployment

See the document [DEPLOYMENT](DEPLOYMENT.md) for more issues regarding the development
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