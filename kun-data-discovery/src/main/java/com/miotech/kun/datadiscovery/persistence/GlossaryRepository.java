package com.miotech.kun.datadiscovery.persistence;

import com.google.common.collect.Lists;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.datadiscovery.util.BasicMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Repository
@Slf4j
public class GlossaryRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public static final String TABLE_NAME_KUN_MT_GLOSSARY = "kun_mt_glossary";
    public static final String TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF = "kun_mt_glossary_to_dataset_ref";
    public static final String ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF = "kmgtdr";
    public static final String ALIAS_KUN_MT_GLOSSARY = "kmg";
    public static final String COLUMN_KUN_MT_GLOSSARY = "id,name,description,parent_id,create_user,create_time,update_user,update_time,prev_id";


    public List<GlossaryBasicInfo> getGlossariesByDataset(Long datasetGid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmg.id as id", "kmg.name as name")
                .from("kun_mt_glossary as kmg")
                .join("inner", "kun_mt_glossary_to_dataset_ref", "kmgtdr")
                .on("kmgtdr.glossary_id = kmg.id")
                .where("kmgtdr.dataset_id = ?")
                .orderBy("kmg.create_time desc")
                .getSQL();
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, datasetGid);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateGraph(Long id, GlossaryGraphRequest glossaryGraphRequest) {
        String sql1 = "update kun_mt_glossary kmg set prev_id = temp.prev_id \n" +
                "from (select prev_id from kun_mt_glossary where id = ?) temp \n" +
                "where kmg.prev_id = ?";

        jdbcTemplate.update(sql1, id, id);

        String selectSql;
        Long nextId = null;

        String parentIdSql = "select parent_id from kun_mt_glossary where id = ?";
        Long parentId = jdbcTemplate.queryForObject(parentIdSql, Long.class, id);
        if (IdUtils.isEmpty(glossaryGraphRequest.getPrevId())) {
            if (IdUtils.isEmpty(parentId)) {
                selectSql = "select id from kun_mt_glossary where prev_id is null and parent_id is null";
                nextId = jdbcTemplate.queryForObject(selectSql, Long.class);
            } else {
                selectSql = "select id from kun_mt_glossary where prev_id is null and parent_id = ?";
                nextId = jdbcTemplate.queryForObject(selectSql, Long.class, parentId);
            }
        } else {
            selectSql = "select id from kun_mt_glossary where prev_id = ?";
            try {
                nextId = jdbcTemplate.queryForObject(selectSql, Long.class, glossaryGraphRequest.getPrevId());
            } catch (Exception ignore) {
            }
        }


        String sql2 = "update kun_mt_glossary kmg set prev_id = ? \n" +
                "where kmg.id = ?";

        jdbcTemplate.update(sql2, glossaryGraphRequest.getPrevId(), id);

        if (IdUtils.isNotEmpty(nextId)) {
            String sql3 = "update kun_mt_glossary kmg set prev_id = ? \n" +
                    "where kmg.id = ?";

            jdbcTemplate.update(sql3, id, nextId);
        }
        return id;
    }

    public Long getParentId(Long id) {
        String sql = "select parent_id from kun_mt_glossary where id = ?";

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                if (rs.getLong(COLUMN_PARENT_ID) == 0) {
                    return null;
                }
                return rs.getLong(COLUMN_PARENT_ID);
            }
            return null;
        }, id);
    }


    @Transactional(rollbackFor = Exception.class)
    public Long insert(GlossaryRequest glossaryRequest) {
        String kmgSql = "insert into kun_mt_glossary values " + toValuesSql(1, 9);
        Long glossaryId = IdGenerator.getInstance().nextId();

        String updateOrderSql;
        if (IdUtils.isNotEmpty(glossaryRequest.getParentId())) {
            updateOrderSql = "update kun_mt_glossary set prev_id = ? where parent_id = ? and prev_id is null";
            jdbcTemplate.update(updateOrderSql, glossaryId, glossaryRequest.getParentId());
        } else {
            updateOrderSql = "update kun_mt_glossary set prev_id = ? where parent_id is null and prev_id is null";
            jdbcTemplate.update(updateOrderSql, glossaryId);
        }

        jdbcTemplate.update(kmgSql,
                glossaryId,
                glossaryRequest.getName(),
                glossaryRequest.getDescription(),
                glossaryRequest.getParentId(),
                glossaryRequest.getCreateUser(),
                glossaryRequest.getCreateTime(),
                glossaryRequest.getUpdateUser(),
                glossaryRequest.getUpdateTime(),
                null
        );

        if (!CollectionUtils.isEmpty(glossaryRequest.getAssetIds())) {
            insertGlossaryDatasetRef(glossaryRequest.getAssetIds(), glossaryId);
        }
        return glossaryId;
    }

    public void insertGlossaryDatasetRef(List<Long> assetIds, Long glossaryId) {
        String kmgtdrSql = "insert into kun_mt_glossary_to_dataset_ref values " + toValuesSql(1, 3);

        for (Long assetId : assetIds) {
            jdbcTemplate.update(kmgtdrSql,
                    IdGenerator.getInstance().nextId(),
                    glossaryId,
                    assetId);
        }
    }

    public List<GlossaryBasicInfo> findChildren(Long parentId) {
        String tmpTableName = "tmp";
        List<Object> paramsList = Lists.newArrayList();
        SQLBuilder optionSqlBuilder = DefaultSQLBuilder.newBuilder().select(COLUMN_KUN_MT_GLOSSARY)
                .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY);
        if (Objects.isNull(parentId)) {
            optionSqlBuilder.where("parent_id is null and prev_id is null");
        } else {
            optionSqlBuilder.where("parent_id =? and prev_id  is null");
            paramsList.add(parentId);
        }
        String optionSql = optionSqlBuilder.getSQL();
        String withSql = DefaultSQLBuilder.newBuilder()
                .select("kmg1.id,kmg1.name,kmg1.description,kmg1.parent_id,kmg1.create_user,kmg1.create_time,kmg1.update_user,kmg1.update_time,kmg1.prev_id")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kmg1").join(tmpTableName, tmpTableName).on("kmg1.prev_id =tmp.id").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(COLUMN_KUN_MT_GLOSSARY).from(tmpTableName).getSQL();
        String sql = withRecursiveSql("tmp", COLUMN_KUN_MT_GLOSSARY, optionSql, withSql, outSql).toString();
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, paramsList.toArray());
    }

    public Long findGlossaryDeepDatasetCount(Long id) {
        String sql = "select   count(dataset_id) dataset_id  from  kun_mt_glossary_to_dataset_ref kmgtdr  where glossary_id = " + id;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, GlossaryRequest glossaryRequest) {
        List<Object> sqlParams = new ArrayList<>();
        String sql = "update kun_mt_glossary set\n" +
                "name = ?, " +
                "description = ?, ";
        sqlParams.add(glossaryRequest.getName());
        sqlParams.add(glossaryRequest.getDescription());
        if (glossaryRequest.getParentId() != null) {
            sql += "parent_id = ?, ";
            sqlParams.add(glossaryRequest.getParentId());
        } else {
            sql += "parent_id = null, ";
        }

        sql += "update_user = ?, update_time = ?\n";
        sqlParams.add(glossaryRequest.getUpdateUser());
        sqlParams.add(glossaryRequest.getUpdateTime());

        String whereClause = "where id = ?";
        sql += whereClause;
        sqlParams.add(id);

        jdbcTemplate.update(sql, sqlParams.toArray());
        overwriteAssets(id, glossaryRequest.getAssetIds());

    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteAssets(Long id, List<Long> assetIds) {
        String deleteSql = "delete from kun_mt_glossary_to_dataset_ref where glossary_id = ?";
        jdbcTemplate.update(deleteSql, id);

        if (!CollectionUtils.isEmpty(assetIds)) {
            String insertSql = "insert into kun_mt_glossary_to_dataset_ref values " + toValuesSql(1, 3);
            for (Long assetId : assetIds) {
                jdbcTemplate.update(insertSql,
                        IdGenerator.getInstance().nextId(),
                        id,
                        assetId);
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        updatePrevId(id);

        deleteRecursively(Collections.singletonList(id));
    }

    private void updatePrevId(Long id) {
        String nextGlossaryIdSql = "select id from kun_mt_glossary where prev_id = ?";
        Object[] sqlParams = new Object[]{id};
        Long nextGlossaryId = jdbcTemplate.query(nextGlossaryIdSql, sqlParams, rs -> {
            Long gId = null;
            if (rs.next()) {
                gId = rs.getLong("id");
                if (rs.wasNull()) {
                    gId = null;
                }
            }

            return gId;
        });
        if (nextGlossaryId == null) {
            return;
        }

        String prevValueSql = "select prev_id from kun_mt_glossary where id = ?";
        Long prevId = jdbcTemplate.query(prevValueSql, sqlParams, rs -> {
            Long pId = null;
            if (rs.next()) {
                pId = rs.getLong("prev_id");
                if (rs.wasNull()) {
                    pId = null;
                }
            }

            return pId;
        });

        String updatePrevIdSql = "update kun_mt_glossary set prev_id = ? where id = ?";
        jdbcTemplate.update(updatePrevIdSql, ps -> {
            if (prevId == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, prevId);
            }

            ps.setLong(2, nextGlossaryId);
        });

    }

    private void deleteRecursively(List<Long> ids) {
        String childSql = "select id from kun_mt_glossary where parent_id in " + collectionToConditionSql(ids);

        List<Long> childIds = jdbcTemplate.query(childSql, rs -> {
            List<Long> idsTemp = new ArrayList<>();
            while (rs.next()) {
                idsTemp.add(rs.getLong("id"));
            }
            return idsTemp;
        }, ids.toArray());
        if (!CollectionUtils.isEmpty(childIds)) {
            deleteRecursively(childIds);
        }

        String sql = "delete from kun_mt_glossary where id in " + collectionToConditionSql(ids);
        jdbcTemplate.update(sql, ids.toArray());

        // unbind with dataset
        String unbindSQL = "delete from kun_mt_glossary_to_dataset_ref where glossary_id in " + collectionToConditionSql(ids);
        jdbcTemplate.update(unbindSQL, ids.toArray());

    }

    public GlossaryPage search(BasicSearchRequest searchRequest) {
        List<Object> sqlArgs = new ArrayList<>();
        String sql = "select id, name from kun_mt_glossary\n";
        String whereClause = wrapSql("where 1=1");

        if (StringUtils.isNotEmpty(searchRequest.getKeyword())) {
            whereClause += wrapSql("and upper(name) like ?");
            sqlArgs.add(toLikeSql(searchRequest.getKeyword().toUpperCase()));
        }

        if (searchRequest instanceof GlossaryBasicSearchRequest) {
            GlossaryBasicSearchRequest glossaryBasicSearchRequest = (GlossaryBasicSearchRequest) searchRequest;
            if (CollectionUtils.isNotEmpty(glossaryBasicSearchRequest.getGlossaryIds())) {
                whereClause += wrapSql("and id in " + collectionToConditionSql(sqlArgs, glossaryBasicSearchRequest.getGlossaryIds()));
            }
        }

        sql += whereClause;

        String orderClause = "order by name asc\n";
        sql += orderClause;

        String limitSql = toLimitSql(1, searchRequest.getPageSize());
        sql += limitSql;

        return jdbcTemplate.query(sql,
                rs -> {
                    GlossaryPage page = new GlossaryPage();
                    while (rs.next()) {
                        GlossaryBasicInfo basic = new GlossaryBasicInfo();
                        basic.setId(rs.getLong("id"));
                        basic.setName(rs.getString("name"));
                        page.add(basic);
                    }
                    return page;
                }, sqlArgs.toArray());
    }


    public GlossaryBasicInfo findGlossaryBaseInfo(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMN_KUN_MT_GLOSSARY).
                from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY)
                .where("id=?").getSQL();
        return jdbcTemplate.queryForObject(sql, GlossaryMapper.GLOSSARY_MAPPER, id);

    }

    public List<Long> findGlossaryToDataSetIdList(Long glossaryId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_id").
                from(TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF, ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .where("glossary_id=?").getSQL();
        return jdbcTemplate.queryForList(sql, Long.class, glossaryId);

    }

    public static StringJoiner withRecursiveSql(String tmpTableName, String tmpColumnList, String optionSql, String withSql, String outSql) {

        return new StringJoiner(" ").add("WITH RECURSIVE").add(tmpTableName).add("(").add(tmpColumnList).add(")")
                .add("AS").add("(").add(optionSql).add("UNION ALL").add(withSql).add(")")
                .add(outSql);
    }

    public List<GlossaryBasicInfoWithCount> findChildrenCountList(Long parentId) {
        List<Object> paramsList = new ArrayList<>();
        String tmpTableName = "t0";
        String childCountName = "child_count";
        String datasetCountName = "dataset_count";
        String columnList = "id,name ,parent_id ,prev_id,description";
        String tmpColumnList = new StringJoiner(",").add(columnList).add(childCountName).add(datasetCountName).toString();
        SQLBuilder optionSqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("kmg1.id,kmg1.name ,kmg1.parent_id ,kmg1.prev_id,kmg1.description", getChildCountColumnSql("kmg1", childCountName), getDataSetCountColumnSql("kmg1", datasetCountName))
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kmg1");
        if (Objects.isNull(parentId)) {
            optionSqlBuilder.where("parent_id is null and prev_id is null");

        } else {
            optionSqlBuilder.where("parent_id=? and prev_id is null");
            paramsList.add(parentId);
        }
        String optionSql = optionSqlBuilder.getSQL();
        String withSql = DefaultSQLBuilder.newBuilder()
                .select("kmg2.id,kmg2.name ,kmg2.parent_id ,kmg2.prev_id,kmg2.description", getChildCountColumnSql("kmg2", childCountName), getDataSetCountColumnSql("kmg2", datasetCountName))
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kmg2")
                .join(tmpTableName, tmpTableName)
                .on(" kmg2.prev_id =t0.id").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList).from(tmpTableName).getSQL();
        String sql = withRecursiveSql(tmpTableName, tmpColumnList, optionSql, withSql, outSql).toString();
        log.debug("findChildrenCountList-sqlTempLate:{}", sql);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            GlossaryBasicInfoWithCount glossaryBasicInfoWithCount = new GlossaryBasicInfoWithCount();
            glossaryBasicInfoWithCount.setId(rs.getLong("id"));
            glossaryBasicInfoWithCount.setName(rs.getString("name"));
            glossaryBasicInfoWithCount.setChildrenCount(rs.getInt(childCountName));
            glossaryBasicInfoWithCount.setParentId(rs.getLong("parent_id"));
            glossaryBasicInfoWithCount.setDescription(rs.getString("description"));
            glossaryBasicInfoWithCount.setDataSetCount(rs.getInt(datasetCountName));
            return glossaryBasicInfoWithCount;
        }, paramsList.toArray());

    }

    private static String getDataSetCountColumnSql(String linkTableAlias, String columnAlias) {
        String tmpTableName = "t";
        String tmpColumnList = "id,parent_id";
        String optionsSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList)
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt1").where("kt1.id =%s.id").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("kt2.id,kt2.parent_id")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt2").join(tmpTableName, tmpTableName).on("kt2.parent_id  =t.id").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder()
                .select("count(distinct kmgtdr.dataset_id) as dataset_count")
                .from(tmpTableName).join("inner", TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF, ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .on("t.id=kmgtdr.glossary_id").getSQL();
        String withRecursiveSql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        String sql = String.format("(" + withRecursiveSql + ")" + "::int" + " as %s", linkTableAlias, columnAlias);
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        return sql;

    }


    private static String getChildCountColumnSql(String linkTableAlias, String columnAlias) {
        String tmpTableName = "t";
        String tmpColumnList = "id,parent_id,prev_id";
        String optionsSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList)
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt1").where("kt1.parent_id  =%s.id and prev_id is null").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("kt2.id,kt2.parent_id,kt2.prev_id")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt2").join(tmpTableName, tmpTableName).on("kt2.prev_id  =t.id").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder()
                .select(" count(t.id)  as chCount")
                .from(tmpTableName).getSQL();
        String withRecursiveSql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        String sql = String.format("(" + withRecursiveSql + ")" + "::int" + " as %s", linkTableAlias, columnAlias);
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        return sql;
    }

    public List<GlossaryBasicInfo> findAncestryGlossaryList(Long id) {
        String tmpTableName = "tmp";
        String tmpColumnList = "id,name ,parent_id ,prev_id,depth";

        String optionsSql = DefaultSQLBuilder.newBuilder().select("id,name ,parent_id ,prev_id ,0 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY).where("id=?").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("wkmg.id,wkmg.name ,wkmg.parent_id,wkmg.prev_id ,depth+1 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "wkmg")
                .join("inner", tmpTableName, "t")
                .on("wkmg.id=t.parent_id").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList).from(tmpTableName).where("depth>0").orderBy("depth desc").getSQL();
        String sql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, id);
    }


    private static class GlossaryMapper extends BasicMapper<GlossaryBasicInfo> {
        public static final GlossaryMapper GLOSSARY_MAPPER = new GlossaryMapper();

        @Override
        public GlossaryBasicInfo mapRow(ResultSet rs, int rowNum) throws SQLException {

            GlossaryBasicInfo glossaryBasicInfo = new GlossaryBasicInfo();
            if (isExistColumn(rs, "id")) {
                glossaryBasicInfo.setId(zeroToNull(rs, "id", Long.class));
            }
            if (isExistColumn(rs, "name")) {
                glossaryBasicInfo.setName(rs.getString("name"));
            }
            if (isExistColumn(rs, "description")) {
                glossaryBasicInfo.setDescription(rs.getString("description"));
            }
            if (isExistColumn(rs, "parent_id")) {
                glossaryBasicInfo.setParentId(zeroToNull(rs, "parent_id", Long.class));
            }
            if (isExistColumn(rs, "prev_id")) {
                glossaryBasicInfo.setPrevId(zeroToNull(rs, "prev_id", Long.class));
            }
            if (isExistColumn(rs, "create_user")) {
                glossaryBasicInfo.setCreateUser(rs.getString("create_user"));
            }
            if (isExistColumn(rs, "create_time")) {
                glossaryBasicInfo.setCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")));
            }
            if (isExistColumn(rs, "update_user")) {
                glossaryBasicInfo.setUpdateUser(rs.getString("update_user"));
            }
            if (isExistColumn(rs, "update_time")) {
                glossaryBasicInfo.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")));
            }

            return glossaryBasicInfo;
        }

    }

}
