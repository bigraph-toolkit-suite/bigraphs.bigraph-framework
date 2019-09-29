# Development

for dev: git-flow

Each module represents a concrete subset of the framework's whole functionality.
Thus, it makes it easy to include a certain dependency in external projects for
specific needs, at the same time decreasing the overall project size.

The version of the parent determines also the version of each sub modules.
Version are increased simultaneously for all sub modules to indicate which dependencies
can be used together. 

<!--## Project Setup (old approach)-->

<!--### Deploy the bigraph model jar-->

<!--A bigraph is an Ecore model that is developed within EMF. -->
<!--The model needs to be deployed to the local maven repository.-->

<!--Change to the root folder of this project and execute:-->

<!--```-->
<!--mvn install:install-file -Dfile=./libs/bigraphModel.jar -DgroupId=de.tudresden.inf.st.bigraphs.model \-->
<!--    -DartifactId=bigraph-ecore-model -Dversion=1.0 -Dpackaging=jar-->
<!--```-->

<!--This will install the bigraph model in your local Maven repository.-->
<!--Do this only once or when the model changes. -->

<!--The model can now be used as a Maven dependency in other modules of this project and will be exported also in the -->
<!--generated *.jar later.-->

<!--_v3: current-->
<!--_v5: cdo migrated model-->
<!--_v6: with extra BBigraph container object-->

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