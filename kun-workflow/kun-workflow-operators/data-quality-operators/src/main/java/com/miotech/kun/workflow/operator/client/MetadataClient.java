package com.miotech.kun.workflow.operator.client;

import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.query.datasource.MetadataDataSource;
import com.miotech.kun.workflow.operator.model.Dataset;
import com.miotech.kun.workflow.operator.model.DatasetField;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
public class MetadataClient {

    private DatabaseOperator databaseOperator;

    private MetadataClient() {
        databaseOperator = new DatabaseOperator(MetadataDataSource.getInstance().getMetadataDataSource());
    }

    private static class SingletonHolder {
        private static MetadataClient instance = new MetadataClient();
    }

    public static MetadataClient getInstance() {
        return MetadataClient.SingletonHolder.instance;
    }

    public Dataset getDataset(Long gid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.gid as gid",
                        "kmd.name as name",
                        "kmd.datasource_id as datasource_id",
                        "kmd.database_name as database",
                        "kmdsrct.name as datasource_type")
                .from("kun_mt_dataset kmd")
                .join("inner", "kun_mt_datasource", "kmdsrc").on("kmd.datasource_id = kmdsrc.id")
                .join("inner", "kun_mt_datasource_type", "kmdsrct").on("kmdsrc.type_id = kmdsrct.id")
                .where("kmd.gid = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            Dataset dataset = new Dataset();
            if (rs.next()) {
                dataset.setId(rs.getLong("gid"));
                dataset.setName(rs.getString("name"));
                dataset.setDatasourceId(rs.getLong("datasource_id"));
                dataset.setDatabase(rs.getString("database"));
                dataset.setDatasourceType(rs.getString("datasource_type"));
            }
            return dataset;
        }, gid);
    }

    public List<DatasetField> getDatasetFields(List<Long> ids) {
        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        for (Long id : ids) {
            stringJoiner.add(id.toString());
        }
        String sql = DefaultSQLBuilder.newBuilder()
                .select("name")
                .from("kun_mt_dataset_field")
                .where("id in " + stringJoiner.toString())
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            List<DatasetField> datasetFields = new ArrayList<>();
            while (rs.next()) {
                DatasetField datasetField = new DatasetField();
                datasetField.setName(rs.getString("name"));
                datasetFields.add(datasetField);
            }
            return datasetFields;
        });
    }
}
