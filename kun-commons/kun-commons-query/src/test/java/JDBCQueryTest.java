/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
public class JDBCQueryTest {

    /*@Test
    public void testPostgresSql() {
        Long datasetId = 1L;

        JDBCQuery query = JDBCQuery.newBuilder()
                .datasetId(datasetId)
                .queryString("select * from kun_mt_dataset where gid = " + datasetId)
                .build();
        QueryResultSet resultSet = JDBCQueryExecutor.getInstance("application-test.yaml").execute(query);
        for (Map map : resultSet.getResultSet()) {
            if (datasetId.equals(map.get("gid"))) {
                System.out.println("gid = " + map.get("gid"));
                return;
            }
        }
        Assert.fail();
    }

    @Test
    public void testAthena() {
        Long datasourceId = 72989195441799168L;

        JDBCQuery query = JDBCQuery.newBuilder()
                .datasourceId(datasourceId)
                .queryString("select 1")
                .build();
        JDBCQueryExecutor.getInstance("application-test.yaml").execute(query);
    }*/
}
