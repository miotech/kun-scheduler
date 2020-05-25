package com.miotech.kun.datadiscover.persistence;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscover.model.bo.DatasetRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Repository
public class TagRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<String> search(String keyword) {
        String sql = "select tag from kun_mt_tag where ? is null or tag like ?";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, keyword);
            ps.setString(2, toLikeSql(keyword));
        }, rs -> {
            List<String> tags = new ArrayList<>();
            while (rs.next()) {
                tags.add(rs.getString("tag"));
            }
            return tags;
        });
    }

    public void save(List<String> tags) {
        if (!CollectionUtils.isEmpty(tags)) {
            String tagSql = "insert into kun_mt_tag values " + toValuesSql(tags.size(), 1) + "\n" +
                    "on conflict (tag)\n" +
                    "do nothing";
            jdbcTemplate.update(tagSql, ps -> {
                int paramIndex = 0;
                for (String tag : tags) {
                    ps.setString(++paramIndex, tag);
                }
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwrite(Long datasetId, List<String> tags) {
        String deleteTagRefSql = "delete from kun_mt_dataset_tags where dataset_gid = ?";
        jdbcTemplate.update(deleteTagRefSql, ps -> ps.setLong(1, datasetId));

        if (!CollectionUtils.isEmpty(tags)) {
            String addTagRefSql = "insert into kun_mt_dataset_tags values " + toValuesSql(tags.size(), 3) + "\n" +
                    "on conflict (dataset_gid, tag)\n" +
                    "do nothing";

            jdbcTemplate.update(addTagRefSql, ps -> {
                int paramIndex = 0;
                for (String tag : tags) {
                    ps.setLong(++paramIndex, IdGenerator.getInstance().nextId());
                    ps.setLong(++paramIndex, datasetId);
                    ps.setString(++paramIndex, tag);
                }
            });
        }
    }
}
