package com.miotech.kun.datadashboard.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.datadashboard.model.bo.ColumnMetricsRequest;
import com.miotech.kun.datadashboard.model.bo.RowCountChangeRequest;
import com.miotech.kun.datadashboard.model.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@Repository
public class MetadataRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Long getTotalDatasetCount() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as count")
                .from("kun_mt_dataset")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public DatasetRowCountChanges getRowCountChange(RowCountChangeRequest rowCountChangeRequest) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("stats_1.dataset_gid",
                        "stats_1.row_count as last_row_count",
                        "stats_2.row_count as prev_row_count",
                        "(stats_1.row_count - stats_2.row_count) as row_change")
                .from("(select dataset_gid, row_count\n" +
                        "      from (select dataset_gid, row_count, rank() over (partition by dataset_gid order by stats_date desc nulls last )\n" +
                        "            from kun_mt_dataset_stats) stats\n" +
                        "      where stats.rank = 1) stats_1")
                .join("inner", "(select dataset_gid, row_count\n" +
                        "      from (select dataset_gid, row_count, rank() over (partition by dataset_gid order by stats_date desc nulls last )\n" +
                        "            from kun_mt_dataset_stats) stats\n" +
                        "      where stats.rank = 2)", "stats_2")
                .on("stats_1.dataset_gid = stats_2.dataset_gid")
                .orderBy("row_change desc")
                .limit(rowCountChangeRequest.getPageSize())
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetRowCountChanges rowCountChanges = new DatasetRowCountChanges();
            while (rs.next()) {
                DatasetRowCountChange rowCountChange = new DatasetRowCountChange();
                DatasetBasic datasetBasic = getDatasetBasic(rs.getLong("dataset_gid"));
                rowCountChange.setDatasetName(datasetBasic.getDatasetName());
                rowCountChange.setDatabase(datasetBasic.getDatabase());
                rowCountChange.setDataSource(datasetBasic.getDataSource());
                rowCountChange.setRowCount(rs.getLong("last_row_count"));
                rowCountChange.setRowChange(rs.getLong("row_change"));
                if (rowCountChange.getRowChange().equals(rowCountChange.getRowCount())) {
                    if (rowCountChange.getRowCount() == 0L) {
                        rowCountChange.setRowChangeRatio(0f);
                    } else {
                        rowCountChange.setRowChangeRatio(Float.POSITIVE_INFINITY);
                    }
                } else {
                    Float changeRatio = (rowCountChange.getRowChange()) * 1.0f / rs.getLong("prev_row_count");
                    rowCountChange.setRowChangeRatio(changeRatio);
                }
                rowCountChanges.add(rowCountChange);
            }
            return rowCountChanges;
        });
    }

    public DatasetBasic getDatasetBasic(Long gid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.name as dataset_name",
                        "kmd.database_name as database_name",
                        "kmdsrca.name as datasource_name")
                .from("kun_mt_dataset kmd")
                .join("inner", "kun_mt_datasource", "kmdsrc").on("kmdsrc.id = kmd.datasource_id")
                .join("inner", "kun_mt_datasource_attrs", "kmdsrca").on("kmdsrc.id = kmdsrca.datasource_id")
                .where("kmd.gid = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic datasetBasic = new DatasetBasic();
            if (rs.next()) {
                datasetBasic.setDatasetName(rs.getString("dataset_name"));
                datasetBasic.setDatabase(rs.getString("database_name"));
                datasetBasic.setDataSource(rs.getString("datasource_name"));
            }
            return datasetBasic;
        }, gid);

    }

    private static final Map<String, String> COLUMN_METRICS_REQUEST_ORDER_MAP = new HashMap<>();
    static {
        COLUMN_METRICS_REQUEST_ORDER_MAP.put("columnNullCount", "null_count");
        COLUMN_METRICS_REQUEST_ORDER_MAP.put("columnDistinctCount", "distinct_count");
        COLUMN_METRICS_REQUEST_ORDER_MAP.put("totalRowCount", "row_count");
    }

    public ColumnMetricsList getColumnMetricsList(ColumnMetricsRequest columnMetricsRequest) {
        SQLBuilder preSqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("field_id",
                        "distinct_count",
                        "row_count",
                        "(row_count - nonnull_count) as null_count",
                        "kmd.name as dataset_name",
                        "kmdf.name as field_name")
                .from("(select field_id, distinct_count, nonnull_count, rank() over (partition by field_id order by stats_date desc nulls last )\n" +
                        "from kun_mt_dataset_field_stats) field_stats")
                .join("inner", "kun_mt_dataset_field", "kmdf").on("kmdf.id = field_stats.field_id")
                .join("inner", "kun_mt_dataset", "kmd").on("kmdf.dataset_gid = kmd.gid")
                .join("inner", "(select dataset_gid, row_count, rank() over (partition by dataset_gid order by stats_date desc nulls last )\n" +
                        "from kun_mt_dataset_stats)", "dataset_stats").on("dataset_stats.dataset_gid = kmdf.dataset_gid")
                .where("field_stats.rank = 1 and dataset_stats.rank = 1");

        String countSql = "select count(1) from (" + preSqlBuilder.getSQL() + ") as result";

        String sql = preSqlBuilder
                .orderBy(COLUMN_METRICS_REQUEST_ORDER_MAP.get(columnMetricsRequest.getSortColumn()) + " " + columnMetricsRequest.getSortOrder())
                .offset(getOffset(columnMetricsRequest.getPageNumber(), columnMetricsRequest.getPageSize()))
                .limit(columnMetricsRequest.getPageSize())
                .getSQL();

        ColumnMetricsList columnMetricsList = new ColumnMetricsList();
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class);
        columnMetricsList.setPageNumber(columnMetricsRequest.getPageNumber());
        columnMetricsList.setPageSize(columnMetricsRequest.getPageSize());
        columnMetricsList.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                ColumnMetrics metrics = new ColumnMetrics();
                metrics.setDatasetName(rs.getString("dataset_name"));
                metrics.setColumnName(rs.getString("field_name"));
                metrics.setTotalRowCount(rs.getLong("row_count"));
                metrics.setColumnDistinctCount(rs.getLong("distinct_count"));
                metrics.setColumnNullCount(rs.getLong("null_count"));
                columnMetricsList.add(metrics);
            }
            return columnMetricsList;
        });
    }
}
