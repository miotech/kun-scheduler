import { user } from './user';
import { route } from './route';
import { dataDiscovery } from './dataDiscovery';
import { datasetDetail } from './datasetDetail';
import { dataSettings } from './dataSettings';
import { glossary } from './glossary';
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
};
