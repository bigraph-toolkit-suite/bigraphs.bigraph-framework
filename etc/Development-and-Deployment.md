# Development Guide

This section gives some guidelines about the active development, documentation and deployment process and can be regarded as a soft version of a more comprehensive "Code of Conduct".

## Coding-Guidelines

### Git-Workflow
- The Git workflow *Gitflow* as described [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) is applied for the development 

- Push tags only: `git push gitlab --tags`

### Versioning

- [Semantic Versioning](https://semver.org/) is employed as versioning scheme for all modules.

- The version of the parent pom determines also the version of each sub module.
  Version are increased simultaneously for all sub modules to indicate which dependencies
  can be used together.
    - Version must be set manually for every module and the parent

### Changelogs

Changelogs belong to the [Documentation](#Documentation) and are generated via the `changelog.sh` bash script. 
See also [Deployment](#Deployment) on how to properly tag the commit messages.

### Check Dependencies

`mvn dependency-check:check`

Identifies dependencies with vulnerabilities (having a CVSS score >= 8).

### Module Separation

Each module represents a concrete subset of the framework's whole functionality.
Thus, it makes it easy to include a certain dependency in external projects for
specific needs, at the same time the project is logically organized.

## Documentation

This section explains how to build and view the user manual and Javadoc API.

- The documentation includes Javadoc and a separate user manual (static website).
And also the Changelog is part of it, see [Deployment](#Deployment).
- [Docusaurus](https://docusaurus.io/) is used as a static site generator. 
- The CI pipeline is responsible to copy the Javadoc API into the Docusaurus manual.
The generated user manual can then link to the separately built Javadoc.

**Requirements for the Documentation**

- Node: use latest version, see: https://www.digitalocean.com/community/tutorials/how-to-install-node-js-on-ubuntu-18-04-de
- NPM: use latest version: `sudo npm install -g npm@latest`


###  Building and Working with the Documentation

To check for npm updates or security issues, run the following inside the docusaurus folder of the documentation module:

```
npm audit
npx npm-check-updates
```

#### Live Editing

First, `cd` into the `./documentation/v2-docusaurus/` folder.
Then, to view and edit the manual execute the following commands:
```console
$ cd ./documentation/v2-docusaurus/
$ npx docusaurus start #or: npm run start
```

To actually build the static site:
```console
$ npm run build
```
The output is exported at `documentation/v2-docusaurus/build/`.

#### Auto-generated Code Samples

- Some of the code samples are automatically derived from the test cases and merged into the documentation
- The module `documentation` is in charge for that
- Execute the following Maven goal (from the root of this project) to create the code samples: `mvn exec:java -f documentation/pom.xml`

#### Build the whole documentation

```console
$ mvn clean install
$ mvn package -Pdistribute                              # creation and aggregation of JavaDocs 
$ nvm use 16                                            # switch node version
$ npm --prefix ./documentation/v2-docusaurus/ install   # install npm dependencies first
$ mvn -f documentation/pom.xml install -Pdistribute     # code sample generation and building the static site
```

The generated user manual is available from `documentation/v2-docusaurus/build/` (use `npm run serve`).
The generated Java documentation of all modules is available from `target/site/apidocs/`.
This aggregated API will be copied to `documentation/v2-docusaurus/static/apidocs`.

## Deployment

This section discusses the deployment process.

- The basic workflow looks like this.
  * Tests are executed on all branches.
  * The deployment process is executed after every merge into master or a when a version tag is created.
  * Documentation including the javadocs API are generated and pushed to an external GitHub repository into the *gh-pages* branch

### Build configuration

- Goals to execute `mvn clean install` to run the build
- Goals to execute `mvn clean deploy` to deploy Artifactory

### Documentation (User Manual + Javadoc API)

- The whole documentation is pushed to GitHub to be displayed by GitHub Pages.

- The following script builds and pushes the documentation to the remote repository:
```console
$ mvn ... # build the whole documentation as described before
$ cd ./documentation/v2-docusaurus/build/ && git init
$ touch CNAME && echo "www.bigraphs.org" > CNAME
$ git add . && git commit -m "updated documentation"
$ git remote add stgithub git@github.com:st-tu-dresden/bigraph-framework.git
$ git push --force stgithub master:gh-pages
```

- In GitLab, the variables SSH_PRIVATE_KEY and SSH_KNOWN_HOSTS must exist under **Settings | CI/CD**.
    - see [Using SSH keys with GitLab CI/CD](https://docs.gitlab.com/ee/ci/ssh_keys/)
    and [Generating a new SSH key pair](https://docs.gitlab.com/ee/ssh/#generating-a-new-ssh-key-pair)
    - Command to execute for SSH_KNOWN_HOSTS:
    ```console
    ssh-keygen -t rsa -b 4096 -C "dominik.grzelak@tu-dresden.de"
    ssh-keyscan github.com
    ```
- Follow the usual setup for creating and authorizing GitHub SSH keys

### Changelog Generation

- The changelog of a new release is generated by the script `etc/ci/changelog.sh` and can be edited
manually afterwards
- It contains multiple sections: ADDED, CHANGED, REMOVED, BUGFIX
- Git commit messages must be properly described using the section names
above as tags
    - They will be extracted using the `git log` command
  
- Usage of the script:
    - First parameter: path to the `maven.config` file (usually `.mvn/maven.config`)
    - Second parameter: path of the output file
