package com.miotech.kun.datadiscovery.persistence;

import com.google.common.collect.Lists;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfo;
import com.miotech.kun.datadiscovery.model.entity.GlossaryBasicInfoWithCount;
import com.miotech.kun.datadiscovery.util.BasicMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public static final String TABLE_ALIAS_NAME_KUN_MT_GLOSSARY = "kun_mt_glossary kmg";
    public static final String TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF = "kun_mt_glossary_to_dataset_ref";
    public static final String TABLE_ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF = "kun_mt_glossary_to_dataset_ref kmgtdr";
    public static final String ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF = "kmgtdr";
    public static final String ALIAS_KUN_MT_GLOSSARY = "kmg";
    public static final String COLUMN_KUN_MT_GLOSSARY = "id,name,description,parent_id,create_user,create_time,update_user,update_time,prev_id,deleted";

    public static OffsetDateTime now() {
        return DateTimeUtils.now();
    }

    public List<GlossaryBasicInfo> getGlossariesByDataset(Long datasetGid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmg.id as id", "kmg.name as name")
                .from("kun_mt_glossary as kmg")
                .join("inner", "kun_mt_glossary_to_dataset_ref", "kmgtdr")
                .on("kmgtdr.glossary_id = kmg.id")
                .where("kmgtdr.dataset_id = ? and kmg.deleted=false and kmgtdr.deleted=false")
                .orderBy("kmg.create_time desc")
                .getSQL();
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, datasetGid);

    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateGraph(String currentUsername, Long id, GlossaryGraphRequest glossaryGraphRequest) {
        OffsetDateTime now = now();
        String sql1 = DefaultSQLBuilder.newBuilder()
                .update(TABLE_ALIAS_NAME_KUN_MT_GLOSSARY)
                .set("update_user=?", "update_time=?", "prev_id = temp.prev_id")
                .from("(select prev_id from kun_mt_glossary where id = ? and deleted=false) temp")
                .where("kmg.prev_id = ? and kmg.deleted=false").getSQL();
        jdbcTemplate.update(sql1, currentUsername, now, id, id);

        String selectSql;
        Long nextId = null;
        Long parentId = findGlossaryBaseInfo(id).getParentId();
        if (IdUtils.isEmpty(glossaryGraphRequest.getPrevId())) {
            if (IdUtils.isEmpty(parentId)) {
                selectSql = DefaultSQLBuilder.newBuilder()
                        .select("id")
                        .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY)
                        .where("prev_id is null and parent_id is null and deleted=false")
                        .getSQL();
                nextId = jdbcTemplate.queryForObject(selectSql, Long.class);
            } else {
                selectSql = DefaultSQLBuilder.newBuilder()
                        .select("id")
                        .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY)
                        .where("prev_id is null and parent_id = ? and deleted=false")
                        .getSQL();
                nextId = jdbcTemplate.queryForObject(selectSql, Long.class, parentId);
            }
        } else {
            selectSql = DefaultSQLBuilder.newBuilder()
                    .select("id")
                    .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY)
                    .where("prev_id = ? and deleted=false")
                    .getSQL();
            try {
                nextId = jdbcTemplate.queryForObject(selectSql, Long.class, glossaryGraphRequest.getPrevId());
            } catch (Exception ignore) {
            }
        }


        String sql2 = DefaultSQLBuilder.newBuilder()
                .update(TABLE_ALIAS_NAME_KUN_MT_GLOSSARY)
                .set("update_user=?", "update_time=?", "prev_id = ?")
                .where("id = ? and deleted=false")
                .getSQL();
        jdbcTemplate.update(sql2, currentUsername, now, glossaryGraphRequest.getPrevId(), id);

        if (IdUtils.isNotEmpty(nextId)) {
            String sql3 = DefaultSQLBuilder.newBuilder()
                    .update(TABLE_ALIAS_NAME_KUN_MT_GLOSSARY)
                    .set("update_user=?", "update_time=?", "prev_id = ?").where("id = ? and deleted=false").getSQL();
            jdbcTemplate.update(sql3, currentUsername, now, id, nextId);
        }
        return id;
    }

    public Long getParentId(Long id) {
        String sql = DefaultSQLBuilder.newBuilder().select("parent_id").from(TABLE_NAME_KUN_MT_GLOSSARY).where("id=? and deleted=false").getSQL();
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
        log.debug("glossaryRequest:{}", glossaryRequest);
        String kmgSql = "insert into kun_mt_glossary values " + toValuesSql(1, 10);
        Long glossaryId = IdGenerator.getInstance().nextId();
        String createUser = glossaryRequest.getCreateUser();
        OffsetDateTime createTime = glossaryRequest.getCreateTime();
        String updateOrderSql;
        if (IdUtils.isNotEmpty(glossaryRequest.getParentId())) {
            updateOrderSql = DefaultSQLBuilder.newBuilder()
                    .update(TABLE_NAME_KUN_MT_GLOSSARY)
                    .set("update_user=?", "update_time=?", "prev_id = ?")
                    .where("parent_id = ? and prev_id is null and deleted=false")
                    .getSQL();
            jdbcTemplate.update(updateOrderSql, createUser, createTime, glossaryId, glossaryRequest.getParentId());
        } else {
            updateOrderSql = DefaultSQLBuilder.newBuilder()
                    .update(TABLE_NAME_KUN_MT_GLOSSARY)
                    .set("prev_id = ?")
                    .where("parent_id is null and prev_id is null and deleted=false")
                    .getSQL();
            jdbcTemplate.update(updateOrderSql, glossaryId);
        }

        jdbcTemplate.update(kmgSql,
                glossaryId,
                StringUtils.stripToEmpty(glossaryRequest.getName()),
                StringUtils.stripToEmpty(glossaryRequest.getDescription()),
                glossaryRequest.getParentId(),
                createUser,
                createTime,
                glossaryRequest.getUpdateUser(),
                glossaryRequest.getUpdateTime(),
                null,
                false
        );

        if (!CollectionUtils.isEmpty(glossaryRequest.getAssetIds())) {
            insertGlossaryDatasetRef(glossaryRequest.getAssetIds(), glossaryId, glossaryRequest.getUpdateUser(), glossaryRequest.getUpdateTime());
        }
        return glossaryId;
    }

    public void insertGlossaryDatasetRef(Collection<Long> assetIds, Long glossaryId, String updateUser, OffsetDateTime updateTime) {
        if (CollectionUtils.isEmpty(assetIds)) {
            return;
        }
        String kmgtdrSql = "insert into kun_mt_glossary_to_dataset_ref values " + toValuesSql(1, 6);
        assetIds.forEach(assetId -> jdbcTemplate.update(kmgtdrSql, IdGenerator.getInstance().nextId(), glossaryId, assetId, updateUser, updateTime, false));
    }

    public List<GlossaryBasicInfo> findChildren(Long parentId) {
        String tmpTableName = "tmp";
        List<Object> paramsList = Lists.newArrayList();
        SQLBuilder optionSqlBuilder = DefaultSQLBuilder.newBuilder().select(COLUMN_KUN_MT_GLOSSARY)
                .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY);
        if (Objects.isNull(parentId)) {
            optionSqlBuilder.where("parent_id is null and prev_id is null and deleted=false");
        } else {
            optionSqlBuilder.where("parent_id =? and prev_id  is null and deleted=false");
            paramsList.add(parentId);
        }
        String optionSql = optionSqlBuilder.getSQL();
        String withSql = DefaultSQLBuilder.newBuilder()
                .select("kmg1.id,kmg1.name,kmg1.description,kmg1.parent_id,kmg1.create_user,kmg1.create_time,kmg1.update_user,kmg1.update_time,kmg1.prev_id,kmg1.deleted")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kmg1").join(tmpTableName, tmpTableName).on("kmg1.prev_id =tmp.id").where("kmg1.deleted=false").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(COLUMN_KUN_MT_GLOSSARY).from(tmpTableName).getSQL();
        String sql = withRecursiveSql("tmp", COLUMN_KUN_MT_GLOSSARY, optionSql, withSql, outSql).toString();
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, paramsList.toArray());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, GlossaryRequest glossaryRequest) {
        GlossaryBasicInfo glossaryBaseInfo = findGlossaryBaseInfo(id);
        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(StringUtils.stripToEmpty(glossaryRequest.getName()));
        sqlParams.add(StringUtils.stripToEmpty(glossaryRequest.getDescription()));
        sqlParams.add(glossaryRequest.getUpdateUser());
        sqlParams.add(glossaryRequest.getUpdateTime());
        sqlParams.add(id);
        String basicUpdateSql = DefaultSQLBuilder.newBuilder()
                .update(TABLE_NAME_KUN_MT_GLOSSARY)
                .set("name=?", "description=?", "update_user=?", "update_time=?")
                .where("id = ? and deleted=false").getSQL();
        jdbcTemplate.update(basicUpdateSql, sqlParams.toArray());
        updateAssets(id, glossaryRequest.getAssetIds(), glossaryRequest.getUpdateUser(), glossaryRequest.getUpdateTime());
        move(glossaryBaseInfo, glossaryRequest.getParentId(), glossaryRequest.getUpdateUser(), glossaryRequest.getUpdateTime());
    }

    public void move(GlossaryBasicInfo glossaryBasicInfo, Long newParentId, String updateUser, OffsetDateTime updateTime) {
        Long oldParentId = glossaryBasicInfo.getParentId();
        Long id = glossaryBasicInfo.getId();
        Long basicInfoPrevId = glossaryBasicInfo.getPrevId();
        if (String.valueOf(newParentId).equals(String.valueOf(oldParentId))) {
            return;
        }
        if (isSelfDescendants(id, newParentId)) {
            throw new IllegalArgumentException(String.format("new parent id  should not be id or  Descendants id,parent id:%s,id:%s", newParentId, id));
        }

        String updateSql;
//        更新老parent节点下的child list
//         首节点:查找到该节点的下一个节点 prev_id 置为 null
//         中间节点 :将后面节点的 prev_id 更新成当前节点的 prev_id
//         尾节点不用处理
        updateSql = DefaultSQLBuilder.newBuilder().update(TABLE_NAME_KUN_MT_GLOSSARY).set("update_user=?", "update_time=?", "prev_id=?").where("prev_id = ? and deleted=false").getSQL();
        jdbcTemplate.update(updateSql, updateUser, updateTime, basicInfoPrevId, id);

//          更新新parent下的首节点为第二个节点
        if (IdUtils.isNotEmpty(newParentId)) {
            updateSql = DefaultSQLBuilder.newBuilder().update(TABLE_NAME_KUN_MT_GLOSSARY).set("update_user=?", "update_time=?", "prev_id=?").where("parent_id = ? and prev_id is null and deleted=false").getSQL();
            jdbcTemplate.update(updateSql, updateUser, updateTime, id, newParentId);
        } else {
            updateSql = DefaultSQLBuilder.newBuilder().update(TABLE_NAME_KUN_MT_GLOSSARY).set("update_user=?", "update_time=?", "prev_id=?").where("parent_id is null and prev_id is null and deleted=false").getSQL();
            jdbcTemplate.update(updateSql, updateUser, updateTime, id);
        }
//        更新节点 parent and prev_id =null
        updateSql = DefaultSQLBuilder.newBuilder().update(TABLE_NAME_KUN_MT_GLOSSARY).set("update_user=?", "update_time=?", "parent_id=?", "prev_id=null").where("id=? and deleted=false").getSQL();
        jdbcTemplate.update(updateSql, updateUser, updateTime, newParentId, id);


    }

    public boolean isSelfDescendants(Long id, Long newParentId) {
        if (Objects.isNull(newParentId)) {
            return false;
        }
        return findSelfDescendants(id).stream().map(GlossaryBasicInfo::getId).anyMatch(newParentId::equals);
    }

    /**
     * return descendants contains self
     *
     * @param id
     * @return
     */
    public List<GlossaryBasicInfo> findSelfDescendants(Long id) {

        String tmpTableName = "tmp";
        String tmpColumnList = "id,name ,parent_id ,prev_id,depth";

        String optionsSql = DefaultSQLBuilder.newBuilder().select("id,name ,parent_id ,prev_id ,0 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY).where("id=? and deleted=false").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("wkmg.id,wkmg.name ,wkmg.parent_id,wkmg.prev_id ,depth+1 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "wkmg")
                .join("inner", tmpTableName, "t")
                .on("wkmg.parent_id=t.id")
                .where("wkmg.deleted=false")
                .getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList).from(tmpTableName).orderBy("depth asc").getSQL();
        String sql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        List<GlossaryBasicInfo> query = jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, id);
        if (CollectionUtils.isEmpty(query)) {
            return Lists.newArrayList();
        }
        return query;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAssets(Long id, List<Long> assetIds, String updateUser, OffsetDateTime updateTime) {
        List<Long> oldList = findGlossaryToDataSetIdList(id);
        Collection<Long> intersection = CollectionUtils.intersection(assetIds, oldList);
        Collection<Long> subtractRemove = CollectionUtils.subtract(oldList, intersection);
        removeGlossaryRef(id, Lists.newArrayList(subtractRemove), updateUser, updateTime);
        Collection<Long> subtractAdd = CollectionUtils.subtract(assetIds, intersection);
        if (CollectionUtils.isNotEmpty(subtractAdd)) {
            insertGlossaryDatasetRef(subtractAdd, id, updateUser, updateTime);
        }
    }


    public List<Long> delete(String currentUserName, Long id) {
        OffsetDateTime now = now();
        updatePrevId(id, currentUserName, now);
        return deleteRecursively(id, currentUserName, now);
    }

    private void updatePrevId(Long id, String updateUser, OffsetDateTime updateTime) {
        GlossaryBasicInfo nextGlossary = findNextGlossaryBaseInfo(id);
        if (Objects.isNull(nextGlossary)) {
            return;
        }
        Long prevId = findGlossaryBaseInfo(id).getPrevId();
        String updatePrevIdSql = "update kun_mt_glossary set update_user=?,update_time=? ,prev_id = ? where id = ?";
        jdbcTemplate.update(updatePrevIdSql, updateUser, updateTime, prevId, nextGlossary.getId());
    }

    public List<Long> deleteRecursively(Long glossaryId, String currentUserName, OffsetDateTime updateTime) {
        List<Long> ids = findSelfDescendants(glossaryId).stream().map(GlossaryBasicInfo::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ids)) {
            removeGlossary(ids, currentUserName, updateTime);
            removeGlossaryRef(ids, currentUserName, updateTime);
        }
        return ids;
    }

    private void removeGlossaryRef(List<Long> glossaryIdList, String currentUserName, OffsetDateTime updateTime) {
        String unbindSQL = DefaultSQLBuilder.newBuilder().update(TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .set("update_user=?", "update_time=?", "deleted=true ").where("glossary_id in " + collectionToConditionSql(glossaryIdList)).getSQL();
        List<Object> paramList = Lists.newArrayList();
        paramList.add(currentUserName);
        paramList.add(updateTime);
        paramList.addAll(glossaryIdList);
        jdbcTemplate.update(unbindSQL, paramList.toArray());
    }

    private void removeGlossaryRef(Long glossaryId, Collection<Long> datasetIdList, String updateUser, OffsetDateTime updateTime) {
        if (CollectionUtils.isEmpty(datasetIdList)) {
            return;
        }
        List<Object> paramList = Lists.newArrayList();
        paramList.add(updateUser);
        paramList.add(updateTime);
        paramList.add(glossaryId);
        paramList.addAll(datasetIdList);
        String unbindSQL = DefaultSQLBuilder.newBuilder().update(TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .set("update_user=?", "update_time=?", "deleted=true").where("glossary_id= ? and dataset_id in " + collectionToConditionSql(datasetIdList)).getSQL();

        jdbcTemplate.update(unbindSQL, paramList.toArray());
    }

    private void removeGlossary(List<Long> glossaryIds, String currentUserName, OffsetDateTime updateTime) {
        List<Object> paramList = Lists.newArrayList();
        String sql = DefaultSQLBuilder.newBuilder().update(TABLE_NAME_KUN_MT_GLOSSARY)
                .set("update_user=?", "update_time=?", "deleted=true").where("id in " + collectionToConditionSql(glossaryIds)).getSQL();
        paramList.add(currentUserName);
        paramList.add(updateTime);
        paramList.addAll(glossaryIds);
        jdbcTemplate.update(sql, paramList.toArray());
    }


    public GlossaryBasicInfo findGlossaryBaseInfo(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMN_KUN_MT_GLOSSARY).
                from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY)
                .where("id=?").getSQL();
        GlossaryBasicInfo glossaryBasicInfo = null;
        glossaryBasicInfo = jdbcTemplate.queryForObject(sql, GlossaryMapper.GLOSSARY_MAPPER, id);
        if (Objects.requireNonNull(glossaryBasicInfo).isDeleted()) {
            throw new RuntimeException(String.format("glossary  deleted,id:%s", id));
        }
        return glossaryBasicInfo;

    }

    public GlossaryBasicInfo findNextGlossaryBaseInfo(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMN_KUN_MT_GLOSSARY).
                from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY)
                .where("prev_id = ? and deleted=false").getSQL();
        GlossaryBasicInfo glossaryBasicInfo = null;
        try {
            glossaryBasicInfo = jdbcTemplate.queryForObject(sql, GlossaryMapper.GLOSSARY_MAPPER, id);
        } catch (DataAccessException e) {
            log.info("glossary is last node");
            return null;
        }
        return glossaryBasicInfo;
    }


    public List<Long> findGlossaryToDataSetIdList(Long glossaryId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_id").
                from(TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF, ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .where("glossary_id=? and deleted=false").getSQL();
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
                .select("kmg1.id,kmg1.name ,kmg1.parent_id ,kmg1.prev_id,kmg1.description",
                        getChildCountColumnSql("kmg1", childCountName), getDataSetCountColumnSql("kmg1", datasetCountName))
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kmg1");
        if (Objects.isNull(parentId)) {
            optionSqlBuilder.where("parent_id is null and prev_id is null and deleted=false");

        } else {
            optionSqlBuilder.where("parent_id=? and prev_id is null and deleted=false");
            paramsList.add(parentId);
        }
        String optionSql = optionSqlBuilder.getSQL();
        String withSql = DefaultSQLBuilder.newBuilder()
                .select("kmg2.id,kmg2.name ,kmg2.parent_id ,kmg2.prev_id,kmg2.description",
                        getChildCountColumnSql("kmg2", childCountName), getDataSetCountColumnSql("kmg2", datasetCountName))
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kmg2")
                .join(tmpTableName, tmpTableName)
                .on(" kmg2.prev_id =t0.id ").where("deleted=false").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList).from(tmpTableName).getSQL();
        String sql = withRecursiveSql(tmpTableName, tmpColumnList, optionSql, withSql, outSql).toString();
        log.debug("findChildrenCountList-sqlTempLate:{}", sql);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            GlossaryBasicInfoWithCount glossaryBasicInfoWithCount = new GlossaryBasicInfoWithCount();
            glossaryBasicInfoWithCount.setId(BasicMapper.zeroToNull(rs, "id", Long.class));
            glossaryBasicInfoWithCount.setName(rs.getString("name"));
            glossaryBasicInfoWithCount.setPrevId(BasicMapper.zeroToNull(rs, "prev_id", Long.class));
            glossaryBasicInfoWithCount.setChildrenCount(rs.getInt(childCountName));
            glossaryBasicInfoWithCount.setParentId(BasicMapper.zeroToNull(rs, "parent_id", Long.class));
            glossaryBasicInfoWithCount.setDescription(rs.getString("description"));
            glossaryBasicInfoWithCount.setDataSetCount(rs.getInt(datasetCountName));
            return glossaryBasicInfoWithCount;
        }, paramsList.toArray());

    }

    private static String getDataSetCountColumnSql(String linkTableAlias, String columnAlias) {
        String tmpTableName = "t";
        String tmpColumnList = "id,parent_id";
        String optionsSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList)
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt1").where("kt1.id =%s.id and kt1.deleted=false").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("kt2.id,kt2.parent_id")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt2").join(tmpTableName, tmpTableName).on("kt2.parent_id  =t.id and kt2.deleted=false").getSQL();
        String outSql = DefaultSQLBuilder.newBuilder()
                .select("count(distinct kmgtdr.dataset_id) as dataset_count")
                .from(tmpTableName).join("inner", TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF, ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .on("t.id=kmgtdr.glossary_id and kmgtdr.deleted is false").getSQL();
        String withRecursiveSql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        String sql = String.format("(" + withRecursiveSql + ")" + "::int" + " as %s", linkTableAlias, columnAlias);
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        return sql;

    }


    private static String getChildCountColumnSql(String linkTableAlias, String columnAlias) {
        String tmpTableName = "t";
        String tmpColumnList = "id,parent_id,prev_id";
        String optionsSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList)
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt1").where("kt1.parent_id  =%s.id and prev_id is null and deleted=false").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("kt2.id,kt2.parent_id,kt2.prev_id")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "kt2").join(tmpTableName, tmpTableName).on("kt2.prev_id  =t.id").where("kt2.deleted=false").getSQL();
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
                .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY).where("id=? and deleted=false").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("wkmg.id,wkmg.name ,wkmg.parent_id,wkmg.prev_id ,depth+1 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "wkmg")
                .join("inner", tmpTableName, "t")
                .on("wkmg.id=t.parent_id")
                .where("wkmg.deleted=false")
                .getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList).from(tmpTableName).where("depth>0").orderBy("depth desc").getSQL();
        String sql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, id);
    }

    public List<GlossaryBasicInfo> findAncestryList(Collection<Long> collectionId) {
        if (CollectionUtils.isEmpty(collectionId)) {
            return Lists.newArrayList();
        }
        String tmpTableName = "tmp";
        String tmpColumnList = "id,name ,parent_id ,prev_id,depth";

        String optionsSql = DefaultSQLBuilder.newBuilder().select("id,name ,parent_id ,prev_id ,0 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, ALIAS_KUN_MT_GLOSSARY).where("id in " + collectionToConditionSql(collectionId) + " and deleted=false").getSQL();
        String withSql = DefaultSQLBuilder.newBuilder().select("wkmg.id,wkmg.name ,wkmg.parent_id,wkmg.prev_id ,depth+1 as depth ")
                .from(TABLE_NAME_KUN_MT_GLOSSARY, "wkmg")
                .join("inner", tmpTableName, "t")
                .on("wkmg.id=t.parent_id")
                .where("wkmg.deleted=false")
                .getSQL();
        String outSql = DefaultSQLBuilder.newBuilder().select(tmpColumnList).from(tmpTableName).getSQL();
        String sql = withRecursiveSql(tmpTableName, tmpColumnList, optionsSql, withSql, outSql).toString();
        log.debug("findAncestryGlossaryList-sqlTemplate:{}", sql);
        return jdbcTemplate.query(sql, GlossaryMapper.GLOSSARY_MAPPER, collectionId.toArray());
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
            if (isExistColumn(rs, "deleted")) {
                glossaryBasicInfo.setDeleted(rs.getBoolean("deleted"));
            }

            return glossaryBasicInfo;
        }

    }

}
