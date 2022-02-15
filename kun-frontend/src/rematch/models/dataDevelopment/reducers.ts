import {
  DataDevelopmentModelFilter,
  DataDevelopmentModelState as ModelState,
} from '@/rematch/models/dataDevelopment/model-state';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';
import { TaskDefinitionViewBase } from '@/definitions/TaskDefinitionView.type';
import produce from 'immer';

export const reducers = {
  setDisplayType: (state: ModelState, payload: 'LIST' | 'DAG'): ModelState => {
    return {
      ...state,
      displayType: payload,
    };
  },
  setCreatingTaskTemplate: (state: ModelState, payload: TaskTemplate | null): ModelState => {
    return {
      ...state,
      creatingTaskTemplate: payload,
    };
  },
  setEditingTaskDefinition: (state: ModelState, payload: TaskDefinition | null): ModelState => {
    return {
      ...state,
      editingTaskDefinition: payload,
    };
  },
  setBackUrl: (state: ModelState, payload: string | null): ModelState => {
    return {
      ...state,
      backUrl: payload,
    };
  },
  setDefinitionFormDirty: (state: ModelState, payload: boolean): ModelState => {
    return {
      ...state,
      definitionFormDirty: payload,
    };
  },
  updateFilter: (state: ModelState, payload: Partial<DataDevelopmentModelFilter>): ModelState => {
    return {
      ...state,
      filters: {
        ...state.filters,
        ...payload,
      },
    };
  },
  setTaskDefinitionViewsList: (state: ModelState, payload: TaskDefinitionViewBase[]): ModelState => {
    return {
      ...state,
      taskDefViewsList: payload,
    };
  },
  addTaskDefinitionViewsToList: (state: ModelState): ModelState => {
    return {
      ...state,
    };
  },
  setSelectedTaskDefinitionView: (state: ModelState, payload: TaskDefinitionViewBase | null): ModelState => {
    return {
      ...state,
      selectedTaskDefView: payload,
    };
  },
  setTaskTemplates: (state: ModelState, payload: TaskTemplate[]): ModelState => {
    return {
      ...state,
      taskTemplates: payload,
    };
  },
  setRecordCount: (state: ModelState, payload: number): ModelState => {
    return {
      ...state,
      recordCount: payload,
    };
  },
  updateTasklist: produce((draftState: ModelState, payload: ModelState['taskList']) => {
    draftState.taskList = {
      ...draftState.taskList,
      ...payload,
    };
  }),
};
