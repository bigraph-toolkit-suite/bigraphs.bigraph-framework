package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import net.jodah.typetools.TypeResolver;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.ecore.EPackage;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

/**
 * @author Dominik Grzelak
 * @see BigraphFactory
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
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature);
        });
    }

    static DiscreteIon createDiscreteIonBuilder(Signature signature, String name, Set<String> outerNames, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newDiscreteIonBuilder(signature, name, outerNames);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newDiscreteIonBuilder(signature, name, outerNames);
        });
    }

    static DiscreteIon createDiscreteIonBuilder(Signature signature, String name, Set<String> outerNames,
                                                EPackage bigraphMetaModel,
                                                Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newDiscreteIonBuilder(signature, name, outerNames, bigraphMetaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newDiscreteIonBuilder(signature, name, outerNames, bigraphMetaModel);
        });
    }

    static Placings createPlacingsBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newPlacingsBuilder(signature);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newPlacingsBuilder(signature);
        });
    }

    static Placings createPlacingsBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newPlacingsBuilder(signature, metaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newPlacingsBuilder(signature, metaModel);
        });
    }

    static Linkings createLinkingsBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newLinkingsBuilder(signature);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newLinkingsBuilder(signature);
        });
    }

    static Linkings createLinkingsBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newLinkingsBuilder(signature, metaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newLinkingsBuilder(signature, metaModel);
        });
    }

    static PureBigraphGenerator createRandomBigraphBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newRandomBigraphBuilder(signature);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newRandomBigraphBuilder(signature);
        });
    }

    static PureBigraphGenerator createRandomBigraphBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newRandomBigraphBuilder(signature, metaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newRandomBigraphBuilder(signature, metaModel);
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, String metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newBigraphBuilder(signature, metaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature, metaModel);
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            return ctx.newBigraphBuilder(signature, metaModel);
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature, metaModel); //TODO: factory
        });
    }

    private DiscreteIon newDiscreteIonBuilder(Signature<?> signature, String name, Set<String> outerNames) {
        return this.factory.createDiscreteIon(name, outerNames, signature);
    }

    private DiscreteIon newDiscreteIonBuilder(Signature<?> signature, String name, Set<String> outerNames, EPackage bigraphMetaModel) {
        return this.factory.createDiscreteIon(name, outerNames, signature, bigraphMetaModel);
    }

    private PureBigraphGenerator newRandomBigraphBuilder(Signature<?> signature) {
        return this.factory.createRandomBuilder((DefaultDynamicSignature) signature);
    }

    private PureBigraphGenerator newRandomBigraphBuilder(Signature<?> signature, EPackage metaModel) {
        return this.factory.createRandomBuilder((DefaultDynamicSignature) signature, metaModel);
    }

    private Placings newPlacingsBuilder(Signature<?> signature) {
        return this.factory.createPlacings(signature);
    }

    private Placings newPlacingsBuilder(Signature<?> signature, EPackage metaModel) {
        return this.factory.createPlacings(signature, metaModel);
    }

    private Linkings newLinkingsBuilder(Signature<?> signature) {
        return this.factory.createLinkings(signature);
    }

    private Linkings newLinkingsBuilder(Signature<?> signature, EPackage metaModel) {
        return this.factory.createLinkings(signature, metaModel);
    }

    private BigraphBuilder newBigraphBuilder(Signature<?> signature) {
        return this.factory.createBigraphBuilder(signature);
    }

    private BigraphBuilder newBigraphBuilder(Signature<?> signature, String metaModel) {
        return this.factory.createBigraphBuilder(signature, metaModel);
    }

    private BigraphBuilder newBigraphBuilder(Signature<?> signature, EPackage metaModel) {
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
