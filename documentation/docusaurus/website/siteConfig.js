/**
 * Copyright (c) 2017-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

// See https://docusaurus.io/docs/site-config for all the possible
// site configuration options.

const {Plugin: Embed} = require('remarkable-embed');
const importMd = require('./static/js/import-md');
// Our custom remarkable plugin factory.
const createVariableInjectionPlugin = variables => {
    // `let` binding used to initialize the `Embed` plugin only once for efficiency.
    // See `if` statement below.
    let initializedPlugin;

    const embed = new Embed();
    embed.register({
        // Call the render method to process the corresponding variable with
        // the passed Remarkable instance.
        // -> the Markdown markup in the variable will be converted to HTML.
        inject: key => initializedPlugin.render(variables[key])
    });

    return (md, options) => {
        if (!initializedPlugin) {
            initializedPlugin = {
                render: md.render.bind(md),
                hook: embed.hook(md, options)
            };
        }

        return initializedPlugin.hook;
    };
};

// List of projects/orgs using your project for the users page.
const users = [
    {
        caption: 'User1',
        // You will need to prepend the image path with your baseUrl
        // if it is not '/', like: '/test-site/img/image.jpg'.
        image: 'img/undraw_open_source.svg',
        infoLink: 'https://www.facebook.com',
        pinned: true,
    },
    {
        caption: 'User1',
        // You will need to prepend the image path with your baseUrl
        // if it is not '/', like: '/test-site/img/image.jpg'.
        image: 'img/undraw_open_source.svg',
        infoLink: 'https://www.facebook.com',
        pinned: true,
    },
];

var urlConfigs = {
    url: 'https://www.bigraphs.org',
    baseUrl: '/products/bigraph-framework/',
};

const siteVariables = {
    revision: 'Version: 0.9.0',
    revisionVar: 'Replace ${revision} with 0.9.0',
    supportedConversions: '<ul><li>BigMC</li><li>BigraphER</li><li>BigRed</li></ul>'
};

const siteConfig = {
    markdownPlugins: [
        createVariableInjectionPlugin(siteVariables),
        importMd(),
    ],
    defaultVersionShown: '0.9.0-SNAPSHOT',
    noIndex: false,
    title: 'Bigraph Framework', // Title for your website.
    tagline: 'A framework written in Java for the manipulation and simulation of bigraphical reactive systems',
    baseUrl: urlConfigs.baseUrl, // Base URL for your project */
    url: urlConfigs.url, // Your website URL, e.g., https://your-docusaurus-test-site.com
    // For github.io type URLs, you would set the url and baseUrl like:
    //   url: 'https://facebook.github.io',
    //   baseUrl: '/test-site/',
    repoUrl: "https://github.com/st-tu-dresden/bigraph-framework",
    artifactRepoUrl: "https://bintray.com/st-tu-dresden/maven-repository/",
    // Used for publishing and more: Project name. This must match your GitHub repository project name (case-sensitive).
    projectName: 'bigraph-framework',
    // GitHub username of the organization or user hosting this project.
    // This is used by the publishing script to determine where your GitHub pages website will be hosted.
    organizationName: 'st-tu-dresden',
    // organizationName: 'stg-tud',
    // For top-level user or org sites, the organization is still the same.
    // e.g., for the https://JoelMarcey.github.io site, it would be set like...
    //   organizationName: 'JoelMarcey'
    apiDocsUrl: 'apidocs/index.html',
    disableHeaderTitle: true,
    // For no header links in the top nav bar -> headerLinks: [],
    headerLinks: [
        {doc: 'index', label: 'Docs'},
        // {doc: 'doc4', label: 'API'},
        {href: urlConfigs.url + urlConfigs.baseUrl + 'apidocs/index.html', label: 'API', external: true},
        // {page: 'help', label: 'Help'},
        {search: true},
        // Links to href destination
        {href: "https://github.com/st-tu-dresden/bigraph-framework", label: "GitHub"},
        // {blog: true, label: 'Blog'},
    ],

    // If you have users set above, you add it here:
    users,

    /* path to images for header/footer */
    headerIcon: 'img/favicon.ico',
    footerIcon: 'img/favicon.ico',
    favicon: 'img/favicon.ico',

    /* Colors for website */
    colors: {
        primaryColor: '#009987',
        secondaryColor: '#ff1265',
    },

    /* Custom fonts for website */
    /*
    fonts: {
      myFont: [
        "Times New Roman",
        "Serif"
      ],
      myOtherFont: [
        "-apple-system",
        "system-ui"
      ]
    },
    */

    // This copyright info is used in /core/Footer.js and blog RSS/Atom feeds.
    copyright: `Copyright © ${new Date().getFullYear()} Dominik Grzelak`,

    highlight: {
        // Highlight.js theme to use for syntax highlighting in code blocks.
        theme: 'monokai-sublime',
        version: '9.12.0',
        // hljs: function(highlightJsInstance) {
        //     // do something here
        // },

        // Default language.
        // It will be used if one is not specified at the top of the code block. You can find the list of supported languages here:
        // https://github.com/isagalaev/highlight.js/tree/master/src/languages
        // defaultLang: 'javascript',

        // custom URL of CSS theme file that you want to use with Highlight.js. If this is provided, the `theme` and `version` fields will be ignored.
        // themeUrl: 'https://raw.githubusercontent.com/highlightjs/highlight.js/master/src/styles/darcula.css'
    },
    usePrism: ['jsx'],

    // Add custom scripts here that would be placed in <script> tags.
    scripts: [
        'https://buttons.github.io/buttons.js',
        'https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.0/clipboard.min.js',
        '/js/code-block-buttons.js',
    ],
    stylesheets: ['/css/code-block-buttons.css', '/css/custom.css'],
    separateCss: ['/static/apidocs/stylesheet.css'],
    // On page navigation for the current documentation page.
    onPageNav: 'separate',
    // No .html extensions for paths.
    cleanUrl: true,

    // Open Graph and Twitter card images.
    ogImage: 'img/undraw_online.svg',
    twitterImage: 'img/undraw_tweetstorm.svg',

    // For sites with a sizable amount of content, set collapsible to true.
    // Expand/collapse the links and subcategories under categories.
    // docsSideNavCollapsible: true,

    // Show documentation's last contributor's name.
    // enableUpdateBy: true,

    // Show documentation's last update time.
    // enableUpdateTime: true,

    // You may provide arbitrary config keys to be used as needed by your
    // template. For example, if you need your repo's URL...
    //   repoUrl: 'https://github.com/facebook/test-site',
};

module.exports = siteConfig;