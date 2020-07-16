package com.miotech.kun.commons.query.persistence;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.query.datasource.MetadataDataSource;
import com.miotech.kun.commons.query.model.MetadataConnectionInfo;
import com.miotech.kun.commons.query.utils.JSONUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @author: Jie Chen
 * @created: 2020/7/10
 */
public class MetadataRepository {

    private static Logger logger = LoggerFactory.getLogger(MetadataRepository.class);

    private DataSource metadataDataSource;

    private DatabaseOperator databaseOperator;

    public MetadataRepository() {
        metadataDataSource = MetadataDataSource.getInstance().getMetadataDataSource();
        databaseOperator = new DatabaseOperator(metadataDataSource);
    }

    public MetadataConnectionInfo getConnectionInfo(Long datasourceId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("connection_info as connInfo",
                        "kmdt.name as type")
                .from("kun_mt_datasource", "kmd")
                .join("inner", "kun_mt_datasource_type", "kmdt").on("kmd.type_id = kmdt.id")
                .where("kmd.id = ?")
                .getSQL();

        return databaseOperator.fetchOne(sql, rs -> {
            MetadataConnectionInfo connectionInfo = new MetadataConnectionInfo();
            try {
                connectionInfo.setConnectionInfo(JSONUtils.toJsonObject(rs.getString("connInfo")));
            } catch (ParseException e) {
                logger.error("Failed to parse json.", e);
                throw ExceptionUtils.wrapIfChecked(e);
            }
            connectionInfo.setType(rs.getString("type"));
            return connectionInfo;
        }, datasourceId);
    }
}
