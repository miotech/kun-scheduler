import { TaskDefinitionModel } from '@/definitions/TaskDefinition.type';
import { PaginationReqBody, SortReqBody } from '@/definitions/common-types';

export interface TaskDefinitionViewBase {
  id: string;
  name: string;
  creator: string;
  createTime: string;
  updateTime: string;
}

export interface TaskDefinitionView extends TaskDefinitionViewBase {
  includedTaskDefinitions: TaskDefinitionModel[];
}

export interface TaskDefinitionViewVO extends TaskDefinitionViewBase {
  includedTaskDefinitionIds: string[];
}

export interface TaskDefinitionViewUpdateVO {
  name: string;
  taskDefinitionIds?: string[];
}

export type TaskDefinitionViewSortColumns = 'id' | 'name' | 'createTime' | 'updateTime';

export interface SearchTaskDefinitionViewParams extends PaginationReqBody, Partial<SortReqBody<TaskDefinitionViewSortColumns>> {
  keyword?: string;
  creator?: string | number;
}
