import styles from "./BigraphDependencyBlock.module.css";
import React from "react";
import clsx from "clsx";
import BigraphMarkdownBlock from "./BigraphMarkdownBlock"

const mavenDepCode = `
<!-- Core Module -->
<dependency>
  <groupId>org.bigraphs.framework</groupId>
  <artifactId>bigraph-core</artifactId>
  <version>2.3.5</version>
</dependency>
<!-- Simulation Module -->
<dependency>
  <groupId>org.bigraphs.framework</groupId>
  <artifactId>bigraph-simulation</artifactId>
  <version>2.3.5</version>
</dependency>
<!-- Visualization Module -->
<dependency>
  <groupId>org.bigraphs.framework</groupId>
  <artifactId>bigraph-visualization</artifactId>
  <version>2.3.5</version>
</dependency>
<!-- Converter Module -->
<dependency>
  <groupId>org.bigraphs.framework</groupId>
  <artifactId>bigraph-converter</artifactId>
  <version>2.3.5</version>
</dependency>
`;

const gradleDepCode = `
compile "org.bigraphs.framework:bigraph-core:2.3.5"
compile "org.bigraphs.framework:bigraph-simulation:2.3.5"
compile "org.bigraphs.framework:bigraph-visualization:2.3.5"
compile "org.bigraphs.framework:bigraph-converter:2.3.5"
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
