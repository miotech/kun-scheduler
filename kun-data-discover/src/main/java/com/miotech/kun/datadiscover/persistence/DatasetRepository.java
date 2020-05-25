package com.miotech.kun.datadiscover.persistence;

import com.miotech.kun.datadiscover.common.util.DateUtil;
import com.miotech.kun.datadiscover.model.bo.DatasetRequest;
import com.miotech.kun.datadiscover.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Dataset;
import com.miotech.kun.datadiscover.model.entity.DatasetBasic;
import com.miotech.kun.datadiscover.model.entity.DatasetBasicPage;
import com.miotech.kun.datadiscover.model.entity.Watermark;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Repository
public class DatasetRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TagRepository tagRepository;

    public DatasetBasicPage search(DatasetSearchRequest datasetSearchRequest) {
        StringBuilder whereClause = new StringBuilder("where 1=1 and kmd.gid is not null").append("\n");
        StringBuilder typeClause = new StringBuilder();
        StringBuilder ownerClause = new StringBuilder();
        StringBuilder tagClause = new StringBuilder();
        List<Object> pstmtArgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(datasetSearchRequest.getDbTypeList())) {
            typeClause.append("and kmdsrct.name in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getDbTypeList())).append("\n");
        }
        if (!CollectionUtils.isEmpty(datasetSearchRequest.getOwnerList())) {
            ownerClause.append("and owner in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getOwnerList())).append("\n");
        }
        if (!CollectionUtils.isEmpty(datasetSearchRequest.getTagList())) {
            tagClause.append("and tag in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getTagList())).append("\n");
        }
        if (StringUtils.isNotEmpty(datasetSearchRequest.getSearchContent())) {
            whereClause.append("and (upper(kmd.name) like ?\n")
                    .append("or upper(description) like ?")
                    .append(")")
                    .append("\n");
            pstmtArgs.add(toLikeSql(datasetSearchRequest.getSearchContent().toUpperCase()));
            pstmtArgs.add(toLikeSql(datasetSearchRequest.getSearchContent().toUpperCase()));
        }
        if (datasetSearchRequest.getWatermarkStart() != null) {
            whereClause.append("and kmds.stats_date >= ").append("?").append("\n");
            pstmtArgs.add(DateUtil.millisToLocalDateTime(datasetSearchRequest.getWatermarkStart()));
        }
        if (datasetSearchRequest.getWatermarkEnd() != null) {
            whereClause.append("and kmds.stats_date <= ").append("?").append("\n");
            pstmtArgs.add(DateUtil.millisToLocalDateTime(datasetSearchRequest.getWatermarkEnd()));
        }

        String sql = "select kmd.*, " +
                "kmdsrct.name as type, " +
                "kmdsrca.name as db_name, " +
                "kmda.description as description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "kmds.stats_date as high_watermark, " +
                "string_agg(distinct(kmdt.tag), ',') as tags\n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n" +
                "         inner join kun_mt_datasource_type kmdsrct on kmdsrct.id = kmdsrc.type_id\n" + typeClause +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" + ownerClause +
                "         left join kun_mt_dataset_stats kmds on kmd.gid = kmds.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" + tagClause +
                "         right join (select dataset_gid, max(stats_date) as max_time from kun_mt_dataset_stats group by dataset_gid) watermark on (kmd.gid = watermark.dataset_gid and kmds.stats_date = watermark.max_time) or kmds.stats_date is null\n" +
                whereClause +
                "group by kmd.gid, type, db_name, description, high_watermark\n";

        String countSql = "select count(1) as total_count from (" + sql + ") as result";

        String orderByClause = "order by kmd.name\n";
        String limitSql = pageInfoToSql(datasetSearchRequest.getPageNumber(), datasetSearchRequest.getPageSize());
        DatasetBasicPage pageResult = new DatasetBasicPage();
        Long totalCount = jdbcTemplate.queryForObject(countSql, pstmtArgs.toArray(),  Long.class);
        pageResult.setPageNumber(datasetSearchRequest.getPageNumber());
        pageResult.setPageSize(datasetSearchRequest.getPageSize());
        pageResult.setTotalCount(totalCount);
        List<DatasetBasic> datasetBasicsResult = jdbcTemplate.query(sql + orderByClause + limitSql, pstmtArgs.toArray(), rs -> {
            List<DatasetBasic> datasetBasics = new ArrayList<>();
            while (rs.next()) {
                DatasetBasic datasetBasic = new DatasetBasic();
                setDatasetBasicField(datasetBasic, rs);
                datasetBasics.add(datasetBasic);
            }
            return datasetBasics;
        });
        pageResult.setDatasets(datasetBasicsResult);
        return pageResult;
    }

    public Dataset find(Long gid) {
        String sql = "select kmd.*, " +
                "kmdsrct.name as type, " +
                "kmdsrca.name as db_name, " +
                "kmda.description as description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "kmds.row_count as row_count, " +
                "watermark.max_time as high_watermark, " +
                "watermark.min_time as low_watermark, " +
                "string_agg(distinct(kmdt.tag), ',') as tags\n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_type kmdsrct on kmdsrct.id = kmdsrc.type_id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrca.datasource_id = kmdsrc.id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_stats kmds on kmd.gid = kmds.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         right join (select dataset_gid, max(stats_date) as max_time, min(stats_date) as min_time from kun_mt_dataset_stats group by dataset_gid) watermark on kmd.gid = watermark.dataset_gid\n";

        String whereClause = "where kmd.gid = ?\n";
        String groupByClause = "group by kmd.gid, type, db_name, description, row_count, high_watermark, low_watermark";
        return jdbcTemplate.query(sql + whereClause + groupByClause, ps -> ps.setLong(1, gid), rs -> {
            Dataset dataset = new Dataset();
            if (rs.next()) {
                setDatasetBasicField(dataset, rs);
                Watermark watermark = new Watermark();
                watermark.setTime(DateUtil.dateTimeToMillis(rs.getObject("low_watermark", OffsetDateTime.class)));
                dataset.setLowWatermark(watermark);
                dataset.setDatabase(rs.getString("db_name"));
                dataset.setRowCount(rs.getLong("row_count"));
                return dataset;
            }
            return dataset;
        });
    }

    public Dataset update(Long gid, DatasetRequest datasetRequest) {
        String sql = "insert into kun_mt_dataset_attrs values " + toValuesSql(1, 2) + "\n" +
                "on conflict (dataset_gid)\n" +
                "do update set description = ?";
        jdbcTemplate.update(sql, ps -> {
            ps.setLong(1, gid);
            ps.setString(2, datasetRequest.getDescription());
            ps.setString(3, datasetRequest.getDescription());
        });
        tagRepository.save(datasetRequest.getTags());
        tagRepository.overwrite(gid, datasetRequest.getTags());
        return find(gid);
    }

    private void setDatasetBasicField(DatasetBasic datasetBasic, ResultSet rs) throws SQLException {
        datasetBasic.setGid(rs.getLong("gid"));
        datasetBasic.setType(rs.getString("type"));
        datasetBasic.setDatabaseName(rs.getString("db_name"));
        datasetBasic.setDescription(rs.getString("description"));
        datasetBasic.setName(rs.getString("name"));
        datasetBasic.setSchema(rs.getString("schema"));
        datasetBasic.setOwners(sqlToList(rs.getString("owners")));
        datasetBasic.setTags(sqlToList(rs.getString("tags")));
        Watermark watermark = new Watermark();
        watermark.setTime(DateUtil.dateTimeToMillis(rs.getObject("high_watermark", OffsetDateTime.class)));
        datasetBasic.setHighWatermark(watermark);
    }


}
