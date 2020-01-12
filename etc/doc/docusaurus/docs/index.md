---
id: index
sidebar_label: Introduction
title: Welcome to Bigraph Framework
---

<!--# Welcome to Bigraph Framework-->

A Complete Guide to the Bigraph Framework.

{@inject: revision}

> **Notice:** The API is subject to change. Use at your own risk.

## What is Bigraph Framework

**Bigraph Framework** is a software framework for the creation and simulation of bigraphs
to expedite the experimental evaluation of the bigraph theory in real-world applications.

The goal of this framework is to facilitate the implementation of context-aware and agent-based systems.
It provides means for model-driven software development based on the bigraph theory.
The high level API eases the programming of bigraphical systems for real-world application.

**Bigraphs** are an emerging graph theory and meta-model for global ubiquitous systems,
mobile computing and Internet of Things (IoT).
The theory provides a unifying framework of existing process calculi for concurrency,
including ambient calculus, action calculi, petri nets, Calculi of communicating
systems and π-calculus.
The mathematical theory enables equally the modeling of static structures
and dynamics of complex systems.

## Overview of the Features

- Dynamic creation of bigraphs at runtime based on an Ecore meta model
- Visualization (beta)
- Bigraph matching (beta)
- Bigraphical reactive system support: simulation of bigraphs by reaction rules (= transition system) (alpha)
- Read and write meta and instance model to file system
- Model transformation (WIP)

## Requirements

- Java
- Maven or Gradle

## Install / Dependency Settings

Artifacts are deployed to Bintray.

> #### Building from Source
> Otherwise, to build the source by yourself, follow the
> [README.md](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/blob/master/README.md)
> inside the repository.

### Packages

Depending on the build management tool you are using, one of the following
configuration for Maven or Gradle is necessary.


<!--DOCUSAURUS_CODE_TABS-->
<!--Maven-->
Add this inside the `<dependencies></dependencies>` section of your project `*.pom`:
{@inject: revisionVar}
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

<!--Gradle-->

Append this in your `build.gradle` file:
{@inject: revisionVar}
```gradle
// the core module
compile 'de.tudresden.inf.st.bigraphs:bigraph-core:${revision}'
// the rewriting module
compile 'de.tudresden.inf.st.bigraphs:bigraph-rewriting:${revision}'
// the visualization module 
compile 'de.tudresden.inf.st.bigraphs:bigraph-visualization:${revision}'
// the converter module 
compile 'de.tudresden.inf.st.bigraphs:bigraph-converter:${revision}'
```

<!--Manually by Classpath-->

not recommended

<!--END_DOCUSAURUS_CODE_TABS-->

### Repository Setup

In order to resolve the dependencies above, the following custom repository
must be added as well. We provide the configuration for Maven and Gradle below.

<!--DOCUSAURUS_CODE_TABS-->

<!--Maven-->

The following Maven repository settings must be added to the user's `settings.xml`, usually found
in `~/.m2/`.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    
    <profiles>
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
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-st-tu-dresden-maven-repository</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/st-tu-dresden/maven-repository</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
```
Alternatively, the project may be build manually. Please consult the `README.md` in the
source repository on how to do so.

<!--Gradle-->

For Gradle, add this to the project's build gradle file:

```gradle
repositories {
    maven {
        url  "https://dl.bintray.com/st-tu-dresden/maven-repository" 
    }
}
```

<!--END_DOCUSAURUS_CODE_TABS-->

<!--## Changelogs-->

<!--- Version: [v0.6-SNAPSHOT](changelogs/changelog_v0.6-SNAPSHOT.txt)-->