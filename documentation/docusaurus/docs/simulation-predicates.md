---
id: simulation-predicates
title: Bigraphical Predicates
---

This section explains how to create predicates for model checking.

Important generic abstract class: `de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicates<B extends Bigraph<? extends Signature<?>>>`.

## Adding Predicates

Any sub-class of `AbstractSimpleReactiveSystem<B extends Bigraph<? extends Signature<?>>>` provides the following method for adding predicates:

```java
AbstractSimpleReactiveSystem#addPredicate(ReactiveSystemPredicates<B> predicate)
```

## Listening to Predicate Evaluation

If predicates are added to a reactive system instance, one can add additional logic when these predicates gets evaluated.

Therefore, one must add a listener the bigraph model checker instance by calling `BigraphModelChecker<B>#setReactiveSystemListener(BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener)`
or by passing the object as argument in one of its constructors.

The listener `BigraphModelChecker.ReactiveSystemListener<B>` contains several methods for getting the evaluation result of a predicate match.

### `onAllPredicateMatched(...)`
This method is called if all available predicates of a reactive system evaluated to true in one state.
In this case, the method `ReactiveSystemListener#onPredicateMatched(Bigraph, ReactiveSystemPredicates)` is not called.

It has the following arguments:
- `B currentAgent`: the agent

### `onPredicateMatched(...)`
This method is called if a predicate evaluated to {@code true} after a transition.
It is only called if not all predicates yielded `true`.

It has the following arguments:
- `B currentAgent`: the agent
- `ReactiveSystemPredicates<B> predicate`: the predicate

### `onPredicateViolated(...)`

Reports a violation of a predicate and supplies a counterexample trace from the initial state to the violating state.
It has the following arguments:

- `B currentAgent`: the agent
- `ReactiveSystemPredicates<B> predicate`: the predicate
- `GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> counterExampleTrace`: the trace representing a counterexample




## Types of predicates

### `AndPredicate<B extends Bigraph<? extends Signature<?>>>`
### `BigraphIsoPredicate<B extends Bigraph<? extends Signature<?>>>`
### `OrPredicate<B extends Bigraph<? extends Signature<?>>>`
### `SubBigraphMatchPredicate<B extends Bigraph<? extends Signature<?>>>`






