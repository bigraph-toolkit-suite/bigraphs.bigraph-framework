---
id: verification
title: Verification
---

<!--# Verification-->

*Verification* is a formal method in software engineering based on mathematical
models and techniques to prove the correctness of programs.

In this process, a *program is checked against* a set of
correctness properties, also called the *specification*.

By proving specific properties of some software,
this type of testing may assist one to develop safe and reliably software
which cannot be detected by traditional means such as unit tests.

One type of verification technique is the so-called **model checking** which
is implemented in Bigraph Framework.

For model checking, we the following dependencies are used:
- tweety, JDD, JavaDD

## Safety and Liveness Properties

As long as the transition system is being built from a BRS specification,
the provided predicates of the user are checked simultaneously for each
new computed state in the course of a BRS simulation.

Note: the user can also listen to those events.

These predicates come in two forms: as safety properties or as liveness
properties. The former represents a state in the system that should not
occur, whereas the later denote a system state that eventually occurs.
By this we can test if a program never reaches a "bad state" (safety),
or correctly terminates (reaching a desirable state), possibly producing
a result (liveness).

Note: Safety is easier to check then liveness
Some further safety properties are: partial correctness, absence of deadlocks,
and mutual exclusion (see [\[1\]](#ref1)).

Note: fairness properties are important for reactive systems.

### Example: Safety

Two processes mutually requesting a exclusive resource. Then, we want to
verify that both processes never are in their critical section at the same
time.


## Listeners

One may listen to specific events that are thrown during the simulation. This gives the user the possibility to interact
with the simulation and fire additional actions or to log these events and evaluate them later, additionally to the
final built reaction graph.



## Async execution

To perform the model checking asynchronously, call the `BigraphModelChecker#executeAsync()`:

```java
Future<ReactionGraph<PureBigraph>> reactionGraphFuture = modelChecker.executeAsync();
ReactionGraph<PureBigraph> pureBigraphReactionGraph = reactionGraphFuture.get();
```

The `executeAsync()` method does not block the execution and returns a `Future` object to fetch the result later.
It contains the complete reaction graph of the simulated BRS.

One can then export the transition system by calling  `modelChecker.exportReactionGraph(pureBigraphReactionGraph)`.

## Provide a custom executor service

The `java.util.concurrent.ExecutorService` is used to submit tasks of the model checker.

Bigraph framework offers to provide a custom `ExecutorService` by implementing the interface
`de.tudresden.inf.st.bigraphs.core.providers.ExecutorServicePoolProvider`, found in the `bigraph-core` dependency.
The class `BigraphModelChecker` uses the `java.util.ServiceLoader` (see [https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#the-serviceloader-class](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#the-serviceloader-class)
to search for an implementation.

A default _executor service provider_ is provided within the `bigraph-rewriting` dependency which creates a fixed thread pool.


## References

- \[1\] <a id="ref1" href="https://dl.acm.org/citation.cfm?doid=357172.357178">S. Owicki and L. Lamport, “Proving Liveness Properties of Concurrent Programs,” ACM Trans. Program. Lang. Syst., vol. 4, no. 3, pp. 455–495, Jul. 1982.</a>
