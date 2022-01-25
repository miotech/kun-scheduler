import React, { FunctionComponent, useEffect, useMemo } from 'react';
import uniqueId from 'lodash/uniqueId';
import { useRequest } from 'ahooks';
import { DAGTaskGraph } from '@/components/DAGGraph';
import { KunSpin } from '@/components/KunSpin';
import { fetchTaskRunDAG, FetchTaskRunDAGOptionParams } from '@/services/task-deployments/deployed-tasks';

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
            failedUpstreamTaskRuns: taskrun?.failedUpstreamTaskRuns?.map(i => ({
              id: i.id,
              name: i.task.name,
            })),
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
