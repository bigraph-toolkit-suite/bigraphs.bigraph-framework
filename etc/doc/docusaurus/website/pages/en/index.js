/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');

const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;

class HomeSplash extends React.Component {
    render() {
        const {siteConfig, language = ''} = this.props;
        const {baseUrl, docsUrl} = siteConfig;
        const docsPart = `${docsUrl ? `${docsUrl}/` : ''}`;
        const langPart = `${language ? `${language}/` : ''}`;
        const docUrl = doc => `${baseUrl}${docsPart}${langPart}${doc}`;

        const SplashContainer = props => (
            <div className="homeContainer">
                {/*<Logo img_src={`${baseUrl}img/bigraph-framework-logo.png`}/>*/}
                <div className="homeSplashFade">
                    <div className="wrapper homeWrapper">{props.children}</div>
                </div>
            </div>
        );

        const Logo = props => (
            <div className="projectLogo" style={taglineStyle}>
                <img src={props.img_src} alt="Project Logo"/>
            </div>
        );

        const taglineStyle = {};

        const ProjectTitle = () => (
            <h2 className="projectTitle">
                {siteConfig.title}
                {/*<small>{siteConfig.tagline}</small>*/}
            </h2>
        );

        const PromoSection = props => (
            <div className="section promoSection">
                <div className="promoRow">
                    <div className="pluginRowBlock">{props.children}</div>
                </div>
            </div>
        );

        const Button = props => (
            <div className="pluginWrapper buttonWrapper">
                <a className="button" href={props.href} target={props.target}>
                    {props.children}
                </a>
            </div>
        );

        return (
            <SplashContainer>
                <Logo img_src={`${baseUrl}img/bigraph-framework-logo.png`}/>
                <div className="inner">
                    <ProjectTitle siteConfig={siteConfig}/>
                    <div className="customTagline">
                        <small>{siteConfig.tagline}</small>
                    </div>
                    <PromoSection>
                        <Button href="#try">Try It Out</Button>
                        <Button href={docUrl('index.html')}>View the User Manual</Button>
                        {/*<Button href={docUrl('doc2.html')}>Example Link 2</Button>*/}
                    </PromoSection>
                </div>
            </SplashContainer>
        );
    }
}

class Index extends React.Component {

    render() {
        const {config: siteConfig, language = ''} = this.props;
        const {baseUrl} = siteConfig;

        const Block = props => (
            <Container
                padding={['bottom', 'top']}
                id={props.id}
                background={props.background}>
                <GridBlock
                    align="center"
                    contents={props.children}
                    layout={props.layout}
                />
            </Container>
        );
        const TryOut = () => {
            const url = siteConfig.repoUrl;
            return (

                <Block background="light" id="try">
                    {[
                        {
                            title: 'Download Bigraph Framework for Java',
                            content:
                            '<p>Bintray: ' +
                                '<a href="https://bintray.com/st-tu-dresden/maven-repository">https://bintray.com/st-tu-dresden/maven-repository</a>' +
                                '</p>' +
                                '<p>GitHub: ' +
                                '<a href="https://github.com/st-tu-dresden/bigraph-framework">https://github.com/st-tu-dresden/bigraph-framework</a>' +
                                '</p>',
                            image: `${baseUrl}img/icon-download.png`,
                            imageAlign: 'left',
                        },
                    ]}
                </Block>
            )
        };

        const Description = () => (
            <Block background="dark">
                {[
                    {
                        content:
                            'This is another description of how this project is useful',
                        image: `${baseUrl}img/undraw_note_list.svg`,
                        imageAlign: 'right',
                        title: 'Description',
                    },
                ]}
            </Block>
        );

        const LearnHow = () => (
            <Block background="light">
                {[
                    {
                        content:
                            'Each new Docusaurus project has **randomly-generated** theme colors.',
                        image: `${baseUrl}img/undraw_youtube_tutorial.svg`,
                        imageAlign: 'right',
                        title: 'Randomly Generated Theme Colors',
                    },
                ]}
            </Block>
        );

        const FeatureCallout = () => (
            <div
                className="productShowcaseSection paddingBottom lightBackground"
                style={{textAlign: 'center', paddingTop: '2em'}}>
                <h2>Features</h2>
                <MarkdownBlock>These are some of the features of this framework</MarkdownBlock>
            </div>
        );

        const Features = () => (
            <Block layout="fourColumn">
                {[
                    {
                        content: 'Dynamically create bigraph models at design and run-time. Compose bigraphs to build more complex bigraphs quickly. Bigraph models are based on the *Ecore* standard and conform to a meta-model.',
                        image: `${baseUrl}img/icon-bigraph-creation.png`,
                        imageAlign: 'top',
                        title: 'Modeling',
                    },
                    {
                        content: 'Create a *bigraphical reactive system* with agents and reaction rules to dynamically evolve a user-defined' +
                            ' system. Perform model checking tasks by defining various kinds of predicates.',
                        image: `${baseUrl}img/icon-bigraph-simulation.png`,
                        imageAlign: 'top',
                        title: 'Simulation and Verification',
                    },
                    {
                        content: 'Export your bigraphs and bigraphical reactive systems as Ecore-based models or any other format such as ' +
                            'GraphML, GXL, BigraphER, BigMC or BigRed.',
                        image: `${baseUrl}img/icon-bigraph-interop.png`,
                        imageAlign: 'top',
                        title: 'Interoperability',
                    },
                    {
                        title: 'Visualization',
                        content: 'Display your bigraphs graphically and export them as `PNG` or `SVG`.',
                        image: `${baseUrl}img/icon-bigraph-vizu.png`,
                        imageAlign: 'top',
                    },
                ]}
            </Block>
        );

        const Showcase = () => {
            if ((siteConfig.users || []).length === 0) {
                return null;
            }

            const showcase = siteConfig.users
                .filter(user => user.pinned)
                .map(user => (
                    <a href={user.infoLink} key={user.infoLink}>
                        <img src={user.image} alt={user.caption} title={user.caption}/>
                    </a>
                ));

            const pageUrl = page => baseUrl + (language ? `${language}/` : '') + page;

            return (
                <div className="productShowcaseSection paddingBottom">
                    <h2>Who is Using This?</h2>
                    <p>This project is used by all these people</p>
                    <div className="logos">{showcase}</div>
                    <div className="more-users">
                        <a className="button" href={pageUrl('users.html')}>
                            More {siteConfig.title} Users
                        </a>
                    </div>
                </div>
            );
        };

        return (
            <div>
                <HomeSplash siteConfig={siteConfig} language={language}/>
                <div className="mainContainer">
                    <FeatureCallout/>
                    <Features/>
                    {/*<LearnHow/>*/}
                    <TryOut/>
                    {/*<Description/>*/}
                    {/*<Showcase/>*/}
                </div>
            </div>
        );
    }
}

module.exports = Index;
