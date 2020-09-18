package com.miotech.kun.metadata.databuilder;

import com.arangodb.ArangoDB;
import com.arangodb.entity.BaseDocument;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import io.testcontainers.arangodb.containers.ArangoContainer;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;

@Singleton
public class TestContainerBuilder {
    private final Logger logger = LoggerFactory.getLogger(TestContainerBuilder.class);

    private static final String MONGO_IMAGE = "mongo:4.2";
    private static final String POSTGRES_IMAGE = "postgres:12.3";
    private static final String ELASTICSEARCH_IMAGE = "elasticsearch:6.8.10";
    private static final String ARANGO_IMAGE_VERSION = "3.6.4";
    private static final Integer MONGO_PORT = 27017;

    private static String DATASOURCE_INSERT_SQL = "INSERT INTO \"public\".kun_mt_datasource(id, connection_info, type_id) VALUES (1, '{\"host\": \"%s\", \"port\": %d, \"username\": \"%s\", \"password\": \"%s\"}', %d)";
    private static String DATASOURCE_TYPE_INSERT_SQL = "INSERT INTO \"public\".\"kun_mt_datasource_type\"(\"id\", \"name\") VALUES (%d, '%s')";

    private DatabaseOperator dbOperator;

    @Inject
    public TestContainerBuilder(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public MongoDBContainer initMongo() {
        MongoDBContainer mongodb = new MongoDBContainer(MONGO_IMAGE).withExposedPorts(MONGO_PORT);
        mongodb.start();

        dbOperator.update(String.format(DATASOURCE_INSERT_SQL, mongodb.getHost(), mongodb.getFirstMappedPort(), "", "", 2));
        dbOperator.update(String.format(DATASOURCE_TYPE_INSERT_SQL, 2, "MONGODB"));

        return mongodb;
    }

    public PostgreSQLContainer initPostgres() {
        PostgreSQLContainer postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE).withInitScript("sql/init_postgresql.sql");
        postgres.start();

        dbOperator.update(String.format(DATASOURCE_INSERT_SQL, postgres.getHost(), postgres.getFirstMappedPort(), postgres.getUsername(), postgres.getPassword(), 3));
        dbOperator.update(String.format(DATASOURCE_TYPE_INSERT_SQL, 3, "POSTGRESQL"));

        return postgres;
    }

    public ElasticsearchContainer initEs() {
        ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
        elasticsearchContainer.start();

        RestHighLevelClient highLevelClient = new RestHighLevelClient(RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort(), "http"))
        );
        IndexRequest request = new IndexRequest("test_index", "test_type")
                .source("{\"id\":65337177252298752,\"name\":\"test name\"}", XContentType.JSON);
        try {
            highLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("DataBuilderTest.initEs error:", e);
        }

        dbOperator.update(String.format(DATASOURCE_INSERT_SQL, elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort(), "elastic", "changeme", 4));
        dbOperator.update(String.format(DATASOURCE_TYPE_INSERT_SQL, 4, "ELASTICSEARCH"));

        return elasticsearchContainer;

    }

    public ArangoContainer initArango() {
        ArangoContainer arango = new ArangoContainer(ARANGO_IMAGE_VERSION).withoutAuth();
        arango.start();

        ArangoDB client = new ArangoDB.Builder()
                .host(arango.getHost(), arango.getPort())
                .user(arango.getUser())
                .password(arango.getPassword())
                .build();
        client.createDatabase("test_db");
        client.db("test_db").createCollection("test_collection");
        BaseDocument myObject = new BaseDocument();
        myObject.setKey("myKey");
        myObject.addAttribute("name", "test_name");
        client.db("test_db").collection("test_collection").insertDocument(myObject);

        dbOperator.update(String.format(DATASOURCE_INSERT_SQL, arango.getHost(), arango.getPort(), arango.getUser(), "", 5));
        dbOperator.update(String.format(DATASOURCE_TYPE_INSERT_SQL, 5, "ARANGO"));

        return arango;
    }

    public void verifyDatasetRowCount(long rowCount) {
        Long datasetRowCount = dbOperator.fetchOne("select count(*) from kun_mt_dataset", rs -> rs.getLong(1));
        MatcherAssert.assertThat(datasetRowCount, Matchers.is(rowCount));
    }

    public void verifyDatasetStatsRowCount(long rowCount) {
        Long datasetStatRowCount = dbOperator.fetchOne("select count(*) from kun_mt_dataset_stats", rs -> rs.getLong(1));
        MatcherAssert.assertThat(datasetStatRowCount, Matchers.is(rowCount));
    }

}
