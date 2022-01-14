package com.miotech.kun.datadiscovery.persistence;

import com.google.common.collect.Lists;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.NumberUtils;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasic;
import com.miotech.kun.datadiscovery.model.entity.DatasetStats;
import com.miotech.kun.datadiscovery.model.entity.LineageDatasetBasic;
import com.miotech.kun.datadiscovery.model.entity.Watermark;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Repository
public class DatasetRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    GlossaryRepository glossaryRepository;

    @Autowired
    TagRepository tagRepository;

    private static final Map<String, String> SORT_KEY_MAP = new HashMap<>();
    static {
        SORT_KEY_MAP.put("name", "name");
        SORT_KEY_MAP.put("databaseName", "database_name");
        SORT_KEY_MAP.put("datasourceName", "datasource_name");
        SORT_KEY_MAP.put("type", "type");
        SORT_KEY_MAP.put("highWatermark", "high_watermark");
    }

    public DatasetBasic findBasic(Long gid) {
        String sql = "select kmd.gid, " +
                "kmd.name as dataset_name, " +
                "kmd.database_name as database_name, " +
                "kmdsrca.name as datasource_name " +
                "from kun_mt_dataset kmd\n" +
                "inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n";

        String whereClause = "where kmd.gid = ?\n";
        sql += whereClause;

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic basic = new DatasetBasic();
            if (rs.next()) {
                basic.setGid(rs.getLong("gid"));
                basic.setName(rs.getString("dataset_name"));
                basic.setDatabase(rs.getString("database_name"));
                basic.setDatasource(rs.getString("datasource_name"));
            }
            return basic;
        }, gid);
    }

    public DatasetStats getLatestStats(Long gid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("row_count",
                        "last_updated_time")
                .from("kun_mt_dataset_stats")
                .where("dataset_gid = ?")
                .orderBy("stats_date desc")
                .limit(1)
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetStats datasetStats = new DatasetStats();
            if (rs.next()) {
                datasetStats.setRowCount(rs.getLong("row_count"));
                Watermark highWatermark = new Watermark();
                highWatermark.setTime(NumberUtils.toDouble(timestampToMillis(rs, "last_updated_time")));
                datasetStats.setHighWatermark(highWatermark);
            }
            return datasetStats;
        }, gid);
    }

    public List<LineageDatasetBasic> getDatasets(List<Long> datasetGids) {
        if (CollectionUtils.isEmpty(datasetGids)) {
            return Lists.newArrayList();
        }
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.gid as dataset_id",
                        "kmd.name as dataset_name",
                        "kmdsrca.name as datasource_name",
                        "kmdsrct.name as datasource_type",
                        "kmd.deleted as deleted")
                .from("kun_mt_dataset kmd")
                .join("inner", "kun_mt_datasource", "kmdsrc").on("kmd.datasource_id = kmdsrc.id")
                .join("inner", "kun_mt_datasource_type", "kmdsrct").on("kmdsrct.id = kmdsrc.type_id")
                .join("inner", "kun_mt_datasource_attrs", "kmdsrca").on("kmdsrca.datasource_id = kmdsrc.id")
                .where("kmd.gid in " + toColumnSql(datasetGids.size()))
                .getSQL();
        return jdbcTemplate.query(sql, rs -> {
            List<LineageDatasetBasic> datasetBasics = new ArrayList<>();
            while (rs.next()) {
                LineageDatasetBasic datasetBasic = new LineageDatasetBasic();
                datasetBasic.setGid(rs.getLong("dataset_id"));
                datasetBasic.setName(rs.getString("dataset_name"));
                datasetBasic.setDatasource(rs.getString("datasource_name"));
                datasetBasic.setType(rs.getString("datasource_type"));
                datasetBasic.setDeleted(rs.getBoolean("deleted"));
                DatasetStats datasetStats = getLatestStats(datasetBasic.getGid());
                datasetBasic.setRowCount(datasetStats.getRowCount());
                datasetBasic.setHighWatermark(datasetStats.getHighWatermark());
                datasetBasics.add(datasetBasic);
            }
            return datasetBasics;
        }, datasetGids.toArray());
    }

}
