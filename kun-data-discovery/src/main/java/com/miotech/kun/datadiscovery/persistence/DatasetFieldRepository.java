package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DatasetField;
import com.miotech.kun.datadiscovery.model.entity.DatasetFieldPage;
import com.miotech.kun.datadiscovery.model.entity.DatasetFieldStats;
import com.miotech.kun.datadiscovery.model.entity.Watermark;
import org.apache.commons.lang3.StringUtils;
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

    public DatasetFieldStats getFieldStats(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("distinct_count",
                        "nonnull_count")
                .from("kun_mt_dataset_field_stats")
                .where("field_id = ?")
                .orderBy("stats_date desc")
                .limit(1)
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetFieldStats stats = new DatasetFieldStats();
            if (rs.next()) {
                stats.setDistinctCount(rs.getLong("distinct_count"));
                stats.setNotNullCount(rs.getLong("nonnull_count"));
            }
            return stats;
        }, id);
    }

    public DatasetFieldPage findByDatasetGid(Long datasetGid, DatasetFieldSearchRequest searchRequest) {
        Long rowCount = datasetRepository.getRowCount(datasetGid);

        DatasetFieldPage datasetFieldPage = new DatasetFieldPage();
        List<Object> pstmtArgs = new ArrayList<>();
        String sql = "select kmdf.*\n" +
                "     from kun_mt_dataset_field kmdf\n";
        String whereClause = "where kmdf.dataset_gid = ?\n";
        pstmtArgs.add(datasetGid);
        if (StringUtils.isNotEmpty(searchRequest.getKeyword())) {
            whereClause += "and upper(kmdf.name) like ?\n";
            pstmtArgs.add(toLikeSql(searchRequest.getKeyword().toUpperCase()));
        }

        sql += whereClause;

        String totalCountSql = "select count(1) as total_count from kun_mt_dataset_field kmdf " + whereClause;
        Long totalCount = jdbcTemplate.query(totalCountSql, rs -> {
            if (rs.next()) {
                return rs.getLong("total_count");
            }
            return null;
        }, pstmtArgs.toArray());

        String orderByClause = "order by kmdf.name\n";
        String limitSql = toLimitSql(searchRequest.getPageNumber(), searchRequest.getPageSize());

        sql += orderByClause + limitSql;

        List<DatasetField> datasetFields = jdbcTemplate.query(sql, rs -> {
            List<DatasetField> columns = new ArrayList<>();
            while (rs.next()) {
                DatasetField column = new DatasetField();
                column.setId(rs.getLong("id"));
                column.setName(rs.getString("name"));
                column.setDescription(rs.getString("description"));
                column.setType(rs.getString("type"));
                DatasetFieldStats fieldStats = getFieldStats(column.getId());
                Watermark watermark = new Watermark();
                column.setHighWatermark(watermark);
                column.setDistinctCount(fieldStats.getDistinctCount());
                column.setNotNullCount(fieldStats.getNotNullCount());
                if (rowCount != null && !rowCount.equals(0L) && column.getNotNullCount() != null) {
                    double nonnullPercentage = column.getNotNullCount() * 1.0 / rowCount;
                    BigDecimal bigDecimal = BigDecimal.valueOf(nonnullPercentage);
                    column.setNotNullPercentage(bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
                }

                columns.add(column);
            }
            return columns;
        }, pstmtArgs.toArray());
        datasetFieldPage.setPageNumber(searchRequest.getPageNumber());
        datasetFieldPage.setPageSize(searchRequest.getPageSize());
        datasetFieldPage.setTotalCount(totalCount);
        datasetFieldPage.setDatasetFields(datasetFields);
        return datasetFieldPage;
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
