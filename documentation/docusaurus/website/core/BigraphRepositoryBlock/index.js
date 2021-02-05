const React = require('react');

const SH = require('react-syntax-highlighter');
var SyntaxHighlighter = SH.PrismLight

class BigraphRepositoryBlock extends React.Component {
    render() {
        const codeString = '<repository>\n' +
            '   <snapshots>\n' +
            '       <enabled>true</enabled>\n' +
            '   </snapshots>\n' +
            '   <id>STFactory</id>\n' +
            '   <name>st-tu-dresden-artifactory</name>\n' +
            '   <url>https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/</url>\n' +
            '</repository>';
        const codeString2 =
            'repositories {\n' +
            '    maven {\n' +
            '        url "https://stgroup.jfrog.io/artifactory/st-tu-dresden-maven-repository/"\n' +
            '    }\n' +
            '}\n' +
            '\n';
        const imgSrc = this.props.img;
        return (
            <div className="gridBlock">
                <div className="blockElement myCustomClass imageAlignSide imageAlignLeft threeByGridBlock">
                    <div className="blockImage alignCenter">
                        <br/>
                        <img src={imgSrc}/>
                        <p>
                            Remote Repositories
                        </p>
                    </div>
                    <div className="blockContent">
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

BigraphRepositoryBlock.defaultProps = {
    img: '',
};

module.exports = BigraphRepositoryBlock;