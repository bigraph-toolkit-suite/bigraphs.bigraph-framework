# Development Guidelines

This section gives some guidelines about the development, documentation and deployment process.

## Technical Standards and Best Practices

### Module Separation

Each module represents a concrete subset of the framework's whole functionality.
Maven modules make it easy to include a certain functionality as dependency in external projects for
specific needs.
At the same time, the project is logically organized.

### Git-Workflow

- The Git workflow *Gitflow* as described [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) is applied for the development
- Push tags only: `git push <remote> --tags`

### Versioning

- [Semantic Versioning](https://semver.org/) is employed as a versioning scheme for all modules.

- The version of the parent pom determines also the version of each submodule.
  Version are increased simultaneously for all submodules to indicate which dependencies
  can be used together.
- Version must be set manually for every module and the parent

### Check Dependencies

`mvn dependency-check:check`

Use this to identify dependency conflicts or to identify dependencies with vulnerabilities (having a CVSS score >= 8).

## Documentation

This section explains how to view, edit and build the user manual and the Javadoc API.

- The documentation includes the Javadoc and a separate user manual (i.e., a static website).
- [Docusaurus](https://docusaurus.io/) is used as a static site generator. 
- The Maven plugins and bash scripts are responsible to copy the Javadoc API into the Docusaurus manual and can be employed in a CI pipeline.
The generated user manual can then link to the separately built Javadoc.

### Requirements

- Install Node.js: 
  - `sudo apt install nodejs`
- NPM: 
  - `sudo apt install npm`
  - use latest version: `sudo npm install -g npm@latest`
- Install NVM
  - `curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.35.3/install.sh | bash`

###  Working with the Documentation

#### NPM Updates
To check for npm updates or security issues, run the following inside the docusaurus folder of the documentation module,
i.e., `documentation/v2-docusaurus/`:

```shell
npm audit
npx npm-check-updates
```

#### Live Editing

To view and edit the manual execute the following commands. 
First, `cd` into the `documentation/v2-docusaurus/` folder:

```shell
nvm install 16
nvm use 16
npm --prefix ./documentation/v2-docusaurus/ install
cd ./documentation/v2-docusaurus/
npx docusaurus start #or: npm run start
```

To actually build the static site content:
```shell
npm run build
```
The output is exported to `documentation/v2-docusaurus/build/`.

#### Auto-generated Code Samples

- Some of the code samples used in the documentation are automatically derived from the test cases and merged into the documentation
- The module `documentation` is in charge of that
- Execute the following Maven goal (from the root of this project) to just create the code samples: 
  - ```shell 
    mvn exec:java -f documentation/pom.xml
    ```

### Build the whole documentation

Execute from the root of this project the following commands:
```shell
mvn clean install -DskipTests
mvn package -P distribute -DskipTests                  # creation and aggregation of JavaDocs 
nvm install 16                                        # install node version 16 if not already installed
nvm use 16                                            # switch to this node version (needed for docusaurus)
npm --prefix ./documentation/v2-docusaurus/ install   # install npm dependencies first
mvn -f documentation/pom.xml install -Pdistribute     # code sample generation and building the static site
```

The generated user manual is available from `documentation/v2-docusaurus/build/`.
Use `npm run serve` inside the folder to start a webserver for reviewing the website.

The generated Java documentation of all modules is available from `target/site/apidocs/`.
This aggregated API (i.e., the merged result of all submodules) will be copied to `documentation/v2-docusaurus/static/apidocs`.

## Deployment

This section discusses the deployment process.

- The basic workflow looks like this:
  * Tests are executed on all branches.
  * The deployment process is executed after every merge into master or when a version tag is created.
  * Documentation, including the Javadocs API, is generated and pushed to an external GitHub repository into the *gh-pages* branch
    * Currently, it is the following repository: https://github.com/st-tu-dresden/bigraphs.org

### Framework

#### TLDR
- Local installation of the framework modules: `mvn clean install`
- Deployment of the framework to the Central Repository: 
  - `mvn clean deploy -P release,central`
  - `mvn clean deploy -P release,central -DskipTests`

Note: When `SNAPSHOT` prefix is present in the version name, a Snapshot Deployment is performed.
Otherwise, a Release Deployment is performed and the artifacts must be released manually after review (see [here](https://central.sonatype.org/publish/publish-portal-maven/)).

#### Authentication

The Sonatype account details (username + password) for the deployment must be provided to the
Maven Sonatype Plugin, which is specified in the project's `pom.xml` file (parent pom).

#### Requirements

The Maven GPG plugin is used to sign the components for the deployment.
It relies on the gpg command being installed:
```shell
sudo apt install gnupg2
```

and the GPG credentials being available e.g. from `settings.xml` (see [here](https://central.sonatype.org/publish/publish-portal-maven/)).
In `settings.xml` should be a `<profile/>` and `<server/>` configuration both with the `<id>central</id>`.

More information can be found [here](https://central.sonatype.org/publish/requirements/gpg/).

Listing keys: `gpg --list-keys --keyid-format short`

The `pom.xml` must also conform to the minimal requirements containing all relevant tags as required by Sonatype.

#### General Steps

- Generate GPG key pair (remember passphrase)
- Distribute Public Key to key server (e.g., keys.openpgp.org)
  - More information can be found [here](https://central.sonatype.org/publish/requirements/gpg/).
- Follow https://central.sonatype.org/publish/publish-portal-snapshots/ for deployment instructions

### Documentation (User Manual + Javadoc API)

- The whole documentation is pushed to GitHub to be served via GitHub Pages.

- The following script builds and pushes the documentation to the remote repository:
```shell
mvn ... # build the whole documentation as described before
cd ./documentation/v2-docusaurus/build/ && git init
touch CNAME && echo "www.bigraphs.org" > CNAME
git add . && git commit -m "updated documentation"
git remote add gh git@github.com:bigraph-toolkit-suite/bigraphs.bigraph-framework.git
git push --force gh main:gh-pages
```

- The site contents are stored at
https://git-st.inf.tu-dresden.de/bigraphs/website/bigraph-website
together with bash scripts for generating the documentation and automatic
deployment of the website to GitHub
