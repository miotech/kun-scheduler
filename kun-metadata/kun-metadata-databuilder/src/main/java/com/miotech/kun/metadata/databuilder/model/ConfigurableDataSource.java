package com.miotech.kun.metadata.databuilder.model;

public class ConfigurableDataSource extends DataSource {

    private final Catalog catalog;

    private final QueryEngine queryEngine;

    public ConfigurableDataSource(long id, Catalog catalog, QueryEngine queryEngine) {
        super(id, Type.AWS);
        this.catalog = catalog;
        this.queryEngine = queryEngine;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public QueryEngine getQueryEngine() {
        return queryEngine;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Catalog catalog;
        private QueryEngine queryEngine;
        private long id;

        private Builder() {
        }

        public Builder withCatalog(Catalog catalog) {
            this.catalog = catalog;
            return this;
        }

        public Builder withQueryEngine(QueryEngine queryEngine) {
            this.queryEngine = queryEngine;
            return this;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public ConfigurableDataSource build() {
            return new ConfigurableDataSource(id, catalog, queryEngine);
        }
    }
}
