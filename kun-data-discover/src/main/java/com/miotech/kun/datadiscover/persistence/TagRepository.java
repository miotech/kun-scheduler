package com.miotech.kun.datadiscover.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Repository
public class TagRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<String> search(String keyword) {
        String sql = "select tag from kun_mt_tag where ? is null or upper(tag) like ?";
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, keyword);
            ps.setString(2, toLikeSql(keyword.toUpperCase()));
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
                    DO_NOTHING;
            jdbcTemplate.update(tagSql, tags.toArray());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteDataset(Long gid, List<String> tags) {
        String deleteTagRefSql = "delete from kun_mt_dataset_tags where dataset_gid = ?";
        jdbcTemplate.update(deleteTagRefSql, gid);

        if (!CollectionUtils.isEmpty(tags)) {
            String addTagRefSql = "insert into kun_mt_dataset_tags values " + toValuesSql(tags.size(), 3);

            jdbcTemplate.update(addTagRefSql, ps -> {
                int paramIndex = 0;
                for (String tag : tags) {
                    ps.setLong(++paramIndex, IdGenerator.getInstance().nextId());
                    ps.setLong(++paramIndex, gid);
                    ps.setString(++paramIndex, tag);
                }
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteDatasource(Long datasourceId, List<String> tags) {
        String deleteTagRefSql = "delete from kun_mt_datasource_tags where datasource_id = ?";
        jdbcTemplate.update(deleteTagRefSql, datasourceId);

        if (!CollectionUtils.isEmpty(tags)) {
            String addTagRefSql = "insert into kun_mt_datasource_tags values " + toValuesSql(tags.size(), 3);

            jdbcTemplate.update(addTagRefSql, ps -> {
                int paramIndex = 0;
                for (String tag : tags) {
                    ps.setLong(++paramIndex, IdGenerator.getInstance().nextId());
                    ps.setLong(++paramIndex, datasourceId);
                    ps.setString(++paramIndex, tag);
                }
            });
        }
    }
}
