---
id: persistence-2
title: Usage with Bigraph Builder
---

## Creating instances by loading persisted meta-models

Provide a signature and the filename of the meta-model to instantiate a builder class:

```java

```

Then, the builder may produce a bigraph by using its available methods as normal.

It is still necessary to provide the signature information as separate object. The reason is that not all needed information
is recorded in the meta-model of the bigraph concerning the signature. Some properties that are missing include the arity
of the control, or whether it is active, passiv or atomic; to mention a few.

### `PureBigraphBuilder#create(Signature, EMetaModelData)`

### `PureBigraphBuilder#create(Signature, String)`

## Creating instances by loading persisted bigraph instances

Though, bigraphs are per-se immutable data structures in Bigraph Framework, one can use the builder to instantiate bigraphs.
Persisted bigraphs can be loaded and supplied to a bigraph builder.

To prepare a bigraph builder:

```java
Signature<DefaultDynamicControl> signature = ...;
PureBigraphBuilder builder = PureBigraphBuilder.create(signature, "meta-model.xmi", "instance-model.ecore");
```

Then, the builder can be used as normal. One can, however, do not alter the pre-defined structure but adding new
elements is still possible.

### `PureBigraphBuilder#create(Signature, String, String)`

Provide a signature, and the filename of the meta-model and instance model to create a builder instance.

### `PureBigraphBuilder#create(Signature, EPackage, EObject)`

Provide a signature, and the loaded meta-model and instance model to create a builder instance.
