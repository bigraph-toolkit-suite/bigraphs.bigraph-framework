---
id: persistence
title: Persisting Bigraphs
---

<!--# Persisting Bigraphs-->

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


## Meta-model data

An important details is

EMetaModelData.builder().setName("sample")
                .setNsPrefix("bigraph").setNsUri("org.example.bigraphs");



## Bigraphical Meta Model

To only store the meta-model of a concrete bigraph (i.e., an abstract bigraph over a signature,
also called *meta model over a signature*), we call the method `BigraphArtifacts#exportAsMetaModel()`.

For demonstration, we create a simple signature and pass some additional
meta data to the bigraph builder. This gives us the option to specify the namespace
and the URI of the meta model.

```java
DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();
signatureBuilder
    .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(2)).assign()
    .newControl().identifier(StringTypedName.of("Laptop")).arity(FiniteOrdinal.ofInteger(1)).assign()
    .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
Signature signature = signatureBuilder.create();
        
PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = factory.createBigraphBuilder(
    signature,
    EMetaModelData.builder()
        .setName("myMetaModel")
        .setNsPrefix("example")
        .setNsUri("http://example.org")
        .create()
);
PureBigraph bigraph = bigraphBuilder.createBigraph();

// Export the meta-model
BigraphArtifacts.exportAsMetaModel(bigraph,
    new FileOutputStream(new File("meta-model.ecore")));
```


## Bigraphical Instance Model

To store an instance model (i.e., a concrete bigraph over a signature):

```java

```



