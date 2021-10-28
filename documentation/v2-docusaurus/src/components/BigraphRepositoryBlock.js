import styles from "./BigraphRepositoryBlock.module.css";
import React from "react";
import clsx from "clsx";
import BigraphMarkdownBlock from "./BigraphMarkdownBlock"

const mavenRepoCode = `
<repository>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
    <id>STFactory</id>
    <name>st-tu-dresden-artifactory</name>
    <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/</url>
</repository>
`;

const mavenRepoCode2 = `
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
`;

const gradleRepoCode = `
repositories {
    maven {
        url "https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/"
    }
}
`;

export const GRADLE_REP_CODE = gradleRepoCode;
export const MAVEN_REP_CODE = mavenRepoCode;
export const MAVEN_REP_CODE2 = mavenRepoCode2;

export default function BigraphRepositoryBlock() {
    return (
        <section>
            <div className="container">
                <div className="row">
                    <div className={clsx('col col--2 text--center')}>
                        <img src={require('../../static/img/icon-remote-repository.png').default}/>
                        <p>
                            Remote Repository Setup
                        </p>
                    </div>
                    <div className={clsx('col col--5')}>
                        <h2>
                            <div><span><p>Maven</p></span></div>
                        </h2>
                        <BigraphMarkdownBlock exampleCode={mavenRepoCode} language="xml"/>
                    </div>
                    <div className={clsx('col col--5')}>
                        <h2>
                            <div><span><p>Gradle</p></span></div>
                        </h2>
                        <BigraphMarkdownBlock exampleCode={gradleRepoCode} language="jsx"/>
                    </div>
                </div>
            </div>
        </section>
    );
}