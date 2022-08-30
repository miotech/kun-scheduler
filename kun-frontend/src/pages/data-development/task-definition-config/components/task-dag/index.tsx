import React, { FunctionComponent, useEffect, useMemo, useCallback, useState } from 'react';
import uniqueId from 'lodash/uniqueId';
import { DAGTaskGraph } from '@/components/DAGGraph';
import { KunSpin } from '@/components/KunSpin';
import { DeployedTask } from '@/definitions/DeployedTask.type';
import { TaskNode, TaskRelation } from '@/components/DAGGraph/typings';
import useRedux from '@/hooks/useRedux';
import { DagState } from '@/rematch/models/dag';

interface OwnProps {
  task: DeployedTask | null;
  width?: number;
  height?: number;
}

type Props = OwnProps;

const DeployedTaskDAG: FunctionComponent<Props> = props => {
  const { task, width, height } = props;
  const { selector, dispatch } = useRedux<DagState>(state => state.dag);
  const [loading, setLoading] = useState(false);

  const dagData = selector;

  useEffect(() => {
    if (task) {
      setLoading(true);
      dispatch.dag.queryTaskDag(task.id).finally(() => {
        setLoading(false);
      });
    }
  }, [task, dispatch]);

  const expandUpstreamDAG = useCallback(
    async id => {
      return dispatch.dag.expandUpstreamTaskDAG(id);
    },
    [dispatch],
  );

  const expandDownstreamDAG = useCallback(
    async id => {
      return dispatch.dag.expandDownstreamTaskDAG(id);
    },
    [dispatch],
  );

  const closeUpstreamDag = useCallback(
    id => {
      return dispatch.dag.closeUpstreamTaskDag(id);
    },
    [dispatch],
  );

  const closeDownstreamDag = useCallback(
    id => {
      dispatch.dag.closeDownstreamTaskDag(id);
    },
    [dispatch],
  );

  const DAGDom = useMemo(() => {
    if (loading) {
      return <KunSpin asBlock />;
    }
    if (dagData) {
      const nodes: TaskNode[] = dagData.nodes.map(item => {
        return {
          id: item.id,
          name: item.name,
          data: {
            id: item.id,
            name: item.name,
          },
        };
      });
      const edges: TaskRelation[] = dagData.edges.map(edge => ({
        id: uniqueId(),
        upstreamTaskId: edge.upstreamTaskId,
        downstreamTaskId: edge.downStreamTaskId,
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
          centerTaskId={task?.id as string | undefined}
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
    expandUpstreamDAG,
    expandDownstreamDAG,
    closeUpstreamDag,
    closeDownstreamDag,
    task,
  ]);

  return (
    <div data-tid="deployed-task-dag" style={{ position: 'relative', width }}>
      {DAGDom}
    </div>
  );
};

export default DeployedTaskDAG;
