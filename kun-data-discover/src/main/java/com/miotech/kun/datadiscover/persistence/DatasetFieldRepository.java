package com.miotech.kun.datadiscover.persistence;

import com.miotech.kun.datadiscover.common.util.DateUtil;
import com.miotech.kun.datadiscover.model.bo.DatasetColumnRequest;
import com.miotech.kun.datadiscover.model.entity.DatasetColumn;
import com.miotech.kun.datadiscover.model.entity.Watermark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Repository
public class DatasetFieldRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<DatasetColumn> findByDatasetGid(Long datasetGid) {
        String getRowCountSql = "select row_count from kun_mt_dataset_stats where dataset_gid = ?";
        Long rowCount = jdbcTemplate.query(getRowCountSql, ps -> ps.setLong(1, datasetGid), rs -> {
            if (rs.next()) {
                return rs.getLong("row_count");
            }
            return null;
        });

        String sql = "select kmdf.*,\n" +
                "       kmdfs.stats_date as high_watermark,\n" +
                "       kmdfs.distinct_count,\n" +
                "       kmdfs.nonnull_count\n" +
                "     from kun_mt_dataset_field kmdf\n" +
                "         left join kun_mt_dataset_field_stats kmdfs on kmdf.id = kmdfs.field_id\n" +
                "         right join (select field_id, max(stats_date) as max_time\n" +
                "                     from kun_mt_dataset_field_stats\n" +
                "                     group by field_id) watermark\n" +
                "                    on (kmdf.id = watermark.field_id and kmdfs.stats_date = watermark.max_time) or\n" +
                "                       kmdfs.stats_date is null\n";
        String whereClause = "where kmdf.dataset_gid = ?\n";
        String groupByClause = "group by kmdf.id, high_watermark, kmdfs.distinct_count, kmdfs.nonnull_count";

        return jdbcTemplate.query(sql + whereClause + groupByClause, ps -> ps.setLong(1, datasetGid), rs -> {
            List<DatasetColumn> columns = new ArrayList<>();
            while (rs.next()) {
                DatasetColumn column = new DatasetColumn();
                column.setId(rs.getLong("id"));
                column.setName(rs.getString("name"));
                column.setDescription(rs.getString("description"));
                column.setType(rs.getString("type"));
                Watermark watermark = new Watermark();
                watermark.setTime(DateUtil.dateTimeToMillis(rs.getObject("high_watermark", OffsetDateTime.class)));
                column.setHighWatermark(watermark);
                column.setDistinctCount(rs.getLong("distinct_count"));
                column.setNotNullCount(rs.getLong("nonnull_count"));
                if (rowCount != null && rowCount != 0L) {
                    double nonnullPercentage = rs.getLong("nonnull_count") * 1.0 / rowCount;
                    BigDecimal bigDecimal = new BigDecimal(nonnullPercentage);
                    column.setNotNullPercentage(bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
                columns.add(column);
            }
            return columns;
        });
    }

    public DatasetColumn update(Long id, DatasetColumnRequest datasetColumnRequest) {
        String sql = "update kun_mt_dataset_field set description = ? where id = ?";
        jdbcTemplate.update(sql, ps -> {
            ps.setString(1, datasetColumnRequest.getDescription());
            ps.setLong(2, id);
        });
        return find(id);
    }

    public DatasetColumn find(Long id) {
        String sql = "select id, name, type, description from kun_mt_dataset_field where id = ?";

        return jdbcTemplate.query(sql, ps -> ps.setLong(1, id), rs -> {
            DatasetColumn datasetColumn = new DatasetColumn();
            if (rs.next()) {
                datasetColumn.setId(rs.getLong("id"));
                datasetColumn.setName(rs.getString("name"));
                datasetColumn.setType(rs.getString("type"));
                datasetColumn.setDescription(rs.getString("description"));
            }
            return datasetColumn;
        });
    }
}
