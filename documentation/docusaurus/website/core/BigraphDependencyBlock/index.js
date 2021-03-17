const React = require('react');

const SH = require('react-syntax-highlighter');
var SyntaxHighlighter = SH.PrismLight

class BigraphDependencyBlock extends React.Component {
    render() {
        const codeString = '<!-- the core module -->\n' +
            '<dependency>\n' +
            '  <groupId>de.tudresden.inf.st.bigraphs</groupId>\n' +
            '  <artifactId>bigraph-core</artifactId>\n' +
            '  <version>${version}</version>\n' +
            '</dependency>\n' +
            '<!-- the rewriting module -->\n' +
            '<dependency>\n' +
            '  <groupId>de.tudresden.inf.st.bigraphs</groupId>\n' +
            '  <artifactId>bigraph-simulation</artifactId>\n' +
            '  <version>${version}</version>\n' +
            '</dependency>\n' +
            '<!-- the visualization module -->\n' +
            '<dependency>\n' +
            '  <groupId>de.tudresden.inf.st.bigraphs</groupId>\n' +
            '  <artifactId>bigraph-visualization</artifactId>\n' +
            '  <version>${version}</version>\n' +
            '</dependency>\n' +
            '<!-- the converter module -->\n' +
            '<dependency>\n' +
            '  <groupId>de.tudresden.inf.st.bigraphs</groupId>\n' +
            '  <artifactId>bigraph-converter</artifactId>\n' +
            '  <version>${version}</version>\n' +
            '</dependency>';
        const codeString2 = 'compile "de.tudresden.inf.st.bigraphs:bigraph-core:${version}"\n' +
            'compile "de.tudresden.inf.st.bigraphs:bigraph-simulation:${version}"\n' +
            'compile "de.tudresden.inf.st.bigraphs:bigraph-visualization:${version}"\n' +
            'compile "de.tudresden.inf.st.bigraphs:bigraph-converter:${version}"';
        const imgSrc = this.props.img;
        return (
            <div className="gridBlock">
                <div className="blockElement myCustomClass imageAlignSide imageAlignLeft threeByGridBlock">
                    <div className="blockImage alignCenter">
                        <img src={imgSrc}/>
                        <p>
                        Dependencies
                        </p>
                    </div>
                    <div className="blockContent" >
                        <h2>
                            <div><span><p>Maven</p></span></div>
                        </h2>
                        <SyntaxHighlighter language="xml">
                            {codeString}
                        </SyntaxHighlighter>
                        <div>
                        </div>
                    </div>
                    {/*<div className="blockElement myCustomClass alignCenter threeByGridBlock">*/}
                    <div className="blockContent">
                        <h2>
                            <div><span><p>Gradle</p></span></div>
                        </h2>
                        <SyntaxHighlighter language="groovy">
                            {codeString2}
                        </SyntaxHighlighter>
                        <div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

BigraphDependencyBlock.defaultProps = {
    img: '',
};

module.exports = BigraphDependencyBlock;