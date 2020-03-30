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




