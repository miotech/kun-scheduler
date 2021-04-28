package com.miotech.kun.datadiscovery.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.datadiscovery.model.bo.BasicSearchRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceRequest;
import com.miotech.kun.datadiscovery.model.bo.DataSourceSearchRequest;
import com.miotech.kun.datadiscovery.model.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Repository
@Slf4j
public class DataSourceRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TagRepository tagRepository;

    public DataSourceBasicPage search(BasicSearchRequest basicSearchRequest) {
        String sql = "select id, name from kun_mt_datasource kmd\n" +
                "left join kun_mt_datasource_attrs kmda on kmd.id = kmda.datasource_id\n";

        String whereClause = "where upper(name) like ?\n";
        sql += whereClause;

        String orderClause = "order by name asc\n";
        sql += orderClause;

        String limitSql = toLimitSql(1, basicSearchRequest.getPageSize());
        sql += limitSql;

        return jdbcTemplate.query(sql, rs -> {
            DataSourceBasicPage page = new DataSourceBasicPage();
            while (rs.next()) {
                DatasourceBasic basic = new DatasourceBasic();
                basic.setId(rs.getLong("id"));
                basic.setName(rs.getString("name"));
                page.add(basic);
            }
            return page;
        }, toLikeSql(basicSearchRequest.getKeyword().toUpperCase()));
    }

    public DataSourcePage search(DataSourceSearchRequest datasourceSearchRequest) {
        List<Object> pstmtArgs = new ArrayList<>();
        String sql = "select kmd.*,\n" +
                "       kmda.*,\n" +
                "       string_agg(distinct(kmdtag.tag), ',') as tags\n" +
                "     from kun_mt_datasource kmd\n" +
                "     left join kun_mt_datasource_attrs kmda on kmd.id = kmda.datasource_id\n" +
                "     left join kun_mt_datasource_tags kmdtag on kmd.id = kmdtag.datasource_id\n";

        if (StringUtils.isNotEmpty(datasourceSearchRequest.getSearch())) {
            String whereClause = "where upper(kmda.name) like ?\n";
            sql += whereClause;
            pstmtArgs.add(toLikeSql(datasourceSearchRequest.getSearch().toUpperCase()));
        }
        String groupByClause = "group by kmd.id, kmda.datasource_id\n";
        sql += groupByClause;
        String countSql = "select count(1) as total_count from (" + sql + ") as result";
        Integer totalCount = jdbcTemplate.queryForObject(countSql, pstmtArgs.toArray(), Integer.class);
        String orderByClause = "order by kmda.name\n";
        String limitSql = toLimitSql(datasourceSearchRequest.getPageNumber(), datasourceSearchRequest.getPageSize());

        List<DataSource> databasesResult = jdbcTemplate.query(sql + orderByClause + limitSql, pstmtArgs.toArray(), rs -> {
            List<DataSource> dataSources = new ArrayList<>();
            while (rs.next()) {
                DataSource datasource = new DataSource();
                setDatasourceField(datasource, rs);
                dataSources.add(datasource);
            }
            return dataSources;
        });
        DataSourcePage datasourcePage = new DataSourcePage();
        datasourcePage.setDatasources(databasesResult);
        datasourcePage.setPageNumber(datasourceSearchRequest.getPageNumber());
        datasourcePage.setPageSize(datasourceSearchRequest.getPageSize());
        datasourcePage.setTotalCount(totalCount);
        return datasourcePage;
    }

    public List<DataSourceType> getAllTypes() {
        final String sql = "select kmdt.id as id, \n" +
                "       kmdt.name as type, \n" +
                "       kmdtf.name as field_key, \n" +
                "       kmdtf.sequence_order as sequence_order, \n" +
                "       kmdtf.format as format, \n" +
                "       kmdtf.require as require\n" +
                "     from kun_mt_datasource_type kmdt\n" +
                "         left join kun_mt_datasource_type_fields kmdtf on kmdt.id = kmdtf.type_id where 1=1";

        Map<String, DataSourceType> typeMap = new HashMap<>();
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                String type = rs.getString("type");
                DataSourceType datasourceType = new DataSourceType();
                datasourceType = ObjectUtils.firstNonNull(typeMap.putIfAbsent(type, datasourceType), datasourceType);
                datasourceType.setId(rs.getLong("id"));
                datasourceType.setName(type);
                DatasourceTypeField field = new DatasourceTypeField();
                field.setName(rs.getString("field_key"));
                field.setSequenceOrder(rs.getInt("sequence_order"));
                field.setFormat(rs.getString("format"));
                field.setRequire(rs.getBoolean("require"));
                datasourceType.addField(field);
            }
            return new ArrayList<>(typeMap.values());
        });
    }

    public DataSource find(Long id) {
        String sql = "select kmd.*,\n" +
                "       kmda.*,\n" +
                "       string_agg(distinct(kmdtag.tag), ',') as tags\n" +
                "     from kun_mt_datasource kmd\n" +
                "     left join kun_mt_datasource_attrs kmda on kmd.id = kmda.datasource_id\n" +
                "     left join kun_mt_datasource_tags kmdtag on kmd.id = kmdtag.datasource_id\n";

        String whereClause = "where kmd.id = ?";
        String groupByClause = "group by kmd.id, kmda.datasource_id\n";
        return jdbcTemplate.query(sql + whereClause + groupByClause, ps -> ps.setLong(1, id), rs -> {
            DataSource datasource = new DataSource();
            if (rs.next()) {
                setDatasourceField(datasource, rs);
            }
            return datasource;
        });
    }

    private void setDatasourceField(DataSource datasource, ResultSet rs) throws SQLException {
        datasource.setId(rs.getLong("id"));
        datasource.setName(rs.getString("name"));
        datasource.setTypeId(rs.getLong("type_id"));
        if (StringUtils.isNotEmpty(rs.getString("connection_info"))) {
            datasource.setConnectInfo(JSONUtils.toJsonObject(rs.getString("connection_info")));
        }
        datasource.setCreateUser(rs.getString("create_user"));
        datasource.setCreateTime(timestampToMillis(rs, "create_time"));
        datasource.setUpdateUser(rs.getString("update_user"));
        datasource.setUpdateTime(timestampToMillis(rs, "update_time"));
        datasource.setTags(sqlToList(rs.getString("tags")));
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSource insert(DataSourceRequest dataSourceRequest) throws SQLException {
        Long datasourceId = IdGenerator.getInstance().nextId();
        String kmdSql = "insert into kun_mt_datasource values " + toValuesSql(1, 3);
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        jsonObject.setValue(JSONUtils.toJsonString(dataSourceRequest.getInformation()));
        jdbcTemplate.update(kmdSql, datasourceId, jsonObject, dataSourceRequest.getTypeId());

        String kmdaSql = "insert into kun_mt_datasource_attrs values " + toValuesSql(1, 6);
        jdbcTemplate.update(kmdaSql,
                datasourceId,
                dataSourceRequest.getName(),
                dataSourceRequest.getCreateUser(),
                millisToTimestamp(dataSourceRequest.getCreateTime()),
                dataSourceRequest.getUpdateUser(),
                millisToTimestamp(dataSourceRequest.getUpdateTime()));

        tagRepository.save(dataSourceRequest.getTags());
        tagRepository.overwriteDatasource(datasourceId, dataSourceRequest.getTags());
        return find(datasourceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public DataSource update(Long id, DataSourceRequest dataSourceRequest) throws SQLException {
        String kmdSql = "update kun_mt_datasource set connection_info = ?, " +
                "type_id = ?\n" +
                "where id = ?";

        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        jsonObject.setValue(JSONUtils.toJsonString(dataSourceRequest.getInformation()));
        jdbcTemplate.update(kmdSql, jsonObject, dataSourceRequest.getTypeId(), id);

        String kmdaSql = "update kun_mt_datasource_attrs set name = ?, " +
                "update_user = ?, " +
                "update_time = ?\n" +
                "where datasource_id = ?";
        jdbcTemplate.update(kmdaSql,
                dataSourceRequest.getName(),
                dataSourceRequest.getUpdateUser(),
                millisToTimestamp(dataSourceRequest.getUpdateTime()),
                id);

        tagRepository.save(dataSourceRequest.getTags());
        tagRepository.overwriteDatasource(id, dataSourceRequest.getTags());
        return find(id);
    }

    public void delete(Long id) {
        String sql = "delete from kun_mt_datasource where id = ?";

        jdbcTemplate.update(sql, id);
    }
}
