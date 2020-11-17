import { TaskDefinitionModel } from '@/definitions/TaskDefinition.type';
import { PaginationReqBody, SortReqBody } from '@/definitions/common-types';

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

export type TaskDefinitionViewSortColumns = 'id' | 'name' | 'createTime' | 'updateTime';

export interface SearchTaskDefinitionViewParams extends PaginationReqBody, SortReqBody<TaskDefinitionViewSortColumns> {
  keyword?: string;
  creator?: string | number;
}
