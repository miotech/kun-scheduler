package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryBasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryGraphRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import com.miotech.kun.metadata.core.model.vo.DatasetFieldInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
public class GlossaryRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;


    public static final String TABLE_NAME_KUN_MT_GLOSSARY = "kun_mt_glossary";
    public static final String TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF = "kun_mt_glossary_to_dataset_ref";
    public static final String ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF = "kmgtdr";
    public static final String ALIAS_KUN_MT_GLOSSARY = "kmg";
    public static final String COLUMN_KUN_MT_GLOSSARY = "id,name,description,parent_id,create_user,create_time,update_user,update_time,prev_id";


    public List<GlossaryBasic> getGlossariesByDataset(Long datasetGid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmg.id as glossary_id",
                        "kmg.name as glossary_name")
                .from("kun_mt_glossary as kmg")
                .join("inner", "kun_mt_glossary_to_dataset_ref", "kmgtdr").on("kmgtdr.glossary_id = kmg.id")
                .where("kmgtdr.dataset_id = ?")
                .orderBy("kmg.create_time desc")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            List<GlossaryBasic> glossaryBasics = new ArrayList<>();
            while (rs.next()) {
                GlossaryBasic glossaryBasic = new GlossaryBasic();
                glossaryBasic.setId(rs.getLong("glossary_id"));
                glossaryBasic.setName(rs.getString("glossary_name"));
                glossaryBasics.add(glossaryBasic);
            }
            return glossaryBasics;
        }, datasetGid);
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
                millisToTimestamp(glossaryRequest.getCreateTime()),
                glossaryRequest.getUpdateUser(),
                millisToTimestamp(glossaryRequest.getUpdateTime()),
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

    public GlossaryChildren findChildren(Long parentId) {
        String sql = "select id, name, description, prev_id \n" +
                "from kun_mt_glossary \n";

        String whereClause = parentId == null ? "where parent_id is null" : "where parent_id = ?";
        sql += whereClause + "\n";

        Object[] sqlParams = parentId == null ? null : new Object[]{parentId};

        return jdbcTemplate.query(sql, sqlParams, rs -> {
            GlossaryChildren glossaryChildren = new GlossaryChildren();
            glossaryChildren.setParentId(parentId);
            Map<Long, GlossaryBasic> idMap = new HashMap<>();
            while (rs.next()) {
                GlossaryBasic child = new GlossaryBasic();
                child.setId(rs.getLong("id"));
                child.setPrevId(rs.getLong("prev_id"));
                child.setName(rs.getString("name"));
                child.setDescription(rs.getString("description"));
                child.setChildrenCount(getChildrenCount(child.getId()));
                child.setDataSetCount(getDatasetCount(child.getId()));
                idMap.put(child.getPrevId(), child);
            }
            sortGlossary(0L, idMap, glossaryChildren);
            return glossaryChildren;
        });
    }

    private Long getDatasetCount(Long id) {
        String sql = "select   count(dataset_id) dataset_id  from  kun_mt_glossary_to_dataset_ref kmgtdr  where glossary_id = " + id;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private void sortGlossary(Long prevId,
                              Map<Long, GlossaryBasic> idMap,
                              GlossaryChildren glossaryChildren) {
        GlossaryBasic glossaryBasic = idMap.get(prevId);
        if (glossaryBasic == null) {
            return;
        }
        glossaryChildren.add(glossaryBasic);
        sortGlossary(glossaryBasic.getId(), idMap, glossaryChildren);
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
        sqlParams.add(millisToTimestamp(glossaryRequest.getUpdateTime()));

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

    private Long getChildrenCount(Long parentId) {
        String sql = "select count(1) as children_count from kun_mt_glossary where parent_id = " + parentId;

        return jdbcTemplate.queryForObject(sql, Long.class);
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
                        GlossaryBasic basic = new GlossaryBasic();
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
        GlossaryBasicInfo glossaryBasicInfo = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            GlossaryBasicInfo glossary = new GlossaryBasicInfo();
            glossary.setId(rs.getLong("id"));
            glossary.setName(rs.getString("name"));
            glossary.setDescription(rs.getString("description"));
            glossary.setParentId(rs.getLong("parent_id"));
            glossary.setCreateUser(rs.getString("create_user"));
            glossary.setCreateTime(timestampToOffsetDateTime(rs, "create_time"));
            glossary.setUpdateUser(rs.getString("update_user"));
            glossary.setUpdateTime(timestampToOffsetDateTime(rs, "update_time"));
            return glossary;
        }, id);
        return glossaryBasicInfo;

    }

    public List<Long> findGlossaryToDataSetIdList(Long glossaryId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_id").
                from(TABLE_KUN_MT_GLOSSARY_TO_DATASET_REF, ALIAS_KUN_MT_GLOSSARY_TO_DATASET_REF)
                .where("glossary_id=?").getSQL();
        return jdbcTemplate.queryForList(sql, Long.class, glossaryId);

    }

}
