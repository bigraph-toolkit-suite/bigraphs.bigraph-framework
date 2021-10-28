import styles from "./BigraphDependencyBlock.module.css";
import React from "react";
import clsx from "clsx";
import BigraphMarkdownBlock from "./BigraphMarkdownBlock"

const mavenDepCode = `
<!-- the core module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-core</artifactId>
  <version>VERSION</version>
</dependency>
<!-- the rewriting module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-simulation</artifactId>
  <version>VERSION</version>
</dependency>
<!-- the visualization module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-visualization</artifactId>
  <version>VERSION</version>
</dependency>
<!-- the converter module -->
<dependency>
  <groupId>de.tudresden.inf.st.bigraphs</groupId>
  <artifactId>bigraph-converter</artifactId>
  <version>VERSION</version>
</dependency>
`;

const gradleDepCode = `
compile "de.tudresden.inf.st.bigraphs:bigraph-core:VERSION"
compile "de.tudresden.inf.st.bigraphs:bigraph-simulation:VERSION"
compile "de.tudresden.inf.st.bigraphs:bigraph-visualization:VERSION"
compile "de.tudresden.inf.st.bigraphs:bigraph-converter:VERSION"
`;

export const GRADLE_DEP_CODE = gradleDepCode;
export const MAVEN_DEP_CODE = mavenDepCode;

export default function BigraphDependencyBlock() {
    return (
        <section>
            <div className="container">
                <div className="row">
                    <div className={clsx('col col--2 text--center')}>
                        <img src={require('../../static/img/icon-artifact.png').default}/>
                        <p>
                            Dependencies
                        </p>
                    </div>
                    <div className={clsx('col col--5')}>
                        <h2>
                            <div><span><p>Maven</p></span></div>
                        </h2>
                        <BigraphMarkdownBlock exampleCode={mavenDepCode} language="xml"/>
                    </div>
                    <div className={clsx('col col--5')}>
                        <h2>
                            <div><span><p>Gradle</p></span></div>
                        </h2>
                        <BigraphMarkdownBlock exampleCode={gradleDepCode} language="jsx"/>
                    </div>
                </div>
            </div>
        </section>
    );
}