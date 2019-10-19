# Development

## Git workflow
- The Git workflow *Gitflow* as described [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) 
is applied for the development 

## Versioning

- [Semantic Versioning](https://semver.org/) is employed as versioning scheme for all modules 

- The version of the parent determines also the version of each sub module.
Version are increased simultaneously for all sub modules to indicate which dependencies
can be used together.

## Modules

Each module represents a concrete subset of the framework's whole functionality.
Thus, it makes it easy to include a certain dependency in external projects for
specific needs, at the same time logically organizing the whole project.

<!--_v3: current-->
<!--_v5: cdo migrated model-->
<!--_v6: with extra BBigraph container object-->

## Documentation

- We are employing [MkDocs](https://www.mkdocs.org), a static site generator, for building the documentation
- Must be installed on the machine:
    - MkDocs, see [installation instructions](https://www.mkdocs.org/#installation)
    - Theme: [Bootstrap](https://mkdocs.readthedocs.io/en/0.15.3/user-guide/styling-your-docs/#bootstrap-and-bootswatch-themes)
<!--        - the theme is provided with the project and resides within `etc/doc/theme/mkdocs_windmill`-->
    
- The corresponding documentation files are stored in `etc/doc/`

### Building the documention

#### Using Maven

- Available Maven goals from the root folder of the project:
```bash
$ ./mvnw clean install -Pdistribute
```
The generated documentation is available from `target/site/reference/html/index.html`.

#### Using Mkdocs directly

- for testing purposes only

You can also manually build the documentation using `mkdocs` directly:

```bash
# change into the 'etc/doc/' folder
$ cd ./etc/doc/
# start the build process: the html files are placed into the sub folder 'sites'
$ mkdocs build
# to publish them on a locally created web server (with auto-reload on changes)
$ mkdocs serve
```

# Deployment

## Build configuration

- Goals to execute ... to run the build
- Goals to execute ... to deploy artifacts to [Bintray](https://bintray.com/)

git finish release
CI:
CI Friendly Versions: https://maven.apache.org/maven-ci-friendly.html
    - file-based version change with Maven: https://blog.soebes.de/blog/2016/08/08/maven-how-to-create-a-release/ 

mvn clean package install
mvn versions:set -DremoveSnapshot
    mvn mod1 deploy
    mvn mod2 deploy
    mvn release:update-versions -DautoVersionSubmodules=true
    
    mvn --batch-mode release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=1.2.0-SNAPSHOT


    mvn new snapshot version

git merge .... / go to dev / ...