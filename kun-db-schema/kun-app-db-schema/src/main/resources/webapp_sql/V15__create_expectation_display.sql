CREATE TABLE IF NOT EXISTS kun_dq_expectation_template (
    name varchar(128) PRIMARY KEY,
    granularity varchar(32),
    description varchar,
    converter varchar(1024),
    display_parameters jsonb
);

INSERT INTO kun_dq_expectation_template values('CUSTOM_SQL', 'CUSTOM', '自定义SQL', 'com.miotech.kun.dataquality.core.converter.CustomSQLExpectationConverter', null);
INSERT INTO kun_dq_expectation_template values('PRIMARY_KEY', 'TABLE', '主键唯一性', 'com.miotech.kun.dataquality.core.converter.PrimaryKeyExpectationConverter', null);

ALTER TABLE kun_dq_expectation DROP COLUMN "method";
ALTER TABLE kun_dq_expectation ADD COLUMN IF NOT EXISTS granularity varchar(128);
ALTER TABLE kun_dq_expectation ADD COLUMN IF NOT EXISTS template_name varchar(128);
ALTER TABLE kun_dq_expectation ADD COLUMN IF NOT EXISTS payload jsonb;