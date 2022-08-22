import { TaskRun, TaskRunDependency } from '@/definitions/TaskRun.type';
import produce from 'immer';
import { fetchTaskRunDAG, fetchDeployedTaskDAG } from '@/services/task-deployments/deployed-tasks';
import cloneDeep from 'lodash/cloneDeep';
import { DeployedTask, DeployedTaskDependency } from '@/definitions/DeployedTask.type';
import { RootDispatch, RootState } from '../store';

const expandNodes = (
  nodes: TaskRun[],
  edges: TaskRunDependency[],
  appendNodes: TaskRun[],
  appendEdges: TaskRunDependency[],
) => {
  appendNodes.forEach(node => {
    if (!nodes?.find(n => n.id === node.id)) {
      nodes.push(node);
    }
  });
  appendEdges.forEach(edge => {
    if (
      !edges?.find(
        n => n.downStreamTaskRunId === edge.downStreamTaskRunId && n.upstreamTaskRunId === edge.upstreamTaskRunId,
      )
    ) {
      edges.push(edge);
    }
  });
  return {
    newNodes: nodes,
    newEdges: edges,
  };
};

const expandTaskNodes = (
  nodes: DeployedTask[],
  edges: DeployedTaskDependency[],
  appendNodes: DeployedTask[],
  appendEdges: DeployedTaskDependency[],
) => {
  appendNodes.forEach(node => {
    if (!nodes?.find(n => n.id === node.id)) {
      nodes.push(node);
    }
  });
  appendEdges.forEach(edge => {
    if (!edges?.find(n => n.downStreamTaskId === edge.downStreamTaskId && n.upstreamTaskId === edge.upstreamTaskId)) {
      edges.push(edge);
    }
  });
  return {
    newNodes: nodes,
    newEdges: edges,
  };
};

