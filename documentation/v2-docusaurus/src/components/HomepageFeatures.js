import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
    {
        title: 'Modeling',
        Svg: require('../../static/img/icon-bigraph-creation.png').default,
        description: (
            <>
                Easily create and manipulate bigraph models at both design-time and run-time. Compose smaller bigraphs into complex structures to model sophisticated systems with ease. All bigraph models adhere to the Ecore standard and conform to a well-defined meta-model, ensuring compatibility and consistency.
            </>
        ),
    },
    {
        title: 'Simulation and Verification',
        Svg: require('../../static/img/icon-bigraph-simulation.png').default,
        description: (
            <>
                Design bigraphical reactive systems by defining agents and reaction rules that drive dynamic system evolution. Leverage built-in tools for model checking by specifying various types of predicates to analyze and verify system properties.
            </>
        ),
    },
    {
        title: 'Interoperability',
        Svg: require('../../static/img/icon-bigraph-interop.png').default,
        description: (
            <>
                Export your bigraph models and bigraphical reactive systems in multiple formats, including Ecore-based models, GraphML, GXL, BigraphER, BigMC, and BigRed. This flexibility ensures seamless integration with other tools and workflows.
            </>
        ),
    },
    {
        title: 'Visualization',
        Svg: require('../../static/img/icon-bigraph-vizu.png').default,
        description: (
            <>
                Visualize your bigraphs in different styles and formats.
                Export visual representations in popular formats such as PNG and SVG for documentation, presentations, and further analysis.
                Interactive visualization is also supported via GraphStream, enabling dynamic exploration of bigraphs in real time.
            </>
        ),
    },
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--3')}>
            <div className="text--center">
                <img className={styles.featureSvg} src={Svg} alt={title}/>
                {/*<Svg className={styles.featureSvg} alt={title}/>*/}
            </div>
            <div className="text--center padding-horiz--md">
                <h3>{title}</h3>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
