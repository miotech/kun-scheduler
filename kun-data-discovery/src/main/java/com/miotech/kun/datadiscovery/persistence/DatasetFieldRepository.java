package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetFieldSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.DatasetField;
import com.miotech.kun.datadiscovery.model.entity.DatasetFieldPage;
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

    public DatasetFieldPage findByDatasetGid(Long datasetGid, DatasetFieldSearchRequest searchRequest) {
        Long rowCount = datasetRepository.getRowCount(datasetGid);

        DatasetFieldPage datasetFieldPage = new DatasetFieldPage();
        List<Object> pstmtArgs = new ArrayList<>();
        String sql = "select kmdf.*,\n" +
                "       kmdfs.stats_date as high_watermark,\n" +
                "       kmdfs.distinct_count,\n" +
                "       kmdfs.nonnull_count\n" +
                "     from kun_mt_dataset_field kmdf\n" +
                "         left join kun_mt_dataset_field_stats kmdfs on kmdf.id = kmdfs.field_id\n" +
                "         inner join (select field_id, max(stats_date) as max_time\n" +
                "                     from (select kmdfs.*\n" +
                "                           from kun_mt_dataset_field_stats kmdfs\n" +
                "                                    right join kun_mt_dataset_field kmdf\n" +
                "                                               on kmdfs.field_id = kmdf.id and kmdf.dataset_gid = ?) as kmdfs\n" +
                "                     group by field_id) watermark\n" +
                "                    on (kmdf.id = watermark.field_id and kmdfs.stats_date = watermark.max_time) or\n" +
                "                       kmdfs.stats_date is null\n";
        String whereClause = "where kmdf.dataset_gid = ?\n";
        pstmtArgs.add(datasetGid);
        pstmtArgs.add(datasetGid);
        if (StringUtils.isNotEmpty(searchRequest.getKeyword())) {
            whereClause += "and upper(kmdf.name) like ?\n";
            pstmtArgs.add(toLikeSql(searchRequest.getKeyword().toUpperCase()));
        }
        String groupByClause = "group by kmdf.id, high_watermark, kmdfs.distinct_count, kmdfs.nonnull_count\n";

        sql += whereClause + groupByClause;

        String totalCountSql = "select count(1) as total_count from kun_mt_dataset_field kmdf " + whereClause;
        Long totalCount = jdbcTemplate.query(totalCountSql, rs -> {
            if (rs.next()) {
                return rs.getLong("total_count");
            }
            return null;
        }, pstmtArgs.subList(1, pstmtArgs.size()).toArray());

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
                Watermark watermark = new Watermark();
                watermark.setTime(timestampToMillis(rs, "high_watermark"));
                column.setHighWatermark(watermark);
                column.setDistinctCount(rs.getLong("distinct_count"));
                column.setNotNullCount(rs.getLong("nonnull_count"));
                if (rowCount != null && rowCount != 0L) {
                    double nonnullPercentage = rs.getLong("nonnull_count") * 1.0 / rowCount;
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
        jdbcTemplate.update(sql, datasetFieldRequest, id);
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