export interface DagState {
  nodes: TaskRun[] | DeployedTask[];
  edges: TaskRunDependency[] | DeployedTaskDependency[];
  expandUpstreamNodeIds: string[];
  expandDownstreamNodeIds: string[];
  currentClickId: string | null;
}
export const dag = {
  state: {
    nodes: [],
    edges: [],
    expandUpstreamNodeIds: [],
    expandDownstreamNodeIds: [],
    currentClickId: null,
  } as DagState,
  reducers: {
    updateState: produce((state: DagState, payload: Partial<DagState>) => ({
      ...state,
      ...payload,
    })),
  },
  effects: (dispatch: RootDispatch) => {
    return {
      // taskRunDag
      async queryTaskRunDag(taskRunId: string) {
        const res = await fetchTaskRunDAG(taskRunId, {
          upstreamLevel: 1,
          downstreamLevel: 1,
        });
        if (res) {
          dispatch.dag.updateState({
            nodes: res?.nodes || [],
            edges: res?.edges || [],
            expandUpstreamNodeIds: [taskRunId],
            expandDownstreamNodeIds: [taskRunId],
          });
        }
        return res;
      },
      async expandUpstreamTaskRunDAG(taskRunId: string, state: RootState) {
        const { nodes, edges, expandUpstreamNodeIds }: DagState = cloneDeep(state.dag);
        const res = await fetchTaskRunDAG(taskRunId, {
          upstreamLevel: 1,
          downstreamLevel: 0,
        });
        if (res) {
          const { newNodes, newEdges } = expandNodes(nodes, edges, res.nodes, res.edges);
          dispatch.dag.updateState({
            nodes: newNodes,
            edges: newEdges,
            expandUpstreamNodeIds: [taskRunId, ...expandUpstreamNodeIds],
          });
        }
        return res;
      },
      async expandDownstreamTaskRunDAG(taskRunId: string, state: RootState) {
        const { nodes, edges, expandDownstreamNodeIds }: DagState = cloneDeep(state.dag);
        const res = await fetchTaskRunDAG(taskRunId, {
          upstreamLevel: 0,
          downstreamLevel: 1,
        });
        if (res) {
          const { newNodes, newEdges } = expandNodes(nodes, edges, res.nodes, res.edges);
          dispatch.dag.updateState({
            nodes: newNodes,
            edges: newEdges,
            expandDownstreamNodeIds: [taskRunId, ...expandDownstreamNodeIds],
          });
        }
        return res;
      },
      closeUpstreamTaskRunDag(id: string, state: RootState) {
        const { nodes, edges, expandUpstreamNodeIds }: DagState = cloneDeep(state.dag);
        edges.forEach(edge => {
          if (edge.downStreamTaskRunId === id) {
            const deleteIndex = nodes.findIndex(node => node.id === edge.upstreamTaskRunId);
            if (deleteIndex > -1) {
              nodes.splice(deleteIndex, 1);
            }
          }
        });
        const deleteIndex = expandUpstreamNodeIds.findIndex(expandUpstreamNodeId => expandUpstreamNodeId === id);
        expandUpstreamNodeIds.splice(deleteIndex, 1);
        dispatch.dag.updateState({
          nodes,
          expandUpstreamNodeIds,
        });
      },
      closeDownstreamTaskRunDag(id: string, state: RootState) {
        const { nodes, edges, expandDownstreamNodeIds }: DagState = cloneDeep(state.dag);
        edges.forEach(edge => {
          if (edge.upstreamTaskRunId === id) {
            const deleteIndex = nodes.findIndex(node => node.id === edge.downStreamTaskRunId);
            if (deleteIndex > -1) {
              nodes.splice(deleteIndex, 1);
            }
          }
        });
        const deleteIndex = expandDownstreamNodeIds.findIndex(expandDownstreamNodeId => expandDownstreamNodeId === id);
        expandDownstreamNodeIds.splice(deleteIndex, 1);
        dispatch.dag.updateState({
          nodes,
          expandDownstreamNodeIds,
        });
      },

      // taskDag
      async queryTaskDag(taskId: string) {
        const res = await fetchDeployedTaskDAG(taskId, {
          upstreamLevel: 1,
          downstreamLevel: 1,
        });
        if (res) {
          dispatch.dag.updateState({
            nodes: res?.nodes || [],
            edges: res?.edges || [],
            expandUpstreamNodeIds: [taskId],
            expandDownstreamNodeIds: [taskId],
          });
        }
        return res;
      },
      async expandUpstreamTaskDAG(taskId: string, state: RootState) {
        const { nodes, edges, expandUpstreamNodeIds }: DagState = cloneDeep(state.dag);
        const res = await fetchDeployedTaskDAG(taskId, {
          upstreamLevel: 1,
          downstreamLevel: 0,
        });
        if (res) {
          const { newNodes, newEdges } = expandTaskNodes(nodes, edges, res.nodes, res.edges);
          dispatch.dag.updateState({
            nodes: newNodes,
            edges: newEdges,
            expandUpstreamNodeIds: [taskId, ...expandUpstreamNodeIds],
          });
        }
        return res;
      },
      async expandDownstreamTaskDAG(taskId: string, state: RootState) {
        const { nodes, edges, expandDownstreamNodeIds }: DagState = cloneDeep(state.dag);
        const res = await fetchDeployedTaskDAG(taskId, {
          upstreamLevel: 0,
          downstreamLevel: 1,
        });
        if (res) {
          const { newNodes, newEdges } = expandTaskNodes(nodes, edges, res.nodes, res.edges);
          dispatch.dag.updateState({
            nodes: newNodes,
            edges: newEdges,
            expandDownstreamNodeIds: [taskId, ...expandDownstreamNodeIds],
          });
        }
        return res;
      },
      closeUpstreamTaskDag(id: string, state: RootState) {
        const { nodes, edges, expandUpstreamNodeIds }: DagState = cloneDeep(state.dag);
        edges.forEach(edge => {
          if (edge.downStreamTaskId === id) {
            const deleteIndex = nodes.findIndex(node => node.id === edge.upstreamTaskId);
            if (deleteIndex > -1) {
              nodes.splice(deleteIndex, 1);
            }
          }
        });
        const deleteIndex = expandUpstreamNodeIds.findIndex(expandUpstreamNodeId => expandUpstreamNodeId === id);
        expandUpstreamNodeIds.splice(deleteIndex, 1);
        dispatch.dag.updateState({
          nodes,
          expandUpstreamNodeIds,
        });
      },
      closeDownstreamTaskDag(id: string, state: RootState) {
        const { nodes, edges, expandDownstreamNodeIds }: DagState = cloneDeep(state.dag);
        edges.forEach(edge => {
          if (edge.upstreamTaskId === id) {
            const deleteIndex = nodes.findIndex(node => node.id === edge.downStreamTaskId);
            if (deleteIndex > -1) {
              nodes.splice(deleteIndex, 1);
            }
          }
        });
        const deleteIndex = expandDownstreamNodeIds.findIndex(expandDownstreamNodeId => expandDownstreamNodeId === id);
        expandDownstreamNodeIds.splice(deleteIndex, 1);
        dispatch.dag.updateState({
          nodes,
          expandDownstreamNodeIds,
        });
      },
    };
  },
};
