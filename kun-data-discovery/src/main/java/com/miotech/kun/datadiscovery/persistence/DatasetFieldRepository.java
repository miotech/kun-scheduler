package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DatasetField;
import com.miotech.kun.datadiscovery.model.entity.DatasetFieldPage;
import com.miotech.kun.datadiscovery.model.entity.Watermark;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Repository
public class DatasetFieldRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatasetRepository datasetRepository;

    public DatasetFieldPage findByDatasetGid(Long datasetGid, DatasetFieldSearchRequest searchRequest) {
        Long rowCount = datasetRepository.getLatestStats(datasetGid).getRowCount();

        DatasetFieldPage datasetFieldPage = new DatasetFieldPage();
        /*
           Columns not included: dataset_gid, raw_type, is_primary_key, is_nullable

           note: "distinct on" keyword only applies to postgresql.
           For compatibility concern (like MySQL) this query could be change to:

           select kmdf.id as id, kmdf.name as name, kmdf.type as type, kmdf.description as description,
           stats.distinct_count as distinct_count, stats.nonnull_count as nonnull_count, stats.stats_date as stats_date
           from kun_mt_dataset_field kmdf
           left join (
               select x.field_id as field_id, y.nonnull_count as nonnull_count, y.distinct_count as distinct_count, x.stats_date as stats_date
               from (
                   select field_id, max(stats_date) as stats_date
                   from kun_mt_dataset_field_stats
                   group by field_id
               ) x
               left join kun_mt_dataset_field_stats y
               on x.field_id = y.field_id and x.stats_date = y.stats_date
           ) stats on kmdf.id = stats.field_id
           where dataset_gid = ? [YOUR WHERE CLAUSE]
           order by kmdf.name asc, stats.stats_date desc;
        */
        StringBuilder sqlBuilder = new StringBuilder(
                "select distinct on (name, id) kmdf.id as id, kmdf.name as name, kmdf.type as type, kmdf.description as description, \n" +
                "stats.distinct_count as distinct_count, stats.nonnull_count as nonnull_count, stats.stats_date as stats_date\n" +
                "from kun_mt_dataset_field kmdf\n" +
                "left join kun_mt_dataset_field_stats stats\n" +
                "on kmdf.id = stats.field_id\n"
        );

        Pair<String, List<Object>> whereClauseAndParams = getWhereClauseAndArgumentsForFindByDatasetGid(datasetGid, searchRequest);
        String whereClause = whereClauseAndParams.getLeft();
        List<Object> preparedStmtArgs = whereClauseAndParams.getRight();
        sqlBuilder.append(whereClause);

        String totalCountSql = "select count(*) as total_count from kun_mt_dataset_field kmdf " + whereClause;
        Integer totalCount = jdbcTemplate.query(totalCountSql, (rs, rowIndex) -> rs.getInt("total_count"), preparedStmtArgs.toArray())
                .stream().findFirst().orElseThrow(IllegalStateException::new);

        String orderByClause = "order by kmdf.name asc, kmdf.id desc, stats.stats_date desc\n";
        String limitSql = toLimitSql(searchRequest.getPageNumber(), searchRequest.getPageSize());

        sqlBuilder.append(orderByClause);
        sqlBuilder.append(limitSql);

        String sql = sqlBuilder.toString();

        List<DatasetField> datasetFields = jdbcTemplate.query(sql, rs -> {
            List<DatasetField> columns = new ArrayList<>();
            while (rs.next()) {
                DatasetField column = new DatasetField();
                column.setId(rs.getLong("id"));
                column.setName(rs.getString("name"));
                column.setDescription(rs.getString("description"));
                column.setType(rs.getString("type"));
                Watermark watermark = new Watermark();
                column.setHighWatermark(watermark);
                column.setDistinctCount(rs.getLong("distinct_count"));
                column.setNotNullCount(rs.getLong("nonnull_count"));
                if (rowCount != null && !rowCount.equals(0L) && column.getNotNullCount() != null) {
                    double nonnullPercentage = column.getNotNullCount() * 1.0 / rowCount;
                    BigDecimal bigDecimal = BigDecimal.valueOf(nonnullPercentage);
                    column.setNotNullPercentage(bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
                columns.add(column);
            }
            return columns;
        }, preparedStmtArgs.toArray());

        datasetFieldPage.setPageNumber(searchRequest.getPageNumber());
        datasetFieldPage.setPageSize(searchRequest.getPageSize());
        datasetFieldPage.setTotalCount(totalCount);
        datasetFieldPage.setDatasetFields(datasetFields);

        return datasetFieldPage;
    }

    private Pair<String, List<Object>> getWhereClauseAndArgumentsForFindByDatasetGid(Long datasetGid, DatasetFieldSearchRequest searchRequest) {
        // init clause builder and parameter
        StringBuilder whereClauseBuilder = new StringBuilder("where kmdf.dataset_gid = ?\n");
        List<Object> whereArgs = new ArrayList<>();
        whereArgs.add(datasetGid);

        if (StringUtils.isNotEmpty(searchRequest.getKeyword())) {
            whereClauseBuilder.append("and kmdf.name ILIKE ?\n");
            whereArgs.add(toLikeSql(searchRequest.getKeyword().toUpperCase()));
        }

        return Pair.of(whereClauseBuilder.toString(), whereArgs);
    }

    public DatasetField update(Long id, DatasetFieldRequest datasetFieldRequest) {
        String sql = "update kun_mt_dataset_field set description = ? where id = ?";
        jdbcTemplate.update(sql, datasetFieldRequest.getDescription(), id);
        return find(id);
    }

    public DatasetField find(Long id) {
        String sql = "select id, name, type, description from kun_mt_dataset_field where id = ?";

        return jdbcTemplate.query(sql, ps -> ps.setLong(1, id), rs -> {
            DatasetField datasetField = new DatasetField();
            if (rs.next()) {
                datasetField.setId(rs.getLong("id"));
                datasetField.setName(rs.getString("name"));
                datasetField.setType(rs.getString("type"));
                datasetField.setDescription(rs.getString("description"));
            }
            return datasetField;
        });
    }
}
