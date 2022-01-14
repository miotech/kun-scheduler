package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.metadata.core.model.dataset.DatasetTag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@Singleton
public class TagDao {

    @Inject
    private DatabaseOperator dbOperator;

    public List<String> searchTags(String keyword) {
        String sql = "select tag from kun_mt_tag ";
        if (StringUtils.isBlank(keyword)) {
            return dbOperator.fetchAll(sql, rs -> rs.getString("tag"));
        }

        String whereClause = "where tag like concat('%', cast(? as text), '%')";
        sql = sql + whereClause;
        return dbOperator.fetchAll(sql, rs -> rs.getString("tag"), keyword);
    }

    public void save(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return;
        }

        for (String tag : tags) {
            if (StringUtils.isBlank(tag)) {
                continue;
            }

            String tagSql = "insert into kun_mt_tag values (?) "+
                    "on conflict (tag) do nothing";
            dbOperator.update(tagSql, tag);
        }
    }

    public void overwriteDatasetTags(Long gid, List<String> tags) {
        deleteDatasetTags(gid);

        if (CollectionUtils.isEmpty(tags)) {
            return;
        }

        for (String tag : tags) {
            if (StringUtils.isBlank(tag)) {
                continue;
            }

            addDatasetTag(gid, tag);
        }
    }

    public void deleteDatasetTags(Long gid) {
        String deleteTagRefSql = "delete from kun_mt_dataset_tags where dataset_gid = ?";
        dbOperator.update(deleteTagRefSql, gid);
    }

    public void addDatasetTag(Long gid, String tag) {
        if (StringUtils.isBlank(tag)) {
            return;
        }

        String addTagRefSql = "insert into kun_mt_dataset_tags values (?, ?, ?) ";
        dbOperator.update(addTagRefSql, IdGenerator.getInstance().nextId(), gid, tag);
    }

    public List<DatasetTag> findDatasetTags(Long gid) {
        String findDatasetTagsSql = "select id, dataset_gid, tag from kun_mt_dataset_tags where dataset_gid = ?";
        return dbOperator.fetchAll(findDatasetTagsSql, rs ->
                new DatasetTag(rs.getLong("id"), rs.getLong("dataset_gid"), rs.getString("tag")),
                gid);
    }

}
