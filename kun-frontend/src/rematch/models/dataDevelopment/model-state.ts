import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { TaskDefinitionViewBase } from '@/definitions/TaskDefinitionView.type';

export interface DataDevelopmentModelFilter {
  taskTemplateName: string | null;
  name: string;
  creators: string[];
}

export interface DataDevelopmentModelState {
  filters: DataDevelopmentModelFilter;
  totalCount: number;
  taskDefinitions: TaskDefinition[];
  taskList: {
    loading: boolean;
    error: string | null;
    pageNum: number;
    pageSize: number;
    totalCount: number;
    data: [];
    isInit: boolean;
  };
  displayType: 'DAG' | 'LIST';
  dagTaskDefs: TaskDefinition[];
  /* Task type that user dragged into center panel */
  creatingTaskTemplate: TaskTemplate | null;
  /* Editing task definition */
  editingTaskDefinition: TaskDefinition | null;
  backUrl: string | null;
  definitionFormDirty: boolean;
  taskDefViewsList: TaskDefinitionViewBase[];
  allTasksCount: number;
  danglingTasksCount: number;
  selectedTaskDefView: TaskDefinitionViewBase | null;
  taskTemplates: TaskTemplate[];
  recordCount: number;
}

export const initState: DataDevelopmentModelState = {
  filters: {
    taskTemplateName: null,
    name: '',
    creators: [],
  },
  displayType: 'LIST',
  taskDefinitions: [],
  taskList: {
    loading: false,
    error: null,
    pageNum: 1,
    pageSize: 25,
    totalCount: 0,
    data: [],
    isInit: false,
  },
  dagTaskDefs: [],
  totalCount: 0,
  creatingTaskTemplate: null,
  editingTaskDefinition: null,
  backUrl: null,
  definitionFormDirty: false,
  taskDefViewsList: [],
  selectedTaskDefView: null,
  taskTemplates: [],
  recordCount: 0,
};
