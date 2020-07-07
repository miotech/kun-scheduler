package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.GlossaryRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Repository
public class GlossaryRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

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

    public Glossary find(Long id, int recursionTimes, boolean needRelation) {
        String sql = "select kmg.*, kmd.gid as dataset_id, kmd.name as dataset_name\n" +
                "     from kun_mt_glossary kmg\n" +
                "         left join kun_mt_glossary_to_dataset_ref kmgtdr on kmg.id = kmgtdr.glossary_id\n" +
                "         left join kun_mt_dataset kmd on kmgtdr.dataset_id = kmd.gid\n";

        String whereClause = "where kmg.id = ?";

        return jdbcTemplate.query(sql + whereClause, ps -> ps.setLong(1, id), rs -> {
            Glossary glossary = new Glossary();
            if (rs.next()) {
                glossary.setId(rs.getLong("id"));
                glossary.setName(rs.getString("name"));
                glossary.setDescription(rs.getString("description"));
                if (recursionTimes >= 0 && rs.getLong(COLUMN_PARENT_ID) != 0) {
                    glossary.setParent(find(rs.getLong(COLUMN_PARENT_ID), recursionTimes - 1, false));
                }
                glossary.setCreateUser(rs.getString("create_user"));
                glossary.setCreateTime(timestampToMillis(rs, "create_time"));
                glossary.setUpdateUser(rs.getString("update_user"));
                glossary.setUpdateTime(timestampToMillis(rs, "update_time"));
                if (needRelation) {
                    List<Asset> assets = new ArrayList<>();
                    if (rs.getLong("dataset_id") != 0) {
                        do {
                            Asset asset = new Asset();
                            asset.setId(rs.getLong("dataset_id"));
                            asset.setType("dataset");
                            asset.setName(rs.getString("dataset_name"));
                            assets.add(asset);
                        } while (rs.next());
                    }
                    glossary.setAssets(assets);
                }
            }
            return glossary;
        });
    }

    public Glossary find(Long id) {
        return find(id, 0, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public Glossary insert(GlossaryRequest glossaryRequest) {
        String kmgSql = "insert into kun_mt_glossary values " + toValuesSql(1, 8);

        Long glossaryId = IdGenerator.getInstance().nextId();
        jdbcTemplate.update(kmgSql,
                glossaryId,
                glossaryRequest.getName(),
                glossaryRequest.getDescription(),
                glossaryRequest.getParentId(),
                glossaryRequest.getCreateUser(),
                millisToTimestamp(glossaryRequest.getCreateTime()),
                glossaryRequest.getUpdateUser(),
                millisToTimestamp(glossaryRequest.getUpdateTime()));

        if (!CollectionUtils.isEmpty(glossaryRequest.getAssetIds())) {
            String kmgtdrSql = "insert into kun_mt_glossary_to_dataset_ref values " + toValuesSql(1, 3);

            for (Long assetId : glossaryRequest.getAssetIds()) {
                jdbcTemplate.update(kmgtdrSql,
                        IdGenerator.getInstance().nextId(),
                        glossaryId,
                        assetId);
            }
        }
        return find(glossaryId);
    }

    public GlossaryChildren findChildren(Long parentId) {
        String sql = "select id, name, description " +
                "from kun_mt_glossary \n";

        String whereClause = parentId == null ? "where parent_id is null" : "where parent_id = ?";
        sql += whereClause + "\n";

        Object[] sqlParams = parentId == null ? null : new Object[]{parentId};

        String orderBySql = "order by id desc";
        sql += orderBySql;

        return jdbcTemplate.query(sql, sqlParams, rs -> {
            GlossaryChildren glossaryChildren = new GlossaryChildren();
            glossaryChildren.setParentId(parentId);
            while (rs.next()) {
                GlossaryBasic child = new GlossaryBasic();
                child.setId(rs.getLong("id"));
                child.setName(rs.getString("name"));
                child.setDescription(rs.getString("description"));
                child.setChildrenCount(getChildrenCount(child.getId()));
                glossaryChildren.add(child);
            }
            return glossaryChildren;
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Glossary update(Long id, GlossaryRequest glossaryRequest) {
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
        return find(id);
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
        batchDelete(Collections.singletonList(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        String childSql = "select id from kun_mt_glossary where parent_id in " + collectionToConditionSql(ids);

        List<Long> childIds = jdbcTemplate.query(childSql, rs -> {
            List<Long> idsTemp = new ArrayList<>();
            while (rs.next()) {
                idsTemp.add(rs.getLong("id"));
            }
            return idsTemp;
        }, ids.toArray());
        if (!CollectionUtils.isEmpty(childIds)) {
            batchDelete(childIds);
        }

        String sql = "delete from kun_mt_glossary where id in " + collectionToConditionSql(ids);
        jdbcTemplate.update(sql, ids.toArray());
    }

    public GlossaryPage search(BasicSearchRequest searchRequest) {
        String sql = "select id, name from kun_mt_glossary\n";

        String whereClause = "where upper(name) like ?\n";
        sql += whereClause;

        String orderClause = "order by name asc\n";
        sql += orderClause;

        String limitSql = toLimitSql(1, searchRequest.getPageSize());
        sql += limitSql;

        return jdbcTemplate.query(sql,
                ps -> ps.setString(1, toLikeSql(searchRequest.getKeyword().toUpperCase())),
                rs -> {
                    GlossaryPage page = new GlossaryPage();
                    while (rs.next()) {
                        GlossaryBasic basic = new GlossaryBasic();
                        basic.setId(rs.getLong("id"));
                        basic.setName(rs.getString("name"));
                        page.add(basic);
                    }
                    return page;
                });
    }
}
