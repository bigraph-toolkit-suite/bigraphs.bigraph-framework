package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import net.jodah.typetools.TypeResolver;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import javax.annotation.Nullable;
import java.util.Objects;
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

    static Optional<FactoryCreationContext> current() {
        Stack<FactoryCreationContext> cs = CONTEXT.get();
        return cs.empty() ? Optional.empty() : Optional.of(cs.peek());
    }

    static FactoryCreationContext get() {
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

    static FactoryCreationContext begin(@Nullable AbstractBigraphFactory bigraphFactory) {
        FactoryCreationContext ctx = new FactoryCreationContext(bigraphFactory);
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
        assert Objects.nonNull(bigraphClass);
        // factory for common pure bigraphs
        if (bigraphClass.isAssignableFrom(PureBigraph.class)) {
            return new PureBigraphFactory();
        }
        // factory for elementary bigraphs
        if (bigraphClass.isAssignableFrom(ElementaryBigraph.class) ||
                (Objects.nonNull(bigraphClass.getSuperclass()) && isAssignable(bigraphClass))) {
            Class<?> typeArg = TypeResolver.resolveRawArgument(ElementaryBigraph.class, bigraphClass);
            if (typeArg.isAssignableFrom(DefaultDynamicSignature.class)) {
                return new PureBigraphFactory();
            }
        }
        throw new RuntimeException("Not implemented yet");
    }

    private static boolean isAssignable(Class<? extends Bigraph> clazz) {
        if (Objects.isNull(clazz)) return false;
        if (clazz.isAssignableFrom(ElementaryBigraph.class)) return true;
        if (Objects.nonNull(clazz.getSuperclass()))
            return isAssignable((Class<? extends Bigraph>) clazz.getSuperclass());
        return false;
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
