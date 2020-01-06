package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;


import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Stack;

/**
 * @author Dominik Grzelak
 */
public final class FactoryCreationContext {
    private static final ThreadLocal<Stack<FactoryCreationContext>> CONTEXT = ThreadLocal.withInitial(Stack::new);

    @Nullable
    private final AbstractBigraphFactory factory;

    private int bigraphBuilderCount = 0;
    private final MutableList<Bigraph> mutableList;
    private final MutableList<AbstractBigraphFactory> mutableFactoryList;

    private FactoryCreationContext(@Nullable AbstractBigraphFactory factory) {
        this.mutableList = Lists.mutable.empty();
        this.mutableFactoryList = Lists.mutable.empty();
        this.factory = factory;
    }

    @Nullable
    public AbstractBigraphFactory getFactory() {
        return factory;
    }

    public static Optional<FactoryCreationContext> current() {
        Stack<FactoryCreationContext> cs = CONTEXT.get();
        return cs.empty() ? Optional.empty() : Optional.of(cs.peek());
    }

    public static FactoryCreationContext get() {
        Stack<FactoryCreationContext> cs = CONTEXT.get();
        if (cs.empty()) {
            throw new IllegalStateException("Not in a FactoryCreationContext");
        } else {
            return cs.peek();
        }
    }

    static FactoryCreationContext begin() {
        return begin((AbstractBigraphFactory) null);
    }

    static FactoryCreationContext begin(@Nullable AbstractBigraphFactory graph) {
        FactoryCreationContext ctx = new FactoryCreationContext(graph);
        ((Stack) CONTEXT.get()).push(ctx);
        return ctx;
    }

    static void end() {
        Stack<FactoryCreationContext> cs = (Stack) CONTEXT.get();
        if (!cs.empty()) {
            FactoryCreationContext ctx = cs.pop();
            if (ctx.factory != null) {
            }
        }

    }

    private static AbstractBigraphFactory findFactoryFor(Class<? extends Bigraph> bigraphClass) {
        if (bigraphClass.isAssignableFrom(PureBigraph.class)) {
            return new PureBigraphFactory();
        }
        throw new RuntimeException("Not implemented yet");
    }


    static PureBigraphFactory createPureBigraphFactory() {
        return current().map(FactoryCreationContext::newPureBigraphFactory).orElseGet(PureBigraphFactory::new);
    }

    static <S extends Signature> Object createSignatureBuilder(Class<PureBigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newSignatureBuilder();
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newSignatureBuilder();
        });
    }

    static BigraphComposite createOperator(Bigraph bigraph) {
        return current().map((ctx) -> {
            return ctx.newBigraphOperator(bigraph);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraph.getClass())).newBigraphOperator(bigraph);
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newBigraphBuilder(signature);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature); //TODO: factory
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, String metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newBigraphBuilder(signature, metaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature, metaModel); //TODO: factory
        });
    }

    private BigraphBuilder newBigraphBuilder(Signature<?> signature) {
        return this.factory.createBigraphBuilder(signature);
    }

    private BigraphBuilder newBigraphBuilder(Signature<?> signature, String metaModel) {
        return this.factory.createBigraphBuilder(signature, metaModel);
    }

    private BigraphComposite newBigraphOperator(Bigraph<?> bigraph) {
        bigraphBuilderCount++;
        return this.factory.asBigraphOperator(bigraph);
    }

    private SignatureBuilder newSignatureBuilder() {
        return this.factory.createSignatureBuilder();
    }

    private PureBigraphFactory newPureBigraphFactory() {
        return (PureBigraphFactory) factory;
    }
}
