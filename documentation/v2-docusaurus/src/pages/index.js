import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/HomepageFeatures';
import BigraphRepositoryBlock from "../components/BigraphRepositoryBlock";
import BigraphDependencyBlock from "../components/BigraphDependencyBlock";

function TryItOut() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <div id={`try`}
             className="text--center padding-horiz--md"
             style={{paddingTop: '2em', backgroundColor: '#efefef', paddingBottom: '1.8em', marginBottom: '2em'}}>
            <h2>Download & Use Bigraph Framework</h2>
            <ul style={{listStyleType: "none"}}>
                <li>The framework is available via the <a
                    href={`https://central.sonatype.com/`}>Maven Central Repository</a>.
                </li>
                <li>Source code is available at <a href={`https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-framework`}>GitHub</a>.</li>
            </ul>
            <strong>Quickstart</strong>
            <ul style={{listStyleType: "none"}}>
                <li>(ZIP Archive) <a
                    href={`https://www.bigraphs.org/software/bigraph-framework/download/Project-Skeleton-for-Bigraph-Framework.zip`}>Project Template (Maven/Gradle)</a></li>
                <li>(GitHub) <a href={`https://github.com/bigraph-toolkit-suite/Project-Skeleton-for-Bigraph-Framework`}>Project Template (Maven/Gradle)</a></li>
            </ul>
            {/*<p>*/}
            {/*    Latest Version: {bigraphFrameworkVersion}*/}
            {/*</p>*/}
        </div>
    );
}

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">{siteConfig.title}</h1>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div className={styles.buttons}>
                    <div className="row">
                    <Link
                        className="button button--secondary button--lg"
                        style={{"margin-right": "1em", "margin-top": "1em"}}
                        to="#try">Try It Out!
                    </Link>
                    <Link
                        className="button button--secondary button--lg"
                        style={{"margin-top": "1em"}}
                        to="/docs/">View User Manual
                    </Link>
                    </div>
                </div>
            </div>
        </header>
    );
}

function FeatureCalloutTitle() {
    return (
        <div
            className="text--center padding-horiz--md"
            style={{paddingTop: '2em'}}>
            <h2>Features</h2>
            {/*<div>*/}
            {/*    This framework provides 4 central building blocks.*/}
            {/*</div>*/}
        </div>
    );
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    // const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;
    return (
        <Layout
            title={`${siteConfig.title}`}
            description="Description will go into a meta tag in <head />">
            <HomepageHeader/>
            <main>
                <FeatureCalloutTitle/>
                <HomepageFeatures/>
                <TryItOut/>
                <BigraphDependencyBlock/>
                {/*<BigraphRepositoryBlock/>*/}
            </main>
        </Layout>
    );
}
