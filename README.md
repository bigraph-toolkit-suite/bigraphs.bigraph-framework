# Bigraph Framework


## Project Setup

### Deploy the bigraph model jar

A bigraph is an Ecore model that is developed within EMF. 
The model needs to be deployed to the local maven repository.

Change to the root folder of the project and execute:
```
mvn install:install-file -Dfile=./libs/bigraphModel.jar -DgroupId=de.tudresden.inf.st.bigraphs.model \
    -DartifactId=bigraph-ecore-model -Dversion=1.0 -Dpackaging=jar
```
Do this only once or when the model changes.
This will install the bigraph model in your local Maven repository.

mvn install:install-file -Dfile=./libs/bigraphModel_v2.jar -DgroupId=de.tudresden.inf.st.bigraphs.model -DartifactId=bigraph-ecore-model -Dversion=2.0 -Dpackaging=jar

<!--
```
mvn deploy:deploy-file -DgroupId=de.tudresden.inf.st.bigraphs.model -DartifactId=bigraph-ecore-model -Dversion=1.0 -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=./libs/bigraphModel.jar
```
-->

The model can now be used as a Maven dependency in other modules of this project and will be exported also in the 
generated *.jar later.
