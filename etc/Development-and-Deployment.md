# Development Guide

This section gives some guidelines about the active development, documentation and deployment process and can be regarded as a soft version of a more comprehensive "Code of Conduct".

## Coding-Guidelines

### Git-Workflow
- The Git workflow *Gitflow* as described [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) is applied for the development 

- Push tags only: `git push origin --tags`

### Versioning

- [Semantic Versioning](https://semver.org/) is employed as versioning scheme for all modules.

- The version of the parent pom determines also the version of each sub module.
Version are increased simultaneously for all sub modules to indicate which dependencies
can be used together.
    - This version can be specified in `.mvn/maven.config`.

### Changelogs

Changelogs belong to the [Documentation](#Documentation) and are generated via the `changelog.sh` bash script. See also [Deployment](#Deployment) on how to properly tag the commit messages.

### Module Separation

Each module represents a concrete subset of the framework's whole functionality.
Thus, it makes it easy to include a certain dependency in external projects for
specific needs, at the same time the project is logically organized.

<!--_v3: current-->
<!--_v5: cdo migrated model-->
<!--_v6: with extra BBigraph container object-->

## Documentation

This section explains how to build and view the user manual and javadocs API.

- The documentation includes javadoc and a separate user manual (static website).
And also the changelog is part of it, see [Deployment](#Deployment).

- Docusaurus is used as a static site generator when building the user manual
It references also to the javadoc.
- The CI pipeline is responsible to copy the javadoc API into the docusaurus manual.
<!--- [MkDocs](https://www.mkdocs.org) is used as a static site generator, for building the user manual-->
<!--    - Must be installed on the machine:-->
<!--        - MkDocs, see [installation instructions](https://www.mkdocs.org/#installation)-->
<!--        - Theme: [Bootstrap](https://mkdocs.readthedocs.io/en/0.15.3/user-guide/styling-your-docs/#bootstrap-and-bootswatch-themes)-->
<!--<!--        - the theme is provided with the project and resides within `etc/doc/theme/mkdocs_windmill`-->-->
<!--    -->
<!--- The corresponding content of the documentation files are stored in `etc/doc/`-->
<!--- The layout is generated automatically-->

### Building the Documention

#### Using Maven

- Available Maven goals from the root folder of the project:
```bash
$ ./mvnw clean install -Pdistribute
```
The generated user manual is available from `etc/doc/docusaurus/website/`.
The generated Java documentation is available from `target/site/apidocs/`.
It will be copied to `etc/doc/docusaurus/website/static/apidocs` by Maven as well.

#### Using Docusaurus

First, `cd` into the `etc/doc/docusaurus/website/` folder.
Then, to view and edit the manual execute the following commands:
```bash
cd ./etc/doc/docusaurus/website
npm start
```
To actually build the static site:
```bash
npm run build
```
The output is exported at `etc/doc/docusaurus/website/build/bigraph-framework/index.html`.

<!--### Using Mkdocs directly-->

<!--- for testing purposes only-->

<!--You can also manually build the documentation using `mkdocs` directly:-->

<!--```bash-->
<!--# change into the 'etc/doc/' folder-->
<!--$ cd ./etc/doc/-->
<!--# start the build process: the html files are placed into the sub folder 'sites'-->
<!--$ mkdocs build-->
<!--# to publish them on a locally created web server (with auto-reload on changes)-->
<!--$ mkdocs serve-->
<!--```-->

## Deployment

This sections discusses the deployment process.

- The basic workflow looks like this.
  - Automated tests are executed on all branches.
  - The deployment process is executed after every merge into master or a 
    version tag is created.
  - Documentation including the javadocs API are generated and pushed to an
    external GitHub repository into the *gh-pages* branch

### Build configuration

- Goals to execute ... to run the build
- Goals to execute ... to deploy artifacts to [Bintray](https://bintray.com/)

#### Documentation (User Manual + Javadoc API)

- Docs are pushed to GitHub to be displayed by GitHub Pages.
- The project contains a second remote repository pointing to [GitHub/st-tu-dresden](https://github.com/st-tu-dresden/)
    - Command to add a remote
    ```bash
    # HTTPS
    git remote add stgithub https://github.com/st-tu-dresden/bigraph-framework.git
    # SSH (preferred)
    git remote add stgithub git@github.com:st-tu-dresden/bigraph-framework.git
    ```
- We use `subtree push` to transfer the user manual to the *gh-pages* branch on GitHub.
- As mentioned above, the full documentation is located at `etc/doc/docusaurus/website/build/bigraph-framework/`.
- Command to execute:
    ```bash
    git subtree push --prefix <PATH-TO-MANUAL> <SECOND-REMOTE> gh-pages
    git subtree push --prefix etc/doc/docusaurus/website/build stgithub gh-pages
    ```

- In GitLab, the variables SSH_PRIVATE_KEY and SSH_KNOWN_HOSTS must exist under Settings>CI/CD.
    - see [Using SSH keys with GitLab CI/CD](https://docs.gitlab.com/ee/ci/ssh_keys/)
    and [Generating a new SSH key pair](https://docs.gitlab.com/ee/ssh/#generating-a-new-ssh-key-pair)
    - Command to execute for SSH_KNOWN_HOSTS:
    ```bash
    ssh-keygen -t rsa -b 4096 -C "dominik.grzelak@tu-dresden.de"
    ssh-keyscan github.com
    ```

#### Changelog Generation

- The changelog of a new release is generated by the script `etc/ci/changelog.sh` and can be edited
manually afterwards
- It contains multiple sections: ADDED, CHANGED, REMOVED, BUGFIX
- Git commit messages must be properly described using the section names
above as tags
    - They will be extracted using the `git log` command
  
- Usage of the script:
    - First parameter: path to the `maven.config` file (usually `.mvn/maven.config`)
    - Second parameter: path of the output file