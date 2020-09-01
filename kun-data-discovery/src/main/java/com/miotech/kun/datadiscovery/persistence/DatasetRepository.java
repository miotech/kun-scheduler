package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetRequest;
import com.miotech.kun.datadiscovery.model.bo.DatasetSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.dataquality.persistence.DataQualityRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    GlossaryRepository glossaryRepository;

    @Autowired
    TagRepository tagRepository;

    public List<Database> getAllDatabase() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("distinct database_name")
                .from("kun_mt_dataset")
                .where("database_name is not null")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            List<Database> databases = new ArrayList<>();
            while (rs.next()) {
                Database database = new Database();
                database.setName(rs.getString("database_name"));
                databases.add(database);
            }
            return databases;
        });
    }

    public DatasetBasicPage search(BasicSearchRequest basicSearchRequest) {
        String sql = "select kmd.gid, " +
                "kmd.name as dataset_name, " +
                "kmd.database_name as database_name, " +
                "kmdsrca.name as datasource_name " +
                "from kun_mt_dataset kmd\n" +
                "inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n";

        String whereClause = "where upper(kmd.name) like ?\n";
        sql += whereClause;

        String orderClause = "order by similarity(kmd.name, ?) desc, kmd.name asc\n";
        sql += orderClause;

        String limitSql = toLimitSql(1, basicSearchRequest.getPageSize());
        sql += limitSql;

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasicPage page = new DatasetBasicPage();
            while (rs.next()) {
                DatasetBasic basic = new DatasetBasic();
                basic.setGid(rs.getLong("gid"));
                basic.setName(rs.getString("dataset_name"));
                basic.setDatabase(rs.getString("database_name"));
                basic.setDatasource(rs.getString("datasource_name"));
                page.add(basic);
            }
            return page;
        }, toLikeSql(basicSearchRequest.getKeyword().toUpperCase()), basicSearchRequest.getKeyword());
    }

    public DatasetBasicPage search(DatasetSearchRequest datasetSearchRequest) {

        StringBuilder whereClause = new StringBuilder("where 1=1").append("\n");
        List<Object> pstmtArgs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(datasetSearchRequest.getDsIdList()) || CollectionUtils.isNotEmpty(datasetSearchRequest.getDsTypeList())) {
            if (CollectionUtils.isNotEmpty(datasetSearchRequest.getDsIdList())) {
                whereClause.append("and kmdsrc.id in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getDsIdList())).append("\n");
            }
            if (CollectionUtils.isNotEmpty(datasetSearchRequest.getDsTypeList())) {
                whereClause.append("and kmdsrc.type_id in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getDsTypeList())).append("\n");
            }
        }
        if (CollectionUtils.isNotEmpty(datasetSearchRequest.getOwnerList())) {
            whereClause.append("and owner in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getOwnerList())).append("\n");
        }
        if (CollectionUtils.isNotEmpty(datasetSearchRequest.getTagList())) {
            whereClause.append("and tag in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getTagList())).append("\n");
        }
        if (CollectionUtils.isNotEmpty(datasetSearchRequest.getGlossaryIdList())) {
            whereClause.append("and kmgtdr.glossary_id in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getGlossaryIdList())).append("\n");
        }
        if (CollectionUtils.isNotEmpty(datasetSearchRequest.getDbList())) {
            whereClause.append("and database_name in ").append(collectionToConditionSql(pstmtArgs, datasetSearchRequest.getDbList())).append("\n");
        }
        if (StringUtils.isNotEmpty(datasetSearchRequest.getSearchContent())) {
            whereClause.append("and upper(kmd.name) like ?").append("\n");
            pstmtArgs.add(toLikeSql(datasetSearchRequest.getSearchContent().toUpperCase()));
        }

        String orderByClause = "order by name\n";
        if (StringUtils.isNotEmpty(datasetSearchRequest.getSortKey())
                && StringUtils.isNotEmpty(datasetSearchRequest.getSortOrder())) {
            orderByClause = "order by " + datasetSearchRequest.getSortKey() + " " + datasetSearchRequest.getSortOrder() + "\n";
        }

        String limitSql = toLimitSql(datasetSearchRequest.getPageNumber(), datasetSearchRequest.getPageSize());

        String sql = "select kmd.*, " +
                "kmdsrct.name as type, " +
                "kmdsrca.name as datasource_name, " +
                "kmda.description as dataset_description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "string_agg(distinct(kmdt.tag), ',') as tags, " +
                "string_agg(concat_ws('_', kmg.id, kmg.name), ',') as glossaries\n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n" +
                "         inner join kun_mt_datasource_type kmdsrct on kmdsrct.id = kmdsrc.type_id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         left join kun_mt_glossary_to_dataset_ref kmgtdr on kmd.gid = kmgtdr.dataset_id\n" +
                "         left join kun_mt_glossary kmg on kmgtdr.glossary_id = kmg.id\n";

        String groupClause = "group by kmd.gid, kmd.name, kmd.datasource_id, kmd.schema, kmd.data_store, kmd.database_name, type, datasource_name, dataset_description\n";
        sql += whereClause;
        sql += groupClause;

        String countSql = "select count(1) as total_count from (" + sql + ") as result";
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, pstmtArgs.toArray());

        sql += orderByClause;
        sql += limitSql;

        DatasetBasicPage pageResult = new DatasetBasicPage();
        pageResult.setPageNumber(datasetSearchRequest.getPageNumber());
        pageResult.setPageSize(datasetSearchRequest.getPageSize());
        pageResult.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                DatasetBasic datasetBasic = new DatasetBasic();
                setDatasetBasicField(datasetBasic, rs);
                if (StringUtils.isNotEmpty(rs.getString("glossaries"))) {
                    List<GlossaryBasic> glossaryBasics = sqlToList(rs.getString("glossaries")).stream().map(glossary -> {
                        GlossaryBasic glossaryBasic = new GlossaryBasic();
                        String[] fields = glossary.split("_");
                        glossaryBasic.setId(Long.valueOf(fields[0]));
                        glossaryBasic.setName(fields[1]);
                        return glossaryBasic;
                    }).collect(Collectors.toList());
                    datasetBasic.setGlossaries(glossaryBasics);
                }
                pageResult.add(datasetBasic);
            }
            return pageResult;
        }, pstmtArgs.toArray());
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

    public Dataset find(Long gid) {
        String sql = "select kmd.*, " +
                "kmdsrct.name as type, " +
                "kmdsrca.name as datasource_name, " +
                "kmda.description as dataset_description, " +
                "string_agg(distinct(kmdo.owner), ',') as owners, " +
                "string_agg(distinct(kmdt.tag), ',') as tags, " +
                "string_agg(distinct(kdcad.case_id::text), ',') as dq_case_ids \n" +
                "from kun_mt_dataset kmd\n" +
                "         inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "         inner join kun_mt_datasource_type kmdsrct on kmdsrct.id = kmdsrc.type_id\n" +
                "         inner join kun_mt_datasource_attrs kmdsrca on kmdsrca.datasource_id = kmdsrc.id\n" +
                "         left join kun_mt_dataset_attrs kmda on kmd.gid = kmda.dataset_gid\n" +
                "         left join kun_mt_dataset_owners kmdo on kmd.gid = kmdo.dataset_gid\n" +
                "         left join kun_mt_dataset_tags kmdt on kmd.gid = kmdt.dataset_gid\n" +
                "         left join kun_dq_case_associated_dataset kdcad on kmd.gid = kdcad.dataset_id\n";

        String whereClause = "where kmd.gid = ?\n";
        String groupByClause = "group by kmd.gid, type, datasource_name, dataset_description";
        return jdbcTemplate.query(sql + whereClause + groupByClause, rs -> {
            Dataset dataset = new Dataset();
            if (rs.next()) {
                setDatasetBasicField(dataset, rs);
                Watermark watermark = new Watermark();
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

        return jdbcTemplate.query(sql, rs -> {
            Long rowCount = null;
            if (rs.next()) {
                rowCount = rs.getLong("row_count");
            }
            return rowCount;
        }, gid);
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
        datasetBasic.setHighWatermark(watermark);
    }

    private List<GlossaryBasic> getGlossaryBasics(List<Long> glossaryIds) {
        GlossaryBasicSearchRequest basicSearchRequest = new GlossaryBasicSearchRequest();
        basicSearchRequest.setPageNumber(1);
        basicSearchRequest.setPageSize(Integer.MAX_VALUE);
        basicSearchRequest.setGlossaryIds(glossaryIds);
        return glossaryRepository.search(basicSearchRequest).getGlossaries();
    }

}
