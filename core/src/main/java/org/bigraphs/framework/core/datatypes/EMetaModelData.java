package org.bigraphs.framework.core.datatypes;

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
            this.name = (name) == null ? "" : name;
            return this;
        }

        public MetaModelDataBuilder setNsPrefix(String nsPrefix) {
            this.nsPrefix = (nsPrefix) == null ? "" : nsPrefix;
            return this;
        }

        public MetaModelDataBuilder setNsUri(String nsUri) {
            this.nsUri = (nsUri) == null ? "" : nsUri;
            return this;
        }

        /**
         * Creates an instance of {@link EMetaModelData}.
         * <p>
         * When only {@code name} is set, the other values are being changed to: <br>
         * nsURI := "http:///{@code name}.ecore", nsPrefix := "{@code name}".
         *
         * @return a meta model data object with the provided parameters
         */
        public EMetaModelData create() {
            if ((nsUri) == null && (nsPrefix) == null && (name) != null) {
                nsUri = "http://" + name + ".ecore";
                nsPrefix = name;
            }
            return new EMetaModelData(name, nsPrefix, nsUri);
        }
    }
}
