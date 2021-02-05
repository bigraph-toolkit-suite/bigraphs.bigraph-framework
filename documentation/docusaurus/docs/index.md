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

- Dynamic creation of bigraphs at runtime based on an Ecore meta model; support of various bigraph operators
- Visualization (beta)
- Bigraph matching (beta)
- Bigraphical reactive system support: simulation of bigraphs by reaction rules and synthesisation of a transition system (beta)
- Read and write meta and instance model to the file system
- Conversion of bigraphs to other file formats (e.g., GraphML, BigMC or BigraphER)
<!-- - Model transformation (WIP) -->

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
</dependency>
<!-- the rewriting module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-simulation</artifactId>
  <version>${revision}</version>
</dependency>
<!-- the visualization module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-visualization</artifactId>
  <version>${revision}</version>
</dependency>
<!-- the converter module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-converter</artifactId>
  <version>${revision}</version>
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

The following Maven repository settings must be added to the `<repositories/>` element of a `pom.xml` for a new project.

```xml
<repository>
    <snapshots>
        <enabled>true</enabled> <!-- set false to disable snapshot releases -->
    </snapshots>
    <id>STFactory</id>
    <name>st-tu-dresden-artifactory</name>
    <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/</url>
</repository>
```

Another option is to modify the `settings.xml`, usually found in `~/.m2/`.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    <profiles>
        <profile>
            <id>stgroup-artifactory</id>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>true</enabled> <!-- set false to disable snapshot releases -->
                    </snapshots>
                    <id>STFactory</id>
                    <name>st-tu-dresden-artifactory</name>
                    <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/</url>
                </repository>
            </repositories>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>stgroup-artifactory</activeProfile>
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
        url  "https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/" 
    }
}
```

<!--END_DOCUSAURUS_CODE_TABS-->

### Logging Configuration

Bigraph Framework employs SLF4J as a facade for the log4j logging framework.

Depending on your project setup, you may need to include the following libraries in your `pom.xml` :

```xml
<!-- When used within a Spring project -->
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

<!--## Changelogs-->

<!--- Version: [v0.6-SNAPSHOT](changelogs/changelog_v0.6-SNAPSHOT.txt)-->