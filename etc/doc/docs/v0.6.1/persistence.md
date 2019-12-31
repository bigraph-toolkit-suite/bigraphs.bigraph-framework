# Persisting Bigraphs

The framework provides simple means for storing and loading bigraphical
meta models and instance models.

Therefore, the utility class `BigraphArtifacts` comprises methods for
persisting bigraphical models to the filesystem.

For a more sophisticated persistence solution, we refer the reader to 
[Eclipse Connected Data Objects (CDO) Model Repository](https://projects.eclipse.org/projects/modeling.emf.cdo) 
and the corresponding implementation [spring-data-cdo]() for working
with the [Spring framework](https://spring.io/). 

## Output format

For the meta model xmi is used and for the instance model ecore.

The ecore model includes a direct references to the meta model for validation.



## Meta Model

To store the meta model (i.e., an abstract bigraph over a signature):

```java

```


## Instance Model

To store an instance model (i.e., a concrete bigraph over a signature):

```java

```



