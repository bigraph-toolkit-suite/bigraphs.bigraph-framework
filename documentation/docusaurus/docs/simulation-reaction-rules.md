---
id: simulation-reaction-rules
title: Reaction Rules
---

This section explains how to create reaction rules.

Important generic interface: `de.tudresden.inf.st.bigraphs.simulation.ReactionRule<B extends Bigraph<? extends Signature<?>>>`.

## Example

```java
    public static ReactionRule<PureBigraph> createReactionRule() throws TypeNotExistsException, InvalidConnectionException, ControlIsAtomicException, InvalidReactionRuleException {
        Signature<DefaultDynamicControl> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.OuterName network2 = builder2.createOuterName("network");
        builder.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network)
                .down()
                .addChild("Job")
        ;
        builder2.createRoot()
                .addChild("Room")
                .down()
                .addChild("Computer").linkToOuter(network2)
                .down()
                .addChild("Job").addChild("Job")
        ;

        builder.makeGround();
        builder2.makeGround();
        PureBigraph redex = builder.createBigraph();
        PureBigraph reactum = builder2.createBigraph();
        ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum);
        return rr;
    }
```

## Bounded Reaction Rules

A reaction rule can be _bound_ to an existing reactive system structure.
Therefore, the method `de.tudresden.inf.st.bigraphs.simulation.reactivesystem.AbstractReactionRule#withReactiveSystem(ReactiveSystem<B>)` is available.
A new class `ReactiveSystemBoundReactionRule<B>` is returned which extends `AbstractReactionRule` and contains the previously create reaction rule as well as the reactive system.
This may help to organize created reaction rules and give them a strong semantic affiliation to a reactive system.

Note that this feature does not add a reaction rule to that reactive system automatically.
As a consequence, the `de.tudresden.inf.st.bigraphs.simulation.reactivesystem.AbstractSimpleReactiveSystem#addReactionRule(ReactionRule<B>)` method has still to be used.





