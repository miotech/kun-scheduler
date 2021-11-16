ALTER TABLE kun_mt_datasource ADD COLUMN datasource_type varchar(64) ;
ALTER TABLE kun_mt_datasource ADD COLUMN connection_config jsonb ;

ALTER TABLE kun_mt_datasource_type_fields ADD COLUMN connection_type varchar(64) ;