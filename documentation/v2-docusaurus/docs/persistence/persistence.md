---
id: persistence title: Persisting Bigraphs
---

<!--# Persisting Bigraphs-->

The framework provides simple means for storing and loading bigraphical meta models and instance models.

Therefore, the utility class `de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement` within the `bigraph-core`
module comprises methods for persisting bigraphical models to the filesystem.

For a more sophisticated persistence solution, we refer the reader to
[Eclipse Connected Data Objects (CDO) Model Repository](https://projects.eclipse.org/projects/modeling.emf.cdo)
and the corresponding implementation [spring-data-cdo](https://git-st.inf.tu-dresden.de/bigraphs/spring-data-cdo) for
working with the [Spring framework](https://spring.io/).

## Output format

For the meta-model the file extension `*.ecore` is used and for the instance model `*.xmi`.

The instance model includes a direct reference to the meta-model which can be used for validation. This information can
also be changed (or manually edited afterwards). This is described
in [Storing an instance model to the filesystem](#storing-an-instance-model-to-the-filesystem)

## Bigraphical Metamodel

To only store the meta-model of a concrete bigraph (i.e., an abstract bigraph over a signature, also called *meta model
over a signature*), we call the method `BigraphFileModelManagement#exportAsMetaModel()`.

### Storing a meta-model to the filesystem

For demonstration, we create a simple signature and pass some additional meta data to the bigraph builder. This gives us
the option to specify the namespace and the URI of the meta model.

```java
DefaultDynamicSignature signature=pureSignatureBuilder()
        .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(2)).assign()
        .newControl().identifier(StringTypedName.of("Laptop")).arity(FiniteOrdinal.ofInteger(1)).assign()
        .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
        .create();

        createOrGetBigraphMetaModel(signature,EMetaModelData.builder()
        .setName("myMetaModel")
        .setNsPrefix("example")
        .setNsUri("http://example.org")
        .create());
        PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder=pureBuilder(
        signature
        );
// do something with the bigraph builder ...
        PureBigraph bigraph=bigraphBuilder.createBigraph();

// Export the metamodel
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph,new FileOutputStream(new File("meta-model.ecore")));
```

As shown above, the metamodel data must be passed to a special method available
from `de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory`. The
method `exportAsMetaModel(EcoreBigraph, OutputStream)` is used then to output the Ecore representation to the
filesystem.

### Loading a metamodel from the filesystem

Note that various overloaded methods exist:

```java
BigraphFileModelManagement.Load.bigraphMetaModel(...);
        BigraphFileModelManagement.Load.signatureMetaModel(...);
```

### Load the bigraph meta-metamodel

Bigraph Framework also contains "bigraph meta-meta-model" which can be acquired at any time by calling:

```java
EPackage bigraphMetaModel=BigraphFileModelManagement.Load.internalBigraphMetaMetaModel();
```

This meta-model is used to dynamically create bigraphs over user-defined signatures, thus, representing the
meta-meta-model of every meta-model created by a builder instance.

### Meta information for the metamodel

An important details is the data structure:

```java
EMetaModelData.builder().setName("sample")
        .setNsPrefix("bigraph").setNsUri("org.example.bigraphs");
```

located in the package `de.tudresden.inf.st.bigraphs.core.datatypes` of the `bigraph-core` module. Its purpose should be
self-explanatory from the above example (see also below).

## Bigraphical Instance Model

### Storing an instance model to the filesystem

To store an instance model (i.e., a concrete bigraph over a signature):

```java
// create some bigraph via the builder
PureBigraph bigraph=...;
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,new FileOutputStream("instance-model.xmi"));
```

To change the namespace location of the corresponding meta-model, you can provide this information as follows:

```java
BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph,
        new FileOutputStream("instance.xmi"),"./path/to/meta-model.ecore");
```

Signatures are also supported:

```java
exportAsInstanceModel(EcoreSignature signature,OutputStream outputStream)
```

### Loading an instance model from the filesystem

Instance models can be loaded directly from the filesystem like this:

```java
List<EObject> eObjects2=BigraphFileModelManagement.Load.bigraphInstanceModel("instance.xmi");
```

In the example above, the validity of the instance model is only performed against the bigraph meta-meta-model. However,
when providing a meta-model, the instance model is validated against it:

```java
EPackage metaModel=BigraphFileModelManagement.Load.bigraphMetaModel("meta-model.ecore");
        List<EObject> eObjects=BigraphFileModelManagement.Load.bigraphInstanceModel(metaModel,"instance.xmi");
```


