import { RootDispatch } from '@/rematch/store';

import {
  fetchAllTaskDefinitions,
  fetchTaskDefinitionDetail,
  searchTaskDefinition,
  SearchTaskDefinitionReqParams,
} from '@/services/data-development/task-definitions';

export const effects = (dispatch: RootDispatch) => ({
  async fetchTaskDefinitionsForDAG() {
    const taskDefs = await fetchAllTaskDefinitions();
    if (taskDefs) {
      dispatch.dataDevelopment.setDAGTaskDefs(taskDefs || []);
    }
  },

  async fetchTaskDefinitions(payload: SearchTaskDefinitionReqParams = {}) {
    const taskDefsResp = await searchTaskDefinition(payload);
    if (taskDefsResp) {
      dispatch.dataDevelopment.setTaskDefinitions(taskDefsResp.records || []);
      dispatch.dataDevelopment.setTotalCount(parseInt(`${taskDefsResp.totalCount}`, 10));
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
