package com.miotech.kun.dataquality.persistence;

import com.miotech.kun.dataquality.model.entity.DatasetBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Repository("dq-datasetRepository")
public class DatasetRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

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
}
