package org.bigraphs.framework.core.factory;

import de.tudresden.inf.st.bigraphs.core.*;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.impl.pure.KindBigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import net.jodah.typetools.TypeResolver;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.ecore.EPackage;

import javax.annotation.Nonnull;
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

    private final AbstractBigraphFactory factory;

    private int bigraphBuilderCount = 0;
    private final MutableList<Bigraph> mutableList;
    private final MutableList<AbstractBigraphFactory> mutableFactoryList;

    private FactoryCreationContext(@Nullable AbstractBigraphFactory factory) {
        this.mutableList = Lists.mutable.empty();
        this.mutableFactoryList = Lists.mutable.empty();
        this.factory = factory;
    }

    @Nonnull
    public AbstractBigraphFactory getFactory() {
        return factory;
    }

    public static Optional<FactoryCreationContext> current() {
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
        if (bigraphFactory == null) {
            bigraphFactory = new PureBigraphFactory();
        }
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

    public static <T extends AbstractBigraphFactory<?>> T findFactoryFor(Class<? extends Bigraph> bigraphClass) {
        assert Objects.nonNull(bigraphClass);
        // factory for common pure bigraphs
        if (bigraphClass.isAssignableFrom(PureBigraph.class)) {
            return (T) new PureBigraphFactory();
        }
        if (bigraphClass.isAssignableFrom(KindBigraph.class)) {
            return (T) new KindBigraphFactory();
        }
        // factory for elementary bigraphs
        if (bigraphClass.isAssignableFrom(ElementaryBigraph.class) ||
                (Objects.nonNull(bigraphClass.getSuperclass()) && isAssignable(bigraphClass))) {
            Class<?> typeArg = TypeResolver.resolveRawArgument(ElementaryBigraph.class, bigraphClass);
            if (typeArg.isAssignableFrom(DefaultDynamicSignature.class)) {
                return (T) new PureBigraphFactory();
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

//    static PureBigraphFactory createPureBigraphFactory() {
//        return current().map(FactoryCreationContext::newPureBigraphFactory).orElseGet(PureBigraphFactory::new);
//    }

    static <S extends Signature> Object createSignatureBuilder(Class<? extends Bigraph> bigraphClass) {
        //TODO check also type and return null to create another context
        //TODO or always create new context
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newSignatureBuilder();
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newSignatureBuilder();
        });
    }

    //TODO: use Class<>
    static BigraphComposite createOperator(Bigraph bigraph) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraph.getClass()))
                return ctx.newBigraphOperator(bigraph);
            else return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraph.getClass())).newBigraphOperator(bigraph);
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newBigraphBuilder(signature);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature);
        });
    }

    static DiscreteIon createDiscreteIonBuilder(Signature signature, String name, Set<String> outerNames, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newDiscreteIonBuilder(signature, name, outerNames);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newDiscreteIonBuilder(signature, name, outerNames);
        });
    }

    static DiscreteIon createDiscreteIonBuilder(Signature signature, String name, Set<String> outerNames,
                                                EPackage bigraphMetaModel,
                                                Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newDiscreteIonBuilder(signature, name, outerNames, bigraphMetaModel);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newDiscreteIonBuilder(signature, name, outerNames, bigraphMetaModel);
        });
    }

    static Placings createPlacingsBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newPlacingsBuilder(signature);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newPlacingsBuilder(signature);
        });
    }

    static Placings createPlacingsBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newPlacingsBuilder(signature, metaModel);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newPlacingsBuilder(signature, metaModel);
        });
    }

    static Linkings createLinkingsBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newLinkingsBuilder(signature);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newLinkingsBuilder(signature);
        });
    }

    static Linkings createLinkingsBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newLinkingsBuilder(signature, metaModel);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newLinkingsBuilder(signature, metaModel);
        });
    }

    static PureBigraphGenerator createRandomBigraphBuilder(Signature signature, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newRandomBigraphBuilder(signature);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newRandomBigraphBuilder(signature);
        });
    }

    static PureBigraphGenerator createRandomBigraphBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newRandomBigraphBuilder(signature, metaModel);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newRandomBigraphBuilder(signature, metaModel);
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, String metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newBigraphBuilder(signature, metaModel);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature, metaModel);
        });
    }

    static BigraphBuilder createBigraphBuilder(Signature signature, EPackage metaModel, Class<? extends Bigraph> bigraphClass) {
        return current().map((ctx) -> {
            if (ctx.getFactory().getBigraphClassType().equals(bigraphClass))
                return ctx.newBigraphBuilder(signature, metaModel);
            return null;
        }).orElseGet(() -> {
            return begin(findFactoryFor(bigraphClass)).newBigraphBuilder(signature, metaModel);
        });
    }

    private DiscreteIon newDiscreteIonBuilder(Signature<?> signature, String name, Set<String> outerNames) {
        return this.factory.createDiscreteIon(name, outerNames, (AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>) signature);
    }

    private DiscreteIon newDiscreteIonBuilder(Signature<?> signature, String name, Set<String> outerNames, EPackage bigraphMetaModel) {
        return this.factory.createDiscreteIon(name, outerNames, (AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>) signature, bigraphMetaModel);
    }

    private PureBigraphGenerator newRandomBigraphBuilder(Signature<?> signature) {
        return this.factory.createRandomBuilder((DefaultDynamicSignature) signature);
    }

    private PureBigraphGenerator newRandomBigraphBuilder(Signature<?> signature, EPackage metaModel) {
        return this.factory.createRandomBuilder((DefaultDynamicSignature) signature, metaModel);
    }

    private Placings newPlacingsBuilder(Signature<?> signature) {
        return this.factory.createPlacings((AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>) signature);
    }

    private Placings newPlacingsBuilder(Signature<?> signature, EPackage metaModel) {
        return this.factory.createPlacings((AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>) signature, metaModel);
    }

    private Linkings newLinkingsBuilder(Signature<?> signature) {
        return this.factory.createLinkings((AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>) signature);
    }

    private Linkings newLinkingsBuilder(Signature<?> signature, EPackage metaModel) {
        return this.factory.createLinkings((AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>) signature, metaModel);
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
}
