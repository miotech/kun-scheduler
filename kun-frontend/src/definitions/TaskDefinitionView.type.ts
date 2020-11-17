import { TaskDefinitionModel } from '@/definitions/TaskDefinition.type';

export interface TaskDefinitionView {
  id: string;
  name: string;
  creator: string;
  createTime: string;
  updateTime: string;
  includedTaskDefinitions: TaskDefinitionModel[];
}

export interface TaskDefinitionViewVO {
  id: string;
  name: string;
  creator: string;
  createTime: string;
  updateTime: string;
  includedTaskDefinitionIds: string[];
}

export interface TaskDefinitionViewUpdateVO {
  name: string;
  includedTaskDefinitionIds?: string[];
}
