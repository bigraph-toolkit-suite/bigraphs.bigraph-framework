import React from "react";
import {render} from "react-dom";
import Highlight, {defaultProps} from "prism-react-renderer";

export default function BigraphMarkdownBlock({exampleCode, language}) {
    return (
        <Highlight {...defaultProps} code={exampleCode} language={language}>
            {({className, style, tokens, getLineProps, getTokenProps}) => (
                <pre className={className} style={style}>
        {tokens.map((line, i) => (
            <div {...getLineProps({line, key: i})}>
                {line.map((token, key) => (
                    <span {...getTokenProps({token, key})} />
                ))}
            </div>
        ))}
      </pre>
            )}
        </Highlight>
    )
}