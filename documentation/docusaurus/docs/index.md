---
id: index
sidebar_label: Introduction
title: Welcome to Bigraph Framework
---

A Complete Guide to the Bigraph Framework.

{@inject: revisionVar}

> **Notice:** The API is subject to change. Use at your own risk.

## What is Bigraph Framework

**Bigraph Framework** is a software framework for the creation and simulation of bigraphs
to expedite the experimental evaluation of the bigraph theory in real-world applications.

The goal of this framework is to facilitate the implementation of context-aware, agent-based systems or cyber-physical systems, for example.
The high level API eases the programming of bigraphical systems for various real-world application.
The framework is developed around a _metamodel-first approach_.
It provides several means for model-driven software development to integrate the bigraph theory into software.

### Overview of the Features

- Dynamic creation of bigraphs at runtime, which are based on the Ecore metamodel
- Visualization of bigraphs (beta)
- Bigraph Matching
- Bigraphical Reactive Systems: Simulation of bigraphs by reaction rules and synthesisation of a labelled transition system
- Read and write meta and instance models to the file system (Ecore/XMI)
- Conversion of bigraphs into other file formats (e.g., GraphML, BigMC or BigraphER)
<!-- - Model transformation (WIP) -->

### What are Bigraphs?

**Bigraphs** are an emerging graph theory and meta-model for global ubiquitous systems,
mobile computing, context-aware systems and the Internet of Things (IoT).
The theory is a unifying framework for many process calculi, including the ambient calculus, action calculi, Petri nets, 
the Calculus of Communicating Systems (CCS) and π-calculus.

## Requirements

- Java
- Maven or Gradle


## Use / Install / Dependency Settings

Bigraph Framework is online available as a Maven dependency.

Artifacts are deployed to [ST-Artifactory](https://stgroup.jfrog.io/).

> #### Building from Source
> Otherwise, to build the source by yourself, follow the
> [README.md](https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/blob/master/README.md)
> inside the repository.
>

### Empty Project Skeleton

To get started using Bigraph Framework right away, download the Maven-based Project Skeleton

- [Empty Project Skeleton for using Bigraph Framework](https://www.bigraphs.org/products/bigraph-framework/download/empty-project-skeleton-bigraphframework.zip)

The README contained therein, describes how to compile and start an application.

Alternatively, follow the step-by-step project setup as described in the following.

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