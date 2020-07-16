/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class JDBCQueryTest {

    /*@Test
    public void testPostgresSql() {
        Long dsId = 72959403468259328L;
        QuerySite querySite = QuerySite
                .newBuilder()
                .datasourceId(dsId)
                .databaseName("kun")
                .build();
        JDBCQueryEntry queryEntry = JDBCQueryEntry
                .newBuilder()
                .queryString("select * from kun_mt_datasource")
                .build();
        JDBCQuery query = JDBCQueryBuilder.builder()
                .querySite(querySite)
                .queryParams(queryEntry)
                .build();
        QueryResultSet resultSet = JDBCQueryExecutor.getInstance("application-test.yaml").execute(query);
        for (Map map : resultSet.getResultSet()) {
            if (dsId.equals(map.get("id"))) {
                return;
            }
        }
        Assert.fail();
    }

    @Test
    public void testAthena() {
        Long dsId = 72989195441799168L;
        QuerySite querySite = QuerySite
                .newBuilder()
                .datasourceId(dsId)
                .build();

        JDBCQueryEntry queryEntry = JDBCQueryEntry
                .newBuilder()
                .queryString("select 1")
                .build();

        JDBCQuery query = JDBCQueryBuilder.builder()
                .querySite(querySite)
                .queryParams(queryEntry)
                .build();
        JDBCQueryExecutor.getInstance("application-test.yaml").execute(query);
    }*/
}
