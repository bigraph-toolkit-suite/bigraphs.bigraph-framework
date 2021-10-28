import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
    {
        title: 'Modeling',
        Svg: require('../../static/img/icon-bigraph-creation.png').default,
        description: (
            <>
                Dynamically create bigraph models at design and run-time. Compose bigraphs to build more complex
                bigraphs quickly. Bigraph models are based on the *Ecore* standard and conform to a meta-model.
            </>
        ),
    },
    {
        title: 'Simulation and Verification',
        Svg: require('../../static/img/icon-bigraph-simulation.png').default,
        description: (
            <>
                Create a bigraphical reactive system with agents and reaction rules to dynamically evolve a
                user-defined system. Perform model checking tasks by defining various kinds of predicates.
            </>
        ),
    },
    {
        title: 'Interoperability',
        Svg: require('../../static/img/icon-bigraph-interop.png').default,
        description: (
            <>
                Export your bigraphs and bigraphical reactive systems as Ecore-based models or any other format such as
                GraphML, GXL, BigraphER, BigMC or BigRed.
            </>
        ),
    },
    {
        title: 'Visualization',
        Svg: require('../../static/img/icon-bigraph-vizu.png').default,
        description: (
            <>
                Display your bigraphs graphically and export them as PNG or SVG.
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
