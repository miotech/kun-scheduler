import com.miotech.kun.commons.query.JDBCQuery;
import com.miotech.kun.commons.query.JDBCQueryExecutor;
import com.miotech.kun.commons.query.model.QueryResultSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/7/13
 */
@Ignore
public class JDBCQueryTest {

    @Test
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

        JDBCQuery query = JDBCQuery.newBuilder()
                .datasetId(65465271921410048L)
                .queryString("select count(*) from dm.ref_esg_csr_indicator where length(csr_indicator_code)/3<>level")
                .build();
        QueryResultSet resultSet = JDBCQueryExecutor.getInstance("application-test.yaml").execute(query);
        Object value = resultSet.getResultSet().get(0).values().iterator().next();
    }
}
