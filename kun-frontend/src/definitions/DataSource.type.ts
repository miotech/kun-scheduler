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
  POSTGRESQL = 'POSTGRESQL',
  MYSQL = 'MYSQL',
  ARANGO = 'ARANGO',
  MONGODB = 'MONGODB',
  ELASTICSEARCH = 'ELASTICSEARCH',
}

const commonFieldList = [
  {
    field: 'host',
    type: 'string',
    disabled: true,
    required: true,
  },
  {
    field: 'port',
    type: 'string',
    disabled: true,
    required: true,
  },
  {
    field: 'username',
    type: 'string',
    required: false,
  },
  {
    field: 'password',
    type: 'password',
    encryption: true,
    required: false,
  },
];

export const FieldListMap = {
  [HiveTempType.HIVE_SERVER]: commonFieldList,
  [HiveTempType.HIVE_METASTORE]: [
    {
      field: 'metaStoreUris',
      type: 'string',
      required: false,
    },
  ],
  [HiveTempType.GLUE]: [
    {
      field: 'glueAccessKey',
      type: 'string',
      encryption: true,
      required: false,
    },
    {
      field: 'glueSecretKey',
      type: 'string',
      encryption: true,
      required: false,
    },
    {
      field: 'glueRegion',
      type: 'string',
      required: false,
    },
  ],
  [HiveTempType.S3]: [
    {
      field: 's3AccessKey',
      type: 'string',
      encryption: true,
      required: false,
    },
    {
      field: 's3SecretKey',
      type: 'string',
      encryption: true,
      required: false,
    },
    {
      field: 's3Region',
      type: 'string',
      required: false,
    },
  ],
  [HiveTempType.ATHENA]: [
    {
      field: 'athenaUrl',
      type: 'string',
      disabled: true,
      required: true,
    },
    {
      field: 'athenaUsername',
      type: 'string',
      required: false,
    },
    {
      field: 'athenaPassword',
      type: 'password',
      encryption: true,
      required: false,
    },
  ],
  [HiveTempType.HDFS]: [
    {
      field: 'hdfsUrl',
      type: 'string',
      required: false,
    },
    {
      field: 'user',
      type: 'string',
      required: false,
    },
  ],
  [HiveTempType.POSTGRESQL]: commonFieldList,
  [HiveTempType.MYSQL]: commonFieldList,
  [HiveTempType.ARANGO]: commonFieldList,
  [HiveTempType.MONGODB]: commonFieldList,
  [HiveTempType.ELASTICSEARCH]: commonFieldList,
};

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

export const mysqlConnections: ConnectionItem[] = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.MYSQL],
  },
  {
    connection: 'dataConnection',
    templateList: [HiveTempType.MYSQL],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.MYSQL],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.MYSQL],
  },
];

export const postgresqlConnections: ConnectionItem[] = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.POSTGRESQL],
  },
  {
    connection: 'dataConnection',
    templateList: [HiveTempType.POSTGRESQL],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.POSTGRESQL],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.POSTGRESQL],
  },
];

export const arangoConnections: ConnectionItem[] = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.ARANGO],
  },
  {
    connection: 'dataConnection',
    templateList: [HiveTempType.ARANGO],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.ARANGO],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.ARANGO],
  },
];

export const mongodbConnections: ConnectionItem[] = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.MONGODB],
  },
  {
    connection: 'dataConnection',
    templateList: [HiveTempType.MONGODB],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.MONGODB],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.MONGODB],
  },
];

export const elasticsearchConnections: ConnectionItem[] = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.ELASTICSEARCH],
  },
  {
    connection: 'dataConnection',
    templateList: [HiveTempType.ELASTICSEARCH],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.ELASTICSEARCH],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.ELASTICSEARCH],
  },
];

export const databaseTemplateMap = {
  [DataSourceType.HIVE]: hiveConnections,
  [DataSourceType.MYSQL]: mysqlConnections,
  [DataSourceType.POSTGRESQL]: postgresqlConnections,
  [DataSourceType.ARANGO]: arangoConnections,
  [DataSourceType.MONGODB]: mongodbConnections,
  [DataSourceType.ELASTICSEARCH]: elasticsearchConnections,
};
export interface DatasourceConfig {
  port?: number;
  host?: string;
  athenaUrl?: string;
}

export interface SecurityInfo {}

export interface ConnectionConfigInfo {
  connectionType: HiveTempType;
}
export interface UserConnection {
  id: string;
  name: string;
  connScope: string;
  connectionConfigInfo: ConnectionConfigInfo;
  description: string;
  updateUser?: string;
  updateTime?: string;
  createUser?: string;
  createTime?: string;
  securityInfo: SecurityInfo;
  securityUserList: string[];
}

export interface DataConnection {}

export interface MetadataConnection {}

export interface StorageConnection {}

export interface DatasourceConnection {
  userConnectionList: UserConnection[];
  dataConnection: DataConnection;
  metadataConnection: MetadataConnection;
  storageConnection: StorageConnection;
}
export interface DataSourceForm {
  datasourceType: DataSourceType;
  name: string;
  datasourceConnection: DatasourceConnection;
  datasourceConfigInfo: DatasourceConfig;
  tags: string[];
}

export interface DataSourceInfo extends DataSourceForm {
  createUser: string;
  createTime: number;
  updateUser: string;
  updateTime: number;
  id: string;
}
