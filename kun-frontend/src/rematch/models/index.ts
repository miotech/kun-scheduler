import { user } from './user';
import { route } from './route';
import { dataDiscovery } from './dataDiscovery';
import { datasetDetail } from './datasetDetail';
import { dataSettings } from './dataSettings';
import { glossary } from './glossary';
import { pdfTryout } from './pdfTryout';
import { pdfExtract } from './pdfExtract';
import { pdfBatchTask } from './pdfBatchTask';
import { pdfFileTask } from './pdfFileTask';
import { pdfNew } from './pdfNew';
import { dataDevelopment } from './dataDevelopment';
import { scheduledTasks } from './operationCenter/scheduledTasks';
import { deployedTaskDetail } from './operationCenter/deployedTaskDetail';

export interface RootModel {
  user: typeof user;
  route: typeof route;
  dataDiscovery: typeof dataDiscovery;
  datasetDetail: typeof datasetDetail;
  dataSettings: typeof dataSettings;
  glossary: typeof glossary;
  dataDevelopment: typeof dataDevelopment;
  scheduledTasks: typeof scheduledTasks;
  deployedTaskDetail: typeof deployedTaskDetail;
  pdfTryout: typeof pdfTryout;
  pdfExtract: typeof pdfExtract;
  pdfBatchTask: typeof pdfBatchTask;
  pdfFileTask: typeof pdfFileTask;
  pdfNew: typeof pdfNew;
}

export const models: RootModel = {
  user,
  route,
  dataDiscovery,
  datasetDetail,
  dataSettings,
  glossary,
  dataDevelopment,
  scheduledTasks,
  deployedTaskDetail,
  pdfTryout,
  pdfExtract,
  pdfBatchTask,
  pdfFileTask,
  pdfNew,
};

// some common types
export interface Pagination {
  pageSize: number;
  pageNumber: number;
  totalCount?: number;
}

export interface Sort {
  sortOrder?: string;
  sortColumn?: string;
}

export enum DbType {
  MySQL = 'MySQL',
  PostgreSQL = 'PostgreSQL',
  Arrango = 'Arrango',
  Hive = 'Hive',
  Redis = 'Redis',
  MongoDB = 'MongoDB',
  Elasticsearch = 'Elasticsearch',
  Amazon_S3 = 'Amazon S3',
  HDFS = 'HDFS',
  FTP = 'FTP',
}
