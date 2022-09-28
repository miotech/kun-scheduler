export interface DataSource {
  id: string;
  name: string;
}

export enum DataSourceType {
  HIVE = 'HIVE',
  POSTGRESQL = 'POSTGRESQL',
  MYSQL = 'MYSQL',
  ARANGO = 'ARANGO',
  MONGODB = 'MONGODB',
  ELASTICSEARCH = 'ELASTICSEARCH',
}

export const dataSourceList = Object.values(DataSourceType);


export enum HiveTempType {
  HIVE_SERVER = 'HIVE_SERVER',
  HIVE_METASTORE = 'HIVE_METASTORE',
  GLUE = 'GLUE',
  S3 = 'S3',
  ATHENA = 'ATHENA',
  HDFS = 'HDFS',
}

export const hiveConnectionTemplateList = [
  {
    tempType: HiveTempType.HIVE_SERVER,
    fieldList: [
      {
        field: 'host',
        type: 'string',
        disabled: true,
      },
      {
        field: 'port',
        type: 'string',
        disabled: true,
      },
      {
        field: 'username',
        type: 'string',
      },
      {
        field: 'password',
        type: 'password',
      },
    ],
  },
  {
    tempType: HiveTempType.HIVE_METASTORE,
    fieldList: [
      {
        field: 'metaStoreUris',
        type: 'string',
      },
    ],
  },
  {
    tempType: HiveTempType.GLUE,
    fieldList: [
      {
        field: 'glueAccessKey',
        type: 'string',
      },
      {
        field: 'glueSecretKey',
        type: 'string',
      },
      {
        field: 'glueRegion',
        type: 'string',
      },
    ],
  },
  {
    tempType: HiveTempType.S3,
    fieldList: [
      {
        field: 's3AccessKey',
        type: 'string',
      },
      {
        field: 's3SecretKey',
        type: 'string',
      },
      {
        field: 's3Region',
        type: 'string',
      },
    ],
  },
  {
    tempType: HiveTempType.ATHENA,
    fieldList: [
      {
        field: 'athenaUrl',
        type: 'string',
        disabled: true,
      },
      {
        field: 'athenaUsername',
        type: 'string',
      },
      {
        field: 'athenaPassword',
        type: 'password',
      },
    ],
  },
  {
    tempType: HiveTempType.HDFS,
    fieldList: [
      {
        field: 'hdfsUrl',
        type: 'string',
      },
      {
        field: 'user',
        type: 'string',
      },
    ],
  },
];

export const otherFieldList = [
  {
    field: 'host',
    type: 'string',
    disabled: true,
  },
  {
    field: 'port',
    type: 'string',
    disabled: true,
  },
  {
    field: 'username',
    type: 'string',
  },
  {
    field: 'password',
    type: 'password',
  },
];

export interface ConnectionItem {
  connection: string;
  templateList: HiveTempType[];
}

export const hiveConnections: ConnectionItem[] = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.HIVE_SERVER, HiveTempType.ATHENA],
  },
  {
    connection: 'dataConnection',
    templateList: [HiveTempType.HIVE_SERVER, HiveTempType.ATHENA],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.HIVE_METASTORE, HiveTempType.GLUE, HiveTempType.HIVE_SERVER, HiveTempType.ATHENA],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.HDFS, HiveTempType.S3, HiveTempType.HIVE_SERVER, HiveTempType.ATHENA],
  },
];

export const otherConnections = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.HIVE_SERVER],
  },
];

export interface DatasourceConfig {
  port?: number;
  host?: string;
  athenaUrl?: string
}

export interface SecurityInfo {

}

export interface ConnectionConfigInfo {

}
export interface UserConnection {
  id: string,
  datasourceId: string,
  connScope: string,
  connectionConfigInfo: ConnectionConfigInfo,
  description: string,
  updateUser?: string,
  createUser?: string,
  createTime?: string,
  securityInfo: SecurityInfo,
  securityUserList: string[]
}

export interface DataConnection {

}

export interface MetadataConnection {

}

export interface StorageConnection {

}

export interface DatasourceConnection {
  userConnection: UserConnection,
  dataConnection: DataConnection,
  metadataConnection: MetadataConnection,
  storageConnection: StorageConnection,
}
export interface DataSourceForm {
  datasourceType: DataSourceType,
  name: string;
  datasourceConnection: DatasourceConnection,
  datasourceConfig: DatasourceConfig,
  tags: string[],
  createUser: string,
  createTime: string
}