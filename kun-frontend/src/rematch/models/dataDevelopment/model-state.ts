import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { TaskDefinitionViewBase } from '@/definitions/TaskDefinitionView.type';

export interface DataDevelopmentModelFilter {
  taskTemplateName: string | null;
  name: string;
  creatorIds: string[];
}

export interface DataDevelopmentModelState {
  filters: DataDevelopmentModelFilter;
  totalCount: number;
  taskDefinitions: TaskDefinition[];
  displayType: 'DAG' | 'LIST';
  dagTaskDefs: TaskDefinition[];
  /* Task type that user dragged into center panel */
  creatingTaskTemplate: TaskTemplate | null;
  /* Editing task definition */
  editingTaskDefinition: TaskDefinition | null;
  backUrl: string | null;
  definitionFormDirty: boolean;
  taskDefViewsList: TaskDefinitionViewBase[];
  selectedTaskDefView: TaskDefinitionViewBase | null;
  taskTemplates: TaskTemplate[],
}

export const initState: DataDevelopmentModelState = {
  filters: {
    taskTemplateName: null,
    name: '',
    creatorIds: [],
  },
  displayType: 'LIST',
  taskDefinitions: [],
  dagTaskDefs: [],
  totalCount: 0,
  creatingTaskTemplate: null,
  editingTaskDefinition: null,
  backUrl: null,
  definitionFormDirty: false,
  taskDefViewsList: [],
  selectedTaskDefView: null,
  taskTemplates: [],
};
