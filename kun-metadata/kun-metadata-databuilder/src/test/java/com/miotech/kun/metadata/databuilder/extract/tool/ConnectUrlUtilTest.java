package com.miotech.kun.metadata.databuilder.extract.tool;

import com.miotech.kun.metadata.databuilder.constant.DatabaseType;
import com.miotech.kun.metadata.databuilder.extract.tool.ConnectUrlUtil;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class ConnectUrlUtilTest {

    @Test
    public void testConvertToConnectUrl_postgres() {
        String url = "jdbc:postgresql://127.0.0.1:5432/";
        String generateUrl = ConnectUrlUtil.convertToConnectUrl("127.0.0.1", 5432, "postgres",
                "password", DatabaseType.POSTGRES);
        MatcherAssert.assertThat(url, Matchers.is(generateUrl));
    }

    @Test
    public void testConvertToConnectUrl_es() {
        String url = "127.0.0.1:9200";
        String generateUrl = ConnectUrlUtil.convertToConnectUrl("127.0.0.1", 9200, "postgres",
                "password", DatabaseType.ELASTICSEARCH);
        MatcherAssert.assertThat(url, Matchers.is(generateUrl));
    }

    @Test
    public void testConvertToConnectUrl_arango() {
        String url = "127.0.0.1:8529";
        String generateUrl = ConnectUrlUtil.convertToConnectUrl("127.0.0.1", 8529, "postgres",
                "password", DatabaseType.ARANGO);
        MatcherAssert.assertThat(url, Matchers.is(generateUrl));
    }

    @Test
    public void testConvertToConnectUrl_mongo() {
        String url = "mongodb://mongo:password@127.0.0.1:27017";
        String generateUrl = ConnectUrlUtil.convertToConnectUrl("127.0.0.1", 27017, "mongo",
                "password", DatabaseType.MONGO);
        MatcherAssert.assertThat(url, Matchers.is(generateUrl));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertToConnectUrl_invalid() {
        String generateUrl = ConnectUrlUtil.convertToConnectUrl("127.0.0.1", 27017, "glue",
                "password", DatabaseType.GLUE);
    }

}
