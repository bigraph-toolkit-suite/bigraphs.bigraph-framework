package org.bigraphs.framework.simulation.encoding;

import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BigraphCanonicalFormSupport {

    char PREFIX_BARREN = 'r';
    /**
     * Elementary bigraphs are handled differently as their encoding exhibit rather a finalized static nature.
     * Their canonical form is described simply by some lambda functions.
     */
    Map<Class<?>, Function<Object, String>> ELEMENTARY_ENCODINGS = Map.of(Placings.Barren.class, (Void) -> createNameSupplier(String.valueOf(PREFIX_BARREN)).get() + "#", Placings.Join.class, (Void) -> createNameSupplier(String.valueOf(PREFIX_BARREN)).get() + "$01#", Placings.Identity1.class, (n) -> createNameSupplier(String.valueOf(PREFIX_BARREN)).get() + "$0#", Placings.Merge.class, (n) -> {
                StringBuilder sb = new StringBuilder(String.valueOf(PREFIX_BARREN)).append(0);
                if (Integer.parseInt(String.valueOf(n)) > 0) {
                    sb.append('$');
                }
                for (int i = 0; i < Integer.parseInt(String.valueOf(n)); i++) {
                    sb.append(i);
                }
                return sb.append('#').toString();
            }, Placings.Permutation.class, (n) -> {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Integer.parseInt(String.valueOf(n)); i++) {
                    sb.append(PREFIX_BARREN).append(i).append('$').append(i).append('#');
                }
                return sb.toString();
            }, Placings.Symmetry.class, (n) -> {
                int N = Integer.parseInt(String.valueOf(n));
                StringBuilder sb = new StringBuilder();
                for (int i = 0, j = (N - 1); i < N; i++, j--) {
                    sb.append(PREFIX_BARREN).append(i).append('$').append(j).append('#');
                }
                return sb.toString();
            },
            // All linkings
            Linkings.Closure.class, (n) -> {
                assert n instanceof Iterable;
                Iterable<BigraphEntity.InnerName> names = (Iterable<BigraphEntity.InnerName>) n;
                StringBuilder sb = new StringBuilder();
                for (BigraphEntity.InnerName eachName : names) {
                    sb.append(eachName.getName()).append('$');
                }
                sb.replace(sb.length() - 1, sb.length(), "");
                return sb.append('#').toString();
            }, Linkings.Substitution.class, (outerInner) -> {
                assert outerInner instanceof Object[];
                assert ((Object[]) outerInner).length == 2;
                BigraphEntity.OuterName outerName = (BigraphEntity.OuterName) ((Object[]) outerInner)[0];
                Iterable<BigraphEntity.InnerName> innerNames = (Iterable<BigraphEntity.InnerName>) ((Object[]) outerInner)[1];
                StringBuilder sb = new StringBuilder();
                for (BigraphEntity.InnerName eachName : innerNames) {
                    sb.append(eachName.getName()).append(outerName.getName()).append('$');
                }
                sb.replace(sb.length() - 1, sb.length(), "");
                return sb.append('#').toString();
            }, Linkings.Identity.class, (outerInner) -> {
                Iterable<BigraphEntity.InnerName> innerNames = (Iterable<BigraphEntity.InnerName>) (outerInner);
                StringBuilder sb = new StringBuilder("");
                for (BigraphEntity.InnerName eachName : innerNames) {
                    sb.append(eachName.getName()).append(eachName.getName()).append('$');
                }
                sb.replace(sb.length() - 1, sb.length(), "");
                return sb.append('#').toString();
            });

    static Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

}
