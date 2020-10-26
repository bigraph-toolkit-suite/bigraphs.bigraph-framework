---
id: basics
title: Getting Started
---

Discover what Bigraph Framework is all about and learn the core concepts behind it.

## Creating a Bigraph

At the moment, Bigraph Framework supports only _pure bigraphs_.

### Signature
To create a bigraph, a so-called **signature** must be created first.
Technically, it defines the **syntax** of a bigraph and determines what **types** (i.e., _controls_ of a bigraphs) we can create in the following.

To create a signature, we begin by spawning a new _pure factory_, more specifically, a _signature builder_.
All further operations will use the same factory in the current _execution context_.
The first step is to create a signature by using the appropriate factory method.

{@import: ../docs/assets/basics/getting_started_guide-0.java}

As mentioned above, the signature specifies the syntax of a bigraph we are going to create in the following.
The resulting signature contains two controls: a _User_ with an arity of 1, and a _Computer_ with an arity of 2.
The arity specifies how many _connections_ a control can have.
The semantic meaning of the term "connection" is left open here - it can represent anything, for example, an ethernet link or an association relationship between elements.

> More about the specific methods of the bigraph factory, bigraph builders and signature builder can be found in [Factories and Builders](./advanced/factories-and-builders).

### Bigraph Builder

Now we are able to instantiate a pure bigraph builder instance.
It allows us to build our bigraph by adding child nodes and connections among them.
The signature above determines which kind of nodes we can add to the bigraph (our syntax).
Therefore, we have to supply the previously created signature to the `pureBuilder()` method.

{@import: ../docs/assets/basics/getting_started_guide-1.java}

The example shows how to add two nodes ("User" and "Computer") under the same root (we do it twice) and how to link all to the same _outer name_ with the label "login". The bigraph is illustrated below.

Note that the method `addChild` will throw an `InvalidConnectionException`
if the node cannot be connected to the outer name (because of its arity specified by the
signature).

![basic-example-bigraph](assets/basics/basic-bigraph.png)

> See [Visualization](visualization) on how to export a bigraph
> as graphic file.


## Elementary Bigraphs

Let us now examine how more trivial bigraphs can be created which are
termed _elementary bigraphs_. Essentially, there are two categories:
Placings and Linkings. They do not contain any node, thus, they are node-free
bigraphs.

Elementary bigraphs allow to build more complex bigraphs easily.
We show how to create a concrete placing and linking, before we use them
for composition later in the next section.

> Even if elementary bigraphs usually do not take a signature, it must be provided.
> This is due to technical reasons with respect to the bigraphical metamodel that is used internally.
> See also [Factories and Builders](./advanced/factories-and-builders) for more details.

<!--Note on equality: Object equality at the instance level is not to be compared-->
<!--with mathematical equality of bigraphs. This is also know as bigraph isomorphism problem.-->

The next code example shows how we can create a so-called _merge_n_ (i.e., a placing where
n sites are located under one root) and an identity link graph (i.e., inner and outer
interfaces of the link graph are connected).

<!-- ```java -->
<!-- @Test -->
<!-- void example() throws InvalidConnectionException { -->
<!--     // ... -->

<!--     Placings<DefaultDynamicSignature> placings = pureFactory.createPlacings(signature); -->
<!--     Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2); -->
<!--     Linkings<DefaultDynamicSignature> linkings = pureFactory.createLinkings(signature); -->
<!--     Linkings<DefaultDynamicSignature>.Identity login = linkings.identity(StringTypedName.of("login")); -->
<!-- } -->
<!-- ``` -->

{@import: ../docs/assets/basics/getting_started_guide-2.java}

The factory provides the method `createPlacings()` to create a placing builder.
With it we create a merge and passing the integer 2 as argument to create a merge
with two sites.
Further, we create an identity link graph with the name "login".

The two elementary bigraphs merge and identity are depicted below.

|Placing: Merge | Linking: Identity |
|---|---|
| ![basic-merge-bigraph](assets/basics/basic-merge-bigraph.png) | ![basic-identity-bigraph](assets/basics/basic-identity-bigraph.png)  |


## Composition of Bigraphs

Bigraphs may be composed by using special operators. This allows us to
modularize bigraphs by constructing sub-structures separately from each other
and compose them later to build a larger bigraph.

To equip a bigraph with operators we simply pass it to the static method
`ops()` provided by the `BigraphFactory` class.

<!-- ```java -->
<!-- @Test -->
<!-- void example() throws InvalidConnectionException { -->
<!--     // ... -->

<!--     BigraphComposite<DefaultDynamicSignature> composed = ops(merge).parallelProduct(login) -->
<!--             .compose(bigraph); -->
<!-- } -->
<!-- ``` -->

{@import: ../docs/assets/basics/getting_started_guide-3.java}

The code above describes a composition that merges the two roots of `bigraph`
under one root node with index 0. At the same time we keep the links so that
all nodes are kept connected.

Note that composition may throw the following two exceptions `IncompatibleSignatureException` and `IncompatibleInterfaceException`
if the signatures are not the same or the interfaces are not compatible.
See [Theory](./advanced/theory) to get more details about how bigraphical
composition works. All available operators are explained in [Composition](composition).

![composed-bigraph](assets/basics/composed.png)

## Conclusion

This page explored some of the basic features of the Bigraph Framework
on how to create signatures, bigraphs, elementary bigraphs and how to compose them to
form larger ones.