package com.miotech.kun.commons.db;

import com.google.inject.Inject;
import com.miotech.kun.commons.testing.DatabaseTestBase;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DualWriteOperatorTest extends DatabaseTestBase {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:6.8.12";
    private static final String INDEX = "test_index";
    private static final String TYPE = "test_type";
    private static final String ID_1 = "1";
    private static final String ID_2 = "2";

    private static final String UPDATE_SQL = "INSERT INTO kun_test VALUES(?)";
    private static final String SOURCE = "{\"name\": \"new name\"}";
    private static final String SOURCE_FORMAT_ERROR = "{\"name\": \"new name\",}";

    private ElasticsearchContainer elasticsearchContainer;

    @Inject
    private DataSource dataSource;

    @Inject
    private DatabaseOperator dbOperator;

    @Override
    protected void setFlayWayLocation() {
        flywayLocation = "sql/";
    }

    @Before
    public void init() {
        elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
        elasticsearchContainer.start();

        prepare();
    }

    @After
    public void close() {
        elasticsearchContainer.close();
    }

    @Test
    public void testWrite_commit() {
        DualWriteOperator operator = new DualWriteOperator(new DBOperator(dataSource), new ESOperator(elasticsearchContainer.getHost(),
                elasticsearchContainer.getFirstMappedPort(), "", ""));
        operator.transaction((dbOperator, esOperator) -> {
            dbOperator.update(UPDATE_SQL, "1");
            esOperator.update(INDEX, TYPE, ID_1, SOURCE);
            esOperator.update(INDEX, TYPE, ID_2, SOURCE);
        });

        verifyDB(is(1L));
        verifyES(ID_1, is("new name"));
        verifyES(ID_2, is("new name"));
    }

    @Test
    public void testWrite_commit_multiThread() {
        DualWriteOperator operator = new DualWriteOperator(new DBOperator(dataSource), new ESOperator(elasticsearchContainer.getHost(),
                elasticsearchContainer.getFirstMappedPort(), "", ""));
        CountDownLatch cd = new CountDownLatch(2);
        new Thread(() -> {
            try {
                operator.transaction((dbOperator, esOperator) -> {
                    dbOperator.update(UPDATE_SQL, "1");
                    esOperator.update(INDEX, TYPE, ID_1, SOURCE);
                });
            } finally {
                cd.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                operator.transaction((dbOperator, esOperator) -> {
                    dbOperator.update(UPDATE_SQL, "2");
                    esOperator.update(INDEX, TYPE, ID_2, SOURCE);
                });
            } finally {
                cd.countDown();
            }
        }).start();

        try {
            cd.await();
        } catch (InterruptedException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
        verifyDB(is(2L));
        verifyES(ID_1, is("new name"));
        verifyES(ID_2, is("new name"));
    }

    @Test
    public void testWrite_rollback() {
        DualWriteOperator operator = new DualWriteOperator(new DBOperator(dataSource), new ESOperator(elasticsearchContainer.getHost(),
                elasticsearchContainer.getFirstMappedPort(), "", ""));
        try {
            operator.transaction((dbOperator, esOperator) -> {
                dbOperator.update(UPDATE_SQL, "1");
                esOperator.update(INDEX, TYPE, ID_1, SOURCE);
                esOperator.update(INDEX, TYPE, ID_2, SOURCE_FORMAT_ERROR);
            });
        } catch (Exception e) {
            assertThat(e, instanceOf(RuntimeException.class));
        }

        verifyDB(is(0L));
        verifyES(ID_1, is("test name 1"));
        verifyES(ID_2, is("test name 2"));
    }

    @Test
    public void testWrite_rollback_multiThread() {
        DualWriteOperator operator = new DualWriteOperator(new DBOperator(dataSource), new ESOperator(elasticsearchContainer.getHost(),
                elasticsearchContainer.getFirstMappedPort(), "", ""));
        CountDownLatch cd = new CountDownLatch(2);
        new Thread(() -> {
            try {
                operator.transaction((dbOperator, esOperator) -> {
                    dbOperator.update(UPDATE_SQL, "1");
                    esOperator.update(INDEX, TYPE, ID_1, SOURCE);
                });
            } finally {
                cd.countDown();
            }
        }).start();

        new Thread(() -> {
            try {
                operator.transaction((dbOperator, esOperator) -> {
                    dbOperator.update(UPDATE_SQL, "2");
                    esOperator.update(INDEX, TYPE, ID_2, SOURCE_FORMAT_ERROR);
                });
            } finally {
                cd.countDown();
            }
        }).start();

        try {
            cd.await();
        } catch (InterruptedException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }

        verifyDB(is(1L));
        verifyES(ID_1, is("new name"));
        verifyES(ID_2, is("test name 2"));
    }

    private void prepare() {
        prepareEs();
        prepareDb();
    }

    private void prepareEs() {
        RestHighLevelClient highLevelClient = createClient();
        try {
            IndexRequest request = new IndexRequest(INDEX, TYPE, ID_1)
                    .source("{\"name\":\"test name 1\"}", XContentType.JSON);
            highLevelClient.index(request, RequestOptions.DEFAULT);

            request = new IndexRequest(INDEX, TYPE, ID_2)
                    .source("{\"name\":\"test name 2\"}", XContentType.JSON);
            highLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    private void prepareDb() {
        dbOperator.update("CREATE TABLE IF NOT EXISTS kun_test(id BIGINT)");
    }

    private RestHighLevelClient createClient() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(elasticsearchContainer.getHost(), elasticsearchContainer.getFirstMappedPort(), "http")));
    }

    private void verifyDB(Matcher<? super Long> matcher) {
        Long rowCount = dbOperator.fetchOne("SELECT COUNT(1) FROM kun_test", rs -> rs.getLong(1));
        assertThat(rowCount, matcher);
    }

    private void verifyES(String id, Matcher<? super Object> matcher) {
        RestHighLevelClient client = createClient();
        GetResponse document;
        try {
            document = client.get(new GetRequest(INDEX, TYPE, id), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
        assertThat(document.getSource().get("name"), matcher);
    }

}
