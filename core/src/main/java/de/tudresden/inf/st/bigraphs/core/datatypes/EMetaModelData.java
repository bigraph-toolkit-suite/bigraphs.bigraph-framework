package de.tudresden.inf.st.bigraphs.core.datatypes;

import java.util.Objects;

/**
 * Meta data for bigraphical Ecore model files.
 * <p>
 * The builder class {@link MetaModelDataBuilder} shall be used to create instances of this class.
 *
 * @author Dominik Grzelak
 */
public class EMetaModelData {

    private final String name;
    private final String nsPrefix;
    private final String nsUri;

    private EMetaModelData(String name, String nsPrefix, String nsUri) {
        this.name = name;
        this.nsPrefix = nsPrefix;
        this.nsUri = nsUri;
    }

    public String getName() {
        return name;
    }

    public String getNsPrefix() {
        return nsPrefix;
    }

    public String getNsUri() {
        return nsUri;
    }

    public static MetaModelDataBuilder builder() {
        return new MetaModelDataBuilder();
    }

    /**
     * Builder class to create {@link EMetaModelData} instances.
     *
     * @author Dominik Grzelak
     */
    public static class MetaModelDataBuilder {
        private String name;
        private String nsPrefix;
        private String nsUri;

        public MetaModelDataBuilder setName(String name) {
            this.name = Objects.isNull(name) ? "" : name;
            return this;
        }

        public MetaModelDataBuilder setNsPrefix(String nsPrefix) {
            this.nsPrefix = Objects.isNull(nsPrefix) ? "" : nsPrefix;
            return this;
        }

        public MetaModelDataBuilder setNsUri(String nsUri) {
            this.nsUri = Objects.isNull(nsUri) ? "" : nsUri;
            return this;
        }

        /**
         * Creates an instance of {@link EMetaModelData}.
         * <p>
         * When only {@code name} is set, the other values are being changed to: <br/>
         * nsURI := "http:///{@code name}.ecore", nsPrefix := "{@code name}".
         *
         * @return a meta model data object with the provided parameters
         */
        public EMetaModelData create() {
            if (Objects.isNull(nsUri) && Objects.isNull(nsPrefix) && Objects.nonNull(name)) {
                nsUri = "http://" + name + ".ecore";
                nsPrefix = name;
            }
            return new EMetaModelData(name, nsPrefix, nsUri);
        }
    }
}
