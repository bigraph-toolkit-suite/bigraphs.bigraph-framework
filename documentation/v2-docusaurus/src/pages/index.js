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
             style={{paddingTop: '2em'}}>
            <h2>Download & Use Bigraph Framework for Java</h2>
            <ul>
                <li>Maven Repository on <a
                    href={`https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/`}>Artifactory</a>
                </li>
                <li>Source code on <a
                    href={`https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/`}>GitLab</a></li>
                <li>Source code on <a href={`https://github.com/st-tu-dresden/bigraph-framework`}>GitHub
                    (mirror)</a></li>
            </ul>
            <i>Project Skeletons that get you started:</i>
            <ul>
                <li><a
                    href={`https://www.bigraphs.org/products/bigraph-framework/download/empty-project-skeleton-bigraphframework.zip`}>Maven-based
                    Project Skeleton for using Bigraph Framework</a></li>
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
                    <Link
                        className="button button--secondary button--lg"
                        to="#try">Try It Out!
                    </Link>
                    <Link
                        className="button button--secondary button--lg"
                        to="/docs/">View the User Manual
                    </Link>
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
            <div>
                This framework provides 4 central building blocks. Some of their features are:
            </div>
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
                <BigraphRepositoryBlock/>
                <BigraphDependencyBlock/>
            </main>
        </Layout>
    );
}
