import React, { FunctionComponent, useEffect, useMemo, useState, useCallback } from 'react';
import uniqueId from 'lodash/uniqueId';
import { DAGTaskGraph } from '@/components/DAGGraph';
import { KunSpin } from '@/components/KunSpin';
import { TaskRun } from '@/definitions/TaskRun.type';
import { TaskNode, TaskRelation } from '@/components/DAGGraph/typings';
import { DagState } from '@/rematch/models/dag';
import useRedux from '@/hooks/useRedux';

interface OwnProps {
  taskRun: TaskRun | null;
  width?: number;
  height?: number;
}

type Props = OwnProps;

export const TaskRunDAG: FunctionComponent<Props> = props => {
  const { taskRun, width, height } = props;
  const [loading, setLoading] = useState(false);
  const { selector, dispatch } = useRedux<DagState>(state => state.dag);
  const dagData = selector;

  useEffect(() => {
    if (taskRun) {
      setLoading(true);
      dispatch.dag.queryTaskRunDag(taskRun.id).finally(() => {
        setLoading(false);
      });
    }
  }, [taskRun, dispatch]);

  const expandUpstreamDAG = useCallback(
    async id => {
      return dispatch.dag.expandUpstreamTaskRunDAG(id);
    },
    [dispatch],
  );

  const expandDownstreamDAG = useCallback(
    async id => {
      return dispatch.dag.expandDownstreamTaskRunDAG(id);
    },
    [dispatch],
  );

  const closeUpstreamDag = useCallback(
    id => {
      return dispatch.dag.closeUpstreamTaskRunDag(id);
    },
    [dispatch],
  );

  const closeDownstreamDag = useCallback(
    id => {
      dispatch.dag.closeDownstreamTaskRunDag(id);
    },
    [dispatch],
  );

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
          expandUpstreamDAG={expandUpstreamDAG}
          expandDownstreamDAG={expandDownstreamDAG}
          closeUpstreamDag={closeUpstreamDag}
          closeDownstreamDag={closeDownstreamDag}
          centerTaskId={taskRun?.id as string | undefined}
        />
      );
    }
    // else
    return <></>;
  }, [
    loading,
    dagData,
    width,
    height,
    taskRun?.id,
    expandUpstreamDAG,
    expandDownstreamDAG,
    closeUpstreamDag,
    closeDownstreamDag,
  ]);

  return <div data-tid="deployed-task-dag">{DAGDom}</div>;
};
