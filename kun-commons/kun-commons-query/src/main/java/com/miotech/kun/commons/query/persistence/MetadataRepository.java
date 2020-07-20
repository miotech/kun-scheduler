package com.miotech.kun.commons.query.persistence;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.query.QuerySite;
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

    public Long getDatasourceId(Long datasetId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("datasource_id")
                .from("kun_mt_dataset")
                .where("gid = ?")
                .getSQL();

        return databaseOperator.fetchOne(sql, rs -> rs.getLong("datasource_id"), datasetId);
    }

    public String getDatabase(Long datasetId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("database_name")
                .from("kun_mt_dataset")
                .where("gid = ?")
                .getSQL();

        return databaseOperator.fetchOne(sql, rs -> rs.getString("database_name"), datasetId);
    }

    public MetadataConnectionInfo getConnectionInfo(QuerySite querySite) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.connection_info as connInfo",
                        "kmdt.name as datasource_type")
                .from("kun_mt_datasource", "kmd")
                .join("inner", "kun_mt_datasource_type", "kmdt").on("kmd.type_id = kmdt.id")
                .where("kmd.id = ?")
                .getSQL();

        Long datasourceId = querySite.getDatasourceId();
        if (datasourceId == null) {
            datasourceId = getDatasourceId(querySite.getDatasetId());
        }

        return databaseOperator.fetchOne(sql, rs -> {
            MetadataConnectionInfo connectionInfo = new MetadataConnectionInfo();
            try {
                connectionInfo.setConnectionInfo(JSONUtils.toJsonObject(rs.getString("connInfo")));
            } catch (ParseException e) {
                logger.error("Failed to parse json.", e);
                throw ExceptionUtils.wrapIfChecked(e);
            }
            connectionInfo.setDatasourceType(rs.getString("datasource_type"));
            if (querySite.getDatasetId() != null) {
                connectionInfo.setDatabase(getDatabase(querySite.getDatasetId()));
            }
            return connectionInfo;
        }, datasourceId);
    }
}
