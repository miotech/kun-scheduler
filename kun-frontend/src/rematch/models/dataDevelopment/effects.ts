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
      return taskDefViews.records;
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

  async fetchTaskList(payload: SearchTaskDefinitionReqParams = {}) {
    dispatch.dataDevelopment.updateTasklist({
      loading: true,
    });
    const res = await searchTaskDefinition(payload);
    if (res) {
      dispatch.dataDevelopment.updateTasklist({
        data: res.records,
        loading: false,
        totalCount: res.totalCount,
        pageNum: res.pageNum,
        pageSize: res.pageSize,
        isInit: true,
      });
    }
    dispatch.dataDevelopment.updateTasklist({
      loading: false,
    });
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
