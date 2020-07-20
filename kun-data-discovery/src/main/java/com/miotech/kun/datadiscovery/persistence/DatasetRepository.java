package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.Dataset;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasic;
import com.miotech.kun.datadiscovery.model.entity.DatasetBasicPage;
import com.miotech.kun.datadiscovery.model.entity.Watermark;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Repository
public class DatasetRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DataQualityRepository dataQualityRepository;

    @Autowired
    TagRepository tagRepository;

    public DatasetBasicPage search(BasicSearchRequest basicSearchRequest) {
        String sql = "select kmd.gid, kmd.name as dataset_name, kmdsrca.name as datasource_name from kun_mt_dataset kmd\n" +
                "inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n";

        String whereClause = "where upper(kmd.name) like ?\n";
        sql += whereClause;

        String orderClause = "order by kmd.name asc\n";
        sql += orderClause;

        String limitSql = toLimitSql(1, basicSearchRequest.getPageSize());
        sql += limitSql;

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasicPage page = new DatasetBasicPage();
            while (rs.next()) {
                DatasetBasic basic = new DatasetBasic();
                basic.setGid(rs.getLong("gid"));
                basic.setName(rs.getString("dataset_name"));
                basic.setDatasource(rs.getString("datasource_name"));
                page.add(basic);
            }
            return page;
        }, toLikeSql(basicSearchRequest.getKeyword().toUpperCase()));
    }

    public DatasetBasicPage search(DatasetSearchRequest datasetSearchRequest) {
        StringBuilder searchSql = new StringBuilder("select kmd.* from kun_mt_dataset kmd").append("\n");
        StringBuilder searchGroupSql = new StringBuilder("group by gid").append("\n");
        StringBuilder whereClause = new StringBuilder("where 1=1").append("\n");
        List<Object> pstmtArgs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(datasetSearchRequest.getDsIdList()) || !CollectionUtils.isEmpty(datasetSearchRequest.getDbTypeList())) {
            searchSql.append("inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id").append("\n");
            if (!CollectionUtils.isEmpty(datasetSearchRequest.getDsIdList())) {
                searchSql.append("and kmdsrc.id in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getDsIdList())).append("\n");
            }
            if (!CollectionUtils.isEmpty(datasetSearchRequest.getDbTypeList())) {
                searchSql.append("and kmdsrc.type_id in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getDbTypeList())).append("\n");
            }
        }
        if (!CollectionUtils.isEmpty(datasetSearchRequest.getOwnerList())) {
            searchSql.append("inner join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid").append("\n");
            searchSql.append("and owner in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getOwnerList())).append("\n");
        }
        if (!CollectionUtils.isEmpty(datasetSearchRequest.getTagList())) {
            searchSql.append("inner join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid").append("\n");
            searchSql.append("and tag in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getTagList())).append("\n");
        }
        if (datasetSearchRequest.getWatermarkStart() != null || datasetSearchRequest.getWatermarkEnd() != null) {
            searchSql.append("inner join (select dataset_gid, max(stats_date) as max_time from kun_mt_dataset_stats group by dataset_gid) watermark on (kmd.gid = watermark.dataset_gid)").append("\n");
            if (datasetSearchRequest.getWatermarkStart() != null) {
                searchSql.append("and watermark.max_time >= ").append("?").append("\n");
                pstmtArgs.add(millisToTimestamp(datasetSearchRequest.getWatermarkStart()));
            }
            if (datasetSearchRequest.getWatermarkEnd() != null) {
                whereClause.append("and watermark.max_time <= ").append("?").append("\n");
                pstmtArgs.add(millisToTimestamp(datasetSearchRequest.getWatermarkEnd()));
            }
        }
        if (StringUtils.isNotEmpty(datasetSearchRequest.getSearchContent())) {
            whereClause.append("and upper(name) like ?").append("\n");
            pstmtArgs.add(toLikeSql(datasetSearchRequest.getSearchContent().toUpperCase()));
        }
        searchSql.append(whereClause);
        searchSql.append(searchGroupSql);

        String countSql = "select count(1) as total_count from (" + searchSql + ") as result";
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, pstmtArgs.toArray());

        String orderByClause = "order by kmd.name\n";
        searchSql.append(orderByClause);
        String limitSql = toLimitSql(datasetSearchRequest.getPageNumber(), datasetSearchRequest.getPageSize());
        searchSql.append(limitSql);

        String sql = "select kmd.*, " +
                "kmdsrct.name as type, " +
                "kmdsrca.name as datasource_name, " +
                "kmda.description as dataset_description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "watermark.max_time as high_watermark, " +
                "string_agg(distinct(kmdt.tag), ',') as tags\n" +
                "from (" + searchSql + ") kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n" +
                "         inner join kun_mt_datasource_type kmdsrct on kmdsrct.id = kmdsrc.type_id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         left join (select dataset_gid, max(stats_date) as max_time from kun_mt_dataset_stats group by dataset_gid) watermark on (kmd.gid = watermark.dataset_gid)\n" +
                "group by kmd.gid, kmd.name, kmd.datasource_id, kmd.schema, kmd.data_store, kmd.database_name, type, datasource_name, dataset_description, high_watermark\n";

        sql += orderByClause;
        DatasetBasicPage pageResult = new DatasetBasicPage();
        pageResult.setPageNumber(datasetSearchRequest.getPageNumber());
        pageResult.setPageSize(datasetSearchRequest.getPageSize());
        pageResult.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                DatasetBasic datasetBasic = new DatasetBasic();
                setDatasetBasicField(datasetBasic, rs);
                pageResult.add(datasetBasic);
            }
            return pageResult;
        }, pstmtArgs.toArray());
    }

    public DatasetBasic findBasic(Long gid) {
        String sql = "select kmd.gid, kmd.name as dataset_name, kmdsrca.name as datasource_name from kun_mt_dataset kmd\n" +
                "inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n";

        String whereClause = "where kmd.gid = ?\n";
        sql += whereClause;

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic basic = new DatasetBasic();
            if (rs.next()) {
                basic.setGid(rs.getLong("gid"));
                basic.setName(rs.getString("dataset_name"));
                basic.setDatasource(rs.getString("datasource_name"));
            }
            return basic;
        }, gid);
    }

    public Dataset find(Long gid) {
        String sql = "select kmd.*, " +
                "kmdsrct.name as type, " +
                "kmdsrca.name as datasource_name, " +
                "kmda.description as dataset_description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "watermark.max_time as high_watermark, " +
                "watermark.min_time as low_watermark, " +
                "string_agg(distinct(kmdt.tag), ',') as tags, " +
                "string_agg(distinct(kdcad.case_id::text), ',') as dq_case_ids \n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_type kmdsrct on kmdsrct.id = kmdsrc.type_id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrca.datasource_id = kmdsrc.id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         inner join (select dataset_gid, max(stats_date) as max_time, min(stats_date) as min_time from kun_mt_dataset_stats where dataset_gid = ? group by dataset_gid) watermark on kmd.gid = watermark.dataset_gid\n" +
                "         left join kun_dq_case_associated_dataset kdcad on kmd.gid = kdcad.dataset_id\n";

        String whereClause = "where kmd.gid = ?\n";
        String groupByClause = "group by kmd.gid, type, datasource_name, dataset_description, high_watermark, low_watermark";
        return jdbcTemplate.query(sql + whereClause + groupByClause, rs -> {
            Dataset dataset = new Dataset();
            if (rs.next()) {
                setDatasetBasicField(dataset, rs);
                Watermark watermark = new Watermark();
                watermark.setTime(timestampToMillis(rs, "low_watermark"));
                dataset.setLowWatermark(watermark);
                dataset.setRowCount(getRowCount(gid));
                List<Long> dqCaseIds = sqlToList(rs.getString("dq_case_ids")).stream().map(Long::valueOf).collect(Collectors.toList());
                dataset.setDataQualities(dataQualityRepository.getCaseBasics(dqCaseIds));
                return dataset;
            }
            return dataset;
        }, gid, gid);
    }

    public Long getRowCount(Long gid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("row_count")
                .from("kun_mt_dataset_stats")
                .where("dataset_gid = ?")
                .orderBy("stats_date desc")
                .limit(1)
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class, gid);
    }

    public Dataset update(Long gid, DatasetRequest datasetRequest) {
        String sql = "insert into kun_mt_dataset_attrs values " + toValuesSql(1, 2) + "\n" +
                "on conflict (dataset_gid)\n" +
                "do update set description = ?";
        jdbcTemplate.update(sql, gid, datasetRequest.getDescription(), datasetRequest.getDescription());
        overwriteOwners(gid, datasetRequest.getOwners());
        tagRepository.save(datasetRequest.getTags());
        tagRepository.overwriteDataset(gid, datasetRequest.getTags());
        return find(gid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteOwners(Long gid, List<String> owners) {
        String sql = "delete from kun_mt_dataset_owners where dataset_gid = ?";
        jdbcTemplate.update(sql, gid);

        if (!CollectionUtils.isEmpty(owners)) {
            String addOwnerRefSql = "insert into kun_mt_dataset_owners values " + toValuesSql(owners.size(), 3);

            jdbcTemplate.update(addOwnerRefSql, ps -> {
                int paramIndex = 0;
                for (String owner : owners) {
                    ps.setLong(++paramIndex, IdGenerator.getInstance().nextId());
                    ps.setLong(++paramIndex, gid);
                    ps.setString(++paramIndex, owner);
                }
            });
        }
    }

    private void setDatasetBasicField(DatasetBasic datasetBasic, ResultSet rs) throws SQLException {
        datasetBasic.setGid(rs.getLong("gid"));
        datasetBasic.setType(rs.getString("type"));
        datasetBasic.setDatasource(rs.getString("datasource_name"));
        datasetBasic.setDescription(rs.getString("dataset_description"));
        datasetBasic.setName(rs.getString("name"));
        datasetBasic.setDatabase(rs.getString("database_name"));
        datasetBasic.setOwners(sqlToList(rs.getString("owners")));
        datasetBasic.setTags(sqlToList(rs.getString("tags")));
        Watermark watermark = new Watermark();
        watermark.setTime(timestampToMillis(rs, "high_watermark"));
        datasetBasic.setHighWatermark(watermark);
    }


}
