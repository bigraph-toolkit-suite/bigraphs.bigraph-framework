# Bigraphical Framework 

<hr/>
<small>
Internal project name: FrogLab - Piweka (Arch) <br/>
Version: 0.1-SNAPSHOT
</small>
<hr/>

A framework for the creation and simulation of bigraphs to expedite the experimental evaluation of the bigraph theory in
real-world applications.

The goal of this framework is to facilitate the implementation of context-aware or agent-based systems.
It provides means for model-driven software development based on the bigraph theory.
The high level API eases the programming of bigraphical systems for real-world application.

**Features**

- dynamic creation of bigraphs at runtime based on an Ecore meta model
- bigraph matching (beta) 
- visualization (beta)
- reaction system (= transition system, but no minimal transition system, alpha)
- simple model file store (WIP)

## Bigraph Meta Model 

Internally, bigraphs are described by a meta model based on Ecore.
This meta model is extended when creating a new bigraphical signature 
which is then called "meta model over a signature" of an abstract bigraph.
We say that the signature is mapped to the meta model over a signature.
From that, multiple instance models can be created where the instance bigraph
relates to the signature _S_, thus, corresponds to the meta model over the signature _S_.

This meta model serves only as a data model for the *Bigraph Framework* which provides  additional functionalities 
and a user-friendly API for the creation and simulation of bigraphical reactive systems.

Separation of concerns: 
The meta model itself is implementation-agnostic. 
The Bigraph Framework adds specific behavior superimposed upon this meta model.
Meaning, the implementation-specific details are kept out from the meta model.

## Getting Started

### User-Friendly API:

- Extending the model with a signature by hand is time-consuming especially when many models are created.
The framework allows to create bigraphs dynamically at runtime by letting the user providing a description of the 
signature. The meta model over a signature is kept in memory and instances can be created from it.
As a result, the bigraph meta model must not be touched manually.
Both the meta model over a signature and the instance model can be stored on the filesystem.
 
### Maven configuration

```xml
<dependency>
  <groupId></groupId>
  <artifactId></artifactId>
  <version>${version}</version>
</dependency>
```

The following Maven repository must be added as well:

```xml
<dependency>
  <groupId></groupId>
  <artifactId></artifactId>
  <version>${version}</version>
</dependency>
```

(See Section [Deployment](#Deployment) for further information on that) 


## Project Setup

### Deploy the bigraph model jar

A bigraph is an Ecore model that is developed within EMF. 
The model needs to be deployed to the local maven repository.

Change to the root folder of this project and execute:
```
mvn install:install-file -Dfile=./libs/bigraphModel.jar -DgroupId=de.tudresden.inf.st.bigraphs.model \
    -DartifactId=bigraph-ecore-model -Dversion=1.0 -Dpackaging=jar
```
This will install the bigraph model in your local Maven repository.
Do this only once or when the model changes. 

The model can now be used as a Maven dependency in other modules of this project and will be exported also in the 
generated *.jar later.

_v3: current
_v5: cdo migrated model
_v6: with extra BBigraph container object
