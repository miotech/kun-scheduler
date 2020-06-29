export type DataStoreTypeEnum =
  | 'ARANGO_COLLECTION'
  | 'ELASTICSEARCH_INDEX'
  | 'FILE'
  | 'HIVE_TABLE'
  | 'MONGO_COLLECTION'
  | 'MYSQL_TABLE'
  | 'POSTGRES_TABLE'
  | 'SHEET'
  | 'TOPIC';

export interface DataStore {
  type: DataStoreTypeEnum;
}
