// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Bigraph Framework',
    tagline: 'A framework written in Java for the manipulation and simulation of bigraphical reactive systems',
    url: 'https://www.bigraphs.org',
    baseUrl: '/software/bigraph-framework/',
    onBrokenLinks: 'warn',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',
    organizationName: 'bigraph-toolkit-suite', // Usually your GitHub org/user name.
    projectName: 'bigraph-framework', // Usually your repo name.

    presets: [
        [
            '@docusaurus/preset-classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    // Please change this to your repo.
                    // editUrl: 'https://github.com/facebook/docusaurus/edit/main/website/',
                },
                blog: {
                    showReadingTime: true,
                    // Please change this to your repo.
                    // editUrl: 'https://github.com/facebook/docusaurus/edit/main/website/blog/',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    plugins: [
        [
            '@docusaurus/plugin-content-docs',
            {
                id: 'tutorials',                    // <-- new docs instance
                path: 'tutorials',                  // folder in repo
                routeBasePath: 'tutorials',         // URLs start with /tutorials
                sidebarPath: require.resolve('./sidebars.tutorials.js'),
                // editUrl: 'https://github.com/your-org/your-repo/edit/main/', // optional
            },
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            navbar: {
                title: 'Bigraph Framework',
                logo: {
                    alt: 'Bigraph Framework Logo',
                    src: 'img/bigraph-pure-logo.png',
                },
                items: [
                    {
                        type: 'doc',
                        docId: 'index',
                        position: 'left',
                        label: 'User Manual',
                    },
                    // themeConfig.navbar.items
                    {
                        type: 'doc',
                        docsPluginId: 'tutorials',
                        docId: 'index',      // first page in the tutorials set (we’ll create it next)
                        position: 'left',
                        label: 'Tutorials',
                    },
                    {
                        to: 'https://www.bigraphs.org/software/bigraph-framework/apidocs/',
                        label: 'JavaDoc API',
                        position: 'left'
                    },
                    {
                        href: 'https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-framework',
                        label: 'GitHub',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Docs',
                        items: [
                            {
                                label: 'User Manual',
                                to: '/docs/',
                            },
                            { label: 'Tutorials', to: '/tutorials/' },
                            {
                                label: 'JavaDoc API',
                                to: 'https://www.bigraphs.org/software/bigraph-framework/apidocs/',
                            },
                        ],
                    },
                    // {
                    //   title: 'Community',
                    //   items: [
                    //     {
                    //       label: 'Stack Overflow',
                    //       href: 'https://stackoverflow.com/questions/tagged/docusaurus',
                    //     },
                    //     {
                    //       label: 'Discord',
                    //       href: 'https://discordapp.com/invite/docusaurus',
                    //     },
                    //     {
                    //       label: 'Twitter',
                    //       href: 'https://twitter.com/docusaurus',
                    //     },
                    //   ],
                    // },
                    {
                        title: 'More',
                        items: [
                            // {
                            //   label: 'Blog',
                            //   to: '/blog',
                            // },
                            {
                                label: 'GitHub',
                                href: 'https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-framework',
                            },
                        ],
                    },
                ],
                // copyright: `Copyright © ${new Date().getFullYear()} My Project, Inc. Built with Docusaurus.`,
                copyright: `Copyright © 2021-${new Date().getFullYear()} Bigraph Toolkit Suite Developers <br/> Unless otherwise specified, the textual content and visual elements featured on this website are subject to the Creative Commons license CC BY-SA 4.0. <br/> Bigraph Framework is licensed under the&nbsp;<a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License, Version 2.0</a>.<script async src="https://scripts.simpleanalyticscdn.com/latest.js"></script>`,
            },
            prism: {
                // theme: lightCodeTheme,
                darkTheme: darkCodeTheme,
                // customCss: [
                    // require.resolve('./src/css/prism-shades-of-purple.css')
                    // darkCodeTheme
                // ]
            },
        }),
};

module.exports = config;
