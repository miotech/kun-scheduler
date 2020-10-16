package com.miotech.kun.metadata.common.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.JSONUtils;
import com.miotech.kun.metadata.core.model.dto.DataSourceConnectionDTO;
import com.miotech.kun.metadata.core.model.dto.DataSourceDTO;

@Singleton
public class MetadataDatasourceDao {

    @Inject
    DatabaseOperator dbOperator;

    public DataSourceDTO getDataSourceById(Long datasourceId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.id as id",
                        "kmd.connection_info as connectionInfo",
                        "kmd.type_id as typeId",
                        "kmdt.name as typeName")
                .from("kun_mt_datasource", "kmd")
                .join("inner", "kun_mt_datasource_type", "kmdt").on("kmd.type_id = kmdt.id")
                .where("kmd.id = ?")
                .getSQL();
        return dbOperator.fetchOne(sql, rs -> DataSourceDTO.newBuilder()
                .withId(rs.getLong("id"))
                .withTypeId(rs.getLong("typeId"))
                .withType(rs.getString("typeName"))
                .withConnectionInfo(JSONUtils.jsonToObject(rs.getString("connectionInfo"), DataSourceConnectionDTO.class))
                .build(), datasourceId);
    }

}
