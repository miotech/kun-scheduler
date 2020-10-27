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
        String sql = "select kmd.gid, kmd.name as dataset_name, kmd.database_name, " +
                "kmdsrca.name as datasource_name, kmdsrct.name as datasource_type from kun_mt_dataset kmd\n" +
                "inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n"+
                "inner join kun_mt_datasource_type kmdsrct on kmdsrc.type_id = kmdsrct.id\n";

        String whereClause = "where kmd.gid = ?\n";
        sql += whereClause;

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic basic = new DatasetBasic();
            if (rs.next()) {
                basic.setGid(rs.getLong("gid"));
                basic.setName(rs.getString("dataset_name"));
                basic.setDatabase(rs.getString("database_name"));
                basic.setDatasource(rs.getString("datasource_name"));
                basic.setDatasourceType(rs.getString("datasource_type"));
            }
            return basic;
        }, gid);
    }

    public DatasetBasic findDatasetId(DatasetBasic basic) {
        String sql = "select kmd.gid from kun_mt_dataset kmd\n" +
                "inner join kun_mt_datasource kmdsrc on kmd.datasource_id = kmdsrc.id\n" +
                "inner join kun_mt_datasource_attrs kmdsrca on kmdsrc.id = kmdsrca.datasource_id\n"+
                "inner join kun_mt_datasource_type kmdsrct on kmdsrc.type_id = kmdsrct.id\n";

        String whereClause = "where kmd.name = ? AND kmd.database_name = ? AND kmdsrca.name = ? AND kmdsrct.name = ?\n";
        sql += whereClause;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                basic.setGid(rs.getLong("gid"));
            } else {
                throw new RuntimeException(basic.getDatabase() + "." + basic.getName() + " does not exist.");
            }
            return basic;
        }, basic.getName(), basic.getDatabase(), basic.getDatasource(), basic.getDatasourceType());
    }
}
