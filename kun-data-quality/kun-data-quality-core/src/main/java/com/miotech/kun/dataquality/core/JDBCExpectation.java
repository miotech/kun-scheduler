package com.miotech.kun.dataquality.core;

import com.miotech.kun.metadata.common.connector.Connector;
import com.miotech.kun.metadata.common.connector.ConnectorFactory;
import com.miotech.kun.metadata.common.connector.Query;
import com.miotech.kun.metadata.core.model.datasource.DataSource;

import java.sql.ResultSet;

public class JDBCExpectation implements Expectation {

    private final ExpectationSpec expectationSpec;

    public JDBCExpectation(ExpectationSpec expectationSpec) {
        this.expectationSpec = expectationSpec;
    }

    @Override
    public ExpectationSpec getSpec() {
        return expectationSpec;
    }

    @Override
    public ValidationResult validate() {
        ExpectationSpec spec = getSpec();
        DataSource dataSource = spec.getDataset().getDataSource();
        Connector connector = null;
        try {
            connector = ConnectorFactory.generateConnector(dataSource);
            Query query = new Query(null, null, ((JDBCExpectationMethod) spec.getMethod()).removeEndSemicolon());
            ResultSet rs = connector.query(query);
            ValidationResult vr = spec.getMethod().validate(rs);
            return vr.cloneBuilder().withExpectationId(spec.getExpectationId()).build();
        } finally {
            if (connector != null) {
                connector.close();
            }
        }
    }

}
