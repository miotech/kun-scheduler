import React, { FunctionComponent, useEffect, useMemo } from 'react';
import uniqueId from 'lodash/uniqueId';
import get from 'lodash/get';
import { useRequest } from 'ahooks';
import { DAGTaskGraph } from '@/components/DAGGraph';
import { KunSpin } from '@/components/KunSpin';
import {
  fetchTaskRunDAG,
  FetchTaskRunDAGOptionParams,
  getTaskDefinitionIdByWorkflowIds,
} from '@/services/task-deployments/deployed-tasks';

import { TaskRun } from '@/definitions/TaskRun.type';
import { TaskNode, TaskRelation } from '@/components/DAGGraph/typings';

interface OwnProps {
  taskRun: TaskRun | null;
  width?: number;
  height?: number;
}

type Props = OwnProps;

export const TaskRunDAG: FunctionComponent<Props> = props => {
  const { taskRun, width, height } = props;

  const { data: dagData, loading, run: fetchDAGFromRemote } = useRequest(
    async (taskRunId: string, optionParams: FetchTaskRunDAGOptionParams = {}) => {
      const data = await fetchTaskRunDAG(taskRunId, optionParams);
      if (data) {
        const workflowTaskIdToTaskDefIdMap = await getTaskDefinitionIdByWorkflowIds(
          data.nodes.map(n => `${n.task.id}` || '') || [],
        );
        data.nodes = data.nodes.map(n => ({ ...n, taskDefinitionId: workflowTaskIdToTaskDefIdMap[n.task.id] }));
      }
      return data;
    },
    {
      manual: true,
    },
  );

  useEffect(() => {
    if (taskRun) {
      fetchDAGFromRemote(taskRun.id, {
        upstreamLevel: 3,
        downstreamLevel: 3,
      });
    }
  }, [fetchDAGFromRemote, taskRun]);

  const DAGDom = useMemo(() => {
    if (loading) {
      return <KunSpin asBlock />;
    }
    if (dagData) {
      const nodes: TaskNode[] = dagData.nodes.map(taskrun => {
        return {
          id: taskrun.id,
          name: taskrun.task?.name || '',
          data: {
            renderAsTaskRunDAG: true,
            id: taskrun.id,
            status: taskrun?.status,
            startTime: taskrun.startAt,
            endTime: taskrun.endAt,
            taskRunId: taskrun.id,
            taskDefinitionId: get(taskrun, 'taskDefinitionId', null),
          } as any,
        };
      });
      const edges: TaskRelation[] = dagData.edges.map(edge => ({
        id: uniqueId(),
        upstreamTaskId: edge.upstreamTaskRunId,
        downstreamTaskId: edge.downStreamTaskRunId,
      }));
      return (
        <DAGTaskGraph
          width={width}
          height={height}
          nodes={nodes || []}
          relations={edges || []}
          centerTaskId={taskRun?.id as string | undefined}
        />
      );
    }
    // else
    return <></>;
  }, [loading, dagData, width, height, taskRun?.id]);

  return <div data-tid="deployed-task-dag">{DAGDom}</div>;
};
