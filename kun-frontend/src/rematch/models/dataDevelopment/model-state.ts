import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';

export interface DataDevelopmentModelFilter {
  pageNum: number;
  pageSize: number;
  taskTemplateName: string | null;
  name: string;
  creatorIds: string[];
}

export interface DataDevelopmentModelState {
  filters: DataDevelopmentModelFilter;
  totalCount: number;
  taskDefinitions: TaskDefinition[];
  dagTaskDefs: TaskDefinition[];
  /* Task type that user dragged into center panel */
  creatingTaskTemplate: TaskTemplate | null;
  /* Editing task definition */
  editingTaskDefinition: TaskDefinition | null;
  backUrl: string | null;
  definitionFormDirty: boolean;
}

export const initState: DataDevelopmentModelState = {
  filters: {
    pageNum: 1,
    pageSize: 25,
    taskTemplateName: null,
    name: '',
    creatorIds: [],
  },
  taskDefinitions: [],
  dagTaskDefs: [],
  totalCount: 0,
  creatingTaskTemplate: null,
  editingTaskDefinition: null,
  backUrl: null,
  definitionFormDirty: false,
};
