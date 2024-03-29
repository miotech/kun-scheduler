import _ from 'lodash';

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
  SAME = 'SAME',
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
      },
      {
        field: 'port',
        type: 'string',
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
  },
  {
    field: 'port',
    type: 'string',
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
    templateList: [HiveTempType.SAME, HiveTempType.HIVE_SERVER, HiveTempType.ATHENA],
  },
  {
    connection: 'metadataConnection',
    templateList: [HiveTempType.SAME, HiveTempType.HIVE_METASTORE, HiveTempType.GLUE],
  },
  {
    connection: 'storageConnection',
    templateList: [HiveTempType.SAME, HiveTempType.HDFS, HiveTempType.S3],
  },
];

export const otherConnections = [
  {
    connection: 'userConnection',
    templateList: [HiveTempType.HIVE_SERVER],
  },
];

export const getCurrentFields = (tempType: HiveTempType) => {
  return hiveConnectionTemplateList.find(i => i.tempType === tempType)?.fieldList ?? otherFieldList;
};

export const getConnectionContentMap = (tempType: HiveTempType) => {
  const fields = getCurrentFields(tempType);
  const fieldMap: { [k: string]: string } = {};
  fields.forEach(i => {
    fieldMap[i.field] = '';
  });
  return fieldMap;
};

export const isSame = (source: { [k: string]: string }, target: { [k: string]: string }) => {
  let flag = true;
  _.forEach(source, (v, k) => {
    if (v !== target[k]) {
      flag = false;
    }
  });
  return flag;
};
