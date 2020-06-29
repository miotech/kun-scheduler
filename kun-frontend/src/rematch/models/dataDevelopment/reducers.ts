import {
  DataDevelopmentModelFilter, DataDevelopmentModelState as ModelState
} from '@/rematch/models/dataDevelopment/model-state';
import { TaskDefinition } from '@/definitions/TaskDefinition.type';
import { TaskTemplate } from '@/definitions/TaskTemplate.type';

export const reducers = {
  setTaskDefinitions: (state: ModelState, payload: TaskDefinition[]): ModelState => {
    return {
      ...state,
      taskDefinitions: payload,
    };
  },
  setDAGTaskDefs: (state: ModelState, payload: TaskDefinition[]): ModelState => {
    return {
      ...state,
      dagTaskDefs: payload,
    };
  },
  setTotalCount: (state: ModelState, payload: number): ModelState => {
    return {
      ...state,
      totalCount: payload,
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
};
