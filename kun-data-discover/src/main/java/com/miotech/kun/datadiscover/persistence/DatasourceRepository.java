package com.miotech.kun.datadiscover.persistence;

import com.miotech.kun.datadiscover.common.util.DateUtil;
import com.miotech.kun.datadiscover.model.bo.DatabaseSearchRequest;
import com.miotech.kun.datadiscover.model.entity.Datasource;
import com.miotech.kun.datadiscover.model.entity.DatasourcePage;
import com.miotech.kun.datadiscover.model.entity.DatasourceType;
import com.miotech.kun.datadiscover.model.entity.DatasourceTypeField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Repository
@Slf4j
public class DatasourceRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public DatasourcePage search(DatabaseSearchRequest databaseSearchRequest) {
        List<Object> pstmtArgs = new ArrayList<>();
        String sql = "select kmd.*,\n" +
                "       kmda.*,\n" +
                "       kmdtype.name as type,\n" +
                "       string_agg(distinct(kmdtag.tag), ',') as tags\n" +
                "     from kun_mt_datasource kmd\n" +
                "     left join kun_mt_datasource_attrs kmda on kmd.id = kmda.datasource_id\n" +
                "     left join kun_mt_datasource_type kmdtype on kmd.type_id = kmdtype.id\n" +
                "     left join kun_mt_datasource_tags kmdtag on kmd.id = kmdtag.datasource_id\n";

        if (StringUtils.isNotEmpty(databaseSearchRequest.getSearch())) {
            String whereClause = "where upper(kmda.name) like ?\n";
            sql += whereClause;
            pstmtArgs.add(toLikeSql(databaseSearchRequest.getSearch().toUpperCase()));
        }
        String groupByClause = "group by kmd.id, kmda.datasource_id, kmda.name, kmda.create_user, kmda.create_time, kmda.update_user, kmda.update_time, type\n";
        sql += groupByClause;
        String countSql = "select count(1) as total_count from (" + sql + ") as result";
        Long totalCount = jdbcTemplate.queryForObject(countSql, pstmtArgs.toArray(), Long.class);
        String orderByClause = "order by kmda.name\n";
        String limitSql = pageInfoToSql(databaseSearchRequest.getPageNumber(), databaseSearchRequest.getPageSize());

        List<Datasource> databasesResult = jdbcTemplate.query(sql + orderByClause + limitSql, pstmtArgs.toArray(), rs -> {
            List<Datasource> datasources = new ArrayList<>();
            while (rs.next()) {
                Datasource datasource = new Datasource();
                datasource.setId(rs.getLong("id"));
                datasource.setName(rs.getString("name"));
                datasource.setType(rs.getString("type"));
                try {
                    datasource.setConnectInfo((JSONObject) new JSONParser().parse(rs.getString("connection_info")));
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }
                datasource.setCreateUser(rs.getString("create_user"));
                datasource.setCreateTime(DateUtil.dateTimeToMillis(rs.getObject("create_time", OffsetDateTime.class)));
                datasource.setUpdateUser(rs.getString("update_user"));
                datasource.setUpdateTime(DateUtil.dateTimeToMillis(rs.getObject("update_time", OffsetDateTime.class)));
                datasource.setTags(sqlToList(rs.getString("tags")));
                datasources.add(datasource);
            }
            return datasources;
        });
        DatasourcePage datasourcePage = new DatasourcePage();
        datasourcePage.setDatasources(databasesResult);
        datasourcePage.setPageNumber(databaseSearchRequest.getPageNumber());
        datasourcePage.setPageSize(databaseSearchRequest.getPageSize());
        datasourcePage.setTotalCount(totalCount);
        return datasourcePage;
    }

    public List<DatasourceType> getAllTypes() {
        String sql = "select kmdt.name as type, \n" +
                "       kmdtf.name as field_key, \n" +
                "       kmdtf.sequence_order as sequence_order, \n" +
                "       kmdtf.format as format, \n" +
                "       kmdtf.require as require\n" +
                "     from kun_mt_datasource_type kmdt\n" +
                "         left join kun_mt_datasource_type_fields kmdtf on kmdt.id = kmdtf.type_id";

        Map<String, DatasourceType> typeMap = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                String type = rs.getString("type");
                DatasourceType datasourceType = typeMap.putIfAbsent(type, new DatasourceType());
                datasourceType.setName(type);
                DatasourceTypeField field = new DatasourceTypeField();
                field.setName(rs.getString("field_key"));
                field.setSequenceOrder(rs.getInt("sequence_order"));
                field.setFormat(rs.getString("format"));
                field.setRequire(rs.getBoolean("require"));
                datasourceType.addField(field);
            }
        });
        List<DatasourceType> datasourceTypes = new ArrayList<>();
        datasourceTypes.addAll(datasourceTypes);
        return datasourceTypes;
    }
}
