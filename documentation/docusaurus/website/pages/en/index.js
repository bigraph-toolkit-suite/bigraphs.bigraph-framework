/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

const React = require('react');
// var createReactClass = require('create-react-class');
const CompLibrary = require('../../core/CompLibrary.js');

const MarkdownBlock = CompLibrary.MarkdownBlock; /* Used to read markdown */
const Container = CompLibrary.Container;
const GridBlock = CompLibrary.GridBlock;
// var Markdown = require('react-remarkable');

var RemoteRepoCodeBlock = require('../../../../../core/BigraphRepositoryBlock')
var DependencyCodeBlock = require('../../../../../core/BigraphDependencyBlock')

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

        // const HowToUse = () => {
        //     return (
        //         <GridBlock
        //             align="center"
        //             layout="twoColumn"
        //             className="myCustomClass"
        //             contents={[
        //                 {
        //                     content: "ABC"
        //                 },
        //                 {
        //                     content: "DEF"
        //                 }
        //             ]}
        //         />
        //     )
        // };

        // const LearnHow = () => (
        //     <Block background="light">
        //         {[
        //             {
        //                 content:
        //                     'Each new Docusaurus project has **randomly-generated** theme colors.',
        //                 image: `${baseUrl}img/undraw_youtube_tutorial.svg`,
        //                 imageAlign: 'right',
        //                 title: 'Randomly Generated Theme Colors',
        //             },
        //         ]}
        //     </Block>
        // );

        // const Showcase = () => {
        //     if ((siteConfig.users || []).length === 0) {
        //         return null;
        //     }
        //
        //     const showcase = siteConfig.users
        //         .filter(user => user.pinned)
        //         .map(user => (
        //             <a href={user.infoLink} key={user.infoLink}>
        //                 <img src={user.image} alt={user.caption} title={user.caption}/>
        //             </a>
        //         ));
        //
        //     const pageUrl = page => baseUrl + (language ? `${language}/` : '') + page;
        //
        //     return (
        //         <div className="productShowcaseSection paddingBottom">
        //             <h2>Who is Using This?</h2>
        //             <p>This project is used by all these people</p>
        //             <div className="logos">{showcase}</div>
        //             <div className="more-users">
        //                 <a className="button" href={pageUrl('users.html')}>
        //                     More {siteConfig.title} Users
        //                 </a>
        //             </div>
        //         </div>
        //     );
        // };

        // const Description = () => (
        //     <Block background="dark">
        //         {[
        //             {
        //                 content:
        //                     'This is another description of how this project is useful',
        //                 image: `${baseUrl}img/undraw_note_list.svg`,
        //                 imageAlign: 'right',
        //                 title: 'Description',
        //             },
        //         ]}
        //     </Block>
        // );

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

        const FeatureCallout = () => (
            <div
                className="productShowcaseSection paddingBottom lightBackground"
                style={{textAlign: 'center', paddingTop: '2em'}}>
                <h2>Features</h2>
                <MarkdownBlock>
                    This framework provides 4 central building blocks. Some of their features are:
                </MarkdownBlock>
            </div>
        );

        const UsageCallout = () => (
            <div id={`try`}
                className="productShowcaseSection paddingBottom lightBackground"
                style={{textAlign: 'center', paddingTop: '2em'}}>
                <h2>Download & Use Bigraph Framework for Java</h2>
                <ul>
                    <li>Maven Repository on <a href={`https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/`}>Artifactory</a> </li>
                    <li>Source code on <a href={`https://git-st.inf.tu-dresden.de/bigraphs/bigraph-framework/`}>GitLab</a></li>
                    <li>Source code on <a href={`https://github.com/st-tu-dresden/bigraph-framework`}>GitHub (mirror)</a></li>
                </ul>
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

        return (
            <div>
                <HomeSplash siteConfig={siteConfig} language={language}/>
                <div className="mainContainer">
                    <FeatureCallout/>
                    <Features/>
                    <UsageCallout/>
                    <RemoteRepoCodeBlock img={`${baseUrl}img/icon-remote-repository.png`} />
                    <DependencyCodeBlock img={`${baseUrl}img/icon-artifact.png`} />
                    {/*<LearnHow/>*/}
                    {/*<TryOut/>*/}
                    {/*<HowToUse/>*/}
                    {/*<Description/>*/}
                    {/*<Showcase/>*/}
                </div>
            </div>
        );
    }
}

module.exports = Index;
