import { RootDispatch } from '@/rematch/store';

import {
  fetchAllTaskDefinitions,
  fetchTaskDefinitionDetail,
  searchTaskDefinition,
  SearchTaskDefinitionReqParams,
} from '@/services/data-development/task-definitions';
import { SearchTaskDefinitionViewParams } from '@/definitions/TaskDefinitionView.type';
import { searchTaskDefinitionViews } from '@/services/data-development/task-definition-views';
import { fetchTaskTemplates } from '@/services/data-development/task-templates';

export const effects = (dispatch: RootDispatch) => ({
  async fetchTaskDefViews(payload: Partial<SearchTaskDefinitionViewParams>) {
    try {
      const taskDefViews = await searchTaskDefinitionViews({
        pageNumber: 1,
        pageSize: 100,
        ...payload,
      });
      dispatch.dataDevelopment.setTaskDefinitionViewsList(taskDefViews.records);
    } finally {
      // do nothing
    }
  },

  async fetchTaskDefinitionsForDAG() {
    const taskDefs = await fetchAllTaskDefinitions();
    if (taskDefs) {
      // dispatch.dataDevelopment.setDAGTaskDefs(taskDefs || []);
    }
  },

  async fetchTaskDefinitions(payload: SearchTaskDefinitionReqParams = {}) {
    const taskDefsResp = await searchTaskDefinition(payload);
    if (taskDefsResp) {
      // dispatch.dataDevelopment.setTaskDefinitions(taskDefsResp.records || []);
      // dispatch.dataDevelopment.setTotalCount(parseInt(`${taskDefsResp.totalCount}`, 10));
    }
  },

  async fetchTaskTemplates() {
    try {
      const respData = await fetchTaskTemplates();
      if (respData) {
        dispatch.dataDevelopment.setTaskTemplates(respData);
      }
    } finally {
      // do nothing
    }
  },

  async fetchEditingTaskDefinition(payload: number | string) {
    try {
      const editingTaskDefinition = await fetchTaskDefinitionDetail(payload);
      if (editingTaskDefinition) {
        dispatch.dataDevelopment.setEditingTaskDefinition(editingTaskDefinition);
      }
    } catch (e) {
      // do nothing
    }
  },
});
